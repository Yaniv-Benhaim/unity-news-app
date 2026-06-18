package com.unitynews.news.data.aidl

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.unitynews.contract.INewsBackendService
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

interface BackendConnection {
    suspend fun connect(): INewsBackendService

    fun disconnect()
}

class AndroidBackendConnection(
    context: Context,
    private val backendPackageName: String = BackendAvailabilityChecker.DEFAULT_BACKEND_PACKAGE,
    private val serviceAction: String = DEFAULT_SERVICE_ACTION,
    private val bindFlags: Int = Context.BIND_AUTO_CREATE,
) : BackendConnection {
    private val applicationContext = context.applicationContext
    private var service: INewsBackendService? = null
    private var serviceConnection: ServiceConnection? = null

    override suspend fun connect(): INewsBackendService {
        service?.let { return it }

        return suspendCancellableCoroutine { continuation ->
            val completed = AtomicBoolean(false)
            lateinit var pendingConnection: ServiceConnection

            fun complete(result: Result<INewsBackendService>) {
                if (completed.compareAndSet(false, true) && continuation.isActive) {
                    result
                        .onSuccess { continuation.resume(it) }
                        .onFailure { continuation.resumeWith(Result.failure(it)) }
                }
            }

            fun fail(message: String) {
                complete(Result.failure(IllegalStateException(message)))
            }

            pendingConnection = object : ServiceConnection {
                override fun onServiceConnected(name: ComponentName, binder: IBinder) {
                    val connectedService = INewsBackendService.Stub.asInterface(binder)
                    if (connectedService == null) {
                        fail("Backend service binder was null")
                        return
                    }
                    if (completed.get() || !continuation.isActive) {
                        runCatching { applicationContext.unbindService(pendingConnection) }
                        return
                    }

                    service = connectedService
                    serviceConnection = pendingConnection
                    complete(Result.success(connectedService))
                }

                override fun onServiceDisconnected(name: ComponentName) {
                    service = null
                }

                override fun onBindingDied(name: ComponentName) {
                    service = null
                    serviceConnection = null
                    fail("Backend service binding died")
                }

                override fun onNullBinding(name: ComponentName) {
                    service = null
                    serviceConnection = null
                    fail("Backend service returned a null binding")
                }
            }

            continuation.invokeOnCancellation {
                if (completed.compareAndSet(false, true)) {
                    runCatching { applicationContext.unbindService(pendingConnection) }
                }
            }

            val intent = Intent(serviceAction).setPackage(backendPackageName)
            try {
                val bound = applicationContext.bindService(intent, pendingConnection, bindFlags)
                if (!bound) {
                    fail("Unable to bind backend service")
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                complete(Result.failure(error))
            }
        }
    }

    override fun disconnect() {
        service = null
        serviceConnection?.let { connection ->
            runCatching { applicationContext.unbindService(connection) }
        }
        serviceConnection = null
    }

    companion object {
        const val DEFAULT_SERVICE_ACTION = "com.unitynews.contract.INewsBackendService"
    }
}
