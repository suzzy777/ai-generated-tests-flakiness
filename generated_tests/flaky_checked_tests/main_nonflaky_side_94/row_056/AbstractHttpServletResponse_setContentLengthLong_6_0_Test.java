package org.apache.servicecomb.foundation.vertx.http;

import org.apache.servicecomb.foundation.vertx.http.AbstractHttpServletResponse;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.Part;
import javax.ws.rs.core.Response.StatusType;

public class AbstractHttpServletResponse_setContentLengthLong_6_0_Test {

    @Test
    public void testSetContentLengthLong() throws NoSuchMethodException {
        AbstractHttpServletResponse response = new AbstractHttpServletResponse() {
        };
        Method method = AbstractHttpServletResponse.class.getDeclaredMethod("setContentLengthLong", long.class);
        method.setAccessible(true);
        assertThrows(Error.class, () -> {
            try {
                method.invoke(response, 100L);
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        });
    }
}
