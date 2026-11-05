package net.sf.marineapi.ais.parser;

import static org.junit.Assert.*;

import java.lang.reflect.Field;

import org.junit.Before;
import org.junit.Test;

import net.sf.marineapi.ais.util.Sixbit;

public class AISMessage09Parser_isAccurate_0_0_Test {

    private AISMessage09Parser parser;

    @Before
    public void setUp() throws Exception {
        // Providing dummy payload of correct length (168 bits) and fillBits
        String payload = new String(new char[28]).replace("\0", "A"); // 28 * 6 = 168 bits
        Sixbit content = new Sixbit(payload, 0);
        parser = new AISMessage09Parser(content);
    }

    @Test
    public void testIsAccurateTrue() throws Exception {
        setPrivateField("fPositionAccuracy", true);
        assertTrue(parser.isAccurate());
    }

    @Test
    public void testIsAccurateFalse() throws Exception {
        setPrivateField("fPositionAccuracy", false);
        assertFalse(parser.isAccurate());
    }

    private void setPrivateField(String fieldName, Object value) throws Exception {
        Field field = AISMessage09Parser.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(parser, value);
    }
}
