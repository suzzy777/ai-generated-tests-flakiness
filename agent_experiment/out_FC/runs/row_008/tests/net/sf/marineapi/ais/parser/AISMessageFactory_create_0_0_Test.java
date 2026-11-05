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

    private SentenceFactory sf;
    private AISMessageFactory amf;
    private Map<Integer, Class<? extends AISMessage>> originalParsers;

    private String s1 = "!AIVDM,1,1,,A,13aEOK?P00PD2wVMdLDRhgvL289?,0*26";
    private String s2_1 = "!AIVDM,2,1,9,B,53nFBv01SJ<thHp6220H4heHTf2222222222221?50:454o<`9QSlUDp,0*09";
    private String s2_2 = "!AIVDM,2,2,9,B,888888888888880,2*2E";

    private AISSentence single;
    private AISSentence split1;
    private AISSentence split2;

    // Mock classes with the required constructor
    public static class MockAISMessage01 implements AISMessage {
        public MockAISMessage01(Sixbit sixbit) {}
        public int getMessageType() { return 1; }
        public int getMMSI() { return 123456789; }
        public int getRepeatIndicator() { return 0; }
    }
    public static class MockAISMessage05 implements AISMessage {
        public MockAISMessage05(Sixbit sixbit) {}
        public int getMessageType() { return 5; }
        public int getMMSI() { return 987654321; }
        public int getRepeatIndicator() { return 0; }
    }

    @Before
    public void setUp() throws Exception {
        sf = SentenceFactory.getInstance();
        amf = AISMessageFactory.getInstance();

        single = (AISSentence) sf.createParser(s1);
        split1 = (AISSentence) sf.createParser(s2_1);
        split2 = (AISSentence) sf.createParser(s2_2);

        Field parsersField = AISMessageFactory.class.getDeclaredField("parsers");
        parsersField.setAccessible(true);
        originalParsers = new HashMap<>((Map<Integer, Class<? extends AISMessage>>) parsersField.get(amf));

        Map<Integer, Class<? extends AISMessage>> mockParsers = new HashMap<>();
        mockParsers.put(1, MockAISMessage01.class);
        mockParsers.put(5, MockAISMessage05.class);
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
        try {
            AISMessage msg = amf.create(single);
            assertTrue(msg instanceof MockAISMessage01);
            assertEquals(1, msg.getMessageType());
        } catch (Exception e) {
            Throwable cause = e.getCause();
            if (cause != null) {
                cause.printStackTrace();
                fail("Exception thrown: " + cause.getMessage());
            } else {
                e.printStackTrace();
                fail("Exception thrown: " + e.getMessage());
            }
        }
    }

    @Test
    public void testCreateWithTwo() {
        try {
            AISMessage msg = amf.create(split1, split2);
            assertTrue(msg instanceof MockAISMessage05);
            assertEquals(5, msg.getMessageType());
        } catch (Exception e) {
            Throwable cause = e.getCause();
            if (cause != null) {
                cause.printStackTrace();
                fail("Exception thrown: " + cause.getMessage());
            } else {
                e.printStackTrace();
                fail("Exception thrown: " + e.getMessage());
            }
        }
    }

    @Test
    public void testCreateWithIncorrectOrder() {
        try {
            amf.create(split2, split1);
            fail("AISMessageFactory didn't throw on incorrect order");
        } catch (IllegalArgumentException iae) {
            assertEquals("Incorrect order of AIS sentences", iae.getMessage());
        } catch (Exception e) {
            fail("Unexpected exception thrown from AISMessageFactory");
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateWithUnknownMessageType() throws Exception {
        Field parsersField = AISMessageFactory.class.getDeclaredField("parsers");
        parsersField.setAccessible(true);
        Map<Integer, Class<? extends AISMessage>> mockParsers = new HashMap<>();
        parsersField.set(amf, mockParsers);

        amf.create(single);
    }
}
