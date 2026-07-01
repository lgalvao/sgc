<template>
  <ModalPadrao
      :model-value="mostrarDetalhes"
      :mostrar-botao-acao="false"
      :texto-cancelar="TEXTOS.comum.BOTAO_FECHAR"
      :titulo="TEXTOS.administracao.FEEDBACKS_MODAL_TITULO"
      data-testid="modal-detalhes-feedback"
      tamanho="lg"
      @fechar="$emit('update:mostrarDetalhes', false)"
      @update:model-value="$emit('update:mostrarDetalhes', $event)"
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
                @click="$emit('abrirImagemAmpliada', feedbackSelecionado.codigo)"
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
        <!-- eslint-disable vue/no-v-html -->
        <dd
            class="col-sm-9 mt-3 text-break shadow-none bg-light p-3 border rounded"
            v-html="feedbackSelecionado.nota"
        ></dd>
        <!-- eslint-enable vue/no-v-html -->

        <dt v-if="feedbackSelecionado.metadataJson" class="col-sm-12 mt-3">
          {{ TEXTOS.administracao.FEEDBACKS_CAMPOS.METADADOS }}
        </dt>
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
  </ModalPadrao>

  <ModalPadrao
      :model-value="mostrarImagemAmpliada"
      :mostrar-botao-acao="false"
      :texto-cancelar="TEXTOS.comum.BOTAO_FECHAR"
      :titulo="TEXTOS.administracao.FEEDBACKS_CAMPOS.CAPTURA"
      centralizado
      data-testid="modal-imagem-ampliada"
      tamanho="xl"
      @fechar="$emit('update:mostrarImagemAmpliada', false)"
      @update:model-value="$emit('update:mostrarImagemAmpliada', $event)"
  >
    <div class="text-center">
      <img :src="urlImagemAmpliada" alt="Captura ampliada" class="img-fluid rounded shadow"/>
    </div>
  </ModalPadrao>
</template>

<script lang="ts" setup>
import {BBadge} from "bootstrap-vue-next";
import ModalPadrao from "@/components/comum/ModalPadrao.vue";
import {TEXTOS} from "@/constants/textos";
import type {FeedbackAdmin} from "@/services/feedbackAdminService";
import {formatarDataHoraBR} from "@/utils";
import {
  formatarMetadados,
  formatarTipo,
  obterIconeTipo,
  obterUrlScreenshot,
  obterVarianteTipo
} from "@/views/feedbacksAdminApresentacao";

defineProps<{
  feedbackSelecionado: FeedbackAdmin | null;
  mostrarDetalhes: boolean;
  mostrarImagemAmpliada: boolean;
  urlImagemAmpliada: string;
}>();

defineEmits<{
  (e: 'abrirImagemAmpliada', codigo: string): void;
  (e: 'update:mostrarDetalhes', valor: boolean): void;
  (e: 'update:mostrarImagemAmpliada', valor: boolean): void;
}>();
</script>

<style scoped>
.feedback-thumbnail {
  max-height: 200px;
  cursor: zoom-in;
  transition: transform 0.2s;
}

.feedback-thumbnail:hover {
  transform: scale(1.02);
}
</style>
