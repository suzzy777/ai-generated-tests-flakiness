package org.apache.dubbo.rpc.protocol.dubbo.telnet;

import org.apache.dubbo.remoting.Channel;
import org.apache.dubbo.remoting.exchange.ExchangeChannel;
import org.apache.dubbo.remoting.exchange.ExchangeServer;
import org.apache.dubbo.rpc.protocol.dubbo.DubboProtocol;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class PortTelnetHandler_telnet_0_0_Test {

    private PortTelnetHandler portTelnetHandler;
    private Channel mockChannel;
    private ExchangeServer mockServer;
    private ExchangeChannel mockExchangeChannel;

    @Before
    public void setUp() throws Exception {
        portTelnetHandler = new PortTelnetHandler();
        mockChannel = mock(Channel.class);
        mockServer = mock(ExchangeServer.class);
        mockExchangeChannel = mock(ExchangeChannel.class);

        // Set up mock server
        when(mockServer.getUrl()).thenReturn(new org.apache.dubbo.common.URL("dubbo", "localhost", 20880));
        when(mockServer.getExchangeChannels()).thenReturn(Collections.singletonList(mockExchangeChannel));

        // Set up mock exchange channel
        when(mockExchangeChannel.getRemoteAddress()).thenReturn(new InetSocketAddress("127.0.0.1", 12345));
        when(mockExchangeChannel.getLocalAddress()).thenReturn(new InetSocketAddress("127.0.0.1", 20880));

        // Reflectively set the DubboProtocol instance
        Field instanceField = DubboProtocol.class.getDeclaredField("INSTANCE");
        instanceField.setAccessible(true);
        DubboProtocol mockProtocol = mock(DubboProtocol.class);
        when(mockProtocol.getServers()).thenReturn(Collections.singletonList(mockServer));
        instanceField.set(null, mockProtocol);
    }

    @Test
    public void testTelnetWithNoMessage() throws Exception {
        String result = portTelnetHandler.telnet(mockChannel, "");
        assertEquals("20880", result);
    }

    @Test
    public void testTelnetWithDetailFlag() throws Exception {
        String result = portTelnetHandler.telnet(mockChannel, "-l");
        assertEquals("dubbo://localhost:20880", result);
    }

    @Test
    public void testTelnetWithSpecificPort() throws Exception {
        String result = portTelnetHandler.telnet(mockChannel, "20880");
        assertEquals("127.0.0.1:12345", result);
    }

    @Test
    public void testTelnetWithSpecificPortAndDetailFlag() throws Exception {
        String result = portTelnetHandler.telnet(mockChannel, "-l 20880");
        assertEquals("127.0.0.1:12345 -> 127.0.0.1:20880", result);
    }

    @Test
    public void testTelnetWithIllegalPort() throws Exception {
        String result = portTelnetHandler.telnet(mockChannel, "abc");
        assertEquals("Illegal port abc, must be integer.", result);
    }

    @Test
    public void testTelnetWithNonExistentPort() throws Exception {
        String result = portTelnetHandler.telnet(mockChannel, "12345");
        assertEquals("No such port 12345", result);
    }
}
