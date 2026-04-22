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
      <AppAlert
          v-if="notificacao"
          :dismissible="notificacao.dismissible"
          :message="notificacao.message"
          :notification="notificacao.notification"
          :stack-trace="notificacao.stackTrace"
          :variant="notificacao.variant"
          @dismissed="clear"
      />

      <section class="mb-5" data-testid="sec-notificacoes-pendentes">
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
            data-testid="alert-notificacoes-sem-pendencias"
            variant="success"
        >
          {{ TEXTOS.administracao.NOTIFICACOES_SEM_PENDENCIAS }}
        </BAlert>
        <BTable
            v-else
            :fields="camposPendentes"
            :items="notificacoesPendentes"
            data-testid="tbl-notificacoes-pendentes"
            hover
            responsive
            small
        >
          <template #cell(unidadeSigla)="{ item }">
            <UnidadeLink :item="item"/>
          </template>

          <template #cell(processoDescricao)="{ item }">
            <span :data-testid="`notificacao-processo-${item.unidadeSigla}`">
              {{ item.processoDescricao }}
            </span>
          </template>

          <template #cell(statusGeral)="{ item }">
            <BBadge :data-testid="`notificacao-status-${item.unidadeSigla}`" :variant="statusVariant(item.statusGeral)">
              {{ statusLabel(item.statusGeral) }}
            </BBadge>
            <div v-if="item.maiorTentativas > 0" class="text-muted small mt-1">
              {{ item.maiorTentativas }} tentativa(s)
            </div>
          </template>

          <template #cell(ultimoErro)="{ item }">
            <span
                v-if="item.ultimoErro"
                :data-testid="`notificacao-erro-${item.unidadeSigla}`"
                :title="item.ultimoErro"
                class="text-muted text-truncate d-inline-block erro-notificacao"
            >
              {{ item.ultimoErro }}
            </span>
            <span v-else :data-testid="`notificacao-erro-${item.unidadeSigla}`">-</span>
          </template>

          <template #cell(proximaTentativaEm)="{ item }">
            <span :data-testid="`notificacao-proxima-tentativa-${item.unidadeSigla}`">
              {{ formatarDataOuHifen(item.proximaTentativaEm) }}
            </span>
          </template>

          <template #cell(acoes)="{ item }">
            <div class="text-end">
              <BButton
                  v-if="item.podeReenviar"
                  :data-testid="`btn-notificacoes-reenviar-${item.unidadeSigla}`"
                  size="sm"
                  variant="outline-dark"
                  @click="confirmarReenvio(item)"
              >
                <i aria-hidden="true" class="bi bi-send"></i>
                {{ TEXTOS.administracao.NOTIFICACOES_REENVIAR }}
              </BButton>
            </div>
          </template>
        </BTable>
      </section>

      <section data-testid="sec-notificacoes-concluidas">
        <PageHeader :title="TEXTOS.administracao.NOTIFICACOES_CONCLUIDAS_TITULO"/>
        <BAlert
            v-if="notificacoesConcluidas.length === 0"
            :model-value="true"
            data-testid="alert-notificacoes-sem-concluidas"
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

          <template #cell(processoDescricao)="{ item }">
            <span :data-testid="`notificacao-processo-${item.unidadeSigla}`">
              {{ item.processoDescricao }}
            </span>
          </template>

          <template #cell(statusGeral)="{ item }">
            <BBadge :data-testid="`notificacao-status-${item.unidadeSigla}`" :variant="statusVariant(item.statusGeral)">
              {{ statusLabel(item.statusGeral) }}
            </BBadge>
          </template>

          <template #cell(dataHoraEnvio)="{ item }">
            <span :data-testid="`notificacao-conclusao-${item.unidadeSigla}`">
              {{ formatarDataOuHifen(item.ultimaNotificacaoEm) }}
            </span>
          </template>
        </BTable>
      </section>
    </template>

    <ModalConfirmacao
        v-model="mostrarModalReenvio"
        :auto-close="false"
        :loading="reenviando"
        :ok-title="TEXTOS.administracao.NOTIFICACOES_REENVIAR"
        test-codigo-confirmar="btn-notificacoes-reenviar-confirmar"
        :titulo="TEXTOS.administracao.NOTIFICACOES_MODAL_REENVIAR_TITULO"
        variant="danger"
        @confirmar="reenviar"
    >
      <p v-if="linhaSelecionada" data-testid="txt-notificacoes-reenviar-confirmacao">
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
import AppAlert from "@/components/comum/AppAlert.vue";
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

const {notificacao, notify, clear} = useNotification();

const linhas = ref<NotificacaoSubprocessoResumo[]>([]);
const carregando = ref(true);
const erro = ref<string | null>(null);
const linhaSelecionada = ref<NotificacaoSubprocessoResumo | null>(null);
const mostrarModalReenvio = ref(false);
const reenviando = ref(false);

const camposBase = [
  {key: "unidadeSigla", label: TEXTOS.administracao.NOTIFICACOES_CAMPOS.UNIDADE},
  {key: "statusGeral", label: TEXTOS.administracao.NOTIFICACOES_CAMPOS.STATUS},
  {key: "processoDescricao", label: TEXTOS.administracao.NOTIFICACOES_CAMPOS.PROCESSO},
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
        "data-testid": `notificacao-unidade-${props.item.unidadeSigla}`,
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
