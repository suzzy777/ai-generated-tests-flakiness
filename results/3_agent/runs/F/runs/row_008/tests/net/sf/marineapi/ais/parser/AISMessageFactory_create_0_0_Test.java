package net.sf.marineapi.ais.parser;

import org.junit.Test;
import static org.junit.Assert.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import net.sf.marineapi.ais.message.AISMessage;
import net.sf.marineapi.ais.util.Sixbit;
import net.sf.marineapi.nmea.parser.SentenceFactory;
import net.sf.marineapi.nmea.sentence.AISSentence;

public class AISMessageFactory_create_0_0_Test {

    @Test
    public void testCreateWithExistingParser() throws Exception {
        SentenceFactory sf = SentenceFactory.getInstance();
        AISSentence sentence = (AISSentence) sf.createParser("!AIVDM,1,1,,A,13aEOK?P00PD2wVMdLDRhgvL289?,0*26");

        AISMessageFactory factory = getAISMessageFactoryInstance();
        setParsers(factory, 1, MockAISMessage.class);

        AISMessage msg = factory.create(sentence);
        assertTrue(msg instanceof MockAISMessage);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateWithNoParser() throws Exception {
        SentenceFactory sf = SentenceFactory.getInstance();
        AISSentence sentence = (AISSentence) sf.createParser("!AIVDM,1,1,,A,13aEOK?P00PD2wVMdLDRhgvL289?,0*26");

        AISMessageFactory factory = getAISMessageFactoryInstance();
        setParsers(factory, 2, MockAISMessage.class); // Ensure no parser for message type 1

        factory.create(sentence);
    }

    @Test(expected = IllegalStateException.class)
    public void testCreateWithReflectionException() throws Exception {
        SentenceFactory sf = SentenceFactory.getInstance();
        AISSentence sentence = (AISSentence) sf.createParser("!AIVDM,1,1,,A,13aEOK?P00PD2wVMdLDRhgvL289?,0*26");

        AISMessageFactory factory = getAISMessageFactoryInstance();
        setParsers(factory, 1, InvalidConstructorAISMessage.class);

        factory.create(sentence);
    }

    private AISMessageFactory getAISMessageFactoryInstance() throws Exception {
        Constructor<AISMessageFactory> constructor = AISMessageFactory.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        return constructor.newInstance();
    }

    private void setParsers(AISMessageFactory factory, int messageType, Class<? extends AISMessage> clazz) throws Exception {
        Field parsersField = AISMessageFactory.class.getDeclaredField("parsers");
        parsersField.setAccessible(true);
        Map<Integer, Class<? extends AISMessage>> parsers = new HashMap<>();
        parsers.put(messageType, clazz);
        parsersField.set(factory, parsers);
    }

    public static class MockAISMessage implements AISMessage {
        public MockAISMessage(Sixbit sixbit) {}
        public int getMessageType() { return 1; }
        public int getMMSI() { return 0; }
        public int getRepeatIndicator() { return 0; }
    }

    public static class InvalidConstructorAISMessage implements AISMessage {
        public InvalidConstructorAISMessage() {}
        public int getMessageType() { return 1; }
        public int getMMSI() { return 0; }
        public int getRepeatIndicator() { return 0; }
    }
}
