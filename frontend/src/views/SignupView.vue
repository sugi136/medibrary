<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { authApi } from '../api'

const router = useRouter()
const name = ref('')
const email = ref('')
const password = ref('')
const confirmPassword = ref('')
const termsAgreed = ref(false)
const medicalNoticeAgreed = ref(false)
const error = ref('')
const loading = ref(false)

const PASSWORD_PATTERN = /^(?=.*[A-Za-z])(?=.*\d)(?=.*[^A-Za-z0-9])\S{8,72}$/

async function signup() {
  error.value = ''

  if (!name.value || !email.value || !password.value) {
    error.value = '이름, 이메일, 비밀번호를 모두 입력해 주세요.'
    return
  }
  if (!PASSWORD_PATTERN.test(password.value)) {
    error.value = '비밀번호는 8자 이상이며 영문, 숫자, 기호를 각각 포함해야 합니다.'
    return
  }
  if (password.value !== confirmPassword.value) {
    error.value = '비밀번호가 일치하지 않습니다.'
    return
  }
  if (!termsAgreed.value || !medicalNoticeAgreed.value) {
    error.value = '서비스 약관과 의료정보 고지에 모두 동의해 주세요.'
    return
  }

  loading.value = true
  try {
    await authApi.signup({
      name: name.value,
      email: email.value,
      password: password.value,
      termsAgreed: termsAgreed.value,
      medicalNoticeAgreed: medicalNoticeAgreed.value,
    })
    router.push({ name: 'login', query: { registered: '1' } })
  } catch (e) {
    error.value =
      e.response?.status === 409
        ? '이미 사용 중인 이메일입니다.'
        : e.response?.data?.message || '회원가입에 실패했습니다. 다시 시도해 주세요.'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <section class="auth-wrap">
    <div class="auth-copy">
      <h1>약 정보를 더<br />가깝게 관리하세요.</h1>
      <p>간단한 회원가입으로 즐겨찾기와 병용금기 체크 기능을 이용할 수 있습니다.</p>
    </div>

    <form class="auth-card card" @submit.prevent="signup">
      <h2>회원가입</h2>

      <div class="field">
        <label for="name">이름</label>
        <input id="name" v-model="name" class="input" placeholder="이름" autocomplete="name" />
      </div>
      <div class="field">
        <label for="email">이메일</label>
        <input
          id="email"
          v-model="email"
          class="input"
          type="email"
          placeholder="이메일"
          autocomplete="email"
        />
      </div>
      <div class="field">
        <label for="password">비밀번호</label>
        <input
          id="password"
          v-model="password"
          class="input"
          type="password"
          placeholder="8자 이상, 영문·숫자·기호 포함"
          autocomplete="new-password"
        />
      </div>
      <div class="field">
        <label for="confirm">비밀번호 확인</label>
        <input
          id="confirm"
          v-model="confirmPassword"
          class="input"
          type="password"
          placeholder="비밀번호를 다시 입력"
          autocomplete="new-password"
        />
      </div>

      <fieldset class="consent-group">
        <legend>필수 동의</legend>
        <label class="consent-item" for="terms-agreed">
          <input id="terms-agreed" v-model="termsAgreed" type="checkbox" />
          <span><strong>[필수]</strong> 서비스 이용 약관에 동의합니다.</span>
        </label>
        <label class="consent-item" for="medical-notice-agreed">
          <input id="medical-notice-agreed" v-model="medicalNoticeAgreed" type="checkbox" />
          <span
            ><strong>[필수]</strong> 의약품 정보는 참고용이며 의사·약사 상담을 대체하지 않음을
            확인했습니다.</span
          >
        </label>
      </fieldset>

      <p v-if="error" class="error-message">{{ error }}</p>
      <button class="button" :disabled="loading" type="submit">
        {{ loading ? '가입 중…' : '가입하기' }}
      </button>
      <p class="auth-bottom">이미 계정이 있으신가요? <RouterLink to="/login">로그인</RouterLink></p>
    </form>
  </section>
</template>

<style scoped>
.auth-wrap {
  display: grid;
  grid-template-columns: 0.95fr 1fr;
  gap: 70px;
  min-height: 580px;
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
  gap: 15px;
  padding: 34px;
  max-width: 430px;
  width: 100%;
}
.auth-card h2 {
  margin-bottom: 5px;
}
.consent-group {
  display: grid;
  gap: 10px;
  margin: 2px 0;
  padding: 13px;
  border: 1px solid var(--border);
  border-radius: 12px;
}
.consent-group legend {
  padding: 0 4px;
  color: var(--muted);
  font-size: 13px;
}
.consent-item {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  color: var(--text);
  font-size: 13px;
  line-height: 1.5;
  cursor: pointer;
}
.consent-item input {
  margin-top: 3px;
  accent-color: var(--primary-dark);
}
.consent-item strong {
  color: var(--primary-dark);
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
