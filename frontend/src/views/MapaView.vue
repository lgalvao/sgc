<template>
  <LayoutPadrao>
    <CarregamentoPagina v-if="carregandoInicial"/>
    <template v-else>
      <MapaAcoesHeader
          :codigo-subprocesso="codigoSubprocesso"
          :habilitar-acao-principal-mapa="habilitarAcaoPrincipalMapa"
          :habilitar-apresentar-sugestoes="habilitarApresentarSugestoes"
          :habilitar-devolver-mapa="habilitarDevolverMapa"
          :habilitar-disponibilizar-mapa="habilitarDisponibilizarMapa"
          :habilitar-validar-mapa="habilitarValidarMapa"
          :loading-disponibilizacao="loadingDisponibilizacao"
          :loading-exportacao-csv="loadingExportacaoCsv"
          :loading-exportacao-pdf="loadingExportacaoPdf"
          :loading-impacto="loadingImpacto"
          :loading-sugestoes-visualizacao="loadingSugestoesVisualizacao"
          :mostrar-acao-principal-mapa="mostrarAcaoPrincipalMapa"
          :mostrar-apresentar-sugestoes="mostrarApresentarSugestoes"
          :mostrar-devolver-mapa="mostrarDevolverMapa"
          :mostrar-disponibilizar-mapa="mostrarDisponibilizarMapa"
          :mostrar-exportacao-mapa="mostrarExportacaoMapa"
          :mostrar-validar-mapa="mostrarValidarMapa"
          :pode-ver-sugestoes="podeVerSugestoes"
          :pode-visualizar-impacto="podeVisualizarImpacto"
          :rotulo-acao-principal-mapa="rotuloAcaoPrincipalMapa"
          :unidade="unidade"
          :usar-menu-acoes-mapa="usarMenuAcoesMapa"
          @abrir-acao-principal="abrirModalAceitar"
          @abrir-devolver="abrirModalDevolucao"
          @abrir-disponibilizar="abrirModalDisponibilizar"
          @abrir-historico="verHistorico"
          @abrir-impacto="abrirModalImpacto"
          @abrir-sugestoes="abrirModalSugestoes"
          @abrir-validar="abrirModalValidar"
          @exportar-csv="exportarMapaAtualCsv"
          @exportar-pdf="exportarMapaAtualPdf"
          @ver-sugestoes="verSugestoes"
      >
        <template #alerta>
          <Alerta
              v-if="erroMapaExibido"
              :chave="erroValidacaoMapaTick"
              :mensagem="erroMapaExibido"
              variante="danger"
              @dismissed="dispensarErroMapa"
          />
        </template>
      </MapaAcoesHeader>

      <div v-if="unidade">
        <div v-if="modoSomenteLeitura" class="mb-4 mt-3">
          <MapaSomenteLeitura :mapa="mapaSomenteLeitura"/>
        </div>

        <template v-else>
          <div class="mb-3 mt-3">
            <BButton
                :disabled="!habilitarEditarMapa"
                data-testid="btn-abrir-criar-competencia"
                variant="outline-primary"
                @click="abrirModalCriarLimpo"
            >
              <i aria-hidden="true" class="bi bi-plus-lg me-1"/> {{ TEXTOS.mapa.BOTAO_CRIAR }}
            </BButton>
          </div>

          <div v-if="competencias.length === 0" class="mb-4 mt-3">
            <EmptyState
                :description="TEXTOS.mapa.EMPTY_DESCRIPTION"
                :title="TEXTOS.mapa.EMPTY_TITLE"
                class="mb-0"
                icon="bi-journal-plus"
            />
          </div>

          <div v-else class="mb-4 mt-3">
            <CompetenciaCard
                v-for="comp in competencias"
                :key="comp.codigo"
                :atividades="atividades"
                :competencia="comp"
                :pode-editar="podeEditarMapa"
                @editar="iniciarEdicaoCompetencia"
                @excluir="(codigo) => excluirCompetencia(codigo)"
                @remover-atividade="(codigoCompetencia, codAtividade) => removerAtividadeAssociada(codigoCompetencia, codAtividade)"
            />
          </div>
        </template>
      </div>

      <div v-else>
        <p>{{ TEXTOS.mapa.UNIDADE_NAO_ENCONTRADA }}</p>
      </div>

      <MapaModaisRoot
          :atividades="atividades"
          :carregando-fluxo-mapa="carregandoFluxoMapa"
          :codigo-subprocesso="codigoSubprocesso"
          :competencia-para-excluir="competenciaParaExcluir"
          :competencia-sendo-editada="competenciaSendoEditada"
          :field-errors="fieldErrors"
          :historico-analise="historicoAnalise"
          :homologacao="acaoPrincipalMapa?.codigo === 'HOMOLOGAR'"
          :impactos="impactos"
          :loading-competencia="loadingCompetencia"
          :loading-disponibilizacao="loadingDisponibilizacao"
          :loading-exclusao="loadingExclusao"
          :loading-impacto="loadingImpacto"
          :loading-sugestoes-envio="loadingSugestoesEnvio"
          :mensagem-erro-devolucao="mensagemErroDevolucao"
          :mensagem-erro-sugestoes="mensagemErroSugestoes"
          :modo-somente-leitura="modoSomenteLeitura"
          :mostrar-modal-aceitar="mostrarModalAceitar"
          :mostrar-modal-criar-nova-competencia="mostrarModalCriarNovaCompetencia"
          :mostrar-modal-devolucao="mostrarModalDevolucao"
          :mostrar-modal-disponibilizar="mostrarModalDisponibilizar"
          :mostrar-modal-excluir-competencia="mostrarModalExcluirCompetencia"
          :mostrar-modal-historico="mostrarModalHistorico"
          :mostrar-modal-impacto="mostrarModalImpacto"
          :mostrar-modal-sugestoes="mostrarModalSugestoes"
          :mostrar-modal-validar="mostrarModalValidar"
          :mostrar-modal-ver-sugestoes="mostrarModalVerSugestoes"
          :notificacao-disponibilizacao="notificacaoDisponibilizacao"
          :observacao-devolucao="observacaoDevolucao"
          :pode-apresentar-sugestoes="podeApresentarSugestoes"
          :sugestoes="sugestoes"
          :sugestoes-visualizacao="sugestoesVisualizacao"
          :data-fim-etapa1="subprocesso?.dataFimEtapa1"
          :ultima-data-limite-subprocesso="subprocesso?.ultimaDataLimiteSubprocesso"
          @disponibilizar="disponibilizarMapa"
          @confirmar-aceitacao="confirmarAceitacao"
          @confirmar-devolucao="confirmarDevolucao"
          @confirmar-exclusao-competencia="confirmarExclusaoCompetencia"
          @confirmar-sugestoes="confirmarSugestoes"
          @confirmar-validacao="confirmarValidacao"
          @fechar-aceite="fecharModalAceitar"
          @fechar-criar-competencia="fecharModalCriarNovaCompetencia"
          @fechar-disponibilizar="fecharModalDisponibilizar"
          @fechar-historico="fecharModalHistorico"
          @fechar-impacto="fecharModalImpacto"
          @fechar-ver-sugestoes="fecharModalVerSugestoes"
          @salvar-competencia="adicionarCompetenciaEFecharModal"
          @update:mostrar-modal-devolucao="mostrarModalDevolucao = $event"
          @update:mostrar-modal-excluir-competencia="mostrarModalExcluirCompetencia = $event"
          @update:mostrar-modal-sugestoes="mostrarModalSugestoes = $event"
          @update:mostrar-modal-validar="mostrarModalValidar = $event"
          @update:mostrar-modal-ver-sugestoes="mostrarModalVerSugestoes = $event"
          @update:observacao-devolucao="observacaoDevolucao = $event"
          @update:sugestoes="sugestoes = $event"
          @update:sugestoes-visualizacao="sugestoesVisualizacao = $event"
      />
    </template>
  </LayoutPadrao>
</template>

<script lang="ts" setup>
import {BButton} from "bootstrap-vue-next";
import Alerta from "@/components/comum/Alerta.vue";
import LayoutPadrao from '@/components/layout/LayoutPadrao.vue';
import EmptyState from "@/components/comum/EmptyState.vue";
import MapaAcoesHeader from "@/components/mapa/MapaAcoesHeader.vue";
import MapaModaisRoot from "@/components/mapa/modais/MapaModaisRoot.vue";
import CompetenciaCard from "@/components/mapa/CompetenciaCard.vue";
import MapaSomenteLeitura from "@/components/mapa/MapaSomenteLeitura.vue";
import CarregamentoPagina from "@/components/comum/CarregamentoPagina.vue";
import {useMapaTela} from "@/composables/useMapaTela";
import {TEXTOS} from "@/constants/textos";

const props = defineProps<{ codProcesso: number | string; sigla: string; codSubprocesso?: number }>();
const {
  carregandoInicial,
  carregandoFluxoMapa,
  codigoSubprocesso,
  unidade,
  subprocesso,
  podeVisualizarImpacto,
  podeApresentarSugestoes,
  podeEditarMapa,
  mostrarValidarMapa,
  mostrarApresentarSugestoes,
  mostrarDisponibilizarMapa,
  mostrarDevolverMapa,
  habilitarApresentarSugestoes,
  habilitarDisponibilizarMapa,
  habilitarEditarMapa,
  habilitarValidarMapa,
  podeVerSugestoes,
  habilitarDevolverMapa,
  acaoPrincipalMapa,
  usarMenuAcoesMapa,
  modoSomenteLeitura,
  mostrarAcaoPrincipalMapa,
  habilitarAcaoPrincipalMapa,
  rotuloAcaoPrincipalMapa,
  mostrarExportacaoMapa,
  loadingExportacaoPdf,
  loadingExportacaoCsv,
  impactos,
  atividades,
  competencias,
  mapaSomenteLeitura,
  mostrarModalAceitar,
  mostrarModalValidar,
  mostrarModalDevolucao,
  mostrarModalHistorico,
  mostrarModalDisponibilizar,
  observacaoDevolucao,
  historicoAnalise,
  sugestoes,
  sugestoesVisualizacao,
  loadingSugestoesVisualizacao,
  loadingSugestoesEnvio,
  mostrarModalSugestoes,
  mostrarModalVerSugestoes,
  mensagemErroDevolucao,
  mensagemErroSugestoes,
  mostrarModalImpacto,
  loadingImpacto,
  loadingCompetencia,
  loadingExclusao,
  loadingDisponibilizacao,
  notificacaoDisponibilizacao,
  fieldErrors,
  erroValidacaoMapaTick,
  erroMapaExibido,
  competenciaSendoEditada,
  mostrarModalCriarNovaCompetencia,
  mostrarModalExcluirCompetencia,
  competenciaParaExcluir,
  abrirModalAceitar,
  abrirModalDevolucao,
  abrirModalDisponibilizar,
  verHistorico,
  abrirModalImpacto,
  abrirModalSugestoes,
  abrirModalValidar,
  exportarMapaAtualCsv,
  exportarMapaAtualPdf,
  verSugestoes,
  dispensarErroMapa,
  abrirModalCriarLimpo,
  iniciarEdicaoCompetencia,
  excluirCompetencia,
  removerAtividadeAssociada,
  disponibilizarMapa,
  confirmarAceitacao,
  confirmarDevolucao,
  confirmarExclusaoCompetencia,
  confirmarSugestoes,
  confirmarValidacao,
  fecharModalAceitar,
  fecharModalCriarNovaCompetencia,
  fecharModalDisponibilizar,
  fecharModalHistorico,
  fecharModalImpacto,
  fecharModalVerSugestoes,
  adicionarCompetenciaEFecharModal,
  existeCompetenciaSemAtividade,
  aplicarErroNormalizado,
} = useMapaTela(props);

defineExpose({
  existeCompetenciaSemAtividade,
  aplicarErroNormalizado,
});
</script>
