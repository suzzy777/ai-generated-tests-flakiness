package org.apache.dubbo.rpc.protocol.dubbo.telnet;

import org.apache.dubbo.remoting.Channel;
import org.apache.dubbo.remoting.exchange.ExchangeChannel;
import org.apache.dubbo.remoting.exchange.ExchangeServer;
import org.apache.dubbo.rpc.protocol.dubbo.DubboProtocol;
import org.junit.After;
import org.apache.dubbo.common.URL;
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

    private PortTelnetHandler handler;
    private DubboProtocol originalProtocol;
    private DubboProtocol mockProtocol;

    @Before
    public void setUp() throws Exception {
        handler = new PortTelnetHandler();
        originalProtocol = DubboProtocol.getDubboProtocol();
        mockProtocol = mock(DubboProtocol.class);
        setDubboProtocolInstance(mockProtocol);
    }

    @After
    public void tearDown() throws Exception {
        setDubboProtocolInstance(originalProtocol);
    }

    @Test
    public void testTelnetWithNoPort() {
        ExchangeServer server = mock(ExchangeServer.class);
        when(server.getUrl()).thenReturn(new URL("dubbo", "localhost", 20880));

        Collection<ExchangeServer> servers = Collections.singletonList(server);
        when(mockProtocol.getServers()).thenReturn(servers);

        String result = handler.telnet(mock(Channel.class), "");

        assertEquals("20880", result);
    }

    @Test
    public void testTelnetWithInvalidPort() {
        String result = handler.telnet(mock(Channel.class), "invalid");

        assertEquals("Illegal port invalid, must be integer.", result);
    }

    @Test
    public void testTelnetWithValidPortNoDetail() {
        ExchangeServer server = mock(ExchangeServer.class);
        when(server.getUrl()).thenReturn(new URL("dubbo", "localhost", 20880));

        Collection<ExchangeServer> servers = Collections.singletonList(server);
        when(mockProtocol.getServers()).thenReturn(servers);

        ExchangeChannel channel = mock(ExchangeChannel.class);
        when(channel.getRemoteAddress()).thenReturn(new InetSocketAddress("127.0.0.1", 12345));

        Collection<ExchangeChannel> channels = Collections.singletonList(channel);
        when(server.getExchangeChannels()).thenReturn(channels);

        String result = handler.telnet(mock(Channel.class), "20880");

        assertEquals("/127.0.0.1:12345", result);
    }

    @Test
    public void testTelnetWithValidPortWithDetail() {
        ExchangeServer server = mock(ExchangeServer.class);
        when(server.getUrl()).thenReturn(new URL("dubbo", "localhost", 20880));

        Collection<ExchangeServer> servers = Collections.singletonList(server);
        when(mockProtocol.getServers()).thenReturn(servers);

        ExchangeChannel channel = mock(ExchangeChannel.class);
        when(channel.getRemoteAddress()).thenReturn(new InetSocketAddress("127.0.0.1", 12345));
        when(channel.getLocalAddress()).thenReturn(new InetSocketAddress("localhost", 20880));

        Collection<ExchangeChannel> channels = Collections.singletonList(channel);
        when(server.getExchangeChannels()).thenReturn(channels);

        String result = handler.telnet(mock(Channel.class), "-l 20880");

        assertEquals("/127.0.0.1:12345 -> localhost/127.0.0.1:20880", result);
    }

    private void setDubboProtocolInstance(DubboProtocol instance) throws Exception {
        java.lang.reflect.Field field = DubboProtocol.class.getDeclaredField("INSTANCE");
        field.setAccessible(true);
        field.set(null, instance);
    }
}
