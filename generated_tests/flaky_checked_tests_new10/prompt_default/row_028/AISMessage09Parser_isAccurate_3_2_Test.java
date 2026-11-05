package net.sf.marineapi.ais.parser;

import org.junit.Before;
import org.junit.Test;
import java.lang.reflect.Field;
import net.sf.marineapi.ais.util.Sixbit;
import net.sf.marineapi.ais.parser.AISMessage09Parser;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.*;
import org.junit.After;
import org.junit.Assert;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;
import net.sf.marineapi.ais.message.AISMessage09;
import net.sf.marineapi.ais.util.AISRuleViolation;
import net.sf.marineapi.ais.util.Angle12;
import net.sf.marineapi.ais.util.Latitude27;
import net.sf.marineapi.ais.util.Longitude28;
import net.sf.marineapi.ais.util.SpeedOverGround;
import net.sf.marineapi.ais.util.TimeStamp;
import java.text.DecimalFormat;

@RunWith(MockitoJUnitRunner.class)
public class AISMessage09Parser_isAccurate_3_2_Test {

    private AISMessage09Parser parser;

    @Before
    public void setUp() throws Exception {
        // Create a mock or an instance of Sixbit with appropriate constructor parameters
        Sixbit content = mock(Sixbit.class);
        // Mock the behavior of Sixbit to avoid IllegalArgumentException
        // Assuming 168 is the correct length
        when(content.length()).thenReturn(168);
        parser = new AISMessage09Parser(content);
        // Use reflection to set the private field fPositionAccuracy
        Field accuracyField = AISMessage09Parser.class.getDeclaredField("fPositionAccuracy");
        accuracyField.setAccessible(true);
        // Set to true for isAccurate to return true
        accuracyField.set(parser, true);
    }

    @Test
    public void testIsAccurateTrue() {
        assertTrue(parser.isAccurate());
    }

    @Test
    public void testIsAccurateFalse() throws Exception {
        // Use reflection to set the private field fPositionAccuracy
        Field accuracyField = AISMessage09Parser.class.getDeclaredField("fPositionAccuracy");
        accuracyField.setAccessible(true);
        // Set to false for isAccurate to return false
        accuracyField.set(parser, false);
        assertFalse(parser.isAccurate());
    }
}
