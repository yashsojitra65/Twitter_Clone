package com.Twitter.com.Services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class MediaStorageService {

    @Value("${app.media.storage-path:uploads}")
    private String storagePath;

    private static final Map<String, String> CONTENT_TYPE_EXTENSION_MAP = new HashMap<>();
    private static final Set<String> ALLOWED_EXTENSIONS = new HashSet<>();

    static {
        CONTENT_TYPE_EXTENSION_MAP.put("image/jpeg", ".jpg");
        CONTENT_TYPE_EXTENSION_MAP.put("image/jpg", ".jpg");
        CONTENT_TYPE_EXTENSION_MAP.put("image/png", ".png");
        CONTENT_TYPE_EXTENSION_MAP.put("image/gif", ".gif");
        CONTENT_TYPE_EXTENSION_MAP.put("image/webp", ".webp");
        CONTENT_TYPE_EXTENSION_MAP.put("video/mp4", ".mp4");
        CONTENT_TYPE_EXTENSION_MAP.put("video/quicktime", ".mov");
        CONTENT_TYPE_EXTENSION_MAP.put("video/x-matroska", ".mkv");
        CONTENT_TYPE_EXTENSION_MAP.put("video/webm", ".webm");
        CONTENT_TYPE_EXTENSION_MAP.put("text/plain", ".txt");
        CONTENT_TYPE_EXTENSION_MAP.put("text/markdown", ".md");

        ALLOWED_EXTENSIONS.addAll(CONTENT_TYPE_EXTENSION_MAP.values());
        ALLOWED_EXTENSIONS.add(".jpeg");
    }

    public String storeFromUrl(String sourceUrl) {
        if (sourceUrl == null || sourceUrl.isBlank()) {
            return null;
        }
        return storeFromUrl(toUrl(sourceUrl));
    }

    //Visible for testing: accepts a prepared URL (e.g., with custom stream handler).
    public String storeFromUrl(URL url) {
        enforceHttpScheme(url);
        String initialExt = extractExtension(url);
        if (!initialExt.isEmpty() && !isAllowedExtension(initialExt)) {
            throw new IllegalStateException("Unsupported file extension: " + initialExt);
        }
        HttpURLConnection connection = null;
        String storedFileName;
        Path storageDir = Paths.get(storagePath).toAbsolutePath().normalize();
        try {
            Files.createDirectories(storageDir);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to create media directory", e);
        }

        Path target;
        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(15000);
            connection.connect();

            int status = connection.getResponseCode();
            if (status >= 400) {
                throw new IllegalStateException("Remote server rejected download with status " + status);
            }

            String contentType = connection.getContentType();
            storedFileName = buildFileName(url, contentType);
            target = storageDir.resolve(storedFileName);

            try (InputStream in = connection.getInputStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to download or save media", e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        return "/media/" + storedFileName;
    }

    private URL toUrl(String sourceUrl) {
        try {
            return new URL(sourceUrl);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid media URL", e);
        }
    }

    private String buildFileName(URL url, String contentType) {
        String extension = extractExtension(url);
        if (extension.isEmpty()) {
            extension = extensionFromContentType(contentType);
        }
        if (extension.isEmpty()) {
            throw new IllegalStateException("Unsupported or unknown content type: " + contentType);
        }
        if (!isAllowedExtension(extension)) {
            throw new IllegalStateException("Unsupported file extension: " + extension);
        }
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        return timestamp + extension;
    }

    private String extractExtension(URL url) {
        String path = url.getPath();
        String fileName = path != null ? Paths.get(path).getFileName().toString() : "";
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        String ext = fileName.substring(fileName.lastIndexOf('.')).trim();
        return ext.isEmpty() ? "" : ext.toLowerCase();
    }

    private String extensionFromContentType(String contentType) {
        if (contentType == null) {
            return "";
        }
        String normalized = contentType.toLowerCase();
        return CONTENT_TYPE_EXTENSION_MAP.getOrDefault(normalized, "");
    }

    private boolean isAllowedExtension(String extension) {
        if (extension == null) {
            return false;
        }
        return ALLOWED_EXTENSIONS.contains(extension.toLowerCase());
    }

    private void enforceHttpScheme(URL url) {
        String protocol = url.getProtocol();
        if (!"http".equalsIgnoreCase(protocol) && !"https".equalsIgnoreCase(protocol)) {
            throw new IllegalArgumentException("Only http/https URLs are supported");
        }
    }
}
