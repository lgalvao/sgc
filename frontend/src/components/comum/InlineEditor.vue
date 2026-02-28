<template>
  <div class="d-flex align-items-center w-100">
    <template v-if="isEditing">
      <BFormInput
          ref="inputRef"
          v-model="editValue"
          :aria-label="ariaLabel"
          :data-testid="testIdInput"
          :size="size"
          class="me-2 flex-grow-1"
          @keydown.enter="save"
          @keydown.esc="cancel"
          @vue:mounted="(el: any) => { if(el?.focus) el.focus(); else if(el?.$el?.focus) el.$el.focus(); }"
      />
      <BButton
          :aria-label="ariaLabelSave"
          :data-testid="testIdSave"
          :size="size"
          class="me-1"
          variant="outline-success"
          @click="save"
      >
        <i aria-hidden="true" class="bi bi-save"/>
      </BButton>
      <BButton
          :aria-label="ariaLabelCancel"
          :data-testid="testIdCancel"
          :size="size"
          variant="outline-secondary"
          @click="cancel"
      >
        <i aria-hidden="true" class="bi bi-x"/>
      </BButton>
    </template>

    <template v-else>
      <div class="flex-grow-1 text-break me-2">
        <slot/>
      </div>

      <div v-if="canEdit" class="d-flex align-items-center fade-controls">
        <BButton
            :aria-label="ariaLabelEdit"
            :data-testid="testIdEdit"
            :size="size"
            class="me-1"
            variant="outline-primary"
            @click="startEdit"
        >
          <i aria-hidden="true" class="bi bi-pencil"/>
        </BButton>
        <slot name="extra-actions"/>
      </div>
    </template>
  </div>
</template>

<script lang="ts" setup>
import {ref} from 'vue';
import {BButton, BFormInput} from 'bootstrap-vue-next';

interface Props {
  modelValue: string;
  canEdit?: boolean;
  size?: 'sm' | 'md' | 'lg';
  ariaLabel?: string;
  ariaLabelSave?: string;
  ariaLabelCancel?: string;
  ariaLabelEdit?: string;
  testIdInput?: string;
  testIdSave?: string;
  testIdCancel?: string;
  testIdEdit?: string;
}

const props = withDefaults(defineProps<Props>(), {
  canEdit: true,
  size: 'sm',
  ariaLabel: 'Editar',
  ariaLabelSave: 'Salvar',
  ariaLabelCancel: 'Cancelar',
  ariaLabelEdit: 'Editar',
  testIdInput: undefined,
  testIdSave: undefined,
  testIdCancel: undefined,
  testIdEdit: undefined,
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


function startEdit() {
  editValue.value = props.modelValue;
  isEditing.value = true;
  emit('edit-start');
}

function save() {
  const trimmed = editValue.value.trim();
  if (trimmed && trimmed !== props.modelValue) {
    emit('update:modelValue', trimmed);
    emit('save', trimmed);
  }
  isEditing.value = false;
  emit('edit-end');
}

function cancel() {
  isEditing.value = false;
  emit('cancel');
  emit('edit-end');
}
</script>

<style scoped>
.fade-controls {
  opacity: 1;
  transition: opacity 0.2s;
}

@media (hover: hover) {
  .fade-controls {
    opacity: 0;
  }

  .d-flex:hover > .fade-controls,
  .d-flex:focus-within > .fade-controls {
    opacity: 1;
  }
}
</style>
