<template>
  <div class="container-fluid mt-4">
    <PageHeader title="Configurações" />

    <AdministradoresSection v-if="podeGerenciarAdministradores" />

    <ParametrosSection />
  </div>
</template>

<script setup lang="ts">
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
