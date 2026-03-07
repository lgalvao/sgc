<template>
  <LayoutPadrao>
    <h1 class="visually-hidden">Painel</h1>
    <!-- Tabela de Processos -->
    <div class="mb-5">
      <PageHeader title="Processos" title-test-id="txt-painel-titulo-processos">
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
          :mostrar-cta-vazio="perfil.isAdmin"
          :processos="processosOrdenados"
          @ordenar="ordenarPor"
          @selecionar-processo="abrirDetalhesProcesso"
          @cta-vazio="router.push({ name: 'CadProcesso' })"
      />
    </div>

    <div>
      <PageHeader title="Alertas" title-test-id="txt-painel-titulo-alertas"/>
      <div class="table-responsive">
        <BTable
            :fields="camposAlertas"
            :items="alertas"
            :striped="alertas.length > 0"
            :tbody-tr-props="rowAttrAlerta"
            :tbody-tr-class="rowClassAlerta"
            data-testid="tbl-alertas"
            hover
            responsive
            show-empty
            stacked="md"
            @sort-changed="handleSortChangeAlertas"
        >
          <template #cell(mensagem)="data">
            <span v-if="!data.item.dataHoraLeitura" class="visually-hidden">Não lido: </span>
            {{ data.value }}
          </template>
          <template #empty>
            <EmptyState
                class="border-0 bg-transparent mb-0"
                data-testid="empty-state-alertas"
                icon="bi-bell-slash"
                title="Nenhum alerta"
            >
            </EmptyState>
          </template>
        </BTable>
      </div>
    </div>
  </LayoutPadrao>
</template>

<script lang="ts" setup>
import {BButton, BTable, useToast} from "bootstrap-vue-next";
import {storeToRefs} from "pinia";
import {computed, onActivated, onMounted, ref} from "vue";
import {useRouter} from "vue-router";
import LayoutPadrao from "@/components/layout/LayoutPadrao.vue";
import PageHeader from "@/components/layout/PageHeader.vue";
import EmptyState from "@/components/comum/EmptyState.vue";
import {formatDateBR} from "@/utils";
import TabelaProcessos from "@/components/processo/TabelaProcessos.vue";
import {useAlertasStore} from "@/stores/alertas";
import {usePerfilStore} from "@/stores/perfil";
import {useProcessosStore} from "@/stores/processos";
import {useToastStore} from "@/stores/toast";
import type {Alerta, ProcessoResumo} from "@/types/tipos";

const perfil = usePerfilStore();
const processosStore = useProcessosStore();
const alertasStore = useAlertasStore();
const toastStore = useToastStore();
const toast = useToast();

const {processosPainel} = storeToRefs(processosStore);
const {alertas} = storeToRefs(alertasStore);

const router = useRouter();

const criterio = ref<keyof ProcessoResumo>("descricao");
const asc = ref(true);

async function carregarDados() {
  if (perfil.perfilSelecionado && perfil.unidadeSelecionada) {
    const promises: Promise<any>[] = [
      processosStore.buscarProcessosPainel(
          perfil.perfilSelecionado,
          Number(perfil.unidadeSelecionada),
          0,
          10,
      ), // Paginação inicial
    ];

    if (perfil.usuarioCodigo) {
      promises.push(
          alertasStore.buscarAlertas(
              perfil.usuarioCodigo,
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
  const pendente = toastStore.consumePending();
  if (pendente) {
    toast.create({
      props: {
        body: pendente.body,
        variant: 'success',
        modelValue: 4000,
        pos: 'bottom-end',
        noProgress: true,
      }
    });
  }
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

function abrirDetalhesProcesso(processo: ProcessoResumo | undefined) {
  if (processo && processo.linkDestino) {
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
        perfil.usuarioCodigo,
        Number(perfil.unidadeSelecionada),
        0,
        10,
        alertaCriterio.value,
        alertaAsc.value ? "asc" : "desc",
    );
  }
}

const camposAlertas = [
  {key: "dataHora", label: "Data/Hora", sortable: true, formatter: (v: any) => formatDateBR(v)},
  {key: "mensagem", label: "Descrição"},
  {key: "processo", label: "Processo", sortable: true},
  {key: "origem", label: "Origem"},
];

const rowClassAlerta = (item: Alerta | null) => {
  if (!item) return "";
  return item.dataHoraLeitura ? "" : "fw-bold";
};

const handleSortChangeAlertas = (ctx: any) => {
  const sortBy = Array.isArray(ctx.sortBy) ? ctx.sortBy[0] : ctx.sortBy;
  const key = sortBy?.key || (typeof sortBy === 'string' ? sortBy : null);
  if (key === "dataHora") {
    ordenarAlertasPor("data");
  } else if (key === "processo") {
    ordenarAlertasPor("processo");
  }
};

const rowAttrAlerta = (item: Alerta | null) => {
  if (item) {
    return {'data-testid': `row-alerta-${item.codigo}`};
  }
  return {};
};
</script>
