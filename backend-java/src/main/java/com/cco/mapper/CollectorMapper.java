package com.cco.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cco.model.dto.CollectorListDTO;
import com.cco.model.entity.Collector;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 催员Mapper
 */
public interface CollectorMapper extends BaseMapper<Collector> {
    
    /**
     * 获取催员列表（用于分案弹窗）
     * 包含机构名称、小组名称、队列信息、今日持案量
     */
    List<CollectorListDTO> selectCollectorListForAssign(
            @Param("agencyId") Long agencyId,
            @Param("teamId") Long teamId,
            @Param("queueId") Long queueId,
            @Param("searchKeyword") String searchKeyword
    );
}
























