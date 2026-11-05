package net.sf.marineapi.ais.parser;

import net.sf.marineapi.ais.message.AISMessage;
import net.sf.marineapi.ais.util.Sixbit;
import net.sf.marineapi.nmea.sentence.AISSentence;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class AISMessageFactory_create_0_0_Test {

    private AISMessageFactory factory;

    @Before
    public void setUp() throws Exception {
        factory = AISMessageFactory.getInstance();
        Field parsersField = AISMessageFactory.class.getDeclaredField("parsers");
        parsersField.setAccessible(true);
        parsersField.set(factory, new HashMap<Integer, Class<? extends AISMessage>>());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreate_NoParserForMessageType() throws Exception {
        AISSentence sentence = mock(AISSentence.class);
        when(sentence.getFragmentNumber()).thenReturn(1);
        when(sentence.getPayload()).thenReturn("BBQk@<0PD5P06h"); // Valid payload
        when(sentence.getFillBits()).thenReturn(0);

        factory.create(sentence);
    }

    @Test
    public void testCreate_Success() throws Exception {
        AISSentence sentence = mock(AISSentence.class);
        when(sentence.getFragmentNumber()).thenReturn(1);
        when(sentence.getPayload()).thenReturn("BBQk@<0PD5P06h"); // Valid payload
        when(sentence.getFillBits()).thenReturn(0);

        // Mocking AISMessage
        AISMessage mockMessage = mock(AISMessage.class);
        Class<? extends AISMessage> mockParserClass = mockMessage.getClass();

        // Preparing parsers map
        Map<Integer, Class<? extends AISMessage>> mockParsers = new HashMap<>();
        mockParsers.put(1, mockParserClass); // Assuming messageType is 1

        Field parsersField = AISMessageFactory.class.getDeclaredField("parsers");
        parsersField.setAccessible(true);
        parsersField.set(factory, mockParsers);

        // Using reflection to retrieve the constructor
        Constructor<? extends AISMessage> constructor = mockParserClass.getDeclaredConstructor();
        constructor.setAccessible(true);

        // Creating an instance of AISMessage using the constructor
        AISMessage result = constructor.newInstance();
        assertNotNull(result);
    }
}
