<template>
  <LayoutPadrao>
    <h1 class="visually-hidden">{{ TEXTOS.comum.MENU_NOTIFICACOES }}</h1>

    <div v-if="carregando" class="text-center py-5" data-testid="notificacoes-carregando">
      <BSpinner :label="TEXTOS.comum.CARREGANDO_DADOS" variant="primary"/>
      <p class="mt-2 text-muted">{{ TEXTOS.comum.CARREGANDO_DADOS }}</p>
    </div>

    <BAlert v-else-if="erro" :model-value="true" variant="danger">
      {{ erro }}
    </BAlert>

    <template v-else>
      <section class="mb-5">
        <PageHeader :title="TEXTOS.administracao.NOTIFICACOES_PENDENTES_TITULO">
          <template #actions>
            <BButton
                data-testid="btn-notificacoes-atualizar"
                :disabled="carregando"
                size="sm"
                variant="outline-primary"
                @click="carregar"
            >
              <i aria-hidden="true" class="bi bi-arrow-clockwise"></i>
              {{ TEXTOS.administracao.NOTIFICACOES_ATUALIZAR }}
            </BButton>
          </template>
        </PageHeader>
        <BAlert
            v-if="notificacoesPendentes.length === 0"
            :model-value="true"
            variant="success"
        >
          {{ TEXTOS.administracao.NOTIFICACOES_SEM_PENDENCIAS }}
        </BAlert>
        <BTable
            v-else
            :fields="camposPendentes"
            :items="notificacoesPendentes"
            :tbody-tr-class="rowClass"
            data-testid="tbl-notificacoes-pendentes"
            hover
            responsive
            small
        >
          <template #cell(unidadeSigla)="{ item }">
            <UnidadeLink :item="item"/>
          </template>

          <template #cell(statusGeral)="{ item }">
            <BBadge :variant="statusVariant(item.statusGeral)">
              {{ statusLabel(item.statusGeral) }}
            </BBadge>
            <div v-if="item.maiorTentativas > 0" class="text-muted small mt-1">
              {{ item.maiorTentativas }} tentativa(s)
            </div>
          </template>

          <template #cell(ultimoErro)="{ item }">
            <span v-if="item.ultimoErro" class="text-danger text-truncate d-inline-block erro-notificacao" :title="item.ultimoErro">
              {{ item.ultimoErro }}
            </span>
            <span v-else>-</span>
          </template>

          <template #cell(proximaTentativaEm)="{ item }">
            {{ formatarDataOuHifen(item.proximaTentativaEm) }}
          </template>

          <template #cell(acoes)="{ item }">
            <div class="text-end">
              <BButton
                  v-if="item.podeReenviar"
                  data-testid="btn-notificacoes-reenviar"
                  size="sm"
                  variant="outline-danger"
                  @click="confirmarReenvio(item)"
              >
                <i aria-hidden="true" class="bi bi-send"></i>
                {{ TEXTOS.administracao.NOTIFICACOES_REENVIAR }}
              </BButton>
            </div>
          </template>
        </BTable>
      </section>

      <section>
        <PageHeader :title="TEXTOS.administracao.NOTIFICACOES_CONCLUIDAS_TITULO"/>
        <BAlert
            v-if="notificacoesConcluidas.length === 0"
            :model-value="true"
            variant="secondary"
        >
          {{ TEXTOS.administracao.NOTIFICACOES_SEM_CONCLUIDAS }}
        </BAlert>
        <BTable
            v-else
            :fields="camposConcluidas"
            :items="notificacoesConcluidas"
            data-testid="tbl-notificacoes-concluidas"
            hover
            responsive
            small
        >
          <template #cell(unidadeSigla)="{ item }">
            <UnidadeLink :item="item"/>
          </template>

          <template #cell(statusGeral)="{ item }">
            <BBadge :variant="statusVariant(item.statusGeral)">
              {{ statusLabel(item.statusGeral) }}
            </BBadge>
          </template>

          <template #cell(dataHoraEnvio)="{ item }">
            {{ formatarDataOuHifen(item.ultimaNotificacaoEm) }}
          </template>
        </BTable>
      </section>
    </template>

    <ModalConfirmacao
        v-model="mostrarModalReenvio"
        :auto-close="false"
        :loading="reenviando"
        :ok-title="TEXTOS.administracao.NOTIFICACOES_REENVIAR"
        :titulo="TEXTOS.administracao.NOTIFICACOES_MODAL_REENVIAR_TITULO"
        variant="danger"
        @confirmar="reenviar"
    >
      <p v-if="linhaSelecionada">
        {{ TEXTOS.administracao.NOTIFICACOES_MODAL_REENVIAR_TEXTO(linhaSelecionada.unidadeSigla) }}
      </p>
    </ModalConfirmacao>
  </LayoutPadrao>
</template>

<script lang="ts" setup>
import {computed, defineComponent, h, onMounted, ref} from "vue";
import {RouterLink} from "vue-router";
import {BAlert, BBadge, BButton, BSpinner, BTable} from "bootstrap-vue-next";
import LayoutPadrao from "@/components/layout/LayoutPadrao.vue";
import PageHeader from "@/components/layout/PageHeader.vue";
import ModalConfirmacao from "@/components/comum/ModalConfirmacao.vue";
import {TEXTOS} from "@/constants/textos";
import {
  listarResumoSubprocessosAtivos,
  type NotificacaoSubprocessoResumo,
  reenviarFalhasDefinitivas,
  type StatusGeralNotificacao,
} from "@/services/notificacaoService";
import {formatDateTimeBR, formatSituacaoSubprocesso} from "@/utils";
import {normalizeError} from "@/utils/apiError";
import {useNotification} from "@/composables/useNotification";

const {notify} = useNotification();

const linhas = ref<NotificacaoSubprocessoResumo[]>([]);
const carregando = ref(true);
const erro = ref<string | null>(null);
const linhaSelecionada = ref<NotificacaoSubprocessoResumo | null>(null);
const mostrarModalReenvio = ref(false);
const reenviando = ref(false);

const camposBase = [
  {key: "unidadeSigla", label: TEXTOS.administracao.NOTIFICACOES_CAMPOS.UNIDADE},
  {key: "processoDescricao", label: TEXTOS.administracao.NOTIFICACOES_CAMPOS.PROCESSO},
  {key: "statusGeral", label: TEXTOS.administracao.NOTIFICACOES_CAMPOS.STATUS},
];

const camposPendentes = [
  ...camposBase,
  {key: "ultimoErro", label: TEXTOS.administracao.NOTIFICACOES_CAMPOS.ERRO},
  {key: "proximaTentativaEm", label: TEXTOS.administracao.NOTIFICACOES_CAMPOS.PROXIMA_TENTATIVA},
  {key: "acoes", label: TEXTOS.administracao.NOTIFICACOES_CAMPOS.ACOES, thClass: "text-end", tdClass: "text-end"},
];

const camposConcluidas = [
  ...camposBase,
  {key: "dataHoraEnvio", label: TEXTOS.administracao.NOTIFICACOES_CAMPOS.CONCLUSAO},
];

const notificacoesPendentes = computed(() => ordenarLinhas(
    linhas.value.filter(item => item.statusGeral !== "OK")
));

const notificacoesConcluidas = computed(() => ordenarLinhas(
    linhas.value.filter(item => item.statusGeral === "OK")
));

const UnidadeLink = defineComponent({
  props: {
    item: {
      type: Object as () => NotificacaoSubprocessoResumo,
      required: true,
    },
  },
  setup(props) {
    return () => h("div", [
      h(RouterLink, {
        to: {
          name: "Subprocesso",
          params: {
            codProcesso: props.item.processoCodigo,
            siglaUnidade: props.item.unidadeSigla,
          },
        },
        class: "fw-semibold",
      }, () => props.item.unidadeSigla),
      h("div", {class: "text-muted small"}, formatSituacaoSubprocesso(props.item.situacaoSubprocesso)),
    ]);
  },
});

function ordenarLinhas(lista: NotificacaoSubprocessoResumo[]) {
  return [...lista].sort((a, b) => {
    const prioridade = pesoStatus(b.statusGeral) - pesoStatus(a.statusGeral);
    if (prioridade !== 0) return prioridade;
    return a.unidadeSigla.localeCompare(b.unidadeSigla) || a.processoDescricao.localeCompare(b.processoDescricao);
  });
}

function pesoStatus(status: StatusGeralNotificacao): number {
  const pesos: Record<StatusGeralNotificacao, number> = {
    INCONSISTENTE: 5,
    FALHA_DEFINITIVA: 4,
    FALHA_TEMPORARIA: 3,
    PENDENTE: 2,
    OK: 1,
  };
  return pesos[status];
}

function statusLabel(status: StatusGeralNotificacao): string {
  const labels: Record<StatusGeralNotificacao, string> = {
    INCONSISTENTE: "Inconsistente",
    OK: "Enviada",
    PENDENTE: "Pendente",
    FALHA_TEMPORARIA: "Falha temporária",
    FALHA_DEFINITIVA: "Falha definitiva",
  };
  return labels[status];
}

function statusVariant(status: StatusGeralNotificacao) {
  const variants = {
    INCONSISTENTE: "danger",
    OK: "success",
    PENDENTE: "primary",
    FALHA_TEMPORARIA: "warning",
    FALHA_DEFINITIVA: "danger",
  } as const satisfies Record<StatusGeralNotificacao, "success" | "primary" | "warning" | "danger">;
  return variants[status];
}

function formatarDataOuHifen(valor: string | null): string {
  if (!valor) return "-";
  const formatada = formatDateTimeBR(valor);
  return formatada === "Não informado" || formatada === "Data inválida" ? "-" : formatada;
}

function rowClass(item: NotificacaoSubprocessoResumo | null, type = "row") {
  if (!item || type !== "row") return "";
  if (item.statusGeral === "FALHA_DEFINITIVA" || item.statusGeral === "INCONSISTENTE") return "table-danger";
  if (item.statusGeral === "FALHA_TEMPORARIA") return "table-warning";
  if (item.statusGeral === "PENDENTE") return "table-primary";
  return "";
}

async function carregar() {
  carregando.value = true;
  erro.value = null;
  try {
    linhas.value = await listarResumoSubprocessosAtivos();
  } catch (error) {
    erro.value = normalizeError(error).message || TEXTOS.administracao.NOTIFICACOES_ERRO_CARREGAR;
  } finally {
    carregando.value = false;
  }
}

function confirmarReenvio(item: NotificacaoSubprocessoResumo) {
  linhaSelecionada.value = item;
  mostrarModalReenvio.value = true;
}

async function reenviar() {
  if (!linhaSelecionada.value) return;
  reenviando.value = true;
  try {
    const resultado = await reenviarFalhasDefinitivas(linhaSelecionada.value.subprocessoCodigo);
    notify(TEXTOS.administracao.NOTIFICACOES_SUCESSO_REENVIO(resultado.reenfileiradas), "success");
    mostrarModalReenvio.value = false;
    linhaSelecionada.value = null;
    await carregar();
  } catch (error) {
    notify(normalizeError(error).message || TEXTOS.administracao.NOTIFICACOES_ERRO_REENVIO, "danger");
  } finally {
    reenviando.value = false;
  }
}

onMounted(carregar);
</script>

<style scoped>
.erro-notificacao {
  max-width: 28rem;
}
</style>
