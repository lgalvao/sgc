<template>
  <LayoutPadrao>
    <h1 class="visually-hidden">{{ TEXTOS.painel.TITULO }}</h1>
    <!-- Tabela de Processos -->
    <div class="mb-5">
      <PageHeader :title="TEXTOS.painel.PROCESSOS" title-test-id="txt-painel-titulo-processos">
        <template #actions>
          <BButton
              v-if="perfil.podeCriarProcesso.value"
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
          :mostrar-cta-vazio="perfil.podeVisualizarTabelaCtaVazio.value"
          :processos="processosOrdenados"
          @ordenar="ordenarPor"
          @selecionar-processo="abrirDetalhesProcesso"
          @cta-vazio="router.push({ name: 'CadProcesso' })"
      />
    </div>

    <div>
      <PageHeader :title="TEXTOS.painel.ALERTAS" title-test-id="txt-painel-titulo-alertas"/>
      <div v-if="alertas.length > 0" class="table-responsive">
        <BTable
            :fields="camposAlertas"
            :items="alertas"
            :tbody-tr-props="rowAttrAlerta"
            :tbody-tr-class="rowClassAlerta"
            aria-label="Alertas"
            data-testid="tbl-alertas"
            responsive
            stacked="md"
            small
        >
          <template #cell(mensagem)="data">
            <span v-if="!data.item.dataHoraLeitura" class="visually-hidden">{{ TEXTOS.comum.NAO_LIDO }}</span>
            {{ data.value }}
          </template>
        </BTable>
      </div>
      <EmptyState
          v-else
          class="border-0 bg-transparent mb-0"
          data-testid="empty-state-alertas"
          icon="bi-bell-slash"
          :title="TEXTOS.painel.NENHUM_ALERTA"
      />
    </div>
  </LayoutPadrao>
</template>

<script lang="ts" setup>
import {BButton, BTable, useToast} from "bootstrap-vue-next";
import {computed, onActivated, onMounted, ref} from "vue";
import {useRouter} from "vue-router";
import LayoutPadrao from "@/components/layout/LayoutPadrao.vue";
import PageHeader from "@/components/layout/PageHeader.vue";
import EmptyState from "@/components/comum/EmptyState.vue";
import {formatDateBR, formatDateTimeBR} from "@/utils";
import TabelaProcessos from "@/components/processo/TabelaProcessos.vue";
import {usePerfilStore} from "@/stores/perfil";
import {usePerfil} from "@/composables/usePerfil";
import {useProcessos} from "@/composables/useProcessos";
import {useToastStore} from "@/stores/toast";
import type {Alerta, ProcessoResumo} from "@/types/tipos";
import type {Page} from "@/services/painelService";
import * as painelService from "@/services/painelService";
import {TEXTOS} from "@/constants/textos";

const perfilStore = usePerfilStore();
const perfil = usePerfil();
const {processosPainel, buscarProcessosPainel} = useProcessos();
const toastStore = useToastStore();
const toast = useToast();
const alertas = ref<Alerta[]>([]);
const alertasPage = ref<Page<Alerta>>({} as Page<Alerta>);

const router = useRouter();

const criterio = ref<keyof ProcessoResumo>("descricao");
const asc = ref(true);

async function buscarAlertas(
    usuarioCodigo: string,
    unidade: number,
    page: number,
    size: number,
    sort?: "data" | "processo",
    order?: "asc" | "desc",
) {
  const response = await painelService.listarAlertas(usuarioCodigo, unidade, page, size, sort, order);
  alertas.value = response.content;
  alertasPage.value = response;
}


async function carregarDados() {
  if (perfil.perfilSelecionado.value && perfilStore.unidadeSelecionada) {
    const promises: Promise<any>[] = [
      buscarProcessosPainel(
          perfil.perfilSelecionado.value,
          Number(perfilStore.unidadeSelecionada),
          0,
          10,
      ), // Paginação inicial
    ];

    if (perfilStore.usuarioCodigo) {
      promises.push(
          buscarAlertas(
              perfilStore.usuarioCodigo,
              Number(perfilStore.unidadeSelecionada),
              0,
              10,
              "dataHora",
              "desc"
          ), // Paginação inicial ordenada por dataHora DESC
      );
    }

    await Promise.all(promises);
  }
}

function exibirToastPendente() {
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
}

onMounted(async () => {
  exibirToastPendente();
  await carregarDados();
});

onActivated(async () => {
  exibirToastPendente();
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
  buscarProcessosPainel(
      perfil.perfilSelecionado.value!,
      Number(perfilStore.unidadeSelecionada),
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

const camposAlertas = [
  {key: "dataHora", label: TEXTOS.painel.CAMPOS_ALERTAS.DATA_HORA, sortable: false, formatter: (v: any) => formatDateTimeBR(v)},
  {key: "mensagem", label: TEXTOS.painel.CAMPOS_ALERTAS.DESCRICAO},
  {key: "processo", label: TEXTOS.painel.CAMPOS_ALERTAS.PROCESSO, sortable: false},
  {key: "origem", label: TEXTOS.painel.CAMPOS_ALERTAS.ORIGEM},
];

const rowClassAlerta = (item: Alerta | null) => {
  if (!item) return "";
  return item.dataHoraLeitura ? "" : "fw-bold";
};

const rowAttrAlerta = (item: Alerta | null) => {
  if (item) {
    return {'data-testid': `row-alerta-${item.codigo}`};
  }
  return {};
};
</script>
