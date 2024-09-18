package com.xuecheng.content.feignclient;

import com.xuecheng.content.config.MultipartSupportConfig;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * ClassName: MediaServiceClient
 * Package: com.xuecheng.content.feignclient
 * Description:
 *
 * @Author 何琛
 * @Create 2024/9/14 13:26
 * @Version 1.0
 */
//fallback拿不到异常
//FallbackFactory可以拿到异常
@FeignClient(value = "media-api",configuration = {MultipartSupportConfig.class},fallbackFactory = SearchServiceClientFallbackFactory.class)//这里设置服务名
public interface SearchServiceClient {


    @PostMapping("/search/index/course")
    public Boolean add(@RequestBody CourseIndex courseIndex);

}
