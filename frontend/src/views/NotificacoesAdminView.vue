<template>
  <LayoutPadrao>
    <PageHeader :title="TEXTOS.administracao.NOTIFICACOES_TITULO">
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

    <div v-if="carregando" class="text-center py-5" data-testid="notificacoes-carregando">
      <BSpinner :label="TEXTOS.comum.CARREGANDO_DADOS" variant="primary"/>
      <p class="mt-2 text-muted">{{ TEXTOS.comum.CARREGANDO_DADOS }}</p>
    </div>

    <BAlert v-else-if="erro" :model-value="true" variant="danger">
      {{ erro }}
    </BAlert>

    <EmptyState
        v-else-if="linhasOrdenadas.length === 0"
        :description="TEXTOS.administracao.NOTIFICACOES_EMPTY_DESCRIPTION"
        icon="bi-envelope-check"
        :title="TEXTOS.administracao.NOTIFICACOES_EMPTY_TITLE"
    />

    <div v-else class="table-responsive">
      <BTable
          :fields="campos"
          :items="linhasOrdenadas"
          :tbody-tr-class="rowClass"
          data-testid="tbl-notificacoes"
          hover
          responsive
          small
      >
        <template #cell(processoDescricao)="{ item }">
          <div class="fw-semibold">{{ item.processoDescricao }}</div>
        </template>

        <template #cell(unidadeSigla)="{ item }">
          <RouterLink
              :to="{ name: 'Subprocesso', params: { codProcesso: item.processoCodigo, siglaUnidade: item.unidadeSigla } }"
              class="fw-semibold"
          >
            {{ item.unidadeSigla }}
          </RouterLink>
          <div class="text-muted small">{{ formatSituacaoSubprocesso(item.situacaoSubprocesso) }}</div>
        </template>

        <template #cell(situacaoSubprocesso)="{ item }">
          {{ formatSituacaoSubprocesso(item.situacaoSubprocesso) }}
        </template>

        <template #cell(statusGeral)="{ item }">
          <BBadge :variant="statusVariant(item.statusGeral)">
            {{ statusLabel(item.statusGeral) }}
          </BBadge>
          <div v-if="item.totalNotificacoes > 0" class="text-muted small mt-1">
            <span v-if="item.maiorTentativas > 0">{{ item.maiorTentativas }} tentativa(s)</span>
            <span v-if="item.ultimoErro" class="d-block text-danger text-truncate erro-notificacao" :title="item.ultimoErro">
              {{ item.ultimoErro }}
            </span>
          </div>
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
    </div>

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
import {computed, onMounted, ref} from "vue";
import {RouterLink} from "vue-router";
import {BAlert, BBadge, BButton, BSpinner, BTable} from "bootstrap-vue-next";
import LayoutPadrao from "@/components/layout/LayoutPadrao.vue";
import PageHeader from "@/components/layout/PageHeader.vue";
import EmptyState from "@/components/comum/EmptyState.vue";
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

const campos = [
  {key: "unidadeSigla", label: TEXTOS.administracao.NOTIFICACOES_CAMPOS.UNIDADE},
  {key: "processoDescricao", label: TEXTOS.administracao.NOTIFICACOES_CAMPOS.PROCESSO},
  {key: "statusGeral", label: TEXTOS.administracao.NOTIFICACOES_CAMPOS.STATUS},
  {key: "proximaTentativaEm", label: TEXTOS.administracao.NOTIFICACOES_CAMPOS.PROXIMA_TENTATIVA},
  {key: "acoes", label: TEXTOS.administracao.NOTIFICACOES_CAMPOS.ACOES, thClass: "text-end", tdClass: "text-end"},
];

const linhasOrdenadas = computed(() => [...linhas.value].sort((a, b) => {
  const prioridade = pesoStatus(b.statusGeral) - pesoStatus(a.statusGeral);
  if (prioridade !== 0) return prioridade;
  return a.processoDescricao.localeCompare(b.processoDescricao) || a.unidadeSigla.localeCompare(b.unidadeSigla);
}));

function pesoStatus(status: StatusGeralNotificacao): number {
  const pesos: Record<StatusGeralNotificacao, number> = {
    FALHA_DEFINITIVA: 5,
    FALHA_TEMPORARIA: 4,
    PENDENTE: 3,
    SEM_NOTIFICACAO: 2,
    OK: 1,
  };
  return pesos[status];
}

function statusLabel(status: StatusGeralNotificacao): string {
  const labels: Record<StatusGeralNotificacao, string> = {
    SEM_NOTIFICACAO: "-",
    OK: "Enviado",
    PENDENTE: "Pendente",
    FALHA_TEMPORARIA: "Falha temporária",
    FALHA_DEFINITIVA: "Falha definitiva",
  };
  return labels[status];
}

function statusVariant(status: StatusGeralNotificacao) {
  const variants = {
    SEM_NOTIFICACAO: "secondary",
    OK: "success",
    PENDENTE: "primary",
    FALHA_TEMPORARIA: "warning",
    FALHA_DEFINITIVA: "danger",
  } as const satisfies Record<StatusGeralNotificacao, "secondary" | "success" | "primary" | "warning" | "danger">;
  return variants[status];
}

function formatarDataOuHifen(valor: string | null): string {
  if (!valor) return "-";
  const formatada = formatDateTimeBR(valor);
  return formatada === "Não informado" || formatada === "Data inválida" ? "-" : formatada;
}

function rowClass(item: NotificacaoSubprocessoResumo | null, type = "row") {
  if (!item || type !== "row") return "";
  if (item.statusGeral === "FALHA_DEFINITIVA") return "table-danger";
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
  max-width: 24rem;
}
</style>
