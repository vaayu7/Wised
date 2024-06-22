package com.wised.auth.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for setting up AWS S3 credentials and configuring an Amazon S3 client.
 */
@Configuration
public class AwsConfig {

    @Value("${cloud.aws.credentials.accessKey}")
    private String awsAccessKey;

    @Value("${cloud.aws.credentials.secretKey}")
    private String awsSecretKey;

    @Value("${cloud.aws.region.static}")
    private String region;

    /**
     * Create and configure an Amazon S3 client bean with AWS credentials and region.
     *
     * @return An AmazonS3 instance with the specified AWS credentials and region.
     */
    @Bean
    public AmazonS3 amazonS3() {
        // Create AWS credentials using access and secret keys
        BasicAWSCredentials awsCredentials = new BasicAWSCredentials(awsAccessKey, awsSecretKey);

        // Build and configure an Amazon S3 client
        return AmazonS3Client.builder()
                .withRegion(region)
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                .build();
    }
}


