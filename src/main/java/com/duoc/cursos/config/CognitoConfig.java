package com.duoc.cursos.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;

@Configuration
public class CognitoConfig {

    @Value("${aws.cognito.region}")
    private String region;

    // Credenciales IAM opcionales (las lee del .env via dotenv -> system properties).
    // Si no están, el SDK usa su cadena por defecto (variables de entorno, ~/.aws, IMDS...).
    @Value("${AWS_ACCESS_KEY_ID:}")
    private String accessKeyId;

    @Value("${AWS_SECRET_ACCESS_KEY:}")
    private String secretAccessKey;

    @Bean
    public CognitoIdentityProviderClient cognitoClient() {
        return CognitoIdentityProviderClient.builder()
                .region(Region.of(region))
                .credentialsProvider(credentialsProvider())
                .build();
    }

    private AwsCredentialsProvider credentialsProvider() {
        if (accessKeyId != null && !accessKeyId.isBlank()
                && secretAccessKey != null && !secretAccessKey.isBlank()) {
            return StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKeyId, secretAccessKey));
        }
        return DefaultCredentialsProvider.create();
    }
}
