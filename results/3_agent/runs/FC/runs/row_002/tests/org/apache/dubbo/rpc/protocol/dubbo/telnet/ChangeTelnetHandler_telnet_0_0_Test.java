package org.apache.dubbo.rpc.protocol.dubbo.telnet;

import org.apache.dubbo.remoting.Channel;
import org.apache.dubbo.rpc.Exporter;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.protocol.dubbo.DubboProtocol;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ChangeTelnetHandler_telnet_0_0_Test {

    private ChangeTelnetHandler changeTelnetHandler;
    private Channel mockChannel;
    private List<Exporter<?>> originalExporters;

    @Before
    public void setUp() throws Exception {
        changeTelnetHandler = new ChangeTelnetHandler();
        mockChannel = mock(Channel.class);

        // Mock the DubboProtocol to return a modifiable list
        DubboProtocol dubboProtocol = mock(DubboProtocol.class);
        originalExporters = new ArrayList<>();
        when(dubboProtocol.getExporters()).thenReturn(originalExporters);
        Field instanceField = DubboProtocol.class.getDeclaredField("INSTANCE");
        instanceField.setAccessible(true);
        instanceField.set(null, dubboProtocol);
    }

    @After
    public void tearDown() throws Exception {
        // Restore the original state
        reset(mockChannel);
    }

    @Test
    public void testTelnet_NoMessage() {
        String result = changeTelnetHandler.telnet(mockChannel, "");
        assertEquals("Please input service name, eg: \r\ncd XxxService\r\ncd com.xxx.XxxService", result);
    }

    @Test
    public void testTelnet_CancelService() {
        when(mockChannel.getAttribute(ChangeTelnetHandler.SERVICE_KEY)).thenReturn("TestService");
        String result = changeTelnetHandler.telnet(mockChannel, "/");
        assertEquals("Cancelled default service TestService.", result);
        verify(mockChannel).removeAttribute(ChangeTelnetHandler.SERVICE_KEY);
    }

    @Test
    public void testTelnet_ValidService() {
        Invoker<String> mockInvoker = mock(Invoker.class);
        Exporter<String> mockExporter = mock(Exporter.class);
        when(mockExporter.getInvoker()).thenReturn(mockInvoker);
        when(mockInvoker.getInterface()).thenReturn(String.class);

        originalExporters.add(mockExporter);

        String result = changeTelnetHandler.telnet(mockChannel, "String");
        assertEquals("Used the String as default.\r\nYou can cancel default service by command: cd /", result);
        verify(mockChannel).setAttribute(ChangeTelnetHandler.SERVICE_KEY, "String");
    }

    @Test
    public void testTelnet_InvalidService() {
        String result = changeTelnetHandler.telnet(mockChannel, "InvalidService");
        assertEquals("No such service InvalidService", result);
    }
}
