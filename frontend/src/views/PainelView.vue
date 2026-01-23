<template>
  <BContainer class="mt-4">
    <!-- Tabela de Processos -->
    <div class="mb-5">
      <PageHeader title="Processos">
        <template #actions>
          <BButton
              v-if="perfil.isAdmin"
              :to="{ name: 'CadProcesso' }"
              data-testid="btn-painel-criar-processo"
              variant="outline-primary"
          >
            <i aria-hidden="true" class="bi bi-plus-lg"/> Criar processo
          </BButton>
        </template>
      </PageHeader>
      <TabelaProcessos
          :compacto="true"
          :criterio-ordenacao="criterio"
          :direcao-ordenacao-asc="asc"
          :processos="processosOrdenados"
          @ordenar="ordenarPor"
          @selecionar-processo="abrirDetalhesProcesso"
      />
    </div>

    <div>
      <PageHeader title="Alertas" />
      <TabelaAlertas
          :alertas="alertas"
          @ordenar="ordenarAlertasPor"
      />
    </div>
  </BContainer>
</template>

<script lang="ts" setup>
import {BButton, BContainer} from "bootstrap-vue-next";
import {storeToRefs} from "pinia";
import {computed, onActivated, onMounted, ref} from "vue";
import {useRouter} from "vue-router";
import PageHeader from "@/components/layout/PageHeader.vue";
import TabelaAlertas from "@/components/TabelaAlertas.vue";
import TabelaProcessos from "@/components/TabelaProcessos.vue";
import {useAlertasStore} from "@/stores/alertas";
import {usePerfilStore} from "@/stores/perfil";
import {useProcessosStore} from "@/stores/processos";
import type {ProcessoResumo} from "@/types/tipos";

const perfil = usePerfilStore();
const processosStore = useProcessosStore();
const alertasStore = useAlertasStore();

const {processosPainel} = storeToRefs(processosStore);
const {alertas} = storeToRefs(alertasStore);

const router = useRouter();

const criterio = ref<keyof ProcessoResumo>("descricao");
const asc = ref(true);

async function carregarDados() {
  if (perfil.perfilSelecionado && perfil.unidadeSelecionada) {
    const promises = [
      await processosStore.buscarProcessosPainel(
          perfil.perfilSelecionado,
          Number(perfil.unidadeSelecionada),
          0,
          10,
      ), // Paginação inicial
    ];

    if (perfil.usuarioCodigo) {
      promises.push(
          alertasStore.buscarAlertas(
              Number(perfil.usuarioCodigo),
              Number(perfil.unidadeSelecionada),
              0,
              10,
          ), // Paginação inicial
      );
    }

    await Promise.all(promises);
  }
}

onMounted(async () => {
  await carregarDados();
});

onActivated(async () => {
  await carregarDados();
});

const processosOrdenados = computed(() => processosPainel.value);

function ordenarPor(campo: keyof ProcessoResumo) {
  if (criterio.value === campo) {
    asc.value = !asc.value;
  } else {
    criterio.value = campo;
    asc.value = true;
  }
  processosStore.buscarProcessosPainel(
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

// Ordenação de alertas por coluna (CDU-02 - cabeçalho "Processo" e padrão por data desc)
const alertaCriterio = ref<"data" | "processo">("data");
const alertaAsc = ref(false); // false = desc (padrão por data/hora)

function ordenarAlertasPor(campo: "data" | "processo") {
  if (alertaCriterio.value === campo) {
    alertaAsc.value = !alertaAsc.value;
  } else {
    alertaCriterio.value = campo;
    alertaAsc.value = campo !== "data";
  }
  if (perfil.usuarioCodigo) {
    alertasStore.buscarAlertas(
        Number(perfil.usuarioCodigo),
        Number(perfil.unidadeSelecionada),
        0,
        10,
        alertaCriterio.value,
        alertaAsc.value ? "asc" : "desc",
    );
  }
}
</script>
