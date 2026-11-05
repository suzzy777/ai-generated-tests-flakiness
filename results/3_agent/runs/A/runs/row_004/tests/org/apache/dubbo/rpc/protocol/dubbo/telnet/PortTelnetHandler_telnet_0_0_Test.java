package org.apache.dubbo.rpc.protocol.dubbo.telnet;

import org.apache.dubbo.remoting.Channel;
import org.apache.dubbo.remoting.exchange.ExchangeChannel;
import org.apache.dubbo.remoting.exchange.ExchangeServer;
import org.apache.dubbo.rpc.protocol.dubbo.DubboProtocol;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class PortTelnetHandler_telnet_0_0_Test {
    private PortTelnetHandler handler;
    private Channel channelMock;
    private ExchangeServer serverMock;
    private ExchangeChannel exchangeChannelMock;
    private DubboProtocol protocolMock;

    @Before
    public void setUp() throws Exception {
        handler = new PortTelnetHandler();
        channelMock = mock(Channel.class);
        serverMock = mock(ExchangeServer.class);
        exchangeChannelMock = mock(ExchangeChannel.class);
        protocolMock = mock(DubboProtocol.class);
        // Set up the protocol mock
        when(protocolMock.getServers()).thenReturn(new ArrayList<ExchangeServer>() {{
            add(serverMock);
        }});

        // Set up the server mock
        when(serverMock.getUrl()).thenReturn(mock(org.apache.dubbo.common.URL.class));
        when(serverMock.getUrl().getProtocol()).thenReturn("dubbo");
        when(serverMock.getUrl().getAddress()).thenReturn("localhost:20880");
        when(serverMock.getUrl().getPort()).thenReturn(20880);
        when(serverMock.getExchangeChannels()).thenReturn(new ArrayList<ExchangeChannel>() {{
            add(exchangeChannelMock);
        }});

        // Set up the exchange channel mock
        when(exchangeChannelMock.getRemoteAddress()).thenReturn(new InetSocketAddress("127.0.0.1", 12345));
        when(exchangeChannelMock.getLocalAddress()).thenReturn(new InetSocketAddress("localhost", 20880));

        // Use reflection to set the INSTANCE field of DubboProtocol
        Field instanceField = DubboProtocol.class.getDeclaredField("INSTANCE");
        instanceField.setAccessible(true);
        instanceField.set(null, protocolMock);
    }

    @Test
    public void testTelnetWithNoPortAndNoDetail() throws Exception {
        String result = handler.telnet(channelMock, "");
        assertEquals("20880", result);
    }

    @Test
    public void testTelnetWithPortAndNoDetail() throws Exception {
        String result = handler.telnet(channelMock, "20880");
        assertEquals("/127.0.0.1:12345", result);
    }

    @Test
    public void testTelnetWithPortAndDetail() throws Exception {
        String result = handler.telnet(channelMock, "-l 20880");
        assertEquals("/127.0.0.1:12345 -> localhost/127.0.0.1:20880", result);
    }

    @Test
    public void testTelnetWithInvalidPort() throws Exception {
        String result = handler.telnet(channelMock, "invalid");
        assertEquals("Illegal port invalid, must be integer.", result);
    }

    @Test
    public void testTelnetWithNoSuchPort() throws Exception {
        when(protocolMock.getServers()).thenReturn(new ArrayList<ExchangeServer>());
        String result = handler.telnet(channelMock, "12345");
        assertEquals("No such port 12345", result);
    }
}

