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
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PortTelnetHandler_telnet_0_0_Test {
    private PortTelnetHandler handler;
    private Channel channel;
    private ExchangeServer server;
    private ExchangeChannel exchangeChannel;

    @Before
    public void setUp() throws Exception {
        handler = new PortTelnetHandler();
        channel = mock(Channel.class);
        server = mock(ExchangeServer.class);
        exchangeChannel = mock(ExchangeChannel.class);

        // Mocking the DubboProtocol to return our mock server
        DubboProtocol protocol = DubboProtocol.getDubboProtocol();
        Field serverMapField = DubboProtocol.class.getDeclaredField("serverMap");
        serverMapField.setAccessible(true);
        Map<String, ExchangeServer> serverMap = new HashMap<>();
        serverMap.put("mock", server);
        serverMapField.set(protocol, serverMap);
    }

    @Test
    public void testTelnet_noPort() throws Exception {
        when(server.getUrl()).thenReturn(new org.apache.dubbo.common.URL("dubbo", "localhost", 8080));

        String result = handler.telnet(channel, "");
        assertEquals("8080", result);
    }

    @Test
    public void testTelnet_withPort() throws Exception {
        when(server.getUrl()).thenReturn(new org.apache.dubbo.common.URL("dubbo", "localhost", 8080));
        when(server.getExchangeChannels()).thenReturn(Collections.singletonList(exchangeChannel));
        when(exchangeChannel.getRemoteAddress()).thenReturn(new InetSocketAddress("localhost", 12345));

        String result = handler.telnet(channel, "8080");
        assertEquals("localhost/127.0.0.1:12345", result);
    }

    @Test
    public void testTelnet_illegalPort() throws Exception {
        String result = handler.telnet(channel, "abc");
        assertEquals("Illegal port abc, must be integer.", result);
    }

    @Test
    public void testTelnet_noSuchPort() throws Exception {
        when(server.getUrl()).thenReturn(new org.apache.dubbo.common.URL("dubbo", "localhost", 8081));

        String result = handler.telnet(channel, "8080");
        assertEquals("No such port 8080", result);
    }
}
