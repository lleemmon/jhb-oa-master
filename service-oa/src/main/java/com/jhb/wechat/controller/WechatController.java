package com.jhb.wechat.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jhb.auth.service.SysUserService;
import com.jhb.common.jwt.JwtHelper;
import com.jhb.common.result.Result;
import com.jhb.model.system.SysUser;
import com.jhb.vo.wechat.BindPhoneVo;
import io.swagger.annotations.ApiOperation;
import me.chanjar.weixin.common.api.WxConsts;
import me.chanjar.weixin.common.bean.WxOAuth2UserInfo;
import me.chanjar.weixin.common.bean.oauth2.WxOAuth2AccessToken;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.net.URLEncoder;

@Controller
@RequestMapping("/admin/wechat")
@CrossOrigin
public class WechatController {
    @Resource
    private SysUserService sysUserService;
    @Resource
    private WxMpService wxMpService;
    @Value("${wechat.userInfoUrl}")
    private String userInfoUrl;

    @ApiOperation("认证")
    @GetMapping("/authorize")
    public String authorize(@RequestParam("returnUrl") String returnUrl,
                            HttpServletRequest request){
        // 参数1 授权路径
        // 参数2 授权类型
        // 参数3 授权成功之后 跳转的路径 授权成功之后 跳转路径 ‘guiguoa’ 转化为 #
        String redirectUrl = wxMpService.getOAuth2Service().buildAuthorizationUrl(userInfoUrl,
                WxConsts.OAuth2Scope.SNSAPI_USERINFO,
                URLEncoder.encode(returnUrl.replace("guiguoa", "#")));
        return "redirect:" + redirectUrl;
    }

    // code 类似 手机验证码
    @ApiOperation("获取用户信息")
    @GetMapping("/userInfo")
    public String userInfo(
            @RequestParam("code") String code,
            @RequestParam("returnUrl") String returnUrl
        ) throws WxErrorException {
        //获取accessToken
        WxOAuth2AccessToken accessToken = wxMpService.getOAuth2Service().getAccessToken(code);
        //使用accessToken获取openId
        String openId = accessToken.getOpenId();
        //获取微信用户信息
        WxOAuth2UserInfo wxMpUser = wxMpService.getOAuth2Service().getUserInfo(accessToken, null);
        //根据openId查询用户表
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getOpenId, openId);
        SysUser sysuser = sysUserService.getOne(wrapper);
        String token = "";
        if(sysuser != null){
            token = JwtHelper.createToken(sysuser.getId(), sysuser.getUsername());
        }
        char sep = returnUrl.contains("?") ? '&' : '?';
        return "redirect:" + returnUrl + sep + "token=" + token + "openId=" + openId;
    }

    @ApiOperation("微信账号绑定手机")
    @GetMapping("/bindPhone")
    @ResponseBody
    public Result<String> bindPhone(@RequestBody BindPhoneVo bindPhoneVo) throws WxErrorException {
        LambdaQueryWrapper<SysUser>wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getPhone, bindPhoneVo.getPhone());
        SysUser sysUser = sysUserService.getOne(wrapper);
        if(sysUser != null){
            sysUser.setOpenId(bindPhoneVo.getOpenId());
            sysUserService.updateById(sysUser);
            String token = JwtHelper.createToken(sysUser.getId(), sysUser.getUsername());
            return Result.ok(token);
        }else{
            return Result.fail("手机号码不存在");
        }
    }

}
