import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'

import SearchView from '../views/SearchView.vue'
import DrugDetailView from '../views/DrugDetailView.vue'
import LoginView from '../views/LoginView.vue'
import SignupView from '../views/SignupView.vue'
import FavoritesView from '../views/FavoritesView.vue'
import DashboardView from '../views/DashboardView.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', redirect: '/search' },
    {
      path: '/search',
      name: 'search',
      component: SearchView,
      meta: { screenId: 'SCR-SEARCH-001' },
    },
    {
      path: '/drugs/:id',
      name: 'drug-detail',
      component: DrugDetailView,
      props: true,
      meta: { screenId: 'SCR-DETAIL-001' },
    },
    {
      path: '/login',
      name: 'login',
      component: LoginView,
      meta: { screenId: 'SCR-AUTH-001', guestOnly: true },
    },
    {
      path: '/signup',
      name: 'signup',
      component: SignupView,
      meta: { screenId: 'SCR-AUTH-002', guestOnly: true },
    },
    {
      path: '/favorites',
      name: 'favorites',
      component: FavoritesView,
      meta: { screenId: 'SCR-MY-001', requiresAuth: true },
    },
    {
      path: '/dashboard',
      name: 'dashboard',
      component: DashboardView,
      meta: { screenId: 'SCR-DASH-001', requiresAuth: true },
    },
  ],
})

router.beforeEach((to) => {
  const auth = useAuthStore()
  if (to.meta.requiresAuth && !auth.isAuthenticated)
    return { name: 'login', query: { redirect: to.fullPath } }
  if (to.meta.guestOnly && auth.isAuthenticated) return { name: 'dashboard' }
})

export default router
