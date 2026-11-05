package net.sf.marineapi.nmea.parser;

import net.sf.marineapi.nmea.sentence.TalkerId;
import org.junit.Before;
import org.junit.Test;
import java.lang.reflect.Field;
import java.util.Arrays;
import net.sf.marineapi.nmea.sentence.GSTSentence;
import net.sf.marineapi.nmea.sentence.SentenceId;
import net.sf.marineapi.nmea.util.Time;
import net.sf.marineapi.nmea.util.Units;
import org.junit.Assert;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.*;
import org.junit.After;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class GSTParser_getSemiMajorError_2_3_Test {

    private GSTParser parser;

    @Before
    public void setUp() throws Exception {
        // Create a GSTParser instance using reflection
        parser = new GSTParser("$GPGST,123456,0.5,1.2,0.8,45.0,0.3,0.4,0.5");
        // Use reflection to set the private fields
        Field fieldsField = SentenceParser.class.getDeclaredField("fields");
        fieldsField.setAccessible(true);
        fieldsField.set(parser, Arrays.asList("123456", "0.5", "1.2", "0.8", "45.0", "0.3", "0.4", "0.5"));
    }

    @Test
    public void testGetSemiMajorError() throws Exception {
        double semiMajorError = parser.getSemiMajorError();
        assertEquals(1.2, semiMajorError, 0.0);
    }
}
