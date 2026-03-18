package com.Twitter.com.Services;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class MediaStorageServiceTest {

    private Path tempDir;

    @AfterEach
    void cleanup() throws IOException {
        if (tempDir != null && Files.exists(tempDir)) {
            Files.walk(tempDir)
                    .map(Path::toFile)
                    .sorted((a, b) -> -a.compareTo(b))
                    .forEach(File::delete);
        }
    }

    @Test
    void storeFromUrl_savesWithTimestampAndExtension() throws Exception {
        tempDir = Files.createTempDirectory("media-test");
        MediaStorageService service = new MediaStorageService();
        ReflectionTestUtils.setField(service, "storagePath", tempDir.toString());

        StubHttpURLConnection conn = new StubHttpURLConnection("image/png", "image-bytes");
        URL url = new URL(null, "http://example.com/myPhoto", new StubHandler(conn));

        String storedPath = service.storeFromUrl(url);

        assertNotNull(storedPath);
        assertTrue(storedPath.endsWith(".png"));
        Path saved = tempDir.resolve(storedPath.replace("/media/", ""));
        assertTrue(Files.exists(saved), "file should be saved on disk");
        assertTrue(saved.getFileName().toString().matches("\\d{8}_\\d{6}\\.png"));
    }

    @Test
    void storeFromUrl_rejectsUnsupportedExtensionEarly() {
        MediaStorageService service = new MediaStorageService();
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> service.storeFromUrl("http://example.com/file.exe"));
        assertTrue(ex.getMessage().contains("Unsupported file extension"));
    }

    @Test
    void storeFromUrl_rejectsUnknownContentTypeWhenNoExtension() throws Exception {
        tempDir = Files.createTempDirectory("media-test");
        MediaStorageService service = new MediaStorageService();
        ReflectionTestUtils.setField(service, "storagePath", tempDir.toString());

        StubHttpURLConnection conn = new StubHttpURLConnection("text/html", "<html></html>");
        URL url = new URL(null, "http://example.com/resource", new StubHandler(conn));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> service.storeFromUrl(url));
        assertTrue(ex.getMessage().toLowerCase().contains("unsupported"));
    }

    @Test
    void storeFromUrl_rejectsNonHttpSchemes() {
        MediaStorageService service = new MediaStorageService();
        assertThrows(IllegalArgumentException.class,
                () -> service.storeFromUrl("ftp://example.com/file.png"));
    }

    // --- test helpers ---

    private static class StubHandler extends URLStreamHandler {
        private final URLConnection connection;

        StubHandler(URLConnection connection) {
            this.connection = connection;
        }

        @Override
        protected URLConnection openConnection(URL u) {
            return connection;
        }
    }

    private static class StubHttpURLConnection extends HttpURLConnection {
        private final String contentType;
        private final String body;

        StubHttpURLConnection(String contentType, String body) throws IOException {
            super(new URL("http://example.com"));
            this.contentType = contentType;
            this.body = body;
        }

        @Override
        public void disconnect() {
        }

        @Override
        public boolean usingProxy() {
            return false;
        }

        @Override
        public void connect() throws IOException {
        }

        @Override
        public int getResponseCode() throws IOException {
            return 200;
        }

        @Override
        public String getContentType() {
            return contentType;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return new ByteArrayInputStream(body.getBytes());
        }

        @Override
        public void setRequestMethod(String method) throws ProtocolException {
            // ignore
        }
    }
}
