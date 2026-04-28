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
          :dismissible="notificacao.dismissible"
          :message="notificacao.message"
          :notification="notificacao.notification"
          :stack-trace="notificacao.stackTrace"
          :variant="notificacao.variant"
          @dismissed="clear"
      />

      <div class="painel-filtros mb-4">
        <div class="row g-3 align-items-end">
          <div class="col-12 col-lg-8">
            <label class="form-label" for="filtro-notificacoes-busca">
              {{ TEXTOS.administracao.NOTIFICACOES_BUSCA_LABEL }}
            </label>
            <input
                id="filtro-notificacoes-busca"
                v-model.trim="termoBusca"
                class="form-control"
                data-testid="input-notificacoes-busca"
                :placeholder="TEXTOS.administracao.NOTIFICACOES_BUSCA_PLACEHOLDER"
                type="search"
            >
          </div>
          <div class="col-12 col-lg-4">
            <label class="form-label" for="filtro-notificacoes-situacao">
              {{ TEXTOS.administracao.NOTIFICACOES_FILTRO_SITUACAO_LABEL }}
            </label>
            <select
                id="filtro-notificacoes-situacao"
                v-model="filtroSituacao"
                class="form-select"
                data-testid="select-notificacoes-situacao"
            >
              <option value="TODAS">{{ TEXTOS.administracao.NOTIFICACOES_FILTRO_SITUACAO_TODAS }}</option>
              <option value="PENDENTE">Pendente</option>
              <option value="ENVIANDO">Enviando...</option>
              <option value="ENVIADO">Enviado</option>
              <option value="FALHA_TEMPORARIA">Falha temporária</option>
              <option value="FALHA_DEFINITIVA">Falha definitiva</option>
            </select>
          </div>
        </div>
      </div>

      <section class="mb-5" data-testid="sec-notificacoes-concluidas">
        <h2 class="h4 mb-3">{{ TEXTOS.administracao.NOTIFICACOES_CONCLUIDAS_TITULO }}</h2>
        <EmptyState
            v-if="enviadas.length === 0"
            :title="TEXTOS.administracao.NOTIFICACOES_SEM_CONCLUIDAS"
            data-testid="alert-notificacoes-sem-concluidas"
            icon="bi-info-circle"
        />
        <BTable
            v-else
            :fields="camposConcluidas"
            :items="enviadas"
            data-testid="tbl-notificacoes-concluidas"
            hover
            responsive
            small
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

          <template #cell(dataHoraEnvio)="{ item }">
            {{ formatarDataOuHifen(item.dataHoraEnvio) }}
          </template>

          <template #cell(acoes)="{ item }">
            <div class="text-end">
              <BButton
                  v-if="item.corpoHtml"
                  size="sm"
                  variant="outline-secondary"
                  class="btn-preview me-2"
                  :data-testid="`btn-preview-${item.codigo}`"
                  title="Ver conteúdo do e-mail"
                  @click="abrirPreview(item)"
              >
                <i aria-hidden="true" class="bi bi-eye"></i>
                <span>Preview</span>
              </BButton>
            </div>
          </template>
        </BTable>
      </section>

      <section data-testid="sec-notificacoes-pendentes">
        <h2 class="h4 mb-3">{{ TEXTOS.administracao.NOTIFICACOES_PENDENTES_TITULO }}</h2>
        <EmptyState
            v-if="pendentes.length === 0"
            :title="TEXTOS.administracao.NOTIFICACOES_SEM_PENDENCIAS"
            data-testid="alert-notificacoes-sem-pendencias"
            icon="bi-check-circle"
        />
        <BTable
            v-else
            :fields="camposPendentes"
            :items="pendentes"
            data-testid="tbl-notificacoes-pendentes"
            hover
            responsive
            small
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
            <BBadge :variant="statusVariant(item.situacao)">
              {{ statusLabel(item.situacao) }}
            </BBadge>
            <div v-if="item.tentativas > 0" class="text-muted small">
              {{ item.tentativas }} tentativa(s)
            </div>
          </template>

          <template #cell(ultimoErro)="{ item }">
            <div class="text-truncate text-muted small" style="max-width: 200px;" :title="item.ultimoErro">
              {{ item.ultimoErro || '-' }}
            </div>
          </template>

          <template #cell(proximaTentativaEm)="{ item }">
             {{ formatarDataOuHifen(item.proximaTentativaEm) }}
          </template>

          <template #cell(acoes)="{ item }">
            <div class="text-end d-flex justify-content-end align-items-center">
              <BButton
                  v-if="item.corpoHtml"
                  size="sm"
                  variant="outline-secondary"
                  class="btn-preview me-2"
                  title="Ver conteúdo do e-mail"
                  @click="abrirPreview(item)"
              >
                <i aria-hidden="true" class="bi bi-eye"></i>
                <span>Preview</span>
              </BButton>
              <BButton
                  v-if="item.situacao === 'FALHA_DEFINITIVA'"
                  :data-testid="`btn-notificacoes-reenviar-${item.codigo}`"
                  size="sm"
                  variant="outline-dark"
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

    <!-- Modal de Preview -->
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
        <p class="text-muted small mb-2">{{ TEXTOS.administracao.NOTIFICACOES_PREVIEW_AVISO }}</p>
        <iframe
            class="email-content-preview"
            data-testid="iframe-preview-email"
            sandbox=""
            :srcdoc="montarPreviewHtml(itemParaPreview.corpoHtml)"
            title="Preview do e-mail"
        />
      </div>
    </BModal>

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
import AppAlert from "@/components/comum/AppAlert.vue";
import EmptyState from "@/components/comum/EmptyState.vue";
import {TEXTOS} from "@/constants/textos";
import {
  listarNotificacoesAdmin,
  type Notificacao,
  reenviarNotificacao,
  type StatusNotificacao,
} from "@/services/notificacaoService";
import {formatDateTimeBR} from "@/utils";
import {normalizeError} from "@/utils/apiError";
import {useNotification} from "@/composables/useNotification";

const {notificacao, notify, clear} = useNotification();

const itens = ref<Notificacao[]>([]);
const carregando = ref(true);
const erro = ref<string | null>(null);
const termoBusca = ref("");
const filtroSituacao = ref<StatusNotificacao | "TODAS">("TODAS");
const itemSelecionado = ref<Notificacao | null>(null);
const itemParaPreview = ref<Notificacao | null>(null);
const mostrarModalReenvio = ref(false);
const mostrarPreview = ref(false);
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

const camposBase = [
  {key: "destinatario", label: TEXTOS.administracao.NOTIFICACOES_CAMPOS.DESTINATARIO, thClass: "col-destinatario", tdClass: "col-destinatario"},
  {key: "tipoNotificacao", label: TEXTOS.administracao.NOTIFICACOES_CAMPOS.TIPO, thClass: "col-tipo", tdClass: "col-tipo"},
  {key: "assunto", label: "Assunto"},
];

const camposConcluidas = [
  ...camposBase,
  {key: "dataHoraEnvio", label: TEXTOS.administracao.NOTIFICACOES_CAMPOS.CONCLUSAO, thClass: "col-data", tdClass: "col-data"},
  {key: "acoes", label: "", thClass: "text-end col-acoes", tdClass: "text-end col-acoes"},
];

const camposPendentes = [
  ...camposBase,
  {key: "situacao", label: TEXTOS.administracao.NOTIFICACOES_CAMPOS.STATUS, thClass: "col-status", tdClass: "col-status"},
  {key: "ultimoErro", label: TEXTOS.administracao.NOTIFICACOES_CAMPOS.ERRO},
  {key: "proximaTentativaEm", label: TEXTOS.administracao.NOTIFICACOES_CAMPOS.PROXIMA_TENTATIVA, thClass: "col-data", tdClass: "col-data"},
  {key: "acoes", label: "", thClass: "text-end col-acoes", tdClass: "text-end col-acoes"},
];

const itensFiltrados = computed(() => itens.value.filter(filtrarNotificacao));
const enviadas = computed(() => itensFiltrados.value.filter(i => i.situacao === "ENVIADO"));
const pendentes = computed(() => itensFiltrados.value.filter(i => i.situacao !== "ENVIADO"));

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

function formatarDataOuHifen(valor?: string | null): string {
  if (!valor) return "-";
  const formatada = formatDateTimeBR(valor);
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
  if (item.unidadeSigla?.trim()) {
    return item.unidadeSigla.trim().toUpperCase();
  }
  return item.destinatario.trim();
}

function formatarAssunto(assunto?: string): string {
  return assunto?.replace(/^SGC:\s*/i, "").trim() || "-";
}

function filtrarNotificacao(item: Notificacao): boolean {
  if (filtroSituacao.value !== "TODAS" && item.situacao !== filtroSituacao.value) {
    return false;
  }

  const termo = termoBusca.value.trim().toLocaleLowerCase("pt-BR");
  if (!termo) {
    return true;
  }

  const conteudo = [
    item.unidadeSigla,
    item.processoDescricao,
    item.assunto,
    item.destinatario,
    formatarDestinatario(item),
    item.tipoNotificacao,
    formatarTipoNotificacao(item.tipoNotificacao),
    item.usuarioDestinoTitulo,
    item.ultimoErro,
  ].filter(Boolean).join(" ").toLocaleLowerCase("pt-BR");

  return conteudo.includes(termo);
}

function montarPreviewHtml(corpoHtml?: string): string {
  const conteudo = corpoHtml?.trim() || "<p>Conteúdo indisponível.</p>";
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
        color: #212529;
        background: #fff;
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

async function carregar() {
  carregando.value = true;
  erro.value = null;
  try {
    itens.value = await listarNotificacoesAdmin();
  } catch (error) {
    erro.value = normalizeError(error).message || TEXTOS.administracao.NOTIFICACOES_ERRO_CARREGAR;
  } finally {
    carregando.value = false;
  }
}

function abrirPreview(item: Notificacao) {
  itemParaPreview.value = item;
  mostrarPreview.value = true;
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
    notify(normalizeError(error).message || "Erro ao reenviar e-mail", "danger");
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
:deep(.col-data) { width: 9rem; }
:deep(.col-acoes) { width: 6rem; }

.painel-filtros {
  background: #f8f9fa;
  border: 1px solid var(--bs-border-color-translucent);
  border-radius: 0.75rem;
  padding: 1rem;
}

.linha-assunto {
  min-width: 0;
}

.btn-preview {
  display: inline-flex;
  align-items: center;
  gap: 0.35rem;
  padding: 0.25rem 0.6rem;
  border-color: #adb5bd;
  color: #495057;
}

.btn-preview:hover,
.btn-preview:focus {
  background: #f1f3f5;
  border-color: #868e96;
  color: #212529;
}

.email-content-preview {
  width: 100%;
  min-height: 420px;
  max-height: 60vh;
  border: 1px solid var(--bs-border-color);
  border-radius: 0.5rem;
  background: #fff;
}
</style>
