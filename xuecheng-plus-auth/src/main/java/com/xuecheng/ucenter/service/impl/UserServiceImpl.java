package com.xuecheng.ucenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.ucenter.mapper.XcUserMapper;
import com.xuecheng.ucenter.model.po.XcUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;

/**
 * ClassName: UserServiceImpl
 * Package: com.xuecheng.ucenter.service.impl
 * Description:
 *
 * @Author 何琛
 * @Create 2024/9/21 13:40
 * @Version 1.0
 */
@Service
public class UserServiceImpl implements UserDetailsService {
    @Autowired
    XcUserMapper xcUserMapper;
    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        String username=s;
        //根据用户名查询数据库
        XcUser xcUser = xcUserMapper.selectOne(new LambdaQueryWrapper<XcUser>().eq(XcUser::getUsername, username));
        //查询到用户不存在，返回null即可，spring security框架抛出异常用户不存在
        if (xcUser==null)
            return null;
        //如果查到了用户且拿到了正确的密码，最终封装成一个UserDetails对象给框架，由框架进行比对
        String[] authorities={"test"};
        UserDetails userDetails = User.withUsername(xcUser.getUsername()).password(xcUser.getPassword()).authorities(authorities).build();

        return userDetails;
    }
}
