<template>
  <div class="d-flex align-items-center w-100">
    <template v-if="isEditing">
      <div class="me-2 flex-grow-1">
        <BFormInput
            ref="inputRef"
            v-model="editValue"
            :aria-label="ariaLabel"
            :data-testid="testCodigoInput"
            :size="size"
            :state="mensagemErroAtual ? false : null"
            @keydown.enter="save"
            @keydown.esc="cancel"
        />
        <BFormInvalidFeedback :state="mensagemErroAtual ? false : null">
          {{ mensagemErroAtual }}
        </BFormInvalidFeedback>
      </div>
      <BButton
          :aria-label="ariaLabelSave"
          :data-testid="testCodigoSalvar"
          :size="size"
          class="me-1"
          variant="outline-success"
          @click="save"
      >
        <i aria-hidden="true" class="bi bi-save"/>
      </BButton>
      <BButton
          :aria-label="ariaLabelCancel"
          :data-testid="testCodigoCancelar"
          :size="size"
          variant="outline-secondary"
          @click="cancel"
      >
        <i aria-hidden="true" class="bi bi-x"/>
      </BButton>
    </template>

    <template v-else>
      <div v-if="canEdit" class="d-flex align-items-center gap-1 fade-controls me-2">
        <BButton
            :aria-label="ariaLabelEdit"
            :data-testid="testCodigoEditar"
            :disabled="!editEnabled"
            :size="size"
            class="btn-compacto"
            variant="outline-secondary"
            @click="startEdit"
        >
          <i aria-hidden="true" class="bi bi-pencil"/>
        </BButton>
        <slot name="extra-actions"/>
      </div>

      <div class="text-break flex-grow-1">
        <slot/>
      </div>
    </template>
  </div>
</template>

<script lang="ts" setup>
import {computed, nextTick, ref, watch} from 'vue';
import {BButton, BFormInput, BFormInvalidFeedback} from 'bootstrap-vue-next';
import {useValidacaoFormulario} from '@/composables/useValidacaoFormulario';

interface Props {
  modelValue: string;
  canEdit?: boolean;
  editEnabled?: boolean;
  size?: 'sm' | 'lg';
  ariaLabel?: string;
  ariaLabelSave?: string;
  ariaLabelCancel?: string;
  ariaLabelEdit?: string;
  testCodigoInput?: string;
  testCodigoSalvar?: string;
  testCodigoCancelar?: string;
  testCodigoEditar?: string;
  mensagemErroObrigatoria?: string;
}

const props = withDefaults(defineProps<Props>(), {
  canEdit: true,
  editEnabled: true,
  size: 'sm',
  ariaLabel: 'Editar',
  ariaLabelSave: 'Salvar',
  ariaLabelCancel: 'Cancelar',
  ariaLabelEdit: 'Editar',
  testCodigoInput: undefined,
  testCodigoSalvar: undefined,
  testCodigoCancelar: undefined,
  testCodigoEditar: undefined,
  mensagemErroObrigatoria: '',
});

const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void;
  (e: 'save', value: string): void;
  (e: 'cancel'): void;
  (e: 'edit-start'): void;
  (e: 'edit-end'): void;
}>();

const isEditing = ref(false);
const editValue = ref('');
const inputRef = ref<InstanceType<typeof BFormInput> | null>(null);
const {
  validarSubmissao,
  resetarValidacao,
  deveExibirErro
} = useValidacaoFormulario();

const mensagemErroAtual = computed(() => {
  if (!props.mensagemErroObrigatoria) {
    return '';
  }

  return deveExibirErro(!editValue.value.trim()) ? props.mensagemErroObrigatoria : '';
});

function startEdit() {
  if (!props.editEnabled) {
    return;
  }

  resetarValidacao();
  editValue.value = props.modelValue;
  isEditing.value = true;
  emit('edit-start');

  nextTick(() => {
    if (inputRef.value?.focus) {
      inputRef.value.focus();
    } else if (inputRef.value?.$el?.focus) {
      inputRef.value.$el.focus();
    }
  });
}

function save() {
  const trimmed = editValue.value.trim();
  const exigePreenchimento = !!props.mensagemErroObrigatoria;

  if (exigePreenchimento && !validarSubmissao(!!trimmed)) {
    return;
  }

  if (trimmed && trimmed !== props.modelValue) {
    emit('update:modelValue', trimmed);
    emit('save', trimmed);
  }
  resetarValidacao();
  isEditing.value = false;
  emit('edit-end');
}

function cancel() {
  resetarValidacao();
  isEditing.value = false;
  emit('cancel');
  emit('edit-end');
}

watch(editValue, (valorAtual, valorAnterior) => {
  if (valorAtual !== valorAnterior && valorAtual.trim()) {
    resetarValidacao();
  }
});
</script>

<style scoped>
.fade-controls {
  opacity: 0.7;
  transition: opacity 0.2s;
}

.d-flex:hover > .fade-controls,
.d-flex:focus-within > .fade-controls {
  opacity: 1;
}

.btn-compacto {
  padding: 0.2rem 0.35rem;
  line-height: 1;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.btn-compacto i {
  font-size: 0.875rem;
}
</style>
