<template>
  <AppAlert
      :data-testid="dataTestid"
      :dispensavel="dispensavel"
      :mensagem="mensagem"
      :variante="variante"
      @dismissed="emit('dismissed')"
  />
</template>

<script lang="ts" setup>
import AppAlert from "@/components/comum/AppAlert.vue";

withDefaults(defineProps<{
  mensagem?: string | null;
  variante?: "danger" | "warning" | "success" | "info";
  dispensavel?: boolean;
  dataTestid?: string;
}>(), {
  mensagem: undefined,
  variante: "danger",
  dispensavel: true,
  dataTestid: "app-alerta-formulario",
});

const emit = defineEmits<{
  dismissed: [];
}>();
</script>
