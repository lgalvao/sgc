<script lang="ts" setup>
import {computed, ref, watch} from "vue";
import {useRoute} from "vue-router";
import {BAlert, BOrchestrator} from "bootstrap-vue-next";
import {useFeedbackStore} from "@/stores/feedback";
import pkg from "../package.json";
import BarraNavegacao from "./components/BarraNavegacao.vue";
import MainNavbar from "./components/MainNavbar.vue";

interface PackageJson {
  version: string;

  [key: string]: any;
}

const route = useRoute();
const feedbackStore = useFeedbackStore();

const hideExtrasOnce = ref(false);

function refreshHideFlag() {
  let came = false;
  try {
    came = sessionStorage.getItem("cameFromNavbar") === "1";
  } catch {
    //
  }
  hideExtrasOnce.value = came;
  if (came) {
    try {
      sessionStorage.removeItem("cameFromNavbar");
    } catch {
      //
    }
  }
}

watch(
    () => route.fullPath,
    () => refreshHideFlag(),
    {immediate: true},
);
const version = (pkg as PackageJson).version;

const shouldShowNavBarExtras = computed(() => {
  if (route.path === "/login") return false;
  if (route.path === "/painel") return false;
  return !hideExtrasOnce.value;
});
</script>

<template>
  <a href="#main-content" class="visually-hidden-focusable p-3 bg-white text-primary position-absolute start-0 top-0" style="z-index: 2050;">
    Pular para o conteúdo principal
  </a>
  <BOrchestrator/>
  <div class="fixed-top w-100 d-flex justify-content-center mt-3" style="z-index: 2000; pointer-events: none;">
    <BAlert
        v-model="feedbackStore.currentFeedback.show"
        :fade="false"
        :variant="feedbackStore.currentFeedback.variant"
        class="shadow-sm"
        data-testid="global-alert"
        dismissible
        style="pointer-events: auto; min-width: 300px; max-width: 600px;"
        @closed="feedbackStore.close()"
    >
      <h6 v-if="feedbackStore.currentFeedback.title" class="alert-heading fw-bold mb-1">
        {{ feedbackStore.currentFeedback.title }}
      </h6>
      <p class="mb-0">
        {{ feedbackStore.currentFeedback.message }}
      </p>
    </BAlert>
  </div>

  <div class="d-flex flex-column min-vh-100">
    <MainNavbar v-if="route.path !== '/login'"/>
    <div
        v-if="shouldShowNavBarExtras"
        class="bg-light border-bottom"
    >
      <div class="container pt-1 pb-2">
        <BarraNavegacao/>
      </div>
    </div>

    <main id="main-content" class="flex-grow-1 pb-3">
      <router-view/>
    </main>

    <footer
        v-if="route.path !== '/login'"
        class="bg-light text-muted border-top mt-auto"
    >
      <div class="container-fluid py-2 small d-flex justify-content-between align-items-center">
        <span>Versão {{ version }}</span>
        <span>© SESEL/COSIS/TRE-PE</span>
      </div>
    </footer>
  </div>
</template>