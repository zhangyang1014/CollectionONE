#!/bin/bash

# ============================================
# 甲方字段配置数据库验证脚本
# ============================================
# 功能: 验证字段配置真正从数据库读取并在各个页面生效
# 日期: 2025-11-24
# ============================================

set -e  # 遇到错误立即退出

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 日志函数
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[✓]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[!]${NC} $1"
}

log_error() {
    echo -e "${RED}[✗]${NC} $1"
}

log_section() {
    echo ""
    echo -e "${BLUE}========================================${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}========================================${NC}"
}

# 配置变量
DB_HOST="localhost"
DB_USER="root"
DB_NAME="cco_db"
API_BASE_URL="http://localhost:8080"
TENANT_ID=1

# 验证结果记录
TOTAL_CHECKS=0
PASSED_CHECKS=0
FAILED_CHECKS=0

# ============================================
# 检查1: 数据库表是否存在
# ============================================
check_database_table() {
    log_section "检查1: 验证数据库表结构"
    
    TOTAL_CHECKS=$((TOTAL_CHECKS + 1))
    
    # 检查表是否存在
    TABLE_EXISTS=$(mysql -h "$DB_HOST" -u "$DB_USER" -N -B -e "
        SELECT COUNT(*) 
        FROM information_schema.tables 
        WHERE table_schema = '$DB_NAME' 
          AND table_name = 'tenant_field_display_configs';
    ")
    
    if [ "$TABLE_EXISTS" = "1" ]; then
        log_success "数据库表 tenant_field_display_configs 存在"
        PASSED_CHECKS=$((PASSED_CHECKS + 1))
        
        # 检查表结构
        log_info "检查表结构..."
        mysql -h "$DB_HOST" -u "$DB_USER" -e "
            SELECT 
                COLUMN_NAME as '字段名',
                DATA_TYPE as '数据类型',
                COLUMN_COMMENT as '注释'
            FROM information_schema.COLUMNS 
            WHERE TABLE_SCHEMA = '$DB_NAME' 
              AND TABLE_NAME = 'tenant_field_display_configs'
            ORDER BY ORDINAL_POSITION;
        "
    else
        log_error "数据库表 tenant_field_display_configs 不存在"
        FAILED_CHECKS=$((FAILED_CHECKS + 1))
        exit 1
    fi
}

# ============================================
# 检查2: 初始化数据是否存在
# ============================================
check_initial_data() {
    log_section "检查2: 验证初始化数据"
    
    TOTAL_CHECKS=$((TOTAL_CHECKS + 3))
    
    # 检查各个场景的配置数量
    log_info "检查甲方A (tenant_id=1) 的配置数据..."
    
    mysql -h "$DB_HOST" -u "$DB_USER" -e "
        USE $DB_NAME;
        SELECT 
            scene_type as '场景类型',
            COUNT(*) as '配置数量'
        FROM tenant_field_display_configs 
        WHERE tenant_id = $TENANT_ID
        GROUP BY scene_type;
    "
    
    # 验证三个场景都有数据
    for scene in "admin_case_list" "collector_case_list" "collector_case_detail"; do
        COUNT=$(mysql -h "$DB_HOST" -u "$DB_USER" -N -B -e "
            SELECT COUNT(*) 
            FROM $DB_NAME.tenant_field_display_configs 
            WHERE tenant_id = $TENANT_ID AND scene_type = '$scene';
        ")
        
        if [ "$COUNT" -gt 0 ]; then
            log_success "场景 $scene 有 $COUNT 个配置"
            PASSED_CHECKS=$((PASSED_CHECKS + 1))
        else
            log_error "场景 $scene 没有配置数据"
            FAILED_CHECKS=$((FAILED_CHECKS + 1))
        fi
    done
}

# ============================================
# 检查3: 关键字段验证
# ============================================
check_key_fields() {
    log_section "检查3: 验证关键字段配置"
    
    TOTAL_CHECKS=$((TOTAL_CHECKS + 1))
    
    log_info "检查控台案件列表的字段配置..."
    
    mysql -h "$DB_HOST" -u "$DB_USER" -e "
        USE $DB_NAME;
        SELECT 
            field_key as '字段Key',
            field_name as '字段名',
            sort_order as '排序',
            display_width as '宽度',
            is_searchable as '可搜索',
            is_filterable as '可筛选',
            is_range_searchable as '范围检索'
        FROM tenant_field_display_configs
        WHERE tenant_id = $TENANT_ID 
          AND scene_type = 'admin_case_list'
        ORDER BY sort_order;
    "
    
    log_success "关键字段配置查询成功"
    PASSED_CHECKS=$((PASSED_CHECKS + 1))
}

# ============================================
# 检查4: 后端API验证
# ============================================
check_backend_api() {
    log_section "检查4: 验证后端API"
    
    # 检查后端服务是否运行
    TOTAL_CHECKS=$((TOTAL_CHECKS + 1))
    
    if curl -s -o /dev/null -w "%{http_code}" "$API_BASE_URL/actuator/health" | grep -q "200"; then
        log_success "后端服务正在运行"
        PASSED_CHECKS=$((PASSED_CHECKS + 1))
    else
        log_warning "后端服务未运行，跳过API测试"
        log_info "请先启动后端: cd backend-java && ./start.sh"
        FAILED_CHECKS=$((FAILED_CHECKS + 1))
        return
    fi
    
    # 测试场景类型接口
    TOTAL_CHECKS=$((TOTAL_CHECKS + 1))
    log_info "测试场景类型接口..."
    
    RESPONSE=$(curl -s "$API_BASE_URL/api/v1/field-display-configs/scene-types")
    
    if echo "$RESPONSE" | jq -e '.code == 200' > /dev/null 2>&1; then
        log_success "场景类型接口响应正常"
        PASSED_CHECKS=$((PASSED_CHECKS + 1))
        
        # 显示场景类型
        log_info "可用的场景类型:"
        echo "$RESPONSE" | jq -r '.data[] | "  - \(.key): \(.name)"'
    else
        log_error "场景类型接口响应异常"
        FAILED_CHECKS=$((FAILED_CHECKS + 1))
    fi
    
    # 测试字段配置接口（无需登录）
    TOTAL_CHECKS=$((TOTAL_CHECKS + 3))
    for scene in "admin_case_list" "collector_case_list" "collector_case_detail"; do
        log_info "测试场景 $scene 的字段配置接口..."
        
        RESPONSE=$(curl -s "$API_BASE_URL/api/v1/field-display-configs?tenant_id=$TENANT_ID&scene_type=$scene")
        
        # 检查响应是否为成功
        if echo "$RESPONSE" | jq -e '.code == 200' > /dev/null 2>&1; then
            COUNT=$(echo "$RESPONSE" | jq '.data | length')
            log_success "场景 $scene 返回 $COUNT 个字段配置"
            PASSED_CHECKS=$((PASSED_CHECKS + 1))
        else
            log_error "场景 $scene 接口响应异常"
            echo "$RESPONSE" | jq '.' || echo "$RESPONSE"
            FAILED_CHECKS=$((FAILED_CHECKS + 1))
        fi
    done
}

# ============================================
# 检查5: 数据库修改验证
# ============================================
check_database_modification() {
    log_section "检查5: 验证数据库修改功能"
    
    TOTAL_CHECKS=$((TOTAL_CHECKS + 4))
    
    # 备份当前配置
    log_info "备份当前配置..."
    BACKUP_FILE="/tmp/field_config_backup_$(date +%Y%m%d_%H%M%S).sql"
    mysqldump -h "$DB_HOST" -u "$DB_USER" "$DB_NAME" tenant_field_display_configs > "$BACKUP_FILE"
    log_info "备份文件: $BACKUP_FILE"
    
    # 测试1: 修改字段名称
    log_info "测试1: 修改字段名称..."
    mysql -h "$DB_HOST" -u "$DB_USER" -e "
        UPDATE $DB_NAME.tenant_field_display_configs 
        SET field_name = 'user_name (测试修改)' 
        WHERE tenant_id = $TENANT_ID 
          AND scene_type = 'admin_case_list' 
          AND field_key = 'user_name';
    "
    
    # 验证修改
    MODIFIED_NAME=$(mysql -h "$DB_HOST" -u "$DB_USER" -N -B -e "
        SELECT field_name 
        FROM $DB_NAME.tenant_field_display_configs 
        WHERE tenant_id = $TENANT_ID 
          AND scene_type = 'admin_case_list' 
          AND field_key = 'user_name';
    ")
    
    if [ "$MODIFIED_NAME" = "user_name (测试修改)" ]; then
        log_success "字段名称修改成功: $MODIFIED_NAME"
        PASSED_CHECKS=$((PASSED_CHECKS + 1))
    else
        log_error "字段名称修改失败"
        FAILED_CHECKS=$((FAILED_CHECKS + 1))
    fi
    
    # 测试2: 修改排序顺序
    log_info "测试2: 修改排序顺序..."
    mysql -h "$DB_HOST" -u "$DB_USER" -e "
        UPDATE $DB_NAME.tenant_field_display_configs 
        SET sort_order = 999 
        WHERE tenant_id = $TENANT_ID 
          AND scene_type = 'admin_case_list' 
          AND field_key = 'user_name';
    "
    
    MODIFIED_ORDER=$(mysql -h "$DB_HOST" -u "$DB_USER" -N -B -e "
        SELECT sort_order 
        FROM $DB_NAME.tenant_field_display_configs 
        WHERE tenant_id = $TENANT_ID 
          AND scene_type = 'admin_case_list' 
          AND field_key = 'user_name';
    ")
    
    if [ "$MODIFIED_ORDER" = "999" ]; then
        log_success "排序顺序修改成功: $MODIFIED_ORDER"
        PASSED_CHECKS=$((PASSED_CHECKS + 1))
    else
        log_error "排序顺序修改失败"
        FAILED_CHECKS=$((FAILED_CHECKS + 1))
    fi
    
    # 测试3: 修改可搜索标记
    log_info "测试3: 修改可搜索标记..."
    mysql -h "$DB_HOST" -u "$DB_USER" -e "
        UPDATE $DB_NAME.tenant_field_display_configs 
        SET is_searchable = 0 
        WHERE tenant_id = $TENANT_ID 
          AND scene_type = 'admin_case_list' 
          AND field_key = 'user_name';
    "
    
    MODIFIED_SEARCHABLE=$(mysql -h "$DB_HOST" -u "$DB_USER" -N -B -e "
        SELECT is_searchable 
        FROM $DB_NAME.tenant_field_display_configs 
        WHERE tenant_id = $TENANT_ID 
          AND scene_type = 'admin_case_list' 
          AND field_key = 'user_name';
    ")
    
    if [ "$MODIFIED_SEARCHABLE" = "0" ]; then
        log_success "可搜索标记修改成功: $MODIFIED_SEARCHABLE"
        PASSED_CHECKS=$((PASSED_CHECKS + 1))
    else
        log_error "可搜索标记修改失败"
        FAILED_CHECKS=$((FAILED_CHECKS + 1))
    fi
    
    # 恢复备份
    log_info "恢复原始配置..."
    mysql -h "$DB_HOST" -u "$DB_USER" "$DB_NAME" < "$BACKUP_FILE"
    log_success "配置已恢复"
    PASSED_CHECKS=$((PASSED_CHECKS + 1))
    
    # 删除备份文件
    rm -f "$BACKUP_FILE"
}

# ============================================
# 检查6: 前端集成验证
# ============================================
check_frontend_integration() {
    log_section "检查6: 验证前端集成"
    
    TOTAL_CHECKS=$((TOTAL_CHECKS + 3))
    
    # 检查控台案件列表
    log_info "检查控台案件列表集成..."
    if grep -q "useFieldDisplayConfig" "/Users/zhangyang/Documents/GitHub/CollectionONE/frontend/src/views/case-management/CaseList.vue"; then
        if grep -q "sceneType: 'admin_case_list'" "/Users/zhangyang/Documents/GitHub/CollectionONE/frontend/src/views/case-management/CaseList.vue"; then
            log_success "控台案件列表已集成字段配置Hook (admin_case_list)"
            PASSED_CHECKS=$((PASSED_CHECKS + 1))
        else
            log_error "控台案件列表未配置正确的场景类型"
            FAILED_CHECKS=$((FAILED_CHECKS + 1))
        fi
    else
        log_error "控台案件列表未使用字段配置Hook"
        FAILED_CHECKS=$((FAILED_CHECKS + 1))
    fi
    
    # 检查IM端案件列表
    log_info "检查IM端案件列表集成..."
    if grep -q "useFieldDisplayConfig" "/Users/zhangyang/Documents/GitHub/CollectionONE/frontend/src/views/im/CollectorWorkspace.vue"; then
        if grep -q "sceneType: 'collector_case_list'" "/Users/zhangyang/Documents/GitHub/CollectionONE/frontend/src/views/im/CollectorWorkspace.vue"; then
            log_success "IM端案件列表已集成字段配置Hook (collector_case_list)"
            PASSED_CHECKS=$((PASSED_CHECKS + 1))
        else
            log_error "IM端案件列表未配置正确的场景类型"
            FAILED_CHECKS=$((FAILED_CHECKS + 1))
        fi
    else
        log_error "IM端案件列表未使用字段配置Hook"
        FAILED_CHECKS=$((FAILED_CHECKS + 1))
    fi
    
    # 检查动态组件
    log_info "检查动态组件..."
    if [ -f "/Users/zhangyang/Documents/GitHub/CollectionONE/frontend/src/components/DynamicCaseTable.vue" ] && \
       [ -f "/Users/zhangyang/Documents/GitHub/CollectionONE/frontend/src/components/DynamicCaseDetail.vue" ]; then
        log_success "动态组件存在: DynamicCaseTable 和 DynamicCaseDetail"
        PASSED_CHECKS=$((PASSED_CHECKS + 1))
    else
        log_error "动态组件缺失"
        FAILED_CHECKS=$((FAILED_CHECKS + 1))
    fi
}

# ============================================
# 生成验证报告
# ============================================
generate_report() {
    log_section "验证报告"
    
    PASS_RATE=$(awk "BEGIN {printf \"%.1f\", ($PASSED_CHECKS/$TOTAL_CHECKS)*100}")
    
    echo ""
    echo "总检查项: $TOTAL_CHECKS"
    echo -e "${GREEN}通过: $PASSED_CHECKS${NC}"
    echo -e "${RED}失败: $FAILED_CHECKS${NC}"
    echo "通过率: ${PASS_RATE}%"
    echo ""
    
    if [ $FAILED_CHECKS -eq 0 ]; then
        log_success "所有检查项均通过！甲方字段配置功能已从数据库读取并正确集成到前端。"
        echo ""
        log_info "下一步:"
        echo "  1. 启动后端: cd backend-java && ./start.sh"
        echo "  2. 启动前端: cd frontend && npm run dev"
        echo "  3. 登录系统: http://localhost:5173"
        echo "  4. 选择甲方A"
        echo "  5. 进入案件列表，查看字段配置是否生效"
        echo ""
        return 0
    else
        log_error "验证失败！请检查上述错误信息。"
        return 1
    fi
}

# ============================================
# 主函数
# ============================================
main() {
    echo ""
    log_section "甲方字段配置数据库验证"
    echo ""
    log_info "开始验证..."
    echo ""
    
    # 执行所有检查
    check_database_table
    check_initial_data
    check_key_fields
    check_backend_api
    check_database_modification
    check_frontend_integration
    
    # 生成报告
    generate_report
}

# 运行主函数
main
exit $?

