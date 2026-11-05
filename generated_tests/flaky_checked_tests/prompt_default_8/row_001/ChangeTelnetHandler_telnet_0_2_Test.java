package org.apache.dubbo.rpc.protocol.dubbo.telnet;

import org.apache.dubbo.remoting.Channel;
import org.apache.dubbo.rpc.Exporter;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.protocol.dubbo.DubboProtocol;
import org.junit.Before;
import org.junit.Test;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
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
public class ChangeTelnetHandler_telnet_0_2_Test {

    private ChangeTelnetHandler changeTelnetHandler;

    @Mock
    private Channel mockChannel;

    @Mock
    private Exporter<Object> mockExporter;

    @Mock
    private Invoker<Object> mockInvoker;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        changeTelnetHandler = new ChangeTelnetHandler();
    }

    @Test
    public void testTelnetWithNullMessage() throws Exception {
        String result = invokeTelnetMethod(mockChannel, null);
        assertEquals("Please input service name, eg: \r\ncd XxxService\r\ncd com.xxx.XxxService", result);
    }

    @Test
    public void testTelnetWithEmptyMessage() throws Exception {
        String result = invokeTelnetMethod(mockChannel, "");
        assertEquals("Please input service name, eg: \r\ncd XxxService\r\ncd com.xxx.XxxService", result);
    }

    @Test
    public void testTelnetWithSlashMessage() throws Exception {
        when(mockChannel.getAttribute(ChangeTelnetHandler.SERVICE_KEY)).thenReturn("TestService");
        String result = invokeTelnetMethod(mockChannel, "/");
        assertEquals("Cancelled default service TestService.", result);
        verify(mockChannel).removeAttribute(ChangeTelnetHandler.SERVICE_KEY);
    }

    @Test
    public void testTelnetWithDotDotMessage() throws Exception {
        when(mockChannel.getAttribute(ChangeTelnetHandler.SERVICE_KEY)).thenReturn("TestService");
        String result = invokeTelnetMethod(mockChannel, "..");
        assertEquals("Cancelled default service TestService.", result);
        verify(mockChannel).removeAttribute(ChangeTelnetHandler.SERVICE_KEY);
    }

    private String invokeTelnetMethod(Channel channel, String message) throws Exception {
        Method telnetMethod = ChangeTelnetHandler.class.getDeclaredMethod("telnet", Channel.class, String.class);
        telnetMethod.setAccessible(true);
        return (String) telnetMethod.invoke(changeTelnetHandler, channel, message);
    }
}
