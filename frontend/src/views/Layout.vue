<template>
  <el-container class="layout-container">
    <el-aside width="220px" class="aside">
      <div class="logo">
        <el-icon class="logo-icon"><Monitor /></el-icon>
        <span v-if="!isCollapse">资产管理系统</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        class="el-menu-vertical"
        background-color="#001529"
        text-color="rgba(255,255,255,0.65)"
        active-text-color="#fff"
        :collapse="isCollapse"
        router
      >
        <el-menu-item index="/assets">
          <el-icon><Monitor /></el-icon>
          <template #title>资产列表</template>
        </el-menu-item>
        <el-menu-item index="/users" v-if="user.role === 'ADMIN'">
          <el-icon><User /></el-icon>
          <template #title>用户管理</template>
        </el-menu-item>
        <el-menu-item index="/borrows">
          <el-icon><List /></el-icon>
          <template #title>借用记录</template>
        </el-menu-item>
        <el-menu-item index="/maintenance">
          <el-icon><Tools /></el-icon>
          <template #title>维修记录</template>
        </el-menu-item>
        <el-menu-item index="/scraps" v-if="user.role === 'ADMIN'">
          <el-icon><Delete /></el-icon>
          <template #title>报废记录</template>
        </el-menu-item>
        <el-menu-item index="/purchases" v-if="user.role === 'ADMIN' || user.role === 'MANAGER'">
          <el-icon><ShoppingCart /></el-icon>
          <template #title>采购申请</template>
        </el-menu-item>
        <el-menu-item index="/statistics" v-if="user.role === 'ADMIN' || user.role === 'MANAGER'">
          <el-icon><DataLine /></el-icon>
          <template #title>数据统计</template>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <div class="header-left">
            <el-icon class="trigger" @click="toggleCollapse">
                <component :is="isCollapse ? 'Expand' : 'Fold'" />
            </el-icon>
            <el-breadcrumb separator="/">
                <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
                <el-breadcrumb-item>{{ currentRouteName }}</el-breadcrumb-item>
            </el-breadcrumb>
        </div>
        <div class="header-right">
            <el-dropdown @command="handleCommand">
                <span class="el-dropdown-link">
                    <el-avatar :size="32" class="user-avatar">{{ user.realName ? user.realName.charAt(0) : 'U' }}</el-avatar>
                    <span class="username">{{ user.realName }}</span>
                    <el-icon class="el-icon--right"><arrow-down /></el-icon>
                </span>
                <template #dropdown>
                    <el-dropdown-menu>
                        <el-dropdown-item command="profile">个人信息</el-dropdown-item>
                        <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
                    </el-dropdown-menu>
                </template>
            </el-dropdown>
        </div>
      </el-header>
      <el-main class="main">
        <router-view v-slot="{ Component }">
            <transition name="fade-transform" mode="out-in">
                <component :is="Component" />
            </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Expand, Fold, ArrowDown, Monitor, User, List, Tools, Delete, DataLine, ShoppingCart } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const user = JSON.parse(localStorage.getItem('user') || '{}')
const isCollapse = ref(false)

const activeMenu = computed(() => route.path)

const currentRouteName = computed(() => {
    const map = {
        '/assets': '资产列表',
        '/users': '用户管理',
        '/borrows': '借用记录',
        '/maintenance': '维修记录',
        '/scraps': '报废记录',
        '/statistics': '数据统计',
        '/purchases': '采购申请'
    }
    return map[route.path] || '当前页面'
})

const toggleCollapse = () => {
    isCollapse.value = !isCollapse.value
}

const handleCommand = (command) => {
    if (command === 'logout') {
        localStorage.removeItem('user')
        router.push('/login')
    }
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
}
.aside {
  background-color: #001529;
  color: white;
  transition: width 0.3s;
  overflow: hidden;
  box-shadow: 2px 0 6px rgba(0,21,41,0.35);
  z-index: 10;
}
.logo {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: bold;
  background: #002140;
  color: white;
  white-space: nowrap;
  overflow: hidden;
}
.logo-icon {
    font-size: 24px;
    margin-right: 10px;
    color: #409EFF;
}
.el-menu-vertical {
    border-right: none;
}
.el-menu-vertical:not(.el-menu--collapse) {
  width: 220px;
}

:deep(.el-menu-item.is-active) {
    background-color: #1890ff !important;
}

.header {
  background-color: white;
  color: #333;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  box-shadow: 0 1px 4px rgba(0,21,41,0.08);
  height: 64px;
  z-index: 9;
}

.header-left {
    display: flex;
    align-items: center;
    gap: 20px;
}

.trigger {
    font-size: 20px;
    cursor: pointer;
    transition: color 0.3s;
}
.trigger:hover {
    color: #1890ff;
}

.header-right {
    display: flex;
    align-items: center;
}

.el-dropdown-link {
    display: flex;
    align-items: center;
    cursor: pointer;
    color: #333;
}
.user-avatar {
    background-color: #409EFF;
    margin-right: 8px;
}
.username {
    font-weight: 500;
}

.main {
  background-color: #f0f2f5;
  padding: 24px;
  overflow-y: auto;
}

/* Route Transition */
.fade-transform-leave-active,
.fade-transform-enter-active {
  transition: all 0.5s;
}

.fade-transform-enter-from {
  opacity: 0;
  transform: translateX(-30px);
}

.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(30px);
}
</style>
