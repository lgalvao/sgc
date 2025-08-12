<script setup lang="ts">
import Navbar from './components/Navbar.vue'
import {usePerfil} from './composables/usePerfil'
import {useRoute, useRouter} from 'vue-router'
import { computed, ref, watch } from 'vue'
import BackButton from './components/BackButton.vue'
import Breadcrumbs from './components/Breadcrumbs.vue'

const perfil = usePerfil()
const route = useRoute()
const router = useRouter()

// Esconde BackButton/Breadcrumbs uma vez quando a navegação veio da navbar
const hideExtrasOnce = ref(false)

function refreshHideFlag() {
  let came = false
  try { came = sessionStorage.getItem('cameFromNavbar') === '1' } catch {}
  hideExtrasOnce.value = came
  if (came) {
    try { sessionStorage.removeItem('cameFromNavbar') } catch {}
  }
}

watch(() => route.fullPath, () => refreshHideFlag(), { immediate: true })

const shouldShowNavBarExtras = computed(() => {
  if (route.path === '/login') return false
  if (route.path === '/painel') return false
  return !hideExtrasOnce.value
})
</script>

<template>
  <Navbar v-if="route.path !== '/login'" />
  <div v-if="shouldShowNavBarExtras" class="bg-light border-bottom">
    <div class="container py-2 d-flex align-items-center gap-3">
      <BackButton />
      <Breadcrumbs />
    </div>
  </div>
  <router-view :key="perfil" />
</template>