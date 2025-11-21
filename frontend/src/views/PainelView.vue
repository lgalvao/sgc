<template>
  <BContainer class="mt-4">
    <!-- Tabela de Processos -->
    <div class="mb-5">
      <div class="d-flex justify-content-between align-items-center mb-3">
        <div
          class="display-6 mb-0"
          data-testid="titulo-processos"
        >
          Processos
        </div>
        <BButton
          v-if="perfil.perfilSelecionado === 'ADMIN'"
          :to="{ name: 'CadProcesso' }"
          variant="outline-primary"
          data-testid="btn-criar-processo"
        >
          <i class="bi bi-plus-lg" /> Criar processo
        </BButton>
      </div>
      <TabelaProcessos
        :processos="processosOrdenados"
        :criterio-ordenacao="criterio"
        :direcao-ordenacao-asc="asc"
        @ordenar="ordenarPor"
        @selecionar-processo="abrirDetalhesProcesso"
      />
    </div>

    <div>
      <div class="d-flex justify-content-between align-items-center mb-3">
        <div
          class="mb-0 display-6"
          data-testid="titulo-alertas"
        >
          Alertas
        </div>
      </div>
      <TabelaAlertas
        :alertas="alertas"
        @ordenar="ordenarAlertasPor"
        @selecionar-alerta="abrirDetalhesAlerta"
      />
    </div>
  </BContainer>
</template>

<script lang="ts" setup>
import {BButton, BContainer} from "bootstrap-vue-next";
import {storeToRefs} from "pinia";
import {computed, onMounted, ref} from "vue";
import {useRouter} from "vue-router";
import TabelaAlertas from "@/components/TabelaAlertas.vue";
import TabelaProcessos from "@/components/TabelaProcessos.vue";
import {useAlertasStore} from "@/stores/alertas";
import {usePerfilStore} from "@/stores/perfil";
import {useProcessosStore} from "@/stores/processos";
import type {Alerta, ProcessoResumo} from "@/types/tipos";

const perfil = usePerfilStore();
const processosStore = useProcessosStore();
const alertasStore = useAlertasStore();

const {processosPainel} = storeToRefs(processosStore);
const {alertas} = storeToRefs(alertasStore);

const router = useRouter();

const criterio = ref<keyof ProcessoResumo>("descricao");
const asc = ref(true);

onMounted(async () => {
  if (perfil.perfilSelecionado && perfil.unidadeSelecionada) {
    processosStore.fetchProcessosPainel(
        perfil.perfilSelecionado,
        Number(perfil.unidadeSelecionada),
        0,
        10,
    ); // Paginação inicial
    alertasStore.fetchAlertas(
        Number(perfil.servidorId) || 0,
        Number(perfil.unidadeSelecionada),
        0,
        10,
    ); // Paginação inicial
  }
});

const processosOrdenados = computed(() => processosPainel.value);

function ordenarPor(campo: keyof ProcessoResumo) {
  if (criterio.value === campo) {
    asc.value = !asc.value;
  } else {
    criterio.value = campo;
    asc.value = true;
  }
  processosStore.fetchProcessosPainel(
    perfil.perfilSelecionado!,
    Number(perfil.unidadeSelecionada),
    0,
    10,
    criterio.value,
      asc.value ? "asc" : "desc",
  );
}

function abrirDetalhesProcesso(processo: ProcessoResumo) {
  if (processo.linkDestino) {
    router.push(processo.linkDestino);
  }
}

function abrirDetalhesAlerta(alerta: Alerta) {
  if (alerta.linkDestino) {
    router.push(alerta.linkDestino);
  }
}

// Ordenação de alertas por coluna (CDU-02 - cabeçalho "Processo" e padrão por data desc)
const alertaCriterio = ref<"data" | "processo">("data");
const alertaAsc = ref(false); // false = desc (padrão por data/hora)

function ordenarAlertasPor(campo: "data" | "processo") {
  if (alertaCriterio.value === campo) {
    alertaAsc.value = !alertaAsc.value;
  } else {
    alertaCriterio.value = campo;
    alertaAsc.value = campo === "data" ? false : true;
  }
  alertasStore.fetchAlertas(
    Number(perfil.servidorId) || 0,
    Number(perfil.unidadeSelecionada),
    0,
    10,
    alertaCriterio.value,
      alertaAsc.value ? "asc" : "desc",
  );
}
</script>
