package com.xuecheng.content.feignclient;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * ClassName: MediaServiceClientFallback
 * Package: com.xuecheng.content.feignclient
 * Description:
 *
 * @Author 何琛
 * @Create 2024/9/14 14:07
 * @Version 1.0
 */
//这种方法拿不到异常
public class MediaServiceClientFallback implements MediaServiceClient{
    @Override
    public String upload(MultipartFile filedata, String objectName) throws IOException {
        return null;
    }
}
