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
            <span v-if="!isEditingProfile && servidorLogado" class="nav-link" style="cursor: pointer;"
                  @click="startEditingProfile">
              {{ perfilSelecionado }} - {{ unidadeSelecionada }}
            </span>

            <select v-else
                    ref="profileSelect"
                    :value="selectedProfileKey"
                    class="form-select form-select-sm"
                    @blur="stopEditingProfile"
                    @change="handleProfileChange">
              <option v-for="perfil in perfisDisponiveis" :key="perfil.id" :value="perfil.id">
                {{ perfil.perfil }} - {{ perfil.unidade }} ({{ perfil.nome }})
              </option>
            </select>
          </li>

          <li v-if="perfilStore.perfilSelecionado === 'ADMIN'" class="nav-item me-2">
            <a class="nav-link" href="#" title="Configurações do sistema"
               @click.prevent="navigateFromNavbar('/configuracoes')">
              <i class="bi bi-gear fs-5"></i>
            </a>
          </li>

          <li class="nav-item">
            <router-link class="nav-link" title="Sair" to="/login">
              <i class="bi bi-box-arrow-right fs-5"></i>
            </router-link>
          </li>
        </ul>
      </div>
    </div>
  </nav>
</template>

<script lang="ts" setup>
import {computed, nextTick, ref} from 'vue'
import {useRouter} from 'vue-router'
import {usePerfilStore} from '@/stores/perfil'
import {useServidoresStore} from '@/stores/servidores'
import {usePerfil} from '@/composables/usePerfil'

const router = useRouter()
const perfilStore = usePerfilStore()
const servidoresStore = useServidoresStore()

const {servidorLogado, perfilSelecionado, unidadeSelecionada, getPerfisDoServidor} = usePerfil()

const isEditingProfile = ref(false)
const profileSelect = ref<HTMLSelectElement | null>(null)

const perfisDisponiveis = computed(() => {
  return servidoresStore.servidores.flatMap(servidor => {
    const pares = getPerfisDoServidor(servidor.id)
    return pares.map(par => ({
      id: `${servidor.id}-${par.perfil}-${par.unidade}`,
      servidorId: servidor.id,
      nome: servidor.nome,
      perfil: par.perfil,
      unidade: par.unidade,
    }))
  })
})

const selectedProfileKey = computed(() => {
  if (!servidorLogado.value || !perfilSelecionado.value || !unidadeSelecionada.value) return ''
  return `${perfilStore.servidorId}-${perfilSelecionado.value}-${unidadeSelecionada.value}`
})

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
  const selectedKey = (event.target as HTMLSelectElement).value
  const selectedPerfil = perfisDisponiveis.value.find(p => p.id === selectedKey)

  if (selectedPerfil) {
    perfilStore.setServidorId(selectedPerfil.servidorId)
    perfilStore.setPerfilUnidade(selectedPerfil.perfil, selectedPerfil.unidade)
    router.push('/painel')
  }
  stopEditingProfile()
}

function navigateFromNavbar(path: string) {
  sessionStorage.setItem('cameFromNavbar', '1')
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
