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

      <Alerta
          v-if="erroTela"
          data-testid="alert-notificacoes-admin-erro"
          :mensagem="erroTela"
          @dismissed="erroDispensado = true"
      />

      <template v-else>
        <Alerta
            v-if="notificacao"
            :chave="notificacao.chave"
            :dispensavel="notificacao.dispensavel"
            :mensagem="notificacao.mensagem"
            :notificacao="notificacao.notificacao"
            :stack-trace="notificacao.stackTrace"
            :variante="notificacao.variante"
            @dismissed="clear"
        />

        <NotificacaoTabela
            :items="notificacoesQuery.itensVisiveis()"
            @detalhes="abrirDetalhes"
            @preview="abrirPreview"
            @reenviar="confirmarReenvio"
        />
      </template>
    </template>

    <NotificacoesAdminFluxoModais
        :formatar-data-ou-hifen="formatarDataOuHifen"
        :formatar-texto-ou-hifen="formatarTextoOuHifen"
        :formatar-tipo-notificacao="formatarTipoNotificacao"
        :item-para-detalhes="itemParaDetalhes"
        :item-para-preview="itemParaPreview"
        :item-selecionado="itemSelecionado"
        :montar-preview-html="montarPreviewHtml"
        :mostrar-detalhes="mostrarDetalhes"
        :mostrar-modal-reenvio="mostrarModalReenvio"
        :mostrar-preview="mostrarPreview"
        :obter-status-notificacao="(situacao) => obterStatusNotificacao(situacao ?? '')"
        :reenviando="reenviando"
        @confirmar-reenvio="reenviar"
        @update:mostrar-detalhes="mostrarDetalhes = $event"
        @update:mostrar-modal-reenvio="mostrarModalReenvio = $event"
        @update:mostrar-preview="mostrarPreview = $event"
    />
  </LayoutPadrao>
</template>

<script lang="ts" setup>
import {computed, onActivated, onMounted, ref, watch} from "vue";
import {BButton} from "bootstrap-vue-next";
import NotificacoesAdminFluxoModais from "@/components/administracao/NotificacoesAdminFluxoModais.vue";
import LayoutPadrao from "@/components/layout/LayoutPadrao.vue";
import PageHeader from "@/components/layout/PageHeader.vue";
import CarregamentoPagina from "@/components/comum/CarregamentoPagina.vue";
import Alerta from "@/components/comum/Alerta.vue";
import NotificacaoTabela from "@/components/administracao/NotificacaoTabela.vue";
import {
  useNotificacoesAdminQuery,
  useReenvioNotificacaoMutation,
  useUrlLeitorEmailTestesQuery
} from "@/composables/useNotificacoesAdminQuery";
import {TEXTOS} from "@/constants/textos";
import {TIPOS_NOTIFICACAO_LABELS} from "@/constants/notificacoes";
import {type Notificacao, obterStatusNotificacao,} from "@/services/notificacaoService";
import {formatarDataHoraBR} from "@/utils";
import {ehModoProducao} from "@/utils/ambiente";
import {normalizarErro} from "@/utils/apiError";
import {useToast} from "@/composables/useToast";
import {useNotification} from "@/composables/useNotification";
import {useAsyncAction} from "@/composables/useAsyncAction";

const {notificacao, notify, clear} = useNotification();
const {exibirSucesso} = useToast();
const notificacoesQuery = useNotificacoesAdminQuery();
const leitorEmailTestesQuery = useUrlLeitorEmailTestesQuery();
const reenvioMutation = useReenvioNotificacaoMutation();
const acaoCarregar = useAsyncAction();
const acaoReenviar = useAsyncAction();

const itemSelecionado = ref<Notificacao | null>(null);
const itemParaPreview = ref<Notificacao | null>(null);
const itemParaDetalhes = ref<Notificacao | null>(null);
const mostrarModalReenvio = ref(false);
const mostrarPreview = ref(false);
const mostrarDetalhes = ref(false);
const erroDispensado = ref(false);

const carregando = computed(() =>
    notificacoesQuery.isPending.value || notificacoesQuery.isLoading.value || acaoCarregar.carregando.value
);
const erro = computed(() => notificacoesQuery.error.value?.message ?? null);
const erroTela = computed(() => erroDispensado.value ? null : erro.value);
const reenviando = computed(() => reenvioMutation.isLoading.value || acaoReenviar.carregando.value);
const urlLeitorEmailTestes = computed(() => leitorEmailTestesQuery.data.value ?? undefined);
const mostrarLinkLeitorEmailTestes = computed(() => !ehModoProducao() && Boolean(urlLeitorEmailTestes.value));
let montadoUmaVez = false;

watch(erro, (novoErro) => {
  if (novoErro) {
    erroDispensado.value = false;
  }
});

function obterMensagemErro(error: unknown, mensagemPadrao: string) {
  return normalizarErro(error).mensagem || mensagemPadrao;
}

function formatarTextoOuHifen(valor?: string | null): string {
  return valor?.trim() || "-";
}

function formatarDataOuHifen(valor?: string | null): string {
  if (!valor) return "-";
  const formatada = formatarDataHoraBR(valor);
  return formatada === "Não informado" || formatada === "Data inválida" ? "-" : formatada;
}

function formatarTipoNotificacao(tipo?: string): string {
  if (!tipo) return "-";
  return TIPOS_NOTIFICACAO_LABELS[tipo] ?? tipo;
}

function fecharReenvio() {
  mostrarModalReenvio.value = false;
  itemSelecionado.value = null;
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
      .container {
        width: 100% !important;
        max-width: none !important;
        margin: 0 !important;
        box-sizing: border-box;
      }
      .content,
      .footer {
        box-sizing: border-box;
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
  await acaoCarregar.executar(
      () => notificacoesQuery.refetch(),
      TEXTOS.administracao.NOTIFICACOES_ERRO_CARREGAR,
      {
        relancarErro: false,
        aoOcorrerErro: (erro, causa) => {
          notify(obterMensagemErro(causa, erro.mensagem), "danger");
        },
      },
  );
}

async function atualizarAoEntrarNaTela() {
  try {
    await notificacoesQuery.refetch();
  } catch {
    // A tela já expõe o erro da query; aqui evitamos rejeição não tratada ao entrar na rota.
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
  await acaoReenviar.executar(
      () => reenvioMutation.mutateAsync(itemSelecionado.value!.codigo),
      TEXTOS.administracao.NOTIFICACOES_ERRO_REENVIO,
      {
        relancarErro: false,
        aoOcorrerErro: (erro, causa) => {
          notify(obterMensagemErro(causa, erro.mensagem), "danger");
        },
        aoSucesso: () => {
          exibirSucesso(TEXTOS.administracao.NOTIFICACOES_SUCESSO_REENVIO);
          fecharReenvio();
        },
      },
  );
}

onMounted(async () => {
  await atualizarAoEntrarNaTela();
  montadoUmaVez = true;
});

onActivated(async () => {
  if (!montadoUmaVez) return;
  await atualizarAoEntrarNaTela();
});
</script>

<style scoped>
:deep(.modal-dialog) {
  max-width: min(1100px, calc(100vw - 2rem));
}

.detalhes-notificacao dt {
  font-weight: 600;
}
</style>
