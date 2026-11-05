package org.apache.dubbo.rpc.protocol.dubbo.telnet;

import org.apache.dubbo.remoting.Channel;
import org.apache.dubbo.rpc.Exporter;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.protocol.dubbo.DubboProtocol;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ChangeTelnetHandler_telnet_0_0_Test {
    private ChangeTelnetHandler changeTelnetHandler;
    private Channel mockChannel;
    private DubboProtocol mockDubboProtocol;

    @Before
    public void setUp() throws Exception {
        changeTelnetHandler = new ChangeTelnetHandler();
        mockChannel = mock(Channel.class);
        mockDubboProtocol = mock(DubboProtocol.class);

        // Use reflection to set the static INSTANCE field
        Field instanceField = DubboProtocol.class.getDeclaredField("INSTANCE");
        instanceField.setAccessible(true);
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(instanceField, instanceField.getModifiers() & ~Modifier.FINAL);
        instanceField.set(null, mockDubboProtocol);
    }

    @Test
    public void testTelnet_withNullMessage() {
        String result = changeTelnetHandler.telnet(mockChannel, null);
        assertEquals("Please input service name, eg: \r\ncd XxxService\r\ncd com.xxx.XxxService", result);
    }

    @Test
    public void testTelnet_withEmptyMessage() {
        String result = changeTelnetHandler.telnet(mockChannel, "");
        assertEquals("Please input service name, eg: \r\ncd XxxService\r\ncd com.xxx.XxxService", result);
    }

    @Test
    public void testTelnet_withSlashMessage() {
        when(mockChannel.getAttribute(ChangeTelnetHandler.SERVICE_KEY)).thenReturn("TestService");

        String result = changeTelnetHandler.telnet(mockChannel, "/");
        assertEquals("Cancelled default service TestService.", result);

        verify(mockChannel).removeAttribute(ChangeTelnetHandler.SERVICE_KEY);
    }

    @Test
    public void testTelnet_withValidServiceName() {
        List<Exporter<?>> exporters = new ArrayList<>();
        Exporter<?> mockExporter = mock(Exporter.class);
        Invoker<?> mockInvoker = mock(Invoker.class);
        when(mockExporter.getInvoker()).thenReturn((Invoker) mockInvoker);
        when(mockInvoker.getInterface()).thenReturn((Class) TestService.class);
        exporters.add(mockExporter);

        when(mockDubboProtocol.getExporters()).thenReturn(exporters);

        String result = changeTelnetHandler.telnet(mockChannel, "TestService");
        assertEquals("Used the TestService as default.\r\nYou can cancel default service by command: cd /", result);

        verify(mockChannel).setAttribute(ChangeTelnetHandler.SERVICE_KEY, "TestService");
    }

    @Test
    public void testTelnet_withInvalidServiceName() {
        when(mockDubboProtocol.getExporters()).thenReturn(new ArrayList<>());

        String result = changeTelnetHandler.telnet(mockChannel, "InvalidService");
        assertEquals("No such service InvalidService", result);
    }

    interface TestService {}
}
