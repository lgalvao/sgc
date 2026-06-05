<template>
  <LayoutPadrao>
    <CarregamentoPagina v-if="carregando"/>
    <DiagnosticoEquipePainel
        v-else
        :cod-subprocesso="codSubprocesso"
        :sigla-unidade="siglaUnidade"
    />
  </LayoutPadrao>
</template>

<script lang="ts" setup>
import {useMonitoramentoDiagnostico} from '@/composables/useMonitoramentoDiagnostico';
import LayoutPadrao from '@/components/layout/LayoutPadrao.vue';
import CarregamentoPagina from '@/components/comum/CarregamentoPagina.vue';
import DiagnosticoEquipePainel from '@/components/diagnostico/DiagnosticoEquipePainel.vue';

const props = defineProps<{
  codSubprocesso: number;
  siglaUnidade: string;
}>();
const {carregando} = useMonitoramentoDiagnostico(props.codSubprocesso);
</script>
