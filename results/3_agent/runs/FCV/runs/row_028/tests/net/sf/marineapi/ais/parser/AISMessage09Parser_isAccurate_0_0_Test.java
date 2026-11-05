package net.sf.marineapi.ais.parser;

import net.sf.marineapi.ais.util.Sixbit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.*;

public class AISMessage09Parser_isAccurate_0_0_Test {

    private AISMessage09Parser parser;
    private boolean originalPositionAccuracy;

    @Before
    public void setUp() throws Exception {
        String payload = "95M2oQ@41Tr4L4H@eRvQ;2h20000";
        Sixbit sixbit = new Sixbit(payload, 0);
        parser = new AISMessage09Parser(sixbit);

        // Save the original value of fPositionAccuracy
        Field field = AISMessage09Parser.class.getDeclaredField("fPositionAccuracy");
        field.setAccessible(true);
        originalPositionAccuracy = field.getBoolean(parser);
    }

    @After
    public void tearDown() throws Exception {
        // Restore the original value of fPositionAccuracy
        Field field = AISMessage09Parser.class.getDeclaredField("fPositionAccuracy");
        field.setAccessible(true);
        field.setBoolean(parser, originalPositionAccuracy);
    }

    @Test
    public void testIsAccurateTrue() throws Exception {
        Field field = AISMessage09Parser.class.getDeclaredField("fPositionAccuracy");
        field.setAccessible(true);
        field.setBoolean(parser, true);

        assertTrue(parser.isAccurate());
    }

    @Test
    public void testIsAccurateFalse() throws Exception {
        Field field = AISMessage09Parser.class.getDeclaredField("fPositionAccuracy");
        field.setAccessible(true);
        field.setBoolean(parser, false);

        assertFalse(parser.isAccurate());
    }
}
