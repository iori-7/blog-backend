package com.itheima.utils;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.InputStream;

/**
 * 阿里云 OSS 上传工具。
 * 注册成单例 Bean：OSSClient 本身是线程安全的，没必要每次上传都 new 一个再 shutdown。
 */
@Component
public class AliOssUtil {

    private static final Logger log = LoggerFactory.getLogger(AliOssUtil.class);

    private final String endpoint;
    private final String bucketName;

    // OSSClient 线程安全，构建一次反复使用；密钥没配置时为 null
    private final OSS ossClient;

    public AliOssUtil(@Value("${aliyun.oss.endpoint}") String endpoint,
                      @Value("${aliyun.oss.bucket-name}") String bucketName,
                      @Value("${aliyun.oss.access-key-id:}") String accessKeyId,
                      @Value("${aliyun.oss.access-key-secret:}") String accessKeySecret) {
        this.endpoint = endpoint;
        this.bucketName = bucketName;
        // 密钥从环境变量来（见 application.yml 的 aliyun.oss 配置），绝不写死在代码里
        if (StringUtils.hasText(accessKeyId) && StringUtils.hasText(accessKeySecret)) {
            this.ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        } else {
            this.ossClient = null;
            log.warn("未配置阿里云 OSS AccessKey（环境变量 ALIBABA_CLOUD_ACCESS_KEY_ID / ALIBABA_CLOUD_ACCESS_KEY_SECRET），文件上传功能不可用");
        }
    }

    //上传文件,返回文件的公网访问地址
    public String uploadFile(String objectName, InputStream inputStream) {
        if (ossClient == null) {
            throw new IllegalStateException("阿里云 OSS AccessKey 未配置，无法上传文件");
        }
        try {
            // 这里不再调用 createBucket：存储空间是一次性资源，在控制台建好后长期复用即可。
            // 每次上传都建桶既多一次网络往返，桶已存在时还会报错。
            ossClient.putObject(bucketName, objectName, inputStream);
            return "https://" + bucketName + "." + endpoint.substring(endpoint.lastIndexOf("/") + 1) + "/" + objectName;
        } catch (OSSException oe) {
            // 上传失败必须抛出去，不能吞掉。否则控制器会返回"上传成功"+空地址给前端
            log.error("OSS 上传被拒绝 objectName={} errorCode={} requestId={} hostId={}",
                    objectName, oe.getErrorCode(), oe.getRequestId(), oe.getHostId(), oe);
            throw new RuntimeException("文件上传失败，请稍后重试", oe);
        } catch (ClientException ce) {
            log.error("OSS 客户端异常，通常是网络不通 objectName={}", objectName, ce);
            throw new RuntimeException("文件上传失败，请检查网络后重试", ce);
        }
    }
}
