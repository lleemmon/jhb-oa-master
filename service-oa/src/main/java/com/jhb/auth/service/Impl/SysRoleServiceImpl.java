package com.jhb.auth.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jhb.auth.mapper.SysRoleMapper;
import com.jhb.auth.mapper.SysUserRoleMapper;
import com.jhb.auth.service.SysRoleService;
import com.jhb.model.system.SysRole;
import com.jhb.model.system.SysUserRole;
import com.jhb.vo.system.AssginRoleVo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;


@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements SysRoleService {

    @Resource
    private SysUserRoleMapper sysUserRoleMapper;
    @Override
    public Map<String, Object> findRoleDataByUserId(Long userId) {

        //全部用户
        List<SysRole> allRoleList = baseMapper.selectList(null);
        //用户包含用户
        LambdaQueryWrapper<SysUserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(SysUserRole::getRoleId);
        wrapper.eq(SysUserRole::getUserId, userId);
        List<SysUserRole> existUserRoleList = sysUserRoleMapper.selectList(wrapper);
        //从查询出来的用户id对应角色List集合 获取所有角色Id
        List<Long> existRoleIdList = existUserRoleList.stream().map(SysUserRole::getRoleId).collect(Collectors.toList());
        List<SysRole> assignRoleList = allRoleList.stream().filter(item -> existRoleIdList.contains(item.getId())).collect(Collectors.toList());
        Map<String, Object> map = new HashMap<>();
        map.put("assginRoleList", assignRoleList);
        map.put("allRolesList", allRoleList);
        return map;
    }

    @Override
    @Transactional
    public void doAssign(AssginRoleVo assginRoleVo) {
        //根据userId删除对应角色
        LambdaQueryWrapper<SysUserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserRole::getUserId, assginRoleVo.getUserId());
        sysUserRoleMapper.delete(wrapper);
        List<Long> roleIdList = assginRoleVo.getRoleIdList();
        SysUserRole sysUserRole = new SysUserRole();
        sysUserRole.setUserId(assginRoleVo.getUserId());
        roleIdList.forEach(item -> {
            sysUserRole.setRoleId(item);
            sysUserRoleMapper.insert(sysUserRole);
        });
    }
}
