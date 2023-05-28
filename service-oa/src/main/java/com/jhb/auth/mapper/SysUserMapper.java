package com.jhb.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jhb.model.system.SysUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 用户表 Mapper 接口
 * </p>
 *
 * @author jhb
 * @since 2023-03-19
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

}
