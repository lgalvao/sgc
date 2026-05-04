<template>
  <LayoutPadrao>
    <PageHeader :title="TEXTOS.administracao.FEEDBACKS_TITULO">
      <template #actions>
        <BButton
            :disabled="carregando"
            data-testid="btn-feedbacks-atualizar"
            variant="outline-primary"
            @click="carregar"
        >
          <i aria-hidden="true" class="bi bi-arrow-clockwise"></i>
          {{ TEXTOS.administracao.FEEDBACKS_ATUALIZAR }}
        </BButton>
      </template>
    </PageHeader>

    <div v-if="carregando" class="text-center py-5" data-testid="feedbacks-carregando">
      <BSpinner :label="TEXTOS.comum.CARREGANDO_DADOS" variant="primary"/>
      <p class="mt-2 text-muted">{{ TEXTOS.comum.CARREGANDO_DADOS }}</p>
    </div>

    <BAlert v-else-if="erro" :model-value="true" variant="danger">
      {{ erro }}
    </BAlert>

    <EmptyState
        v-else-if="feedbacks.length === 0"
        :description="TEXTOS.administracao.FEEDBACKS_SEM_REGISTROS"
        :title="TEXTOS.administracao.FEEDBACKS_TITULO"
        icon="bi-chat-left-text"
    />

    <BTable
        v-else
        :fields="campos"
        :items="feedbacks"
        hover
        responsive
        small
        striped
    >
      <template #cell(tipo)="{ item }">
        <span class="fw-semibold">{{ formatarTipo(item.tipo) }}</span>
      </template>

      <template #cell(status)="{ item }">
        <BBadge :variant="obterVarianteStatus(item.status)">
          {{ item.status }}
        </BBadge>
      </template>

      <template #cell(usuarioNome)="{ item }">
        <div class="fw-semibold">{{ item.usuarioNome }}</div>
        <div class="small text-body-secondary">{{ item.usuarioCodigo }}</div>
      </template>

      <template #cell(nota)="{ item }">
        <span class="feedback-resumo">{{ resumirNota(item.nota) }}</span>
      </template>

      <template #cell(enviadoEm)="{ item }">
        {{ formatarDataHoraBR(item.enviadoEm) }}
      </template>

      <template #cell(caminhoScreenshot)="{ item }">
        {{ item.caminhoScreenshot ? "Sim" : "Não" }}
      </template>

      <template #cell(acoes)="{ item }">
        <BButton
            :aria-label="`Ver detalhes do feedback ${item.codigo}`"
            :data-testid="`btn-feedback-detalhes-${item.codigo}`"
            size="sm"
            variant="outline-secondary"
            @click="abrirDetalhes(item)"
        >
          {{ TEXTOS.administracao.NOTIFICACOES_DETALHES }}
        </BButton>
      </template>
    </BTable>

    <BModal
        v-model="mostrarDetalhes"
        :title="TEXTOS.administracao.FEEDBACKS_MODAL_TITULO"
        data-testid="modal-detalhes-feedback"
        hide-footer
        scrollable
        size="lg"
    >
      <div v-if="feedbackSelecionado" class="p-2">
        <dl class="row mb-0">
          <dt class="col-sm-3">{{ TEXTOS.administracao.FEEDBACKS_CAMPOS.TIPO }}</dt>
          <dd class="col-sm-9">{{ formatarTipo(feedbackSelecionado.tipo) }}</dd>

          <dt class="col-sm-3">{{ TEXTOS.administracao.FEEDBACKS_CAMPOS.STATUS }}</dt>
          <dd class="col-sm-9">{{ feedbackSelecionado.status }}</dd>

          <dt class="col-sm-3">{{ TEXTOS.administracao.FEEDBACKS_CAMPOS.USUARIO }}</dt>
          <dd class="col-sm-9">{{ feedbackSelecionado.usuarioNome }} ({{ feedbackSelecionado.usuarioCodigo }})</dd>

          <dt class="col-sm-3">{{ TEXTOS.administracao.FEEDBACKS_CAMPOS.ROTA }}</dt>
          <dd class="col-sm-9 text-break">{{ feedbackSelecionado.rota }}</dd>

          <dt class="col-sm-3">{{ TEXTOS.administracao.FEEDBACKS_CAMPOS.ENVIADO_EM }}</dt>
          <dd class="col-sm-9">{{ formatarDataHoraBR(feedbackSelecionado.enviadoEm) }}</dd>

          <dt class="col-sm-3">{{ TEXTOS.administracao.FEEDBACKS_CAMPOS.CAPTURA }}</dt>
          <dd class="col-sm-9">{{ feedbackSelecionado.caminhoScreenshot ? "Enviada" : "Não enviada" }}</dd>

          <dt class="col-sm-3">{{ TEXTOS.administracao.FEEDBACKS_CAMPOS.NOTA }}</dt>
          <dd class="col-sm-9 text-break">{{ feedbackSelecionado.nota }}</dd>

          <dt v-if="feedbackSelecionado.metadataJson" class="col-sm-3">{{ TEXTOS.administracao.FEEDBACKS_CAMPOS.METADADOS }}</dt>
          <dd v-if="feedbackSelecionado.metadataJson" class="col-sm-9">
            <pre class="feedback-metadados">{{ feedbackSelecionado.metadataJson }}</pre>
          </dd>
        </dl>
      </div>
    </BModal>
  </LayoutPadrao>
</template>

<script lang="ts" setup>
import {onMounted, ref} from "vue";
import {BAlert, BBadge, BButton, BModal, BSpinner, BTable} from "bootstrap-vue-next";
import LayoutPadrao from "@/components/layout/LayoutPadrao.vue";
import PageHeader from "@/components/layout/PageHeader.vue";
import EmptyState from "@/components/comum/EmptyState.vue";
import {TEXTOS} from "@/constants/textos";
import {type FeedbackAdmin, listarFeedbacksAdmin} from "@/services/feedbackAdminService";
import {formatarDataHoraBR} from "@/utils";
import {normalizarErro} from "@/utils/apiError";

const feedbacks = ref<FeedbackAdmin[]>([]);
const carregando = ref(true);
const erro = ref<string | null>(null);
const mostrarDetalhes = ref(false);
const feedbackSelecionado = ref<FeedbackAdmin | null>(null);

const campos = [
  {key: "tipo", label: TEXTOS.administracao.FEEDBACKS_CAMPOS.TIPO},
  {key: "status", label: TEXTOS.administracao.FEEDBACKS_CAMPOS.STATUS},
  {key: "usuarioNome", label: TEXTOS.administracao.FEEDBACKS_CAMPOS.USUARIO},
  {key: "rota", label: TEXTOS.administracao.FEEDBACKS_CAMPOS.ROTA},
  {key: "nota", label: TEXTOS.administracao.FEEDBACKS_CAMPOS.NOTA},
  {key: "caminhoScreenshot", label: TEXTOS.administracao.FEEDBACKS_CAMPOS.CAPTURA},
  {key: "enviadoEm", label: TEXTOS.administracao.FEEDBACKS_CAMPOS.ENVIADO_EM},
  {key: "acoes", label: TEXTOS.administracao.FEEDBACKS_CAMPOS.ACOES},
];

function formatarTipo(tipo: FeedbackAdmin["tipo"]): string {
  const tipos: Record<FeedbackAdmin["tipo"], string> = {
    BUG: "Bug",
    SUGESTAO: "Sugestão",
    QUESTAO: "Questão",
    ELOGIO: "Elogio",
  };
  return tipos[tipo] ?? tipo;
}

function obterVarianteStatus(status: FeedbackAdmin["status"]): string {
  const variantes: Record<FeedbackAdmin["status"], string> = {
    NOVO: "warning",
    REVISADO: "info",
    RESOLVIDO: "success",
    DESCARTADO: "secondary",
  };
  return variantes[status] ?? "secondary";
}

function resumirNota(nota: string): string {
  if (nota.length <= 120) {
    return nota;
  }
  return `${nota.slice(0, 117)}...`;
}

function abrirDetalhes(item: FeedbackAdmin) {
  feedbackSelecionado.value = item;
  mostrarDetalhes.value = true;
}

async function carregar() {
  carregando.value = true;
  erro.value = null;
  try {
    feedbacks.value = await listarFeedbacksAdmin();
  } catch (error) {
    erro.value = normalizarErro(error).mensagem || TEXTOS.administracao.FEEDBACKS_ERRO_CARREGAR;
  } finally {
    carregando.value = false;
  }
}

onMounted(carregar);
</script>

<style scoped>
.feedback-resumo {
  display: inline-block;
  max-width: 30rem;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.feedback-metadados {
  margin: 0;
  padding: 0.75rem;
  border-radius: 0.5rem;
  background: var(--bs-tertiary-bg);
  white-space: pre-wrap;
  word-break: break-word;
}
</style>
