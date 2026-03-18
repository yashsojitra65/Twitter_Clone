package com.Twitter.com.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    @Value("${app.media.storage-path:uploads}")
    private String storagePath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadPath = Paths.get(storagePath).toAbsolutePath().normalize();
        String fileLocation = uploadPath.toUri().toString();
        if (!fileLocation.endsWith("/")) {
            fileLocation = fileLocation + "/";
        }

        registry
                .addResourceHandler("/media/**")
                .addResourceLocations(fileLocation)
                .setCachePeriod(3600);
    }
}
