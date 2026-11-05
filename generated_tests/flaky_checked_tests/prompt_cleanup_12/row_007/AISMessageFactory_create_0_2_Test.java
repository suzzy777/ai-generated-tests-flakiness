package net.sf.marineapi.ais.parser;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import net.sf.marineapi.ais.message.AISMessage;
import net.sf.marineapi.ais.util.Sixbit;
import net.sf.marineapi.nmea.sentence.AISSentence;
import net.sf.marineapi.ais.parser.AISMessageParser;
import net.sf.marineapi.ais.parser.AISMessageFactory;
import org.junit.Assert;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.*;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class AISMessageFactory_create_0_2_Test {

    private AISMessageFactory factory;

    private Map<Integer, Class<? extends AISMessage>> originalParsers;

    private Map<Integer, Class<? extends AISMessage>> testParsers;

    @Before
    public void setUp() throws Exception {
        factory = AISMessageFactory.getInstance();
        // Save the original parsers map
        Field parsersField = AISMessageFactory.class.getDeclaredField("parsers");
        parsersField.setAccessible(true);
        originalParsers = (Map<Integer, Class<? extends AISMessage>>) parsersField.get(factory);
        // Set up a test parsers map
        testParsers = new HashMap<>();
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
    public void testCreate_noParserForMessageType() {
        AISSentence sentence = mock(AISSentence.class);
        when(sentence.getFragmentNumber()).thenReturn(1);
        when(sentence.getPayload()).thenReturn("somePayload");
        when(sentence.getFillBits()).thenReturn(0);
        when(sentence.isFragmented()).thenReturn(false);
        factory.create(sentence);
    }
}
