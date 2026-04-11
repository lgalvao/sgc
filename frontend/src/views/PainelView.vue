<template>
  <LayoutPadrao>
    <h1 class="visually-hidden">{{ TEXTOS.painel.TITULO }}</h1>
    <div v-if="carregandoPainel" class="text-center py-5" data-testid="painel-carregando">
      <BSpinner :label="TEXTOS.comum.CARREGANDO_DADOS" variant="primary" />
      <p class="mt-2 text-muted">{{ TEXTOS.comum.CARREGANDO_DADOS }}</p>
    </div>
    <template v-else>
      <!-- Tabela de Processos -->
      <div class="mb-5">
        <PageHeader :title="TEXTOS.painel.PROCESSOS" title-test-codigo="txt-painel-titulo-processos">
          <template #actions>
            <BButton
                v-if="perfil.mostrarCriarProcesso.value"
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
            :mostrar-cta-vazio="perfil.mostrarCtaPainelVazio.value"
            :processos="processosOrdenados"
            @ordenar="ordenarPor"
            @selecionar-processo="abrirDetalhesProcesso"
            @cta-vazio="router.push({ name: 'CadProcesso' })"
        />
      </div>

      <div>
        <PageHeader :title="TEXTOS.painel.ALERTAS" title-test-codigo="txt-painel-titulo-alertas"/>
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
    </template>
  </LayoutPadrao>
</template>

<script lang="ts" setup>
import {BButton, BSpinner, BTable, useToast} from "bootstrap-vue-next";
import {computed, onActivated, onMounted, ref} from "vue";
import {useRouter} from "vue-router";
import LayoutPadrao from "@/components/layout/LayoutPadrao.vue";
import PageHeader from "@/components/layout/PageHeader.vue";
import EmptyState from "@/components/comum/EmptyState.vue";
import {formatDateTimeBR} from "@/utils";
import TabelaProcessos from "@/components/processo/TabelaProcessos.vue";
import {usePerfilStore} from "@/stores/perfil";
import {usePerfil} from "@/composables/usePerfil";
import {useToastStore} from "@/stores/toast";
import {usePainelStore} from "@/stores/painel";
import type {Alerta, ProcessoResumo} from "@/types/tipos";
import * as painelService from "@/services/painelService";
import {TEXTOS} from "@/constants/textos";

const perfilStore = usePerfilStore();
const perfil = usePerfil();
const toastStore = useToastStore();
const painelStore = usePainelStore();
const toast = useToast();
const carregandoPainel = ref(true);

const router = useRouter();

const criterio = ref<keyof ProcessoResumo>("descricao");
const asc = ref(true);

// Ordenação local: sem round-trip ao backend
const processosOrdenados = computed(() => {
  const lista = [...painelStore.processos];
  const campo = criterio.value;
  const direcao = asc.value ? 1 : -1;
  return lista.sort((a, b) => {
    const va = a[campo] ?? "";
    const vb = b[campo] ?? "";
    if (va < vb) return -1 * direcao;
    if (va > vb) return 1 * direcao;
    return 0;
  });
});

// Alertas já ordenados pelo backend (desc dataHora); exibidos diretamente
const alertas = computed(() => painelStore.alertas);

async function carregarDados() {
  const unidadeCodigo = perfilStore.unidadeSelecionada;
  if (!perfil.perfilSelecionado.value || !unidadeCodigo) {
    carregandoPainel.value = false;
    return;
  }

  carregandoPainel.value = true;
  try {
    // Bootstrap: carrega processos e alertas em um único round-trip
    const bootstrap = await painelService.obterBootstrap();

    painelStore.definirDados(
        bootstrap.processos,
        bootstrap.alertas
    );

    // Marcar não lidos como lidos: fire-and-forget, não bloqueia a tela
    const codigosNaoLidos = bootstrap.alertas
        .filter((a: Alerta) => !a.dataHoraLeitura && !painelStore.isMarcadoComoLido(a.codigo))
        .map((a: Alerta) => a.codigo);
    if (codigosNaoLidos.length > 0) {
      painelStore.registrarLeitura(codigosNaoLidos);
      painelService.marcarAlertasLidos(codigosNaoLidos).catch(() => {/* silencioso: não crítico para a UI */});
    }
  } finally {
    carregandoPainel.value = false;
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
    return true;
  }
  return false;
}

// Controla se o mount inicial já ocorreu para evitar double-load com onActivated
let montadoUmaVez = false;

onMounted(async () => {
  montadoUmaVez = true;
  exibirToastPendente();
  await carregarDados();
});

onActivated(async () => {
  if (!montadoUmaVez) return;
  exibirToastPendente();
  // Só recarrega se o cache foi invalidado (ex: após ação de workflow)
  if (painelStore.dadosValidos()) return;
  await carregarDados();
});

function ordenarPor(campo: keyof ProcessoResumo) {
  if (criterio.value === campo) {
    asc.value = !asc.value;
  } else {
    criterio.value = campo;
    asc.value = true;
  }
  // Ordenação aplicada localmente via computed — sem chamada ao backend
}

function abrirDetalhesProcesso(processo: ProcessoResumo | undefined) {
  if (processo && processo.linkDestino) {
    router.push(processo.linkDestino);
  }
}

const camposAlertas = [
  {key: "dataHora", label: TEXTOS.painel.CAMPOS_ALERTAS.DATA_HORA, sortable: false, formatter: (v: unknown) => formatDateTimeBR(v as string | Date)},
  {key: "mensagem", label: TEXTOS.painel.CAMPOS_ALERTAS.DESCRICAO},
  {key: "processo", label: TEXTOS.painel.CAMPOS_ALERTAS.PROCESSO, sortable: false},
  {key: "origem", label: TEXTOS.painel.CAMPOS_ALERTAS.ORIGEM},
];

const rowClassAlerta = (item: Alerta | null, type = "row") => {
  if (!item || type !== "row") return "";
  return item.dataHoraLeitura ? "" : "fw-bold";
};

const rowAttrAlerta = (item: Alerta | null, type = "row") => {
  if (item && type === "row") {
    return {'data-testid': `row-alerta-${item.codigo}`};
  }
  return {};
};
</script>
