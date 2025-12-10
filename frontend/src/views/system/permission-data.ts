/**
 * 权限数据定义
 * 基于权限管理文档.md中的权限矩阵
 */

export interface PermissionItem {
  name: string
  description?: string
  permissions: {
    [roleCode: string]: 'yes' | 'no' | 'limited'
  }
}

export interface PermissionModule {
  [key: string]: PermissionItem[]
}

const permissionData: PermissionModule = {
  // 系统管理权限
  system: [
    {
      name: '系统配置管理',
      description: '管理系统全局配置参数',
      permissions: {
        SUPER_ADMIN: 'yes',
        TENANT_ADMIN: 'no',
        AGENCY_ADMIN: 'no',
        TEAM_LEADER: 'no',
        QUALITY_INSPECTOR: 'no',
        DATA_SOURCE: 'no',
        COLLECTOR: 'no'
      }
    },
    {
      name: '用户管理',
      description: '管理系统用户账号',
      permissions: {
        SUPER_ADMIN: 'yes',
        TENANT_ADMIN: 'no',
        AGENCY_ADMIN: 'no',
        TEAM_LEADER: 'no',
        QUALITY_INSPECTOR: 'no',
        DATA_SOURCE: 'no',
        COLLECTOR: 'no'
      }
    },
    {
      name: '角色管理',
      description: '管理系统角色和权限配置',
      permissions: {
        SUPER_ADMIN: 'yes',
        TENANT_ADMIN: 'no',
        AGENCY_ADMIN: 'no',
        TEAM_LEADER: 'no',
        QUALITY_INSPECTOR: 'no',
        DATA_SOURCE: 'no',
        COLLECTOR: 'no'
      }
    },
    {
      name: '日志查看',
      description: '查看系统操作日志和审计日志',
      permissions: {
        SUPER_ADMIN: 'yes',
        TENANT_ADMIN: 'yes',
        AGENCY_ADMIN: 'no',
        TEAM_LEADER: 'no',
        QUALITY_INSPECTOR: 'no',
        DATA_SOURCE: 'no',
        COLLECTOR: 'no'
      }
    },
    {
      name: '数据备份与恢复',
      description: '执行数据备份和恢复操作',
      permissions: {
        SUPER_ADMIN: 'yes',
        TENANT_ADMIN: 'no',
        AGENCY_ADMIN: 'no',
        TEAM_LEADER: 'no',
        QUALITY_INSPECTOR: 'no',
        DATA_SOURCE: 'no',
        COLLECTOR: 'no'
      }
    }
  ],

  // 甲方管理权限
  tenant: [
    {
      name: '查看甲方列表',
      description: '查看所有甲方信息',
      permissions: {
        SUPER_ADMIN: 'yes',
        TENANT_ADMIN: 'limited',
        AGENCY_ADMIN: 'no',
        TEAM_LEADER: 'no',
        QUALITY_INSPECTOR: 'no',
        DATA_SOURCE: 'no',
        COLLECTOR: 'no'
      }
    },
    {
      name: '创建甲方',
      description: '创建新的甲方账号',
      permissions: {
        SUPER_ADMIN: 'yes',
        TENANT_ADMIN: 'no',
        AGENCY_ADMIN: 'no',
        TEAM_LEADER: 'no',
        QUALITY_INSPECTOR: 'no',
        DATA_SOURCE: 'no',
        COLLECTOR: 'no'
      }
    },
    {
      name: '编辑甲方',
      description: '修改甲方配置信息',
      permissions: {
        SUPER_ADMIN: 'yes',
        TENANT_ADMIN: 'limited',
        AGENCY_ADMIN: 'no',
        TEAM_LEADER: 'no',
        QUALITY_INSPECTOR: 'no',
        DATA_SOURCE: 'no',
        COLLECTOR: 'no'
      }
    },
    {
      name: '删除甲方',
      description: '删除甲方账号',
      permissions: {
        SUPER_ADMIN: 'yes',
        TENANT_ADMIN: 'no',
        AGENCY_ADMIN: 'no',
        TEAM_LEADER: 'no',
        QUALITY_INSPECTOR: 'no',
        DATA_SOURCE: 'no',
        COLLECTOR: 'no'
      }
    },
    {
      name: '切换甲方',
      description: '在不同甲方之间切换',
      permissions: {
        SUPER_ADMIN: 'yes',
        TENANT_ADMIN: 'no',
        AGENCY_ADMIN: 'no',
        TEAM_LEADER: 'no',
        QUALITY_INSPECTOR: 'no',
        DATA_SOURCE: 'no',
        COLLECTOR: 'no'
      }
    },
    {
      name: '甲方字段配置',
      description: '配置甲方自定义字段',
      permissions: {
        SUPER_ADMIN: 'yes',
        TENANT_ADMIN: 'yes',
        AGENCY_ADMIN: 'no',
        TEAM_LEADER: 'no',
        QUALITY_INSPECTOR: 'no',
        DATA_SOURCE: 'no',
        COLLECTOR: 'no'
      }
    },
    {
      name: '甲方触达渠道管理',
      description: '管理甲方的渠道供应商配置',
      permissions: {
        SUPER_ADMIN: 'yes',
        TENANT_ADMIN: 'yes',
        AGENCY_ADMIN: 'no',
        TEAM_LEADER: 'no',
        QUALITY_INSPECTOR: 'no',
        DATA_SOURCE: 'no',
        COLLECTOR: 'no'
      }
    },
    {
      name: '甲方队列管理',
      description: '管理甲方的案件队列配置',
      permissions: {
        SUPER_ADMIN: 'yes',
        TENANT_ADMIN: 'yes',
        AGENCY_ADMIN: 'no',
        TEAM_LEADER: 'no',
        QUALITY_INSPECTOR: 'no',
        DATA_SOURCE: 'no',
        COLLECTOR: 'no'
      }
    }
  ],

  // 机构管理权限
  agency: [
    {
      name: '查看机构列表',
      description: '查看机构信息列表',
      permissions: {
        SUPER_ADMIN: 'yes',
        TENANT_ADMIN: 'yes',
        AGENCY_ADMIN: 'limited',
        TEAM_LEADER: 'no',
        QUALITY_INSPECTOR: 'no',
        DATA_SOURCE: 'no',
        COLLECTOR: 'no'
      }
    },
    {
      name: '创建机构',
      description: '创建新的机构',
      permissions: {
        SUPER_ADMIN: 'yes',
        TENANT_ADMIN: 'yes',
        AGENCY_ADMIN: 'no',
        TEAM_LEADER: 'no',
        QUALITY_INSPECTOR: 'no',
        DATA_SOURCE: 'no',
        COLLECTOR: 'no'
      }
    },
    {
      name: '编辑机构',
      description: '修改机构信息',
      permissions: {
        SUPER_ADMIN: 'yes',
        TENANT_ADMIN: 'yes',
        AGENCY_ADMIN: 'limited',
        TEAM_LEADER: 'no',
        QUALITY_INSPECTOR: 'no',
        DATA_SOURCE: 'no',
        COLLECTOR: 'no'
      }
    },
    {
      name: '删除机构',
      description: '删除机构',
      permissions: {
        SUPER_ADMIN: 'yes',
        TENANT_ADMIN: 'yes',
        AGENCY_ADMIN: 'no',
        TEAM_LEADER: 'no',
        QUALITY_INSPECTOR: 'no',
        DATA_SOURCE: 'no',
        COLLECTOR: 'no'
      }
    },
    {
      name: '机构时区配置',
      description: '配置机构时区',
      permissions: {
        SUPER_ADMIN: 'yes',
        TENANT_ADMIN: 'yes',
        AGENCY_ADMIN: 'limited',
        TEAM_LEADER: 'no',
        QUALITY_INSPECTOR: 'no',
        DATA_SOURCE: 'no',
        COLLECTOR: 'no'
      }
    },
    {
      name: '机构作息时间管理',
      description: '配置机构工作时间和作息安排',
      permissions: {
        SUPER_ADMIN: 'yes',
        TENANT_ADMIN: 'yes',
        AGENCY_ADMIN: 'limited',
        TEAM_LEADER: 'no',
        QUALITY_INSPECTOR: 'no',
        DATA_SOURCE: 'no',
        COLLECTOR: 'no'
      }
    },
    {
      name: '机构管理员管理',
      description: '管理机构管理员账号',
      permissions: {
        SUPER_ADMIN: 'yes',
        TENANT_ADMIN: 'yes',
        AGENCY_ADMIN: 'limited',
        TEAM_LEADER: 'no',
        QUALITY_INSPECTOR: 'no',
        DATA_SOURCE: 'no',
        COLLECTOR: 'no'
      }
    }
  ],

  // 小组管理权限
  team: [
    {
      name: '查看小组列表',
      description: '查看小组信息列表',
      permissions: {
        SUPER_ADMIN: 'yes',
        TENANT_ADMIN: 'yes',
        AGENCY_ADMIN: 'yes',
        TEAM_LEADER: 'limited',
        QUALITY_INSPECTOR: 'no',
        DATA_SOURCE: 'no',
        COLLECTOR: 'no'
      }
    },
    {
      name: '创建小组',
      description: '创建新的小组',
      permissions: {
        SUPER_ADMIN: 'yes',
        TENANT_ADMIN: 'yes',
        AGENCY_ADMIN: 'yes',
        TEAM_LEADER: 'no',
        QUALITY_INSPECTOR: 'no',
        DATA_SOURCE: 'no',
        COLLECTOR: 'no'
      }
    },
    {
      name: '编辑小组',
      description: '修改小组信息',
      permissions: {
        SUPER_ADMIN: 'yes',
        TENANT_ADMIN: 'yes',
        AGENCY_ADMIN: 'yes',
        TEAM_LEADER: 'no',
        QUALITY_INSPECTOR: 'no',
        DATA_SOURCE: 'no',
        COLLECTOR: 'no'
      }
    },
    {
      name: '删除小组',
      description: '删除小组',
      permissions: {
        SUPER_ADMIN: 'yes',
        TENANT_ADMIN: 'yes',
        AGENCY_ADMIN: 'yes',
        TEAM_LEADER: 'no',
        QUALITY_INSPECTOR: 'no',
        DATA_SOURCE: 'no',
        COLLECTOR: 'no'
      }
    },
    {
      name: '小组管理员管理',
      description: '管理小组管理员账号',
      permissions: {
        SUPER_ADMIN: 'yes',
        TENANT_ADMIN: 'yes',
        AGENCY_ADMIN: 'yes',
        TEAM_LEADER: 'no',
        QUALITY_INSPECTOR: 'no',
        DATA_SOURCE: 'no',
        COLLECTOR: 'no'
      }
    },
    {
      name: '添加组员',
      description: '向小组添加催员',
      permissions: {
        SUPER_ADMIN: 'yes',
        TENANT_ADMIN: 'yes',
        AGENCY_ADMIN: 'yes',
        TEAM_LEADER: 'no',
        QUALITY_INSPECTOR: 'no',
        DATA_SOURCE: 'no',
        COLLECTOR: 'no'
      }
    },
    {
      name: '移除组员',
      description: '从小组移除催员',
      permissions: {
        SUPER_ADMIN: 'yes',
        TENANT_ADMIN: 'yes',
        AGENCY_ADMIN: 'yes',
        TEAM_LEADER: 'no',
        QUALITY_INSPECTOR: 'no',
        DATA_SOURCE: 'no',
        COLLECTOR: 'no'
      }
    }
  ],

  // 催员管理权限
  collector: [
    {
      name: '查看催员列表',
      description: '查看催员信息列表',
      permissions: {
        SUPER_ADMIN: 'yes',
        TENANT_ADMIN: 'yes',
        AGENCY_ADMIN: 'yes',
        TEAM_LEADER: 'limited',
        QUALITY_INSPECTOR: 'no',
        DATA_SOURCE: 'no',
        COLLECTOR: 'no'
      }
    },
    {
      name: '创建催员',
      description: '创建新的催员账号',
      permissions: {
        SUPER_ADMIN: 'yes',
        TENANT_ADMIN: 'yes',
        AGENCY_ADMIN: 'yes',
        TEAM_LEADER: 'no',
        QUALITY_INSPECTOR: 'no',
        DATA_SOURCE: 'no',
        COLLECTOR: 'no'
      }
    },
    {
      name: '编辑催员',
      description: '修改催员信息',
      permissions: {
        SUPER_ADMIN: 'yes',
        TENANT_ADMIN: 'yes',
        AGENCY_ADMIN: 'yes',
        TEAM_LEADER: 'no',
        QUALITY_INSPECTOR: 'no',
        DATA_SOURCE: 'no',
        COLLECTOR: 'no'
      }
    },
    {
      name: '删除催员',
      description: '删除催员账号',
      permissions: {
        SUPER_ADMIN: 'yes',
        TENANT_ADMIN: 'yes',
        AGENCY_ADMIN: 'yes',
        TEAM_LEADER: 'no',
        QUALITY_INSPECTOR: 'no',
        DATA_SOURCE: 'no',
        COLLECTOR: 'no'
      }
    },
    {
      name: '重置催员密码',
      description: '重置催员登录密码',
      permissions: {
        SUPER_ADMIN: 'yes',
        TENANT_ADMIN: 'yes',
        AGENCY_ADMIN: 'yes',
        TEAM_LEADER: 'no',
        QUALITY_INSPECTOR: 'no',
        DATA_SOURCE: 'no',
        COLLECTOR: 'no'
      }
    },
    {
      name: '导出催员账号密码',
      description: '导出催员账号和密码信息',
      permissions: {
        SUPER_ADMIN: 'yes',
        TENANT_ADMIN: 'yes',
        AGENCY_ADMIN: 'yes',
        TEAM_LEADER: 'no',
        QUALITY_INSPECTOR: 'no',
        DATA_SOURCE: 'no',
        COLLECTOR: 'no'
      }
    },
    {
      name: '催员状态管理',
      description: '启用/禁用催员账号',
      permissions: {
        SUPER_ADMIN: 'yes',
        TENANT_ADMIN: 'yes',
        AGENCY_ADMIN: 'yes',
        TEAM_LEADER: 'no',
        QUALITY_INSPECTOR: 'no',
        DATA_SOURCE: 'no',
        COLLECTOR: 'no'
      }
    }
  ],

  // 案件管理权限
  case: [
    {
      name: '查看全部案件',
      description: '查看所有案件信息',
      permissions: {
        SUPER_ADMIN: 'yes',
        TENANT_ADMIN: 'yes',
        AGENCY_ADMIN: 'no',
        TEAM_LEADER: 'no',
        QUALITY_INSPECTOR: 'no',
        DATA_SOURCE: 'no',
        COLLECTOR: 'no'
      }
    },
    {
      name: '查看机构案件',
      description: '查看机构下所有案件',
      permissions: {
        SUPER_ADMIN: 'yes',
        TENANT_ADMIN: 'yes',
        AGENCY_ADMIN: 'yes',
        TEAM_LEADER: 'no',
        QUALITY_INSPECTOR: 'no',
        DATA_SOURCE: 'no',
        COLLECTOR: 'no'
      }
    },
    {
      name: '查看小组案件',
      description: '查看小组下所有案件',
      permissions: {
        SUPER_ADMIN: 'yes',
        TENANT_ADMIN: 'yes',
        AGENCY_ADMIN: 'yes',
        TEAM_LEADER: 'yes',
        QUALITY_INSPECTOR: 'no',
        DATA_SOURCE: 'no',
        COLLECTOR: 'no'
      }
    },
    {
      name: '查看个人案件',
      description: '查看分配给自己的案件',
      permissions: {
        SUPER_ADMIN: 'yes',
        TENANT_ADMIN: 'yes',
        AGENCY_ADMIN: 'yes',
        TEAM_LEADER: 'yes',
        QUALITY_INSPECTOR: 'no',
        DATA_SOURCE: 'no',
        COLLECTOR: 'yes'
      }
    },
    {
      name: '案件分配',
      description: '分配案件到机构/小组/催员',
      permissions: {
        SUPER_ADMIN: 'yes',
        TENANT_ADMIN: 'yes',
        AGENCY_ADMIN: 'yes',
        TEAM_LEADER: 'no',
        QUALITY_INSPECTOR: 'no',
        DATA_SOURCE: 'no',
        COLLECTOR: 'no'
      }
    },
    {
      name: '案件转移',
      description: '转移案件到其他催员',
      permissions: {
        SUPER_ADMIN: 'yes',
        TENANT_ADMIN: 'yes',
        AGENCY_ADMIN: 'yes',
        TEAM_LEADER: 'yes',
        QUALITY_INSPECTOR: 'no',
        DATA_SOURCE: 'no',
        COLLECTOR: 'no'
      }
    },
    {
      name: '案件回收',
      description: '回收案件到小组或机构',
      permissions: {
        SUPER_ADMIN: 'yes',
        TENANT_ADMIN: 'yes',
        AGENCY_ADMIN: 'yes',
        TEAM_LEADER: 'yes',
        QUALITY_INSPECTOR: 'no',
        DATA_SOURCE: 'no',
        COLLECTOR: 'no'
      }
    },
    {
      name: '案件催收操作',
      description: '执行催收相关操作（联系、记录等）',
      permissions: {
        SUPER_ADMIN: 'yes',
        TENANT_ADMIN: 'yes',
        AGENCY_ADMIN: 'yes',
        TEAM_LEADER: 'yes',
        QUALITY_INSPECTOR: 'no',
        DATA_SOURCE: 'no',
        COLLECTOR: 'yes'
      }
    },
    {
      name: '案件编辑',
      description: '编辑案件信息',
      permissions: {
        SUPER_ADMIN: 'yes',
        TENANT_ADMIN: 'yes',
        AGENCY_ADMIN: 'yes',
        TEAM_LEADER: 'yes',
        QUALITY_INSPECTOR: 'no',
        DATA_SOURCE: 'no',
        COLLECTOR: 'limited'
      }
    },
    {
      name: '案件删除',
      description: '删除案件',
      permissions: {
        SUPER_ADMIN: 'yes',
        TENANT_ADMIN: 'yes',
        AGENCY_ADMIN: 'yes',
        TEAM_LEADER: 'no',
        QUALITY_INSPECTOR: 'no',
        DATA_SOURCE: 'no',
        COLLECTOR: 'no'
      }
    },
    {
      name: '案件导出',
      description: '导出案件数据',
      permissions: {
        SUPER_ADMIN: 'yes',
        TENANT_ADMIN: 'yes',
        AGENCY_ADMIN: 'yes',
        TEAM_LEADER: 'yes',
        QUALITY_INSPECTOR: 'no',
        DATA_SOURCE: 'no',
        COLLECTOR: 'limited'
      }
    }
  ],

  // 字段配置权限
  field: [
    {
      name: '标准字段管理',
      description: '管理系统标准字段',
      permissions: {
        SUPER_ADMIN: 'yes',
        TENANT_ADMIN: 'no',
        AGENCY_ADMIN: 'no',
        TEAM_LEADER: 'no',
        QUALITY_INSPECTOR: 'no',
        DATA_SOURCE: 'no',
        COLLECTOR: 'no'
      }
    },
    {
      name: '自定义字段管理',
      description: '管理甲方自定义字段',
      permissions: {
        SUPER_ADMIN: 'yes',
        TENANT_ADMIN: 'yes',
        AGENCY_ADMIN: 'no',
        TEAM_LEADER: 'no',
        QUALITY_INSPECTOR: 'no',
        DATA_SOURCE: 'no',
        COLLECTOR: 'no'
      }
    },
    {
      name: '字段分组管理',
      description: '管理字段分组配置',
      permissions: {
        SUPER_ADMIN: 'yes',
        TENANT_ADMIN: 'no',
        AGENCY_ADMIN: 'no',
        TEAM_LEADER: 'no',
        QUALITY_INSPECTOR: 'no',
        DATA_SOURCE: 'no',
        COLLECTOR: 'no'
      }
    },
    {
      name: '队列字段配置',
      description: '配置队列字段显示和隐藏',
      permissions: {
        SUPER_ADMIN: 'yes',
        TENANT_ADMIN: 'yes',
        AGENCY_ADMIN: 'no',
        TEAM_LEADER: 'no',
        QUALITY_INSPECTOR: 'no',
        DATA_SOURCE: 'no',
        COLLECTOR: 'no'
      }
    }
  ],

  // 触达渠道配置权限
  channel: [
    {
      name: '渠道供应商管理',
      description: '管理渠道供应商信息',
      permissions: {
        SUPER_ADMIN: 'yes',
        TENANT_ADMIN: 'yes',
        AGENCY_ADMIN: 'no',
        TEAM_LEADER: 'no',
        QUALITY_INSPECTOR: 'no',
        DATA_SOURCE: 'no',
        COLLECTOR: 'no'
      }
    },
    {
      name: '渠道发送限制配置',
      description: '配置渠道发送限制规则',
      permissions: {
        SUPER_ADMIN: 'yes',
        TENANT_ADMIN: 'yes',
        AGENCY_ADMIN: 'no',
        TEAM_LEADER: 'no',
        QUALITY_INSPECTOR: 'no',
        DATA_SOURCE: 'no',
        COLLECTOR: 'no'
      }
    },
    {
      name: '渠道使用统计',
      description: '查看渠道使用统计数据',
      permissions: {
        SUPER_ADMIN: 'yes',
        TENANT_ADMIN: 'yes',
        AGENCY_ADMIN: 'yes',
        TEAM_LEADER: 'yes',
        QUALITY_INSPECTOR: 'no',
        DATA_SOURCE: 'no',
        COLLECTOR: 'no'
      }
    }
  ],

  // 业绩查看权限
  performance: [
    {
      name: '查看全部业绩',
      description: '查看所有业绩数据',
      permissions: {
        SUPER_ADMIN: 'yes',
        TENANT_ADMIN: 'yes',
        AGENCY_ADMIN: 'no',
        TEAM_LEADER: 'no',
        QUALITY_INSPECTOR: 'no',
        DATA_SOURCE: 'no',
        COLLECTOR: 'no'
      }
    },
    {
      name: '查看机构业绩',
      description: '查看机构业绩统计',
      permissions: {
        SUPER_ADMIN: 'yes',
        TENANT_ADMIN: 'yes',
        AGENCY_ADMIN: 'yes',
        TEAM_LEADER: 'no',
        QUALITY_INSPECTOR: 'no',
        DATA_SOURCE: 'no',
        COLLECTOR: 'no'
      }
    },
    {
      name: '查看小组业绩',
      description: '查看小组业绩统计',
      permissions: {
        SUPER_ADMIN: 'yes',
        TENANT_ADMIN: 'yes',
        AGENCY_ADMIN: 'yes',
        TEAM_LEADER: 'limited',
        QUALITY_INSPECTOR: 'no',
        DATA_SOURCE: 'yes',
        COLLECTOR: 'no'
      }
    },
    {
      name: '查看个人业绩',
      description: '查看个人业绩统计',
      permissions: {
        SUPER_ADMIN: 'yes',
        TENANT_ADMIN: 'yes',
        AGENCY_ADMIN: 'yes',
        TEAM_LEADER: 'limited',
        QUALITY_INSPECTOR: 'no',
        DATA_SOURCE: 'yes',
        COLLECTOR: 'yes'
      }
    },
    {
      name: '业绩报表导出',
      description: '导出业绩报表数据',
      permissions: {
        SUPER_ADMIN: 'yes',
        TENANT_ADMIN: 'yes',
        AGENCY_ADMIN: 'yes',
        TEAM_LEADER: 'yes',
        QUALITY_INSPECTOR: 'no',
        DATA_SOURCE: 'yes',
        COLLECTOR: 'no'
      }
    },
    {
      name: '业绩数据分析',
      description: '查看业绩分析图表',
      permissions: {
        SUPER_ADMIN: 'yes',
        TENANT_ADMIN: 'yes',
        AGENCY_ADMIN: 'yes',
        TEAM_LEADER: 'yes',
        QUALITY_INSPECTOR: 'no',
        DATA_SOURCE: 'yes',
        COLLECTOR: 'no'
      }
    }
  ],

  // 聊天内容查看权限
  chat: [
    {
      name: '查看全部聊天',
      description: '查看所有聊天记录',
      permissions: {
        SUPER_ADMIN: 'yes',
        TENANT_ADMIN: 'yes',
        AGENCY_ADMIN: 'no',
        TEAM_LEADER: 'no',
        QUALITY_INSPECTOR: 'no',
        DATA_SOURCE: 'no',
        COLLECTOR: 'no'
      }
    },
    {
      name: '查看机构聊天',
      description: '查看机构下所有聊天记录',
      permissions: {
        SUPER_ADMIN: 'yes',
        TENANT_ADMIN: 'yes',
        AGENCY_ADMIN: 'yes',
        TEAM_LEADER: 'no',
        QUALITY_INSPECTOR: 'no',
        DATA_SOURCE: 'no',
        COLLECTOR: 'no'
      }
    },
    {
      name: '查看小组聊天',
      description: '查看小组下所有聊天记录',
      permissions: {
        SUPER_ADMIN: 'yes',
        TENANT_ADMIN: 'yes',
        AGENCY_ADMIN: 'yes',
        TEAM_LEADER: 'limited',
        QUALITY_INSPECTOR: 'no',
        DATA_SOURCE: 'no',
        COLLECTOR: 'no'
      }
    },
    {
      name: '查看组员聊天',
      description: '查看指定组员的聊天记录',
      permissions: {
        SUPER_ADMIN: 'yes',
        TENANT_ADMIN: 'yes',
        AGENCY_ADMIN: 'yes',
        TEAM_LEADER: 'limited',
        QUALITY_INSPECTOR: 'limited',
        DATA_SOURCE: 'no',
        COLLECTOR: 'no'
      }
    },
    {
      name: '查看个人聊天',
      description: '查看自己的聊天记录',
      permissions: {
        SUPER_ADMIN: 'yes',
        TENANT_ADMIN: 'yes',
        AGENCY_ADMIN: 'yes',
        TEAM_LEADER: 'yes',
        QUALITY_INSPECTOR: 'no',
        DATA_SOURCE: 'no',
        COLLECTOR: 'yes'
      }
    },
    {
      name: '聊天记录导出',
      description: '导出聊天记录数据',
      permissions: {
        SUPER_ADMIN: 'yes',
        TENANT_ADMIN: 'yes',
        AGENCY_ADMIN: 'yes',
        TEAM_LEADER: 'yes',
        QUALITY_INSPECTOR: 'yes',
        DATA_SOURCE: 'no',
        COLLECTOR: 'no'
      }
    }
  ],

  // 工作台权限
  dashboard: [
    {
      name: '查看工作台',
      description: '访问工作台首页',
      permissions: {
        SUPER_ADMIN: 'yes',
        TENANT_ADMIN: 'yes',
        AGENCY_ADMIN: 'yes',
        TEAM_LEADER: 'yes',
        QUALITY_INSPECTOR: 'yes',
        DATA_SOURCE: 'yes',
        COLLECTOR: 'yes'
      }
    },
    {
      name: '查看待办事项',
      description: '查看待处理的案件和任务',
      permissions: {
        SUPER_ADMIN: 'yes',
        TENANT_ADMIN: 'yes',
        AGENCY_ADMIN: 'yes',
        TEAM_LEADER: 'yes',
        QUALITY_INSPECTOR: 'no',
        DATA_SOURCE: 'no',
        COLLECTOR: 'yes'
      }
    },
    {
      name: '查看统计概览',
      description: '查看数据统计概览',
      permissions: {
        SUPER_ADMIN: 'yes',
        TENANT_ADMIN: 'yes',
        AGENCY_ADMIN: 'yes',
        TEAM_LEADER: 'yes',
        QUALITY_INSPECTOR: 'no',
        DATA_SOURCE: 'yes',
        COLLECTOR: 'no'
      }
    }
  ]
}

export default permissionData

