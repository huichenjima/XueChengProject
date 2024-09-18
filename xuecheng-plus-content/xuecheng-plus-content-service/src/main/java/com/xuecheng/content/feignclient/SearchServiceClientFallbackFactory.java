package com.xuecheng.content.feignclient;

import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * ClassName: SearchServiceClientFallbackFactory
 * Package: com.xuecheng.content.feignclient
 * Description:
 *
 * @Author 何琛
 * @Create 2024/9/18 13:56
 * @Version 1.0
 */
@Slf4j
@Component
public class SearchServiceClientFallbackFactory implements FallbackFactory<SearchServiceClient> {
    @Override
    public SearchServiceClient create(Throwable throwable) {
        return new SearchServiceClient() {

            @Override
            public Boolean add(CourseIndex courseIndex) {
                log.debug("添加课程索引发生熔断，异常信息:{}",throwable.toString(),throwable);
                //走降级返回false
                return false;
            }


        };
    }
}
