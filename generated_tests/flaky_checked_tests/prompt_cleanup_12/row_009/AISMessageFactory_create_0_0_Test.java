package net.sf.marineapi.ais.parser;

import net.sf.marineapi.ais.message.AISMessage;
import net.sf.marineapi.ais.parser.AISMessageParser;
import net.sf.marineapi.ais.util.Sixbit;
import net.sf.marineapi.nmea.sentence.AISSentence;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import org.mockito.*;
import org.junit.Assert;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

public class AISMessageFactory_create_0_0_Test {

    private AISMessageFactory factory;

    private Map<Integer, Class<? extends AISMessage>> originalParsers;

    @Before
    public void setUp() throws Exception {
        // Use reflection to get the singleton instance
        Method getInstanceMethod = AISMessageFactory.class.getDeclaredMethod("getInstance");
        getInstanceMethod.setAccessible(true);
        factory = (AISMessageFactory) getInstanceMethod.invoke(null);
        // Use reflection to access the private parsers map
        Field parsersField = AISMessageFactory.class.getDeclaredField("parsers");
        parsersField.setAccessible(true);
        originalParsers = (Map<Integer, Class<? extends AISMessage>>) parsersField.get(factory);
        // Set up a new parsers map for testing
        Map<Integer, Class<? extends AISMessage>> testParsers = new HashMap<>();
        parsersField.set(factory, testParsers);
    }

    @After
    public void tearDown() throws Exception {
        // Restore the original parsers map
        Field parsersField = AISMessageFactory.class.getDeclaredField("parsers");
        parsersField.setAccessible(true);
        parsersField.set(factory, originalParsers);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateWithNoParser() {
        // Mock an AISSentence
        AISSentence sentence = mock(AISSentence.class);
        when(sentence.isFragmented()).thenReturn(false);
        when(sentence.getFragmentNumber()).thenReturn(1);
        when(sentence.getPayload()).thenReturn("payload");
        when(sentence.getFillBits()).thenReturn(0);
        // Mock AISMessageParser
        AISMessageParser parser = mock(AISMessageParser.class);
        // Message type not in parsers map
        when(parser.getMessageType()).thenReturn(99);
        // Test the create method
        factory.create(sentence);
    }
}
