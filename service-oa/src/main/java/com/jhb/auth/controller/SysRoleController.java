package com.jhb.auth.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jhb.auth.service.SysRoleService;
import com.jhb.common.result.Result;
import com.jhb.model.system.SysRole;
import com.jhb.vo.system.AssginRoleVo;
import com.jhb.vo.system.SysRoleQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Api("角色管理接口")
@RestController
@RequestMapping("/admin/system/sysRole")
public class SysRoleController {
    @Resource
    private SysRoleService sysRoleService;

    @ApiOperation("根据用户获取角色数据")
    @GetMapping("/toAssign/{userId}")
    public Result toAssign(@PathVariable Long userId){
        Map<String, Object> map = sysRoleService.findRoleDataByUserId(userId);
        return Result.ok(map);
    }

    //一个用户id 多个角色id
    @ApiOperation("为用户分配角色")
    @PostMapping("/doAssign")
    public Result toAssign(@RequestBody AssginRoleVo assginRoleVo){
        sysRoleService.doAssign(assginRoleVo);
        return Result.ok();
    }

    @ApiOperation("查询所有角色")
    @GetMapping("/findAll")
    public Result<List<SysRole>> findAll(){
        List<SysRole> data = sysRoleService.list();
        return Result.ok(data);
    }

    @PreAuthorize("hasAnyAuthority('bnt.sysRole.list')")
    @ApiOperation("角色的条件分页查询")
    @GetMapping("/{page}/{limit}")
    public Result<IPage<SysRole>> pageQueryRole(
            @ApiParam(name = "page", value="当前页码", required = true)
            @PathVariable("page") Integer page,
            @ApiParam(name = "limit", value="每页记录数", required = true)
            @PathVariable("limit") Integer limit,
            @ApiParam(name = "sysRoleQueryVo", value="查询对象")
            SysRoleQueryVo sysRoleQueryVo
    ){
        Page<SysRole> sysRolePage = new Page<>(page, limit);
        LambdaQueryWrapper<SysRole>lambdaQueryWrapper = new LambdaQueryWrapper<>();
        if(!StringUtils.isEmpty(sysRoleQueryVo.getRoleName())){
            lambdaQueryWrapper.like(SysRole::getRoleName, sysRoleQueryVo.getRoleName());
        }
        return Result.ok(sysRoleService.page(sysRolePage, lambdaQueryWrapper));
    }

    @PreAuthorize("hasAnyAuthority('bnt.sysRole.add')")
    @ApiOperation("添加角色")
    @PostMapping("save")
    public Result save(@RequestBody SysRole role){
        boolean isSuccess = sysRoleService.save(role);
        if(isSuccess){
            return Result.ok();
        }else{
            return Result.fail();
        }
    }

    @PreAuthorize("hasAnyAuthority('bnt.sysRole.list')")
    @ApiOperation("根据id查询角色")
    @GetMapping("get/{id}")
    public Result<SysRole> get(@PathVariable("id") Long id){
        SysRole sysRole = sysRoleService.getById(id);
        return Result.ok(sysRole);
    }

    @PreAuthorize("hasAnyAuthority('bnt.sysRole.update')")
    @ApiOperation("修改角色")
    @PutMapping("update")
    public Result update(@RequestBody SysRole sysRole){
        boolean isSuccess = sysRoleService.updateById(sysRole);
        if(isSuccess){
            return Result.ok();
        }else{
            return Result.fail();
        }
    }

    @PreAuthorize("hasAnyAuthority('bnt.sysRole.remove')")
    @ApiOperation("根据id删除角色")
    @DeleteMapping("remove/{id}")
    public Result remove(@PathVariable Long id){
        boolean isSuccess = sysRoleService.removeById(id);
        if(isSuccess){
            return Result.ok();
        }else{
            return Result.fail();
        }
    }

    @PreAuthorize("hasAnyAuthority('bnt.sysRole.remove')")
    @ApiOperation("批量删除")
    @DeleteMapping("batchRemove")
    public Result batchRemove(@RequestBody List<Long> idList){
        boolean isSuccess = sysRoleService.removeByIds(idList);
        if(isSuccess){
            return Result.ok();
        }else{
            return Result.fail();
        }
    }
}
