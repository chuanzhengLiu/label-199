import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'
import Layout from '../views/Layout.vue'
import AssetList from '../views/AssetList.vue'
import BorrowList from '../views/BorrowList.vue'
import MaintenanceList from '../views/MaintenanceList.vue'
import ScrapList from '../views/ScrapList.vue'
import Statistics from '../views/Statistics.vue'
import PurchaseList from '../views/PurchaseList.vue'
import UserList from '../views/UserList.vue'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: Login
  },
  {
    path: '/',
    component: Layout,
    redirect: '/assets',
    children: [
      {
        path: 'assets',
        name: 'Assets',
        component: AssetList
      },
      {
        path: 'users',
        name: 'Users',
        component: UserList
      },
      {
        path: 'borrows',
        name: 'Borrows',
        component: BorrowList
      },
      {
        path: 'maintenance',
        name: 'Maintenance',
        component: MaintenanceList
      },
      {
        path: 'scraps',
        name: 'Scraps',
        component: ScrapList
      },
      {
        path: 'statistics',
        name: 'Statistics',
        component: Statistics
      },
      {
        path: 'purchases',
        name: 'Purchases',
        component: PurchaseList
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const user = localStorage.getItem('user')
  if (to.path !== '/login' && !user) {
    next('/login')
  } else {
    next()
  }
})

export default router
