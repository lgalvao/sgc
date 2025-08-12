<script setup lang="ts">
import Navbar from './components/Navbar.vue'
import {usePerfil} from './composables/usePerfil'
import {useRoute} from 'vue-router'
import { computed } from 'vue'
import BackButton from './components/BackButton.vue'
import Breadcrumbs from './components/Breadcrumbs.vue'

const perfil = usePerfil()
const route = useRoute()

const shouldShowNavBarExtras = computed(() => {
  if (route.path === '/login') return false
  if (route.path === '/painel') return false
  if (route.query.fromNavbar) return false
  return true
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