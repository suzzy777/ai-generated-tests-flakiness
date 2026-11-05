package org.apache.dubbo.rpc.protocol.dubbo.telnet;

import org.apache.dubbo.remoting.Channel;
import org.apache.dubbo.remoting.exchange.ExchangeChannel;
import org.apache.dubbo.remoting.exchange.ExchangeServer;
import org.apache.dubbo.rpc.protocol.dubbo.DubboProtocol;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class PortTelnetHandler_telnet_0_0_Test {

    private PortTelnetHandler handler;
    private DubboProtocol originalProtocolInstance;

    @Before
    public void setUp() throws Exception {
        handler = new PortTelnetHandler();
        originalProtocolInstance = getDubboProtocolInstance();
        setDubboProtocolInstance(mock(DubboProtocol.class));
    }

    @After
    public void tearDown() throws Exception {
        setDubboProtocolInstance(originalProtocolInstance);
    }

    @Test
    public void testTelnet_noPort() throws Exception {
        ExchangeServer server = mock(ExchangeServer.class);
        when(server.getUrl()).thenReturn(new org.apache.dubbo.common.URL("dubbo", "localhost", 20880));
        Collection<ExchangeServer> servers = Collections.singletonList(server);
        when(getDubboProtocolInstance().getServers()).thenReturn(servers);

        String result = handler.telnet(mock(Channel.class), "");
        assertEquals("20880", result);
    }

    @Test
    public void testTelnet_withDetail() throws Exception {
        ExchangeServer server = mock(ExchangeServer.class);
        when(server.getUrl()).thenReturn(new org.apache.dubbo.common.URL("dubbo", "localhost", 20880));
        Collection<ExchangeServer> servers = Collections.singletonList(server);
        when(getDubboProtocolInstance().getServers()).thenReturn(servers);

        String result = handler.telnet(mock(Channel.class), "-l");
        assertEquals("dubbo://localhost:20880", result);
    }

    @Test
    public void testTelnet_specificPort() throws Exception {
        ExchangeServer server = mock(ExchangeServer.class);
        when(server.getUrl()).thenReturn(new org.apache.dubbo.common.URL("dubbo", "localhost", 20881));
        Collection<ExchangeServer> servers = Collections.emptyList();
        when(getDubboProtocolInstance().getServers()).thenReturn(servers);

        String result = handler.telnet(mock(Channel.class), "20881");
        assertEquals("No such port 20881", result);
    }

    private DubboProtocol getDubboProtocolInstance() throws Exception {
        return (DubboProtocol) DubboProtocol.class.getDeclaredMethod("getDubboProtocol").invoke(null);
    }

    private void setDubboProtocolInstance(DubboProtocol instance) throws Exception {
        java.lang.reflect.Field field = DubboProtocol.class.getDeclaredField("INSTANCE");
        field.setAccessible(true);
        field.set(null, instance);
    }
}
