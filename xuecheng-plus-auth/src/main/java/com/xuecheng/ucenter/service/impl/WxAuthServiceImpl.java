package com.xuecheng.ucenter.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.ucenter.mapper.XcUserMapper;
import com.xuecheng.ucenter.mapper.XcUserRoleMapper;
import com.xuecheng.ucenter.model.dto.AuthParamsDto;
import com.xuecheng.ucenter.model.dto.XcUserExt;
import com.xuecheng.ucenter.model.po.XcUser;
import com.xuecheng.ucenter.model.po.XcUserRole;
import com.xuecheng.ucenter.service.AuthService;
import com.xuecheng.ucenter.service.WxAuthService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * ClassName: PasswordAuthServiceImpl
 * Package: com.xuecheng.ucenter.service.impl
 * Description:
 *
 * @Author 何琛
 * @Create 2024/9/22 14:10
 * @Version 1.0
 */
//微信扫码认证
@Service("wx_authservice")
public class WxAuthServiceImpl implements AuthService, WxAuthService {

    @Value("${weixin.appid}")
    String appid;

    @Value("${weixin.secret}")
    String secret;

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    XcUserMapper xcUserMapper;

    @Autowired
    XcUserRoleMapper xcUserRoleMapper;

    @Autowired
    WxAuthServiceImpl currentProxy;
    /**
     *
     * @param code
     * 微信扫码认证，申请令牌，携带令牌查询用户信息，保存用户信息到数据库
     * @return
     */
    @Override
    public XcUser wxAuth(String code) {
        //申请令牌
        Map<String, String> access_token = getAccess_token(code);

        String token = access_token.get("access_token");
        String openid = access_token.get("openid");
        //携带令牌查询用户信息
        Map<String, String> userinfo = getUserinfo(token, openid);

        //保存用户到数据库
        XcUser xcUser = currentProxy.addWxUser(userinfo);



        return xcUser;
    }


    @Override
    public XcUserExt execute(AuthParamsDto authParamsDto) {
        //得到账号
        String username = authParamsDto.getUsername();
        //查询数据库
        XcUser xcUser = xcUserMapper.selectOne(new LambdaQueryWrapper<XcUser>().eq(XcUser::getUsername, username));
        if (xcUser==null)
            throw new RuntimeException("用户不存在");
        XcUserExt xcUserExt = new XcUserExt();
        BeanUtils.copyProperties(xcUser,xcUserExt);
        return xcUserExt;


    }

    /**
     * 携带授权码来申请令牌 https://api.weixin.qq.com/sns/oauth2/access_token?appid=APPID&secret=SECRET&code=CODE&grant_type=authorization_code
     * 响应内容为
     * {
     * "access_token":"ACCESS_TOKEN",
     * "expires_in":7200,
     * "refresh_token":"REFRESH_TOKEN",
     * "openid":"OPENID",
     * "scope":"SCOPE",
     * "unionid": "o6_bmasdasdsad6_2sgVt7hMZOPfL"
     * }
     * @param code
     * @return
     */
    private Map<String,String> getAccess_token(String code){

        String url_template="https://api.weixin.qq.com/sns/oauth2/access_token?appid=%s&secret=%s&code=%s&grant_type=authorization_code";
        String url = String.format(url_template, appid, secret, code);

        ResponseEntity<String> exchange = restTemplate.exchange(url, HttpMethod.POST, null, String.class);

        String result = exchange.getBody();

        Map<String,String> map = JSON.parseObject(result, Map.class);

        return map;


    }

    /**
     * 携带令牌方法查询用户信息
     * http请求方式: GET
     * https://api.weixin.qq.com/sns/userinfo?access_token=ACCESS_TOKEN&openid=OPENID
     * 响应结果
     * {
     * "openid":"OPENID",
     * "nickname":"NICKNAME",
     * "sex":1,
     * "province":"PROVINCE",
     * "city":"CITY",
     * "country":"COUNTRY",
     * "headimgurl": "https://thirdwx.qlogo.cn/mmopen/g3MonUZtNHkdmzicIlibx6iaFqAc56vxLSUfpb6n5WKSYVY0ChQKkiaJSgQ1dZuTOgvLLrhJbERQQ4eMsv84eavHiaiceqxibJxCfHe/0",
     * "privilege":[
     * "PRIVILEGE1",
     * "PRIVILEGE2"
     * ],
     * "unionid": " o6_bmasdasdsad6_2sgVt7hMZOPfL"
     *
     * }
     * @param access_token
     * @param openid
     * @return
     */
    private Map<String,String> getUserinfo(String access_token,String openid){
        String url_Template="https://api.weixin.qq.com/sns/userinfo?access_token=%s&openid=%s";
        String url = String.format(url_Template,access_token, openid);

        ResponseEntity<String> exchange = restTemplate.exchange(url, HttpMethod.POST, null, String.class);

        String result = new String(exchange.getBody().getBytes(StandardCharsets.ISO_8859_1),StandardCharsets.UTF_8);

        Map<String,String> map = JSON.parseObject(result, Map.class);
        return map;

    }

    @Transactional
    public XcUser addWxUser(Map<String,String> userInfo_map){

        String  unionid = userInfo_map.get("unionid");
        String nickname = userInfo_map.get("nickname");

        XcUser xcUser = xcUserMapper.selectOne(new LambdaQueryWrapper<XcUser>().eq(XcUser::getWxUnionid, unionid));
        if (xcUser!=null)
            //更新数据库？他这里没有进行更新直接返回的
            return xcUser;
        //插入数据库
        xcUser = new XcUser();
        xcUser.setId(UUID.randomUUID().toString());
        xcUser.setUsername(unionid);
        xcUser.setPassword(unionid);
        xcUser.setWxUnionid(unionid);
        xcUser.setNickname(nickname);
        xcUser.setName(nickname);
        xcUser.setUtype("101001");//学生类型
        xcUser.setStatus("1");//用户状态
        xcUser.setCreateTime(LocalDateTime.now());
        //插入
        int insert = xcUserMapper.insert(xcUser);

        XcUserRole xcUserRole = new XcUserRole();
        xcUserRole.setId(UUID.randomUUID().toString());
        xcUserRole.setUserId(xcUser.getId());
        xcUserRole.setRoleId("17"); //17号表示学生角色
        xcUserRole.setCreateTime(LocalDateTime.now());

        int insert1 = xcUserRoleMapper.insert(xcUserRole);

        return xcUser;




    }
}
