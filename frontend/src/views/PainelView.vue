<template>
  <LayoutPadrao>
    <h1 class="visually-hidden">{{ TEXTOS.painel.TITULO }}</h1>
    <CarregamentoPagina v-if="carregandoPainel" data-testid="painel-carregando"/>
    <template v-else>
      <!-- Tabela de Processos -->
      <div class="mb-5">
        <PageHeader :title="TEXTOS.painel.PROCESSOS" title-test-id="txt-painel-titulo-processos">
          <template #actions>
            <BButton
                v-if="perfil.mostrarCriarProcesso.value"
                :to="{ name: 'CadProcesso' }"
                class="btn-painel-criar-processo"
                data-testid="btn-painel-criar-processo"
                variant="primary"
            >
              <i aria-hidden="true" class="bi bi-plus-lg"/> Criar processo
            </BButton>
          </template>
        </PageHeader>
        <TabelaProcessos
            :compacto="true"
            :criterio-ordenacao="criterio"
            :direcao-ordenacao-asc="asc"
            :mostrar-cta-vazio="perfil.mostrarCtaPainelVazio.value"
            :processos="processosOrdenados"
            @ordenar="ordenarPor"
            @selecionar-processo="abrirDetalhesProcesso"
            @cta-vazio="void router.push({ name: 'CadProcesso' })"
        />
      </div>

      <div>
        <PageHeader :title="TEXTOS.painel.ALERTAS" title-test-id="txt-painel-titulo-alertas"/>
        <div v-if="alertas.length > 0" class="table-responsive">
          <BTable
              :fields="camposAlertas"
              :items="alertas"
              :tbody-tr-class="rowClassAlerta"
              :tbody-tr-props="rowAttrAlerta"
              aria-label="Alertas"
              data-testid="tbl-alertas"
              responsive
              small
              stacked="md"
          >
            <template #cell(mensagem)="data">
              <span v-if="!data.item.dataHoraLeitura" class="visually-hidden">{{ TEXTOS.comum.NAO_LIDO }}</span>
              {{ data.value }}
            </template>
          </BTable>
        </div>
        <EmptyState
            v-else
            :description="TEXTOS.painel.ALERTAS_VAZIO_DESCRICAO"
            :title="TEXTOS.painel.ALERTAS_VAZIO_TITULO"
            class="mb-0"
            data-testid="empty-state-alertas"
            icon="bi-bell-slash"
        />
      </div>
    </template>
  </LayoutPadrao>
</template>

<script lang="ts" setup>
import {BButton, BTable} from "bootstrap-vue-next";
import {useRouter} from "vue-router";
import LayoutPadrao from "@/components/layout/LayoutPadrao.vue";
import PageHeader from "@/components/layout/PageHeader.vue";
import EmptyState from "@/components/comum/EmptyState.vue";
import CarregamentoPagina from "@/components/comum/CarregamentoPagina.vue";
import TabelaProcessos from "@/components/processo/TabelaProcessos.vue";
import {usePainelTela} from "@/composables/usePainelTela";
import {TEXTOS} from "@/constants/textos";

const router = useRouter();

const tela = usePainelTela();

const {
  perfil,
  criterio,
  asc,
  processosOrdenados,
  alertas,
  carregandoPainel,
  camposAlertas,
  rowClassAlerta,
  rowAttrAlerta,
  ordenarPor,
  abrirDetalhesProcesso,
} = tela;

defineExpose({
  ordenarPor: tela.ordenarPor,
  asc: tela.asc,
  criterio: tela.criterio,
  abrirDetalhesProcesso: tela.abrirDetalhesProcesso,
  rowClassAlerta: tela.rowClassAlerta,
  rowAttrAlerta: tela.rowAttrAlerta,
  processosOrdenados: tela.processosOrdenados,
});
</script>

<style scoped>
:global([data-bs-theme="dark"] .btn-painel-criar-processo) {
  background-color: #2563eb !important;
  border-color: #2563eb !important;
  color: #f8fafc !important;
}

:global([data-bs-theme="dark"] .btn-painel-criar-processo .bi) {
  color: inherit !important;
}
</style>
