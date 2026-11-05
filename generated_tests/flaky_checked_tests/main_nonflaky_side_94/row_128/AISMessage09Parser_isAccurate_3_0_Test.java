package net.sf.marineapi.ais.parser;

import org.junit.Before;
import org.junit.Test;
import java.lang.reflect.Field;
import net.sf.marineapi.ais.util.Sixbit;
import org.junit.After;
import org.junit.Assert;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import net.sf.marineapi.ais.message.AISMessage09;
import net.sf.marineapi.ais.util.AISRuleViolation;
import net.sf.marineapi.ais.util.Angle12;
import net.sf.marineapi.ais.util.Latitude27;
import net.sf.marineapi.ais.util.Longitude28;
import net.sf.marineapi.ais.util.SpeedOverGround;
import net.sf.marineapi.ais.util.TimeStamp;
import java.text.DecimalFormat;
import org.mockito.*;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class AISMessage09Parser_isAccurate_3_0_Test {

    private AISMessage09Parser parser;

    @Before
    public void setUp() throws Exception {
        // Mocking Sixbit with a valid content length for AISMessage09Parser
        Sixbit content = mock(Sixbit.class);
        // Assuming that the content length should match the expected length
        // Example valid length for AIS message type 9
        when(content.length()).thenReturn(168);
        parser = new AISMessage09Parser(content);
        // Using reflection to set the private field fPositionAccuracy
        Field positionAccuracyField = AISMessage09Parser.class.getDeclaredField("fPositionAccuracy");
        positionAccuracyField.setAccessible(true);
        // Set initial value for testing
        positionAccuracyField.set(parser, false);
    }

    @Test
    public void testIsAccurateFalse() throws Exception {
        // Ensure fPositionAccuracy is set to false
        Field positionAccuracyField = AISMessage09Parser.class.getDeclaredField("fPositionAccuracy");
        positionAccuracyField.setAccessible(true);
        positionAccuracyField.set(parser, false);
        // Invoke the method and assert
        assertFalse(parser.isAccurate());
    }

    @Test
    public void testIsAccurateTrue() throws Exception {
        // Set fPositionAccuracy to true
        Field positionAccuracyField = AISMessage09Parser.class.getDeclaredField("fPositionAccuracy");
        positionAccuracyField.setAccessible(true);
        positionAccuracyField.set(parser, true);
        // Invoke the method and assert
        assertTrue(parser.isAccurate());
    }
}
