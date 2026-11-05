package net.sf.marineapi.ais.parser;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import net.sf.marineapi.ais.message.AISMessage;
import net.sf.marineapi.ais.util.Sixbit;
import net.sf.marineapi.nmea.sentence.AISSentence;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.*;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

public class AISMessageFactory_create_0_0_Test {

    private AISMessageFactory factory;

    private Map<Integer, Class<? extends AISMessage>> originalParsers;

    @Before
    public void setUp() throws Exception {
        // Use reflection to access the private constructor and instance
        Constructor<AISMessageFactory> constructor = AISMessageFactory.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        factory = constructor.newInstance();
        // Save the original parsers map
        originalParsers = (Map<Integer, Class<? extends AISMessage>>) getField(factory, "parsers");
        // Set a new parsers map for testing
        Map<Integer, Class<? extends AISMessage>> mockParsers = new HashMap<>();
        mockParsers.put(1, MockAISMessage.class);
        setField(factory, "parsers", mockParsers);
    }

    @After
    public void tearDown() throws Exception {
        // Restore the original parsers map
        setField(factory, "parsers", originalParsers);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateWithInvalidMessageType() throws Exception {
        AISSentence sentence = mock(AISSentence.class);
        when(sentence.isFragmented()).thenReturn(false);
        when(sentence.getFragmentNumber()).thenReturn(1);
        when(sentence.getPayload()).thenReturn("payload");
        when(sentence.getFillBits()).thenReturn(0);
        // Modify the parsers map to not contain the message type
        Map<Integer, Class<? extends AISMessage>> mockParsers = new HashMap<>();
        setField(factory, "parsers", mockParsers);
        factory.create(sentence);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateWithFragmentedSentenceOutOfOrder() throws Exception {
        AISSentence sentence1 = mock(AISSentence.class);
        when(sentence1.isFragmented()).thenReturn(true);
        when(sentence1.getFragmentNumber()).thenReturn(2);
        when(sentence1.getPayload()).thenReturn("payload1");
        when(sentence1.getFillBits()).thenReturn(0);
        AISSentence sentence2 = mock(AISSentence.class);
        when(sentence2.isFragmented()).thenReturn(true);
        when(sentence2.getFragmentNumber()).thenReturn(1);
        when(sentence2.getPayload()).thenReturn("payload2");
        when(sentence2.getFillBits()).thenReturn(0);
        factory.create(sentence1, sentence2);
    }

    private Object getField(Object obj, String fieldName) throws Exception {
        java.lang.reflect.Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(obj);
    }

    private void setField(Object obj, String fieldName, Object value) throws Exception {
        java.lang.reflect.Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(obj, value);
    }

    // Mock class to use in tests
    public static class MockAISMessage implements AISMessage {

        public MockAISMessage(Sixbit sb) {
        }

        @Override
        public int getMMSI() {
            return 0;
        }

        @Override
        public int getRepeatIndicator() {
            return 0;
        }

        @Override
        public int getMessageType() {
            return 1;
        }
    }
}
