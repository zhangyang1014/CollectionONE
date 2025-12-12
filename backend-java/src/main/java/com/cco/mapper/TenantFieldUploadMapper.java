package com.cco.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cco.model.entity.TenantFieldUpload;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 甲方字段上传记录Mapper
 *
 * @author CCO Team
 * @since 2025-12-08
 */
@Mapper
public interface TenantFieldUploadMapper extends BaseMapper<TenantFieldUpload> {

    /**
     * 获取甲方+场景的最大版本号
     */
    @Select("SELECT COALESCE(MAX(version), 0) FROM tenant_field_uploads " +
            "WHERE tenant_id = #{tenantId} AND scene = #{scene}")
    Integer getMaxVersion(@Param("tenantId") String tenantId, @Param("scene") String scene);

    /**
     * 将甲方+场景的所有版本设为非激活状态
     */
    @Update("UPDATE tenant_field_uploads SET is_active = false " +
            "WHERE tenant_id = #{tenantId} AND scene = #{scene}")
    int deactivateAllVersions(@Param("tenantId") String tenantId, @Param("scene") String scene);
}



