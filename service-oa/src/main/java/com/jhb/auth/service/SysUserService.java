package com.jhb.auth.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jhb.model.system.SysUser;

import java.util.Map;

/**
 * <p>
 * 用户表 服务类
 * </p>
 *
 * @author jhb
 * @since 2023-03-19
 */
public interface SysUserService extends IService<SysUser> {

    void updateStatus(Long id, Integer status);

    SysUser getByUsername(String username);

    Map<String, Object> getCurrentUser();
}
