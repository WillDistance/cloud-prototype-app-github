package com.app.upload;
import com.aliyun.oss.*;
import com.aliyun.oss.common.auth.*;
import com.aliyun.oss.common.comm.SignVersion;
import com.aliyun.oss.internal.OSSHeaders;
import com.aliyun.oss.model.GeneratePresignedUrlRequest;
import com.aliyun.oss.model.StorageClass;

import java.net.URL;
import java.util.*;
import java.util.Date;

public class GetSignUrl {
    public static void main(String[] args) throws Throwable {
        // 以华东1（杭州）的外网Endpoint为例，其它Region请按实际情况填写。
        String endpoint = "https://oss-cn-hangzhou.aliyuncs.com";
        // 从环境变量中获取访问凭证。运行本代码示例之前，请确保已设置环境变量OSS_ACCESS_KEY_ID和OSS_ACCESS_KEY_SECRET。
        EnvironmentVariableCredentialsProvider credentialsProvider = CredentialsProviderFactory.newEnvironmentVariableCredentialsProvider();
        // 填写Bucket名称，例如examplebucket。
        String bucketName = "examplebucket";
        // 填写Object完整路径，例如exampleobject.txt。Object完整路径中不能包含Bucket名称。
        String objectName = "exampleobject.txt";
        // 填写Bucket所在地域。以华东1（杭州）为例，Region填写为cn-hangzhou。
        String region = "cn-hangzhou";

        // 创建OSSClient实例。
        // 当OSSClient实例不再使用时，调用shutdown方法以释放资源。
        ClientBuilderConfiguration clientBuilderConfiguration = new ClientBuilderConfiguration();
        clientBuilderConfiguration.setSignatureVersion(SignVersion.V4);
        OSS ossClient = OSSClientBuilder.create()
                .endpoint(endpoint)
                .credentialsProvider(credentialsProvider)
                .clientConfiguration(clientBuilderConfiguration)
                .region(region)
                .build();

        // 构造回调参数
        String callbackUrl = "http://www.example.com/callback";
        String callbackBody = "{\"callbackUrl\":\"" + callbackUrl + "\",\"callbackBody\":\"bucket=${bucket}&object=${object}&my_var_1=${x:var1}&my_var_2=${x:var2}\"}";
        String callbackBase64 = Base64.getEncoder().encodeToString(callbackBody.getBytes());

        String callbackVarJson = "{\"x:var1\":\"value1\",\"x:var2\":\"value2\"}";
        String callbackVarBase64 = Base64.getEncoder().encodeToString(callbackVarJson.getBytes());

        // 设置请求头。
        Map<String, String> headers = new HashMap<String, String>();
        // 指定StorageClass。
        headers.put(OSSHeaders.STORAGE_CLASS, StorageClass.Standard.toString());
        // 指定ContentType。
        headers.put(OSSHeaders.CONTENT_TYPE, "text/plain; charset=utf8");

        // 指定CALLBACK。
        headers.put(OSSHeaders.OSS_HEADER_CALLBACK, callbackBase64);
        // 指定CALLBACK-VAR。
        headers.put(OSSHeaders.OSS_HEADER_CALLBACK_VAR, callbackVarBase64);

        // 设置用户自定义元数据。
        Map<String, String> userMetadata = new HashMap<String, String>();
        userMetadata.put("key1","value1");
        userMetadata.put("key2","value2");

        URL signedUrl = null;
        try {
            // 指定生成的预签名URL过期时间，单位为毫秒。本示例以设置过期时间为1小时为例。
            Date expiration = new Date(new Date().getTime() + 3600 * 1000L);

            // 生成预签名URL。
            GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucketName, objectName, HttpMethod.PUT);
            // 设置过期时间。
            request.setExpiration(expiration);

            // 将请求头加入到request中。
            request.setHeaders(headers);
            // 添加用户自定义元数据。
            request.setUserMetadata(userMetadata);

            // 通过HTTP PUT请求生成预签名URL。
            signedUrl = ossClient.generatePresignedUrl(request);
            // 打印预签名URL。
            System.out.println("signed url for putObject: " + signedUrl);

        } catch (OSSException oe) {
            System.out.println("Caught an OSSException, which means your request made it to OSS, "
                    + "but was rejected with an error response for some reason.");
            System.out.println("Error Message:" + oe.getErrorMessage());
            System.out.println("Error Code:" + oe.getErrorCode());
            System.out.println("Request ID:" + oe.getRequestId());
            System.out.println("Host ID:" + oe.getHostId());
        } catch (ClientException ce) {
            System.out.println("Caught an ClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with OSS, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message:" + ce.getMessage());
        }
    }
}       