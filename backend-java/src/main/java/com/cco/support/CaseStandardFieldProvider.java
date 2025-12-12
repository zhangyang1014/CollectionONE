package com.cco.support;

import com.cco.model.dto.CaseStandardFieldVO;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 案件标准字段定义（代码内统一管理，只读）。
 */
public class CaseStandardFieldProvider {

    private static final List<CaseStandardFieldVO> CASE_LIST_FIELDS;
    private static final List<CaseStandardFieldVO> CASE_DETAIL_FIELDS;

    static {
        CASE_LIST_FIELDS = buildCaseListFields();
        CASE_DETAIL_FIELDS = buildCaseDetailFields();
    }

    /**
     * 案件列表标准字段（管理端）。
     */
    public static List<CaseStandardFieldVO> getCaseListFields() {
        return Collections.unmodifiableList(CASE_LIST_FIELDS);
    }

    /**
     * 案件详情标准字段（催员端）。
     */
    public static List<CaseStandardFieldVO> getCaseDetailFields() {
        return Collections.unmodifiableList(CASE_DETAIL_FIELDS);
    }

    private static List<CaseStandardFieldVO> buildCaseListFields() {
        List<CaseStandardFieldVO> list = new ArrayList<>();
        // 案件列表标准字段定义
        list.add(CaseStandardFieldVO.builder()
                .id(1L).sceneType("admin_case_list").sceneName("控台案件管理列表")
                .fieldKey("case_code").fieldName("案件编号").fieldDataType("String")
                .fieldSource("standard").sortOrder(1).displayWidth(140)
                .required(true).searchable(false).filterable(false).rangeSearchable(false)
                .colorType("normal").description("系统唯一案件编号")
                .build());
        // 新增：用户id（必填，放在客户字段前）
        list.add(CaseStandardFieldVO.builder()
                .id(2L).sceneType("admin_case_list").sceneName("控台案件管理列表")
                .fieldKey("user_id").fieldName("用户id").fieldDataType("String")
                .fieldSource("standard").sortOrder(2).displayWidth(120)
                .required(true).searchable(false).filterable(false).rangeSearchable(false)
                .colorType("normal").description("系统内部唯一用户标识")
                .build());
        list.add(CaseStandardFieldVO.builder()
                .id(3L).sceneType("admin_case_list").sceneName("控台案件管理列表")
                .fieldKey("user_name").fieldName("客户").fieldDataType("String")
                .fieldSource("standard").sortOrder(3).displayWidth(140)
                .required(true).searchable(false).filterable(false).rangeSearchable(false)
                .colorType("normal").description("客户姓名")
                .build());
        list.add(CaseStandardFieldVO.builder()
                .id(4L).sceneType("admin_case_list").sceneName("控台案件管理列表")
                .fieldKey("mobile_number").fieldName("手机号").fieldDataType("String")
                .fieldSource("standard").sortOrder(4).displayWidth(150)
                .required(true).searchable(false).filterable(false).rangeSearchable(false)
                .colorType("normal").description("客户手机号")
                .build());
        // 新增：首复借类型（选填，放在贷款金额前）
        list.add(CaseStandardFieldVO.builder()
                .id(5L).sceneType("admin_case_list").sceneName("控台案件管理列表")
                .fieldKey("collection_type").fieldName("首复借类型").fieldDataType("Enum")
                .fieldSource("standard").sortOrder(5).displayWidth(120)
                .required(false).searchable(false).filterable(false).rangeSearchable(false)
                .colorType("normal").description("标记案件是否为首次或再次催收")
                .build());
        list.add(CaseStandardFieldVO.builder()
                .id(6L).sceneType("admin_case_list").sceneName("控台案件管理列表")
                .fieldKey("loan_amount").fieldName("贷款金额").fieldDataType("Decimal")
                .fieldSource("standard").sortOrder(6).displayWidth(120)
                .required(true).searchable(false).filterable(false).rangeSearchable(false)
                .colorType("normal").description("放款金额")
                .build());
        list.add(CaseStandardFieldVO.builder()
                .id(7L).sceneType("admin_case_list").sceneName("控台案件管理列表")
                .fieldKey("outstanding_amount").fieldName("未还金额").fieldDataType("Decimal")
                .fieldSource("standard").sortOrder(7).displayWidth(120)
                .required(true).searchable(false).filterable(false).rangeSearchable(false)
                .colorType("normal").description("当前未还金额")
                .build());
        // 新增：减免金额（选填，放在未还金额后面）
        list.add(CaseStandardFieldVO.builder()
                .id(8L).sceneType("admin_case_list").sceneName("控台案件管理列表")
                .fieldKey("waived_amount").fieldName("减免金额").fieldDataType("Decimal")
                .fieldSource("standard").sortOrder(8).displayWidth(120)
                .required(false).searchable(false).filterable(false).rangeSearchable(false)
                .colorType("normal").description("费用减免金额")
                .build());
        list.add(CaseStandardFieldVO.builder()
                .id(9L).sceneType("admin_case_list").sceneName("控台案件管理列表")
                .fieldKey("overdue_days").fieldName("逾期天数").fieldDataType("Integer")
                .fieldSource("standard").sortOrder(9).displayWidth(100)
                .required(true).searchable(false).filterable(false).rangeSearchable(false)
                .colorType("normal").description("当前逾期天数")
                .build());
        list.add(CaseStandardFieldVO.builder()
                .id(10L).sceneType("admin_case_list").sceneName("控台案件管理列表")
                .fieldKey("case_status").fieldName("案件状态").fieldDataType("Enum")
                .fieldSource("standard").sortOrder(10).displayWidth(110)
                .required(true).searchable(false).filterable(false).rangeSearchable(false)
                .colorType("normal").description("案件状态")
                .build());
        // 新增：逾期期数（必填，放在案件状态后面）
        list.add(CaseStandardFieldVO.builder()
                .id(11L).sceneType("admin_case_list").sceneName("控台案件管理列表")
                .fieldKey("overdue_installments").fieldName("逾期期数").fieldDataType("Integer")
                .fieldSource("standard").sortOrder(11).displayWidth(100)
                .required(true).searchable(false).filterable(false).rangeSearchable(false)
                .colorType("normal").description("当前逾期期数")
                .build());
        list.add(CaseStandardFieldVO.builder()
                .id(12L).sceneType("admin_case_list").sceneName("控台案件管理列表")
                .fieldKey("expected_start_date").fieldName("预期开始日期").fieldDataType("Date")
                .fieldSource("standard").sortOrder(12).displayWidth(120)
                .required(true).searchable(false).filterable(false).rangeSearchable(false)
                .colorType("normal").description("预期开始还款日期")
                .build());
        list.add(CaseStandardFieldVO.builder()
                .id(13L).sceneType("admin_case_list").sceneName("控台案件管理列表")
                .fieldKey("total_installments").fieldName("总期数").fieldDataType("Integer")
                .fieldSource("standard").sortOrder(13).displayWidth(100)
                .required(true).searchable(false).filterable(false).rangeSearchable(false)
                .colorType("normal").description("贷款总期数")
                .build());
        list.add(CaseStandardFieldVO.builder()
                .id(14L).sceneType("admin_case_list").sceneName("控台案件管理列表")
                .fieldKey("system_name").fieldName("所属系统").fieldDataType("String")
                .fieldSource("standard").sortOrder(14).displayWidth(120)
                .required(true).searchable(false).filterable(false).rangeSearchable(false)
                .colorType("normal").description("来源系统")
                .build());
        list.add(CaseStandardFieldVO.builder()
                .id(15L).sceneType("admin_case_list").sceneName("控台案件管理列表")
                .fieldKey("product_name").fieldName("产品").fieldDataType("String")
                .fieldSource("standard").sortOrder(15).displayWidth(130)
                .required(true).searchable(false).filterable(false).rangeSearchable(false)
                .colorType("normal").description("产品名称")
                .build());
        list.add(CaseStandardFieldVO.builder()
                .id(16L).sceneType("admin_case_list").sceneName("控台案件管理列表")
                .fieldKey("app_name").fieldName("APP").fieldDataType("String")
                .fieldSource("standard").sortOrder(16).displayWidth(120)
                .required(true).searchable(false).filterable(false).rangeSearchable(false)
                .colorType("normal").description("应用名称")
                .build());
        list.add(CaseStandardFieldVO.builder()
                .id(17L).sceneType("admin_case_list").sceneName("控台案件管理列表")
                .fieldKey("merchant_name").fieldName("商户").fieldDataType("String")
                .fieldSource("standard").sortOrder(17).displayWidth(120)
                .required(true).searchable(false).filterable(false).rangeSearchable(false)
                .colorType("normal").description("合作商户")
                .build());
        list.add(CaseStandardFieldVO.builder()
                .id(18L).sceneType("admin_case_list").sceneName("控台案件管理列表")
                .fieldKey("loan_id").fieldName("关联借款ID").fieldDataType("String")
                .fieldSource("standard").sortOrder(18).displayWidth(150)
                .required(true).searchable(false).filterable(false).rangeSearchable(false)
                .colorType("normal").description("用于与借款主数据关联")
                .build());
        // 新增：案件类型1-5（枚举类型，选填）
        list.add(CaseStandardFieldVO.builder()
                .id(19L).sceneType("admin_case_list").sceneName("控台案件管理列表")
                .fieldKey("case_type_1").fieldName("案件类型1").fieldDataType("Enum")
                .fieldSource("standard").sortOrder(19).displayWidth(120)
                .required(false).searchable(false).filterable(false).rangeSearchable(false)
                .colorType("normal").description("案件分类类型1")
                .build());
        list.add(CaseStandardFieldVO.builder()
                .id(20L).sceneType("admin_case_list").sceneName("控台案件管理列表")
                .fieldKey("case_type_2").fieldName("案件类型2").fieldDataType("Enum")
                .fieldSource("standard").sortOrder(20).displayWidth(120)
                .required(false).searchable(false).filterable(false).rangeSearchable(false)
                .colorType("normal").description("案件分类类型2")
                .build());
        list.add(CaseStandardFieldVO.builder()
                .id(21L).sceneType("admin_case_list").sceneName("控台案件管理列表")
                .fieldKey("case_type_3").fieldName("案件类型3").fieldDataType("Enum")
                .fieldSource("standard").sortOrder(21).displayWidth(120)
                .required(false).searchable(false).filterable(false).rangeSearchable(false)
                .colorType("normal").description("案件分类类型3")
                .build());
        list.add(CaseStandardFieldVO.builder()
                .id(22L).sceneType("admin_case_list").sceneName("控台案件管理列表")
                .fieldKey("case_type_4").fieldName("案件类型4").fieldDataType("Enum")
                .fieldSource("standard").sortOrder(22).displayWidth(120)
                .required(false).searchable(false).filterable(false).rangeSearchable(false)
                .colorType("normal").description("案件分类类型4")
                .build());
        list.add(CaseStandardFieldVO.builder()
                .id(23L).sceneType("admin_case_list").sceneName("控台案件管理列表")
                .fieldKey("case_type_5").fieldName("案件类型5").fieldDataType("Enum")
                .fieldSource("standard").sortOrder(23).displayWidth(120)
                .required(false).searchable(false).filterable(false).rangeSearchable(false)
                .colorType("normal").description("案件分类类型5")
                .build());
        return list;
    }

    private static List<CaseStandardFieldVO> buildCaseDetailFields() {
        List<CaseStandardFieldVO> list = new ArrayList<>();
        // 详情页也同步展示案件列表的全量标准字段，字段Key前加 detail_ 前缀，全部必填只读
        String[] keys = {
                "case_code", "user_id", "user_name", "mobile_number", "collection_type", 
                "loan_amount", "outstanding_amount", "waived_amount", "overdue_days", 
                "overdue_installments", "case_status", "expected_start_date", "total_installments", 
                "system_name", "product_name", "app_name", "merchant_name", "loan_id",
                "case_type_1", "case_type_2", "case_type_3", "case_type_4", "case_type_5"
        };
        String[] names = {
                "案件编号", "用户id", "客户", "手机号", "首复借类型",
                "贷款金额", "未还金额", "减免金额", "逾期天数", "逾期期数",
                "案件状态", "预期开始日期", "总期数", "所属系统", "产品",
                "APP", "商户", "关联借款ID", "案件类型1", "案件类型2",
                "案件类型3", "案件类型4", "案件类型5"
        };
        String[] types = {
                "String", "String", "String", "String", "Enum",
                "Decimal", "Decimal", "Decimal", "Integer", "Integer",
                "Enum", "Date", "Integer", "String", "String",
                "String", "String", "String", "Enum", "Enum",
                "Enum", "Enum", "Enum"
        };

        for (int i = 0; i < keys.length; i++) {
            list.add(CaseStandardFieldVO.builder()
                    .id((long) (i + 1))
                    .sceneType("collector_case_detail")
                    .sceneName("案件详情")
                    .fieldKey("detail_" + keys[i])
                    .fieldName(names[i])
                    .fieldDataType(types[i])
                    .fieldSource("standard")
                    .sortOrder(i + 1)
                    .displayWidth(0)
                    .required(true)
                    .searchable(false)
                    .filterable(false)
                    .rangeSearchable(false)
                    .colorType("normal")
                    .description(names[i])
                    .build());
        }
        return list;
    }
}
