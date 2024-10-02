package com.xuecheng.ucenter.service;

import com.xuecheng.ucenter.model.dto.AuthParamsDto;
import com.xuecheng.ucenter.model.dto.XcUserExt;

/**
 * ClassName: AuthService
 * Package: com.xuecheng.ucenter.service
 * Description:
 *
 * @Author 何琛
 * @Create 2024/9/22 14:08
 * 统一认证接口
 * @Version 1.0
 */
public interface AuthService {

    /**
     * @description 认证方法
     * @param authParamsDto 认证参数
     * @return com.xuecheng.ucenter.model.po.XcUser 用户信息
     */
    XcUserExt execute(AuthParamsDto authParamsDto);
}
