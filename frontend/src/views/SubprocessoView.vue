<template>
  <LayoutPadrao>
    <AppAlert
        v-if="notificacao"
        :dispensavel="notificacao.dispensavel ?? true"
        :mensagem="notificacao.mensagem"
        :variante="notificacao.variante"
        @dismissed="clear()"
    />
    <div v-if="subprocesso">
      <SubprocessoResumoHeader
          :format-data-simples="formatDataSimples"
          :format-situacao-subprocesso="formatSituacaoSubprocesso"
          :format-tipo-responsabilidade="formatTipoResponsabilidade"
          :habilitar-alterar-data-limite="habilitarAlterarDataLimite"
          :habilitar-enviar-lembrete="habilitarEnviarLembrete"
          :habilitar-reabrir-cadastro="habilitarReabrirCadastro"
          :habilitar-reabrir-revisao="habilitarReabrirRevisao"
          :mostrar-acoes-cabecalho="mostrarAcoesCabecalho"
          :mostrar-alterar-data-limite="mostrarAlterarDataLimite"
          :mostrar-enviar-lembrete="mostrarEnviarLembrete"
          :mostrar-reabrir-cadastro="mostrarReabrirCadastro"
          :mostrar-reabrir-revisao="mostrarReabrirRevisao"
          :sigla-unidade-fallback="props.siglaUnidade"
          :subprocesso="subprocesso"
          @abrir-alterar-data-limite="abrirModalAlterarDataLimite"
          @abrir-reabrir-cadastro="abrirModalReabrirCadastro"
          @abrir-reabrir-revisao="abrirModalReabrirRevisao"
          @confirmar-enviar-lembrete="confirmarEnviarLembrete"
      />

      <SubprocessoCards
          v-if="codigoSubprocesso"
          :cod-processo="props.codProcesso"
          :cod-subprocesso="codigoSubprocesso"
          :mapa="null"
          :sigla-unidade="props.siglaUnidade"
          :situacao="subprocesso.situacao"
          :subprocesso="subprocesso"
          :tipo-processo="subprocesso.tipoProcesso || TipoProcesso.MAPEAMENTO"
      />

      <SubprocessoMovimentacoes :movimentacoes="movimentacoes"/>
    </div>
    <div v-else-if="erroIntegracaoContexto" class="py-2">
      <BAlert
          :model-value="true"
          dismissible
          variant="danger"
          @dismissed="limparErroIntegracao()"
      >
        {{ erroIntegracaoContexto.mensagem }}
        <div v-if="erroIntegracaoContexto.detalhes">
          <small>Detalhes: {{ erroIntegracaoContexto.detalhes }}</small>
        </div>
      </BAlert>
    </div>
    <div v-else-if="erroNaoEncontrado" class="text-center py-5">
      <i class="bi bi-exclamation-triangle fs-1 text-warning mb-3 d-block"></i>
      <h3>{{ TEXTOS.subprocesso.NAO_ENCONTRADO_TITULO }}</h3>
      <p class="text-muted">{{ TEXTOS.subprocesso.NAO_ENCONTRADO_DESC }}</p>
      <BButton class="mt-3" to="/painel" variant="primary">Voltar para o Painel</BButton>
    </div>
    <CarregamentoPagina v-else class="py-5"/>
  </LayoutPadrao>

  <SubprocessoFluxoModais
      :data-limite-atual="dataLimite"
      :data-fim-etapa1="subprocesso?.dataFimEtapa1 ? analisarData(subprocesso.dataFimEtapa1) : null"
      :etapa-atual="subprocesso?.etapaAtual ?? null"
      :justificativa-reabertura="justificativaReabertura"
      :loading-data-limite="loadingDataLimite"
      :loading-lembrete="loadingLembrete"
      :loading-reabertura="loadingReabertura"
      :mensagem-erro-justificativa="mensagemErroJustificativa"
      :modal-lembrete-aberto="modalLembreteAberto"
      :mostrar-modal-alterar-data-limite="mostrarModalAlterarDataLimite"
      :mostrar-modal-reabrir="mostrarModalReabrir"
      :sigla-unidade="subprocesso?.unidade?.sigla ?? ''"
      :tipo-reabertura="tipoReabertura"
      :ultima-data-limite-subprocesso="subprocesso?.ultimaDataLimiteSubprocesso ? analisarData(subprocesso.ultimaDataLimiteSubprocesso) : null"
      @confirmar-alteracao-data="confirmarAlteracaoDataLimite"
      @confirmar-enviar-lembrete="enviarLembreteConfirmado"
      @confirmar-reabertura="confirmarReabertura"
      @fechar-modal-data="fecharModalAlterarDataLimite"
      @update:justificativa-reabertura="justificativaReabertura = $event"
      @update:modal-lembrete-aberto="modalLembreteAberto = $event"
      @update:mostrar-modal-reabrir="mostrarModalReabrir = $event"
  />
</template>

<script lang="ts" setup>
import {BAlert, BButton} from "bootstrap-vue-next";
import LayoutPadrao from "@/components/layout/LayoutPadrao.vue";
import SubprocessoCards from "@/components/processo/SubprocessoCards.vue";
import SubprocessoFluxoModais from "@/components/processo/SubprocessoFluxoModais.vue";
import SubprocessoMovimentacoes from "@/components/processo/SubprocessoMovimentacoes.vue";
import SubprocessoResumoHeader from "@/components/processo/SubprocessoResumoHeader.vue";
import AppAlert from "@/components/comum/AppAlert.vue";
import CarregamentoPagina from "@/components/comum/CarregamentoPagina.vue";
import {useSubprocessoTela} from "@/composables/useSubprocessoTela";

const props = defineProps<{ codProcesso: number; siglaUnidade: string; codSubprocesso?: number }>();

const tela = useSubprocessoTela(props);

const {
  erroIntegracaoContexto,
  limparErroIntegracao,
  notificacao,
  clear,
  subprocesso,
  formatDataSimples,
  formatSituacaoSubprocesso,
  formatTipoResponsabilidade,
  habilitarAlterarDataLimite,
  habilitarEnviarLembrete,
  habilitarReabrirCadastro,
  habilitarReabrirRevisao,
  mostrarAcoesCabecalho,
  mostrarAlterarDataLimite,
  mostrarEnviarLembrete,
  mostrarReabrirCadastro,
  mostrarReabrirRevisao,
  codigoSubprocesso,
  movimentacoes,
  erroNaoEncontrado,
  TEXTOS,
  TipoProcesso,
  dataLimite,
  analisarData,
  justificativaReabertura,
  loadingDataLimite,
  loadingLembrete,
  loadingReabertura,
  mensagemErroJustificativa,
  modalLembreteAberto,
  mostrarModalAlterarDataLimite,
  mostrarModalReabrir,
  tipoReabertura,
  abrirModalAlterarDataLimite,
  abrirModalReabrirCadastro,
  abrirModalReabrirRevisao,
  confirmarEnviarLembrete,
  confirmarAlteracaoDataLimite,
  enviarLembreteConfirmado,
  confirmarReabertura,
  fecharModalAlterarDataLimite,
} = tela;

defineExpose({
  mostrarModalAlterarDataLimite: tela.mostrarModalAlterarDataLimite,
  mostrarModalReabrir: tela.mostrarModalReabrir,
  modalLembreteAberto: tela.modalLembreteAberto,
  tipoReabertura: tela.tipoReabertura,
  notify: tela.notify,
  confirmarAlteracaoDataLimite: tela.confirmarAlteracaoDataLimite,
  abrirModalAlterarDataLimite: tela.abrirModalAlterarDataLimite,
  erroNaoEncontrado: tela.erroNaoEncontrado,
  formatTipoResponsabilidade: tela.formatTipoResponsabilidade,
  formatDataSimples: tela.formatDataSimples,
});
</script>
