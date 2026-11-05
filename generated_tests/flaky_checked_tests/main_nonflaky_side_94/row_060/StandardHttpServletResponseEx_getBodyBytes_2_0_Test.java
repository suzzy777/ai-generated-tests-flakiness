package org.apache.servicecomb.foundation.vertx.http;

import io.vertx.core.buffer.Buffer;
import org.apache.servicecomb.foundation.vertx.http.BodyBufferSupport;
import org.apache.servicecomb.foundation.vertx.http.StandardHttpServletResponseEx;
import java.lang.reflect.Field;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.CompletableFuture;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.servlet.http.Part;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response.StatusType;
import org.apache.servicecomb.foundation.common.http.HttpStatus;
import org.apache.servicecomb.foundation.vertx.stream.PumpFromPart;
import io.vertx.core.Context;
import io.vertx.core.Vertx;

@ExtendWith(MockitoExtension.class)
public class StandardHttpServletResponseEx_getBodyBytes_2_0_Test {

    private StandardHttpServletResponseEx responseEx;

    @Mock
    private BodyBufferSupport mockBodyBuffer;

    @BeforeEach
    public void setUp() throws Exception {
        responseEx = new StandardHttpServletResponseEx(Mockito.mock(HttpServletResponse.class));
        // Use reflection to set the private bodyBuffer field
        Field bodyBufferField = StandardHttpServletResponseEx.class.getDeclaredField("bodyBuffer");
        bodyBufferField.setAccessible(true);
        bodyBufferField.set(responseEx, mockBodyBuffer);
    }

    @Test
    public void testGetBodyBytes() {
        byte[] expectedBytes = new byte[] { 1, 2, 3, 4 };
        when(mockBodyBuffer.getBodyBytes()).thenReturn(expectedBytes);
        byte[] actualBytes = responseEx.getBodyBytes();
        assertArrayEquals(expectedBytes, actualBytes);
        verify(mockBodyBuffer, times(1)).getBodyBytes();
    }
}
