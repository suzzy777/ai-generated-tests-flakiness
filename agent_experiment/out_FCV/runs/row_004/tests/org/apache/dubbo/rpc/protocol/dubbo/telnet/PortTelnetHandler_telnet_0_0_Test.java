package org.apache.dubbo.rpc.protocol.dubbo.telnet;

import org.apache.dubbo.remoting.Channel;
import org.apache.dubbo.remoting.exchange.ExchangeChannel;
import org.apache.dubbo.remoting.exchange.ExchangeServer;
import org.apache.dubbo.rpc.protocol.dubbo.DubboProtocol;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import org.apache.dubbo.common.URL;

public class PortTelnetHandler_telnet_0_0_Test {

    private PortTelnetHandler handler;
    private DubboProtocol originalDubboProtocolInstance;

    @Before
    public void setUp() throws Exception {
        handler = new PortTelnetHandler();

        // Save the original DubboProtocol instance
        Field instanceField = DubboProtocol.class.getDeclaredField("INSTANCE");
        instanceField.setAccessible(true);
        originalDubboProtocolInstance = (DubboProtocol) instanceField.get(null);

        // Mock DubboProtocol and its behavior
        DubboProtocol mockProtocol = mock(DubboProtocol.class);
        Collection<ExchangeServer> servers = new ArrayList<>();
        ExchangeServer mockServer = mock(ExchangeServer.class);
        ExchangeChannel mockChannel = mock(ExchangeChannel.class);

        when(mockChannel.getRemoteAddress()).thenReturn(new InetSocketAddress("127.0.0.1", 20880));
        when(mockServer.getExchangeChannels()).thenReturn(Collections.singletonList(mockChannel));
        when(mockServer.getUrl()).thenReturn(new URL("dubbo", "127.0.0.1", 20880));
        servers.add(mockServer);
        when(mockProtocol.getServers()).thenReturn(servers);

        // Set the mock instance
        instanceField.set(null, mockProtocol);
    }

    @After
    public void tearDown() throws Exception {
        // Restore the original DubboProtocol instance
        Field instanceField = DubboProtocol.class.getDeclaredField("INSTANCE");
        instanceField.setAccessible(true);
        instanceField.set(null, originalDubboProtocolInstance);
    }

    @Test
    public void testTelnet_NoPort() {
        Channel mockChannel = mock(Channel.class);
        String result = handler.telnet(mockChannel, "");
        assertTrue(result.contains("20880"));
    }

    @Test
    public void testTelnet_WithPort() {
        Channel mockChannel = mock(Channel.class);
        String result = handler.telnet(mockChannel, "20880");
        assertTrue(result.contains("127.0.0.1"));
    }

    @Test
    public void testTelnet_WithInvalidPort() {
        Channel mockChannel = mock(Channel.class);
        String result = handler.telnet(mockChannel, "invalid");
        assertEquals("Illegal port invalid, must be integer.", result);
    }

    @Test
    public void testTelnet_NoSuchPort() {
        Channel mockChannel = mock(Channel.class);
        String result = handler.telnet(mockChannel, "20881");
        assertEquals("No such port 20881", result);
    }

    @Test
    public void testTelnet_WithDetail() {
        Channel mockChannel = mock(Channel.class);
        String result = handler.telnet(mockChannel, "-l");
        assertTrue(result.contains("dubbo://127.0.0.1:20880"));
    }
}
