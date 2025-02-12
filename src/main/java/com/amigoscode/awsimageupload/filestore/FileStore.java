package com.amigoscode.awsimageupload.filestore;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.util.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.IIOException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

@Service
public class FileStore {

    private final AmazonS3 s3;

    @Value("${target}")
    private String targetFolder;

    @Autowired
    public FileStore(AmazonS3 s3) {
        this.s3 = s3;
    }

    public void save(String path,
                     String fileName,
                     Optional<Map<String, String>> optionalMetadata,
                     InputStream inputStream) {
        ObjectMetadata metadata = new ObjectMetadata();

        System.out.println("Meta Data content :"+optionalMetadata);
        metadata.setContentLength(Long.parseLong(optionalMetadata.get().get("Content-Length")));
        metadata.setContentType(optionalMetadata.get().get("Content-Type"));
//        optionalMetadata.ifPresent(map -> {
//            if (!map.isEmpty()) {
//                map.forEach(metadata::addUserMetadata);
//            }
//        });

        System.out.println("List Of Buckets :"+s3.listBuckets());
        System.out.println("Meta Data :"+metadata);
        try {
            s3.putObject(path, fileName, inputStream, metadata);
        } catch (AmazonServiceException e) {
            throw new IllegalStateException("Failed to store file to s3", e);
        }
    }

    public byte[] download(String path, String key) {
        try {

            S3Object object = s3.getObject(path, key);
            File targetFile = new File(targetFolder,key);
            System.out.println("File downloaded successfully: " + targetFile.getAbsolutePath());
            S3ObjectInputStream s3ObjectInputStream = object.getObjectContent();
            FileOutputStream fileOutputStream = new FileOutputStream(targetFile);
            IOUtils.copy(s3ObjectInputStream,fileOutputStream);
            fileOutputStream.close();
            return IOUtils.toByteArray(object.getObjectContent());
        } catch (AmazonServiceException | IOException e) {
            throw new IllegalStateException("Failed to download file to s3", e);
        }
    }
}
