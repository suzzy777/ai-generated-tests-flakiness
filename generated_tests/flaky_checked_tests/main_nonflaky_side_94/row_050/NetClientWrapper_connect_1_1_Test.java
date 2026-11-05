package org.apache.servicecomb.foundation.vertx.client.tcp;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetSocket;
import org.apache.servicecomb.foundation.vertx.client.tcp.TcpClientConfig;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class NetClientWrapper_connect_1_1_Test {

    private Vertx vertx;

    private TcpClientConfig normalClientConfig;

    private TcpClientConfig sslClientConfig;

    @Mock
    private NetClient normalNetClient;

    @Mock
    private NetClient sslNetClient;

    private NetClientWrapper netClientWrapper;

    @BeforeEach
    public void setUp() {
        vertx = Vertx.vertx();
        normalClientConfig = new TcpClientConfig();
        sslClientConfig = new TcpClientConfig();
        netClientWrapper = new NetClientWrapper(vertx, normalClientConfig, sslClientConfig);
        setPrivateField(netClientWrapper, "normalNetClient", normalNetClient);
        setPrivateField(netClientWrapper, "sslNetClient", sslNetClient);
    }

    private void setPrivateField(Object target, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testConnectWithoutSsl() {
        Handler<AsyncResult<NetSocket>> handler = mock(Handler.class);
        netClientWrapper.connect(false, 8080, "localhost", handler);
        verify(normalNetClient).connect(eq(8080), eq("localhost"), eq(handler));
    }

    @Test
    public void testConnectWithSsl() {
        Handler<AsyncResult<NetSocket>> handler = mock(Handler.class);
        netClientWrapper.connect(true, 8443, "localhost", handler);
        verify(sslNetClient).connect(eq(8443), eq("localhost"), eq(handler));
    }
}
