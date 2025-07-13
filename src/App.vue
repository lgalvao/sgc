<script setup>
import Navbar from './components/Navbar.vue'
import { usePerfil } from './composables/usePerfil'
const perfil = usePerfil()
</script>

<template>
  <Navbar />
  <router-view :key="perfil.value" />
</template>

<style scoped>
/* Estilos globais podem ser definidos em style.css */
</style>
