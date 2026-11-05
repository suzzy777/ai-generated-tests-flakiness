package net.sf.marineapi.ais.parser;

import net.sf.marineapi.ais.message.AISMessage;
import net.sf.marineapi.ais.message.AISMessage01;
import net.sf.marineapi.ais.message.AISMessage05;
import net.sf.marineapi.nmea.parser.SentenceFactory;
import net.sf.marineapi.nmea.sentence.AISSentence;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class AISMessageFactory_create_0_0_Test {

    private AISMessageFactory factory;
    private Map<Integer, Class<? extends AISMessage>> originalParsers;

    @Before
    public void setUp() throws Exception {
        factory = AISMessageFactory.getInstance();
        Field parsersField = AISMessageFactory.class.getDeclaredField("parsers");
        parsersField.setAccessible(true);
        originalParsers = (Map<Integer, Class<? extends AISMessage>>) parsersField.get(factory);

        // Set up a new parsers map for testing
        Map<Integer, Class<? extends AISMessage>> testParsers = new HashMap<>();
        testParsers.put(1, AISMessage01.class);
        testParsers.put(5, AISMessage05.class);
        parsersField.set(factory, testParsers);
    }

    @After
    public void tearDown() throws Exception {
        // Restore the original parsers map
        Field parsersField = AISMessageFactory.class.getDeclaredField("parsers");
        parsersField.setAccessible(true);
        parsersField.set(factory, originalParsers);
    }

    @Test
    public void testCreateSingleSentence() {
        SentenceFactory sf = SentenceFactory.getInstance();
        AISSentence single = (AISSentence) sf.createParser("!AIVDM,1,1,,A,13aEOK?P00PD2wVMdLDRhgvL289?,0*26");
        AISMessage msg = factory.create(single);
        assertTrue(msg instanceof AISMessage01);
        assertEquals(1, msg.getMessageType());
    }

    @Test
    public void testCreateMultipleSentences() {
        SentenceFactory sf = SentenceFactory.getInstance();
        AISSentence split1 = (AISSentence) sf.createParser("!AIVDM,2,1,3,B,55P5TL01VIaAL@7WKO@mBplU@<PDhh000000001S;AJ::4A80?4i@E53,0*3E");
        AISSentence split2 = (AISSentence) sf.createParser("!AIVDM,2,2,3,B,1@0000000000000,2*55");
        AISMessage msg = factory.create(split1, split2);
        assertTrue(msg instanceof AISMessage05);
        assertEquals(5, msg.getMessageType());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateWithIncorrectOrder() {
        SentenceFactory sf = SentenceFactory.getInstance();
        AISSentence split1 = (AISSentence) sf.createParser("!AIVDM,2,1,3,B,55P5TL01VIaAL@7WKO@mBplU@<PDhh000000001S;AJ::4A80?4i@E53,0*3E");
        AISSentence split2 = (AISSentence) sf.createParser("!AIVDM,2,2,3,B,1@0000000000000,2*55");
        factory.create(split2, split1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateWithNoParser() throws Exception {
        // Remove all parsers to simulate no parser for message type
        Field parsersField = AISMessageFactory.class.getDeclaredField("parsers");
        parsersField.setAccessible(true);
        parsersField.set(factory, new HashMap<>());

        SentenceFactory sf = SentenceFactory.getInstance();
        AISSentence single = (AISSentence) sf.createParser("!AIVDM,1,1,,A,13aEOK?P00PD2wVMdLDRhgvL289?,0*26");
        factory.create(single);
    }
}
