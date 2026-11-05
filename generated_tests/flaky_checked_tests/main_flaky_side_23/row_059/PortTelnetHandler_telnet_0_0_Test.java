package org.apache.dubbo.rpc.protocol.dubbo.telnet;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.remoting.Channel;
import org.apache.dubbo.remoting.exchange.ExchangeChannel;
import org.apache.dubbo.remoting.exchange.ExchangeServer;
import org.apache.dubbo.rpc.protocol.dubbo.DubboProtocol;
import org.apache.dubbo.rpc.protocol.dubbo.telnet.PortTelnetHandler;
import org.junit.Before;
import org.junit.Test;
import java.net.InetSocketAddress;
import java.util.Collections;
import org.mockito.*;
import org.junit.After;
import org.junit.Assert;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.common.utils.StringUtils;
import org.apache.dubbo.remoting.telnet.TelnetHandler;
import org.apache.dubbo.remoting.telnet.support.Help;
import java.util.Collection;

public class PortTelnetHandler_telnet_0_0_Test {

    private PortTelnetHandler portTelnetHandler;

    private DubboProtocol dubboProtocol;

    private ExchangeServer exchangeServer;

    private ExchangeChannel exchangeChannel;

    private Channel channel;

    @Before
    public void setUp() throws Exception {
        portTelnetHandler = new PortTelnetHandler();
        dubboProtocol = mock(DubboProtocol.class);
        exchangeServer = mock(ExchangeServer.class);
        exchangeChannel = mock(ExchangeChannel.class);
        channel = mock(Channel.class);
        when(exchangeServer.getUrl()).thenReturn(URL.valueOf("dubbo://localhost:8080"));
        when(exchangeChannel.getRemoteAddress()).thenReturn(new InetSocketAddress("localhost", 8081));
        when(exchangeChannel.getLocalAddress()).thenReturn(new InetSocketAddress("localhost", 8080));
        when(dubboProtocol.getServers()).thenReturn(Collections.singleton(exchangeServer));
    }

    @Test
    public void testTelnetWithInvalidPort() {
        String result = portTelnetHandler.telnet(channel, "invalidPort");
        assertEquals("Illegal port invalidPort, must be integer.", result);
    }
}
