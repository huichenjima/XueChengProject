package com.xuecheng.ucenter.feignclient;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * ClassName: CheckCodeClient
 * Package: com.xuecheng.ucenter.feignclient
 * Description:
 *
 * @Author 何琛
 * @Create 2024/9/25 14:27
 * @Version 1.0
 */
@FeignClient(value = "checkcode",fallbackFactory = CheckCodeClientFactory.class)//这里设置服务名
@RequestMapping("/checkcode")
public interface CheckCodeClient {

    @PostMapping(value = "/verify")
    public Boolean verify(@RequestParam("key") String key, @RequestParam("code") String code);




}
