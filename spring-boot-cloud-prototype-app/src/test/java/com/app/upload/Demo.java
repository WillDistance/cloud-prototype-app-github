package com.app.upload;

import com.aliyun.oss.*;
import com.aliyun.oss.common.auth.CredentialsProviderFactory;
import com.aliyun.oss.common.auth.EnvironmentVariableCredentialsProvider;
import com.aliyun.oss.common.comm.SignVersion;
import com.aliyun.oss.model.GeneratePresignedUrlRequest;

import java.net.URL;
import java.net.URLEncoder;
import java.util.Date;

/**
 * 生成强制下载的预签名url
 *
 * @author yanlei
 * @since 2026-09-05
 */
public class Demo {
    public static void main(String[] args) throws Throwable {
        // 以华东1（杭州）的外网Endpoint为例，其它Region请按实际情况填写。
        String endpoint = "https://oss-cn-shenzhen.aliyuncs.com";
        // 从环境变量中获取访问凭证。运行本代码示例之前，请确保已设置环境变量OSS_ACCESS_KEY_ID和OSS_ACCESS_KEY_SECRET。
        EnvironmentVariableCredentialsProvider credentialsProvider = CredentialsProviderFactory.newEnvironmentVariableCredentialsProvider();
        // 填写Bucket名称，例如examplebucket。
        String bucketName = "yl-img-bucket";
        // 填写Object完整路径，例如exampleobject.txt。Object完整路径中不能包含Bucket名称。
        String objectName = "2026-09-05/01-login.png";
        // 填写Bucket所在地域。以华东1（杭州）为例，Region填写为cn-hangzhou。
        String region = "cn-shenzhen";


        ClientBuilderConfiguration clientBuilderConfiguration = new ClientBuilderConfiguration();
        // 请注意，若您使用自定义域名，需设置CNAME为true。
        clientBuilderConfiguration.setSupportCname(true);

        //设置V4签名算法
        clientBuilderConfiguration.setSignatureVersion(SignVersion.V4);
        OSS ossClient = OSSClientBuilder.create()
                .endpoint(endpoint)
                .credentialsProvider(credentialsProvider)
                .clientConfiguration(clientBuilderConfiguration)
                .region(region)
                .build();

        URL signedUrl = null;
        try {
            // 指定生成的预签名URL过期时间，单位为毫秒。本示例以设置过期时间为1小时为例。
            Date expiration = new Date(new Date().getTime() + 3600 * 1000L);

            // 定义客户端下载时显示的文件名，此处以"homework.txt"为例。
            String filename = "homework.png";

            // 生成预签名URL。
            GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucketName, objectName, HttpMethod.GET);

            // 设置下载行为（强制下载而非预览）及文件名
            request.getResponseHeaders().setContentDisposition("attachment;filename=" + URLEncoder.encode(filename,"UTF-8"));

            // 设置过期时间。
            request.setExpiration(expiration);

            // 通过HTTP GET请求生成预签名URL。
            signedUrl = ossClient.generatePresignedUrl(request);
            // 打印预签名URL。
            System.out.println("signed url for getObject: " + signedUrl);
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