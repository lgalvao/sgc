<template>
  <div class="text-center py-5" data-testid="pagina-carregando">
    <BSpinner :label="mensagemExibicao" variant="primary"/>
  </div>
</template>

<script lang="ts" setup>
import {computed} from "vue";
import {BSpinner} from "bootstrap-vue-next";
import {TEXTOS} from "@/constants/textos";

const props = defineProps<{
  mensagem?: string;
}>();

const mensagemExibicao = computed(() => props.mensagem || TEXTOS.comum.CARREGANDO_DADOS);
</script>
