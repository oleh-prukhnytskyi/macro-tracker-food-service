package com.olehprukhnytskyi.macrotrackerfoodservice.properties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "aws")
public class S3Properties {
    @NotBlank
    private String region;

    @NotBlank
    private String s3Bucket;

    @NotNull
    private Credentials credentials;

    @Getter
    @Setter
    public static class Credentials {
        @NotBlank
        private String accessKey;

        @NotBlank
        private String secretKey;
    }
}
