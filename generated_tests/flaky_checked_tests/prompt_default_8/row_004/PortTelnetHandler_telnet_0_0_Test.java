package org.apache.dubbo.rpc.protocol.dubbo.telnet;

import org.apache.dubbo.remoting.Channel;
import org.apache.dubbo.remoting.exchange.ExchangeChannel;
import org.apache.dubbo.remoting.exchange.ExchangeServer;
import org.apache.dubbo.rpc.protocol.dubbo.DubboProtocol;
import org.apache.dubbo.rpc.protocol.dubbo.telnet.PortTelnetHandler;
import org.junit.Before;
import org.junit.Test;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
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

public class PortTelnetHandler_telnet_0_0_Test {

    private PortTelnetHandler portTelnetHandler;

    private Channel mockChannel;

    private DubboProtocol mockDubboProtocol;

    @Before
    public void setUp() throws Exception {
        portTelnetHandler = new PortTelnetHandler();
        mockChannel = mock(Channel.class);
        mockDubboProtocol = mock(DubboProtocol.class);
        // Use reflection to set the INSTANCE field of DubboProtocol
        java.lang.reflect.Field instanceField = DubboProtocol.class.getDeclaredField("INSTANCE");
        instanceField.setAccessible(true);
        instanceField.set(null, mockDubboProtocol);
    }

    @Test
    public void testTelnetNoMessage() {
        ExchangeServer mockServer = mockServer(20880, "dubbo://127.0.0.1:20880");
        when(mockDubboProtocol.getServers()).thenReturn(Collections.singletonList(mockServer));
        String result = portTelnetHandler.telnet(mockChannel, "");
        assertEquals("20880", result);
    }

    @Test
    public void testTelnetWithDetailFlag() {
        ExchangeServer mockServer = mockServer(20880, "dubbo://127.0.0.1:20880");
        when(mockDubboProtocol.getServers()).thenReturn(Collections.singletonList(mockServer));
        String result = portTelnetHandler.telnet(mockChannel, "-l");
        assertEquals("dubbo://127.0.0.1:20880", result);
    }

    @Test
    public void testTelnetWithIllegalPort() {
        String result = portTelnetHandler.telnet(mockChannel, "abc");
        assertEquals("Illegal port abc, must be integer.", result);
    }

    @Test
    public void testTelnetWithNonExistentPort() {
        ExchangeServer mockServer = mockServer(20880, "dubbo://127.0.0.1:20880");
        when(mockDubboProtocol.getServers()).thenReturn(Collections.singletonList(mockServer));
        String result = portTelnetHandler.telnet(mockChannel, "20881");
        assertEquals("No such port 20881", result);
    }

    private ExchangeServer mockServer(int port, String url) {
        ExchangeServer mockServer = mock(ExchangeServer.class);
        when(mockServer.getUrl()).thenReturn(new org.apache.dubbo.common.URL("dubbo", "127.0.0.1", port, url));
        return mockServer;
    }

    private ExchangeChannel mockChannel(String remoteAddress, int port) {
        ExchangeChannel mockChannel = mock(ExchangeChannel.class);
        when(mockChannel.getRemoteAddress()).thenReturn(new InetSocketAddress(remoteAddress, port));
        when(mockChannel.getLocalAddress()).thenReturn(new InetSocketAddress("127.0.0.1", port));
        return mockChannel;
    }
}
