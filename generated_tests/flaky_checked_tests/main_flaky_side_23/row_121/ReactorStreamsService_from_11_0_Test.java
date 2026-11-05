package org.apache.camel.component.reactor.engine;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.junit.Before;
import org.junit.Test;
import org.reactivestreams.Publisher;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;
import org.mockito.*;
import org.junit.After;
import org.junit.Assert;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import java.util.function.Function;
import org.apache.camel.component.reactive.streams.ReactiveStreamsCamelSubscriber;
import org.apache.camel.component.reactive.streams.ReactiveStreamsConsumer;
import org.apache.camel.component.reactive.streams.ReactiveStreamsHelper;
import org.apache.camel.component.reactive.streams.ReactiveStreamsProducer;
import org.apache.camel.component.reactive.streams.api.CamelReactiveStreamsService;
import org.apache.camel.component.reactive.streams.util.BodyConverter;
import org.apache.camel.component.reactive.streams.util.ConvertingPublisher;
import org.apache.camel.component.reactive.streams.util.ConvertingSubscriber;
import org.apache.camel.component.reactive.streams.util.UnwrapStreamProcessor;
import org.apache.camel.spi.Synchronization;
import org.apache.camel.support.ServiceSupport;
import org.apache.camel.util.function.Suppliers;
import org.reactivestreams.Subscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class ReactorStreamsService_from_11_0_Test {

    private ReactorStreamsService reactorStreamsService;

    private CamelContext camelContext;

    private ConcurrentMap<String, String> publishedUriToStream;

    @Before
    public void setUp() throws Exception {
        camelContext = mock(CamelContext.class);
        publishedUriToStream = new ConcurrentHashMap<>();
        reactorStreamsService = new ReactorStreamsService(camelContext);
        // Use reflection to set the private field
        java.lang.reflect.Field field = ReactorStreamsService.class.getDeclaredField("publishedUriToStream");
        field.setAccessible(true);
        field.set(reactorStreamsService, publishedUriToStream);
    }

    @Test
    public void testFrom_NewUri() throws Exception {
        String uri = "direct:input";
        String uuid = "test-uuid";
        // Mocking UUID generation
        when(camelContext.getUuidGenerator()).thenReturn(() -> uuid);
        // Mocking route addition
        doNothing().when(camelContext).addRoutes(any(RouteBuilder.class));
        Publisher<Exchange> publisher = reactorStreamsService.from(uri);
        assertNotNull(publisher);
        assertTrue(publishedUriToStream.containsKey(uri));
        assertEquals(uuid, publishedUriToStream.get(uri));
    }

    @Test
    public void testFrom_ExistingUri() throws Exception {
        String uri = "direct:input";
        String uuid = "test-uuid";
        publishedUriToStream.put(uri, uuid);
        Publisher<Exchange> publisher = reactorStreamsService.from(uri);
        assertNotNull(publisher);
        assertEquals(uuid, publishedUriToStream.get(uri));
        verify(camelContext, never()).addRoutes(any(RouteBuilder.class));
    }

    @Test(expected = IllegalStateException.class)
    public void testFrom_ExceptionHandling() throws Exception {
        String uri = "direct:input";
        // Mocking an exception during route addition
        when(camelContext.getUuidGenerator()).thenThrow(new RuntimeException("Route error"));
        reactorStreamsService.from(uri);
    }
}
