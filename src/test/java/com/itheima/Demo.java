package com.itheima;


import com.aliyun.oss.*;
import com.aliyun.oss.common.auth.*;
import com.aliyun.oss.model.*;

import java.io.File;

public class Demo {

    public static void main(String[] args) throws Exception {
        String endpoint = "https://oss-cn-beijing.aliyuncs.com";

        //读取环境变量密钥
        EnvironmentVariableCredentialsProvider credentialsProvider = CredentialsProviderFactory.newEnvironmentVariableCredentialsProvider();

        String bucketName = "big-even7t";
        String objectName = "001.jpg";

        // ============删除V4签名配置，删掉region============
//        ClientBuilderConfiguration clientBuilderConfiguration = new ClientBuilderConfiguration();
//        clientBuilderConfiguration.setSignatureVersion(SignVersion.V4);

        OSS ossClient = OSSClientBuilder.create()
                .endpoint(endpoint)
                .credentialsProvider(credentialsProvider)
                //.clientConfiguration(clientBuilderConfiguration)
                //.region("cn-beijing")
                .build();

        try {
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, objectName, new File("D:\\桌面\\临时素材\\jjy.jpg"));
            PutObjectResult result = ossClient.putObject(putObjectRequest);
            System.out.println("🎉上传成功！");

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
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }
}
