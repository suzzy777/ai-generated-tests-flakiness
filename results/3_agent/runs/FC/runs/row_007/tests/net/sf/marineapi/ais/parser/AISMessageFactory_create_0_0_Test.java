package net.sf.marineapi.ais.parser;

import net.sf.marineapi.ais.message.AISMessage;
import net.sf.marineapi.nmea.parser.SentenceFactory;
import net.sf.marineapi.nmea.sentence.AISSentence;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Test AISMessageFactory.create method
 */
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

    @Before
    public void setUp() throws Exception {
        sf = SentenceFactory.getInstance();
        amf = AISMessageFactory.getInstance();

        single = (AISSentence) sf.createParser(s1);
        split1 = (AISSentence) sf.createParser(s2_1);
        split2 = (AISSentence) sf.createParser(s2_2);

        // Save the original state of parsers
        Field parsersField = AISMessageFactory.class.getDeclaredField("parsers");
        parsersField.setAccessible(true);
        originalParsers = new HashMap<>((Map<Integer, Class<? extends AISMessage>>) parsersField.get(amf));

        // Set up the parsers map for testing
        Map<Integer, Class<? extends AISMessage>> testParsers = new HashMap<>();
        testParsers.put(1, AISMessage01Parser.class);
        testParsers.put(5, AISMessage05Parser.class);
        parsersField.set(amf, testParsers);
    }

    @After
    public void tearDown() throws Exception {
        // Restore the original state of parsers
        Field parsersField = AISMessageFactory.class.getDeclaredField("parsers");
        parsersField.setAccessible(true);
        parsersField.set(amf, originalParsers);
    }

    @Test
    public void testCreateSingle() {
        try {
            AISMessage msg = amf.create(single);
            assertNotNull(msg);
            assertEquals(1, msg.getMessageType());
        } catch (IllegalStateException e) {
            e.printStackTrace();
            fail("IllegalStateException thrown: " + e.getCause());
        }
    }

    @Test
    public void testCreateWithTwo() {
        try {
            AISMessage msg = amf.create(split1, split2);
            assertNotNull(msg);
            assertEquals(5, msg.getMessageType());
        } catch (IllegalStateException e) {
            e.printStackTrace();
            fail("IllegalStateException thrown: " + e.getCause());
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
}
