package com.unitynews.news.data.aidl

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Binder
import android.os.IBinder
import com.unitynews.contract.ArticleFilterRequest
import com.unitynews.contract.BackendStatusDto
import com.unitynews.contract.IArticlesCallback
import com.unitynews.contract.IBackendStatusCallback
import com.unitynews.contract.IFilterSpecsCallback
import com.unitynews.contract.INewsBackendService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
@OptIn(ExperimentalCoroutinesApi::class)
class BackendConnectionTest {
    private val componentName = ComponentName("com.example.unitynewsbackend", "NewsBackendService")

    @Test
    fun `concurrent connect calls share one bind attempt`() = runTest {
        val service = FakeConnectionBackendService()
        val binder = FakeServiceBinder(serviceFactory = { service })
        val connection = AndroidBackendConnection(serviceBinder = binder)

        val first = async { connection.connect() }
        val second = async { connection.connect() }
        runCurrent()

        assertEquals(1, binder.bindCalls)
        binder.lastConnection.onServiceConnected(componentName, service.asBinder())

        assertSame(service, first.await())
        assertSame(service, second.await())
        assertEquals(1, binder.bindCalls)
    }

    @Test
    fun `null binding unbinds active bind attempt and fails connect`() = runTest {
        val binder = FakeServiceBinder()
        val connection = AndroidBackendConnection(serviceBinder = binder)

        val result = async { runCatching { connection.connect() } }
        runCurrent()

        binder.lastConnection.callServiceConnectionCallback("onNullBinding", componentName)

        assertTrue(result.await().isFailure)
        assertEquals(1, binder.unbindCalls)
    }

    @Test
    fun `dead binding unbinds active bind attempt and fails connect`() = runTest {
        val binder = FakeServiceBinder()
        val connection = AndroidBackendConnection(serviceBinder = binder)

        val result = async { runCatching { connection.connect() } }
        runCurrent()

        binder.lastConnection.callServiceConnectionCallback("onBindingDied", componentName)

        assertTrue(result.await().isFailure)
        assertEquals(1, binder.unbindCalls)
    }

    @Test
    fun `unusable connected binder unbinds active bind attempt and fails connect`() = runTest {
        val binder = FakeServiceBinder(serviceFactory = { null })
        val connection = AndroidBackendConnection(serviceBinder = binder)

        val result = async { runCatching { connection.connect() } }
        runCurrent()

        binder.lastConnection.onServiceConnected(componentName, Binder())

        assertTrue(result.await().isFailure)
        assertEquals(1, binder.unbindCalls)
    }

    @Test
    fun `service disconnected unbinds old connection before reconnecting`() = runTest {
        val firstService = FakeConnectionBackendService()
        val secondService = FakeConnectionBackendService()
        val services = ArrayDeque(listOf(firstService, secondService))
        val binder = FakeServiceBinder(serviceFactory = { services.removeFirst() })
        val connection = AndroidBackendConnection(serviceBinder = binder)

        val firstConnect = async { connection.connect() }
        runCurrent()
        val firstConnection = binder.lastConnection
        firstConnection.onServiceConnected(componentName, firstService.asBinder())
        assertSame(firstService, firstConnect.await())

        firstConnection.onServiceDisconnected(componentName)
        assertEquals(1, binder.unbindCalls)

        val secondConnect = async { connection.connect() }
        runCurrent()
        assertEquals(2, binder.bindCalls)
        binder.lastConnection.onServiceConnected(componentName, secondService.asBinder())

        assertSame(secondService, secondConnect.await())
        assertEquals(1, binder.unbindCalls)
    }

    @Test
    fun `stale service disconnected callback does not clear newer cached service`() = runTest {
        val firstService = FakeConnectionBackendService()
        val secondService = FakeConnectionBackendService()
        val thirdService = FakeConnectionBackendService()
        val services = ArrayDeque(listOf(firstService, secondService, thirdService))
        val binder = FakeServiceBinder(serviceFactory = { services.removeFirst() })
        val connection = AndroidBackendConnection(serviceBinder = binder)

        val firstConnect = async { connection.connect() }
        runCurrent()
        val firstConnection = binder.lastConnection
        firstConnection.onServiceConnected(componentName, firstService.asBinder())
        assertSame(firstService, firstConnect.await())

        firstConnection.onServiceDisconnected(componentName)
        val secondConnect = async { connection.connect() }
        runCurrent()
        binder.lastConnection.onServiceConnected(componentName, secondService.asBinder())
        assertSame(secondService, secondConnect.await())
        assertEquals(2, binder.bindCalls)
        assertEquals(1, binder.unbindCalls)

        firstConnection.onServiceDisconnected(componentName)
        val cachedConnect = async { connection.connect() }
        runCurrent()
        if (binder.bindCalls > 2) {
            binder.lastConnection.onServiceConnected(componentName, thirdService.asBinder())
        }

        assertSame(secondService, cachedConnect.await())
        assertEquals(2, binder.bindCalls)
        assertEquals(1, binder.unbindCalls)
    }
}

private fun ServiceConnection.callServiceConnectionCallback(methodName: String, name: ComponentName) {
    javaClass.getMethod(methodName, ComponentName::class.java).invoke(this, name)
}

private class FakeServiceBinder(
    private val serviceFactory: (IBinder) -> INewsBackendService? = { FakeConnectionBackendService() },
) : ServiceBinder {
    var bindCalls = 0
        private set
    var unbindCalls = 0
        private set
    lateinit var lastConnection: ServiceConnection
        private set

    override fun bind(intent: Intent, connection: ServiceConnection, flags: Int): Boolean {
        bindCalls += 1
        lastConnection = connection
        return true
    }

    override fun unbind(connection: ServiceConnection) {
        unbindCalls += 1
    }

    override fun asService(binder: IBinder): INewsBackendService? =
        serviceFactory(binder)
}

private class FakeConnectionBackendService : INewsBackendService.Stub() {
    override fun getApiVersion(): Int = 2

    override fun getFilterSpecs(callback: IFilterSpecsCallback) {
        callback.onSuccess(emptyList())
    }

    override fun getArticles(request: ArticleFilterRequest, callback: IArticlesCallback) {
        callback.onSuccess(emptyList())
    }

    override fun getBackendStatus(callback: IBackendStatusCallback) {
        callback.onSuccess(BackendStatusDto(isRunning = true, scenario = "test", articleCount = 0))
    }
}
