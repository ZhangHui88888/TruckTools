<template>
  <a-layout class="main-layout">
    <!-- 侧边栏 -->
    <a-layout-sider
      v-model:collapsed="collapsed"
      :trigger="null"
      collapsible
      :width="240"
      :collapsed-width="80"
      class="layout-sider"
    >
      <div class="logo" @click="$router.push('/')">
        <img src="@/assets/images/logo.svg" alt="TruckTools" class="logo-icon" />
        <transition name="fade">
          <span v-if="!collapsed" class="logo-text">TruckTools</span>
        </transition>
      </div>

      <a-menu
        v-model:selectedKeys="selectedKeys"
        v-model:openKeys="openKeys"
        mode="inline"
        theme="dark"
        class="side-menu"
      >
        <template v-for="item in menuItems">
          <!-- 有子菜单 -->
          <a-sub-menu v-if="item.children && item.children.length > 0" :key="'sub-' + item.key">
            <template #icon>
              <component :is="item.icon" />
            </template>
            <template #title>{{ item.label }}</template>
            <a-menu-item
              v-for="child in item.children"
              :key="child.key"
              @click="handleMenuItemClick(child)"
            >
              {{ child.label }}
            </a-menu-item>
          </a-sub-menu>
          <!-- 无子菜单 -->
          <a-menu-item v-else :key="'item-' + item.key" @click="handleMenuItemClick(item)">
            <template #icon>
              <component :is="item.icon" />
            </template>
            <span>{{ item.label }}</span>
            <a-tag v-if="!item.released" size="small" color="orange" style="margin-left: 8px; font-size: 10px;">
              即将推出
            </a-tag>
          </a-menu-item>
        </template>
      </a-menu>

      <!-- 底部路线图入口 -->
      <div class="sider-footer">
        <a-button type="link" block @click="$router.push('/roadmap')">
          <template #icon>
            <FlagOutlined />
          </template>
          <span v-if="!collapsed">功能路线图</span>
        </a-button>
      </div>
    </a-layout-sider>

    <!-- 主内容区 -->
    <a-layout class="layout-content">
      <!-- 顶部导航 -->
      <a-layout-header class="layout-header">
        <div class="header-left">
          <a-button type="text" class="trigger" @click="collapsed = !collapsed">
            <MenuUnfoldOutlined v-if="collapsed" />
            <MenuFoldOutlined v-else />
          </a-button>
          
          <!-- 面包屑 -->
          <a-breadcrumb class="breadcrumb">
            <a-breadcrumb-item v-for="item in breadcrumbs" :key="item.path">
              <router-link v-if="item.path" :to="item.path">{{ item.title }}</router-link>
              <span v-else>{{ item.title }}</span>
            </a-breadcrumb-item>
          </a-breadcrumb>
        </div>

        <div class="header-center">
          <a-input-search
            v-model:value="searchKeyword"
            placeholder="搜索客户..."
            style="width: 320px"
            @search="handleSearch"
          />
        </div>

        <div class="header-right">
          <a-tooltip title="帮助文档">
            <a-button type="text">
              <QuestionCircleOutlined />
            </a-button>
          </a-tooltip>
          
          <a-badge :count="notificationCount" :offset="[-5, 5]">
            <a-button type="text">
              <BellOutlined />
            </a-button>
          </a-badge>

          <a-dropdown :trigger="['click']">
            <div class="user-info">
              <a-avatar :src="userStore.avatar" :size="32">
                {{ userStore.nickname?.charAt(0) || 'U' }}
              </a-avatar>
              <span class="username">{{ userStore.nickname || userStore.username }}</span>
              <DownOutlined style="font-size: 12px" />
            </div>
            <template #overlay>
              <a-menu>
                <a-menu-item key="profile" @click="$router.push('/settings/profile')">
                  <UserOutlined />
                  <span>个人中心</span>
                </a-menu-item>
                <a-menu-item key="settings" @click="$router.push('/settings/smtp')">
                  <SettingOutlined />
                  <span>系统设置</span>
                </a-menu-item>
                <a-menu-divider />
                <a-menu-item key="logout" @click="handleLogout">
                  <LogoutOutlined />
                  <span>退出登录</span>
                </a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
        </div>
      </a-layout-header>

      <!-- 页面内容 -->
      <a-layout-content class="layout-main">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </a-layout-content>
    </a-layout>
  </a-layout>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import {
  MenuFoldOutlined,
  MenuUnfoldOutlined,
  DashboardOutlined,
  TeamOutlined,
  MailOutlined,
  ShoppingOutlined,
  CarOutlined,
  ApartmentOutlined,
  FileTextOutlined,
  LineChartOutlined,
  SettingOutlined,
  FlagOutlined,
  QuestionCircleOutlined,
  BellOutlined,
  UserOutlined,
  LogoutOutlined,
  DownOutlined,
  AppstoreOutlined
} from '@ant-design/icons-vue'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const collapsed = ref(false)
const searchKeyword = ref('')
const notificationCount = ref(0)

// 当前选中的菜单项
const selectedKeys = ref<string[]>([])
const openKeys = ref<string[]>([])

// 菜单原始配置
const menuItems = [
  {
    key: 'dashboard',
    icon: DashboardOutlined,
    label: '工作台',
    path: '/dashboard',
    released: true
  },
  {
    key: 'customer',
    icon: TeamOutlined,
    label: '客户管理',
    path: '/customer',
    released: true,
    children: [
      { key: 'customer-list', label: '客户列表', path: '/customer/list', released: true },
      { key: 'card-upload', label: '名片识别', path: '/customer/card-upload', released: true },
      { key: 'customer-import', label: 'Excel导入', path: '/customer/import', released: true }
    ]
  },
  {
    key: 'product',
    icon: AppstoreOutlined,
    label: '产品管理',
    path: '/product',
    released: true,
    children: [
      { key: 'product-list', label: '产品目录', path: '/product/list', released: true },
      { key: 'product-import', label: '产品导入', path: '/product/import', released: true },
      { key: 'product-quote', label: '搜索报价', path: '/product/quote', released: true }
    ]
  },
  {
    key: 'email',
    icon: MailOutlined,
    label: '邮件营销',
    path: '/email',
    released: true,
    children: [
      { key: 'email-template', label: '邮件模板', path: '/email/template', released: true },
      { key: 'email-send', label: '批量发送', path: '/email/send', released: true },
      { key: 'email-logs', label: '发送日志', path: '/email/logs', released: true }
    ]
  },
  {
    key: 'sales',
    icon: ShoppingOutlined,
    label: '销售管理',
    path: '/sales',
    released: false
  },
  {
    key: 'logistics',
    icon: CarOutlined,
    label: '物流跟踪',
    path: '/logistics',
    released: false
  },
  {
    key: 'supply-chain',
    icon: ApartmentOutlined,
    label: '供应链管理',
    path: '/supply-chain',
    released: false
  },
  {
    key: 'document',
    icon: FileTextOutlined,
    label: '文档中心',
    path: '/document',
    released: false
  },
  {
    key: 'analytics',
    icon: LineChartOutlined,
    label: '数据分析',
    path: '/analytics',
    released: false
  },
  {
    key: 'settings',
    icon: SettingOutlined,
    label: '系统设置',
    path: '/settings',
    released: true,
    children: [
      { key: 'settings-profile', label: '个人资料', path: '/settings/profile', released: true },
      { key: 'settings-smtp', label: '邮箱配置', path: '/settings/smtp', released: true },
      { key: 'settings-fields', label: '自定义字段', path: '/settings/custom-fields', released: true }
    ]
  }
]


// 面包屑
const breadcrumbs = computed(() => {
  const matched = route.matched.filter(item => item.meta?.title)
  return matched.map(item => ({
    title: item.meta.title as string,
    path: item.path
  }))
})

// 监听路由变化，更新菜单选中状态
watch(
  () => route.path,
  (path) => {
    // 查找匹配的菜单项
    for (const item of menuItems) {
      if (item.children) {
        const child = item.children.find(c => path.startsWith(c.path))
        if (child) {
          selectedKeys.value = [child.key]
          openKeys.value = [item.key]
          return
        }
      }
      if (path.startsWith(item.path)) {
        selectedKeys.value = [item.key]
        return
      }
    }
  },
  { immediate: true }
)

// 菜单项点击
const handleMenuItemClick = (item: { key: string; path: string; released: boolean }) => {
  if (!item.released) {
    message.info('此功能即将推出，敬请期待！')
    return
  }
  router.push(item.path)
}

// 搜索
const handleSearch = () => {
  if (searchKeyword.value) {
    router.push({
      path: '/customer/list',
      query: { keyword: searchKeyword.value }
    })
  }
}

// 退出登录
const handleLogout = () => {
  userStore.logout()
  router.push('/login')
  message.success('已退出登录')
}
</script>

<style lang="less" scoped>
.main-layout {
  min-height: 100vh;
}

.layout-sider {
  position: fixed;
  left: 0;
  top: 0;
  bottom: 0;
  z-index: 100;
  overflow: auto;
  background: #001529;
  
  .logo {
    height: 64px;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 0 16px;
    cursor: pointer;
    overflow: hidden;
    
    .logo-icon {
      width: 32px;
      height: 32px;
      flex-shrink: 0;
    }
    
    .logo-text {
      color: #fff;
      font-size: 18px;
      font-weight: 600;
      margin-left: 12px;
      white-space: nowrap;
    }
  }
  
  .side-menu {
    border-right: none;
    
    :deep(.ant-menu-item),
    :deep(.ant-menu-submenu-title) {
      margin: 4px 8px;
      border-radius: 6px;
      
      .coming-tag {
        margin-left: 8px;
        font-size: 10px;
        line-height: 16px;
        height: 16px;
        padding: 0 4px;
      }
    }
  }
  
  .sider-footer {
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    padding: 16px;
    border-top: 1px solid rgba(255, 255, 255, 0.1);
    
    :deep(.ant-btn) {
      color: rgba(255, 255, 255, 0.65);
      
      &:hover {
        color: #fff;
      }
    }
  }
}

.layout-content {
  margin-left: 240px;
  transition: margin-left 0.2s;
  
  .ant-layout-sider-collapsed + & {
    margin-left: 80px;
  }
}

.layout-header {
  position: sticky;
  top: 0;
  z-index: 99;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  
  .header-left {
    display: flex;
    align-items: center;
    
    .trigger {
      font-size: 18px;
      padding: 0 12px;
    }
    
    .breadcrumb {
      margin-left: 16px;
    }
  }
  
  .header-right {
    display: flex;
    align-items: center;
    gap: 8px;
    
    .user-info {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 4px 12px;
      cursor: pointer;
      border-radius: 6px;
      transition: background 0.2s;
      
      &:hover {
        background: rgba(0, 0, 0, 0.04);
      }
      
      .username {
        max-width: 100px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
    }
  }
}

.layout-main {
  margin: 24px;
  min-height: calc(100vh - 64px - 48px);
}

// 侧边栏折叠时的适配
:deep(.ant-layout-sider-collapsed) {
  .logo-text {
    display: none;
  }
  
  .coming-tag {
    display: none;
  }
  
  .sider-footer span {
    display: none;
  }
}

// 折叠状态下的内容区
.ant-layout-sider-collapsed + .layout-content {
  margin-left: 80px;
}
</style>

