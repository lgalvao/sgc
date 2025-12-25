<template>
  <BNavbar toggleable="lg" variant="dark" bg-variant="dark" class="navbar-dark bg-dark border-bottom sticky-top">
    <BNavbarBrand to="/painel" class="fw-bold fs-5 me-4 text-white">
      SGC
    </BNavbarBrand>

    <BNavbarToggle target="nav-collapse" />

    <BCollapse id="nav-collapse" is-nav>
      <BNavbarNav class="me-auto">
        <BNavItem to="/painel" @click="setNavbarNavigation">
          <i class="bi bi-house-door me-1"/> Painel
        </BNavItem>
        <BNavItem :to="`/unidade/${perfilStore.unidadeSelecionada}`" @click="setNavbarNavigation">
          <i class="bi bi-person me-1"/> Minha unidade
        </BNavItem>
        <BNavItem to="/relatorios" @click="setNavbarNavigation">
          <i class="bi bi-bar-chart-line me-1"/> Relatórios
        </BNavItem>
        <BNavItem to="/historico" @click="setNavbarNavigation">
          <i class="bi bi-clock-history me-1"/> Histórico
        </BNavItem>
      </BNavbarNav>

      <!-- Right aligned nav items -->
      <BNavbarNav class="ms-auto">
        <!-- Divisor visível apenas quando colapsado -->
        <div class="d-lg-none border-top border-secondary my-2 w-100"></div>

        <BNavItem 
          v-b-tooltip.hover.bottom="{ title: perfilStore.usuarioNome || 'Usuário', disabled: isMobile }"
          class="me-2 user-profile-item"
        >
          <div class="d-flex align-items-center">
            <i class="bi bi-person-circle me-2"/>
            <div class="user-info-text">
              <span class="d-lg-inline">{{ perfilSelecionado }} - {{ unidadeSelecionada }}</span>
            </div>
          </div>
        </BNavItem>
        
        <BNavItem 
          v-if="perfilStore.perfilSelecionado === 'ADMIN'" 
          class="me-lg-1"
          title="Configurações do sistema"
          aria-label="Configurações do sistema"
          data-testid="btn-configuracoes"
          to="/configuracoes"
          @click="setNavbarNavigation"
        >
          <i class="bi bi-gear me-lg-0 me-1" aria-hidden="true"/>
          <span class="d-lg-none">Configurações</span>
        </BNavItem>

        <BNavItem 
          class="me-lg-0"
          title="Sair"
          aria-label="Sair"
          data-testid="btn-logout"
          @click.prevent="handleLogout"
        >
          <i class="bi bi-box-arrow-right me-lg-0 me-1" aria-hidden="true"/>
          <span class="d-lg-none">Sair</span>
        </BNavItem>
      </BNavbarNav>
    </BCollapse>
  </BNavbar>
</template>

<script lang="ts" setup>
import {BCollapse, BNavbar, BNavbarBrand, BNavbarNav, BNavbarToggle, BNavItem, vBTooltip} from "bootstrap-vue-next";
import {computed, onMounted, onUnmounted, ref} from "vue";
import {useRouter} from "vue-router";
import {usePerfil} from "@/composables/usePerfil";
import {usePerfilStore} from "@/stores/perfil";

const router = useRouter();
const perfilStore = usePerfilStore();

const {perfilSelecionado, unidadeSelecionada} = usePerfil();

// Controle reativo de largura para desabilitar tooltips no mobile
const windowWidth = ref(window.innerWidth);
const updateWidth = () => { windowWidth.value = window.innerWidth; };
onMounted(() => window.addEventListener('resize', updateWidth));
onUnmounted(() => window.removeEventListener('resize', updateWidth));
const isMobile = computed(() => windowWidth.value < 992);

function setNavbarNavigation() {
  sessionStorage.setItem("cameFromNavbar", "1");
}

function handleLogout() {
  perfilStore.logout();
  router.push("/login");
}

defineExpose({
  setNavbarNavigation,
});
</script>

<style scoped>
/* Ajuste de ícones no navbar */
.nav-link i {
  vertical-align: middle;
  color: inherit !important;
}

/* Garantir que o texto dos links seja branco quando usar navbar-dark */
:deep(.navbar-dark .navbar-nav .nav-link) {
  color: rgba(255, 255, 255, 0.75) !important;
}

:deep(.navbar-dark .navbar-nav .nav-link:hover),
:deep(.navbar-dark .navbar-nav .nav-link:focus) {
  color: rgba(255, 255, 255, 1) !important;
}

:deep(.navbar-dark .navbar-nav .nav-link.active) {
  color: #fff !important;
}

/* Estilo específico para o item de perfil para não parecer um link clicável se não houver ação */
.user-profile-item :deep(.nav-link) {
  cursor: default;
  color: rgba(255, 255, 255, 0.9) !important; /* Mais claro que os outros links para destaque */
}

@media (max-width: 991.98px) {
  .user-profile-item {
    margin-bottom: 0.5rem;
  }
}
</style>
