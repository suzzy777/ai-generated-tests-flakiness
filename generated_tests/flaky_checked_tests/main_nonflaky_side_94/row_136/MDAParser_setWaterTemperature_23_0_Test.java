package net.sf.marineapi.nmea.parser;

import java.lang.reflect.Method;
import org.junit.Before;
import org.junit.Test;
import net.sf.marineapi.nmea.parser.MDAParser;
import net.sf.marineapi.nmea.sentence.TalkerId;
import org.mockito.*;
import org.junit.After;
import org.junit.Assert;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import net.sf.marineapi.nmea.sentence.MDASentence;

public class MDAParser_setWaterTemperature_23_0_Test {

    private MDAParser parser;

    @Before
    public void setUp() {
        parser = new MDAParser(TalkerId.GP);
    }

    @Test
    public void testSetWaterTemperature() throws Exception {
        double expectedTemperature = 25.5;
        // Invoke the method to set the water temperature
        parser.setWaterTemperature(expectedTemperature);
        // Use reflection to access the private method getWaterTemperature
        Method getWaterTemperatureMethod = MDAParser.class.getDeclaredMethod("getWaterTemperature");
        getWaterTemperatureMethod.setAccessible(true);
        // Retrieve the water temperature value
        double actualTemperature = (double) getWaterTemperatureMethod.invoke(parser);
        // Check if the water temperature was set correctly
        assertEquals(expectedTemperature, actualTemperature, 0.001);
    }

    @Test
    public void testSetWaterTemperatureWithNegativeValue() throws Exception {
        double negativeTemperature = -5.0;
        // Invoke the method to set the water temperature
        parser.setWaterTemperature(negativeTemperature);
        // Use reflection to access the private method getWaterTemperature
        Method getWaterTemperatureMethod = MDAParser.class.getDeclaredMethod("getWaterTemperature");
        getWaterTemperatureMethod.setAccessible(true);
        // Retrieve the water temperature value
        double actualTemperature = (double) getWaterTemperatureMethod.invoke(parser);
        // Check if the water temperature was set correctly
        assertEquals(negativeTemperature, actualTemperature, 0.001);
    }
}
