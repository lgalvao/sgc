<template>
  <button
    v-if="shouldShow"
    class="btn btn-outline-secondary btn-sm"
    type="button"
    @click="goBack"
  >
    <i class="bi bi-arrow-left"></i> Voltar
  </button>
</template>

<script setup lang="ts">
import {computed} from 'vue'
import {useRoute, useRouter} from 'vue-router'

const route = useRoute()
const router = useRouter()

const fallbackPath = '/painel'

const shouldShow = computed(() => {
  // Esconde no login e quando veio diretamente via navbar
  if (route.path === '/login') return false
  if (route.query.fromNavbar) return false
  return true
})

function canGoBack(): boolean {
  // Heurística simples: histórico > 1
  return window.history.length > 1
}

function goBack() {
  if (canGoBack()) router.back()
  else router.push(fallbackPath)
}
</script>
