package com.app.support.oss.hwcloud;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import com.app.enums.ErrorCodeEnum;
import com.app.exception.OssException;
import com.app.support.oss.FileMetadata;
import com.app.support.oss.ObsFileService;
import com.app.support.oss.ObsProperties;
import com.obs.services.ObsClient;
import com.obs.services.model.*;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 华为云obs服务实现
 *
 * @author yanlei
 * @since 2022-09-24
 */
@Slf4j
@AllArgsConstructor
public class HwCloudObsServiceImpl implements ObsFileService {

    private ObsClient obsClient;

    private ObsProperties obsProperties;

    /**
     * 初始化检查桶是否存在
     */
    @PostConstruct
    public void initCheckBucket() {
        String bucketName = obsProperties.getBucketName();
        log.info("HwCloudObsServiceImpl initCheckBucket bucketName={}", bucketName);
        if (StringUtils.isBlank(bucketName)) {
            log.error("HwCloudObsServiceImpl initCheckBucket bucketNames config is null !!!");
            return;
        }

        boolean bucketExists = this.bucketExists(bucketName);
        if (bucketExists) {
            log.info("HwCloudObsServiceImpl bucket already exists, bucketName={}", bucketName);
        } else {
            throw new OssException(ErrorCodeEnum.OSS);
        }
    }

    @Override
    public boolean bucketExists(String bucketName) {
        try {
            // 判断 存储桶存不存在
            return obsClient.headBucket(bucketName);
        } catch (Exception e) {
            log.error("HwCloudObsServiceImpl bucketExists error, bucketName={},errorMsg={}", bucketName, e);
            throw new OssException(ErrorCodeEnum.OSS);
        }
    }

    @Override
    public FileMetadata statObject(String bucketName, String objectName) {
        FileMetadata fileMetadata = new FileMetadata();
        try {
            ObjectMetadata metadata = obsClient.getObjectMetadata(bucketName, objectName);
            fileMetadata.setSize(metadata.getContentLength()).setContentType(metadata.getContentType());
        } catch (Exception e) {
            log.error("HwCloudObsServiceImpl statObject error, bucketName={},objectName={},errorMsg={}", bucketName, objectName, e);
            throw new OssException(ErrorCodeEnum.OSS); // 获取对象的元数据失败
        }
        return fileMetadata;
    }

    @Override
    public String putObject(String bucketName, String fileName, MultipartFile multipartFile) {
        // 获取文件名
        fileName = DateUtil.format(LocalDateTime.now(), DatePattern.PURE_DATE_PATTERN) + "/" + fileName;
        try (InputStream in = multipartFile.getInputStream()) {
            PutObjectRequest putObjectRequest = new PutObjectRequest();
            putObjectRequest.setBucketName(bucketName);
            putObjectRequest.setObjectKey(fileName);
            putObjectRequest.setInput(in);
            PutObjectResult putObjectResult = obsClient.putObject(putObjectRequest);
            return putObjectResult.getObjectKey();
        } catch (Exception e) {
            log.error("HwCloudObsServiceImpl putObject multipartFile error, bucketName={},fileName={},errorMsg={}", bucketName, fileName, e);
            throw new OssException(ErrorCodeEnum.OSS); // 文件上传失败
        }
    }

    @Override
    public String putObject(String bucketName, String fileName, File file) {
        // 获取文件名
        fileName = DateUtil.format(LocalDateTime.now(), DatePattern.PURE_DATE_PATTERN) + "/" + fileName;
        try (InputStream in = new FileInputStream(file)) {
            PutObjectRequest putObjectRequest = new PutObjectRequest();
            putObjectRequest.setBucketName(bucketName);
            putObjectRequest.setObjectKey(fileName);
            putObjectRequest.setInput(in);
            PutObjectResult putObjectResult = obsClient.putObject(putObjectRequest);
            return putObjectResult.getObjectKey();
        } catch (Exception e) {
            log.error("HwCloudObsServiceImpl putObject file error, bucketName={},fileName={},errorMsg={}", bucketName, fileName, e);
            throw new OssException(ErrorCodeEnum.OSS); // 文件上传失败
        }
    }


    @Override
    public String putObjectByte(String bucketName, String fileName, byte[] fileByte) {
        // 获取文件名
        fileName = DateUtil.format(LocalDateTime.now(), DatePattern.PURE_DATE_PATTERN) + "/" + fileName;

        try (InputStream in = new ByteArrayInputStream(fileByte)) {
            PutObjectRequest putObjectRequest = new PutObjectRequest();

            putObjectRequest.setBucketName(bucketName);

            putObjectRequest.setObjectKey(fileName);

            putObjectRequest.setInput(in);
            PutObjectResult putObjectResult = obsClient.putObject(putObjectRequest);

            return putObjectResult.getObjectKey();
        } catch (Exception e) {
            log.error("HwCloudObsServiceImpl putObjectByte error, bucketName={},fileName={},errorMsg={}", bucketName, fileName, e);
            throw new OssException(ErrorCodeEnum.OSS); // 文件上传失败
        }
    }

    @Override
    public InputStream getObject(String bucketName, String objectName, Long offset, Long length) {
        try {
            // 范围下载
            GetObjectRequest request = new GetObjectRequest(bucketName, objectName);

            // 指定开始和结束范围
            request.setRangeStart(offset);
            request.setRangeEnd(offset + length - 1);

            ObsObject obsObject = obsClient.getObject(request);

            return obsObject.getObjectContent();
        } catch (Exception e) {
            log.error("HwCloudObsServiceImpl getObject range error, bucketName={},objectName={},errorMsg={}", bucketName, objectName, e);
            throw new OssException(ErrorCodeEnum.OSS); // 从下载文件失败
        }
    }

    @Override
    public InputStream getObject(String bucketName, String objectName) {
        try {
            ObsObject obsObject = obsClient.getObject(bucketName, objectName);

            return obsObject.getObjectContent();
        } catch (Exception e) {
            log.error("HwCloudObsServiceImpl getObject error, bucketName={},objectName={},errorMsg={}", bucketName, objectName, e);
            throw new OssException(ErrorCodeEnum.OSS); // 从下载文件失败
        }
    }

    @Override
    public void removeObject(String bucketName, String objectName) throws Exception {
        obsClient.deleteObject(bucketName, objectName);
    }

    @Override
    public boolean existObject(String bucketName, String objectName) {
        return obsClient.doesObjectExist(bucketName, objectName);
    }

    @Override
    public void copyObject(String sourceBucketName, String sourceObjectName, String targetBucketName, String targetObjectName) {
        try {
            obsClient.copyObject(sourceBucketName, sourceObjectName, targetBucketName, targetObjectName);
        } catch (Exception e) {
            log.error("HwCloudObsServiceImpl copyObject error, sourceBucketName={},sourceObjectName={},targetBucketName={},targetObjectName={},errorMsg={}", sourceBucketName, sourceObjectName, targetBucketName, targetObjectName, e);
            throw new OssException(ErrorCodeEnum.OSS); // 复制文件失败
        }
    }

    @Override
    public String generatePresignedDownloadUrl(String bucketName, String objectName, Duration expiration) {
        return "";
    }

    @Override
    public String generateUploadPresignedUrl(String bucketName, String objectName, Duration expiration, Map<String, String> extParam, Map<String, String> headers) {
        return "";
    }

}
