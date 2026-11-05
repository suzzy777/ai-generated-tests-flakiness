package org.apache.dubbo.rpc.protocol.dubbo.telnet;

import org.apache.dubbo.remoting.Channel;
import org.apache.dubbo.rpc.Exporter;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.protocol.dubbo.DubboProtocol;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ChangeTelnetHandler_telnet_0_0_Test {
    private ChangeTelnetHandler handler;
    private Channel channel;
    private Map<String, Exporter<?>> originalExporterMap;

    @Before
    public void setUp() throws Exception {
        handler = new ChangeTelnetHandler();
        channel = mock(Channel.class);

        // Save the original exporterMap
        Field exporterMapField = DubboProtocol.class.getDeclaredField("exporterMap");
        exporterMapField.setAccessible(true);
        originalExporterMap = new HashMap<>((Map<String, Exporter<?>>) exporterMapField.get(DubboProtocol.getDubboProtocol()));
    }

    @After
    public void tearDown() throws Exception {
        // Restore the original exporterMap
        Field exporterMapField = DubboProtocol.class.getDeclaredField("exporterMap");
        exporterMapField.setAccessible(true);
        exporterMapField.set(DubboProtocol.getDubboProtocol(), originalExporterMap);
    }

    @Test
    public void testTelnetWithNullMessage() {
        String result = handler.telnet(channel, null);
        assertEquals("Please input service name, eg: \r\ncd XxxService\r\ncd com.xxx.XxxService", result);
    }

    @Test
    public void testTelnetWithEmptyMessage() {
        String result = handler.telnet(channel, "");
        assertEquals("Please input service name, eg: \r\ncd XxxService\r\ncd com.xxx.XxxService", result);
    }

    @Test
    public void testTelnetWithSlashMessage() {
        when(channel.getAttribute(ChangeTelnetHandler.SERVICE_KEY)).thenReturn("TestService");

        String result = handler.telnet(channel, "/");
        assertEquals("Cancelled default service TestService.", result);

        verify(channel).removeAttribute(ChangeTelnetHandler.SERVICE_KEY);
    }

    @Test
    public void testTelnetWithValidServiceName() throws Exception {
        Exporter<?> exporter = mock(Exporter.class);
        Invoker<?> invoker = mock(Invoker.class);
        when(invoker.getInterface()).thenReturn((Class<?>) TestService.class);
        when(exporter.getInvoker()).thenReturn((Invoker<?>) invoker);

        Field exporterMapField = DubboProtocol.class.getDeclaredField("exporterMap");
        exporterMapField.setAccessible(true);
        Map<String, Exporter<?>> exporterMap = new HashMap<>();
        exporterMap.put("TestService", exporter);
        exporterMapField.set(DubboProtocol.getDubboProtocol(), exporterMap);

        String result = handler.telnet(channel, "TestService");
        assertEquals("Used the TestService as default.\r\nYou can cancel default service by command: cd /", result);

        verify(channel).setAttribute(ChangeTelnetHandler.SERVICE_KEY, "TestService");
    }

    @Test
    public void testTelnetWithInvalidServiceName() {
        String result = handler.telnet(channel, "InvalidService");
        assertEquals("No such service InvalidService", result);
    }

    // Dummy service interface for mocking
    interface TestService {}
}
