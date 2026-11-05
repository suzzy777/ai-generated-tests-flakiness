package org.apache.dubbo.rpc.protocol.dubbo.telnet;

import org.apache.dubbo.remoting.Channel;
import org.apache.dubbo.remoting.exchange.ExchangeChannel;
import org.apache.dubbo.remoting.exchange.ExchangeServer;
import org.apache.dubbo.rpc.protocol.dubbo.DubboProtocol;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest(DubboProtocol.class)
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

        Collection<ExchangeServer> servers = new ArrayList<>();
        servers.add(mockServer);

        DubboProtocol mockDubboProtocol = mock(DubboProtocol.class);
        when(mockDubboProtocol.getServers()).thenReturn(servers);

        PowerMockito.mockStatic(DubboProtocol.class);
        when(DubboProtocol.getDubboProtocol()).thenReturn(mockDubboProtocol);
    }

    @Test
    public void testTelnetWithNoMessage() {
        when(mockServer.getUrl().getPort()).thenReturn(20880);
        String result = portTelnetHandler.telnet(mockChannel, "");
        assertEquals("20880", result);
    }

    @Test
    public void testTelnetWithDetail() {
        when(mockServer.getUrl().getProtocol()).thenReturn("dubbo");
        when(mockServer.getUrl().getAddress()).thenReturn("127.0.0.1:20880");
        String result = portTelnetHandler.telnet(mockChannel, "-l");
        assertEquals("dubbo://127.0.0.1:20880", result);
    }

    @Test
    public void testTelnetWithIllegalPort() {
        String result = portTelnetHandler.telnet(mockChannel, "abc");
        assertEquals("Illegal port abc, must be integer.", result);
    }

    @Test
    public void testTelnetWithValidPort() {
        when(mockServer.getUrl().getPort()).thenReturn(20880);
        Collection<ExchangeChannel> channels = new ArrayList<>();
        channels.add(mockExchangeChannel);
        when(mockServer.getExchangeChannels()).thenReturn(channels);
        when(mockExchangeChannel.getRemoteAddress()).thenReturn(new InetSocketAddress("127.0.0.1", 12345));

        String result = portTelnetHandler.telnet(mockChannel, "20880");
        assertEquals("/127.0.0.1:12345", result);
    }

    @Test
    public void testTelnetWithValidPortAndDetail() {
        when(mockServer.getUrl().getPort()).thenReturn(20880);
        Collection<ExchangeChannel> channels = new ArrayList<>();
        channels.add(mockExchangeChannel);
        when(mockServer.getExchangeChannels()).thenReturn(channels);
        when(mockExchangeChannel.getRemoteAddress()).thenReturn(new InetSocketAddress("127.0.0.1", 12345));
        when(mockExchangeChannel.getLocalAddress()).thenReturn(new InetSocketAddress("127.0.0.1", 20880));

        String result = portTelnetHandler.telnet(mockChannel, "20880 -l");
        assertEquals("/127.0.0.1:12345 -> /127.0.0.1:20880", result);
    }

    @Test
    public void testTelnetWithNonExistentPort() {
        when(mockServer.getUrl().getPort()).thenReturn(20881);
        String result = portTelnetHandler.telnet(mockChannel, "20880");
        assertEquals("No such port 20880", result);
    }
}
