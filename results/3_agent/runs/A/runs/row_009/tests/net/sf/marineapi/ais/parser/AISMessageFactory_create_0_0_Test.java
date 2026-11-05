package net.sf.marineapi.ais.parser;

import net.sf.marineapi.ais.message.AISMessage;
import net.sf.marineapi.nmea.sentence.AISSentence;
import net.sf.marineapi.ais.util.Sixbit;
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
    private Map<Integer, Class<? extends AISMessage>> parsersMock;
    private AISSentence sentenceMock;
    private AISMessageParser parserMock;
    private Class<? extends AISMessage> messageClassMock;
    private Constructor<? extends AISMessage> constructorMock;
    private AISMessage messageMock;
    private Sixbit sixbitMock;

    @Before
    public void setUp() throws Exception {
        factory = AISMessageFactory.getInstance();

        // Use reflection to set the private parsers field
        Field parsersField = AISMessageFactory.class.getDeclaredField("parsers");
        parsersField.setAccessible(true);

        parsersMock = new HashMap<>();
        parsersField.set(factory, parsersMock);

        // Mock dependencies
        sentenceMock = mock(AISSentence.class);
        parserMock = mock(AISMessageParser.class);
        messageClassMock = mock(Class.class);
        
        messageMock = mock(AISMessage.class);
        sixbitMock = mock(Sixbit.class);

        // Mock behaviors
        when(sentenceMock.getPayload()).thenReturn("payload");
        when(sentenceMock.getFragmentNumber()).thenReturn(1);
        when(sentenceMock.getFillBits()).thenReturn(0);
        when(parserMock.getMessageType()).thenReturn(1);
        when(parserMock.getSixbit()).thenReturn(sixbitMock);

        parsersMock.put(1, messageClassMock);
        constructorMock = (Constructor<? extends AISMessage>) AISMessage.class.getConstructors()[0];
        doReturn(messageMock).when(constructorMock).newInstance(sixbitMock);
    }

    @Test
    public void testCreate_Success() throws Exception {
        // Test the create method
        AISMessage result = factory.create(sentenceMock);

        // Verify the result
        assertNotNull(result);
        assertEquals(messageMock, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreate_NoParser() throws Exception {
        // Remove parser to trigger exception
        parsersMock.remove(1);

        // Test the create method
        factory.create(sentenceMock);
    }

    @Test(expected = IllegalStateException.class)
    public void testCreate_ReflectionFailure() throws Exception {
        // Make constructor throw an exception
        when(constructorMock.newInstance(sixbitMock)).thenThrow(new ReflectiveOperationException());

        // Test the create method
        factory.create(sentenceMock);
    }
}
