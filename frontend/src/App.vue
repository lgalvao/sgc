<script lang="ts" setup>
import {computed, onMounted, watch} from "vue";
import {useRoute} from "vue-router";
import {BOrchestrator} from "bootstrap-vue-next";
import pkg from "../package.json";
import BarraNavegacao from "./components/layout/BarraNavegacao.vue";
import MainNavbar from "./components/layout/MainNavbar.vue";
import {TEXTOS} from "@/constants/textos";
import {usePerfilStore} from "@/stores/perfil";
import {useCacheSync} from "@/composables/useCacheSync";
import {useConfiguracoes} from "@/composables/useConfiguracoes";
import {useTemaPreferencia} from "@/composables/useTemaPreferencia";

interface PackageJson {
  version: string;

  [key: string]: unknown;
}

const route = useRoute();
const perfilStore = usePerfilStore();
const {carregarConfiguracoes} = useConfiguracoes();
const {obterTemaEscuro, definirContextoUsuarioTemaEscuro} = useTemaPreferencia();
const version = (pkg as PackageJson).version;

const aplicarTema = () => {
  const isDark = obterTemaEscuro();
  document.documentElement.setAttribute("data-bs-theme", isDark ? "dark" : "light");
};

onMounted(() => {
  definirContextoUsuarioTemaEscuro(perfilStore.usuarioCodigo);
  aplicarTema();
});

watch(
    () => [perfilStore.usuarioCodigo, perfilStore.permissoesSessao?.mostrarMenuConfiguracoes],
    async ([codigo, podeConfigurar]) => {
      if (codigo && podeConfigurar) {
        await carregarConfiguracoes();
      }
    },
    {immediate: true}
);

watch(
    () => obterTemaEscuro(),
    () => aplicarTema()
);

watch(
    () => perfilStore.usuarioCodigo,
    (codigo) => {
      definirContextoUsuarioTemaEscuro(codigo);
      aplicarTema();
    },
    {immediate: true}
);

watch(
    () => perfilStore.usuarioCodigo,
    (codigo, _codigoAnterior, aoLimpar) => {
      if (!codigo) {
        return;
      }

      const encerrarSincronizacao = useCacheSync();
      aoLimpar(encerrarSincronizacao);
    },
    {immediate: true}
);

const shouldShowNavBarExtras = computed(() => {
  if (route.path === "/login" || route.path === "/erro") return false;
  return route.path !== "/painel";

});

const maximoRotasEmCache = 10;
const chaveSessao = computed(() =>
    `${perfilStore.versaoSessao}-${perfilStore.usuarioCodigo ?? "anon"}-${perfilStore.perfilSelecionado ?? "sem-perfil"}-${perfilStore.unidadeSelecionada ?? "sem-unidade"}`
);
</script>

<template>
  <a
      class="visually-hidden-focusable p-3 bg-white text-primary position-absolute start-0 top-0"
      href="#main-content"
      style="z-index: 2050;"
      data-testid="skip-link"
  >
    {{ TEXTOS.comum.PULAR_CONTEUDO }}
  </a>
  <BOrchestrator
      aria-atomic="true"
      aria-live="polite"
      class="orchestrator-container"
  />

  <div class="d-flex flex-column min-vh-100">
    <div
        v-if="route.path !== '/login' && route.path !== '/erro'"
        class="cabecalho-fixo sticky-top"
    >
      <MainNavbar/>
      <div
          v-if="shouldShowNavBarExtras"
          class="bg-body-tertiary border-bottom"
      >
        <div class="container py-1">
          <BarraNavegacao/>
        </div>
      </div>
    </div>

    <main id="main-content" class="flex-grow-1 pb-3" data-testid="main-content">
      <router-view v-slot="{ Component, route: currentRoute }">
        <KeepAlive :max="maximoRotasEmCache">
          <component
              :is="Component"
              v-if="currentRoute.meta?.keepAlive"
              :key="`${chaveSessao}:${currentRoute.fullPath}`"
          />
        </KeepAlive>
        <component
            :is="Component"
            v-if="!currentRoute.meta?.keepAlive"
            :key="`${chaveSessao}:${currentRoute.fullPath}`"
        />
      </router-view>
    </main>

    <footer
        v-if="route.path !== '/login' && route.path !== '/erro'"
        class="footer-app bg-body-tertiary text-body-secondary border-top mt-auto"
        data-testid="app-footer"
    >
      <div class="container-fluid footer-app__conteudo small d-flex justify-content-between align-items-center">
        <span data-testid="app-version">{{ TEXTOS.comum.VERSAO }} {{ version }}</span>
        <span>{{ TEXTOS.comum.RODAPE }}</span>
      </div>
    </footer>
  </div>
</template>

<style>
[data-bs-theme="dark"] {
  --bs-body-bg: #0f172a;
  --bs-body-color: #f8fafc;
  --bs-tertiary-bg: #020617;
  --bs-secondary-bg: #1e293b;
  --bs-secondary-color: #94a3b8;
  --bs-tertiary-color: #64748b;
  --bs-emphasis-color: #ffffff;
  --bs-border-color: #334155;
  --bs-primary: #818cf8;
  --bs-primary-rgb: 129, 140, 248;
  --bs-link-color: #a5b4fc;
  --bs-link-hover-color: #c7d2fe;
  --bs-card-bg: #1e293b;
  --bs-card-border-color: #475569;
}

[data-bs-theme="dark"] .card {
  background-color: var(--bs-card-bg);
  border: 1px solid var(--bs-card-border-color);
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.2);
}

[data-bs-theme="dark"] .card:hover {
  border-color: #818cf8;
  box-shadow: 0 0 12px rgba(129, 140, 248, 0.15);
}

[data-bs-theme="dark"] .btn-outline-primary {
  --bs-btn-color: #818cf8;
  --bs-btn-border-color: #818cf8;
  --bs-btn-hover-color: #ffffff;
  --bs-btn-hover-bg: #6366f1;
  --bs-btn-hover-border-color: #6366f1;
  --bs-btn-focus-shadow-rgb: 129, 140, 248;
  --bs-btn-active-color: #ffffff;
  --bs-btn-active-bg: #6366f1;
  --bs-btn-active-border-color: #6366f1;
}

[data-bs-theme="dark"] .btn-outline-secondary {
  --bs-btn-color: #94a3b8;
  --bs-btn-border-color: #475569;
  --bs-btn-hover-color: #ffffff;
  --bs-btn-hover-bg: #334155;
  --bs-btn-hover-border-color: #334155;
  --bs-btn-focus-shadow-rgb: 148, 163, 184;
  --bs-btn-active-color: #ffffff;
  --bs-btn-active-bg: #334155;
  --bs-btn-active-border-color: #334155;
}

[data-bs-theme="dark"] .bg-body-tertiary {
  background-color: var(--bs-tertiary-bg) !important;
}

[data-bs-theme="dark"] .text-muted {
  color: #94a3b8 !important;
}

.cabecalho-fixo {
  z-index: 1020;
}

.footer-app__conteudo {
  padding-top: 0.35rem;
  padding-bottom: 0.35rem;
  gap: 0.75rem;
  line-height: 1.2;
}

.orchestrator-container .toast-container {
  width: auto !important;
}

.orchestrator-container .toast {
  width: auto !important;
  max-width: 22rem;
  display: inline-flex;
}

.toast .toast-body {
  padding: 0.75rem 0.9rem;
  white-space: normal;
}

@media (max-width: 576px) {
  .footer-app__conteudo {
    align-items: flex-start !important;
    flex-direction: column;
  }
}

button:disabled,
.btn:disabled,
.btn.disabled {
  cursor: not-allowed !important;
  pointer-events: auto !important;
}

.link-discreto {
  color: var(--bs-secondary-color);
  text-decoration: none;
  transition: color 0.15s ease;
}

.link-discreto:hover,
.link-discreto:focus {
  color: var(--bs-emphasis-color);
  text-decoration: none;
}

.btn-acao-sutil {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 2rem;
  height: 2rem;
  padding: 0;
  border-color: var(--bs-border-color) !important;
  color: var(--bs-secondary-color) !important;
  transition: all 0.2s;
}

.btn-acao-sutil:hover,
.btn-acao-sutil:focus {
  background-color: var(--bs-tertiary-bg) !important;
  border-color: var(--bs-border-color-translucent) !important;
  color: var(--bs-emphasis-color) !important;
}
</style>
