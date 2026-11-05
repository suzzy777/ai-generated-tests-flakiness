package org.apache.dubbo.rpc.protocol.dubbo.telnet;

import org.apache.dubbo.remoting.Channel;
import org.apache.dubbo.remoting.exchange.ExchangeChannel;
import org.apache.dubbo.remoting.exchange.ExchangeServer;
import org.apache.dubbo.rpc.protocol.dubbo.DubboProtocol;
import org.apache.dubbo.rpc.protocol.dubbo.telnet.PortTelnetHandler;
import org.junit.Before;
import org.junit.Test;
import java.net.InetSocketAddress;
import java.util.Arrays;
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

    private ExchangeServer mockServer;

    private ExchangeChannel mockExchangeChannel;

    @Before
    public void setUp() {
        portTelnetHandler = new PortTelnetHandler();
        mockChannel = Mockito.mock(Channel.class);
        mockServer = Mockito.mock(ExchangeServer.class);
        mockExchangeChannel = Mockito.mock(ExchangeChannel.class);
        // Mocking DubboProtocol to return our mock server
        DubboProtocol mockDubboProtocol = Mockito.mock(DubboProtocol.class);
        when(mockDubboProtocol.getServers()).thenReturn(Collections.singletonList(mockServer));
        try {
            java.lang.reflect.Field instanceField = DubboProtocol.class.getDeclaredField("INSTANCE");
            instanceField.setAccessible(true);
            instanceField.set(null, mockDubboProtocol);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testTelnetWithoutMessage() {
        when(mockServer.getUrl()).thenReturn(new org.apache.dubbo.common.URL("dubbo", "localhost", 20880));
        String result = portTelnetHandler.telnet(mockChannel, "");
        assertEquals("20880", result);
    }

    @Test
    public void testTelnetWithDetailFlag() {
        when(mockServer.getUrl()).thenReturn(new org.apache.dubbo.common.URL("dubbo", "localhost", 20880));
        String result = portTelnetHandler.telnet(mockChannel, "-l");
        assertEquals("dubbo://localhost:20880", result);
    }

    @Test
    public void testTelnetWithIllegalPort() {
        String result = portTelnetHandler.telnet(mockChannel, "abc");
        assertEquals("Illegal port abc, must be integer.", result);
    }

    @Test
    public void testTelnetWithNonExistentPort() {
        when(mockServer.getUrl()).thenReturn(new org.apache.dubbo.common.URL("dubbo", "localhost", 20880));
        String result = portTelnetHandler.telnet(mockChannel, "9999");
        assertEquals("No such port 9999", result);
    }
}
