package net.sf.marineapi.ais.parser;

import java.lang.reflect.Field;
import org.junit.Before;
import org.junit.Test;
import net.sf.marineapi.ais.parser.AISMessage21Parser;
import net.sf.marineapi.ais.util.Sixbit;
import org.junit.After;
import org.junit.Assert;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import net.sf.marineapi.ais.message.AISMessage21;
import net.sf.marineapi.ais.util.AISRuleViolation;
import net.sf.marineapi.ais.util.Latitude27;
import net.sf.marineapi.ais.util.Longitude28;
import net.sf.marineapi.ais.util.NavAidType;
import org.mockito.*;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class AISMessage21Parser_getStarboard_8_1_Test {

    private AISMessage21Parser parser;

    // Example value for testing
    private static final int TEST_STARBOARD = 15;

    @Before
    public void setUp() throws Exception {
        // Create a mock Sixbit content with valid data to avoid IllegalArgumentException
        Sixbit content = mock(Sixbit.class);
        // Assuming 360 is a valid length
        when(content.length()).thenReturn(360);
        // Mock necessary methods
        when(content.getString(anyInt(), anyInt())).thenReturn("Test");
        parser = new AISMessage21Parser(content);
        // Use reflection to set the private field fStarboard
        Field starboardField = AISMessage21Parser.class.getDeclaredField("fStarboard");
        starboardField.setAccessible(true);
        starboardField.set(parser, TEST_STARBOARD);
    }

    @Test
    public void testGetStarboard() {
        assertEquals(TEST_STARBOARD, parser.getStarboard());
    }

    @Test
    public void testParserNotNull() {
        assertNotNull(parser);
    }
}
