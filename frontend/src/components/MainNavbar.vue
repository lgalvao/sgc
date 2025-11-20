<template>
  <BNavbar
    class="navbar-expand-lg navbar-dark bg-dark border-bottom"
    toggleable="lg"
  >
    <BContainer>
      <BNavbarBrand to="/painel">
        SGC
      </BNavbarBrand>
      <BNavbarToggle target="navbarNav" />

      <BCollapse
        id="navbarNav"
        is-nav
      >
        <BNavbarNav class="me-auto mb-2 mb-lg-0 left-nav">
          <BNavItem
            href="#"
            @click.prevent="navigateFromNavbar('/painel')"
          >
            <i class="bi bi-house-door" /> Painel
          </BNavItem>
          <BNavItem
            href="#"
            @click.prevent="navigateFromNavbar(`/unidade/${perfilStore.unidadeSelecionada}`)"
          >
            <i class="bi bi-person" /> Minha unidade
          </BNavItem>
          <BNavItem
            href="#"
            @click.prevent="navigateFromNavbar('/relatorios')"
          >
            <i class="bi bi-bar-chart-line" /> Relatórios
          </BNavItem>
          <BNavItem
            href="#"
            @click.prevent="navigateFromNavbar('/historico')"
          >
            <i class="bi bi-clock-history" /> Histórico
          </BNavItem>
        </BNavbarNav>

        <BNavbarNav class="align-items-center">
          <BNavItem class="me-3 d-flex align-items-center">
            <i class="bi bi-person-circle me-2 fs-5" />
            <span class="nav-link">
              {{ perfilSelecionado }} - {{ unidadeSelecionada }}
            </span>
          </BNavItem>

          <BNavItem
            v-if="perfilStore.perfilSelecionado === 'ADMIN'"
            class="me-2"
            href="#"
            title="Configurações do sistema"
            data-testid="btn-configuracoes"
            @click.prevent="navigateFromNavbar('/configuracoes')"
          >
            <i class="bi bi-gear fs-5" />
          </BNavItem>

          <BNavItem
            title="Sair"
            to="/login"
          >
            <i class="bi bi-box-arrow-right fs-5" />
          </BNavItem>
        </BNavbarNav>
      </BCollapse>
    </BContainer>
  </BNavbar>
</template>

<script lang="ts" setup>
import {
  BCollapse,
  BContainer,
  BNavbar,
  BNavbarBrand,
  BNavbarNav,
  BNavbarToggle,
  BNavItem,
} from 'bootstrap-vue-next';
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
