package org.apache.dubbo.rpc.protocol.dubbo.telnet;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.remoting.Channel;
import org.apache.dubbo.remoting.exchange.ExchangeChannel;
import org.apache.dubbo.remoting.exchange.ExchangeServer;
import org.apache.dubbo.rpc.protocol.dubbo.DubboProtocol;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PortTelnetHandler_telnet_0_0_Test {

    private PortTelnetHandler handler;
    private DubboProtocol originalDubboProtocol;

    @Before
    public void setUp() {
        handler = new PortTelnetHandler();
        originalDubboProtocol = DubboProtocol.getDubboProtocol();
    }

    @After
    public void tearDown() {
        // Restore the original DubboProtocol instance
        setDubboProtocolInstance(originalDubboProtocol);
    }

    @Test
    public void testTelnetWithoutPort() {
        ExchangeServer server = mock(ExchangeServer.class);
        when(server.getUrl()).thenReturn(URL.valueOf("dubbo://127.0.0.1:20880"));

        setDubboProtocolInstance(createDubboProtocol(server));

        Channel channel = mock(Channel.class);
        String result = handler.telnet(channel, "");

        assertEquals("20880", result);
    }

    @Test
    public void testTelnetWithInvalidPort() {
        Channel channel = mock(Channel.class);
        String result = handler.telnet(channel, "invalidPort");

        assertEquals("Illegal port invalidPort, must be integer.", result);
    }

    @Test
    public void testTelnetWithValidPortAndNoMatchingServer() {
        setDubboProtocolInstance(createDubboProtocol());

        Channel channel = mock(Channel.class);
        String result = handler.telnet(channel, "20880");

        assertEquals("No such port 20880", result);
    }

    private DubboProtocol createDubboProtocol(ExchangeServer... servers) {
        DubboProtocol dubboProtocol = mock(DubboProtocol.class);
        Collection<ExchangeServer> serverList = Arrays.asList(servers);
        when(dubboProtocol.getServers()).thenReturn(serverList);
        return dubboProtocol;
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
}
