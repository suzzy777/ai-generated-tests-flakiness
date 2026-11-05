package org.apache.dubbo.rpc.protocol.dubbo.telnet;

import org.apache.dubbo.remoting.Channel;
import org.apache.dubbo.rpc.Exporter;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.protocol.dubbo.DubboProtocol;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class ChangeTelnetHandler_telnet_0_0_Test {

    private ChangeTelnetHandler handler;
    private Channel mockChannel;
    private String originalServiceKey;
    private DubboProtocol originalProtocolInstance;

    @Before
    public void setUp() throws Exception {
        handler = new ChangeTelnetHandler();
        mockChannel = mock(Channel.class);
        originalServiceKey = (String) mockChannel.getAttribute(ChangeTelnetHandler.SERVICE_KEY);

        // Save the original protocol instance
        originalProtocolInstance = DubboProtocol.getDubboProtocol();
    }

    @After
    public void tearDown() throws Exception {
        mockChannel.setAttribute(ChangeTelnetHandler.SERVICE_KEY, originalServiceKey);

        // Restore the original protocol instance
        setMockProtocolInstance(originalProtocolInstance);
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
    public void testTelnetWithValidService() {
        String serviceName = "TestService";
        mockExporterCollection(serviceName);

        String result = handler.telnet(mockChannel, serviceName);
        assertEquals("Used the TestService as default.\r\nYou can cancel default service by command: cd /", result);
        verify(mockChannel).setAttribute(ChangeTelnetHandler.SERVICE_KEY, serviceName);
    }

    @Test
    public void testTelnetWithInvalidService() {
        String serviceName = "InvalidService";
        mockExporterCollection("OtherService");

        String result = handler.telnet(mockChannel, serviceName);
        assertEquals("No such service InvalidService", result);
    }

    private void mockExporterCollection(String serviceName) {
        Collection<Exporter<?>> exporters = new ArrayList<>();
        Exporter<?> mockExporter = mock(Exporter.class);
        Invoker<?> mockInvoker = mock(Invoker.class);

        Class<?> mockInterface = mock(Class.class);
        when(mockInterface.getSimpleName()).thenReturn(serviceName);
        when(mockInvoker.getInterface()).thenReturn((Class) mockInterface);
        when(mockExporter.getInvoker()).thenReturn((Invoker) mockInvoker);
        exporters.add(mockExporter);

        DubboProtocol mockProtocol = spy(DubboProtocol.getDubboProtocol());
        doReturn(exporters).when(mockProtocol).getExporters();

        // Set the mock protocol instance
        setMockProtocolInstance(mockProtocol);
    }

    private void setMockProtocolInstance(DubboProtocol mockProtocol) {
        try {
            java.lang.reflect.Field instanceField = DubboProtocol.class.getDeclaredField("INSTANCE");
            instanceField.setAccessible(true);
            instanceField.set(null, mockProtocol);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
