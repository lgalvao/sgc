<template>
  <LayoutPadrao>
    <PageHeader title="Administradores"/>
    <AdministradoresSection/>
  </LayoutPadrao>
</template>

<script lang="ts" setup>
import LayoutPadrao from '@/components/layout/LayoutPadrao.vue';
import PageHeader from '@/components/layout/PageHeader.vue';
import AdministradoresSection from '@/components/configuracoes/AdministradoresSection.vue';
</script>
