package org.apache.dubbo.rpc.protocol.dubbo.telnet;

import org.apache.dubbo.remoting.Channel;
import org.apache.dubbo.rpc.Exporter;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.protocol.dubbo.DubboProtocol;
import org.junit.Before;
import org.junit.Test;
import java.util.Collections;
import org.junit.Assert;
import org.junit.After;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.remoting.telnet.TelnetHandler;
import org.apache.dubbo.remoting.telnet.support.Help;
import org.mockito.*;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class ChangeTelnetHandler_telnet_0_1_Test {

    private ChangeTelnetHandler changeTelnetHandler;

    private Channel mockChannel;

    private DubboProtocol mockDubboProtocol;

    @Before
    public void setUp() throws Exception {
        changeTelnetHandler = new ChangeTelnetHandler();
        mockChannel = mock(Channel.class);
        mockDubboProtocol = mock(DubboProtocol.class);
        // Use reflection to set the DubboProtocol singleton instance
        java.lang.reflect.Field instanceField = DubboProtocol.class.getDeclaredField("INSTANCE");
        instanceField.setAccessible(true);
        instanceField.set(null, mockDubboProtocol);
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
    public void testTelnetWithCancelService() {
        when(mockChannel.getAttribute(ChangeTelnetHandler.SERVICE_KEY)).thenReturn("TestService");
        String result = changeTelnetHandler.telnet(mockChannel, "/");
        assertEquals("Cancelled default service TestService.", result);
        verify(mockChannel).removeAttribute(ChangeTelnetHandler.SERVICE_KEY);
    }

    @Test
    public void testTelnetWithValidServiceName() {
        String serviceName = "ValidService";
        Exporter<?> mockExporter = mock(Exporter.class);
        Invoker<?> mockInvoker = mock(Invoker.class);
        when(mockDubboProtocol.getExporters()).thenReturn(Collections.singletonList(mockExporter));
        when(mockExporter.getInvoker()).thenReturn((Invoker) mockInvoker);
        when(mockInvoker.getInterface()).thenReturn((Class) ValidService.class);
        String result = changeTelnetHandler.telnet(mockChannel, serviceName);
        assertEquals("Used the ValidService as default.\r\nYou can cancel default service by command: cd /", result);
        verify(mockChannel).setAttribute(ChangeTelnetHandler.SERVICE_KEY, serviceName);
    }

    @Test
    public void testTelnetWithInvalidServiceName() {
        when(mockDubboProtocol.getExporters()).thenReturn(Collections.emptyList());
        String result = changeTelnetHandler.telnet(mockChannel, "InvalidService");
        assertEquals("No such service InvalidService", result);
    }

    private interface ValidService {
    }
}
