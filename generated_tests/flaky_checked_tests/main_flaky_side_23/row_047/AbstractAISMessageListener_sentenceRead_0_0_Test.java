package net.sf.marineapi.ais.event;

import net.sf.marineapi.ais.event.AbstractAISMessageListener;
import net.sf.marineapi.ais.message.AISMessage;
import net.sf.marineapi.ais.parser.AISMessageFactory;
import net.sf.marineapi.nmea.sentence.AISSentence;
import org.junit.Before;
import org.junit.Test;
import java.lang.reflect.Field;
import java.util.Queue;
import org.mockito.*;
import org.junit.After;
import org.junit.Assert;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import net.sf.marineapi.nmea.event.AbstractSentenceListener;
import net.sf.marineapi.nmea.event.SentenceListener;
import net.sf.marineapi.util.GenericTypeResolver;
import java.util.LinkedList;

public class AbstractAISMessageListener_sentenceRead_0_0_Test {

    private AbstractAISMessageListener<AISMessage> listener;

    private AISSentence mockSentence;

    private AISMessageFactory mockFactory;

    private AISMessage mockMessage;

    @Before
    public void setUp() throws Exception {
        mockSentence = mock(AISSentence.class);
        mockFactory = mock(AISMessageFactory.class);
        mockMessage = mock(AISMessage.class);
        listener = new AbstractAISMessageListener<AISMessage>(AISMessage.class) {

            @Override
            public void onMessage(AISMessage msg) {
                // implementation for testing
            }
        };
        // Use reflection to set the private factory instance
        Field factoryField = AbstractAISMessageListener.class.getDeclaredField("factory");
        factoryField.setAccessible(true);
        factoryField.set(listener, mockFactory);
    }

    @Test
    public void testSentenceRead_FirstFragment() throws Exception {
        when(mockSentence.isFirstFragment()).thenReturn(true);
        when(mockSentence.isLastFragment()).thenReturn(false);
        listener.sentenceRead(mockSentence);
        Queue<AISSentence> queue = getQueue();
        assertEquals(1, queue.size());
    }

    @Test
    public void testSentenceRead_ClearQueueOnFirstFragment() throws Exception {
        when(mockSentence.isFirstFragment()).thenReturn(true);
        when(mockSentence.isLastFragment()).thenReturn(false);
        listener.sentenceRead(mockSentence);
        Queue<AISSentence> queue = getQueue();
        assertEquals(1, queue.size());
        listener.sentenceRead(mockSentence);
        queue = getQueue();
        assertEquals(1, queue.size());
    }

    private Queue<AISSentence> getQueue() throws Exception {
        Field queueField = AbstractAISMessageListener.class.getDeclaredField("queue");
        queueField.setAccessible(true);
        return (Queue<AISSentence>) queueField.get(listener);
    }
}
