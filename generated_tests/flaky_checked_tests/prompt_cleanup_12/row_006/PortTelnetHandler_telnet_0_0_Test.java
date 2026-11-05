package org.apache.dubbo.rpc.protocol.dubbo.telnet;

import org.apache.dubbo.remoting.Channel;
import org.apache.dubbo.remoting.exchange.ExchangeChannel;
import org.apache.dubbo.remoting.exchange.ExchangeServer;
import org.apache.dubbo.rpc.protocol.dubbo.DubboProtocol;
import org.apache.dubbo.common.URL;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.util.Collections;
import org.mockito.*;
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

    private DubboProtocol originalInstance;

    private PortTelnetHandler portTelnetHandler;

    @Before
    public void setUp() throws Exception {
        portTelnetHandler = new PortTelnetHandler();
        // Save the original DubboProtocol instance
        Field instanceField = DubboProtocol.class.getDeclaredField("INSTANCE");
        instanceField.setAccessible(true);
        originalInstance = (DubboProtocol) instanceField.get(null);
        // Mock the DubboProtocol and its methods
        DubboProtocol mockProtocol = mock(DubboProtocol.class);
        ExchangeServer mockServer = mock(ExchangeServer.class);
        when(mockServer.getUrl()).thenReturn(new URL("dubbo", "localhost", 20880));
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
    public void testTelnetNoMessage() {
        Channel mockChannel = mock(Channel.class);
        String result = portTelnetHandler.telnet(mockChannel, "");
        assertEquals("20880", result);
    }

    @Test
    public void testTelnetWithIllegalPort() {
        Channel mockChannel = mock(Channel.class);
        String result = portTelnetHandler.telnet(mockChannel, "illegal");
        assertEquals("Illegal port illegal, must be integer.", result);
    }

    @Test
    public void testTelnetWithValidPortNoDetail() {
        Channel mockChannel = mock(Channel.class);
        String result = portTelnetHandler.telnet(mockChannel, "20880");
        assertEquals("", result);
    }

    @Test
    public void testTelnetWithNonExistentPort() {
        Channel mockChannel = mock(Channel.class);
        String result = portTelnetHandler.telnet(mockChannel, "12345");
        assertEquals("No such port 12345", result);
    }
}
