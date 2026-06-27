<template>
  <AppAlert
      v-if="feedback"
      :data-testid="dataTestid"
      :mensagem="feedback.mensagem"
      :variante="feedback.variante"
      @dismissed="emit('dismissed')"
  />
</template>

<script lang="ts" setup>
import AppAlert from "@/components/comum/AppAlert.vue";

export interface FeedbackAcao {
  mensagem: string;
  variante: "danger" | "success";
}

withDefaults(defineProps<{
  feedback?: FeedbackAcao | null;
  dataTestid?: string;
}>(), {
  feedback: null,
  dataTestid: "app-alerta-acao",
});

const emit = defineEmits<{
  dismissed: [];
}>();
</script>
