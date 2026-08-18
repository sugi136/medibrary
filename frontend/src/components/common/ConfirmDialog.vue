<script setup>
defineProps({
  open: { type: Boolean, default: false },
  title: { type: String, required: true },
  message: { type: String, required: true },
  confirmLabel: { type: String, default: '확인' },
  loading: { type: Boolean, default: false },
})

defineEmits(['confirm', 'cancel'])
</script>

<template>
  <Teleport to="body">
    <div v-if="open" class="dialog-backdrop" role="presentation" @click.self="$emit('cancel')">
      <section class="dialog" role="dialog" aria-modal="true" :aria-label="title">
        <h2>{{ title }}</h2>
        <p>{{ message }}</p>
        <div class="actions">
          <button
            class="button button-secondary"
            type="button"
            :disabled="loading"
            @click="$emit('cancel')"
          >
            취소
          </button>
          <button
            class="button button-danger"
            type="button"
            :disabled="loading"
            @click="$emit('confirm')"
          >
            {{ loading ? '처리 중…' : confirmLabel }}
          </button>
        </div>
      </section>
    </div>
  </Teleport>
</template>

<style scoped>
.dialog-backdrop {
  position: fixed;
  inset: 0;
  z-index: 100;
  display: grid;
  place-items: center;
  padding: 20px;
  background: rgba(24, 58, 74, 0.35);
}
.dialog {
  width: min(100%, 390px);
  padding: 24px;
  border-radius: 18px;
  background: #fff;
  box-shadow: 0 20px 60px rgba(16, 62, 82, 0.24);
}
.dialog h2 {
  margin-bottom: 8px;
}
.dialog p {
  color: var(--muted);
  line-height: 1.7;
}
.actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 24px;
}
</style>
