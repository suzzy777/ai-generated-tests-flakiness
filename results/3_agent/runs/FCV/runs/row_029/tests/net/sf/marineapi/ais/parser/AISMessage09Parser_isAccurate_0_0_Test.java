package net.sf.marineapi.ais.parser;

import net.sf.marineapi.ais.util.Sixbit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class AISMessage09Parser_isAccurate_0_0_Test {

    private AISMessage09Parser parser;
    private boolean originalPositionAccuracy;

    @Before
    public void setUp() {
        // Initialize with known payload
        String payload = "95M2oQ@41Tr4L4H@eRvQ;2h20000";
        Sixbit sixbit = new Sixbit(payload, 0);
        parser = new AISMessage09Parser(sixbit);

        // Save original state
        originalPositionAccuracy = parser.isAccurate();
    }

    @After
    public void tearDown() {
        // Restore original state if modified
        if (parser.isAccurate() != originalPositionAccuracy) {
            setPrivateField("fPositionAccuracy", originalPositionAccuracy);
        }
    }

    @Test
    public void testIsAccurateTrue() {
        // Ensure the method returns true
        assertTrue(parser.isAccurate());
    }

    @Test
    public void testIsAccurateFalse() {
        // Modify the accuracy flag
        setPrivateField("fPositionAccuracy", false);

        // Ensure the method returns false
        assertFalse(parser.isAccurate());
    }

    private void setPrivateField(String fieldName, boolean value) {
        try {
            java.lang.reflect.Field field = AISMessage09Parser.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(parser, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
