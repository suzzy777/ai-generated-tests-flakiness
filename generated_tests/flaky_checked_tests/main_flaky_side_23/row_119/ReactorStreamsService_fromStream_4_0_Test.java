package org.apache.camel.component.reactor.engine;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.junit.Before;
import org.junit.Test;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import java.lang.reflect.Method;
import org.junit.After;
import org.junit.Assert;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.function.Supplier;
import org.apache.camel.builder.RouteBuilder;
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
import reactor.core.publisher.Mono;
import java.lang.reflect.InvocationTargetException;
import org.mockito.*;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

public class ReactorStreamsService_fromStream_4_0_Test {

    private ReactorStreamsService reactorStreamsService;

    private CamelContext camelContext;

    @Before
    public void setUp() {
        camelContext = Mockito.mock(CamelContext.class);
        reactorStreamsService = new ReactorStreamsService(camelContext);
    }

    private Method getFromStreamMethod() throws NoSuchMethodException {
        Method method = ReactorStreamsService.class.getDeclaredMethod("fromStream", String.class, Class.class);
        method.setAccessible(true);
        return method;
    }

    @Test
    public void testFromStreamWithExchangeType() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = getFromStreamMethod();
        Publisher<Exchange> publisher = (Publisher<Exchange>) method.invoke(reactorStreamsService, "testStream", Exchange.class);
        assertNotNull(publisher);
    }

    @Test
    public void testFromStreamWithOtherType() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = getFromStreamMethod();
        Publisher<String> publisher = (Publisher<String>) method.invoke(reactorStreamsService, "testStream", String.class);
        assertNotNull(publisher);
    }
}
