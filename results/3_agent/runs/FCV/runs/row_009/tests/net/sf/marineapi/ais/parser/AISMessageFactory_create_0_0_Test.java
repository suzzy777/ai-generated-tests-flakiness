package net.sf.marineapi.ais.parser;

import net.sf.marineapi.ais.message.AISMessage;
import net.sf.marineapi.ais.util.Sixbit;
import net.sf.marineapi.nmea.parser.SentenceFactory;
import net.sf.marineapi.nmea.sentence.AISSentence;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class AISMessageFactory_create_0_0_Test {

    private AISMessageFactory factory;
    private Map<Integer, Class<? extends AISMessage>> originalParsers;
    private final SentenceFactory sf = SentenceFactory.getInstance();

    @Before
    public void setUp() throws Exception {
        factory = AISMessageFactory.getInstance();

        Field parsersField = AISMessageFactory.class.getDeclaredField("parsers");
        parsersField.setAccessible(true);
        originalParsers = (Map<Integer, Class<? extends AISMessage>>) parsersField.get(factory);

        Map<Integer, Class<? extends AISMessage>> mockParsers = new HashMap<>();
        mockParsers.put(1, MockAISMessage.class);
        mockParsers.put(5, MockAISMessage.class);

        parsersField.set(factory, mockParsers);
    }

    @After
    public void tearDown() throws Exception {
        Field parsersField = AISMessageFactory.class.getDeclaredField("parsers");
        parsersField.setAccessible(true);
        parsersField.set(factory, originalParsers);
    }

    @Test
    public void testCreateSingleSentence() {
        AISSentence sentence = (AISSentence) sf.createParser("!AIVDM,1,1,,A,13aEOK?P00PD2wVMdLDRhgvL289?,0*26");
        AISMessage message = factory.create(sentence);
        assertTrue(message instanceof MockAISMessage);
        assertEquals(1, message.getMessageType());
    }

    @Test
    public void testCreateMultipleSentences() {
        AISSentence sentence1 = (AISSentence) sf.createParser("!AIVDM,2,1,9,B,53nFBv01SJ<thHp6220H4heHTf2222222222221?50:454o<`9QSlUDp,0*09");
        AISSentence sentence2 = (AISSentence) sf.createParser("!AIVDM,2,2,9,B,888888888888880,2*2E");
        AISMessage message = factory.create(sentence1, sentence2);
        assertTrue(message instanceof MockAISMessage);
        assertEquals(5, message.getMessageType());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateWithIncorrectOrder() {
        AISSentence sentence1 = (AISSentence) sf.createParser("!AIVDM,2,2,9,B,888888888888880,2*2E");
        AISSentence sentence2 = (AISSentence) sf.createParser("!AIVDM,2,1,9,B,53nFBv01SJ<thHp6220H4heHTf2222222222221?50:454o<`9QSlUDp,0*09");
        factory.create(sentence1, sentence2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateWithUnknownType() throws Exception {
        Field parsersField = AISMessageFactory.class.getDeclaredField("parsers");
        parsersField.setAccessible(true);
        Map<Integer, Class<? extends AISMessage>> mockParsers = new HashMap<>();
        parsersField.set(factory, mockParsers);
        
        AISSentence sentence = (AISSentence) sf.createParser("!AIVDM,1,1,,A,13aEOK?P00PD2wVMdLDRhgvL289?,0*26");
        factory.create(sentence);
    }

    public static class MockAISMessage implements AISMessage {
        private final int messageType;

        public MockAISMessage(Sixbit sixbit) {
            this.messageType = sixbit.getInt(0, 6);
        }

        @Override
        public int getMessageType() {
            return messageType;
        }

        @Override
        public int getMMSI() {
            return 0; // Return a default value for testing purposes
        }

        @Override
        public int getRepeatIndicator() {
            return 0; // Return a default value for testing purposes
        }
    }
}
