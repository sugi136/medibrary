<script setup>
import { useRouter } from 'vue-router'
import { useAuthStore } from './stores/auth'

const router = useRouter()
const auth = useAuthStore()

function logout() {
  auth.logout()
  router.push('/search')
}
</script>

<template>
  <div class="app-shell">
    <header class="site-header">
      <RouterLink class="brand" to="/search" aria-label="메디브러리 홈">
        <span class="brand-mark">M</span>
        <span>메디브러리</span>
      </RouterLink>
      <nav class="main-nav" aria-label="주요 메뉴">
        <RouterLink to="/search">약 검색</RouterLink>
        <RouterLink v-if="auth.isAuthenticated" to="/dashboard">대시보드</RouterLink>
        <RouterLink v-if="auth.isAuthenticated" to="/favorites">즐겨찾기</RouterLink>
      </nav>
      <div class="header-actions">
        <template v-if="auth.isAuthenticated">
          <button class="text-button" type="button" @click="logout">로그아웃</button>
        </template>
        <template v-else>
          <RouterLink class="text-link" to="/login">로그인</RouterLink>
          <RouterLink class="button button-small" to="/signup">회원가입</RouterLink>
        </template>
      </div>
    </header>

    <main class="page-container">
      <RouterView />
    </main>

    <footer class="site-footer">
      <p>
        메디브러리는 의약품 정보를 쉽게 확인할 수 있도록 돕는 서비스입니다. 복용 전에는 의사 또는
        약사와 상담하세요.
      </p>
    </footer>
  </div>
</template>
