package com.xuecheng.auth.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

/**
 * ClassName: DaoAuthenticationProviderCustom
 * Package: com.xuecheng.auth.config
 * Description:
 *
 * @Author 何琛
 * @Create 2024/9/22 13:58
 * 重写框架的DaoAuthenticationProvider
 * @Version 1.0
 */
@Component
@Slf4j
public class DaoAuthenticationProviderCustom extends DaoAuthenticationProvider {

    @Autowired
    public void setUserDetailsService(UserDetailsService userDetailsService) {
        super.setUserDetailsService(userDetailsService);
    }

    //统一认证方式有一些认证方式不需要校验密码，所以要重写这个校验方法
    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {





    }
}
