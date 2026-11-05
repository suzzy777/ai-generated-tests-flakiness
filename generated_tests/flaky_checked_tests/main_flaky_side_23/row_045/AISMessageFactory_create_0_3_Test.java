package net.sf.marineapi.ais.parser;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import net.sf.marineapi.ais.message.AISMessage;
import net.sf.marineapi.ais.parser.AISMessageParser;
import net.sf.marineapi.ais.util.Sixbit;
import net.sf.marineapi.nmea.sentence.AISSentence;
import org.junit.Assert;
import org.mockito.*;
import org.junit.After;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

public class AISMessageFactory_create_0_3_Test {

    private AISMessageFactory factory;

    private Map<Integer, Class<? extends AISMessage>> parsers;

    @Before
    public void setUp() throws Exception {
        factory = AISMessageFactory.getInstance();
        parsers = new HashMap<>();
        // Set the parsers map using reflection
        java.lang.reflect.Field parsersField = AISMessageFactory.class.getDeclaredField("parsers");
        parsersField.setAccessible(true);
        parsersField.set(factory, parsers);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreate_NoParserForMessageType() {
        AISSentence sentence = mock(AISSentence.class);
        when(sentence.getFragmentNumber()).thenReturn(1);
        when(sentence.getPayload()).thenReturn("payload");
        when(sentence.getFillBits()).thenReturn(0);
        AISSentence[] sentences = { sentence };
        factory.create(sentences);
    }
}
