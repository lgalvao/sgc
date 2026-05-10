<template>
  <BNavbar class="border-bottom navbar-principal-sgc navbar-dark" toggleable="lg">
    <BNavbarBrand class="fw-bold fs-5 me-4 navbar-principal-sgc__marca" to="/painel">
      {{ TEXTOS.comum.NOME_SISTEMA }}
    </BNavbarBrand>

    <BNavbarToggle target="nav-collapse"/>

    <BCollapse id="nav-collapse" is-nav>
      <BNavbarNav class="me-auto">
        <BNavItem data-testid="nav-link-painel" to="/painel">
          <i aria-hidden="true" class="bi bi-house-door me-1"/> {{ TEXTOS.comum.MENU_PAINEL }}
        </BNavItem>
        <BNavItem :to="linkUnidade">
          <i :class="iconUnidade" aria-hidden="true"/> {{ labelUnidade }}
        </BNavItem>
        <BNavItem v-if="podeVerRelatorios" to="/relatorios">
          <i aria-hidden="true" class="bi bi-bar-chart-line me-1"/> {{ TEXTOS.comum.MENU_RELATORIOS }}
        </BNavItem>
        <BNavItem to="/historico">
          <i aria-hidden="true" class="bi bi-clock-history me-1"/> {{ TEXTOS.comum.MENU_HISTORICO }}
        </BNavItem>
      </BNavbarNav>

      <BNavbarNav class="ms-auto align-items-lg-center">
        <BNavItem
            v-b-tooltip.hover.bottom="{ title: perfilStore.usuarioNome || 'Usuário', disabled: isMobile }"
            class="me-2 user-profile-item"
        >
          <div class="d-flex align-items-center navbar-principal-sgc__texto-secundario">
            <i aria-hidden="true" class="bi bi-person-circle me-2"/>
            <div class="user-info-text">
              <span class="d-lg-inline">{{ isAdmin ? 'ADMIN' : `${perfilSelecionado} - ${unidadeSelecionada}` }}</span>
            </div>
          </div>
        </BNavItem>

        <BNavItem
            v-if="isAdmin"
            class="me-lg-1"
            data-testid="nav-link-notificacoes"
            title="Notificações"
            to="/administracao/notificacoes"
        >
          <template #default>
            <span class="visually-hidden">{{ TEXTOS.comum.MENU_NOTIFICACOES }}</span>
            <i aria-hidden="true" class="bi bi-envelope-exclamation me-lg-0 me-1"/>
            <span aria-hidden="true" class="d-lg-none">{{ TEXTOS.comum.MENU_NOTIFICACOES }}</span>
          </template>
        </BNavItem>

        <BNavItem
            v-if="mostrarMenuConfiguracoes"
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
            v-if="mostrarMenuAdministradores"
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

        <BNavItemDropdown
            v-if="isAdmin"
            class="me-lg-1"
            data-testid="dropdown-acoes-especiais"
            no-caret
            right
        >
          <template #button-content>
            <span class="visually-hidden">Ações Especiais</span>
            <i aria-hidden="true" class="bi bi-gear me-lg-0 me-1" title="Ações Especiais"/>
            <span aria-hidden="true" class="d-lg-none">Ações Especiais</span>
          </template>
          <BDropdownItem
              data-testid="btn-nav-feedbacks"
              to="/administracao/feedbacks"
          >
            <i aria-hidden="true" class="bi bi-chat-left-text me-2"></i> {{ TEXTOS.comum.MENU_FEEDBACKS }}
          </BDropdownItem>
          <BDropdownItem
              data-testid="btn-nav-limpeza-processos"
              to="/administracao/limpeza-processos"
              variant="danger"
          >
            <span class="text-danger">
              <i aria-hidden="true" class="bi bi-trash me-2"></i> {{ TEXTOS.administracao.BOTAO_LIMPEZA_PROCESSOS }}
            </span>
          </BDropdownItem>
        </BNavItemDropdown>

        <BNavItem
            class="me-lg-1"
            data-testid="btn-toggle-tema"
            :title="tituloTema"
            @click.prevent="alternarTema"
        >
          <template #default>
            <span class="visually-hidden">{{ tituloTema }}</span>
            <i aria-hidden="true" :class="`${iconeTema} me-lg-0 me-1`"/>
            <span aria-hidden="true" class="d-lg-none">Tema</span>
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
import {
  BCollapse,
  BDropdownItem,
  BNavbar,
  BNavbarBrand,
  BNavbarNav,
  BNavbarToggle,
  BNavItem,
  BNavItemDropdown,
  vBTooltip
} from "bootstrap-vue-next";
import {computed, onMounted, onUnmounted, ref} from "vue";
import {useRouter} from "vue-router";
import {usePerfil} from "@/composables/usePerfil";
import {useTemaPreferencia} from "@/composables/useTemaPreferencia";
import {usePerfilStore} from "@/stores/perfil";
import {TEXTOS} from "@/constants/textos";

const router = useRouter();
const perfilStore = usePerfilStore();
const {getTemaEscuro, setTemaEscuro} = useTemaPreferencia();

const {
  perfilSelecionado,
  unidadeSelecionada,
  isAdmin,
  podeVerRelatorios,
  mostrarArvoreCompletaUnidades,
  mostrarMenuConfiguracoes,
  mostrarMenuAdministradores
} = usePerfil();
const windowWidth = ref(window.innerWidth);
const updateWidth = () => {
  windowWidth.value = window.innerWidth;
};
onMounted(() => window.addEventListener('resize', updateWidth));
onUnmounted(() => window.removeEventListener('resize', updateWidth));
const isMobile = computed(() => windowWidth.value < 992);
const labelUnidade = computed(() => mostrarArvoreCompletaUnidades.value ? TEXTOS.comum.MENU_UNIDADES : TEXTOS.comum.MENU_MINHA_UNIDADE);
const iconUnidade = computed(() => mostrarArvoreCompletaUnidades.value ? 'bi bi-diagram-3 me-1' : 'bi bi-person me-1');
const linkUnidade = computed(() => mostrarArvoreCompletaUnidades.value ? '/unidades' : `/unidade/${perfilStore.unidadeSelecionada}`);
const iconeTema = computed(() => getTemaEscuro() ? 'bi bi-sun' : 'bi bi-moon-stars');
const tituloTema = computed(() => getTemaEscuro() ? 'Desativar modo escuro' : 'Ativar modo escuro');

function alternarTema() {
  setTemaEscuro(!getTemaEscuro());
}

async function handleLogout() {
  await perfilStore.logout();
  await router.push("/login");
}
</script>

<style scoped>
.navbar-principal-sgc {
  --navbar-principal-fundo: #1f2937;
  --navbar-principal-borda: #374151;
  --navbar-principal-texto: #e5e7eb;
  --navbar-principal-texto-secundario: #cbd5e1;
  --navbar-principal-texto-ativo: #ffffff;
  --navbar-principal-marca: #f8fafc;
  --navbar-principal-toggler: rgba(248, 250, 252, 0.35);
  background-color: var(--navbar-principal-fundo);
  border-bottom-color: var(--navbar-principal-borda) !important;
  --bs-navbar-color: var(--navbar-principal-texto);
  --bs-navbar-hover-color: #ffffff;
  --bs-navbar-active-color: #ffffff;
  --bs-navbar-brand-color: var(--navbar-principal-marca);
  --bs-navbar-brand-hover-color: #ffffff;
  --bs-navbar-disabled-color: #94a3b8;
  --bs-navbar-toggler-border-color: var(--navbar-principal-toggler);
  --bs-navbar-toggler-icon-bg: url("data:image/svg+xml,%3csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 30 30'%3e%3cpath stroke='rgba%28248, 250, 252, 0.95%29' stroke-linecap='round' stroke-miterlimit='10' stroke-width='2' d='M4 7h22M4 15h22M4 23h22'/%3e%3c/svg%3e");
}

:global([data-bs-theme="dark"] .navbar-principal-sgc) {
  --navbar-principal-fundo: #212529;
  --navbar-principal-borda: #343a40;
  --navbar-principal-texto: #dee2e6;
  --navbar-principal-texto-secundario: #adb5bd;
  --navbar-principal-texto-ativo: #ffffff;
  --navbar-principal-marca: #f8f9fa;
  --navbar-principal-toggler: rgba(248, 249, 250, 0.3);
}

.navbar-principal-sgc__marca {
  color: var(--navbar-principal-marca) !important;
}

.navbar-principal-sgc__texto-secundario {
  color: var(--navbar-principal-texto-secundario);
}

:deep(.navbar-principal-sgc .navbar-brand),
:deep(.navbar-principal-sgc .navbar-brand *),
:deep(.navbar-principal-sgc a),
:deep(.navbar-principal-sgc a *),
:deep(.navbar-principal-sgc .nav-link),
:deep(.navbar-principal-sgc .nav-link *),
:deep(.navbar-principal-sgc .dropdown-toggle),
:deep(.navbar-principal-sgc .dropdown-toggle *),
:deep(.navbar-principal-sgc .navbar-nav .nav-item),
:deep(.navbar-principal-sgc .navbar-nav .nav-item *),
:deep(.navbar-principal-sgc i) {
  color: var(--navbar-principal-texto) !important;
}

:deep(.navbar-principal-sgc .navbar-toggler) {
  border-color: var(--navbar-principal-toggler);
}

:deep(.navbar-principal-sgc .nav-link:hover),
:deep(.navbar-principal-sgc .nav-link:focus),
:deep(.navbar-principal-sgc .nav-link:hover *),
:deep(.navbar-principal-sgc .nav-link:focus *),
:deep(.navbar-principal-sgc a:hover),
:deep(.navbar-principal-sgc a:focus),
:deep(.navbar-principal-sgc a:hover *),
:deep(.navbar-principal-sgc a:focus *),
:deep(.navbar-principal-sgc .dropdown-toggle:hover),
:deep(.navbar-principal-sgc .dropdown-toggle:focus),
:deep(.navbar-principal-sgc .dropdown-toggle:hover *),
:deep(.navbar-principal-sgc .dropdown-toggle:focus *),
:deep(.navbar-principal-sgc i:hover),
:deep(.navbar-principal-sgc i:focus) {
  color: var(--navbar-principal-texto-ativo) !important;
}

:deep(.navbar-principal-sgc .router-link-active),
:deep(.navbar-principal-sgc .router-link-active *),
:deep(.navbar-principal-sgc .active),
:deep(.navbar-principal-sgc .active *) {
  color: var(--navbar-principal-texto-ativo) !important;
}

.user-info-text {
  font-size: 0.875rem;
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
