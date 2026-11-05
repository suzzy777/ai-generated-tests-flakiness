package com.intuit.karate;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import com.intuit.karate.JsonUtils;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.spi.json.JsonProvider;
import com.jayway.jsonpath.spi.json.JsonSmartJsonProvider;
import com.jayway.jsonpath.spi.mapper.JsonSmartMappingProvider;
import com.jayway.jsonpath.spi.mapper.MappingProvider;
import de.siegmar.fastcsv.reader.CsvReader;
import de.siegmar.fastcsv.reader.CsvRow;
import de.siegmar.fastcsv.writer.CsvWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minidev.json.JSONStyle;
import net.minidev.json.JSONValue;
import net.minidev.json.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import static net.minidev.json.JSONValue.defaultReader;

public class JsonUtils_toJson_2_0_Test {

    @Test
    public void testToJson() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        // Test with a simple object
        String result = JsonUtils.toJson("test");
        assertNotNull(result);
        assertEquals("\"test\"", result);
        // Test with a complex object
        Map<String, Object> complexObject = new LinkedHashMap<>();
        complexObject.put("key1", "value1");
        complexObject.put("key2", 123);
        result = JsonUtils.toJson(complexObject);
        assertNotNull(result);
        assertEquals("{\"key1\":\"value1\",\"key2\":123}", result);
        // Test with null
        result = JsonUtils.toJson(null);
        assertNotNull(result);
        assertEquals("null", result);
        // Test private method toJsonSafe using reflection
        Method toJsonSafeMethod = JsonUtils.class.getDeclaredMethod("toJsonSafe", Object.class, boolean.class);
        toJsonSafeMethod.setAccessible(true);
        result = (String) toJsonSafeMethod.invoke(null, complexObject, true);
        assertNotNull(result);
    }
}
