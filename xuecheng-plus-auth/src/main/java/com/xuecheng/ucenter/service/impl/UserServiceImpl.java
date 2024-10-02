package com.xuecheng.ucenter.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.ucenter.feignclient.CheckCodeClient;
import com.xuecheng.ucenter.mapper.XcMenuMapper;
import com.xuecheng.ucenter.mapper.XcUserMapper;
import com.xuecheng.ucenter.model.dto.AuthParamsDto;
import com.xuecheng.ucenter.model.dto.XcUserExt;
import com.xuecheng.ucenter.model.po.XcMenu;
import com.xuecheng.ucenter.model.po.XcUser;
import com.xuecheng.ucenter.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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

    @Autowired
    ApplicationContext applicationContext;


    @Autowired
    XcMenuMapper xcMenuMapper;



    //传入的请求认证的参数就是AuthParamsDto
    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {

        //将传入的json转换为AuthParmsDto对象
        AuthParamsDto authParamsDto = null;
        try {
            authParamsDto = JSON.parseObject(s, AuthParamsDto.class);
        } catch (Exception e) {
            throw  new RuntimeException("请求认证的参数不符合要求");
        }

        //认证类型，有password ，wx 。。。
        String authType = authParamsDto.getAuthType();

        //根据认证类型从spring容器中取出指定的bean
        String beanName=authType+"_authservice";
        AuthService authService = applicationContext.getBean(beanName,AuthService.class);
        //调用对应的校验方法，即excute方法完成认证
        XcUserExt xcUserExt = authService.execute(authParamsDto);

        //根据userDetails对象生成令牌
        UserDetails userPrincipal = getUserPrincipal(xcUserExt);

        return userPrincipal;
    }

    /**
     * @description 查询用户信息
     * @param user  用户id，主键
     * @return com.xuecheng.ucenter.model.po.XcUser 用户信息
     * @author Mr.M
     * @date 2022/9/29 12:19
     */
    public UserDetails getUserPrincipal(XcUserExt user){
        //调用对应的校验方法，即excute方法完成认证


        //如果查到了用户且拿到了正确的密码，最终封装成一个UserDetails对象给框架，由框架进行比对
        //TODO 手动授权，这个要改
        String[] authorities={"test"};

        //这里要根据角色去数据库查权限授权

        List<XcMenu> xcMenus = xcMenuMapper.selectPermissionByUserId(user.getId());

        if (xcMenus.size()>0)
        {
//            List<String> permissions=new ArrayList<>();
            //拿到用户的权限标识符
            List<String> permissions = xcMenus.stream().map(xcMenu -> xcMenu.getCode()).collect(Collectors.toList());
            authorities = permissions.toArray(new String[0]);

        }




        //将用户信息转成json
        String password = user.getPassword();
        //将敏感信息置空
        user.setPassword(null);
        String userJson = JSON.toJSONString(user);

        UserDetails userDetails = User.withUsername(userJson).password(password).authorities(authorities).build();
        return userDetails;

    }
}
