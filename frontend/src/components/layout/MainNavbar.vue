<template>
  <BNavbar bg-variant="dark" class="navbar-dark bg-dark border-bottom sticky-top" toggleable="lg" variant="dark">
    <BNavbarBrand class="fw-bold fs-5 me-4 text-white" to="/painel">
      SGC
    </BNavbarBrand>

    <BNavbarToggle target="nav-collapse"/>

    <BCollapse id="nav-collapse" is-nav>
      <BNavbarNav class="me-auto">
        <BNavItem to="/painel">
          <i aria-hidden="true" class="bi bi-house-door me-1"/> Painel
        </BNavItem>
        <BNavItem :to="linkUnidade">
          <i :class="iconUnidade" aria-hidden="true"/> {{ labelUnidade }}
        </BNavItem>
        <BNavItem to="/relatorios">
          <i aria-hidden="true" class="bi bi-bar-chart-line me-1"/> Relatórios
        </BNavItem>
        <BNavItem to="/historico">
          <i aria-hidden="true" class="bi bi-clock-history me-1"/> Histórico
        </BNavItem>
      </BNavbarNav>

      <!-- Right aligned nav items -->
      <BNavbarNav class="ms-auto">

        <BNavItem
            v-b-tooltip.hover.bottom="{ title: perfilStore.usuarioNome || 'Usuário', disabled: isMobile }"
            class="me-2 user-profile-item"
        >
          <div class="d-flex align-items-center">
            <i aria-hidden="true" class="bi bi-person-circle me-2"/>
            <div class="user-info-text">
              <span class="d-lg-inline">{{ perfilSelecionado }} - {{ unidadeSelecionada }}</span>
            </div>
          </div>
        </BNavItem>

        <BNavItem
            v-if="isAdmin"
            class="me-lg-1"
            data-testid="btn-configuracoes"
            title="Parâmetros do sistema"
            to="/parametros"
        >
          <template #default>
            <span class="visually-hidden">Parâmetros do sistema</span>
            <i aria-hidden="true" class="bi bi-sliders me-lg-0 me-1"/>
            <span aria-hidden="true" class="d-lg-none">Parâmetros</span>
          </template>
        </BNavItem>

        <BNavItem
            v-if="isAdmin"
            class="me-lg-1"
            data-testid="btn-administradores"
            title="Administradores do sistema"
            to="/administradores"
        >
          <template #default>
            <span class="visually-hidden">Administradores do sistema</span>
            <i aria-hidden="true" class="bi bi-people me-lg-0 me-1"/>
            <span aria-hidden="true" class="d-lg-none">Administradores</span>
          </template>
        </BNavItem>

        <BNavItem
            class="me-lg-0"
            data-testid="btn-logout"
            title="Sair"
            @click.prevent="handleLogout"
        >
          <template #default>
            <span class="visually-hidden">Sair</span>
            <i aria-hidden="true" class="bi bi-box-arrow-right me-lg-0 me-1"/>
            <span aria-hidden="true" class="d-lg-none">Sair</span>
          </template>
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

const {perfilSelecionado, unidadeSelecionada, podeAcessarTodasUnidades, isAdmin} = usePerfil();

// Controle reativo de largura para desabilitar tooltips no mobile
const windowWidth = ref(window.innerWidth);
const updateWidth = () => {
  windowWidth.value = window.innerWidth;
};
onMounted(() => window.addEventListener('resize', updateWidth));
onUnmounted(() => window.removeEventListener('resize', updateWidth));
const isMobile = computed(() => windowWidth.value < 992);

// Para ADMIN: mostra "Unidades" e direciona para a árvore completa
// Para outros perfis: mostra "Minha unidade" e direciona para unidade do usuário
const labelUnidade = computed(() => podeAcessarTodasUnidades.value ? 'Unidades' : 'Minha unidade');
const iconUnidade = computed(() => podeAcessarTodasUnidades.value ? 'bi bi-diagram-3 me-1' : 'bi bi-person me-1');
const linkUnidade = computed(() => podeAcessarTodasUnidades.value ? '/unidades' : `/unidade/${perfilStore.unidadeSelecionada}`);

function handleLogout() {
  perfilStore.logout();
  router.push("/login");
}
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

@media (max-width: 992px) {
  .user-profile-item {
    margin-bottom: 0.5rem;
  }
}
</style>
