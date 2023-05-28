package com.jhb.process.controller.api;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jhb.auth.mapper.SysUserMapper;
import com.jhb.auth.service.SysUserService;
import com.jhb.common.result.Result;
import com.jhb.model.process.ProcessTemplate;
import com.jhb.process.mapper.OaProcessTemplateMapper;
import com.jhb.process.service.OaProcessService;
import com.jhb.process.service.OaProcessTemplateService;
import com.jhb.process.service.OaProcessTypeService;
import com.jhb.vo.process.ApprovalVo;
import com.jhb.vo.process.ProcessFormVo;
import com.jhb.vo.process.ProcessVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Api(tags = "审批流管理")
@RestController
@RequestMapping("/admin/process")
@CrossOrigin
public class ProcessApiController {
    @Resource
    private OaProcessTypeService oaProcessTypeService;
    @Resource
    private OaProcessTemplateService oaProcessTemplateService;
    @Resource
    private OaProcessService oaProcessService;

    @Resource
    private SysUserService sysUserService;

    @ApiOperation("获取全部审批分类及模板")
    @GetMapping("findProcessType")
    public Result findProcessType(){
        return Result.ok(oaProcessTypeService.findProcessType());
    }

    @ApiOperation("获取审批模板")
    @GetMapping("getProcessTemplate/{processTemplateId}")
    public Result get(@PathVariable Long processTemplateId){
        ProcessTemplate processTemplate = oaProcessTemplateService.getById(processTemplateId);
        return Result.ok(processTemplate);
    }

    @ApiOperation("启动流程")
    @PostMapping("/startUp")
    public Result startUp(@RequestBody ProcessFormVo processFormVo){
        oaProcessService.startUp(processFormVo);
        return Result.ok();
    }

    @ApiOperation(value = "待处理")
    @GetMapping("/findPending/{page}/{limit}")
    public Result findPending(
            @ApiParam(name = "page", value = "当前页码", required = true)
            @PathVariable Long page,
            @ApiParam(name = "limit", value = "每页记录数", required = true)
            @PathVariable Long limit) {
        Page<Process> pageParam = new Page<>(page,limit);
        IPage<ProcessVo> pageModel = oaProcessService.findfindPending(pageParam);
        return Result.ok(pageModel);
    }

    @ApiOperation(value = "待处理")
    @GetMapping("/show/{id}")
    public Result show(
            @ApiParam(name = "id", value = "process的id", required = true)
            @PathVariable Long id
    ) {
        Map<String, Object> map = oaProcessService.show(id);
        return Result.ok(map);
    }

    //审批
    @ApiOperation(value = "审批")
    @PostMapping("approve")
    public Result approve(@RequestBody ApprovalVo approvalVo) {
        oaProcessService.approve(approvalVo);
        return Result.ok();
    }

    @ApiOperation(value = "已处理")
    @GetMapping("/findProcessed/{page}/{limit}")
    public Result findProcessed(
            @ApiParam(name = "page", value = "当前页码", required = true)
            @PathVariable Long page,
            @ApiParam(name = "limit", value = "每页记录数", required = true)
            @PathVariable Long limit) {
        Page<ProcessVo> pageParam = new Page<>(page,limit);
        IPage<ProcessVo> pageModel = oaProcessService.findProcessed(pageParam);
        return Result.ok(pageModel);
    }

    @ApiOperation(value = "已发起")
    @GetMapping("/findStarted/{page}/{limit}")
    public Result findStarted(
            @ApiParam(name = "page", value = "当前页码", required = true)
            @PathVariable Long page,
            @ApiParam(name = "limit", value = "每页记录数", required = true)
            @PathVariable Long limit) {
        Page<ProcessVo> pageParam = new Page<>(page, limit);
        IPage<ProcessVo> pageModel = oaProcessService.findStarted(pageParam);
        return Result.ok(pageModel);
    }

    @GetMapping("getCurrentUser")
    public Result getCurrentUser() {
        Map<String,Object> map = sysUserService.getCurrentUser();
        return Result.ok(map);
    }

}
