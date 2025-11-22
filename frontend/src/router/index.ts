import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import MainLayout from '@/layouts/MainLayout.vue'
import ImLayout from '@/layouts/ImLayout.vue'
import { useImUserStore } from '@/stores/imUser'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const routes: RouteRecordRaw[] = [
  // 默认登录页重定向到管理后台登录
  {
    path: '/login',
    redirect: '/admin/login',
  },
  
  // 管理后台登录页
  {
    path: '/admin/login',
    name: 'AdminLogin',
    component: () => import('@/views/admin/Login.vue'),
    meta: { title: 'CCO 管理控台登录', requiresAuth: false },
  },
  
  // 管理后台路由
  {
    path: '/',
    component: MainLayout,
    redirect: '/dashboard',
    meta: { requiresAuth: true },
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/Index.vue'),
        meta: { title: '工作台', requiresAuth: true },
      },
      // 数据看板路由
      {
        path: 'performance/my-dashboard',
        name: 'MyPerformanceDashboard',
        component: () => import('@/views/dashboard/CollectorPerformance.vue'),
        meta: { title: '单催员业绩看板', requiresAuth: true },
      },
      {
        path: 'performance/collector/:id',
        name: 'CollectorPerformance',
        component: () => import('@/views/dashboard/CollectorPerformance.vue'),
        meta: { title: '催员业绩看板', requiresAuth: true },
      },
      {
        path: 'dashboard/idle-monitor',
        name: 'IdleMonitor',
        component: () => import('@/views/dashboard/IdleMonitor.vue'),
        meta: { title: '空闲催员监控', requiresAuth: true },
      },
      {
        path: 'cases',
        name: 'CaseList',
        component: () => import('@/views/case-management/CaseList.vue'),
        meta: { title: '案件列表', requiresAuth: true },
      },
      {
        path: 'cases/:id',
        name: 'CaseDetail',
        component: () => import('@/views/case-management/CaseDetail.vue'),
        meta: { title: '案件详情', requiresAuth: true },
      },
      {
        path: 'auto-assignment',
        name: 'AutoAssignment',
        component: () => import('@/views/case-management/AutoAssignment.vue'),
        meta: { title: '自动化分案', requiresAuth: true },
      },
      {
        path: 'field-config/standard',
        name: 'StandardFields',
        component: () => import('@/views/field-config/StandardFields.vue'),
        meta: { title: '标准字段管理', requiresAuth: true },
      },
      {
        path: 'field-config/tenant-fields-view',
        name: 'TenantFieldsView',
        component: () => import('@/views/field-config/TenantFieldsView.vue'),
        meta: { title: '甲方字段查看', requiresAuth: true },
      },
      {
        path: 'field-config/custom',
        name: 'CustomFields',
        component: () => import('@/views/field-config/CustomFields.vue'),
        meta: { title: '字段映射配置', requiresAuth: true },
      },
      {
        path: 'field-config/groups',
        name: 'FieldGroups',
        component: () => import('@/views/field-config/FieldGroups.vue'),
        meta: { title: '字段分组管理', requiresAuth: true },
      },
      {
        path: 'field-config/display',
        name: 'FieldDisplayConfig',
        component: () => import('@/views/field-config/FieldDisplayConfig.vue'),
        meta: { title: '甲方字段展示配置', requiresAuth: true },
      },
      {
        path: 'tenants',
        name: 'TenantList',
        component: () => import('@/views/tenant-management/TenantList.vue'),
        meta: { title: '甲方管理', requiresAuth: true },
      },
      {
        path: 'tenants/:id/field-config',
        name: 'TenantFieldConfig',
        component: () => import('@/views/tenant-management/TenantFieldConfig.vue'),
        meta: { title: '甲方字段配置', requiresAuth: true },
      },
      {
        path: 'tenants/queue-management',
        name: 'QueueManagement',
        component: () => import('@/views/tenant-management/QueueManagement.vue'),
        meta: { title: '甲方案件队列管理', requiresAuth: true },
      },
      {
        path: 'organization/agencies',
        name: 'AgencyManagement',
        component: () => import('@/views/organization/AgencyManagement.vue'),
        meta: { title: '机构管理', requiresAuth: true },
      },
      {
        path: 'organization/agencies/:id/working-hours',
        name: 'AgencyWorkingHours',
        component: () => import('@/views/organization/AgencyWorkingHours.vue'),
        meta: { title: '机构作息时间管理', requiresAuth: true },
      },
      {
        path: 'organization/team-groups',
        name: 'TeamGroupManagement',
        component: () => import('@/views/organization/TeamGroupManagement.vue'),
        meta: { title: '小组群管理', requiresAuth: true },
      },
      {
        path: 'organization/teams',
        name: 'TeamManagement',
        component: () => import('@/views/organization/TeamManagement.vue'),
        meta: { title: '小组管理', requiresAuth: true },
      },
      {
        path: 'organization/admin-accounts',
        name: 'AdminAccountManagement',
        component: () => import('@/views/organization/AdminAccountManagement.vue'),
        meta: { title: '小组管理员管理', requiresAuth: true },
      },
      {
        path: 'organization/collectors',
        name: 'CollectorManagement',
        component: () => import('@/views/organization/CollectorManagement.vue'),
        meta: { title: '催员管理', requiresAuth: true },
      },
      {
        path: 'channel-config/limits',
        name: 'ChannelLimitConfig',
        component: () => import('@/views/channel-config/ChannelLimitConfig.vue'),
        meta: { title: '渠道发送限制配置', requiresAuth: true },
      },
      {
        path: 'channel-config/suppliers',
        name: 'TenantChannelManagement',
        component: () => import('@/views/channel-config/TenantChannelManagement.vue'),
        meta: { title: '甲方渠道管理', requiresAuth: true, roles: ['SuperAdmin', 'TenantAdmin', 'super_admin', 'tenant_admin'] },
      },
      {
        path: 'system/permissions',
        name: 'PermissionConfiguration',
        component: () => import('@/views/system/PermissionConfiguration.vue'),
        meta: { title: '权限配置', requiresAuth: true, roles: ['SuperAdmin', 'TenantAdmin', 'AgencyAdmin', 'TeamLeader', 'super_admin', 'tenant_admin'] },
      },
      {
        path: 'system/permission-management',
        name: 'PermissionManagement',
        component: () => import('@/views/system/PermissionManagement.vue'),
        meta: { title: '权限查看', requiresAuth: true, roles: ['SuperAdmin', 'TenantAdmin', 'AgencyAdmin', 'TeamLeader', 'super_admin', 'tenant_admin'] },
      },
      {
        path: 'system/notification-config',
        name: 'NotificationConfig',
        component: () => import('@/views/system/NotificationConfig.vue'),
        meta: { title: '通知配置', requiresAuth: true, roles: ['SuperAdmin', 'TenantAdmin', 'super_admin', 'tenant_admin'] },
      },
    ],
  },

  // IM端路由
  {
    path: '/im/login',
    name: 'ImLogin',
    component: () => import('@/views/im/Login.vue'),
    meta: { title: 'CCO-IM 登录', requiresAuth: false },
  },
  {
    path: '/im',
    component: ImLayout,
    redirect: '/im/workspace',
    meta: { requiresAuth: true },
    children: [
      {
        path: 'workspace',
        name: 'ImWorkspace',
        component: () => import('@/views/im/CollectorWorkspace.vue'),
        meta: { title: '催员工作台', requiresAuth: true },
      },
      {
        path: 'cases',
        name: 'ImCases',
        component: () => import('@/views/im/Workspace.vue'), // 暂时复用，后续替换
        meta: { title: '我的案件', requiresAuth: true },
      },
      {
        path: 'messages',
        name: 'ImMessages',
        component: () => import('@/views/im/Workspace.vue'), // 暂时复用，后续替换
        meta: { title: '消息', requiresAuth: true },
      },
    ],
  },
  
  // 404 页面 - 捕获所有未匹配的路由
  {
    path: '/:pathMatch(.*)*',
    redirect: '/admin/login',
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

// 路由守卫 - 权限验证和IM端鉴权
router.beforeEach((to, _from, next) => {
  const userStore = useUserStore()
  const imUserStore = useImUserStore()
  
  // 优先检查IM端路由（避免被管理后台路由守卫拦截）
  if (to.path.startsWith('/im')) {
    console.log('[IM路由守卫] 当前路径:', to.path)
    console.log('[IM路由守卫] isLoggedIn:', imUserStore.isLoggedIn)
    console.log('[IM路由守卫] requiresAuth:', to.meta.requiresAuth)
    
    // 如果需要认证但未登录
    if (to.meta.requiresAuth && !imUserStore.isLoggedIn) {
      console.log('[IM路由守卫] 需要认证但未登录，尝试从localStorage恢复')
      // 尝试从localStorage恢复IM端登录状态
      const restored = imUserStore.initFromStorage()
      console.log('[IM路由守卫] localStorage恢复结果:', restored)
      console.log('[IM路由守卫] 恢复后 isLoggedIn:', imUserStore.isLoggedIn)
      
      if (!imUserStore.isLoggedIn) {
        console.log('[IM路由守卫] 仍未登录，重定向到登录页')
        next('/im/login')
        return
      }
    }
    
    // 如果已登录但访问登录页
    if (to.path === '/im/login' && imUserStore.isLoggedIn) {
      console.log('[IM路由守卫] 已登录，从登录页重定向到工作台')
      next('/im/workspace')
      return
    }
    
    // IM端路由检查完成，直接放行
    console.log('[IM路由守卫] 检查通过，放行')
    next()
    return
  }
  
  // 管理后台路由守卫
  if (to.path.startsWith('/admin')) {
    // 如果是登录页，已登录则跳转到首页
    if (to.path === '/admin/login') {
      if (userStore.userInfo && userStore.token) {
        next('/dashboard')
        return
      }
    }
    next()
    return
  }
  
  // 管理后台主路由（需要登录）
  if (to.path !== '/admin/login' && to.matched.some(record => record.meta.requiresAuth)) {
    // 检查是否已登录
    if (!userStore.userInfo || !userStore.token) {
      // 尝试从localStorage恢复
      userStore.initFromStorage()
      
      if (!userStore.userInfo || !userStore.token) {
        // 未登录，跳转到登录页
        next('/admin/login')
        return
      }
    }
  }
  
  // 检查是否需要权限验证
  if (to.meta.requiresAuth && to.meta.roles) {
    const allowedRoles = to.meta.roles as string[] || []
    
    // 如果用户信息未加载，尝试从localStorage恢复
    if (!userStore.userInfo) {
      const savedUserInfo = localStorage.getItem('userInfo')
      if (savedUserInfo) {
        try {
          userStore.setUserInfo(JSON.parse(savedUserInfo))
        } catch (e) {
          console.error('解析用户信息失败', e)
        }
      }
    }
    
    const finalUserRole = userStore.userInfo?.role || ''
    
    // 如果角色不匹配，检查是否是大小写变体
    if (allowedRoles.length > 0 && !allowedRoles.includes(finalUserRole)) {
      // 尝试大小写不敏感匹配
      const roleLower = finalUserRole.toLowerCase()
      const allowedRolesLower = allowedRoles.map(r => r.toLowerCase())
      
      if (!allowedRolesLower.includes(roleLower)) {
        ElMessage.error('您没有权限访问此页面')
        next('/dashboard')
        return
      }
    }
  }
  
  next()
})

export default router

