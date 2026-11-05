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

    private SentenceFactory sentenceFactory;
    private AISMessageFactory aisMessageFactory;
    private Map<Integer, Class<? extends AISMessage>> originalParsers;

    private String s1 = "!AIVDM,1,1,,A,13aEOK?P00PD2wVMdLDRhgvL289?,0*26";
    private String s2_1 = "!AIVDM,2,1,9,B,53nFBv01SJ<thHp6220H4heHTf2222222222221?50:454o<`9QSlUDp,0*09";
    private String s2_2 = "!AIVDM,2,2,9,B,888888888888880,2*2E";

    private AISSentence single;
    private AISSentence split1;
    private AISSentence split2;

    @Before
    public void setUp() throws Exception {
        sentenceFactory = SentenceFactory.getInstance();
        aisMessageFactory = AISMessageFactory.getInstance();

        single = (AISSentence) sentenceFactory.createParser(s1);
        split1 = (AISSentence) sentenceFactory.createParser(s2_1);
        split2 = (AISSentence) sentenceFactory.createParser(s2_2);

        Field parsersField = AISMessageFactory.class.getDeclaredField("parsers");
        parsersField.setAccessible(true);
        originalParsers = new HashMap<>((Map<Integer, Class<? extends AISMessage>>) parsersField.get(aisMessageFactory));

        Map<Integer, Class<? extends AISMessage>> mockParsers = new HashMap<>();
        mockParsers.put(1, AISMessage01.class);
        mockParsers.put(5, AISMessage05.class);

        parsersField.set(aisMessageFactory, mockParsers);
    }

    @After
    public void tearDown() throws Exception {
        Field parsersField = AISMessageFactory.class.getDeclaredField("parsers");
        parsersField.setAccessible(true);
        parsersField.set(aisMessageFactory, originalParsers);
    }

    @Test
    public void testCreate() {
        try {
            AISMessage msg = aisMessageFactory.create(single);
            assertTrue(msg instanceof AISMessage01);
            assertEquals(1, msg.getMessageType());
        } catch (IllegalStateException e) {
            System.err.println("Error in testCreate: " + e.getMessage());
            e.printStackTrace();
            fail("Unexpected IllegalStateException: " + e.getMessage());
        }
    }

    @Test
    public void testCreateWithTwo() {
        try {
            AISMessage msg = aisMessageFactory.create(split1, split2);
            assertTrue(msg instanceof AISMessage05);
            assertEquals(5, msg.getMessageType());
        } catch (IllegalStateException e) {
            System.err.println("Error in testCreateWithTwo: " + e.getMessage());
            e.printStackTrace();
            fail("Unexpected IllegalStateException: " + e.getMessage());
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateWithIncorrectOrder() {
        aisMessageFactory.create(split2, split1);
    }
}
