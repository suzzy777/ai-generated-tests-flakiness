package net.sf.marineapi.ais.parser;

import net.sf.marineapi.ais.util.Sixbit;
import org.junit.Test;
import static org.junit.Assert.*;

import java.lang.reflect.Field;

public class AISMessage09Parser_isAccurate_0_0_Test {

    @Test
    public void testIsAccurate() throws Exception {
        // Create a mock Sixbit object
        Sixbit sixbitMock = new Sixbit("13aG;o@034o?;oVQ@E`G?wvl0TKH", 0);
        
        // Instantiate AISMessage09Parser
        AISMessage09Parser parser = new AISMessage09Parser(sixbitMock);

        // Use reflection to set the private field fPositionAccuracy
        Field positionAccuracyField = AISMessage09Parser.class.getDeclaredField("fPositionAccuracy");
        positionAccuracyField.setAccessible(true);

        // Test when fPositionAccuracy is true
        positionAccuracyField.set(parser, true);
        assertTrue(parser.isAccurate());

        // Test when fPositionAccuracy is false
        positionAccuracyField.set(parser, false);
        assertFalse(parser.isAccurate());
    }
}
