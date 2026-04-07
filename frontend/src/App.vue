<script lang="ts" setup>
import {computed} from "vue";
import {useRoute} from "vue-router";
import {BOrchestrator} from "bootstrap-vue-next";
import pkg from "../package.json";
import BarraNavegacao from "./components/layout/BarraNavegacao.vue";
import MainNavbar from "./components/layout/MainNavbar.vue";
import {TEXTOS} from "@/constants/textos";
import {usePerfilStore} from "@/stores/perfil";

interface PackageJson {
  version: string;

  [key: string]: unknown;
}

const route = useRoute();
const perfilStore = usePerfilStore();
const version = (pkg as PackageJson).version;

const shouldShowNavBarExtras = computed(() => {
  if (route.path === "/login" || route.path === "/erro") return false;
  return route.path !== "/painel";

});

const maximoRotasEmCache = 10;
const chaveSessao = computed(() =>
    `${perfilStore.usuarioCodigo ?? "anon"}-${perfilStore.perfilSelecionado ?? "sem-perfil"}-${perfilStore.unidadeSelecionada ?? "sem-unidade"}`
);
</script>

<template>
  <a
      class="visually-hidden-focusable p-3 bg-white text-primary position-absolute start-0 top-0"
      href="#main-content"
      style="z-index: 2050;"
  >
    {{ TEXTOS.comum.PULAR_CONTEUDO }}
  </a>
  <BOrchestrator/>

  <div class="d-flex flex-column min-vh-100">
    <MainNavbar v-if="route.path !== '/login' && route.path !== '/erro'"/>
    <div
        v-if="shouldShowNavBarExtras"
        class="bg-light border-bottom"
    >
      <div class="container pt-1 pb-2">
        <BarraNavegacao/>
      </div>
    </div>

    <main id="main-content" class="flex-grow-1 pb-3">
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
        class="bg-light text-muted border-top mt-auto"
    >
      <div class="container-fluid py-2 small d-flex justify-content-between align-items-center">
        <span>{{ TEXTOS.comum.VERSAO }} {{ version }}</span>
        <span>{{ TEXTOS.comum.RODAPE }}</span>
      </div>
    </footer>
  </div>
</template>

<style>
</style>
