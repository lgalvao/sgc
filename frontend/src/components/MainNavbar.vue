<template>
  <nav class="navbar navbar-expand navbar-dark bg-dark border-bottom">
    <div class="container-fluid">
      <router-link to="/painel" class="navbar-brand fw-bold fs-5 me-4">
        SGC
      </router-link>

      <ul class="navbar-nav me-auto">
        <li class="nav-item">
          <a href="#" class="nav-link" @click.prevent="navigateFromNavbar('/painel')">
            <i class="bi bi-house-door"/> Painel
          </a>
        </li>
        <li class="nav-item">
          <a href="#" class="nav-link" @click.prevent="navigateFromNavbar(`/unidade/${perfilStore.unidadeSelecionada}`)">
            <i class="bi bi-person"/> Minha unidade
          </a>
        </li>
        <li class="nav-item">
          <a href="#" class="nav-link" @click.prevent="navigateFromNavbar('/relatorios')">
            <i class="bi bi-bar-chart-line"/> Relatórios
          </a>
        </li>
        <li class="nav-item">
          <a href="#" class="nav-link" @click.prevent="navigateFromNavbar('/historico')">
            <i class="bi bi-clock-history"/> Histórico
          </a>
        </li>
      </ul>

      <ul class="navbar-nav align-items-center">
        <li class="nav-item me-3">
          <span class="nav-link">
            <i class="bi bi-person-circle me-2"/>
            {{ perfilSelecionado }} - {{ unidadeSelecionada }}
          </span>
        </li>
        <li v-if="perfilStore.perfilSelecionado === 'ADMIN'" class="nav-item me-2">
          <a 
            href="#" 
            class="nav-link" 
            title="Configurações do sistema"
            aria-label="Configurações do sistema"
            data-testid="btn-configuracoes"
            @click.prevent="navigateFromNavbar('/configuracoes')"
          >
            <i class="bi bi-gear" aria-hidden="true"/>
          </a>
        </li>
        <li class="nav-item">
          <a 
            href="#" 
            class="nav-link" 
            title="Sair"
            aria-label="Sair"
            data-testid="btn-logout"
            @click.prevent="handleLogout"
          >
            <i class="bi bi-box-arrow-right" aria-hidden="true"/>
          </a>
        </li>
      </ul>
    </div>
  </nav>
</template>

<script lang="ts" setup>
import {useRouter} from "vue-router";
import {usePerfil} from "@/composables/usePerfil";
import {usePerfilStore} from "@/stores/perfil";

const router = useRouter();
const perfilStore = usePerfilStore();

const {perfilSelecionado, unidadeSelecionada} = usePerfil();

function navigateFromNavbar(path: string) {
  sessionStorage.setItem("cameFromNavbar", "1");
  router.push(path);
}

function handleLogout() {
  perfilStore.logout();
  router.push("/login");
}

defineExpose({
  navigateFromNavbar,
});
</script>

<style scoped>
.nav-link {
  color: rgba(255, 255, 255, 0.85) !important;
  white-space: nowrap;
}

.nav-link:hover {
  color: rgba(255, 255, 255, 1) !important;
}

.nav-link i {
  margin-right: 0.25rem;
}
</style>
