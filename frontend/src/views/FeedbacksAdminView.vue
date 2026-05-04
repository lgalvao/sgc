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
        <BBadge :variant="obterVarianteTipo(item.tipo)">
          <i :class="['bi', iconesTipo[item.tipo], 'me-1']"></i>
          {{ formatarTipo(item.tipo) }}
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

      <template #cell(acoes)="{ item }">
        <BButton
            :aria-label="`Ver detalhes do feedback ${item.codigo}`"
            :data-testid="`btn-feedback-detalhes-${item.codigo}`"
            size="sm"
            variant="outline-secondary"
            @click="abrirDetalhes(item)"
        >
          <i class="bi bi-eye"></i>
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
          <dd class="col-sm-9">
            <BBadge :variant="obterVarianteTipo(feedbackSelecionado.tipo)">
              <i :class="['bi', iconesTipo[feedbackSelecionado.tipo], 'me-1']"></i>
              {{ formatarTipo(feedbackSelecionado.tipo) }}
            </BBadge>
          </dd>

          <dt class="col-sm-3">{{ TEXTOS.administracao.FEEDBACKS_CAMPOS.USUARIO }}</dt>
          <dd class="col-sm-9">{{ feedbackSelecionado.usuarioNome }} ({{ feedbackSelecionado.usuarioCodigo }})</dd>

          <dt class="col-sm-3">{{ TEXTOS.administracao.FEEDBACKS_CAMPOS.ROTA }}</dt>
          <dd class="col-sm-9 text-break"><code>{{ feedbackSelecionado.rota }}</code></dd>

          <dt class="col-sm-3">{{ TEXTOS.administracao.FEEDBACKS_CAMPOS.ENVIADO_EM }}</dt>
          <dd class="col-sm-9">{{ formatarDataHoraBR(feedbackSelecionado.enviadoEm) }}</dd>

          <dt class="col-sm-3">{{ TEXTOS.administracao.FEEDBACKS_CAMPOS.CAPTURA }}</dt>
          <dd class="col-sm-9">
            <div v-if="feedbackSelecionado.screenshotDisponivel">
              <button class="btn p-0 border-0 d-inline-block" @click="abrirImagemAmpliada(feedbackSelecionado.codigo)">
                <img
                    :src="obterUrlScreenshot(feedbackSelecionado.codigo)"
                    alt="Captura de tela"
                    class="img-fluid border rounded feedback-thumbnail shadow-sm"
                />
              </button>
              <div class="small text-muted mt-1">Clique para ampliar</div>
            </div>
            <span v-else class="text-muted">Não disponível no servidor</span>
          </dd>

          <dt class="col-sm-3 mt-3">{{ TEXTOS.administracao.FEEDBACKS_CAMPOS.NOTA }}</dt>
          <dd class="col-sm-9 mt-3 text-break shadow-none bg-light p-3 border rounded" v-html="feedbackSelecionado.nota"></dd>

          <dt v-if="feedbackSelecionado.metadataJson" class="col-sm-12 mt-3">{{ TEXTOS.administracao.FEEDBACKS_CAMPOS.METADADOS }}</dt>
          <dd v-if="feedbackSelecionado.metadataJson" class="col-sm-12">
            <div class="table-responsive border rounded mt-2">
              <table class="table table-sm table-striped mb-0">
                <thead class="table-light">
                  <tr>
                    <th class="ps-2">Chave</th>
                    <th>Valor</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="(valor, chave) in formatarMetadados(feedbackSelecionado.metadataJson)" :key="chave">
                    <td class="small fw-semibold ps-2" style="width: 30%">{{ chave }}</td>
                    <td class="small text-break">{{ valor }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </dd>
        </dl>
      </div>
    </BModal>

    <!-- Modal de Imagem Ampliada -->
    <BModal
        v-model="mostrarImagemAmpliada"
        :title="TEXTOS.administracao.FEEDBACKS_CAMPOS.CAPTURA"
        centered
        hide-footer
        size="xl"
    >
      <div class="text-center">
        <img :src="urlImagemAmpliada" alt="Captura ampliada" class="img-fluid rounded shadow"/>
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
import {type FeedbackAdmin, listarFeedbacksAdmin, obterUrlScreenshot} from "@/services/feedbackAdminService";
import {formatarDataHoraBR} from "@/utils";
import {normalizarErro} from "@/utils/apiError";

const feedbacks = ref<FeedbackAdmin[]>([]);
const carregando = ref(true);
const erro = ref<string | null>(null);
const mostrarDetalhes = ref(false);
const feedbackSelecionado = ref<FeedbackAdmin | null>(null);
const mostrarImagemAmpliada = ref(false);
const urlImagemAmpliada = ref("");

const campos = [
  {key: "tipo", label: TEXTOS.administracao.FEEDBACKS_CAMPOS.TIPO},
  {key: "usuarioNome", label: TEXTOS.administracao.FEEDBACKS_CAMPOS.USUARIO},
  {key: "nota", label: TEXTOS.administracao.FEEDBACKS_CAMPOS.NOTA},
  {key: "rota", label: TEXTOS.administracao.FEEDBACKS_CAMPOS.ROTA},
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

const iconesTipo: Record<FeedbackAdmin["tipo"], string> = {
  BUG: "bi-bug",
  SUGESTAO: "bi-lightbulb",
  QUESTAO: "bi-question-circle",
  ELOGIO: "bi-emoji-smile",
};

function obterVarianteTipo(tipo: FeedbackAdmin["tipo"]): string {
  const variantes: Record<FeedbackAdmin["tipo"], string> = {
    BUG: "danger",
    SUGESTAO: "primary",
    QUESTAO: "info",
    ELOGIO: "success",
  };
  return variantes[tipo] ?? "secondary";
}

function resumirNota(nota: string): string {
  if (!nota) return "";
  const doc = new DOMParser().parseFromString(nota, "text/html");
  const textoLimpo = (doc.body.textContent || "").replace(/\s+/g, " ").trim();

  if (textoLimpo.length <= 120) {
    return textoLimpo;
  }
  return `${textoLimpo.slice(0, 117)}...`;
}

function abrirDetalhes(item: FeedbackAdmin) {
  feedbackSelecionado.value = item;
  mostrarDetalhes.value = true;
}

function abrirImagemAmpliada(codigo: string) {
  urlImagemAmpliada.value = obterUrlScreenshot(codigo);
  mostrarImagemAmpliada.value = true;
}

function formatarMetadados(json?: string | null): Record<string, any> {
  if (!json) return {};
  try {
    return JSON.parse(json);
  } catch (e) {
    return {erro: "JSON inválido", valor: json};
  }
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
  max-width: 20rem;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.feedback-thumbnail {
  max-height: 200px;
  cursor: zoom-in;
  transition: transform 0.2s;
}

.feedback-thumbnail:hover {
  transform: scale(1.02);
}
</style>
