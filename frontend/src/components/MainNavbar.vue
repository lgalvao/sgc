<template>
  <nav class="navbar navbar-expand-lg navbar-dark bg-dark border-bottom">
    <div class="container">
      <router-link
        class="navbar-brand"
        to="/painel"
      >
        SGC
      </router-link>
      <button
        aria-controls="navbarNav"
        aria-expanded="false"
        aria-label="Alternar navegação"
        class="navbar-toggler"
        data-bs-target="#navbarNav"
        data-bs-toggle="collapse"
        type="button"
      >
        <span class="navbar-toggler-icon" />
      </button>

      <div
        id="navbarNav"
        class="collapse navbar-collapse"
      >
        <ul class="navbar-nav me-auto mb-2 mb-lg-0 left-nav">
          <li class="nav-item">
            <a
              class="nav-link"
              href="#"
              @click.prevent="navigateFromNavbar('/painel')"
            >
              <i class="bi bi-house-door" /> Painel
            </a>
          </li>
          <li class="nav-item">
            <a
              class="nav-link"
              href="#"
              @click.prevent="navigateFromNavbar(`/unidade/${perfilStore.unidadeSelecionada}`)"
            >
              <i class="bi bi-person" /> Minha unidade
            </a>
          </li>
          <li class="nav-item">
            <a
              class="nav-link"
              href="#"
              @click.prevent="navigateFromNavbar('/relatorios')"
            >
              <i class="bi bi-bar-chart-line" /> Relatórios
            </a>
          </li>
          <li class="nav-item">
            <a
              class="nav-link"
              href="#"
              @click.prevent="navigateFromNavbar('/historico')"
            >
              <i class="bi bi-clock-history" /> Histórico
            </a>
          </li>
        </ul>

        <ul class="navbar-nav align-items-center">
          <li class="nav-item me-3 d-flex align-items-center">
            <i class="bi bi-person-circle me-2 fs-5" />
            <span class="nav-link">
              {{ perfilSelecionado }} - {{ unidadeSelecionada }}
            </span>
          </li>

          <li
            v-if="perfilStore.perfilSelecionado === 'ADMIN'"
            class="nav-item me-2"
          >
            <a
              class="nav-link"
              href="#"
              title="Configurações do sistema"
              @click.prevent="navigateFromNavbar('/configuracoes')"
            >
              <i class="bi bi-gear fs-5" />
            </a>
          </li>

          <li class="nav-item">
            <router-link
              class="nav-link"
              title="Sair"
              to="/login"
            >
              <i class="bi bi-box-arrow-right fs-5" />
            </router-link>
          </li>
        </ul>
      </div>
    </div>
  </nav>
</template>

<script lang="ts" setup>
import {useRouter} from 'vue-router';
import {usePerfilStore} from '@/stores/perfil';
import {usePerfil} from '@/composables/usePerfil';

const router = useRouter();
const perfilStore = usePerfilStore();

const {perfilSelecionado, unidadeSelecionada} = usePerfil();



function navigateFromNavbar(path: string) {
  sessionStorage.setItem('cameFromNavbar', '1');
  router.push(path);
}

defineExpose({
  navigateFromNavbar,
})
</script>

<style scoped>
.left-nav .nav-link {
  color: rgba(255, 255, 255, 0.85) !important;
}

.left-nav .nav-link:hover {
  color: rgba(255, 255, 255, 1) !important;
}
</style>
