package org.apache.dubbo.rpc.protocol.dubbo.telnet;

import org.apache.dubbo.remoting.Channel;
import org.apache.dubbo.remoting.exchange.ExchangeChannel;
import org.apache.dubbo.remoting.exchange.ExchangeServer;
import org.apache.dubbo.rpc.protocol.dubbo.DubboProtocol;
import org.junit.Before;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class PortTelnetHandler_telnet_0_0_Test {
    private PortTelnetHandler handler;
    private Channel mockChannel;
    private ExchangeServer mockServer;
    private ExchangeChannel mockExchangeChannel;

    @Before
    public void setUp() throws Exception {
        handler = new PortTelnetHandler();
        mockChannel = mock(Channel.class);
        mockServer = mock(ExchangeServer.class);
        mockExchangeChannel = mock(ExchangeChannel.class);

        when(mockServer.getUrl()).thenReturn(new org.apache.dubbo.common.URL("dubbo", "localhost", 20880));
        when(mockExchangeChannel.getRemoteAddress()).thenReturn(new InetSocketAddress("127.0.0.1", 12345));
        when(mockExchangeChannel.getLocalAddress()).thenReturn(new InetSocketAddress("127.0.0.1", 20880));

        // Use reflection to set the INSTANCE field of DubboProtocol
        DubboProtocol mockProtocol = mock(DubboProtocol.class);
        java.lang.reflect.Field instanceField = DubboProtocol.class.getDeclaredField("INSTANCE");
        instanceField.setAccessible(true);
        instanceField.set(null, mockProtocol);

        when(mockProtocol.getServers()).thenReturn(Collections.singletonList(mockServer));
    }

    @Test
    public void testTelnetNoPort() throws Exception {
        String result = handler.telnet(mockChannel, "");
        assertEquals("20880", result);
    }

    @Test
    public void testTelnetWithPort() throws Exception {
        when(mockServer.getExchangeChannels()).thenReturn(Collections.singletonList(mockExchangeChannel));

        String result = handler.telnet(mockChannel, "20880");
        assertEquals("/127.0.0.1:12345", result);
    }

    @Test
    public void testTelnetWithIllegalPort() throws Exception {
        String result = handler.telnet(mockChannel, "notaport");
        assertEquals("Illegal port notaport, must be integer.", result);
    }

    @Test
    public void testTelnetWithNonExistingPort() throws Exception {
        String result = handler.telnet(mockChannel, "12345");
        assertEquals("No such port 12345", result);
    }
}
