package org.apache.dubbo.rpc.protocol.dubbo.telnet;

import org.apache.dubbo.remoting.Channel;
import org.apache.dubbo.rpc.Exporter;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.protocol.dubbo.DubboProtocol;
import org.junit.*;
import org.mockito.Mockito;
import org.mockito.MockedStatic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.*;

public class ChangeTelnetHandler_telnet_0_0_Test {
    private ChangeTelnetHandler changeTelnetHandler;
    private Channel mockChannel;
    private List<Exporter<?>> originalExporters;

    @Before
    public void setUp() throws Exception {
        changeTelnetHandler = new ChangeTelnetHandler();
        mockChannel = Mockito.mock(Channel.class);
        originalExporters = new ArrayList<>(DubboProtocol.getDubboProtocol().getExporters());
    }

    @After
    public void tearDown() throws Exception {
        // Restore the original exporters if modified
        Collection<Exporter<?>> currentExporters = DubboProtocol.getDubboProtocol().getExporters();
        if (!new ArrayList<>(currentExporters).equals(originalExporters)) {
            currentExporters.clear();
            currentExporters.addAll(originalExporters);
        }
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
        Mockito.when(mockChannel.getAttribute(ChangeTelnetHandler.SERVICE_KEY)).thenReturn("TestService");
        String response = changeTelnetHandler.telnet(mockChannel, "/");
        assertEquals("Cancelled default service TestService.", response);
        Mockito.verify(mockChannel).removeAttribute(ChangeTelnetHandler.SERVICE_KEY);
    }

    @Test
    public void testTelnetWithValidService() {
        // Setup mock exporters
        Exporter<?> mockExporter = Mockito.mock(Exporter.class);
        Invoker<?> mockInvoker = Mockito.mock(Invoker.class);
        Mockito.when(mockExporter.getInvoker()).thenReturn((Invoker) mockInvoker);
        Mockito.when(mockInvoker.getInterface()).thenReturn((Class) TestService.class);
        Collection<Exporter<?>> exporters = new ArrayList<>();
        exporters.add(mockExporter);

        // Mock DubboProtocol behavior
        try (MockedStatic<DubboProtocol> mocked = Mockito.mockStatic(DubboProtocol.class, Mockito.CALLS_REAL_METHODS)) {
            DubboProtocol mockDubboProtocol = Mockito.mock(DubboProtocol.class);
            mocked.when(DubboProtocol::getDubboProtocol).thenReturn(mockDubboProtocol);
            Mockito.when(mockDubboProtocol.getExporters()).thenReturn((Collection) exporters);

            String response = changeTelnetHandler.telnet(mockChannel, "TestService");
            assertEquals("Used the TestService as default.\r\nYou can cancel default service by command: cd /", response);
            Mockito.verify(mockChannel).setAttribute(ChangeTelnetHandler.SERVICE_KEY, "TestService");
        }
    }

    @Test
    public void testTelnetWithInvalidService() {
        String response = changeTelnetHandler.telnet(mockChannel, "InvalidService");
        assertEquals("No such service InvalidService", response);
    }

    // Dummy interface for testing purposes
    interface TestService {
    }
}
