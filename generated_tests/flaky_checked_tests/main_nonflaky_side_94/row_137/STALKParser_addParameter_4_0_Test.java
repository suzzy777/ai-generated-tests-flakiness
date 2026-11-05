package net.sf.marineapi.nmea.parser;

import net.sf.marineapi.nmea.parser.STALKParser;
import net.sf.marineapi.nmea.sentence.TalkerId;
import org.junit.Before;
import org.junit.Test;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.mockito.*;
import org.junit.After;
import org.junit.Assert;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import net.sf.marineapi.nmea.sentence.STALKSentence;
import net.sf.marineapi.nmea.sentence.SentenceId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class STALKParser_addParameter_4_0_Test {

    private STALKParser parser;

    @Before
    public void setUp() {
        parser = new STALKParser(TalkerId.ST);
    }

    @Test
    public void testAddParameter() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        // Initial parameters
        String[] initialParams = { "param1", "param2" };
        // Using reflection to set initial parameters
        Method setParametersMethod = STALKParser.class.getDeclaredMethod("setParameters", String[].class);
        setParametersMethod.setAccessible(true);
        setParametersMethod.invoke(parser, (Object) initialParams);
        // Add new parameter
        String newParam = "param3";
        parser.addParameter(newParam);
        // Expected parameters after addition
        String[] expectedParams = { "param1", "param2", "param3" };
        // Using reflection to get parameters
        Method getParametersMethod = STALKParser.class.getDeclaredMethod("getParameters");
        getParametersMethod.setAccessible(true);
        String[] actualParams = (String[]) getParametersMethod.invoke(parser);
        // Assert the parameters are as expected
        assertArrayEquals(expectedParams, actualParams);
    }
}
