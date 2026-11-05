package net.sf.marineapi.ais.parser;

import net.sf.marineapi.ais.util.Sixbit;
import org.junit.Test;
import java.lang.reflect.Field;

import static org.junit.Assert.*;

/**
 * Unit test for AISMessage09Parser.isAccurate() method.
 */
public class AISMessage09Parser_isAccurate_0_0_Test {

    @Test
    public void testIsAccurate() throws Exception {
        // Arrange
        String payload = "95M2oQ@41Tr4L4H@eRvQ;2h20000";
        Sixbit sixbit = new Sixbit(payload, 0);
        AISMessage09Parser parser = new AISMessage09Parser(sixbit);

        // Use reflection to set the private field fPositionAccuracy
        Field positionAccuracyField = AISMessage09Parser.class.getDeclaredField("fPositionAccuracy");
        positionAccuracyField.setAccessible(true);
        positionAccuracyField.set(parser, true);

        // Act
        boolean result = parser.isAccurate();

        // Assert
        assertTrue(result);

        // Set the field to false and test again
        positionAccuracyField.set(parser, false);
        result = parser.isAccurate();
        assertFalse(result);
    }
}
