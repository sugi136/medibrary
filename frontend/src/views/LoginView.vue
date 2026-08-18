<script setup>
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()
const email = ref('')
const password = ref('')
const error = ref('')
const loading = ref(false)

async function login() {
  error.value = ''
  if (!email.value || password.value.length < 4) {
    error.value = '이메일과 4자 이상의 비밀번호를 입력해 주세요.'
    return
  }
  loading.value = true
  try {
    await auth.login({ email: email.value, password: password.value })
    router.push(route.query.redirect || '/dashboard')
  } catch {
    error.value = '이메일 또는 비밀번호가 올바르지 않습니다.'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <section class="auth-wrap">
    <div class="auth-copy">
      <h1>다시 만나서 반가워요.</h1>
      <p>로그인하고 즐겨찾기한 약과 병용금기 정보를 관리하세요.</p>
    </div>
    <form class="auth-card card" @submit.prevent="login">
      <h2>로그인</h2>
      <div class="field">
        <label for="email">이메일</label
        ><input
          id="email"
          v-model="email"
          class="input"
          type="email"
          placeholder="이메일"
          autocomplete="email"
        />
      </div>
      <div class="field">
        <label for="password">비밀번호</label
        ><input
          id="password"
          v-model="password"
          class="input"
          type="password"
          placeholder="비밀번호"
          autocomplete="current-password"
        />
      </div>
      <p v-if="error" class="error-message">{{ error }}</p>
      <button class="button" :disabled="loading" type="submit">
        {{ loading ? '로그인 중…' : '로그인' }}
      </button>
      <p class="auth-bottom">
        아직 회원이 아니신가요? <RouterLink to="/signup">회원가입</RouterLink>
      </p>
    </form>
  </section>
</template>

<style scoped>
.auth-wrap {
  display: grid;
  grid-template-columns: 0.95fr 1fr;
  gap: 70px;
  min-height: 540px;
  align-items: center;
}
.auth-copy h1 {
  margin-top: 12px;
}
.auth-copy p {
  color: var(--muted);
  line-height: 1.8;
}
.auth-card {
  display: grid;
  gap: 18px;
  padding: 34px;
  max-width: 430px;
  width: 100%;
}
.auth-card h2 {
  margin-bottom: 5px;
}
.auth-card .button {
  width: 100%;
  margin-top: 5px;
}
.auth-bottom {
  margin: 4px 0 0;
  color: var(--muted);
  font-size: 14px;
  text-align: center;
}
.auth-bottom a {
  color: var(--primary-dark);
  font-weight: 700;
}
@media (max-width: 720px) {
  .auth-wrap {
    grid-template-columns: 1fr;
    gap: 25px;
    min-height: auto;
  }
  .auth-card {
    max-width: none;
  }
  .auth-copy h1 {
    margin-bottom: 8px;
  }
}
</style>
