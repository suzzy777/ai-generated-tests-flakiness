package org.apache.dubbo.rpc.protocol.dubbo.telnet;

import org.apache.dubbo.remoting.Channel;
import org.apache.dubbo.rpc.Exporter;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.protocol.dubbo.DubboProtocol;
import org.apache.dubbo.rpc.protocol.dubbo.telnet.ChangeTelnetHandler;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
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
public class ChangeTelnetHandler_telnet_0_0_Test {

    private ChangeTelnetHandler changeTelnetHandler;

    private Channel mockChannel;

    private DubboProtocol originalProtocol;

    private DubboProtocol mockProtocol;

    @Before
    public void setUp() throws Exception {
        changeTelnetHandler = new ChangeTelnetHandler();
        mockChannel = mock(Channel.class);
        // Backup the original DubboProtocol instance
        Field instanceField = DubboProtocol.class.getDeclaredField("INSTANCE");
        instanceField.setAccessible(true);
        originalProtocol = (DubboProtocol) instanceField.get(null);
        // Mock the DubboProtocol instance
        mockProtocol = mock(DubboProtocol.class);
        instanceField.set(null, mockProtocol);
    }

    @After
    public void tearDown() throws Exception {
        // Restore the original DubboProtocol instance
        Field instanceField = DubboProtocol.class.getDeclaredField("INSTANCE");
        instanceField.setAccessible(true);
        instanceField.set(null, originalProtocol);
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
    public void testTelnetWithCancelService() {
        when(mockChannel.getAttribute(ChangeTelnetHandler.SERVICE_KEY)).thenReturn("TestService");
        String response = changeTelnetHandler.telnet(mockChannel, "/");
        assertEquals("Cancelled default service TestService.", response);
        verify(mockChannel).removeAttribute(ChangeTelnetHandler.SERVICE_KEY);
    }

    @Test
    public void testTelnetWithValidService() {
        String serviceName = "ValidService";
        Collection<Exporter<?>> exporters = new ArrayList<>();
        Exporter<?> mockExporter = mock(Exporter.class);
        Invoker<?> mockInvoker = mock(Invoker.class);
        when(mockInvoker.getInterface()).thenReturn((Class) ValidService.class);
        when(mockExporter.getInvoker()).thenReturn((Invoker) mockInvoker);
        exporters.add(mockExporter);
        when(mockProtocol.getExporters()).thenReturn(exporters);
        String response = changeTelnetHandler.telnet(mockChannel, serviceName);
        assertEquals("Used the ValidService as default.\r\nYou can cancel default service by command: cd /", response);
        verify(mockChannel).setAttribute(ChangeTelnetHandler.SERVICE_KEY, serviceName);
    }

    @Test
    public void testTelnetWithInvalidService() {
        String serviceName = "InvalidService";
        Collection<Exporter<?>> exporters = new ArrayList<>();
        when(mockProtocol.getExporters()).thenReturn(exporters);
        String response = changeTelnetHandler.telnet(mockChannel, serviceName);
        assertEquals("No such service InvalidService", response);
    }

    interface ValidService {
    }
}
