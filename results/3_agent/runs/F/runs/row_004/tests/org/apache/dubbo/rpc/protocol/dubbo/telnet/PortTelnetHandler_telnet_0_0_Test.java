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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PortTelnetHandler_telnet_0_0_Test {

    private PortTelnetHandler portTelnetHandler;
    private Channel mockChannel;
    private ExchangeServer mockServer;
    private ExchangeChannel mockExchangeChannel;

    @Before
    public void setUp() {
        portTelnetHandler = new PortTelnetHandler();
        mockChannel = mock(Channel.class);
        mockServer = mock(ExchangeServer.class);
        mockExchangeChannel = mock(ExchangeChannel.class);

        // Mocking DubboProtocol singleton
        DubboProtocol dubboProtocol = mock(DubboProtocol.class);
        when(dubboProtocol.getServers()).thenReturn(Collections.singletonList(mockServer));
        setDubboProtocolInstance(dubboProtocol);
    }

    private void setDubboProtocolInstance(DubboProtocol instance) {
        try {
            java.lang.reflect.Field field = DubboProtocol.class.getDeclaredField("INSTANCE");
            field.setAccessible(true);
            field.set(null, instance);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testTelnet_NoMessage() {
        when(mockServer.getUrl()).thenReturn(new org.apache.dubbo.common.URL("dubbo", "localhost", 20880));
        String result = portTelnetHandler.telnet(mockChannel, "");
        assertEquals("20880", result);
    }

    @Test
    public void testTelnet_WithDetail() {
        when(mockServer.getUrl()).thenReturn(new org.apache.dubbo.common.URL("dubbo", "localhost", 20880));
        String result = portTelnetHandler.telnet(mockChannel, "-l");
        assertEquals("dubbo://localhost:20880", result);
    }

    @Test
    public void testTelnet_WithPort_NoDetail() {
        when(mockServer.getUrl()).thenReturn(new org.apache.dubbo.common.URL("dubbo", "localhost", 20880));
        when(mockServer.getExchangeChannels()).thenReturn(Collections.singletonList(mockExchangeChannel));
        when(mockExchangeChannel.getRemoteAddress()).thenReturn(new InetSocketAddress("127.0.0.1", 12345));

        String result = portTelnetHandler.telnet(mockChannel, "20880");
        assertEquals("/127.0.0.1:12345", result);
    }

    @Test
    public void testTelnet_WithPort_WithDetail() {
        when(mockServer.getUrl()).thenReturn(new org.apache.dubbo.common.URL("dubbo", "localhost", 20880));
        when(mockServer.getExchangeChannels()).thenReturn(Collections.singletonList(mockExchangeChannel));
        when(mockExchangeChannel.getRemoteAddress()).thenReturn(new InetSocketAddress("127.0.0.1", 12345));
        when(mockExchangeChannel.getLocalAddress()).thenReturn(new InetSocketAddress("localhost", 20880));

        String result = portTelnetHandler.telnet(mockChannel, "20880 -l");
        assertEquals("/127.0.0.1:12345 -> localhost/127.0.0.1:20880", result);
    }

    @Test
    public void testTelnet_IllegalPort() {
        String result = portTelnetHandler.telnet(mockChannel, "abc");
        assertEquals("Illegal port abc, must be integer.", result);
    }

    @Test
    public void testTelnet_NoSuchPort() {
        when(mockServer.getUrl()).thenReturn(new org.apache.dubbo.common.URL("dubbo", "localhost", 20881));
        String result = portTelnetHandler.telnet(mockChannel, "20880");
        assertEquals("No such port 20880", result);
    }
}
