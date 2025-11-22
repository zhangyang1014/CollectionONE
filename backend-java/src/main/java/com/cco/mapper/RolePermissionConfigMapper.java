package com.cco.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cco.model.entity.RolePermissionConfig;
import org.apache.ibatis.annotations.Mapper;

/**
 * 角色权限配置Mapper
 * 对应Python: app/models/permission.py - RolePermissionConfig
 */
@Mapper
public interface RolePermissionConfigMapper extends BaseMapper<RolePermissionConfig> {
}

