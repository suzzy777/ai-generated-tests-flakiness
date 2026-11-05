package net.sf.marineapi.ais.parser;

import static org.junit.Assert.*;

import java.lang.reflect.Field;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import net.sf.marineapi.ais.util.Sixbit;

public class AISMessage09Parser_isAccurate_0_0_Test {

    private AISMessage09Parser parser;
    private boolean originalPositionAccuracy;

    @Before
    public void setUp() throws Exception {
        // Create a Sixbit instance with a valid payload and fill bits
        Sixbit content = new Sixbit("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@", 0);
        parser = new AISMessage09Parser(content);

        // Save the original value of the private field fPositionAccuracy
        Field positionAccuracyField = AISMessage09Parser.class.getDeclaredField("fPositionAccuracy");
        positionAccuracyField.setAccessible(true);
        originalPositionAccuracy = positionAccuracyField.getBoolean(parser);
    }

    @After
    public void tearDown() throws Exception {
        // Restore the original value of the private field fPositionAccuracy
        Field positionAccuracyField = AISMessage09Parser.class.getDeclaredField("fPositionAccuracy");
        positionAccuracyField.setAccessible(true);
        positionAccuracyField.setBoolean(parser, originalPositionAccuracy);
    }

    @Test
    public void testIsAccurateTrue() throws Exception {
        // Set the private field fPositionAccuracy to true
        Field positionAccuracyField = AISMessage09Parser.class.getDeclaredField("fPositionAccuracy");
        positionAccuracyField.setAccessible(true);
        positionAccuracyField.setBoolean(parser, true);

        // Test the isAccurate method
        assertTrue(parser.isAccurate());
    }

    @Test
    public void testIsAccurateFalse() throws Exception {
        // Set the private field fPositionAccuracy to false
        Field positionAccuracyField = AISMessage09Parser.class.getDeclaredField("fPositionAccuracy");
        positionAccuracyField.setAccessible(true);
        positionAccuracyField.setBoolean(parser, false);

        // Test the isAccurate method
        assertFalse(parser.isAccurate());
    }
}
