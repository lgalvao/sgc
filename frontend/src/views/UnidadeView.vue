<template>
  <LayoutPadrao>
    <Alerta
        v-if="!carregandoPagina && ultimoErro"
        data-testid="alert-unidade-erro"
        :mensagem="ultimoErro"
        @dismissed="erroDispensado = true"
    />

    <CarregamentoPagina v-if="carregandoPagina" :mensagem="TEXTOS.unidade.CARREGANDO"/>
    <template v-else>
      <div v-if="unidade">
        <PageHeader
            :subtitle="unidade.nome"
            :title="unidade.sigla"
            actions-test-id="unidade-view__acoes"
            title-test-id="unidade-view__titulo"
        >
          <template #actions>
            <BDropdown
                v-if="podeExportarMapaVigente"
                text="Mapa vigente"
                data-testid="btn-exportar-mapa-vigente"
                toggle-class="text-nowrap"
                variant="outline-secondary"
            >
              <BDropdownItemButton
                  :disabled="loadingExportacaoPdf"
                  data-testid="btn-exportar-mapa-vigente-pdf"
                  @click="exportarMapaVigentePdf"
              >
                PDF
              </BDropdownItemButton>
              <BDropdownItemButton
                  :disabled="loadingExportacaoCsv"
                  data-testid="btn-exportar-mapa-vigente-csv"
                  @click="exportarMapaVigenteCsv"
              >
                {{ TEXTOS_RELATORIOS.BOTAO_CSV }}
              </BDropdownItemButton>
            </BDropdown>
            <BButton
                v-if="mostrarCriarAtribuicaoTemporaria"
                data-testid="unidade-view__btn-criar-atribuicao"
                variant="outline-secondary"
                @click="irParaCriarAtribuicao"
            >
              <span data-testid="unidade-view__btn-atribuicao-texto">{{ textoBotaoAtribuicao }}</span>
            </BButton>
          </template>
        </PageHeader>

        <BCard class="mb-4" no-body>
          <BCardBody>
            <UnidadeContatoInfo
                v-if="titularExibivel"
                :contato="unidade.titular"
                :label="TEXTOS.unidade.LABEL_TITULAR"
                :nome-fallback="TEXTOS.unidade.NAO_INFORMADO"
                data-testid="unidade-titular-info"
                detalhes-class="ms-3 mb-2"
            />
            <UnidadeContatoInfo
                v-if="responsavelExibivel"
                :contato="responsavelExibivel"
                :descricao="descricaoContatoPrincipal"
                :label="labelContatoPrincipal"
                data-testid="unidade-responsavel-info"
            />
          </BCardBody>
        </BCard>
      </div>
      <EmptyState
          v-else
          :description="TEXTOS.unidade.EMPTY_DESCRIPTION"
          :title="TEXTOS.unidade.EMPTY_TITLE"
          icon="bi-building"
      />

      <div
          v-if="temSubordinadas"
          class="mt-5"
      >
        <TreeTable
            :columns="colunasTabela"
            :data="dadosFormatadosSubordinadas"
            :hide-headers="true"
            :title="TEXTOS.unidade.SUBORDINADAS_TITULO"
            @row-click="navegarParaUnidadeSubordinada"
        />
      </div>
    </template>
  </LayoutPadrao>
</template>

<script lang="ts" setup>
import {BButton, BCard, BCardBody, BDropdown, BDropdownItemButton} from "bootstrap-vue-next";
import Alerta from '@/components/comum/Alerta.vue';
import LayoutPadrao from '@/components/layout/LayoutPadrao.vue';
import TreeTable from "@/components/comum/TreeTable.vue";
import PageHeader from "@/components/layout/PageHeader.vue";
import EmptyState from "@/components/comum/EmptyState.vue";
import CarregamentoPagina from "@/components/comum/CarregamentoPagina.vue";
import UnidadeContatoInfo from "@/components/unidade/UnidadeContatoInfo.vue";
import {useUnidadeTela} from "@/composables/useUnidadeTela";
import {TEXTOS} from "@/constants/textos";
import {TEXTOS_RELATORIOS} from "@/constants/textos-relatorios";

const props = defineProps<{ codUnidade: number }>();

const tela = useUnidadeTela(props);

const {
  unidade,
  carregandoPagina,
  ultimoErro,
  erroDispensado,
  loadingExportacaoPdf,
  loadingExportacaoCsv,
  podeExportarMapaVigente,
  mostrarCriarAtribuicaoTemporaria,
  textoBotaoAtribuicao,
  titularExibivel,
  responsavelExibivel,
  labelContatoPrincipal,
  descricaoContatoPrincipal,
  temSubordinadas,
  colunasTabela,
  dadosFormatadosSubordinadas,
  irParaCriarAtribuicao,
  navegarParaUnidadeSubordinada,
  exportarMapaVigentePdf,
  exportarMapaVigenteCsv,
} = tela;

defineExpose({
  unidade: tela.unidade,
  mapaVigente: tela.mapaVigente,
  carregandoPagina: tela.carregandoPagina,
  ultimoErro: tela.ultimoErro,
  erroDispensado: tela.erroDispensado,
  loadingExportacaoPdf: tela.loadingExportacaoPdf,
  loadingExportacaoCsv: tela.loadingExportacaoCsv,
  podeExportarMapaVigente: tela.podeExportarMapaVigente,
  textoBotaoAtribuicao: tela.textoBotaoAtribuicao,
  titularExibivel: tela.titularExibivel,
  responsavelExibivel: tela.responsavelExibivel,
  labelContatoPrincipal: tela.labelContatoPrincipal,
  descricaoContatoPrincipal: tela.descricaoContatoPrincipal,
  temSubordinadas: tela.temSubordinadas,
  dadosFormatadosSubordinadas: tela.dadosFormatadosSubordinadas,
  irParaCriarAtribuicao: tela.irParaCriarAtribuicao,
  navegarParaUnidadeSubordinada: tela.navegarParaUnidadeSubordinada,
  exportarMapaVigentePdf: tela.exportarMapaVigentePdf,
  exportarMapaVigenteCsv: tela.exportarMapaVigenteCsv,
});
</script>
