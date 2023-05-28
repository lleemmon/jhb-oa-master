package com.jhb.process.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jhb.model.process.ProcessTemplate;
import com.jhb.model.process.ProcessType;
import com.jhb.process.mapper.OaProcessTemplateMapper;
import com.jhb.process.mapper.OaProcessTypeMapper;
import com.jhb.process.service.OaProcessTemplateService;
import com.jhb.process.service.OaProcessTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 审批类型 服务实现类
 * </p>
 *
 * @author jhb
 * @since 2023-03-24
 */
@Service
public class OaProcessTypeServiceImpl extends ServiceImpl<OaProcessTypeMapper, ProcessType> implements OaProcessTypeService {
    @Resource
    private OaProcessTemplateMapper processTemplateMapper;
    //查询所有审批分类和每个分类所有的审批模板
    @Override
    public List<ProcessType> findProcessType() {
        List<ProcessType> processTypes = baseMapper.selectList(null);
        List<Long> processTypeIds = processTypes.stream().map(ProcessType::getId).collect(Collectors.toList());
        LambdaQueryWrapper<ProcessTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(ProcessTemplate::getProcessTypeId, processTypeIds);
        List<ProcessTemplate> processTemplates = processTemplateMapper.selectList(wrapper);
        Map<Long, List<ProcessTemplate>>map = new HashMap<>();
        processTemplates.forEach(item -> {
            Long processTypeId = item.getProcessTypeId();
            if(!map.containsKey(item.getProcessTypeId())){
                map.put(processTypeId, new ArrayList<>());
            }
            map.get(processTypeId).add(item);
        });
        processTypes.forEach(processType -> {
            Long id = processType.getId();
            if(map.containsKey(processType.getId())){
                processType.setProcessTemplateList(map.get(id));
            }else{
                processType.setProcessTemplateList(new ArrayList<>());
            }
        });
        return processTypes;
    }
}
