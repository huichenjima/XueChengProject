package com.xuecheng.ucenter.service;

import com.xuecheng.ucenter.model.dto.XcUserExt;
import com.xuecheng.ucenter.model.po.XcUser;

/**
 * ClassName: WxAuthService
 * Package: com.xuecheng.ucenter.service
 * Description:
 *
 * @Author 何琛
 * @Create 2024/9/26 21:16
 * @Version 1.0
 */
public interface WxAuthService {
    /**
     *
     * @param code
     * 微信扫码认证，申请令牌，携带令牌查询用户信息，保存用户信息到数据库
     * @return
     */
    public XcUser wxAuth(String code);
}
