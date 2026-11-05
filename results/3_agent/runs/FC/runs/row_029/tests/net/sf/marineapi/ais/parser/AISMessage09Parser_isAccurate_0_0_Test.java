package net.sf.marineapi.ais.parser;

import net.sf.marineapi.ais.util.Sixbit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.lang.reflect.Field;
import static org.junit.Assert.*;

public class AISMessage09Parser_isAccurate_0_0_Test {
    private AISMessage09Parser parser;
    private boolean originalPositionAccuracy;

    @Before
    public void setUp() throws Exception {
        // Provide a valid six-bit string with 168 bits
        String validSixbitString = "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@"; // 28 characters * 6 bits = 168 bits
        Sixbit sixbit = new Sixbit(validSixbitString, 0);
        parser = new AISMessage09Parser(sixbit);
        
        // Save the original value of fPositionAccuracy
        Field field = AISMessage09Parser.class.getDeclaredField("fPositionAccuracy");
        field.setAccessible(true);
        originalPositionAccuracy = field.getBoolean(parser);
    }

    @After
    public void tearDown() throws Exception {
        // Restore the original value of fPositionAccuracy
        Field field = AISMessage09Parser.class.getDeclaredField("fPositionAccuracy");
        field.setAccessible(true);
        field.setBoolean(parser, originalPositionAccuracy);
    }

    @Test
    public void testIsAccurate_true() throws Exception {
        Field field = AISMessage09Parser.class.getDeclaredField("fPositionAccuracy");
        field.setAccessible(true);
        field.setBoolean(parser, true);
        assertTrue(parser.isAccurate());
    }

    @Test
    public void testIsAccurate_false() throws Exception {
        Field field = AISMessage09Parser.class.getDeclaredField("fPositionAccuracy");
        field.setAccessible(true);
        field.setBoolean(parser, false);
        assertFalse(parser.isAccurate());
    }
}
