<script setup>
import Navbar from './components/Navbar.vue'
import { usePerfil } from './composables/usePerfil'
import { useRoute } from 'vue-router'

const perfil = usePerfil()
const route = useRoute()
</script>

<template>
  <Navbar v-if="route.path !== '/login'" />
  <router-view :key="perfil.value" />
</template>

<style scoped>
/* Estilos globais podem ser definidos em style.css */
</style>
