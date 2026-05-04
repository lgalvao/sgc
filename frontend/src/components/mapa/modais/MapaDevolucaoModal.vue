<script lang="ts" setup>
import {computed, ref} from "vue";
import {BFormGroup, BFormInvalidFeedback, BFormTextarea} from "bootstrap-vue-next";
import ModalConfirmacao from "@/components/comum/ModalConfirmacao.vue";
import {TEXTOS} from "@/constants/textos";

const props = defineProps<{
  modelValue: boolean;
  loading: boolean;
  observacao: string;
  erro?: string;
}>();

const emit = defineEmits<{
  (e: "update:modelValue", valor: boolean): void;
  (e: "update:observacao", valor: string): void;
  (e: "confirmar"): void;
}>();

const mostrar = computed({
  get: () => props.modelValue,
  set: (valor: boolean) => emit("update:modelValue", valor),
});

const observacaoModel = computed({
  get: () => props.observacao,
  set: (valor: string) => emit("update:observacao", valor),
});

const inputRef = ref<InstanceType<typeof BFormTextarea> | null>(null);

function focar() {
  inputRef.value?.$el?.focus();
}
</script>

<template>
  <ModalConfirmacao
      v-model="mostrar"
      :auto-close="false"
      :loading="loading"
      :ok-title="TEXTOS.mapa.BOTAO_DEVOLVER"
      test-codigo-cancelar="btn-devolucao-mapa-cancelar"
      test-codigo-confirmar="btn-devolucao-mapa-confirmar"
      titulo="Devolver mapa"
      variant="danger"
      @confirmar="$emit('confirmar')"
      @shown="focar"
  >
    <p>Confirma a devolução da validação do mapa para ajustes?</p>
    <BFormGroup :state="erro ? false : null" class="mb-3" label-for="observacaoDevolucao">
      <template #label>Observação: <span aria-hidden="true" class="text-danger">*</span></template>
      <BFormTextarea
          id="observacaoDevolucao" ref="inputRef" v-model="observacaoModel" :state="erro ? false : null"
          data-testid="inp-devolucao-mapa-obs" placeholder="Digite observações sobre a devolução..." rows="3"
      />
      <BFormInvalidFeedback :state="erro ? false : null">{{ erro }}</BFormInvalidFeedback>
    </BFormGroup>
  </ModalConfirmacao>
</template>
