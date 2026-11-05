package com.intuit.karate.http;

import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.Map;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import com.intuit.karate.FileUtils;
import com.intuit.karate.StringUtils;
import com.intuit.karate.shell.Command;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMessage;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import java.io.File;
import java.io.FileInputStream;
import java.net.URI;
import java.security.KeyStore;
import java.security.Security;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class HttpUtils_parseContentTypeCharset_0_1_Test {

    @Test
    void testParseContentTypeCharset() throws Exception {
        // Use reflection to access the private static method parseContentTypeParams
        Method parseContentTypeParamsMethod = HttpUtils.class.getDeclaredMethod("parseContentTypeParams", String.class);
        parseContentTypeParamsMethod.setAccessible(true);
        // Test cases
        String mimeTypeWithCharset = "text/html; charset=UTF-8";
        String mimeTypeWithoutCharset = "text/html";
        String mimeTypeWithInvalidCharset = "text/html; charset=INVALID";
        // Expected results
        Charset expectedCharset = Charset.forName("UTF-8");
        Charset expectedInvalidCharset = null;
        Charset expectedNoCharset = null;
        // Invoke parseContentTypeParams to get the map
        Map<String, String> mapWithCharset = (Map<String, String>) parseContentTypeParamsMethod.invoke(null, mimeTypeWithCharset);
        Map<String, String> mapWithoutCharset = (Map<String, String>) parseContentTypeParamsMethod.invoke(null, mimeTypeWithoutCharset);
        Map<String, String> mapWithInvalidCharset = (Map<String, String>) parseContentTypeParamsMethod.invoke(null, mimeTypeWithInvalidCharset);
        // Test parseContentTypeCharset method
        assertEquals(expectedCharset, HttpUtils.parseContentTypeCharset(mimeTypeWithCharset));
        assertEquals(expectedNoCharset, HttpUtils.parseContentTypeCharset(mimeTypeWithoutCharset));
        assertThrows(IllegalArgumentException.class, () -> HttpUtils.parseContentTypeCharset(mimeTypeWithInvalidCharset));
        // Additional tests to cover parseContentTypeParams behavior
        assertNotNull(mapWithCharset);
        assertEquals("UTF-8", mapWithCharset.get("charset"));
        assertNull(mapWithoutCharset);
        assertNotNull(mapWithInvalidCharset);
        assertEquals("INVALID", mapWithInvalidCharset.get("charset"));
    }
}
