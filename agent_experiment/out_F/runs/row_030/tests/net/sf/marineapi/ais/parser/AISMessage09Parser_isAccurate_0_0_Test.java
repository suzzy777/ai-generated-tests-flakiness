package net.sf.marineapi.ais.parser;

import net.sf.marineapi.ais.util.Sixbit;
import org.junit.Test;
import java.lang.reflect.Field;

import static org.junit.Assert.*;

public class AISMessage09Parser_isAccurate_0_0_Test {

    @Test
    public void testIsAccurateTrue() throws Exception {
        // Arrange
        String payload = "95M2oQ@41Tr4L4H@eRvQ;2h20000";
        Sixbit sixbit = new Sixbit(payload, 0);
        AISMessage09Parser parser = new AISMessage09Parser(sixbit);

        // Use reflection to set the private field
        Field fPositionAccuracy = AISMessage09Parser.class.getDeclaredField("fPositionAccuracy");
        fPositionAccuracy.setAccessible(true);
        fPositionAccuracy.set(parser, true);

        // Act
        boolean result = parser.isAccurate();

        // Assert
        assertTrue(result);
    }

    @Test
    public void testIsAccurateFalse() throws Exception {
        // Arrange
        String payload = "95M2oQ@41Tr4L4H@eRvQ;2h20000";
        Sixbit sixbit = new Sixbit(payload, 0);
        AISMessage09Parser parser = new AISMessage09Parser(sixbit);

        // Use reflection to set the private field
        Field fPositionAccuracy = AISMessage09Parser.class.getDeclaredField("fPositionAccuracy");
        fPositionAccuracy.setAccessible(true);
        fPositionAccuracy.set(parser, false);

        // Act
        boolean result = parser.isAccurate();

        // Assert
        assertFalse(result);
    }
}
