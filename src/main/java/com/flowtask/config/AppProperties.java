package com.flowtask.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app")
@Getter
@Setter
public class AppProperties {
    /** application.properties: app.system-id=1 */
    private Long systemId;
    /** application-{profile}.properties: app.upload-dir=... */
    private String uploadDir;
}
