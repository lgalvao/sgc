<template>
  <LayoutPadrao>
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
      >
        <template #alerta>
          <Alerta
              v-if="notificacao"
              :chave="notificacao.chave"
              :dispensavel="notificacao.dispensavel ?? true"
              :mensagem="notificacao.mensagem"
              :variante="notificacao.variante"
              @dismissed="clear()"
          />
        </template>
        <template #acoes-extras>
          <BButton
              v-if="mostrarHistoricoAnaliseDiagnostico"
              data-testid="btn-historico-analise-subprocesso"
              variant="outline-secondary"
              @click="abrirHistoricoAnaliseDiagnostico"
          >
            {{ TEXTOS.diagnostico.BTN_HISTORICO_ANALISE }}
          </BButton>
          <BButton
              v-if="mostrarConcluirDiagnosticoCabecalho"
              :disabled="concluindoDiagnostico || !habilitarConcluirDiagnosticoCabecalho"
              data-testid="btn-concluir-diagnostico-cabecalho"
              variant="success"
              @click="abrirModalConcluirDiagnostico"
          >
            <BSpinner v-if="concluindoDiagnostico" aria-hidden="true" class="me-1"/>
            {{ TEXTOS.diagnostico.BTN_CONCLUIR_DIAGNOSTICO }}
          </BButton>
        </template>
      </SubprocessoResumoHeader>

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

      <SubprocessoDiagnosticoPainel
          v-if="ehDiagnostico && codigoSubprocesso && !ehServidorPuro"
          :cod-subprocesso="codigoSubprocesso"
          :exibir-botao-voltar="false"
          :exibir-cabecalho="false"
          :exibir-botao-concluir-diagnostico="false"
          :permissoes-subprocesso="subprocesso.permissoes ?? null"
          :sigla-unidade="props.siglaUnidade"
      />

      <SubprocessoMovimentacoes v-if="!ehDiagnostico || !ehServidorPuro" :movimentacoes="movimentacoes"/>
    </div>
    <div v-else-if="erroIntegracaoContexto" class="py-2">
      <Alerta
          :dispensavel="true"
          :mensagem="erroIntegracaoContexto.detalhes ? undefined : erroIntegracaoContexto.mensagem"
          :notificacao="erroIntegracaoContexto.detalhes ? { resumo: erroIntegracaoContexto.mensagem, detalhes: [String(erroIntegracaoContexto.detalhes)] } : undefined"
          variante="danger"
          @dismissed="limparErroIntegracao()"
      />
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
      :carregando-historico-diagnostico="carregandoHistoricoDiagnostico"
      :concluindo-diagnostico="concluindoDiagnostico"
      :data-limite-atual="dataLimite"
      :data-fim-etapa1="subprocesso?.dataFimEtapa1 ? analisarData(subprocesso.dataFimEtapa1) : null"
      :erro-concluir-diagnostico="erroConcluirDiagnostico"
      :etapa-atual="subprocesso?.etapaAtual ?? null"
      :historico-analises-diagnostico="historicoAnalisesDiagnostico"
      :justificativa-reabertura="justificativaReabertura"
      :loading-data-limite="loadingDataLimite"
      :loading-lembrete="loadingLembrete"
      :loading-reabertura="loadingReabertura"
      :modal-concluir-diagnostico-aberto="modalConcluirDiagnosticoAberto"
      :modal-historico-diagnostico-aberto="modalHistoricoDiagnosticoAberto"
      :mensagem-erro-justificativa="mensagemErroJustificativa"
      :modal-lembrete-aberto="modalLembreteAberto"
      :mostrar-modal-alterar-data-limite="mostrarModalAlterarDataLimite"
      :mostrar-modal-reabrir="mostrarModalReabrir"
      :sigla-unidade="subprocesso?.unidade?.sigla ?? ''"
      :tipo-reabertura="tipoReabertura"
      :ultima-data-limite-subprocesso="subprocesso?.ultimaDataLimiteSubprocesso ? analisarData(subprocesso.ultimaDataLimiteSubprocesso) : null"
      @confirmar-alteracao-data="confirmarAlteracaoDataLimite"
      @confirmar-concluir-diagnostico="confirmarConcluirDiagnostico"
      @confirmar-enviar-lembrete="enviarLembreteConfirmado"
      @confirmar-reabertura="confirmarReabertura"
      @fechar-modal-data="fecharModalAlterarDataLimite"
      @update:justificativa-reabertura="justificativaReabertura = $event"
      @update:modal-concluir-diagnostico-aberto="modalConcluirDiagnosticoAberto = $event"
      @update:modal-historico-diagnostico-aberto="modalHistoricoDiagnosticoAberto = $event"
      @update:modal-lembrete-aberto="modalLembreteAberto = $event"
      @update:mostrar-modal-reabrir="mostrarModalReabrir = $event"
  />
</template>

<script lang="ts" setup>
import {BButton, BSpinner} from "bootstrap-vue-next";
import {useRouter} from "vue-router";
import LayoutPadrao from "@/components/layout/LayoutPadrao.vue";
import SubprocessoDiagnosticoPainel from "@/components/diagnostico/SubprocessoDiagnosticoPainel.vue";
import SubprocessoCards from "@/components/processo/SubprocessoCards.vue";
import SubprocessoFluxoModais from "@/components/processo/SubprocessoFluxoModais.vue";
import SubprocessoMovimentacoes from "@/components/processo/SubprocessoMovimentacoes.vue";
import SubprocessoResumoHeader from "@/components/processo/SubprocessoResumoHeader.vue";
import Alerta from "@/components/comum/Alerta.vue";
import CarregamentoPagina from "@/components/comum/CarregamentoPagina.vue";
import {useToast} from "@/composables/useToast";
import {useFluxoDiagnostico} from "@/composables/useFluxoDiagnostico";
import {listarAnalisesDiagnostico} from "@/services/analiseService";
import {normalizarErro} from "@/utils/apiError/normalizer";
import type {Analise} from "@/types/tipos";
import {useSubprocessoTela} from "@/composables/useSubprocessoTela";
import {computed, ref} from "vue";
import {useAsyncAction} from "@/composables/useAsyncAction";

const props = defineProps<{ codProcesso: number; siglaUnidade: string; codSubprocesso?: number }>();

const tela = useSubprocessoTela(props);
const router = useRouter();

const {
  erroIntegracaoContexto,
  limparErroIntegracao,
  notificacao,
  clear,
  notify,
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

const ehDiagnostico = computed(() => subprocesso?.value?.tipoProcesso === TipoProcesso.DIAGNOSTICO);
const ehServidorPuro = computed(() => {
  const permissoes = subprocesso.value?.permissoes;
  if (!permissoes) {
    return false;
  }
  return !permissoes.podeCriarConsenso
      && !permissoes.podeConcluirDiagnostico
      && !permissoes.podeValidarDiagnostico
      && !permissoes.podeDevolverDiagnostico
      && !permissoes.podeHomologarDiagnostico;
});
const {registrarPendente} = useToast();
const acaoDiagnostico = useAsyncAction();
const modalConcluirDiagnosticoAberto = ref(false);
const erroConcluirDiagnostico = ref('');
const modalHistoricoDiagnosticoAberto = ref(false);
const carregandoHistoricoDiagnostico = ref(false);
const historicoAnalisesDiagnostico = ref<Analise[]>([]);
const {
  concluindo: concluindoDiagnostico,
  erroConcluir,
  erroValidacaoConcluir,
  validarConclusaoDiagnostico,
  concluirDiagnostico,
} = useFluxoDiagnostico(() => codigoSubprocesso.value ?? 0);
const mostrarHistoricoAnaliseDiagnostico = computed(() =>
    !!subprocesso.value?.permissoes?.mostrarHistoricoAnaliseDiagnostico,
);
const mostrarConcluirDiagnosticoCabecalho = computed(() =>
    !!subprocesso.value?.permissoes?.podeConcluirDiagnostico,
);
const habilitarConcluirDiagnosticoCabecalho = computed(() =>
    !!subprocesso.value?.permissoes?.habilitarConcluirDiagnostico,
);

async function abrirHistoricoAnaliseDiagnostico() {
  if (!codigoSubprocesso.value) return;
  carregandoHistoricoDiagnostico.value = true;
  modalHistoricoDiagnosticoAberto.value = true;
  try {
    const resultado = await acaoDiagnostico.executar(
        () => listarAnalisesDiagnostico(codigoSubprocesso.value!),
        TEXTOS.diagnostico.ERRO_SALVAR,
        {relancarErro: false},
    );
    if (resultado === undefined) {
      notify(TEXTOS.diagnostico.ERRO_SALVAR, 'danger', true);
    } else {
      historicoAnalisesDiagnostico.value = resultado;
    }
  } finally {
    carregandoHistoricoDiagnostico.value = false;
  }
}

async function abrirModalConcluirDiagnostico() {
  erroConcluirDiagnostico.value = '';
  await acaoDiagnostico.executar(
      () => validarConclusaoDiagnostico(),
      erroValidacaoConcluir.value?.message ?? TEXTOS.diagnostico.ERRO_SALVAR,
      {
        relancarErro: false,
        aoSucesso: () => {
          modalConcluirDiagnosticoAberto.value = true;
        },
        aoOcorrerErro: (_erro, causa) => {
          const mensagemErro = normalizarErro(causa).mensagem
              ?? erroValidacaoConcluir.value?.message
              ?? TEXTOS.diagnostico.ERRO_SALVAR;
          erroConcluirDiagnostico.value = mensagemErro;
          notify(mensagemErro, 'danger', true);
        },
      },
  );
}

async function confirmarConcluirDiagnostico() {
  await acaoDiagnostico.executar(
      () => concluirDiagnostico(),
      erroConcluir.value?.message ?? TEXTOS.diagnostico.ERRO_SALVAR,
      {
        relancarErro: false,
        aoSucesso: async () => {
          modalConcluirDiagnosticoAberto.value = false;
          registrarPendente(TEXTOS.diagnostico.SUCESSO_DIAGNOSTICO_CONCLUIDO);
          await router.push({name: 'Painel'});
        },
        aoOcorrerErro: (_erro, causa) => {
          erroConcluirDiagnostico.value = normalizarErro(causa).mensagem
              ?? erroConcluir.value?.message
              ?? TEXTOS.diagnostico.ERRO_SALVAR;
        },
      },
  );
}

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
