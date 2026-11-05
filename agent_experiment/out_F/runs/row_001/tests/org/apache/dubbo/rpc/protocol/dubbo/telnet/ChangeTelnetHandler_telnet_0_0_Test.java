package org.apache.dubbo.rpc.protocol.dubbo.telnet;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.remoting.Channel;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.protocol.dubbo.DubboProtocol;
import org.apache.dubbo.rpc.protocol.dubbo.support.DemoService;
import org.apache.dubbo.rpc.protocol.dubbo.support.ProtocolUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;

public class ChangeTelnetHandler_telnet_0_0_Test {

    private ChangeTelnetHandler handler;
    private Channel mockChannel;
    private Invoker<DemoService> mockInvoker;

    @Before
    public void setUp() {
        handler = new ChangeTelnetHandler();
        mockChannel = mock(Channel.class);
        mockInvoker = mock(Invoker.class);
        given(mockChannel.getAttribute(ChangeTelnetHandler.SERVICE_KEY)).willReturn("org.apache.dubbo.rpc.protocol.dubbo.support.DemoService");
        given(mockInvoker.getInterface()).willReturn(DemoService.class);
        given(mockInvoker.getUrl()).willReturn(URL.valueOf("dubbo://127.0.0.1:20884/demo"));
    }

    @After
    public void tearDown() {
        ProtocolUtils.closeAll();
        reset(mockChannel, mockInvoker);
    }

    @Test
    public void testTelnetWithValidServiceName() {
        DubboProtocol.getDubboProtocol().export(mockInvoker);
        String result = handler.telnet(mockChannel, "DemoService");
        assertEquals("Used the DemoService as default.\r\nYou can cancel default service by command: cd /", result);
    }

    @Test
    public void testTelnetWithInvalidServiceName() {
        String result = handler.telnet(mockChannel, "InvalidService");
        assertEquals("No such service InvalidService", result);
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
    public void testTelnetCancelService() {
        String result = handler.telnet(mockChannel, "..");
        assertEquals("Cancelled default service org.apache.dubbo.rpc.protocol.dubbo.support.DemoService.", result);
    }

    @Test
    public void testTelnetCancelServiceWithSlash() {
        String result = handler.telnet(mockChannel, "/");
        assertEquals("Cancelled default service org.apache.dubbo.rpc.protocol.dubbo.support.DemoService.", result);
    }
}
