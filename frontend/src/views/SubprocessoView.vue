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
          :mostrar-acoes-cabecalho="mostrarAcoesCabecalho"
          :mostrar-alterar-data-limite="mostrarAlterarDataLimite"
          :mostrar-enviar-lembrete="mostrarEnviarLembrete"
          :mostrar-reabrir-cadastro="mostrarReabrirCadastro"
          :mostrar-reabrir-revisao="mostrarReabrirRevisao"
          :habilitar-alterar-data-limite="habilitarAlterarDataLimite"
          :habilitar-enviar-lembrete="habilitarEnviarLembrete"
          :habilitar-reabrir-cadastro="habilitarReabrirCadastro"
          :habilitar-reabrir-revisao="habilitarReabrirRevisao"
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

      <SubprocessoMovimentacoes :movimentacoes="movimentacoes" />
    </div>
    <div v-else-if="subprocessoStore.erroIntegracaoContexto" class="py-2">
      <BAlert
          :model-value="true"
          variant="danger"
          dismissible
          @dismissed="subprocessoStore.limparErroIntegracao()"
      >
        {{ subprocessoStore.erroIntegracaoContexto.mensagem }}
        <div v-if="subprocessoStore.erroIntegracaoContexto.detalhes">
          <small>Detalhes: {{ subprocessoStore.erroIntegracaoContexto.detalhes }}</small>
        </div>
      </BAlert>
    </div>
    <div v-else-if="erroNaoEncontrado" class="text-center py-5">
      <i class="bi bi-exclamation-triangle fs-1 text-warning mb-3 d-block"></i>
      <h3>{{ TEXTOS.subprocesso.NAO_ENCONTRADO_TITULO }}</h3>
      <p class="text-muted">{{ TEXTOS.subprocesso.NAO_ENCONTRADO_DESC }}</p>
      <BButton to="/painel" variant="primary" class="mt-3">Voltar para o Painel</BButton>
    </div>
    <CarregamentoPagina v-else class="py-5" />
  </LayoutPadrao>

  <SubprocessoFluxoModais
      :data-limite-atual="dataLimite"
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
import {BAlert, BButton, useToast} from "bootstrap-vue-next";
import {computed} from "vue";
import LayoutPadrao from "@/components/layout/LayoutPadrao.vue";
import SubprocessoCards from "@/components/processo/SubprocessoCards.vue";
import SubprocessoFluxoModais from "@/components/processo/SubprocessoFluxoModais.vue";
import SubprocessoMovimentacoes from "@/components/processo/SubprocessoMovimentacoes.vue";
import SubprocessoResumoHeader from "@/components/processo/SubprocessoResumoHeader.vue";
import AppAlert from "@/components/comum/AppAlert.vue";
import CarregamentoPagina from "@/components/comum/CarregamentoPagina.vue";
import {useNotification} from "@/composables/useNotification";
import {useFluxoSubprocesso} from "@/composables/useFluxoSubprocesso";
import {enviarLembrete as enviarLembreteService} from "@/services/processo";

import {useAcesso} from "@/composables/acesso";
import {type Movimentacao, type ResponsavelDto, type SubprocessoDetalhe, TipoProcesso} from "@/types/tipos";
import {formatarDataBR, analisarData} from "@/utils";
import {formatSituacaoSubprocesso} from "@/utils/formatters";
import {TEXTOS} from "@/constants/textos";
import {useSubprocessoStore} from "@/stores/subprocesso";
import {useMapasStore} from "@/stores/mapas";
import {useToastStore} from "@/stores/toast";
import {useValidacaoFormulario} from "@/composables/useValidacaoFormulario";
import {useSubprocessoAcoesAdministrativas} from "@/views/subprocessoAcoesAdministrativas";
import {useSubprocessoCarregamento} from "@/views/subprocessoCarregamento";

const props = defineProps<{ codProcesso: number; siglaUnidade: string; codSubprocesso?: number }>();

function formatDataSimples(dataStr: string | null): string { return dataStr ? formatarDataBR(dataStr) : ''; }

function formatTipoResponsabilidade(resp: ResponsavelDto | null): string {
  if (!resp?.tipo) return '';
  if (resp.tipo === 'Substituição' && resp.dataFim) {
    return `Substituição (até ${formatDataSimples(resp.dataFim)})`;
  } else if (resp.tipo === 'Atribuição temporária' && resp.dataFim) {
    return `Atrib. temporária (até ${formatDataSimples(resp.dataFim)})`;
  }
  return resp.tipo;
}

const subprocessoStore = useSubprocessoStore();
const mapasStore = useMapasStore();
const fluxoSubprocesso = useFluxoSubprocesso();
const {notificacao, notify, clear} = useNotification();
const toastStore = useToastStore();
const toast = useToast();
const {
  validarSubmissao,
  resetarValidacao,
  deveExibirErro,
  focarPrimeiroErroInvalido
} = useValidacaoFormulario();

const subprocesso = computed<SubprocessoDetalhe | null>(
    () => subprocessoStore.contextoEdicao?.detalhes ?? null,
);

const {
  habilitarAlterarDataLimite,
  habilitarReabrirCadastro,
  habilitarReabrirRevisao,
  habilitarEnviarLembrete,
  mostrarAlterarDataLimite,
  mostrarReabrirCadastro,
  mostrarReabrirRevisao,
  mostrarEnviarLembrete
} = useAcesso(subprocesso);

const mostrarAcoesCabecalho = computed(() =>
    mostrarAlterarDataLimite.value
    || mostrarReabrirCadastro.value
    || mostrarReabrirRevisao.value
    || mostrarEnviarLembrete.value
);

const movimentacoes = computed<Movimentacao[]>(
    () => subprocesso.value?.movimentacoes ?? [],
);
const dataLimite = computed(() => {
  if (subprocesso.value?.prazoEtapaAtual) {
    return analisarData(subprocesso.value.prazoEtapaAtual);
  }
  const ultimaDataLimite = subprocesso.value?.ultimaDataLimiteSubprocesso;
  return ultimaDataLimite ? analisarData(ultimaDataLimite) : null;
});

function exibirToastPendente() {
  const pendente = toastStore.consumePending();
  if (pendente) {
    toast.create({
      props: {
        body: pendente.body,
        variant: 'success',
        modelValue: 4000,
        pos: 'bottom-end',
        noProgress: true,
      }
    });
  }
}
const {
  codigoSubprocesso,
  erroNaoEncontrado,
  atualizarSubprocessoAtual,
} = useSubprocessoCarregamento({
  codProcesso: props.codProcesso,
  siglaUnidade: props.siglaUnidade,
  codSubprocesso: props.codSubprocesso,
  erroIntegracaoContexto: computed(() => subprocessoStore.erroIntegracaoContexto),
  garantirContextoEdicao: subprocessoStore.garantirContextoEdicao,
  garantirContextoEdicaoPorProcessoEUnidade: subprocessoStore.garantirContextoEdicaoPorProcessoEUnidade,
  invalidarMapa: mapasStore.invalidar,
  exibirToastPendente,
});

const {
  tipoReabertura,
  justificativaReabertura,
  modalLembreteAberto,
  mostrarModalAlterarDataLimite,
  mostrarModalReabrir,
  loadingDataLimite,
  loadingReabertura,
  loadingLembrete,
  mensagemErroJustificativa,
  abrirModalAlterarDataLimite,
  fecharModalAlterarDataLimite,
  confirmarAlteracaoDataLimite,
  abrirModalReabrirCadastro,
  abrirModalReabrirRevisao,
  confirmarReabertura,
  confirmarEnviarLembrete,
  enviarLembreteConfirmado,
} = useSubprocessoAcoesAdministrativas({
  subprocesso,
  codigoSubprocesso,
  codProcesso: props.codProcesso,
  habilitarAlterarDataLimite,
  deveExibirErro,
  resetarValidacao,
  validarSubmissao,
  focarPrimeiroErroInvalido,
  notify,
  atualizarSubprocessoAtual,
  exibirToastPendente,
  alterarDataLimiteSubprocesso: fluxoSubprocesso.alterarDataLimiteSubprocesso,
  reabrirCadastro: fluxoSubprocesso.reabrirCadastro,
  reabrirRevisaoCadastro: fluxoSubprocesso.reabrirRevisaoCadastro,
  enviarLembrete: enviarLembreteService,
  garantirContextoEdicao: subprocessoStore.garantirContextoEdicao,
});

</script>
