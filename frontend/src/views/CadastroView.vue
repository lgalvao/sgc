<template>
  <LayoutPadrao>
    <CarregamentoPagina v-if="carregandoInicial"/>
    <template v-else>
      <CadastroAcoesHeader
          :acao-principal-cadastro="acaoPrincipalCadastro"
          :cod-subprocesso="codigoSubprocesso"
          :loading-validacao="loadingValidacao"
          :mostrar-devolver-cadastro="mostrarDevolverCadastro"
          :mostrar-disponibilizar-cadastro="mostrarDisponibilizarCadastro"
          :mostrar-importar-atividades="mostrarImportarAtividades"
          :permissoes="permissoesUI"
          :pode-visualizar-impacto="podeVisualizarImpacto"
          :unidade="unidade"
          @disponibilizar="disponibilizarCadastro"
          @abrir-historico="abrirModalHistorico"
          @abrir-devolver="abrirModalDevolverAnalise"
          @abrir-validar="abrirModalValidarAnalise"
          @abrir-impacto="abrirModalImpacto"
          @abrir-importar="mostrarModalImportar = true"
      >
        <template #alerta>
          <AppAlert
              v-if="erroGlobal"
              :chave="erroTick"
              data-testid="alerta-erro-global"
              :mensagem="erroGlobal"
              variante="danger"
              @dismissed="erroGlobal = null"
          />

          <AppAlert
              v-if="notificacao"
              :chave="notificacao.chave"
              :dispensavel="notificacao.dispensavel"
              :mensagem="notificacao.mensagem"
              :variante="notificacao.variante"
              @dismissed="clear()"
          />
        </template>
      </CadastroAcoesHeader>

      <div v-if="mostrarControlesEdicaoCadastro && isRevisao" class="mt-3 mb-2">
        <BFormCheckbox
            v-model="disponibilizacaoSemMudancas"
            :disabled="checkboxSemMudancasDesabilitado"
            data-testid="chk-disponibilizacao-sem-mudancas"
        >
          {{ TEXTOS.atividades.CHECKBOX_DISPONIBILIZACAO_SEM_MUDANCAS }}
        </BFormCheckbox>
        <div
            v-if="loadingInicioRevisao"
            class="d-inline-flex align-items-center mt-1"
            data-testid="cad-atividades__spinner-iniciando-revisao"
        >
          <BSpinner small/>
        </div>
      </div>

      <CadAtividadeForm
          v-if="mostrarControlesEdicaoCadastro"
          ref="atividadeFormRef"
          v-model="novaAtividade"
          :disabled="!codigoSubprocesso || !habilitarEditarCadastro"
          :erro="erroNovaAtividade"
          :loading="loadingAdicionar"
          @submit="adicionarNovaAtividade"
      />

      <EmptyState
          v-if="atividades?.length === 0"
          :description="mostrarControlesEdicaoCadastro ? TEXTOS.atividades.EMPTY_DESCRIPTION : TEXTOS.treeTable.EMPTY_DESCRIPTION"
          :title="mostrarControlesEdicaoCadastro ? TEXTOS.atividades.EMPTY_TITLE : TEXTOS.treeTable.EMPTY_TITLE"
          data-testid="cad-atividades-empty-state"
          icon="bi-list-check"
      />

      <div
          v-for="atividade in atividadesOrdenadas"
          :key="atividade.codigo"
          :ref="el => setAtividadeRef(atividade.codigo, el)"
      >
        <AtividadeItem
            :atividade="atividade"
            :erro-validacao="obterErroParaAtividade(atividade.codigo)"
            :habilitar-edicao="habilitarEditarCadastro"
            :pode-editar="mostrarControlesEdicaoCadastro"
            @atualizar-atividade="(desc: string) => salvarEdicaoAtividade(atividade.codigo, desc)"
            @remover-atividade="() => removerAtividade(atividade.codigo)"
            @adicionar-conhecimento="(desc: string) => adicionarConhecimento(atividade.codigo, desc)"
            @atualizar-conhecimento="(idC: number, desc: string) => salvarEdicaoConhecimento(atividade.codigo, idC, desc)"
            @remover-conhecimento="(idC: number) => removerConhecimento(atividade.codigo, idC)"
        />
      </div>

      <CadastroFluxoModais
          :acao-principal-cadastro="acaoPrincipalCadastro"
          :codigo-subprocesso="codigoSubprocesso"
          :dados-remocao="dadosRemocao"
          :erro-fluxo="erroFluxoCadastro"
          :historico-analises="historicoAnalises"
          :impactos="impactos"
          :is-revisao="isRevisao"
          :loading-analise-cadastro="loadingAnaliseCadastro"
          :loading-devolucao-analise="loadingDevolucaoAnalise"
          :loading-disponibilizacao="loadingDisponibilizacao"
          :loading-impacto="loadingImpacto"
          :loading-remocao="loadingRemocao"
          :mostrar-modal-confirmacao="mostrarModalConfirmacao"
          :mostrar-modal-confirmacao-remocao="mostrarModalConfirmacaoRemocao"
          :mostrar-modal-devolver-analise="mostrarModalDevolverAnalise"
          :mostrar-modal-historico="mostrarModalHistorico"
          :mostrar-modal-impacto="mostrarModalImpacto"
          :mostrar-modal-importar="mostrarModalImportar"
          :mostrar-modal-validar-analise="mostrarModalValidarAnalise"
          :observacao-devolucao="observacaoDevolucao"
          :erro-observacao-devolucao="mensagemErroObservacaoDevolucao"
          :observacao-validacao="observacaoValidacao"
          @importar="aoImportarAtividades"
          @confirmar-devolucao-analise="confirmarDevolucaoAnalise"
          @confirmar-disponibilizacao="confirmarDisponibilizacao"
          @confirmar-remocao="confirmarRemocao"
          @confirmar-validacao-analise="confirmarValidacaoAnalise"
          @fechar-impacto="fecharModalImpacto"
          @update:mostrar-modal-confirmacao="mostrarModalConfirmacao = $event"
          @update:mostrar-modal-confirmacao-remocao="mostrarModalConfirmacaoRemocao = $event"
          @update:mostrar-modal-devolver-analise="mostrarModalDevolverAnalise = $event"
          @update:mostrar-modal-historico="mostrarModalHistorico = $event"
          @update:mostrar-modal-importar="mostrarModalImportar = $event"
          @update:mostrar-modal-validar-analise="mostrarModalValidarAnalise = $event"
          @update:observacao-devolucao="observacaoDevolucao = $event"
          @update:observacao-validacao="observacaoValidacao = $event"
      />
    </template>
  </LayoutPadrao>
</template>

<script lang="ts" setup>
import {BFormCheckbox, BSpinner} from "bootstrap-vue-next";
import AppAlert from "@/components/comum/AppAlert.vue";
import LayoutPadrao from "@/components/layout/LayoutPadrao.vue";
import CarregamentoPagina from "@/components/comum/CarregamentoPagina.vue";
import EmptyState from "@/components/comum/EmptyState.vue";
import CadAtividadeForm from "@/components/atividades/CadAtividadeForm.vue";
import AtividadeItem from "@/components/atividades/AtividadeItem.vue";
import CadastroAcoesHeader from "@/components/cadastro/CadastroAcoesHeader.vue";
import CadastroFluxoModais from "@/components/cadastro/CadastroFluxoModais.vue";
import {useCadastroTela} from "@/composables/useCadastroTela";
import {TEXTOS} from "@/constants/textos";

const props = defineProps<{
  codProcesso: number | string;
  sigla: string;
  codSubprocesso?: number;
}>();

const tela = useCadastroTela(props);

const {
  atividades,
  carregandoInicial,
  codigoSubprocesso,
  unidade,
  permissoesUI,
  isRevisao,
  mostrarControlesEdicaoCadastro,
  disponibilizacaoSemMudancas,
  checkboxSemMudancasDesabilitado,
  loadingInicioRevisao,
  erroTick,
  erroGlobal,
  notificacao,
  clear,
  novaAtividade,
  erroNovaAtividade,
  loadingAdicionar,
  atividadesOrdenadas,
  obterErroParaAtividade,
  dadosRemocao,
  erroFluxoCadastro,
  historicoAnalises,
  impactos,
  loadingAnaliseCadastro,
  loadingDevolucaoAnalise,
  loadingDisponibilizacao,
  loadingImpacto,
  loadingRemocao,
  mostrarModalConfirmacao,
  mostrarModalConfirmacaoRemocao,
  mostrarModalDevolverAnalise,
  mostrarModalHistorico,
  mostrarModalImpacto,
  mostrarModalImportar,
  mostrarModalValidarAnalise,
  observacaoDevolucao,
  mensagemErroObservacaoDevolucao,
  observacaoValidacao,
  habilitarEditarCadastro,
  mostrarDevolverCadastro,
  mostrarDisponibilizarCadastro,
  mostrarImportarAtividades,
  podeVisualizarImpacto,
  acaoPrincipalCadastro,
  loadingValidacao,
  atividadeFormRef,
  setAtividadeRef,
  aoImportarAtividades,
  confirmarDevolucaoAnalise,
  confirmarDisponibilizacao,
  confirmarRemocao,
  confirmarValidacaoAnalise,
  fecharModalImpacto,
  adicionarNovaAtividade,
  salvarEdicaoAtividade,
  removerAtividade,
  adicionarConhecimento,
  salvarEdicaoConhecimento,
  removerConhecimento,
  abrirModalHistorico,
  abrirModalDevolverAnalise,
  abrirModalValidarAnalise,
  abrirModalImpacto,
  disponibilizarCadastro,
} = tela;

defineExpose({
  atividades: tela.atividades,
  atividadesSnapshotInicial: tela.atividadesSnapshotInicial,
  disponibilizacaoSemMudancas: tela.disponibilizacaoSemMudancas,
  mostrarModalConfirmacao: tela.mostrarModalConfirmacao,
  mostrarModalImportar: tela.mostrarModalImportar,
  mostrarModalConfirmacaoRemocao: tela.mostrarModalConfirmacaoRemocao,
  dadosRemocao: tela.dadosRemocao,
  erroGlobal: tela.erroGlobal,
  erroNovaAtividade: tela.erroNovaAtividade,
  errosValidacao: tela.errosValidacao,
  notificacao: tela.notificacao,
  novaAtividade: tela.novaAtividade,
  aoImportarAtividades: tela.aoImportarAtividades,
  disponibilizarCadastro: tela.disponibilizarCadastro,
  adicionarAtividade: tela.adicionarAtividade,
  confirmarRemocao: tela.confirmarRemocao,
  processarRespostaLocal: tela.processarRespostaLocal,
  notify: tela.notify,
  carregarContextoInicial: tela.carregarContextoInicial,
  scrollParaPrimeiroErro: tela.scrollParaPrimeiroErro,
  houveAlteracaoCadastro: tela.houveAlteracaoCadastro,
  podeEditarCadastro: tela.podeEditarCadastro,
  esconderEdicaoCadastroParaChefe: tela.esconderEdicaoCadastroParaChefe,
  codigoSubprocesso: tela.codigoSubprocesso,
  unidade: tela.unidade,
  limparErrosValidacao: tela.limparErrosValidacao,
  habilitarEditarCadastro: tela.habilitarEditarCadastro,
});

</script>
