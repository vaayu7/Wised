package com.wised.auth.service;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URL;

@Service
@AllArgsConstructor
@NoArgsConstructor
public class S3FileService {

    @Autowired
    private AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucketName}")
    private  String bucketName ;

    public S3FileService(AmazonS3 amazonS3) {
        this.amazonS3 = amazonS3;
    }

    //for testing only
    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    // Upload a file to S3
    public void uploadFile(String key, MultipartFile file) throws IOException {
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, key, file.getInputStream(), null);
        amazonS3.putObject(putObjectRequest);
    }

    public void uploadFile(String key, byte[] fileBytes, String format) throws IOException {
        try {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(fileBytes);
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentLength(fileBytes.length);

            // Set the format (file type) as user-defined metadata
            //        objectMetadata.addUserMetadata("format", format);

            PutObjectRequest putObjectRequest = new PutObjectRequest(this.bucketName, key, byteArrayInputStream, objectMetadata);
            amazonS3.putObject(putObjectRequest);
        }catch ( Exception e){
            System.out.println(e);
        }


    }

    // Download a file from S3
    public S3Object downloadFile(String key) {
        GetObjectRequest getObjectRequest = new GetObjectRequest(bucketName, key);
        return amazonS3.getObject(getObjectRequest);
    }


    // Delete a file from S3
    public void deleteFile(String key) {
        DeleteObjectRequest deleteObjectRequest = new DeleteObjectRequest(bucketName, key);
        amazonS3.deleteObject(deleteObjectRequest);
    }

    // Create a pre-signed URL for a file
    public String generatePresignedUrl(String key, long expirationInSeconds) {
        try {
            GeneratePresignedUrlRequest generatePresignedUrlRequest = new GeneratePresignedUrlRequest(bucketName, key)
                    .withMethod(HttpMethod.GET)
                    .withExpiration(java.util.Date.from(java.time.Instant.now().plusSeconds(expirationInSeconds)));

            URL presignedUrl = amazonS3.generatePresignedUrl(generatePresignedUrlRequest);

            if (presignedUrl != null) {
                return presignedUrl.toString();
            } else {
                throw new RuntimeException("Failed to generate presigned URL");
            }
        } catch (Exception e) {
            // Log the exception for debugging
            e.printStackTrace();
            throw new RuntimeException("Failed to generate presigned URL: " + e.getMessage());
        }
    }


    // Copy a file within the same or different buckets
    public void copyFile(String sourceKey, String destinationKey, String destinationBucket) {
        CopyObjectRequest copyObjectRequest = new CopyObjectRequest(bucketName, sourceKey, destinationBucket, destinationKey);
        amazonS3.copyObject(copyObjectRequest);
    }

    // Get metadata of a file in S3
    public ObjectMetadata getObjectMetadata(String key) {
        GetObjectMetadataRequest getObjectMetadataRequest = new GetObjectMetadataRequest(bucketName, key);
        return amazonS3.getObjectMetadata(getObjectMetadataRequest);
    }

    public String getFileFormatFromS3(String key) {
        ObjectMetadata objectMetadata = amazonS3.getObjectMetadata(bucketName, key);
        return objectMetadata.getUserMetadata().get("format");
    }

    // Check if a file exists in S3
    public boolean doesFileExist(String key) {
        return amazonS3.doesObjectExist(bucketName, key);
    }

    // Upload a file from a local file system
    public void uploadFileFromLocal(String key, String filePath) {
        File file = new File(filePath);
        amazonS3.putObject(new PutObjectRequest(bucketName, key, file));
    }

    // Download a file from S3 to a local file system
    public void downloadFileToLocal(String key, String localFilePath) {
        try {
            S3Object s3Object = downloadFile(key);

            if (s3Object != null) {
                S3ObjectInputStream inputStream = s3Object.getObjectContent();

                try {
                    File localFile = new File(localFilePath);
                    File parentDirectory = localFile.getParentFile();

                    if (!parentDirectory.exists()) {
                        if (!parentDirectory.mkdirs()) {
                            throw new RuntimeException("Failed to create directory for local file");
                        }
                    }

                    try (OutputStream outputStream = new FileOutputStream(localFile)) {
                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                        }
                    }
                } catch (IOException e) {
                    throw new RuntimeException("Failed to download file to local", e);
                }
            } else {
                throw new RuntimeException("Failed to download file from S3");
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch S3 object: " + e.getMessage(), e);
        }
    }

    // Get the URL of an object in S3
    public String getObjectUrl(String key) {
        return amazonS3.getUrl(bucketName, key).toString();
    }
}

