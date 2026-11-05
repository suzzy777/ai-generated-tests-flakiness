package org.apache.dubbo.rpc.protocol.dubbo.telnet;

import org.apache.dubbo.remoting.Channel;
import org.apache.dubbo.rpc.Exporter;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.protocol.dubbo.DubboProtocol;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ChangeTelnetHandler_telnet_0_0_Test {
    private ChangeTelnetHandler handler;
    private Channel mockChannel;
    private DubboProtocol mockProtocol;

    @Before
    public void setUp() throws Exception {
        handler = new ChangeTelnetHandler();
        mockChannel = mock(Channel.class);
        mockProtocol = mock(DubboProtocol.class);

        // Use reflection to set the INSTANCE field in DubboProtocol
        Field instanceField = DubboProtocol.class.getDeclaredField("INSTANCE");
        instanceField.setAccessible(true);
        instanceField.set(null, mockProtocol);
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
    public void testTelnetWithCancelService() {
        when(mockChannel.getAttribute(ChangeTelnetHandler.SERVICE_KEY)).thenReturn("TestService");

        String result = handler.telnet(mockChannel, "/");

        verify(mockChannel).removeAttribute(ChangeTelnetHandler.SERVICE_KEY);
        assertEquals("Cancelled default service TestService.", result);
    }

    @Test
    public void testTelnetWithExistingService() {
        List<Exporter<?>> exporters = new ArrayList<>();
        Exporter<?> mockExporter = mock(Exporter.class);
        Invoker<?> mockInvoker = mock(Invoker.class);

        when(mockInvoker.getInterface()).thenReturn((Class)TestService.class);
        when(mockExporter.getInvoker()).thenReturn((Invoker)mockInvoker);
        exporters.add(mockExporter);

        when(mockProtocol.getExporters()).thenReturn(exporters);

        String result = handler.telnet(mockChannel, "TestService");

        verify(mockChannel).setAttribute(ChangeTelnetHandler.SERVICE_KEY, "TestService");
        assertEquals("Used the TestService as default.\r\nYou can cancel default service by command: cd /", result);
    }

    @Test
    public void testTelnetWithNonExistingService() {
        when(mockProtocol.getExporters()).thenReturn(new ArrayList<>());

        String result = handler.telnet(mockChannel, "NonExistingService");

        assertEquals("No such service NonExistingService", result);
    }

    interface TestService {}
}
