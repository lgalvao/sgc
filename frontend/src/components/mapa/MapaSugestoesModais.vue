<script setup lang="ts">
import {computed, ref} from "vue";
import {BFormGroup, BFormInvalidFeedback, BFormTextarea} from "bootstrap-vue-next";
import ModalPadrao from "@/components/comum/ModalPadrao.vue";
import ModalConfirmacao from "@/components/comum/ModalConfirmacao.vue";
import {TEXTOS} from "@/constants/textos";

interface Props {
  mostrarModalVerSugestoes: boolean;
  podeApresentarSugestoes: boolean;
  sugestoesVisualizacao: string;
  mostrarModalSugestoes: boolean;
  loadingSugestoesEnvio: boolean;
  mensagemErroSugestoes: string;
  sugestoes: string;
}

const props = defineProps<Props>();

const emit = defineEmits<{
  (e: "update:mostrarModalVerSugestoes", valor: boolean): void;
  (e: "fechar-ver-sugestoes"): void;
  (e: "update:sugestoesVisualizacao", valor: string): void;
  (e: "update:mostrarModalSugestoes", valor: boolean): void;
  (e: "confirmar-sugestoes"): void;
  (e: "update:sugestoes", valor: string): void;
}>();

const modalVerSugestoes = computed({
  get: () => props.mostrarModalVerSugestoes,
  set: (valor: boolean) => emit("update:mostrarModalVerSugestoes", valor),
});

const modalSugestoes = computed({
  get: () => props.mostrarModalSugestoes,
  set: (valor: boolean) => emit("update:mostrarModalSugestoes", valor),
});

const sugestoesVisualizacaoModel = computed({
  get: () => props.sugestoesVisualizacao,
  set: (valor: string) => emit("update:sugestoesVisualizacao", valor),
});

const sugestoesModel = computed({
  get: () => props.sugestoes,
  set: (valor: string) => emit("update:sugestoes", valor),
});

const sugestoesTextareaRef = ref<InstanceType<typeof BFormTextarea> | null>(null);

function focarSugestoes() {
  sugestoesTextareaRef.value?.$el?.focus();
}
</script>

<template>
  <ModalPadrao
      v-model="modalVerSugestoes"
      :mostrar-botao-acao="false"
      test-codigo-cancelar="btn-ver-sugestoes-mapa-fechar"
      texto-cancelar="Fechar"
      titulo="Sugestões sobre o mapa"
      @fechar="$emit('fechar-ver-sugestoes')"
  >
    <BFormGroup class="mb-3">
      <template #label>
        Sugestões registradas para o mapa de competências:
      </template>

      <div
          v-if="!podeApresentarSugestoes"
          class="border rounded p-3 bg-body-tertiary white-space-pre-line"
          data-testid="txt-ver-sugestoes-mapa-texto"
      >
        {{ sugestoesVisualizacao }}
      </div>

      <BFormTextarea
          v-else
          id="sugestoesVisualizacao"
          v-model="sugestoesVisualizacaoModel"
          data-testid="txt-ver-sugestoes-mapa"
          readonly
          rows="5"
      />
    </BFormGroup>
  </ModalPadrao>

  <ModalConfirmacao
      v-model="modalSugestoes"
      :auto-close="false"
      :loading="loadingSugestoesEnvio"
      :ok-title="TEXTOS.comum.BOTAO_APRESENTAR"
      test-codigo-cancelar="btn-sugestoes-mapa-cancelar"
      test-codigo-confirmar="btn-sugestoes-mapa-confirmar"
      titulo="Apresentar sugestões"
      variant="success"
      @confirmar="$emit('confirmar-sugestoes')"
      @shown="focarSugestoes"
  >
    <BFormGroup
        label-for="sugestoesTextarea"
        :state="mensagemErroSugestoes ? false : null"
        class="mb-3"
    >
      <template #label>
        Sugestões para o mapa de competências: <span aria-hidden="true" class="text-danger">*</span>
      </template>
      <BFormTextarea
          id="sugestoesTextarea"
          ref="sugestoesTextareaRef"
          v-model="sugestoesModel"
          aria-required="true"
          :state="mensagemErroSugestoes ? false : null"
          data-testid="inp-sugestoes-mapa-texto"
          rows="5"
      />
      <BFormInvalidFeedback :state="mensagemErroSugestoes ? false : null">
        {{ mensagemErroSugestoes }}
      </BFormInvalidFeedback>
    </BFormGroup>
  </ModalConfirmacao>
</template>
