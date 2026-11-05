package org.apache.dubbo.rpc.protocol.dubbo.telnet;

import org.apache.dubbo.remoting.Channel;
import org.apache.dubbo.rpc.Exporter;
import org.apache.dubbo.rpc.protocol.dubbo.DubboProtocol;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ChangeTelnetHandler_telnet_0_0_Test {
    private ChangeTelnetHandler handler;
    private Channel mockChannel;
    private Collection<Exporter<?>> originalExporters;

    @Before
    public void setUp() throws Exception {
        handler = new ChangeTelnetHandler();
        mockChannel = mock(Channel.class);

        // Save the original exporters
        DubboProtocol protocol = DubboProtocol.getDubboProtocol();
        originalExporters = new ArrayList<>(protocol.getExporters());

        // Mock the exporters
        List<Exporter<?>> mockExporters = new ArrayList<>();
        Exporter<?> mockExporter = mock(Exporter.class, RETURNS_DEEP_STUBS);
        when(mockExporter.getInvoker().getInterface().getSimpleName()).thenReturn("TestService");
        mockExporters.add(mockExporter);

        // Set the mock exporters
        protocol.getExporters().clear();
        protocol.getExporters().addAll(mockExporters);
    }

    @After
    public void tearDown() throws Exception {
        // Restore the original exporters
        DubboProtocol protocol = DubboProtocol.getDubboProtocol();
        protocol.getExporters().clear();
        protocol.getExporters().addAll(originalExporters);
    }

    @Test
    public void testTelnetWithNullMessage() {
        String result = handler.telnet(mockChannel, null);
        assertEquals("Please input service name, eg: \r\ncd XxxService\r\ncd com.xxx.XxxService", result);
    }

    @Test
    public void testTelnetWithEmptyMessage() {
        String result = handler.telnet(mockChannel, "");
        assertEquals("Please input service name, eg: \r\ncd XxxService\r\ncd com.xxx.XxxService", result);
    }

    @Test
    public void testTelnetWithCancelCommand() {
        when(mockChannel.getAttribute(ChangeTelnetHandler.SERVICE_KEY)).thenReturn("TestService");

        String result = handler.telnet(mockChannel, "/");
        assertEquals("Cancelled default service TestService.", result);

        verify(mockChannel).removeAttribute(ChangeTelnetHandler.SERVICE_KEY);
    }

    @Test
    public void testTelnetWithValidServiceName() {
        String result = handler.telnet(mockChannel, "TestService");
        assertEquals("Used the TestService as default.\r\nYou can cancel default service by command: cd /", result);

        verify(mockChannel).setAttribute(ChangeTelnetHandler.SERVICE_KEY, "TestService");
    }

    @Test
    public void testTelnetWithInvalidServiceName() {
        String result = handler.telnet(mockChannel, "InvalidService");
        assertEquals("No such service InvalidService", result);
    }
}
