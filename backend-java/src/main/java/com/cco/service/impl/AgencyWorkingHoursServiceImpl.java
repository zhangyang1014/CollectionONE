package com.cco.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cco.mapper.AgencyWorkingHoursMapper;
import com.cco.model.entity.AgencyWorkingHours;
import com.cco.service.AgencyWorkingHoursService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 机构作息时间服务实现
 * 
 * @author CCO Team
 * @since 2025-01-11
 */
@Slf4j
@Service
public class AgencyWorkingHoursServiceImpl extends ServiceImpl<AgencyWorkingHoursMapper, AgencyWorkingHours> implements AgencyWorkingHoursService {
    
    @Override
    public List<AgencyWorkingHours> getByAgencyId(Long agencyId) {
        // 先查询数据库中的作息时间
        LambdaQueryWrapper<AgencyWorkingHours> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AgencyWorkingHours::getAgencyId, agencyId);
        wrapper.orderByAsc(AgencyWorkingHours::getDayOfWeek);
        List<AgencyWorkingHours> existing = this.list(wrapper);
        
        // 转换为Map，key为day_of_week
        Map<Integer, AgencyWorkingHours> existingMap = existing.stream()
                .collect(Collectors.toMap(AgencyWorkingHours::getDayOfWeek, wh -> wh));
        
        // 返回7天的数据，如果某天没有配置，返回默认值（不工作）
        List<AgencyWorkingHours> result = new ArrayList<>();
        for (int day = 1; day <= 7; day++) {
            AgencyWorkingHours wh = existingMap.get(day);
            if (wh == null) {
                // 创建默认值（不工作）
                wh = new AgencyWorkingHours();
                wh.setAgencyId(agencyId);
                wh.setDayOfWeek(day);
                wh.setStartTime(null);
                wh.setEndTime(null);
                wh.setIsActive(false);
            }
            result.add(wh);
        }
        
        return result;
    }
    
    @Override
    @Transactional
    public List<AgencyWorkingHours> batchUpdate(Long agencyId, List<AgencyWorkingHours> workingHours) {
        // 删除旧的作息时间
        LambdaQueryWrapper<AgencyWorkingHours> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AgencyWorkingHours::getAgencyId, agencyId);
        this.remove(wrapper);
        
        // 创建新的作息时间
        if (workingHours != null && !workingHours.isEmpty()) {
            for (AgencyWorkingHours wh : workingHours) {
                wh.setAgencyId(agencyId);
                if (wh.getIsActive() == null) {
                    wh.setIsActive(true);
                }
            }
            this.saveBatch(workingHours);
        }
        
        // 返回更新后的数据
        return getByAgencyId(agencyId);
    }
    
    @Override
    public boolean isWorkingHours(Long agencyId, LocalDateTime datetime) {
        // 获取星期几（PRD要求：1-7，1=周一）
        int dayOfWeek = datetime.getDayOfWeek().getValue(); // Java的DayOfWeek：1=周一，7=周日
        
        // 查询该天的作息时间
        LambdaQueryWrapper<AgencyWorkingHours> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AgencyWorkingHours::getAgencyId, agencyId)
                .eq(AgencyWorkingHours::getDayOfWeek, dayOfWeek)
                .eq(AgencyWorkingHours::getIsActive, true);
        
        AgencyWorkingHours wh = this.getOne(wrapper);
        
        // 如果没有配置或未启用，返回false
        if (wh == null || wh.getStartTime() == null || wh.getEndTime() == null) {
            return false;
        }
        
        // 获取当前时间（只比较时分）
        LocalTime currentTime = datetime.toLocalTime();
        LocalTime startTime = wh.getStartTime();
        LocalTime endTime = wh.getEndTime();
        
        // 检查是否在时间范围内
        return !currentTime.isBefore(startTime) && currentTime.isBefore(endTime);
    }
}

