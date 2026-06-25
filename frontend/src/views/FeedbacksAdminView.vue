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

    <FeedbacksAdminFluxoModais
        :feedback-selecionado="feedbackSelecionado"
        :mostrar-detalhes="mostrarDetalhes"
        :mostrar-imagem-ampliada="mostrarImagemAmpliada"
        :url-imagem-ampliada="urlImagemAmpliada"
        @abrir-imagem-ampliada="abrirImagemAmpliada"
        @update:mostrar-detalhes="mostrarDetalhes = $event"
        @update:mostrar-imagem-ampliada="mostrarImagemAmpliada = $event"
    />
  </LayoutPadrao>
</template>

<script lang="ts" setup>
import {computed, ref} from "vue";
import {BAlert, BBadge, BButton, BTable} from "bootstrap-vue-next";
import FeedbacksAdminFluxoModais from "@/components/administracao/FeedbacksAdminFluxoModais.vue";
import LayoutPadrao from "@/components/layout/LayoutPadrao.vue";
import PageHeader from "@/components/layout/PageHeader.vue";
import CarregamentoPagina from "@/components/comum/CarregamentoPagina.vue";
import EmptyState from "@/components/comum/EmptyState.vue";
import {useFeedbacksAdminQuery} from "@/composables/useFeedbacksAdminQuery";
import {TEXTOS} from "@/constants/textos";
import type {FeedbackAdmin} from "@/services/feedbackAdminService";
import {formatarDataHoraBR} from "@/utils";
import {
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
</style>
