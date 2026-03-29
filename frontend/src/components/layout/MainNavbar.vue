<template>
  <BNavbar bg-variant="dark" class="navbar-dark bg-dark border-bottom sticky-top" toggleable="lg" variant="dark">
    <BNavbarBrand class="fw-bold fs-5 me-4 text-white" to="/painel">
      {{ TEXTOS.comum.NOME_SISTEMA }}
    </BNavbarBrand>

    <BNavbarToggle target="nav-collapse"/>

    <BCollapse id="nav-collapse" is-nav>
      <BNavbarNav class="me-auto">
        <BNavItem to="/painel" data-testid="nav-link-painel">
          <i aria-hidden="true" class="bi bi-house-door me-1"/> {{ TEXTOS.comum.MENU_PAINEL }}
        </BNavItem>
        <BNavItem :to="linkUnidade">
          <i :class="iconUnidade" aria-hidden="true"/> {{ labelUnidade }}
        </BNavItem>
        <BNavItem to="/relatorios">
          <i aria-hidden="true" class="bi bi-bar-chart-line me-1"/> {{ TEXTOS.comum.MENU_RELATORIOS }}
        </BNavItem>
        <BNavItem to="/historico">
          <i aria-hidden="true" class="bi bi-clock-history me-1"/> {{ TEXTOS.comum.MENU_HISTORICO }}
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
              <span class="d-lg-inline">{{ isAdmin ? 'ADMIN' : `${perfilSelecionado} - ${unidadeSelecionada}` }}</span>
            </div>
          </div>
        </BNavItem>

        <BNavItem
            v-if="isAdmin"
            class="me-lg-1"
            data-testid="btn-configuracoes"
            title="Configurações"
            to="/configuracoes"
        >
          <template #default>
            <span class="visually-hidden">Configurações</span>
            <i aria-hidden="true" class="bi bi-sliders me-lg-0 me-1"/>
            <span aria-hidden="true" class="d-lg-none">Configurações</span>
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
            <span class="visually-hidden">Administradores</span>
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
import {TEXTOS} from "@/constants/textos";

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
const labelUnidade = computed(() => podeAcessarTodasUnidades.value ? TEXTOS.comum.MENU_UNIDADES : TEXTOS.comum.MENU_MINHA_UNIDADE);
const iconUnidade = computed(() => podeAcessarTodasUnidades.value ? 'bi bi-diagram-3 me-1' : 'bi bi-person me-1');
const linkUnidade = computed(() => podeAcessarTodasUnidades.value ? '/unidades' : `/unidade/${perfilStore.unidadeSelecionada}`);

function handleLogout() {
  perfilStore.logout();
  router.push("/login");
}
</script>

<style scoped>
.navbar {
  background: rgba(15, 23, 42, 0.9) !important; /* Slate-900 com transparência */
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  padding: 0.75rem 1.5rem;
  box-shadow: var(--sombra-md);
}

.navbar-brand {
  letter-spacing: -0.02em;
  font-weight: 700;
}

/* Ajuste de ícones no navbar */
.nav-link {
  font-weight: 500;
  display: flex !important;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 1rem !important;
  border-radius: 0.5rem;
  transition: all 0.2s ease;
  color: rgba(255, 255, 255, 0.85) !important; /* Aumentado de 0.7 para 0.85 para melhor contraste */
}


.nav-link i {
  font-size: 1.1rem;
  color: inherit !important;
}

.nav-link:hover {
  color: #fff !important;
  background: rgba(255, 255, 255, 0.1);
}

.nav-link.active {
  color: #fff !important;
  background: rgba(255, 255, 255, 0.15);
}

/* Estilo específico para o item de perfil */
.user-profile-item :deep(.nav-link) {
  cursor: default;
  background: rgba(255, 255, 255, 0.05);
  margin-right: 0.5rem;
}

.user-profile-item :deep(.nav-link:hover) {
  background: rgba(255, 255, 255, 0.05);
}

.user-info-text {
  font-size: 0.875rem;
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

@media (max-width: 992px) {
  .navbar {
    padding: 1rem;
  }
  
  .nav-link {
    margin-bottom: 0.25rem;
  }
}
</style>

