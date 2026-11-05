package net.sf.marineapi.ais.parser;

import org.junit.Before;
import org.junit.Test;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import net.sf.marineapi.ais.message.AISMessage;
import net.sf.marineapi.nmea.sentence.AISSentence;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class AISMessageFactory_create_0_0_Test {

    private AISMessageFactory factory;
    private AISSentence mockSentence;

    @Before
    public void setUp() throws Exception {
        factory = AISMessageFactory.getInstance();

        // Use reflection to set the parsers map
        Field parsersField = AISMessageFactory.class.getDeclaredField("parsers");
        parsersField.setAccessible(true);
        Map<Integer, Class<? extends AISMessage>> parsers = new HashMap<>();
        parsersField.set(factory, parsers);

        // Mock AISSentence
        mockSentence = mock(AISSentence.class);
        when(mockSentence.getFragmentNumber()).thenReturn(1);
        when(mockSentence.isFragmented()).thenReturn(false);
        when(mockSentence.getPayload()).thenReturn("payload");
        when(mockSentence.getFillBits()).thenReturn(0);
    }

    @Test
    public void testCreate_noParserForMessageType() throws Exception {
        try {
            // Use reflection to invoke the private method
            Method createMethod = AISMessageFactory.class.getDeclaredMethod("create", AISSentence[].class);
            createMethod.setAccessible(true);
            createMethod.invoke(factory, (Object) new AISSentence[]{mockSentence});
            fail("Expected IllegalArgumentException");
        } catch (Exception e) {
            Throwable cause = e.getCause();
            assertTrue(cause instanceof IllegalArgumentException);
            assertEquals("Invalid payload characters", cause.getMessage());
        }
    }
}
