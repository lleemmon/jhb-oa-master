package com.jhb.process.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jhb.model.process.ProcessTemplate;
import com.jhb.model.process.ProcessType;
import com.jhb.process.mapper.OaProcessTemplateMapper;
import com.jhb.process.mapper.OaProcessTypeMapper;
import com.jhb.process.service.OaProcessService;
import com.jhb.process.service.OaProcessTemplateService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 审批模板 服务实现类
 * </p>
 *
 * @author jhb
 * @since 2023-03-24
 */
@Service
public class OaProcessTemplateServiceImpl extends ServiceImpl<OaProcessTemplateMapper, ProcessTemplate> implements OaProcessTemplateService {

    @Resource
    private OaProcessTypeMapper oaProcessTypeMapper;
    @Resource
    private OaProcessService oaProcessService;
    @Override
    public IPage<ProcessTemplate> selectPageProcessTempate(Page<ProcessTemplate> pageParam) {
        //实现分页查询
        Page<ProcessTemplate> processTemplatePage = baseMapper.selectPage(pageParam, null);
        //获取分页数据中的数据
        List<ProcessTemplate> records = processTemplatePage.getRecords();
        //获取所有records的ids集合
        List<Long> processTypeIds = records.stream().distinct().map(ProcessTemplate::getProcessTypeId).collect(Collectors.toList());
        LambdaQueryWrapper<ProcessType> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(ProcessType::getId);
        wrapper.select(ProcessType::getName);
        wrapper.in(ProcessType::getId, processTypeIds);
        List<ProcessType> processTypes = oaProcessTypeMapper.selectList(wrapper);
        Map<Long, String> hashMap = new HashMap<>();
        processTypes.forEach(item -> hashMap.put(item.getId(), item.getName()));
        records.forEach(item -> item.setProcessTypeName(hashMap.get(item.getProcessTypeId())));
        return processTemplatePage;
    }

    @Override
    public void publish(Long id) {
        ProcessTemplate processTemplate = baseMapper.selectById(id);
        processTemplate.setStatus(1);
        baseMapper.updateById(processTemplate);
        if(!StringUtils.isEmpty(processTemplate.getProcessDefinitionPath())){
            oaProcessService.deployByZip(processTemplate.getProcessDefinitionPath());
        }
        //后续完善 流程定义部署
    }
}
