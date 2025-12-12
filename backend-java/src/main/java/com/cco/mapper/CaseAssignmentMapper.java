package com.cco.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cco.model.entity.CaseAssignment;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 案件分配记录Mapper
 */
public interface CaseAssignmentMapper extends BaseMapper<CaseAssignment> {
    
    /**
     * 批量插入分配记录
     */
    int batchInsert(@Param("list") List<CaseAssignment> assignments);
}


























