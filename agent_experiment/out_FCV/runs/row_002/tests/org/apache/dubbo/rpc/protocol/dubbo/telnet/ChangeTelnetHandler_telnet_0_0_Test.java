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
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ChangeTelnetHandler_telnet_0_0_Test {
    private ChangeTelnetHandler changeTelnetHandler;
    private Channel mockChannel;
    private Map<String, Exporter<?>> originalExporterMap;

    @Before
    public void setUp() throws Exception {
        changeTelnetHandler = new ChangeTelnetHandler();
        mockChannel = mock(Channel.class);

        // Save the original exporter map using the method
        originalExporterMap = new ConcurrentHashMap<>((Map<String, Exporter<?>>) getExporterMap());

        // Clear the exporter map using the method
        getExporterMap().clear();
    }

    @After
    public void tearDown() throws Exception {
        // Restore the original exporter map using the method
        getExporterMap().clear();
        getExporterMap().putAll(originalExporterMap);
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
    public void testTelnetWithDotDotMessage() {
        when(mockChannel.getAttribute(ChangeTelnetHandler.SERVICE_KEY)).thenReturn("TestService");
        String result = changeTelnetHandler.telnet(mockChannel, "..");
        assertEquals("Cancelled default service TestService.", result);
        verify(mockChannel).removeAttribute(ChangeTelnetHandler.SERVICE_KEY);
    }

    @Test
    public void testTelnetWithValidServiceName() throws Exception {
        // Mock an exporter
        Exporter<?> mockExporter = mock(Exporter.class);
        Invoker<?> mockInvoker = mock(Invoker.class);
        when(mockExporter.getInvoker()).thenReturn((Invoker) mockInvoker);
        when(mockInvoker.getInterface()).thenReturn((Class) Class.forName("java.lang.String"));
        when(mockInvoker.getUrl()).thenReturn(null);

        // Add the mock exporter to the DubboProtocol using the method
        getExporterMap().put("String", mockExporter);

        String result = changeTelnetHandler.telnet(mockChannel, "String");
        assertEquals("Used the String as default.\r\nYou can cancel default service by command: cd /", result);
        verify(mockChannel).setAttribute(ChangeTelnetHandler.SERVICE_KEY, "String");
    }

    @Test
    public void testTelnetWithInvalidServiceName() {
        String result = changeTelnetHandler.telnet(mockChannel, "InvalidService");
        assertEquals("No such service InvalidService", result);
    }

    private Map<String, Exporter<?>> getExporterMap() throws Exception {
        Field exporterMapField = DubboProtocol.class.getDeclaredField("exporterMap");
        exporterMapField.setAccessible(true);
        return (Map<String, Exporter<?>>) exporterMapField.get(DubboProtocol.getDubboProtocol());
    }
}
