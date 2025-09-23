<script lang="ts" setup>
import Navbar from './components/Navbar.vue'
import {useRoute} from 'vue-router'
import {computed, ref, watch} from 'vue'
import BarraNavegacao from './components/BarraNavegacao.vue';
import NotificacaoContainer from './components/NotificacaoContainer.vue';
import pkg from '../package.json';

interface PackageJson {
  version: string;
  [key: string]: unknown;
}

const route = useRoute()

const hideExtrasOnce = ref(false)

function refreshHideFlag() {
  let came = false
  try {
    came = sessionStorage.getItem('cameFromNavbar') === '1'
  } catch {
  }
  hideExtrasOnce.value = came
  if (came) {
    try {
      sessionStorage.removeItem('cameFromNavbar')
    } catch {
    }
  }
}

watch(() => route.fullPath, () => refreshHideFlag(), {immediate: true})
const version = (pkg as PackageJson).version

const shouldShowNavBarExtras = computed(() => {
  if (route.path === '/login') return false
  if (route.path === '/painel') return false
  return !hideExtrasOnce.value
})
</script>

<template>
  <Navbar v-if="route.path !== '/login'" />
  <div
    v-if="shouldShowNavBarExtras"
    class="bg-light border-bottom"
  >
    <div class="container py-2">
      <BarraNavegacao />
    </div>
  </div>
  <router-view />
  <NotificacaoContainer />
  <footer
  v-if="route.path !== '/login'"
  class="bg-light text-muted border-top mt-4">
    <div class="container py-3 small d-flex justify-content-between align-items-center">
      <span>Versão {{ version }}</span>
      <span>© SESEL/COSIS/TRE-PE</span>
    </div>
  </footer>
</template>