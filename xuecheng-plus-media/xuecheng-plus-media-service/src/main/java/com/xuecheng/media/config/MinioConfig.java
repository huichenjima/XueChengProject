package com.xuecheng.media.config;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ClassName: MinioConfig
 * Package: com.xuecheng.media.config
 * Description:
 *
 * @Author 何琛
 * @Create 2024/7/17 21:28
 * @Version 1.0
 */
@Configuration
public class MinioConfig {
    @Value("${minio.endpoint}")
    private String endpoint;
    @Value("${minio.accessKey}")
    private String accessKey;
    @Value("${minio.secretKey}")
    private String secretKey;


    @Bean
    public MinioClient minioClient(){
        MinioClient minioClient=MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey,secretKey)
                .build();
        return minioClient;
    }

}
