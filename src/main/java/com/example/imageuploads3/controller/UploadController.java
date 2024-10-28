package com.example.imageuploads3.controller;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@RestController
@RequestMapping("/home")
public class UploadController {
    @Autowired
    private AmazonS3 s3Client;

    @Value("${endpointUrl}")
    private String endpointUrl;
    
    @Value("${bucketName}")
    private String bucketName;

    @PostMapping("/upload")
    public String uploadFile(@RequestPart(value = "file")MultipartFile multipartFile) {
        String fileUrl = "";
        String status = null;
        
        try {
            // converting multipart file to file
            File file = convertMultipartToFile(multipartFile);
            String fileName = multipartFile.getOriginalFilename();
            
            fileUrl = endpointUrl + "/" + bucketName + "/" + fileName;
            
            status = uploadFileToS3Bucket(fileName, file);
            
            file.delete();
        } catch (Exception e) {

            return "UploadController().uploadFile().Exception : " + e.getMessage();

        }

        return status + " " +  fileUrl;
    } 
    
    private File convertMultipartToFile(MultipartFile file) throws IOException {
        File convFile = new File(file.getOriginalFilename());

        FileOutputStream fos = new FileOutputStream(convFile);
        fos.write(file.getBytes());
        fos.close();
        return convFile;
    }
    
    private String uploadFileToS3Bucket(String fileName, File file) {
        try {
            s3Client.putObject(new PutObjectRequest(bucketName, fileName, file));
        } catch (AmazonServiceException ex) {
            return "uploadFileToS3Bucket fail: " + ex.getMessage();
        }
        
        return "Upload successful";
    }
}
