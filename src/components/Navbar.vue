<template>
  <nav class="navbar navbar-expand-lg navbar-dark bg-dark border-bottom">
    <div class="container">
      <router-link class="navbar-brand" to="/painel">SGC</router-link>
      <button aria-controls="navbarNav" aria-expanded="false"
              aria-label="Alternar navegação" class="navbar-toggler" data-bs-target="#navbarNav"
              data-bs-toggle="collapse" type="button">
        <span class="navbar-toggler-icon"></span>
      </button>

      <div id="navbarNav" class="collapse navbar-collapse">
        <ul class="navbar-nav me-auto mb-2 mb-lg-0 left-nav">
          <li class="nav-item">
            <a class="nav-link" href="#" @click.prevent="navigateFromNavbar('/painel')">
              <i class="bi bi-house-door"></i> Painel
            </a>
          </li>
          <li class="nav-item">
            <a class="nav-link" href="#" @click.prevent="navigateFromNavbar(`/unidade/${unidadeSelecionada}`)">
              <i class="bi bi-person"></i> Minha unidade
            </a>
          </li>
          <li class="nav-item">
            <a class="nav-link" href="#" @click.prevent="navigateFromNavbar('/relatorios')">
              <i class="bi bi-bar-chart-line"></i> Relatórios
            </a>
          </li>
          <li class="nav-item">
            <a class="nav-link" href="#" @click.prevent="navigateFromNavbar('/historico')">
              <i class="bi bi-clock-history"></i> Histórico
            </a>
          </li>
        </ul>

        <ul class="navbar-nav align-items-center">
          <li class="nav-item me-3 d-flex align-items-center">
            <i class="bi bi-person-circle me-2 fs-5"></i>
            <span v-if="!isEditingProfile && servidorLogado" @click="startEditingProfile" class="nav-link"
                  style="cursor: pointer;">
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

          <li class="nav-item me-2">
            <a class="nav-link" href="#" @click.prevent="navigateFromNavbar('/configuracoes')" title="Configurações do sistema">
              <i class="bi bi-gear fs-5"></i>
            </a>
          </li>

          <li class="nav-item">
            <router-link class="nav-link" to="/login" title="Sair">
              <i class="bi bi-box-arrow-right fs-5"></i>
            </router-link>
          </li>
        </ul>
      </div>
    </div>
  </nav>
</template>

<script setup lang="ts">
import {nextTick, ref} from 'vue'
import {useRouter} from 'vue-router'
import {usePerfilStore} from '@/stores/perfil'
import {usePerfil} from '@/composables/usePerfil'

const router = useRouter()

const {servidorLogado, servidoresComPerfil, perfilSelecionado, unidadeSelecionada} = usePerfil()
const perfilStore = usePerfilStore()
const isEditingProfile = ref(false)
const profileSelect = ref<HTMLSelectElement | null>(null)

const startEditingProfile = () => {
  isEditingProfile.value = true
  nextTick(() => {
    profileSelect.value?.focus()
  })
}

const stopEditingProfile = () => {
  isEditingProfile.value = false
}

const handleProfileChange = (event: Event) => {
  const selectedId = Number((event.target as HTMLSelectElement).value);
  const selectedServidor = servidoresComPerfil.value.find(s => s.id === selectedId);
  if (selectedServidor) {
    perfilStore.setServidorId(selectedId);
    perfilStore.setPerfilUnidade(selectedServidor.perfil, selectedServidor.unidade);
    router.push('/painel'); // Redireciona para o Painel
  }
  stopEditingProfile();
}

function navigateFromNavbar(path: string) {
  try { sessionStorage.setItem('cameFromNavbar', '1') } catch {}
  router.push(path)
}

</script>

<style scoped>
.left-nav .nav-link {
  color: rgba(255, 255, 255, 0.85) !important;
}

.left-nav .nav-link:hover {
  color: rgba(255, 255, 255, 1) !important;
}
</style>