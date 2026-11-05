package org.apache.dubbo.rpc.protocol.dubbo.telnet;

import org.apache.dubbo.remoting.Channel;
import org.apache.dubbo.rpc.Exporter;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.protocol.dubbo.DubboProtocol;
import org.junit.Before;
import org.junit.Test;
import java.util.Collections;
import org.junit.After;
import org.junit.Assert;
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

    @Mock
    private Channel mockChannel;

    @Mock
    private Exporter<?> mockExporter;

    @Mock
    private Invoker<?> mockInvoker;

    @Before
    public void setUp() {
        changeTelnetHandler = new ChangeTelnetHandler();
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
        Mockito.verify(mockChannel).removeAttribute(ChangeTelnetHandler.SERVICE_KEY);
    }

    @Test
    public void testTelnetWithDotDotMessage() {
        when(mockChannel.getAttribute(ChangeTelnetHandler.SERVICE_KEY)).thenReturn("TestService");
        String result = changeTelnetHandler.telnet(mockChannel, "..");
        assertEquals("Cancelled default service TestService.", result);
        Mockito.verify(mockChannel).removeAttribute(ChangeTelnetHandler.SERVICE_KEY);
    }

    @Test
    public void testTelnetWithValidServiceName() {
        when(mockExporter.getInvoker()).thenReturn((Invoker) mockInvoker);
        when(mockInvoker.getInterface()).thenReturn((Class) TestService.class);
        DubboProtocol dubboProtocol = Mockito.mock(DubboProtocol.class);
        when(dubboProtocol.getExporters()).thenReturn(Collections.singletonList(mockExporter));
        // Use reflection to set the INSTANCE field of DubboProtocol
        try {
            java.lang.reflect.Field instanceField = DubboProtocol.class.getDeclaredField("INSTANCE");
            instanceField.setAccessible(true);
            instanceField.set(null, dubboProtocol);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        String result = changeTelnetHandler.telnet(mockChannel, "TestService");
        assertEquals("Used the TestService as default.\r\nYou can cancel default service by command: cd /", result);
        Mockito.verify(mockChannel).setAttribute(ChangeTelnetHandler.SERVICE_KEY, "TestService");
    }

    @Test
    public void testTelnetWithInvalidServiceName() {
        DubboProtocol dubboProtocol = Mockito.mock(DubboProtocol.class);
        when(dubboProtocol.getExporters()).thenReturn(Collections.emptyList());
        // Use reflection to set the INSTANCE field of DubboProtocol
        try {
            java.lang.reflect.Field instanceField = DubboProtocol.class.getDeclaredField("INSTANCE");
            instanceField.setAccessible(true);
            instanceField.set(null, dubboProtocol);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        String result = changeTelnetHandler.telnet(mockChannel, "InvalidService");
        assertEquals("No such service InvalidService", result);
    }

    // Mock service interface for testing
    interface TestService {
    }
}
