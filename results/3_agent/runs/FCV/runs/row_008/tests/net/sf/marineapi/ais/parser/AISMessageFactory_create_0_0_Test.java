package net.sf.marineapi.ais.parser;

import net.sf.marineapi.ais.message.AISMessage;
import net.sf.marineapi.ais.util.Sixbit;
import net.sf.marineapi.nmea.parser.SentenceFactory;
import net.sf.marineapi.nmea.sentence.AISSentence;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class AISMessageFactory_create_0_0_Test {

    private final SentenceFactory sf = SentenceFactory.getInstance();
    private AISMessageFactory amf;
    private Map<Integer, Class<? extends AISMessage>> originalParsers;

    private String s1 = "!AIVDM,1,1,,A,13aEOK?P00PD2wVMdLDRhgvL289?,0*26";
    private final AISSentence single = (AISSentence) sf.createParser(s1);

    @Before
    public void setUp() throws Exception {
        amf = AISMessageFactory.getInstance();
        Field parsersField = AISMessageFactory.class.getDeclaredField("parsers");
        parsersField.setAccessible(true);
        originalParsers = (Map<Integer, Class<? extends AISMessage>>) parsersField.get(amf);
        Map<Integer, Class<? extends AISMessage>> mockParsers = new HashMap<>();
        mockParsers.put(1, MockAISMessage.class);
        parsersField.set(amf, mockParsers);
    }

    @After
    public void tearDown() throws Exception {
        Field parsersField = AISMessageFactory.class.getDeclaredField("parsers");
        parsersField.setAccessible(true);
        parsersField.set(amf, originalParsers);
    }

    @Test
    public void testCreate() {
        AISMessage msg = amf.create(single);
        assertNotNull(msg);
        assertTrue(msg instanceof MockAISMessage);
    }

    public static class MockAISMessage implements AISMessage {
        public MockAISMessage(Sixbit sixbit) {}
        @Override
        public int getMessageType() {
            return 1;
        }
        @Override
        public int getMMSI() {
            return 0;
        }
        @Override
        public int getRepeatIndicator() {
            return 0;
        }
    }
}
