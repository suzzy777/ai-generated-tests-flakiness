package net.sf.marineapi.ais.parser;

import net.sf.marineapi.ais.parser.AISMessage09Parser;
import net.sf.marineapi.ais.util.Longitude28;
import net.sf.marineapi.ais.util.Sixbit;
import org.junit.Before;
import org.junit.Test;
import java.lang.reflect.Field;
import org.mockito.*;
import org.junit.After;
import org.junit.Assert;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import net.sf.marineapi.ais.message.AISMessage09;
import net.sf.marineapi.ais.util.AISRuleViolation;
import net.sf.marineapi.ais.util.Angle12;
import net.sf.marineapi.ais.util.Latitude27;
import net.sf.marineapi.ais.util.SpeedOverGround;
import net.sf.marineapi.ais.util.TimeStamp;
import java.text.DecimalFormat;

public class AISMessage09Parser_getLongitudeInDegrees_4_1_Test {

    private AISMessage09Parser parser;

    private Sixbit mockSixbit;

    @Before
    public void setUp() throws Exception {
        mockSixbit = mock(Sixbit.class);
        // Ensure the length is correct to avoid IllegalArgumentException
        when(mockSixbit.length()).thenReturn(168);
        parser = new AISMessage09Parser(mockSixbit);
        // Use reflection to set the private field fLongitude
        Field longitudeField = AISMessage09Parser.class.getDeclaredField("fLongitude");
        longitudeField.setAccessible(true);
        // Set a test value for fLongitude
        longitudeField.set(parser, 12345678);
    }

    @Test
    public void testGetLongitudeInDegrees() {
        double expected = Longitude28.toDegrees(12345678);
        double actual = parser.getLongitudeInDegrees();
        assertEquals(expected, actual, 0.0001);
    }
}
