package net.sf.marineapi.nmea.parser;

import org.junit.Before;
import org.junit.Test;
import java.lang.reflect.Method;
import java.util.List;
import java.util.ArrayList;
import net.sf.marineapi.nmea.parser.SentenceParser;
import net.sf.marineapi.nmea.sentence.TalkerId;
import net.sf.marineapi.nmea.sentence.SentenceId;
import org.junit.After;
import org.junit.Assert;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Arrays;
import net.sf.marineapi.nmea.sentence.Checksum;
import net.sf.marineapi.nmea.sentence.Sentence;
import net.sf.marineapi.nmea.sentence.SentenceValidator;
import org.mockito.*;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class SentenceParser_setFieldCount_24_0_Test {

    private SentenceParser parser;

    @Before
    public void setUp() {
        // Initialize the SentenceParser object with a dummy TalkerId and SentenceId
        parser = new SentenceParser('!', TalkerId.AB, SentenceId.GGA, 1);
    }

    @SuppressWarnings("unchecked")
    private List<String> getFields() throws Exception {
        // Use reflection to access private fields
        java.lang.reflect.Field fieldsField = SentenceParser.class.getDeclaredField("fields");
        fieldsField.setAccessible(true);
        return (List<String>) fieldsField.get(parser);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetFieldCountZero() throws Exception {
        parser.setFieldCount(0);
    }

    @Test
    public void testSetFieldCountIncrease() throws Exception {
        parser.setFieldCount(3);
        List<String> fields = getFields();
        assertEquals(3, fields.size());
        assertEquals("", fields.get(0));
        assertEquals("", fields.get(1));
        assertEquals("", fields.get(2));
    }

    @Test
    public void testSetFieldCountDecrease() throws Exception {
        parser.setFieldCount(3);
        parser.setFieldCount(2);
        List<String> fields = getFields();
        assertEquals(2, fields.size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetFieldCountNegative() throws Exception {
        parser.setFieldCount(-1);
    }
}
