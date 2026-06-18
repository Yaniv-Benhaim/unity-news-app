package com.unitynews.news.data.aidl

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.unitynews.contract.INewsBackendService
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CancellationException

interface BackendConnection {
    suspend fun connect(): INewsBackendService

    fun disconnect()
}

interface ServiceBinder {
    fun bind(intent: Intent, connection: ServiceConnection, flags: Int): Boolean

    fun unbind(connection: ServiceConnection)

    fun asService(binder: IBinder): INewsBackendService?
}

class AndroidServiceBinder(
    context: Context,
) : ServiceBinder {
    private val applicationContext = context.applicationContext

    override fun bind(intent: Intent, connection: ServiceConnection, flags: Int): Boolean =
        applicationContext.bindService(intent, connection, flags)

    override fun unbind(connection: ServiceConnection) {
        applicationContext.unbindService(connection)
    }

    override fun asService(binder: IBinder): INewsBackendService? =
        INewsBackendService.Stub.asInterface(binder)
}

class AndroidBackendConnection(
    private val serviceBinder: ServiceBinder,
    private val backendPackageName: String = BackendAvailabilityChecker.DEFAULT_BACKEND_PACKAGE,
    private val serviceAction: String = DEFAULT_SERVICE_ACTION,
    private val bindFlags: Int = Context.BIND_AUTO_CREATE,
) : BackendConnection {
    constructor(
        context: Context,
        backendPackageName: String = BackendAvailabilityChecker.DEFAULT_BACKEND_PACKAGE,
        serviceAction: String = DEFAULT_SERVICE_ACTION,
        bindFlags: Int = Context.BIND_AUTO_CREATE,
    ) : this(
        serviceBinder = AndroidServiceBinder(context.applicationContext),
        backendPackageName = backendPackageName,
        serviceAction = serviceAction,
        bindFlags = bindFlags,
    )

    private val lock = Any()
    private var service: INewsBackendService? = null
    private var serviceConnection: ServiceConnection? = null
    private var inFlight: BindingAttempt? = null

    override suspend fun connect(): INewsBackendService {
        val attemptToStart: BindingAttempt?
        val deferred = synchronized(lock) {
            service?.let { return it }
            inFlight?.let {
                attemptToStart = null
                return@synchronized it.deferred
            }

            createBindingAttempt().also { attempt ->
                inFlight = attempt
                serviceConnection = attempt.connection
                attemptToStart = attempt
            }.deferred
        }

        attemptToStart?.start()
        return deferred.await()
    }

    override fun disconnect() {
        val activeAttempt: BindingAttempt?
        val activeConnection: ServiceConnection?
        synchronized(lock) {
            service = null
            activeAttempt = inFlight
            activeConnection = serviceConnection
            inFlight = null
            serviceConnection = null
        }

        activeAttempt?.deferred?.cancel(CancellationException("Backend connection disconnected"))
        activeConnection?.let { serviceBinder.unbindSafely(it) }
    }

    private fun createBindingAttempt(): BindingAttempt {
        val deferred = CompletableDeferred<INewsBackendService>()
        lateinit var connection: ServiceConnection
        lateinit var attempt: BindingAttempt

        connection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName, binder: IBinder) {
                val connectedService = serviceBinder.asService(binder)
                if (connectedService == null) {
                    failBinding(attempt, "Backend service binder was null", shouldUnbind = true)
                    return
                }

                val shouldUnbind = synchronized(lock) {
                    if (inFlight === attempt && serviceConnection === connection) {
                        service = connectedService
                        inFlight = null
                        false
                    } else {
                        true
                    }
                }

                if (shouldUnbind) {
                    serviceBinder.unbindSafely(connection)
                } else {
                    deferred.complete(connectedService)
                }
            }

            override fun onServiceDisconnected(name: ComponentName) {
                synchronized(lock) {
                    service = null
                }
            }

            override fun onBindingDied(name: ComponentName) {
                failBinding(attempt, "Backend service binding died", shouldUnbind = true)
            }

            override fun onNullBinding(name: ComponentName) {
                failBinding(attempt, "Backend service returned a null binding", shouldUnbind = true)
            }
        }

        attempt = BindingAttempt(
            intent = Intent(serviceAction).setPackage(backendPackageName),
            connection = connection,
            flags = bindFlags,
            deferred = deferred,
        )
        return attempt
    }

    private fun BindingAttempt.start() {
        try {
            val bound = serviceBinder.bind(intent, connection, flags)
            if (!bound) {
                failBinding(this, "Unable to bind backend service", shouldUnbind = false)
            }
        } catch (error: CancellationException) {
            throw error
        } catch (error: Throwable) {
            failBinding(this, error, shouldUnbind = false)
        }
    }

    private fun failBinding(
        attempt: BindingAttempt,
        message: String,
        shouldUnbind: Boolean,
    ) {
        failBinding(attempt, IllegalStateException(message), shouldUnbind)
    }

    private fun failBinding(
        attempt: BindingAttempt,
        error: Throwable,
        shouldUnbind: Boolean,
    ) {
        val unbindConnection = synchronized(lock) {
            if (inFlight === attempt) {
                inFlight = null
            }
            if (serviceConnection === attempt.connection) {
                service = null
                serviceConnection = null
                attempt.connection.takeIf { shouldUnbind }
            } else {
                null
            }
        }

        unbindConnection?.let { serviceBinder.unbindSafely(it) }
        attempt.deferred.completeExceptionally(error)
    }

    private fun ServiceBinder.unbindSafely(connection: ServiceConnection) {
        runCatching { unbind(connection) }
    }

    companion object {
        const val DEFAULT_SERVICE_ACTION = "com.unitynews.contract.INewsBackendService"
    }
}

private data class BindingAttempt(
    val intent: Intent,
    val connection: ServiceConnection,
    val flags: Int,
    val deferred: CompletableDeferred<INewsBackendService>,
)
