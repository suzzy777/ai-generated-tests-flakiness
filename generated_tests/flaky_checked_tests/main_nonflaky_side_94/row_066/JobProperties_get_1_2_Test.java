package io.elasticjob.lite.executor.handler;

import io.elasticjob.lite.executor.handler.JobProperties;
import io.elasticjob.lite.executor.handler.JobProperties.JobPropertiesEnum;
import org.junit.Before;
import org.junit.Test;
import java.lang.reflect.Field;
import java.util.EnumMap;
import org.mockito.*;
import org.junit.After;
import org.junit.Assert;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import io.elasticjob.lite.executor.handler.impl.DefaultExecutorServiceHandler;
import io.elasticjob.lite.executor.handler.impl.DefaultJobExceptionHandler;
import io.elasticjob.lite.util.json.GsonFactory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import java.util.LinkedHashMap;
import java.util.Map;

public class JobProperties_get_1_2_Test {

    private JobProperties jobProperties;

    @Before
    public void setUp() throws Exception {
        jobProperties = new JobProperties();
        // Using reflection to set private map field
        Field mapField = JobProperties.class.getDeclaredField("map");
        mapField.setAccessible(true);
        EnumMap<JobPropertiesEnum, String> map = new EnumMap<>(JobPropertiesEnum.class);
        map.put(JobPropertiesEnum.EXECUTOR_SERVICE_HANDLER, "CustomExecutorServiceHandler");
        mapField.set(jobProperties, map);
    }

    @Test
    public void testGetWhenKeyExists() {
        String result = jobProperties.get(JobPropertiesEnum.EXECUTOR_SERVICE_HANDLER);
        assertEquals("CustomExecutorServiceHandler", result);
    }

    @Test
    public void testGetWhenKeyDoesNotExist() {
        String result = jobProperties.get(JobPropertiesEnum.JOB_EXCEPTION_HANDLER);
        assertEquals(JobPropertiesEnum.JOB_EXCEPTION_HANDLER.getDefaultValue(), result);
    }
}
