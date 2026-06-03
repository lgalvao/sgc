<template>
  <LayoutPadrao>
    <CarregamentoPagina v-if="carregando"/>

    <template v-else>
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

      <BAlert v-if="erro" :model-value="true" dismissible variant="danger">
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
          data-testid="tbl-feedbacks"
          hover
          responsive
          small
      >
        <template #cell(tipo)="{ item }">
          <BBadge :variant="obterVarianteTipo(item.tipo)">
            <i :class="['bi', obterIconeTipo(item.tipo), 'me-1']"></i>
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
              class="btn-acao-sutil"
              size="sm"
              variant="outline-secondary"
              @click="abrirDetalhes(item)"
          >
            <i class="bi bi-eye"></i>
          </BButton>
        </template>
      </BTable>
    </template>

    <BModal
        v-model="mostrarDetalhes"
        :title="TEXTOS.administracao.FEEDBACKS_MODAL_TITULO"
        :ok-title="TEXTOS.comum.BOTAO_FECHAR"
        ok-only
        ok-variant="secondary"
        data-testid="modal-detalhes-feedback"
        scrollable
        size="lg"
    >
      <div v-if="feedbackSelecionado" class="p-2">
        <dl class="row mb-0">
          <dt class="col-sm-3">{{ TEXTOS.administracao.FEEDBACKS_CAMPOS.TIPO }}</dt>
          <dd class="col-sm-9">
            <BBadge :variant="obterVarianteTipo(feedbackSelecionado.tipo)">
              <i :class="['bi', obterIconeTipo(feedbackSelecionado.tipo), 'me-1']"></i>
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
              <button
                  aria-label="Ampliar captura de tela"
                  class="btn p-0 border-0 d-inline-block"
                  data-testid="btn-feedback-ampliar-captura"
                  title="Clique para ampliar"
                  type="button"
                  @click="abrirImagemAmpliada(feedbackSelecionado.codigo)"
              >
                <img
                    :src="obterUrlScreenshot(feedbackSelecionado.codigo)"
                    alt="Captura de tela do feedback"
                    class="img-fluid border rounded feedback-thumbnail shadow-sm"
                    data-testid="img-feedback-captura"
                />
              </button>
            </div>
            <span v-else class="text-muted">Não disponível no servidor</span>
          </dd>

          <dt class="col-sm-3 mt-3">{{ TEXTOS.administracao.FEEDBACKS_CAMPOS.NOTA }}</dt>
          <!-- eslint-disable-next-line vue/no-v-html -->
          <dd class="col-sm-9 mt-3 text-break shadow-none bg-light p-3 border rounded" v-html="feedbackSelecionado.nota"></dd>

          <dt v-if="feedbackSelecionado.metadataJson" class="col-sm-12 mt-3">{{ TEXTOS.administracao.FEEDBACKS_CAMPOS.METADADOS }}</dt>
          <dd v-if="feedbackSelecionado.metadataJson" class="col-sm-12">
            <div class="table-responsive border rounded mt-2">
              <table class="table table-sm mb-0">
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
        :ok-title="TEXTOS.comum.BOTAO_FECHAR"
        ok-only
        ok-variant="secondary"
        centered
        size="xl"
        data-testid="modal-imagem-ampliada"
    >
      <div class="text-center">
        <img :src="urlImagemAmpliada" alt="Captura ampliada" class="img-fluid rounded shadow"/>
      </div>
    </BModal>
  </LayoutPadrao>
</template>

<script lang="ts" setup>
import {computed, ref} from "vue";
import {BAlert, BBadge, BButton, BModal, BTable} from "bootstrap-vue-next";
import LayoutPadrao from "@/components/layout/LayoutPadrao.vue";
import PageHeader from "@/components/layout/PageHeader.vue";
import CarregamentoPagina from "@/components/comum/CarregamentoPagina.vue";
import EmptyState from "@/components/comum/EmptyState.vue";
import {useFeedbacksAdminQuery} from "@/composables/useFeedbacksAdminQuery";
import {TEXTOS} from "@/constants/textos";
import type {FeedbackAdmin} from "@/services/feedbackAdminService";
import {formatarDataHoraBR} from "@/utils";
import {
  formatarMetadados,
  formatarTipo,
  obterIconeTipo,
  obterUrlScreenshot,
  obterVarianteTipo,
  resumirNota
} from "@/views/feedbacksAdminApresentacao";

const feedbacksQuery = useFeedbacksAdminQuery();
const mostrarDetalhes = ref(false);
const feedbackSelecionado = ref<FeedbackAdmin | null>(null);
const mostrarImagemAmpliada = ref(false);
const urlImagemAmpliada = ref("");

const feedbacks = computed(() => feedbacksQuery.data.value ?? []);
const carregando = computed(() => feedbacksQuery.isPending.value || feedbacksQuery.isLoading.value);
const erro = computed(() => feedbacksQuery.error.value?.message || null);

const campos = [
  {key: "tipo", label: TEXTOS.administracao.FEEDBACKS_CAMPOS.TIPO},
  {key: "usuarioNome", label: TEXTOS.administracao.FEEDBACKS_CAMPOS.USUARIO},
  {key: "nota", label: TEXTOS.administracao.FEEDBACKS_CAMPOS.NOTA},
  {key: "rota", label: TEXTOS.administracao.FEEDBACKS_CAMPOS.ROTA},
  {key: "enviadoEm", label: TEXTOS.administracao.FEEDBACKS_CAMPOS.ENVIADO_EM},
  {key: "acoes", label: ""},
];

function abrirDetalhes(item: FeedbackAdmin) {
  feedbackSelecionado.value = item;
  mostrarDetalhes.value = true;
}

function abrirImagemAmpliada(codigo: string) {
  urlImagemAmpliada.value = obterUrlScreenshot(codigo);
  mostrarImagemAmpliada.value = true;
}

async function carregar() {
  await feedbacksQuery.refetch();
}
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
