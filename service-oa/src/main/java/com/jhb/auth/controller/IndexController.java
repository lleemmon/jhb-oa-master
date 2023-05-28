package com.jhb.auth.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jhb.auth.service.SysMenuService;
import com.jhb.auth.service.SysUserService;
import com.jhb.common.config.exception.GuiguException;
import com.jhb.common.jwt.JwtHelper;
import com.jhb.common.result.Result;
import com.jhb.common.utils.MD5;
import com.jhb.model.system.SysUser;
import com.jhb.vo.system.LoginVo;
import com.jhb.vo.system.RouterVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Api("用户相关")
@RestController
@RequestMapping("/admin/system/index")
public class IndexController {
    @Resource
    private SysUserService sysUserService;
    @Resource
    private SysMenuService sysMenuService;

    @ApiOperation("登录")
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody LoginVo loginVo){
        String username = loginVo.getUsername();
        LambdaQueryWrapper<SysUser> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.eq(SysUser::getUsername, username);
        SysUser sysUser = sysUserService.getOne(userWrapper);
        if(sysUser == null){
            throw new GuiguException(201, "用户不存在");
        }
        String password = loginVo.getPassword();
        String passwordMd5 = MD5.encrypt(password);
        if(!passwordMd5.equals(sysUser.getPassword())){
            throw new GuiguException(201, "密码错误");
        }
        if(sysUser.getStatus().equals(0)){
            throw new GuiguException(201, "用户已被禁用");
        }
        String token = JwtHelper.createToken(sysUser.getId(), username);
        Map<String, Object>map = new HashMap<>();
        map.put("token", token);
        return Result.ok(map);
    }

    @ApiOperation("获取用户信息")
    @GetMapping("/info")
    public Result<Map<String, Object>> info(HttpServletRequest request){
        //1、 从请求头获取用户信息(获取请求头token字符串)
        String token = request.getHeader("token");
        //2、从token字符串获取userId 或者 username
        Long userId = JwtHelper.getUserId(token);
        //3、根据用户id查询数据库
        SysUser sysuser = sysUserService.getById(userId);
        if(sysuser == null){
            throw new GuiguException(201, "用户不存在");
        }
        //4 根据用户id
        // 获取用户可以操作菜单列表
        // 查询数据库 动态构建路由
        // // 获取用户可以操作按钮列表
        List<RouterVo> routerList = sysMenuService.findUserMenuListByUserId(userId);
        List<String> permsList = sysMenuService.findUserPermsListByUserId(userId);

        Map<String, Object> map = new HashMap<>();
        map.put("roles", "[admin]");
        map.put("name", sysuser.getName());
        map.put("avatar", "https://oss.aliyuncs.com/aliyun_id_photo_bucket/default_handsome.jpg");
        map.put("routers", routerList);
        map.put("buttons", permsList);
        return Result.ok(map);
    }

    @ApiOperation("退出登录")
    @GetMapping("/logout")
    public Result<Map<String, Object>> logout(){
        return Result.ok();
    }
}
