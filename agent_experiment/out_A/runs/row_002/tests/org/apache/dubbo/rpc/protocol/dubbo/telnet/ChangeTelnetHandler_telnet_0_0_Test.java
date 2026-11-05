package org.apache.dubbo.rpc.protocol.dubbo.telnet;

import org.apache.dubbo.remoting.Channel;
import org.apache.dubbo.rpc.Exporter;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.protocol.dubbo.DubboProtocol;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ChangeTelnetHandler_telnet_0_0_Test {
    private ChangeTelnetHandler changeTelnetHandler;
    private Channel mockChannel;

    @Before
    public void setUp() {
        changeTelnetHandler = new ChangeTelnetHandler();
        mockChannel = mock(Channel.class);
    }

    @Test
    public void testTelnetWithNullMessage() {
        String result = changeTelnetHandler.telnet(mockChannel, null);
        assertEquals("Please input service name, eg: \r\ncd XxxService\r\ncd com.xxx.XxxService", result);
    }

    @Test
    public void testTelnetWithEmptyMessage() {
        String result = changeTelnetHandler.telnet(mockChannel, "");
        assertEquals("Please input service name, eg: \r\ncd XxxService\r\ncd com.xxx.XxxService", result);
    }

    @Test
    public void testTelnetWithSlashMessage() {
        when(mockChannel.getAttribute(ChangeTelnetHandler.SERVICE_KEY)).thenReturn("TestService");
        String result = changeTelnetHandler.telnet(mockChannel, "/");
        assertEquals("Cancelled default service TestService.", result);
        verify(mockChannel).removeAttribute(ChangeTelnetHandler.SERVICE_KEY);
    }

    @Test
    public void testTelnetWithValidServiceName() {
        List<Exporter<?>> exporters = new ArrayList<>();
        Exporter<?> mockExporter = mock(Exporter.class);
        Invoker<?> mockInvoker = mock(Invoker.class);
        when(mockInvoker.getInterface()).thenReturn((Class) TestService.class);
        when(mockExporter.getInvoker()).thenReturn((Invoker) mockInvoker);
        exporters.add(mockExporter);

        DubboProtocol mockProtocol = mock(DubboProtocol.class);
        when(mockProtocol.getExporters()).thenReturn(exporters);
        setDubboProtocolInstance(mockProtocol);

        String result = changeTelnetHandler.telnet(mockChannel, "TestService");
        assertEquals("Used the TestService as default.\r\nYou can cancel default service by command: cd /", result);
        verify(mockChannel).setAttribute(ChangeTelnetHandler.SERVICE_KEY, "TestService");
    }

    @Test
    public void testTelnetWithInvalidServiceName() {
        List<Exporter<?>> exporters = new ArrayList<>();

        DubboProtocol mockProtocol = mock(DubboProtocol.class);
        when(mockProtocol.getExporters()).thenReturn(exporters);
        setDubboProtocolInstance(mockProtocol);

        String result = changeTelnetHandler.telnet(mockChannel, "InvalidService");
        assertEquals("No such service InvalidService", result);
    }

    private interface TestService {}

    private void setDubboProtocolInstance(DubboProtocol instance) {
        try {
            java.lang.reflect.Field instanceField = DubboProtocol.class.getDeclaredField("INSTANCE");
            instanceField.setAccessible(true);
            instanceField.set(null, instance);
        } catch (Exception e) {
            fail("Failed to set DubboProtocol instance: " + e.getMessage());
        }
    }
}
