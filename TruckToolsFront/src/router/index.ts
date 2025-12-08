import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import MainLayout from '@/layouts/MainLayout.vue'

// 路由配置
const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/Login.vue'),
    meta: { title: '登录', requiresAuth: false }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/auth/Register.vue'),
    meta: { title: '注册', requiresAuth: false }
  },
  {
    path: '/',
    component: MainLayout,
    redirect: '/dashboard',
    children: [
      // 工作台
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '工作台', icon: 'dashboard', released: true }
      },
      // 客户管理模块
      {
        path: 'customer',
        name: 'Customer',
        redirect: '/customer/list',
        meta: { title: '客户管理', icon: 'team', released: true },
        children: [
          {
            path: 'list',
            name: 'CustomerList',
            component: () => import('@/views/customer/CustomerList.vue'),
            meta: { title: '客户列表', released: true }
          },
          {
            path: 'card-upload',
            name: 'CardUpload',
            component: () => import('@/views/customer/CardUpload.vue'),
            meta: { title: '名片识别', released: true }
          },
          {
            path: 'import',
            name: 'CustomerImport',
            component: () => import('@/views/customer/CustomerImport.vue'),
            meta: { title: 'Excel导入', released: true }
          },
          {
            path: 'script-template',
            name: 'ScriptTemplate',
            component: () => import('@/views/customer/ScriptTemplate.vue'),
            meta: { title: '话术模板', released: true }
          },
          {
            path: ':id',
            name: 'CustomerDetail',
            component: () => import('@/views/customer/CustomerDetail.vue'),
            meta: { title: '客户详情', released: true, hidden: true }
          }
        ]
      },
      // 产品管理模块
      {
        path: 'product',
        name: 'Product',
        redirect: '/product/list',
        meta: { title: '产品管理', icon: 'appstore', released: true },
        children: [
          {
            path: 'list',
            name: 'ProductList',
            component: () => import('@/views/product/ProductList.vue'),
            meta: { title: '产品目录', released: true }
          },
          {
            path: 'import',
            name: 'ProductImport',
            component: () => import('@/views/product/ProductImport.vue'),
            meta: { title: '产品导入', released: true }
          },
          {
            path: 'quote',
            name: 'ProductQuote',
            component: () => import('@/views/product/ProductQuote.vue'),
            meta: { title: '搜索报价', released: true }
          },
          {
            path: 'quote-import',
            name: 'ProductQuoteImport',
            component: () => import('@/views/product/ProductQuoteImport.vue'),
            meta: { title: '报价导入', released: true }
          }
        ]
      },
      // 邮件营销模块
      {
        path: 'email',
        name: 'Email',
        redirect: '/email/template',
        meta: { title: '邮件营销', icon: 'mail', released: true },
        children: [
          {
            path: 'template',
            name: 'EmailTemplate',
            component: () => import('@/views/email/TemplateList.vue'),
            meta: { title: '邮件模板', released: true }
          },
          {
            path: 'template/edit/:id?',
            name: 'EmailTemplateEdit',
            component: () => import('@/views/email/TemplateEdit.vue'),
            meta: { title: '编辑模板', released: true, hidden: true }
          },
          {
            path: 'send',
            name: 'EmailSend',
            component: () => import('@/views/email/EmailSend.vue'),
            meta: { title: '批量发送', released: true }
          },
          {
            path: 'logs',
            name: 'EmailLogs',
            component: () => import('@/views/email/EmailLogs.vue'),
            meta: { title: '发送日志', released: true }
          },
          {
            path: 'task/:id',
            name: 'EmailTaskDetail',
            component: () => import('@/views/email/TaskDetail.vue'),
            meta: { title: '任务详情', released: true, hidden: true }
          }
        ]
      },
      // 销售管理模块 - 即将推出
      {
        path: 'sales',
        name: 'Sales',
        component: () => import('@/views/coming-soon/index.vue'),
        meta: { 
          title: '销售管理', 
          icon: 'shopping', 
          released: false,
          plannedQuarter: '2026 Q2',
          features: ['报价单管理', '合同管理', '订单管理', '销售漏斗']
        }
      },
      // 物流跟踪模块 - 即将推出
      {
        path: 'logistics',
        name: 'Logistics',
        component: () => import('@/views/coming-soon/index.vue'),
        meta: { 
          title: '物流跟踪', 
          icon: 'car', 
          released: false,
          plannedQuarter: '2026 Q3',
          features: ['订单物流', '货运追踪', '清关进度']
        }
      },
      // 供应链管理模块 - 即将推出
      {
        path: 'supply-chain',
        name: 'SupplyChain',
        component: () => import('@/views/coming-soon/index.vue'),
        meta: { 
          title: '供应链管理', 
          icon: 'apartment', 
          released: false,
          plannedQuarter: '2026 Q3',
          features: ['供应商管理', '采购管理', '库存管理']
        }
      },
      // 文档中心模块 - 即将推出
      {
        path: 'document',
        name: 'Document',
        component: () => import('@/views/coming-soon/index.vue'),
        meta: { 
          title: '文档中心', 
          icon: 'file-text', 
          released: false,
          plannedQuarter: '2026 Q4',
          features: ['合同文档', '发票管理', '证书归档']
        }
      },
      // 数据分析模块 - 即将推出
      {
        path: 'analytics',
        name: 'Analytics',
        component: () => import('@/views/coming-soon/index.vue'),
        meta: { 
          title: '数据分析', 
          icon: 'line-chart', 
          released: false,
          plannedQuarter: '2027',
          features: ['销售报表', '客户分析', '业务预测']
        }
      },
      // 系统设置
      {
        path: 'settings',
        name: 'Settings',
        redirect: '/settings/profile',
        meta: { title: '系统设置', icon: 'setting', released: true },
        children: [
          {
            path: 'profile',
            name: 'SettingsProfile',
            component: () => import('@/views/settings/Profile.vue'),
            meta: { title: '个人资料', released: true }
          },
          {
            path: 'smtp',
            name: 'SettingsSMTP',
            component: () => import('@/views/settings/SMTPConfig.vue'),
            meta: { title: '邮箱配置', released: true }
          },
          {
            path: 'custom-fields',
            name: 'SettingsCustomFields',
            component: () => import('@/views/settings/CustomFields.vue'),
            meta: { title: '自定义字段', released: true }
          }
        ]
      },
      // 功能路线图
      {
        path: 'roadmap',
        name: 'Roadmap',
        component: () => import('@/views/roadmap/index.vue'),
        meta: { title: '功能路线图', icon: 'flag', released: true }
      }
    ]
  },
  // 404
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/error/404.vue'),
    meta: { title: '页面不存在', requiresAuth: false }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, _from, next) => {
  // 设置页面标题
  document.title = `${to.meta.title || 'TruckTools'} - TruckTools`
  
  // 检查登录状态
  const token = localStorage.getItem('token')
  const requiresAuth = to.meta.requiresAuth !== false
  
  if (requiresAuth && !token) {
    next({ name: 'Login', query: { redirect: to.fullPath } })
  } else if (token && (to.name === 'Login' || to.name === 'Register')) {
    next({ name: 'Dashboard' })
  } else {
    next()
  }
})

export default router

