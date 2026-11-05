package org.apache.dubbo.rpc.protocol.dubbo.telnet;

import org.apache.dubbo.remoting.Channel;
import org.apache.dubbo.rpc.Exporter;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.protocol.dubbo.DubboProtocol;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

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
        String response = changeTelnetHandler.telnet(mockChannel, null);
        assertEquals("Please input service name, eg: \r\ncd XxxService\r\ncd com.xxx.XxxService", response);
    }

    @Test
    public void testTelnetWithEmptyMessage() {
        String response = changeTelnetHandler.telnet(mockChannel, "");
        assertEquals("Please input service name, eg: \r\ncd XxxService\r\ncd com.xxx.XxxService", response);
    }

    @Test
    public void testTelnetWithSlashMessage() {
        when(mockChannel.getAttribute(ChangeTelnetHandler.SERVICE_KEY)).thenReturn("TestService");
        String response = changeTelnetHandler.telnet(mockChannel, "/");
        assertEquals("Cancelled default service TestService.", response);
        verify(mockChannel).removeAttribute(ChangeTelnetHandler.SERVICE_KEY);
    }

    @Test
    public void testTelnetWithDoubleDotMessage() {
        when(mockChannel.getAttribute(ChangeTelnetHandler.SERVICE_KEY)).thenReturn("TestService");
        String response = changeTelnetHandler.telnet(mockChannel, "..");
        assertEquals("Cancelled default service TestService.", response);
        verify(mockChannel).removeAttribute(ChangeTelnetHandler.SERVICE_KEY);
    }

    @Test
    public void testTelnetWithExistingService() {
        Exporter<?> mockExporter = mock(Exporter.class);
        Invoker<?> mockInvoker = mock(Invoker.class);
        when(mockInvoker.getInterface()).thenReturn((Class)TestService.class);
        when(mockExporter.getInvoker()).thenReturn((Invoker)mockInvoker);

        DubboProtocol mockProtocol = mock(DubboProtocol.class);
        when(mockProtocol.getExporters()).thenReturn(Collections.singletonList(mockExporter));
        setDubboProtocolInstance(mockProtocol);

        String response = changeTelnetHandler.telnet(mockChannel, "TestService");
        assertEquals("Used the TestService as default.\r\nYou can cancel default service by command: cd /", response);
        verify(mockChannel).setAttribute(ChangeTelnetHandler.SERVICE_KEY, "TestService");
    }

    @Test
    public void testTelnetWithNonExistingService() {
        DubboProtocol mockProtocol = mock(DubboProtocol.class);
        when(mockProtocol.getExporters()).thenReturn(Collections.emptyList());
        setDubboProtocolInstance(mockProtocol);

        String response = changeTelnetHandler.telnet(mockChannel, "NonExistingService");
        assertEquals("No such service NonExistingService", response);
    }

    private interface TestService {}

    private void setDubboProtocolInstance(DubboProtocol instance) {
        try {
            java.lang.reflect.Field field = DubboProtocol.class.getDeclaredField("INSTANCE");
            field.setAccessible(true);
            field.set(null, instance);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
