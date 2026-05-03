<template>
  <LayoutPadrao>
    <PageHeader :title="TEXTOS.comum.MENU_NOTIFICACOES">
      <template #actions>
        <BButton
            data-testid="btn-notificacoes-atualizar"
            :disabled="carregando"
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

    <template v-else>
      <AppAlert
          v-if="notificacao"
          :dispensavel="notificacao.dispensavel"
          :mensagem="notificacao.mensagem"
          :notification="notificacao.notificacao"
          :stack-trace="notificacao.stackTrace"
          :variante="notificacao.variante"
          @dismissed="clear"
      />

      <section class="mb-5" data-testid="sec-notificacoes">
        <EmptyState
            v-if="itensOrdenados.length === 0"
            :title="TEXTOS.administracao.NOTIFICACOES_SEM_REGISTROS"
            data-testid="alert-notificacoes-sem-registros"
            icon="bi-info-circle"
        />
        <BTable
            v-else
            :fields="camposTabela"
            :items="itensOrdenados"
            data-testid="tbl-notificacoes"
            hover
            responsive
            small
            sort-icon-left
        >
          <template #cell(destinatario)="{ item }">
            <div class="fw-semibold" :title="item.destinatario">
              {{ formatarDestinatario(item) }}
            </div>
          </template>

          <template #cell(tipoNotificacao)="{ item }">
            <span>{{ formatarTipoNotificacao(item.tipoNotificacao) }}</span>
          </template>

          <template #cell(assunto)="{ item }">
            <div class="linha-assunto" :title="item.assunto">
              <div class="fw-semibold">{{ formatarAssunto(item.assunto) }}</div>
              <div class="text-muted small">
                {{ resumirContexto(item) }}
              </div>
            </div>
          </template>

          <template #cell(situacao)="{ item }">
            <BBadge :variante="statusVariant(item.situacao)">
              {{ statusLabel(item.situacao) }}
            </BBadge>
          </template>

          <template #cell(quando)="{ item }">
            {{ formatarQuando(item) }}
          </template>

          <template #cell(acoes)="{ item }">
            <div class="text-end d-flex justify-content-end align-items-center gap-2">
              <BButton
                  size="sm"
                  variant="outline-secondary"
                  class="btn-acao"
                  :data-testid="`btn-detalhes-${item.codigo}`"
                  :title="TEXTOS.administracao.NOTIFICACOES_DETALHES"
                  @click="abrirDetalhes(item)"
              >
                <i aria-hidden="true" class="bi bi-info-circle"></i>
              </BButton>
              <BButton
                  v-if="item.corpoHtml"
                  size="sm"
                  variant="outline-secondary"
                  class="btn-acao"
                  :data-testid="`btn-preview-${item.codigo}`"
                  title="Ver conteúdo do e-mail"
                  @click="abrirPreview(item)"
              >
                <i aria-hidden="true" class="bi bi-eye"></i>
              </BButton>
              <BButton
                  v-if="item.situacao === 'FALHA_DEFINITIVA'"
                  :data-testid="`btn-notificacoes-reenviar-${item.codigo}`"
                  size="sm"
                  variant="outline-dark"
                  class="btn-acao"
                  title="Tentar reenviar e-mail"
                  @click="confirmarReenvio(item)"
              >
                <i aria-hidden="true" class="bi bi-send"></i>
              </BButton>
            </div>
          </template>
        </BTable>
      </section>
    </template>

    <BModal
        v-model="mostrarPreview"
        :title="itemParaPreview?.assunto"
        size="lg"
        hide-footer
        scrollable
        data-testid="modal-preview-email"
    >
      <div v-if="itemParaPreview" class="p-3">
        <div class="mb-3 border-bottom pb-2">
          <strong>{{ TEXTOS.administracao.NOTIFICACOES_PREVIEW_DESTINATARIO }}:</strong> {{ itemParaPreview.destinatario }}<br>
          <strong>{{ TEXTOS.administracao.NOTIFICACOES_PREVIEW_CRIACAO }}:</strong> {{ formatarDataOuHifen(itemParaPreview.dataHoraCriacao) }}
        </div>
        <iframe
            class="email-content-preview"
            data-testid="iframe-preview-email"
            sandbox=""
            :srcdoc="montarPreviewHtml(itemParaPreview.corpoHtml)"
            title="Preview do e-mail"
        />
      </div>
    </BModal>

    <ModalPadrao
        v-model="mostrarDetalhes"
        data-testid="modal-detalhes-notificacao"
        :mostrar-botao-acao="false"
        tamanho="lg"
        texto-cancelar="Fechar"
        titulo="Detalhes da notificação"
    >
      <div v-if="itemParaDetalhes" class="p-3">
        <dl class="row mb-0 detalhes-notificacao">
          <dt class="col-sm-4">Destinatário</dt>
          <dd class="col-sm-8">{{ formatarDestinatario(itemParaDetalhes) }}</dd>

          <dt class="col-sm-4">E-mail</dt>
          <dd class="col-sm-8 text-break">{{ itemParaDetalhes.destinatario }}</dd>

          <dt class="col-sm-4">Tipo</dt>
          <dd class="col-sm-8">{{ formatarTipoNotificacao(itemParaDetalhes.tipoNotificacao) }}</dd>

          <dt class="col-sm-4">Situação</dt>
          <dd class="col-sm-8">{{ statusLabel(itemParaDetalhes.situacao) }}</dd>

          <dt class="col-sm-4">Criado em</dt>
          <dd class="col-sm-8">{{ formatarDataOuHifen(itemParaDetalhes.dataHoraCriacao) }}</dd>

          <dt class="col-sm-4">Enviado em</dt>
          <dd class="col-sm-8">{{ formatarDataOuHifen(itemParaDetalhes.dataHoraEnvio) }}</dd>

          <dt class="col-sm-4">Próxima tentativa</dt>
          <dd class="col-sm-8">{{ formatarDataOuHifen(itemParaDetalhes.proximaTentativaEm) }}</dd>

          <dt class="col-sm-4">Falhas anteriores</dt>
          <dd class="col-sm-8">{{ itemParaDetalhes.tentativas }}</dd>

          <dt class="col-sm-4">Último erro</dt>
          <dd class="col-sm-8 text-break">{{ itemParaDetalhes.ultimoErro || "-" }}</dd>
        </dl>
      </div>
    </ModalPadrao>

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
      <p v-if="itemSelecionado" data-testid="txt-notificacoes-reenviar-confirmacao">
        Confirma o reenvio deste e-mail específico para {{ itemSelecionado.destinatario }}?
      </p>
    </ModalConfirmacao>
  </LayoutPadrao>
</template>

<script lang="ts" setup>
import {computed, onMounted, ref} from "vue";
import {BAlert, BBadge, BButton, BModal, BSpinner, BTable} from "bootstrap-vue-next";
import LayoutPadrao from "@/components/layout/LayoutPadrao.vue";
import PageHeader from "@/components/layout/PageHeader.vue";
import ModalConfirmacao from "@/components/comum/ModalConfirmacao.vue";
import ModalPadrao from "@/components/comum/ModalPadrao.vue";
import AppAlert from "@/components/comum/AppAlert.vue";
import EmptyState from "@/components/comum/EmptyState.vue";
import {TEXTOS} from "@/constants/textos";
import {
  listarNotificacoesAdmin,
  type Notificacao,
  reenviarNotificacao,
  type StatusNotificacao,
} from "@/services/notificacaoService";
import {formatarDataHoraBR} from "@/utils";
import {normalizarErro} from "@/utils/apiError";
import {useNotification} from "@/composables/useNotification";

const {notificacao, notify, clear} = useNotification();

const itens = ref<Notificacao[]>([]);
const carregando = ref(true);
const erro = ref<string | null>(null);
const itemSelecionado = ref<Notificacao | null>(null);
const itemParaPreview = ref<Notificacao | null>(null);
const itemParaDetalhes = ref<Notificacao | null>(null);
const mostrarModalReenvio = ref(false);
const mostrarPreview = ref(false);
const mostrarDetalhes = ref(false);
const reenviando = ref(false);

const TIPOS_NOTIFICACAO_LABELS: Record<string, string> = {
  PROCESSO_INICIADO: "Início do processo",
  PROCESSO_FINALIZADO: "Finalização do processo",
  DATA_LIMITE_ALTERADA: "Alteração da data limite",
  LEMBRETE_PRAZO: "Lembrete de prazo",
  ATRIBUICAO_TEMPORARIA: "Atribuição temporária",
  CADASTRO_DISPONIBILIZADO: "Cadastro disponibilizado",
  CADASTRO_DEVOLVIDO: "Cadastro devolvido para ajustes",
  CADASTRO_ACEITO: "Cadastro aceito",
  CADASTRO_HOMOLOGADO: "Cadastro homologado",
  CADASTRO_REABERTO: "Cadastro reaberto",
  REVISAO_CADASTRO_DISPONIBILIZADA: "Revisão de cadastro disponibilizada",
  REVISAO_CADASTRO_DEVOLVIDA: "Revisão de cadastro devolvida",
  REVISAO_CADASTRO_ACEITA: "Revisão de cadastro aceita",
  REVISAO_CADASTRO_HOMOLOGADA: "Revisão de cadastro homologada",
  REVISAO_CADASTRO_REABERTA: "Revisão de cadastro reaberta",
  MAPA_DISPONIBILIZADO: "Mapa disponibilizado",
  MAPA_SUGESTOES_APRESENTADAS: "Sugestões apresentadas para o mapa",
  MAPA_VALIDADO: "Mapa validado",
  MAPA_VALIDACAO_DEVOLVIDA: "Validação do mapa devolvida",
  MAPA_VALIDACAO_ACEITA: "Validação do mapa aceita",
  MAPA_HOMOLOGADO: "Mapa homologado",
};

const camposTabela = [
  {key: "destinatario", label: TEXTOS.administracao.NOTIFICACOES_CAMPOS.DESTINATARIO, thClass: "col-destinatario", tdClass: "col-destinatario", sortable: true},
  {key: "tipoNotificacao", label: TEXTOS.administracao.NOTIFICACOES_CAMPOS.TIPO, thClass: "col-tipo", tdClass: "col-tipo", sortable: true, formatter: ({value, item}: {value: unknown, item: Notificacao}) => formatarTipoNotificacao(typeof value === "string" ? value : item?.tipoNotificacao)},
  {key: "assunto", label: "Assunto", sortable: true, formatter: ({value}: {value: unknown}) => formatarAssunto(typeof value === "string" ? value : undefined)},
  {key: "situacao", label: TEXTOS.administracao.NOTIFICACOES_CAMPOS.STATUS, thClass: "col-status", tdClass: "col-status", sortable: true},
  {key: "quando", label: TEXTOS.administracao.NOTIFICACOES_CAMPOS.QUANDO, thClass: "col-data", tdClass: "col-data", sortable: true, formatter: ({item}: {item: Notificacao}) => item ? formatarQuando(item) : "-"},
  {key: "acoes", label: "", thClass: "text-end col-acoes", tdClass: "text-end col-acoes"},
];

const itensOrdenados = computed(() => [...itens.value].sort(compararNotificacoes));

function statusLabel(status: StatusNotificacao): string {
  const labels: Record<StatusNotificacao, string> = {
    ENVIADO: "Enviado",
    PENDENTE: "Pendente",
    ENVIANDO: "Enviando...",
    FALHA_TEMPORARIA: "Falha temporária",
    FALHA_DEFINITIVA: "Falha definitiva",
  };
  return labels[status];
}

function statusVariant(status: StatusNotificacao) {
  const variants = {
    ENVIADO: "success",
    PENDENTE: "secondary",
    ENVIANDO: "primary",
    FALHA_TEMPORARIA: "warning",
    FALHA_DEFINITIVA: "danger",
  } as const;
  return variants[status];
}

function prioridadeStatus(status: StatusNotificacao): number {
  const prioridades: Record<StatusNotificacao, number> = {
    FALHA_DEFINITIVA: 0,
    FALHA_TEMPORARIA: 1,
    PENDENTE: 2,
    ENVIANDO: 3,
    ENVIADO: 4,
  };
  return prioridades[status];
}

function formatarDataOuHifen(valor?: string | null): string {
  if (!valor) return "-";
  const formatada = formatarDataHoraBR(valor);
  return formatada === "Não informado" || formatada === "Data inválida" ? "-" : formatada;
}

function resumirContexto(item: Notificacao): string {
  return [
    item.processoDescricao,
    item.usuarioDestinoTitulo ? `Título ${item.usuarioDestinoTitulo}` : null,
  ].filter(Boolean).join(" • ") || "Sem contexto adicional";
}

function formatarTipoNotificacao(tipo?: string): string {
  if (!tipo) {
    return "-";
  }
  return TIPOS_NOTIFICACAO_LABELS[tipo] || tipo;
}

function formatarDestinatario(item: Notificacao): string {
  if (item.usuarioDestinoTitulo?.trim()) {
    return item.destinatario.trim();
  }
  if (item.unidadeSigla?.trim()) {
    return item.unidadeSigla.trim().toUpperCase();
  }
  const correspondenciaEmailInstitucional = item.destinatario.trim().match(/^([^@]+)@tre-pe\.jus\.br$/i);
  if (correspondenciaEmailInstitucional?.[1]) {
    return correspondenciaEmailInstitucional[1].toUpperCase();
  }
  return item.destinatario.trim();
}

function formatarAssunto(assunto?: string): string {
  return assunto?.replace(/^SGC:\s*/i, "").trim() || "-";
}

function formatarQuando(item: Notificacao): string {
  if (item.situacao === "ENVIADO") {
    return formatarDataOuHifen(item.dataHoraEnvio);
  }
  return formatarDataOuHifen(item.proximaTentativaEm || item.dataHoraCriacao);
}

function compararNotificacoes(a: Notificacao, b: Notificacao): number {
  const prioridade = prioridadeStatus(a.situacao) - prioridadeStatus(b.situacao);
  if (prioridade !== 0) {
    return prioridade;
  }
  return obterTimestampOrdenacao(b) - obterTimestampOrdenacao(a);
}

function obterTimestampOrdenacao(item: Notificacao): number {
  const referencia = item.proximaTentativaEm || item.dataHoraEnvio || item.dataHoraCriacao;
  const timestamp = referencia ? Date.parse(referencia) : Number.NaN;
  return Number.isNaN(timestamp) ? 0 : timestamp;
}

function montarPreviewHtml(corpoHtml?: string): string {
  const conteudo = limparHtmlPreview(corpoHtml?.trim() || "<p>Conteúdo indisponível.</p>");
  return `<!DOCTYPE html>
<html lang="pt-BR">
  <head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <style>
      body {
        margin: 0;
        padding: 16px;
        font-family: Arial, sans-serif;
        line-height: 1.5;
        color: var(--bs-body-color, #212529);
        background: var(--bs-body-bg, #fff);
        overflow-wrap: anywhere;
      }
      img, table, pre {
        max-width: 100%;
      }
      table {
        display: block;
        overflow-x: auto;
      }
    </style>
  </head>
  <body>${conteudo}</body>
</html>`;
}

function limparHtmlPreview(html: string): string {
  return html
      .replace(/<script\b[^<]*(?:(?!<\/script>)<[^<]*)*<\/script>/gi, "")
      .replace(/\son\w+=(["']).*?\1/gi, "")
      .replace(/\son\w+=([^\s>]+)/gi, "");
}

async function carregar() {
  carregando.value = true;
  erro.value = null;
  try {
    itens.value = await listarNotificacoesAdmin();
  } catch (error) {
    erro.value = normalizarErro(error).mensagem || TEXTOS.administracao.NOTIFICACOES_ERRO_CARREGAR;
  } finally {
    carregando.value = false;
  }
}

function abrirPreview(item: Notificacao) {
  itemParaPreview.value = item;
  mostrarPreview.value = true;
}

function abrirDetalhes(item: Notificacao) {
  itemParaDetalhes.value = item;
  mostrarDetalhes.value = true;
}

function confirmarReenvio(item: Notificacao) {
  itemSelecionado.value = item;
  mostrarModalReenvio.value = true;
}

async function reenviar() {
  if (!itemSelecionado.value) return;
  reenviando.value = true;
  try {
    await reenviarNotificacao(itemSelecionado.value.codigo);
    notify("E-mail recolocado na fila de envio", "success");
    mostrarModalReenvio.value = false;
    itemSelecionado.value = null;
    await carregar();
  } catch (error) {
    notify(normalizarErro(error).mensagem || "Erro ao reenviar e-mail", "danger");
  } finally {
    reenviando.value = false;
  }
}

onMounted(carregar);
</script>

<style scoped>
:deep(.modal-dialog) {
  max-width: min(1100px, calc(100vw - 2rem));
}

:deep(.col-destinatario) { width: 12rem; }
:deep(.col-tipo) { width: 14rem; }
:deep(.col-status) { width: 10rem; }
:deep(.col-data) { width: 10rem; }
:deep(.col-acoes) { width: 8rem; }

.linha-assunto {
  min-width: 0;
}

.btn-acao {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 0.25rem 0.55rem;
  border-color: var(--bs-border-color);
  color: var(--bs-secondary-color);
}

.btn-acao:hover,
.btn-acao:focus {
  background: var(--bs-secondary-bg);
  border-color: var(--bs-secondary-color);
  color: var(--bs-body-color);
}

.detalhes-notificacao dt {
  font-weight: 600;
}

.email-content-preview {
  width: 100%;
  min-height: 420px;
  max-height: 60vh;
  border: 1px solid var(--bs-border-color);
  border-radius: 0.5rem;
  background: var(--bs-body-bg);
}
</style>
