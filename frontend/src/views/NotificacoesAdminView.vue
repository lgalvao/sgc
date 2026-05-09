<template>
  <LayoutPadrao>
    <CarregamentoPagina v-if="carregando"/>

    <template v-else>
      <PageHeader :title="TEXTOS.comum.MENU_NOTIFICACOES">
        <template #actions>
          <div class="d-flex flex-wrap justify-content-end gap-2">
              <a
                  v-if="mostrarLinkLeitorEmailTestes"
                  :href="urlLeitorEmailTestes"
                  class="btn btn-outline-secondary"
                  data-testid="link-leitor-email-testes"
                rel="noopener noreferrer"
                target="_blank"
            >
              <i aria-hidden="true" class="bi bi-mailbox2 me-1"></i>
              Leitor de e-mail de testes
            </a>
            <BButton
                :disabled="carregando"
                data-testid="btn-notificacoes-atualizar"
                variant="outline-primary"
                @click="carregar"
            >
              <i aria-hidden="true" class="bi bi-arrow-clockwise"></i>
              {{ TEXTOS.administracao.NOTIFICACOES_ATUALIZAR }}
            </BButton>
          </div>
        </template>
      </PageHeader>

      <BAlert v-if="erro" :model-value="true" variant="danger">
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

        <NotificacaoTabela
            :items="itensOrdenados"
            @detalhes="abrirDetalhes"
            @preview="abrirPreview"
            @reenviar="confirmarReenvio"
        />
      </template>
    </template>

    <BModal
        v-model="mostrarPreview"
        :title="itemParaPreview?.assunto"
        data-testid="modal-preview-email"
        scrollable
        size="lg"
    >
      <div v-if="itemParaPreview" class="p-3">
        <div class="mb-3 border-bottom pb-2">
          <strong>{{ TEXTOS.administracao.NOTIFICACOES_PREVIEW_DESTINATARIO }}:</strong> {{
            itemParaPreview.destinatario
          }}<br>
          <strong>{{ TEXTOS.administracao.NOTIFICACOES_PREVIEW_CRIACAO }}:</strong>
          {{ formatarDataOuHifen(itemParaPreview.dataHoraCriacao) }}
        </div>
        <iframe
            :srcdoc="montarPreviewHtml(itemParaPreview.corpoHtml)"
            class="email-content-preview"
            data-testid="iframe-preview-email"
            sandbox=""
            title="Preview do e-mail"
        />
      </div>
      <template #footer>
        <div class="d-flex justify-content-end w-100">
          <BButton
              data-testid="btn-fechar-preview-email"
              variant="link"
              class="text-decoration-none text-secondary fw-medium btn-fechar-link"
              @click="mostrarPreview = false"
          >
            Fechar
          </BButton>
        </div>
      </template>
    </BModal>

    <ModalPadrao
        v-model="mostrarDetalhes"
        :mostrar-botao-acao="false"
        data-testid="modal-detalhes-notificacao"
        tamanho="lg"
        texto-cancelar="Fechar"
        titulo="Detalhes da notificação"
    >
      <div v-if="itemParaDetalhes" class="p-3">
        <dl class="row mb-0 detalhes-notificacao">
          <dt class="col-sm-4">Destinatário</dt>
          <dd class="col-sm-8">{{ itemParaDetalhes.destinatario }}</dd>

          <dt class="col-sm-4">Tipo</dt>
          <dd class="col-sm-8">{{ formatarTipoNotificacao(itemParaDetalhes.tipoNotificacao) }}</dd>

          <dt class="col-sm-4">Situação</dt>
          <dd class="col-sm-8">{{ obterStatusNotificacao(itemParaDetalhes.situacao).label }}</dd>

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
        :titulo="TEXTOS.administracao.NOTIFICACOES_MODAL_REENVIAR_TITULO"
        test-id-confirmar="btn-notificacoes-reenviar-confirmar"
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
import {BAlert, BButton, BModal} from "bootstrap-vue-next";
import LayoutPadrao from "@/components/layout/LayoutPadrao.vue";
import PageHeader from "@/components/layout/PageHeader.vue";
import CarregamentoPagina from "@/components/comum/CarregamentoPagina.vue";
import ModalConfirmacao from "@/components/comum/ModalConfirmacao.vue";
import ModalPadrao from "@/components/comum/ModalPadrao.vue";
import AppAlert from "@/components/comum/AppAlert.vue";
import NotificacaoTabela from "@/components/administracao/NotificacaoTabela.vue";
import {TEXTOS} from "@/constants/textos";
import {TIPOS_NOTIFICACAO_LABELS} from "@/constants/notificacoes";
import {
  buscarUrlLeitorEmailTestes,
  compararNotificacoes,
  listarNotificacoesAdmin,
  type Notificacao,
  obterStatusNotificacao,
  reenviarNotificacao,
} from "@/services/notificacaoService";
import {formatarDataHoraBR} from "@/utils";
import {ehModoProducao} from "@/utils/ambiente";
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
const urlLeitorEmailTestes = ref<string>();

const itensOrdenados = computed(() => [...itens.value].sort(compararNotificacoes));
const mostrarLinkLeitorEmailTestes = computed(() => !ehModoProducao() && Boolean(urlLeitorEmailTestes.value));

function formatarDataOuHifen(valor?: string | null): string {
  if (!valor) return "-";
  const formatada = formatarDataHoraBR(valor);
  return formatada === "Não informado" || formatada === "Data inválida" ? "-" : formatada;
}

function formatarTipoNotificacao(tipo?: string): string {
  if (!tipo) return "-";
  return TIPOS_NOTIFICACAO_LABELS[tipo] || tipo;
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
      .replaceAll(/<script\b[^<]*(?:(?!<\/script>)<[^<]*)*<\/script>/gi, "")
      .replaceAll(/\son\w+=(["']).*?\1/gi, "")
      .replaceAll(/\son\w+=([^\s>]+)/gi, "");
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

async function carregarUrlLeitorEmailTestes() {
  try {
    urlLeitorEmailTestes.value = (await buscarUrlLeitorEmailTestes()) ?? undefined;
  } catch {
    urlLeitorEmailTestes.value = undefined;
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
    notify(TEXTOS.administracao.NOTIFICACOES_SUCESSO_REENVIO, "success");
    mostrarModalReenvio.value = false;
    itemSelecionado.value = null;
    await carregar();
  } catch (error) {
    notify(normalizarErro(error).mensagem || TEXTOS.administracao.NOTIFICACOES_ERRO_REENVIO, "danger");
  } finally {
    reenviando.value = false;
  }
}

onMounted(() => {
  void carregar();
  void carregarUrlLeitorEmailTestes();
});
</script>

<style scoped>
:deep(.modal-dialog) {
  max-width: min(1100px, calc(100vw - 2rem));
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

