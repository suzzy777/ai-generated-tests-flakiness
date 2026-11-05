package net.sf.marineapi.ais.parser;

import net.sf.marineapi.ais.util.Sixbit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.*;

public class AISMessage09Parser_isAccurate_0_0_Test {

    private AISMessage09Parser parser;
    private Field fPositionAccuracyField;
    private boolean originalPositionAccuracy;

    @Before
    public void setUp() throws Exception {
        // Initialize the Sixbit content
        Sixbit sixbit = new Sixbit("95M2oQ@41Tr4L4H@eRvQ;2h20000", 0);
        parser = new AISMessage09Parser(sixbit);

        // Access the private field fPositionAccuracy
        fPositionAccuracyField = AISMessage09Parser.class.getDeclaredField("fPositionAccuracy");
        fPositionAccuracyField.setAccessible(true);

        // Save the original value of fPositionAccuracy
        originalPositionAccuracy = fPositionAccuracyField.getBoolean(parser);
    }

    @After
    public void tearDown() throws Exception {
        // Restore the original value of fPositionAccuracy
        fPositionAccuracyField.setBoolean(parser, originalPositionAccuracy);
    }

    @Test
    public void testIsAccurateTrue() throws Exception {
        // Set fPositionAccuracy to true
        fPositionAccuracyField.setBoolean(parser, true);

        // Test if isAccurate returns true
        assertTrue(parser.isAccurate());
    }

    @Test
    public void testIsAccurateFalse() throws Exception {
        // Set fPositionAccuracy to false
        fPositionAccuracyField.setBoolean(parser, false);

        // Test if isAccurate returns false
        assertFalse(parser.isAccurate());
    }
}
