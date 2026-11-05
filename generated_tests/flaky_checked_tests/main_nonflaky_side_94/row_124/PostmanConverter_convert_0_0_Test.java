package com.intuit.karate.formats;

import com.intuit.karate.formats.PostmanConverter;
import com.intuit.karate.formats.PostmanUtils;
import org.junit.jupiter.api.io.TempDir;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

public class PostmanConverter_convert_0_0_Test {

    private PostmanConverter converter;

    private static final String POSTMAN_JSON = "{ \"item\": [] }";

    @BeforeEach
    public void setUp() {
        converter = new PostmanConverter();
    }

    @Test
    public void testConvertFileDoesNotExist() {
        boolean result = converter.convert("nonexistent_file.postman_collection", "outputDir");
        assertFalse(result);
    }

    @Test
    public void testConvertSuccess(@TempDir Path tempDir) throws Exception {
        Path tempFile = tempDir.resolve("temp.postman_collection");
        Files.write(tempFile, POSTMAN_JSON.getBytes());
        boolean result = converter.convert(tempFile.toString(), tempDir.toString());
        assertTrue(result);
        Path outputFilePath = tempDir.resolve("temp.feature");
        assertTrue(Files.exists(outputFilePath));
    }

    @Test
    public void testConvertWithEmptyJson(@TempDir Path tempDir) throws Exception {
        Path tempFile = tempDir.resolve("empty.postman_collection");
        Files.write(tempFile, POSTMAN_JSON.getBytes());
        boolean result = converter.convert(tempFile.toString(), tempDir.toString());
        assertTrue(result);
        Path outputFilePath = tempDir.resolve("empty.feature");
        assertTrue(Files.exists(outputFilePath));
    }
}
