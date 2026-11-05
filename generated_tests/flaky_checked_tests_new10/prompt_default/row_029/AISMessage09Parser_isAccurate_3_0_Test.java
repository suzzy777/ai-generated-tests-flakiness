package net.sf.marineapi.ais.parser;

import org.junit.Before;
import org.junit.Test;
import java.lang.reflect.Field;
import net.sf.marineapi.ais.parser.AISMessage09Parser;
import net.sf.marineapi.ais.util.Sixbit;
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
import net.sf.marineapi.ais.util.Longitude28;
import net.sf.marineapi.ais.util.SpeedOverGround;
import net.sf.marineapi.ais.util.TimeStamp;
import java.text.DecimalFormat;

public class AISMessage09Parser_isAccurate_3_0_Test {

    private AISMessage09Parser parser;

    @Before
    public void setUp() {
        Sixbit mockSixbit = Mockito.mock(Sixbit.class);
        // Assuming the Sixbit mock needs to be set up to prevent IllegalArgumentException
        // You may need to adjust the setup according to actual Sixbit class behavior
        // Example length, adjust according to needs
        Mockito.when(mockSixbit.length()).thenReturn(168);
        parser = new AISMessage09Parser(mockSixbit);
    }

    @Test
    public void testIsAccurateTrue() throws Exception {
        setPrivateField("fPositionAccuracy", true);
        assertTrue(parser.isAccurate());
    }

    @Test
    public void testIsAccurateFalse() throws Exception {
        setPrivateField("fPositionAccuracy", false);
        assertFalse(parser.isAccurate());
    }

    private void setPrivateField(String fieldName, Object value) throws Exception {
        Field field = AISMessage09Parser.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(parser, value);
    }
}
