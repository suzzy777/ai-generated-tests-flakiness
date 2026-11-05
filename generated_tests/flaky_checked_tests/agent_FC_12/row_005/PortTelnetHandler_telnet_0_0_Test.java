package org.apache.dubbo.rpc.protocol.dubbo.telnet;

import org.apache.dubbo.remoting.Channel;
import org.apache.dubbo.remoting.exchange.ExchangeChannel;
import org.apache.dubbo.remoting.exchange.ExchangeServer;
import org.apache.dubbo.rpc.protocol.dubbo.DubboProtocol;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class PortTelnetHandler_telnet_0_0_Test {
    private DubboProtocol originalInstance;
    private DubboProtocol mockProtocol;
    private ExchangeServer mockServer;
    private ExchangeChannel mockChannel;
    private Channel mockTelnetChannel;
    
    @Before
    public void setUp() throws Exception {
        // Save the original DubboProtocol instance
        Field instanceField = DubboProtocol.class.getDeclaredField("INSTANCE");
        instanceField.setAccessible(true);
        originalInstance = (DubboProtocol) instanceField.get(null);

        // Mock DubboProtocol and its methods
        mockProtocol = mock(DubboProtocol.class);
        mockServer = mock(ExchangeServer.class);
        mockChannel = mock(ExchangeChannel.class);
        mockTelnetChannel = mock(Channel.class);

        when(mockServer.getUrl()).thenReturn(new org.apache.dubbo.common.URL("dubbo", "localhost", 20880));
        when(mockServer.getExchangeChannels()).thenReturn(Collections.singletonList(mockChannel));
        when(mockChannel.getRemoteAddress()).thenReturn(new InetSocketAddress("127.0.0.1", 12345));
        when(mockChannel.getLocalAddress()).thenReturn(new InetSocketAddress("127.0.0.1", 20880));
        when(mockProtocol.getServers()).thenReturn(Collections.singletonList(mockServer));

        // Set the mock instance
        instanceField.set(null, mockProtocol);
    }

    @After
    public void tearDown() throws Exception {
        // Restore the original DubboProtocol instance
        Field instanceField = DubboProtocol.class.getDeclaredField("INSTANCE");
        instanceField.setAccessible(true);
        instanceField.set(null, originalInstance);
    }

    @Test
    public void testTelnetWithNoMessage() {
        PortTelnetHandler handler = new PortTelnetHandler();
        String result = handler.telnet(mockTelnetChannel, "");
        assertEquals("20880", result);
    }

    @Test
    public void testTelnetWithPortOnly() {
        PortTelnetHandler handler = new PortTelnetHandler();
        String result = handler.telnet(mockTelnetChannel, "20880");
        assertEquals("/127.0.0.1:12345", result);
    }

    @Test
    public void testTelnetWithInvalidPort() {
        PortTelnetHandler handler = new PortTelnetHandler();
        String result = handler.telnet(mockTelnetChannel, "invalid");
        assertEquals("Illegal port invalid, must be integer.", result);
    }

    @Test
    public void testTelnetWithDetailFlag() {
        PortTelnetHandler handler = new PortTelnetHandler();
        String result = handler.telnet(mockTelnetChannel, "-l");
        assertEquals("dubbo://localhost:20880", result);
    }

    @Test
    public void testTelnetWithDetailFlagAndPort() {
        PortTelnetHandler handler = new PortTelnetHandler();
        String result = handler.telnet(mockTelnetChannel, "-l 20880");
        assertEquals("/127.0.0.1:12345 -> /127.0.0.1:20880", result);
    }

    @Test
    public void testTelnetWithNonExistingPort() {
        PortTelnetHandler handler = new PortTelnetHandler();
        String result = handler.telnet(mockTelnetChannel, "9999");
        assertEquals("No such port 9999", result);
    }
}
