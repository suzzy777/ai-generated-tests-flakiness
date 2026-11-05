package net.sf.marineapi.ais.parser;

import net.sf.marineapi.ais.message.AISMessage;
import net.sf.marineapi.ais.message.AISMessage01;
import net.sf.marineapi.ais.message.AISMessage05;
import net.sf.marineapi.nmea.parser.SentenceFactory;
import net.sf.marineapi.nmea.sentence.AISSentence;
import org.junit.Before;
import org.junit.Test;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import static org.junit.Assert.*;

public class AISMessageFactory_create_0_0_Test {

    private final SentenceFactory sf = SentenceFactory.getInstance();
    private AISMessageFactory amf;

    private String s1 = "!AIVDM,1,1,,A,13aEOK?P00PD2wVMdLDRhgvL289?,0*26";
    private String s2_1 = "!AIVDM,2,1,9,B,53nFBv01SJ<thHp6220H4heHTf2222222222221?50:454o<`9QSlUDp,0*09";
    private String s2_2 = "!AIVDM,2,2,9,B,888888888888880,2*2E";

    private AISSentence single;
    private AISSentence split1;
    private AISSentence split2;

    @Before
    public void setUp() throws Exception {
        amf = AISMessageFactory.getInstance();
        single = (AISSentence) sf.createParser(s1);
        split1 = (AISSentence) sf.createParser(s2_1);
        split2 = (AISSentence) sf.createParser(s2_2);

        // Use reflection to set the parsers map with concrete classes
        Field parsersField = AISMessageFactory.class.getDeclaredField("parsers");
        parsersField.setAccessible(true);
        Map<Integer, Class<? extends AISMessage>> parsers = new HashMap<>();
        parsers.put(1, AISMessage01Parser.class);
        parsers.put(5, AISMessage05Parser.class);
        parsersField.set(amf, parsers);
    }

    @Test
    public void testCreateSingleMessage() {
        try {
            AISMessage msg = amf.create(single);
            assertTrue(msg instanceof AISMessage01);
            assertEquals(1, msg.getMessageType());
        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception thrown: " + e + ", cause: " + e.getCause());
        }
    }

    @Test
    public void testCreateSplitMessage() {
        try {
            AISMessage msg = amf.create(split1, split2);
            assertTrue(msg instanceof AISMessage05);
            assertEquals(5, msg.getMessageType());
        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception thrown: " + e + ", cause: " + e.getCause());
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateWithUnknownMessageType() throws Exception {
        // Use reflection to clear the parsers map
        Field parsersField = AISMessageFactory.class.getDeclaredField("parsers");
        parsersField.setAccessible(true);
        Map<Integer, Class<? extends AISMessage>> parsers = new HashMap<>();
        parsersField.set(amf, parsers);

        amf.create(single);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateWithIncorrectOrder() {
        amf.create(split2, split1);
    }
}
