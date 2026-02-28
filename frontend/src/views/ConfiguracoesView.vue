<template>
  <div class="container-fluid mt-4">
    <PageHeader title="Configurações"/>

    <!-- Seção de Administradores -->
    <AdministradoresSection v-if="podeGerenciarAdministradores"/>

    <!-- Seção de Configurações de Parâmetros -->
    <ParametrosSection/>
  </div>
</template>

<script lang="ts" setup>
import {computed} from 'vue';
import PageHeader from '@/components/layout/PageHeader.vue';
import AdministradoresSection from '@/components/configuracoes/AdministradoresSection.vue';
import ParametrosSection from '@/components/configuracoes/ParametrosSection.vue';
import {usePerfilStore} from '@/stores/perfil';

const perfilStore = usePerfilStore();

const podeGerenciarAdministradores = computed(() => {
  return perfilStore.isAdmin;
});
</script>
