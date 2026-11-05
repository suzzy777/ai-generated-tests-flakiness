package org.apache.servicecomb.foundation.vertx.stream;

import io.netty.buffer.ByteBuf;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;

@ExtendWith(MockitoExtension.class)
public class BufferInputStream_close_12_1_Test {

    private BufferInputStream bufferInputStream;

    @Mock
    private ByteBuf byteBufMock;

    @BeforeEach
    public void setUp() {
        bufferInputStream = new BufferInputStream(byteBufMock);
    }

    @Test
    public void testClose() {
        bufferInputStream.close();
        verify(byteBufMock, times(1)).release();
    }
}
