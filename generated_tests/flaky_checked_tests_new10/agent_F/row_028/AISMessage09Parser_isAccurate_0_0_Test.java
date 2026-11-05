package net.sf.marineapi.ais.parser;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import net.sf.marineapi.ais.util.Sixbit;

public class AISMessage09Parser_isAccurate_0_0_Test {

    private AISMessage09Parser parser;

    @Before
    public void setUp() throws Exception {
        // Create a Sixbit object with a length of 168
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 28; i++) { // 28 * 6 = 168 bits
            sb.append("101010");
        }
        String payload = sb.toString();
        Sixbit sixbit = new Sixbit(payload, 0); // No fill bits
        
        // Create parser instance with the Sixbit object
        parser = new AISMessage09Parser(sixbit);
        
        // Set the fPositionAccuracy field using reflection
        Field positionAccuracyField = AISMessage09Parser.class.getDeclaredField("fPositionAccuracy");
        positionAccuracyField.setAccessible(true);
        positionAccuracyField.set(parser, true); // Set to true for testing
    }

    @Test
    public void testIsAccurate() {
        assertTrue("The position accuracy should be true", parser.isAccurate());
    }

    @Test
    public void testIsNotAccurate() throws Exception {
        // Set the fPositionAccuracy field to false
        Field positionAccuracyField = AISMessage09Parser.class.getDeclaredField("fPositionAccuracy");
        positionAccuracyField.setAccessible(true);
        positionAccuracyField.set(parser, false); // Set to false for testing

        assertFalse("The position accuracy should be false", parser.isAccurate());
    }
}
