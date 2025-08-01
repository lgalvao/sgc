<template>
  <nav class="navbar navbar-expand-lg navbar-light bg-light border-bottom">
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
            <router-link class="nav-link" to="/painel">
              <i class="bi bi-house-door"></i> Painel
            </router-link>
          </li>
          <li class="nav-item">
            <router-link class="nav-link" :to="`/unidade/${unidadeSelecionada}`">
              <i class="bi bi-person"></i> Minha unidade
            </router-link>
          </li>
          <li class="nav-item">
            <router-link class="nav-link" to="/relatorios">
              <i class="bi bi-bar-chart-line"></i> Relatórios
            </router-link>
          </li>
          <li class="nav-item">
            <router-link class="nav-link" to="/historico">
              <i class="bi bi-clock-history"></i> Histórico
            </router-link>
          </li>
        </ul>

        <ul class="navbar-nav align-items-center">
          <li class="nav-item me-3 d-flex align-items-center">
            <i class="bi bi-person-circle me-2 fs-5"></i>
            <span v-if="!isEditingProfile && servidorLogado" @click="startEditingProfile" class="nav-link" style="cursor: pointer;">
              {{ perfilSelecionado }} - {{ unidadeSelecionada }}
            </span>
            <select v-else
                    ref="profileSelect"
                    :value="servidorLogado?.id"
                    @change="handleProfileChange"
                    @blur="stopEditingProfile"
                    class="form-select form-select-sm">
              <option v-for="servidor in servidoresComPerfil" :key="servidor.id" :value="servidor.id">
                {{ servidor.perfil }} - {{ servidor.unidade }} ({{ servidor.nome }})
              </option>
            </select>
          </li>

          <li class="nav-item">
            <router-link class="btn btn-outline-secondary btn-sm" to="/login" title="Sair">
              <i class="bi bi-box-arrow-right"></i>
            </router-link>
          </li>
        </ul>
      </div>
    </div>
  </nav>
</template>

<script setup>
import {ref, nextTick} from 'vue'
import {usePerfilStore} from '../stores/perfil'
import {usePerfil} from '../composables/usePerfil'

const { servidorLogado, servidoresComPerfil, perfilSelecionado, unidadeSelecionada } = usePerfil()
const perfilStore = usePerfilStore()
const isEditingProfile = ref(false)
const profileSelect = ref(null)

const startEditingProfile = () => {
  isEditingProfile.value = true
  nextTick(() => {
    profileSelect.value?.focus()
  })
}

const stopEditingProfile = () => {
  isEditingProfile.value = false
}

const handleProfileChange = (event) => {
  const selectedId = Number(event.target.value);
  const selectedServidor = servidoresComPerfil.value.find(s => s.id === selectedId);
  if (selectedServidor) {
    perfilStore.setServidorId(selectedId);
    perfilStore.setPerfilUnidade(selectedServidor.perfil, selectedServidor.unidade);
  }
  stopEditingProfile();
}

</script>