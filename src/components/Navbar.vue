<template>
  <nav class="navbar navbar-expand-lg navbar-dark bg-primary">
    <div class="container">
      <router-link class="navbar-brand" to="/painel">SGC</router-link>
      <button aria-controls="navbarNav" aria-expanded="false"
              aria-label="Alternar navegação" class="navbar-toggler" data-bs-target="#navbarNav"
              data-bs-toggle="collapse" type="button">
        <span class="navbar-toggler-icon"></span>
      </button>

      <div id="navbarNav" class="collapse navbar-collapse">
        <ul class="navbar-nav me-auto mb-2 mb-lg-0">
          <li class="nav-item">
            <router-link class="nav-link" to="/painel">Painel</router-link>
          </li>
          <li class="nav-item">
            <router-link class="nav-link" to="/unidades">Unidades</router-link>
          </li>
          <li class="nav-item">
            <router-link class="nav-link" to="/historico">Histórico</router-link>
          </li>
        </ul>

        <ul class="navbar-nav align-items-center">
          <li class="nav-item me-2">
            <select v-model="perfilValue" class="form-select form-select-sm">
              <option value="SEDOC">SEDOC</option>
              <option value="CHEFE">CHEFE</option>
              <option value="GESTOR">GESTOR</option>
            </select>
          </li>
          <li class="nav-item">
            <router-link class="nav-link" to="/login">Sair</router-link>
          </li>
        </ul>
      </div>
    </div>
  </nav>
</template>

<script setup>
import {computed} from 'vue'
import {usePerfil} from '../composables/usePerfil'

const perfil = usePerfil()
const perfilValue = computed({
  get: () => perfil.value,
  set: v => {
    perfil.setPerfil(v)
  }
})

// Lógica minimalista para exibição dos menus conforme perfil
const isPainelVisible = computed(() => true)
const isProcessosVisible = computed(() => perfil.value === 'SEDOC' || perfil.value === 'GESTOR')
const isMapasVisible = computed(() => perfil.value === 'SEDOC' || perfil.value === 'GESTOR' || perfil.value === 'CHEFE')
const isDiagnosticoVisible = computed(() => perfil.value === 'SEDOC' || perfil.value === 'GESTOR')
const isAtribuicoesVisible = computed(() => perfil.value === 'SEDOC')
const isAtividadesVisible = computed(() => perfil.value === 'CHEFE' || perfil.value === 'GESTOR')
</script>