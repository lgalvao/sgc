<template>
  <nav class="navbar navbar-expand-lg navbar-dark bg-primary">
    <div class="container-fluid">
      <router-link class="navbar-brand" to="/painel">SGC</router-link>
      <button class="navbar-toggler" type="button" 
              data-bs-toggle="collapse" 
              data-bs-target="#navbarNav" 
              aria-controls="navbarNav" 
              aria-expanded="false" 
              aria-label="Alternar navegação">
        <span class="navbar-toggler-icon"></span>
      </button>
      
      <div class="collapse navbar-collapse" id="navbarNav">
        <ul class="navbar-nav me-auto mb-2 mb-lg-0">
          <li class="nav-item" v-if="isPainelVisible">
            <router-link class="nav-link" to="/painel">Painel</router-link>
          </li>
          <li class="nav-item" v-if="isProcessosVisible">
            <router-link class="nav-link" to="/processos">Processos</router-link>
          </li>
          <li class="nav-item" v-if="isMapasVisible">
            <router-link class="nav-link" to="/mapas">Mapas</router-link>
          </li>
          <li class="nav-item" v-if="isDiagnosticoVisible">
            <router-link class="nav-link" to="/diagnostico">Diagnóstico</router-link>
          </li>
          <li class="nav-item" v-if="isAtribuicoesVisible">
            <router-link class="nav-link" to="/atribuicoes">Atribuições</router-link>
          </li>
          <li class="nav-item" v-if="isAtividadesVisible">
            <router-link class="nav-link" to="/processos/1/unidade/1/atividades">Atividades</router-link>
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
import { computed } from 'vue'
import { usePerfil } from '../composables/usePerfil'
const perfil = usePerfil()
const perfilValue = computed({
  get: () => perfil.value,
  set: v => { perfil.setPerfil(v) }
})

// Lógica minimalista para exibição dos menus conforme perfil
const isPainelVisible = computed(() => true)
const isProcessosVisible = computed(() => perfil.value === 'SEDOC' || perfil.value === 'GESTOR')
const isMapasVisible = computed(() => perfil.value === 'SEDOC' || perfil.value === 'GESTOR' || perfil.value === 'CHEFE')
const isDiagnosticoVisible = computed(() => perfil.value === 'SEDOC' || perfil.value === 'GESTOR')
const isAtribuicoesVisible = computed(() => perfil.value === 'SEDOC')
const isAtividadesVisible = computed(() => perfil.value === 'CHEFE' || perfil.value === 'GESTOR')
</script> 