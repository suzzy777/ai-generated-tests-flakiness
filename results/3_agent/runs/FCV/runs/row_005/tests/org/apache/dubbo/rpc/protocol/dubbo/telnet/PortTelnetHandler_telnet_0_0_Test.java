package org.apache.dubbo.rpc.protocol.dubbo.telnet;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.remoting.Channel;
import org.apache.dubbo.remoting.exchange.ExchangeChannel;
import org.apache.dubbo.remoting.exchange.ExchangeServer;
import org.apache.dubbo.rpc.protocol.dubbo.DubboProtocol;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class PortTelnetHandler_telnet_0_0_Test {
    private PortTelnetHandler handler;
    private DubboProtocol originalDubboProtocol;

    @Before
    public void setUp() throws Exception {
        handler = new PortTelnetHandler();

        // Save the original DubboProtocol instance
        Field instanceField = DubboProtocol.class.getDeclaredField("INSTANCE");
        instanceField.setAccessible(true);
        originalDubboProtocol = (DubboProtocol) instanceField.get(null);

        // Mock DubboProtocol
        DubboProtocol mockProtocol = mock(DubboProtocol.class);
        instanceField.set(null, mockProtocol);
    }

    @After
    public void tearDown() throws Exception {
        // Restore the original DubboProtocol instance
        Field instanceField = DubboProtocol.class.getDeclaredField("INSTANCE");
        instanceField.setAccessible(true);
        instanceField.set(null, originalDubboProtocol);
    }

    @Test
    public void testTelnetWithNoMessage() throws Exception {
        DubboProtocol mockProtocol = DubboProtocol.getDubboProtocol();
        ExchangeServer mockServer = mock(ExchangeServer.class);
        when(mockServer.getUrl()).thenReturn(new URL("dubbo", "localhost", 20880));

        when(mockProtocol.getServers()).thenReturn(Collections.singletonList(mockServer));

        Channel mockChannel = mock(Channel.class);

        String result = handler.telnet(mockChannel, "");

        assertEquals("20880", result);
    }

    @Test
    public void testTelnetWithInvalidPort() throws Exception {
        Channel mockChannel = mock(Channel.class);

        String result = handler.telnet(mockChannel, "invalidPort");

        assertEquals("Illegal port invalidPort, must be integer.", result);
    }

    @Test
    public void testTelnetWithValidPortNoDetail() throws Exception {
        DubboProtocol mockProtocol = DubboProtocol.getDubboProtocol();
        ExchangeServer mockServer = mock(ExchangeServer.class);
        when(mockServer.getUrl()).thenReturn(new URL("dubbo", "localhost", 20880));

        ExchangeChannel mockExchangeChannel = mock(ExchangeChannel.class);
        when(mockExchangeChannel.getRemoteAddress()).thenReturn(new InetSocketAddress("remoteHost", 12345));
        when(mockServer.getExchangeChannels()).thenReturn(Collections.singletonList(mockExchangeChannel));

        when(mockProtocol.getServers()).thenReturn(Collections.singletonList(mockServer));

        Channel mockChannel = mock(Channel.class);

        String result = handler.telnet(mockChannel, "20880");

        assertEquals("remoteHost:12345", result);
    }

    @Test
    public void testTelnetWithValidPortWithDetail() throws Exception {
        DubboProtocol mockProtocol = DubboProtocol.getDubboProtocol();
        ExchangeServer mockServer = mock(ExchangeServer.class);
        when(mockServer.getUrl()).thenReturn(new URL("dubbo", "localhost", 20880));

        ExchangeChannel mockExchangeChannel = mock(ExchangeChannel.class);
        when(mockExchangeChannel.getRemoteAddress()).thenReturn(new InetSocketAddress("remoteHost", 12345));
        when(mockExchangeChannel.getLocalAddress()).thenReturn(new InetSocketAddress("127.0.0.1", 20880));
        when(mockServer.getExchangeChannels()).thenReturn(Collections.singletonList(mockExchangeChannel));

        when(mockProtocol.getServers()).thenReturn(Collections.singletonList(mockServer));

        Channel mockChannel = mock(Channel.class);

        String result = handler.telnet(mockChannel, "20880 -l");

        assertEquals("remoteHost:12345 -> /127.0.0.1:20880", result);
    }

    @Test
    public void testTelnetWithNonExistentPort() throws Exception {
        DubboProtocol mockProtocol = DubboProtocol.getDubboProtocol();
        ExchangeServer mockServer = mock(ExchangeServer.class);
        when(mockServer.getUrl()).thenReturn(new URL("dubbo", "localhost", 20880));

        when(mockProtocol.getServers()).thenReturn(Collections.singletonList(mockServer));

        Channel mockChannel = mock(Channel.class);

        String result = handler.telnet(mockChannel, "20881");

        assertEquals("No such port 20881", result);
    }
}
