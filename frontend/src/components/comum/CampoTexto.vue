<template>
  <BFormGroup :label-for="id">
    <template v-if="label" #label>
      {{ label }} <span v-if="obrigatorio" aria-hidden="true" class="text-danger">*</span>
    </template>
    <BFormInput
        :id="id"
        :model-value="modelValue"
        :state="erro ? false : null"
        :disabled="disabled"
        :placeholder="placeholder"
        :required="obrigatorio"
        :data-testid="dataTestid"
        type="text"
        @update:model-value="emit('update:modelValue', String($event))"
    />
    <BFormInvalidFeedback :state="erro ? false : null">
      {{ erro }}
    </BFormInvalidFeedback>
  </BFormGroup>
</template>

<script lang="ts" setup>
import {BFormGroup, BFormInput, BFormInvalidFeedback} from "bootstrap-vue-next";

defineProps<{
  id: string;
  modelValue: string;
  label?: string;
  placeholder?: string;
  obrigatorio?: boolean;
  disabled?: boolean;
  erro?: string | null;
  dataTestid?: string;
}>();

const emit = defineEmits<{
  (e: "update:modelValue", valor: string): void;
}>();
</script>
