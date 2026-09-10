package com.app.support.oss.minio;

import cn.hutool.core.io.FileTypeUtil;
import com.app.enums.ErrorCodeEnum;
import com.app.exception.OssException;
import com.app.support.oss.FileMetadata;
import com.app.support.oss.ObsFileService;
import com.app.support.oss.ObsProperties;
import io.minio.*;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.time.Duration;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * MinIO调用工具类
 *
 * @author yanlei
 * @since 2022-09-24
 */
@Slf4j
@AllArgsConstructor
public class MinioObsServiceImpl implements ObsFileService {
    private MinioClient minioClient;

    private ObsProperties obsProperties;

    /**
     * 初始化MinIO桶
     */
    @PostConstruct
    public void initMinIOBucket() {
        String bucketNames = obsProperties.getBucketName();
        log.info("MinioUtils initMinIOBucket bucketNames={}", bucketNames);
        if (StringUtils.isBlank(bucketNames)) {
            log.error("init MinIO bucket bucketNames config is null !!!");
            return;
        }

        for (String bucketName : bucketNames.split(",")) {
            boolean boo = this.makeBucket(bucketName);
            log.info("start init MinIO bucket,bucketName={}, result={}", bucketName, boo);
        }
    }

    /**
     * 创建存储桶
     *
     * @param bucketName 存储桶名称
     * @return 是否创建成功
     */
    private boolean makeBucket(String bucketName) {
        try {
            // 判断 存储桶存不存在
            boolean bucketExists = this.bucketExists(bucketName);
            if (bucketExists) {
                log.info("Minio bucket already exists, bucketName={}", bucketName);
            } else {
                // 创建桶
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                log.info("Minio create bucket success, bucketName={}", bucketName);
            }
            return true;
        } catch (Exception e) {
            log.error("MinioObsServiceImpl create error, bucketName={},errorMsg={}", bucketName, e);
            throw new OssException(ErrorCodeEnum.OSS);
        }
    }

    @Override
    public boolean bucketExists(String bucketName) {
        try {
            // 判断 存储桶存不存在
            return minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        } catch (Exception e) {
            log.error("MinioObsServiceImpl bucketExists error, bucketName={},errorMsg={}", bucketName, e);
            throw new OssException(ErrorCodeEnum.OSS);
        }
    }

    @Override
    public FileMetadata statObject(String bucketName, String objectName) {
        FileMetadata fileMetadata = new FileMetadata();
        try {
            StatObjectResponse statObjectResponse = minioClient.statObject(StatObjectArgs.builder().bucket(bucketName).object(objectName).build());
            fileMetadata.setSize(statObjectResponse.size()).setContentType(statObjectResponse.contentType());
        } catch (Exception e) {
            log.error("MinioObsServiceImpl statObject error, bucketName={},objectName={},errorMsg={}", bucketName, objectName, e);
            throw new OssException(ErrorCodeEnum.OSS); // 获取MinIO对象的元数据失败
        }
        return fileMetadata;
    }

    @Override
    public String putObject(String bucketName, String fileName, MultipartFile multipartFile) {
        // 获取文件名
        try (InputStream in = multipartFile.getInputStream()) {
            /**
             * 设置要上传的流。
             * objectSize：上传的对象大小；
             * partSize：分段上传的大小（大概理解为，大文件可以设置为分段上传，每段的大小），单位为byte
             * 如果要上传的对象大小未知，则将 -1 传递给objectSize，并需要传递有效的partSize。
             * 如果要上传的对象大小已知，可以将 -1 传递给partSize，系统会进行自动检测是否需要分段上传；
             * 也可以传递有效的 partSize 来控制对内存的使用。
             * 如果 partSize 大于 objectSize，则 objectSize 将覆盖 partSize 的值。
             * partSize有效的取值大小介于 5MiB 到 5GiB 之间（前后闭区间）
             */
            PutObjectArgs putObjectArgs = PutObjectArgs.builder().bucket(bucketName).object(fileName).stream(in, multipartFile.getSize(), -1L).contentType(multipartFile.getContentType()).build();
            ObjectWriteResponse objectWriteResponse = minioClient.putObject(putObjectArgs);
            return objectWriteResponse.object();
        } catch (Exception e) {
            log.error("MinioObsServiceImpl putObject multipartFile error, bucketName={},fileName={},errorMsg={}", bucketName, fileName, e);
            throw new OssException(ErrorCodeEnum.OSS); // 文件上传MinIo失败
        }
    }


    @Override
    public String putObject(String bucketName, String fileName, File file) {
        // 获取文件名
        String contentType = ContentTypeConstants.CONTENT_TYPE_MAP.get(FileTypeUtil.getType(file).toLowerCase(Locale.ROOT));
        try (InputStream inputStream = new FileInputStream(file)) {
            /**
             * 设置要上传的流。
             * objectSize: 上传的对象大小;
             * partSize: 分段上传的大小（大概理解为，大文件可以设置为分段上传，每段的大小），单位为byte
             * 如果要上传的对象大小未知，则将 -1 传递给 objectSize，并需要传递有效的 partSize。
             * 如果要上传的对象大小已知，可以将 -1 传递给 partSize，系统会进行自动检测是否需要分段上传;也可以传递有效的 partSize 来控制对内存的使用。
             * 如果 partSize 大于 objectSize，则 objectSize 将覆盖 partSize的值。
             * partSize有效的取值大小介于 5MiB 到 5GiB 之间（前后闭区间）
             */
            PutObjectArgs putObjectArgs = PutObjectArgs.builder().bucket(bucketName).object(fileName).stream(inputStream, file.length(), -1L).contentType(contentType).build();

            ObjectWriteResponse objectWriteResponse = minioClient.putObject(putObjectArgs);
            return objectWriteResponse.object();
        } catch (Exception e) {
            log.error("MinioObsServiceImpl putObject file error, bucketName={},fileName={},errorMsg={}", bucketName, fileName, e);
            throw new OssException(ErrorCodeEnum.OSS); // 文件上传MinIo失败
        }
    }

    @Override
    public String putObjectByte(String bucketName, String fileName, byte[] fileByte) {
        String contentType = ContentTypeConstants.CONTENT_TYPE_MAP.get(fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase(Locale.ROOT));
        try (ByteArrayInputStream in = new ByteArrayInputStream(fileByte)) {
            PutObjectArgs putObjectArgs = PutObjectArgs.builder().bucket(bucketName).object(fileName)
                    /**
                     * 设置要上传的流。
                     * objectSize: 上传的对象大小;
                     * partSize: 分段上传的大小（大概理解为，大文件可以设置为分段上传，每段的大小），单位为byte
                     * 如果要上传的对象大小未知，则将 -1 传递给 objectSize，并需要传递有效的 partSize。
                     * 如果要上传的对象大小已知，可以将 -1 传递给 partSize，系统会进行自动检测是否需要分段上传；也可以传递有效的 partSize 来控制对内存的使用。
                     * 如果 partSize 大于 objectSize，则 objectSize 将覆盖 partSize的值。
                     * partSize有效的取值大小介于 5MiB 到 5GiB 之间（前后闭区间）
                     */.stream(in, (long) fileByte.length, -1L).contentType(contentType) // 文件类型
                    .build();
            ObjectWriteResponse objectWriteResponse = minioClient.putObject(putObjectArgs);
            return objectWriteResponse.object();
        } catch (Exception e) {
            log.error("MinioObsServiceImpl putObjectByte error, bucketName={},fileName={},errorMsg={}", bucketName, fileName, e);
            throw new OssException(ErrorCodeEnum.OSS); // 文件上传MinIo失败
        }
    }

    @Override
    public InputStream getObject(String bucketName, String objectName, Long offset, Long length) {
        GetObjectArgs.Builder getObjectArgs = GetObjectArgs.builder().bucket(bucketName).object(objectName);
        if (Objects.nonNull(offset)) {
            getObjectArgs.offset(offset);
        }
        if (Objects.nonNull(length)) {
            getObjectArgs.length(length);
        }
        GetObjectResponse response;
        try {
            response = minioClient.getObject(getObjectArgs.build());
        } catch (Exception e) {
            log.error("MinioObsServiceImpl getObject error, bucketName={},objectName={},offset={},length={},errorMsg={}", bucketName, objectName, offset, length, e);
            throw new OssException(ErrorCodeEnum.OSS); // 从MinIo下载文件失败
        }
        return response;
    }

    @Override
    public InputStream getObject(String bucketName, String objectName) {
        GetObjectResponse response;
        try {
            GetObjectArgs.Builder getObjectArgs = GetObjectArgs.builder().bucket(bucketName).object(objectName);
            response = minioClient.getObject(getObjectArgs.build());
        } catch (Exception e) {
            log.error("MinioObsServiceImpl getObject error, bucketName={},objectName={},errorMsg={}", bucketName, objectName, e);
            throw new OssException(ErrorCodeEnum.OSS); // 从MinIo下载文件失败
        }
        return response;
    }

    @Override
    public void removeObject(String bucketName, String objectName) throws Exception {
        minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucketName).object(objectName).build());
    }

    @Override
    public boolean existObject(String bucketName, String objectName) {
        try {
            StatObjectResponse statObjectResponse = minioClient.statObject(StatObjectArgs.builder().bucket(bucketName).object(objectName).build());
            return Objects.nonNull(statObjectResponse);
        } catch (Exception e) {
            if (e instanceof ErrorResponseException && StringUtils.equals(((ErrorResponseException) e).getDetailMessageCode(), "NoSuchKey")) {
                return false;
            }
            log.error("MinioObsServiceImpl existObject error, bucketName={},objectName={},errorMsg={}", bucketName, objectName, e);
            throw new OssException(ErrorCodeEnum.OSS); // 获取MinIO对象的元数据失败
        }
    }

    @Override
    public void copyObject(String sourceBucketName, String sourceObjectName, String targetBucketName, String targetObjectName) {
        try {
            SourceObject sourceBuild = SourceObject.builder().bucket(sourceBucketName).object(sourceObjectName).build();
            CopyObjectArgs.Builder copyObjectArgs = CopyObjectArgs.builder().source(sourceBuild).bucket(targetBucketName).object(targetObjectName);
            minioClient.copyObject(copyObjectArgs.build());
        } catch (Exception e) {
            log.error("MinioObsServiceImpl copyObject error, sourceBucketName={},sourceObjectName={},targetBucketName={},targetObjectName={},errorMsg={}", sourceBucketName, sourceObjectName, targetBucketName, targetObjectName, e);
            throw new OssException(ErrorCodeEnum.OSS); // minio复制文件失败
        }
    }


    @Override
    public String generatePresignedDownloadUrl(String bucketName, String objectName, Duration expiration) {
        try {
            return minioClient.getPresignedObjectUrl(io.minio.GetPresignedObjectUrlArgs.builder().method(Http.Method.GET) // 下载使用 GET
                    .bucket(bucketName).object(objectName).expiry((int) expiration.getSeconds()).build());
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate presigned download URL", e);
        }
    }

    @Override
    public String generateUploadPresignedUrl(String bucketName, String objectName, Duration expiration, Map<String, String> extParam, Map<String, String> headers) {
        try {
            GetPresignedObjectUrlArgs.Builder builder = GetPresignedObjectUrlArgs.builder().method(Http.Method.PUT) // 这里演示上传，下载请改为 GET
                    .bucket(bucketName).object(objectName).expiry((int) expiration.getSeconds())

                    .skipValidation(true);
            if (MapUtils.isNotEmpty(extParam)) {
                builder.extraQueryParams(extParam);
            }
            if (MapUtils.isNotEmpty(headers)) {
                builder.extraHeaders(headers);
            }

            return minioClient.getPresignedObjectUrl(builder.build());
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate presigned URL with headers", e);
        }
    }

}