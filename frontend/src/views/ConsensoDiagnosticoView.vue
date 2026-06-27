<template>
  <LayoutPadrao>
    <CarregamentoPagina v-if="carregando"/>

    <template v-else>
      <div :class="{'cursor-salvando': salvandoAutomaticamente}">
      <PageHeader
          :subtitle="subtituloServidor"
          :title="TEXTOS.diagnostico.TITULO_CONSENSO"
      >
        <template #alerta>
          <AppAlert
              v-if="erroMensagem"
              :chave="erroMensagemChave"
              :mensagem="erroMensagem"
              variante="danger"
              @dismissed="erroMensagem = ''"
          />

          <AppAlert
              v-if="ehConsensoAprovado"
              :dispensavel="false"
              data-testid="alert-consenso-aprovado"
              mensagem="A avaliação de consenso já foi aprovada."
              variante="success"
          />
        </template>
        <template #actions>
          <ConsensoDiagnosticoAcoes
              :aprovando="aprovando"
              :concluindo-avaliacao="concluindoAvaliacao"
              :habilitar-aprovar-consenso="habilitarAprovarConsenso"
              :habilitar-concluir-avaliacao="habilitarConcluirAvaliacao"
              :pode-aprovar-consenso="podeAprovarConsenso"
              :pode-concluir-avaliacao="podeConcluirAvaliacao"
              :servidor-eh-usuario-logado="servidorEhUsuarioLogado"
              @aprovar-consenso="abrirModalAprovarConsenso"
              @concluir-avaliacao="confirmarConcluirAvaliacao"
              @voltar="voltar"
          />
        </template>
      </PageHeader>

      <ConsensoDiagnosticoTabela
          :competencias="competenciasDetalhadasComDescricao"
          :eh-consenso-aprovado="ehConsensoAprovado"
          :habilitar-concluir-avaliacao="habilitarConcluirAvaliacao"
          :pode-editar="podeEditar"
          @atualizar-nota="atualizarNotaDetalhada"
      />
      </div>

      <DiagnosticoFluxoModais
          :concluindo="concluindoAvaliacao"
          :modal-concluir-aberto="modalConcluirAberto"
          :titulo-concluir="TEXTOS.diagnostico.MODAL_CONCLUIR_CONSENSO_TITULO"
          :mensagem-concluir="TEXTOS.diagnostico.MODAL_CONCLUIR_CONSENSO_MENSAGEM"
          :botao-concluir="TEXTOS.diagnostico.BTN_CONCLUIR_CONSENSO"
          test-id-confirmar-concluir="btn-confirmar-concluir"
          :aprovando-consenso="aprovando"
          :modal-aprovar-consenso-aberto="modalAprovarConsensoAberto"
          :erro-aprovar-consenso="erroAprovar ? erroAprovar.message : null"
          test-id-confirmar-aprovar-consenso="btn-confirmar-aprovar-consenso"
          @confirmar-concluir="confirmarConcluir"
          @update:modal-concluir-aberto="modalConcluirAberto = $event"
          @confirmar-aprovar-consenso="confirmarAprovarConsenso"
          @update:modal-aprovar-consenso-aberto="modalAprovarConsensoAberto = $event"
      />
    </template>
  </LayoutPadrao>
</template>

<script lang="ts" setup>
import {computed, ref} from 'vue';
import {useRouter} from 'vue-router';
import LayoutPadrao from '@/components/layout/LayoutPadrao.vue';
import PageHeader from '@/components/layout/PageHeader.vue';
import CarregamentoPagina from '@/components/comum/CarregamentoPagina.vue';
import AppAlert from '@/components/comum/AppAlert.vue';
import ConsensoDiagnosticoAcoes from '@/components/diagnostico/ConsensoDiagnosticoAcoes.vue';
import ConsensoDiagnosticoTabela from '@/components/diagnostico/ConsensoDiagnosticoTabela.vue';
import DiagnosticoFluxoModais from '@/components/diagnostico/DiagnosticoFluxoModais.vue';
import {useToast} from '@/composables/useToast';
import {useDiagnosticoContexto} from '@/composables/useDiagnosticoContexto';
import {useConsensoDiagnostico} from '@/composables/useConsensoDiagnostico';
import {TEXTOS} from '@/constants/textos';
import {usePerfilStore} from '@/stores/perfil';
import type {ConsensoCompetenciaDetalhada} from '@/types/diagnostico-competencias';

const props = defineProps<{
  codSubprocesso: number;
  siglaUnidade: string;
  servidorTitulo: string;
  servidorNome?: string;
}>();

const router = useRouter();
const perfilStore = usePerfilStore();
const {registrarPendente} = useToast();
const servidorEhUsuarioLogado = computed(() =>
  String(props.servidorTitulo) === String(perfilStore.usuarioCodigo ?? ''),
);

const {data: contexto} = useDiagnosticoContexto(props.codSubprocesso);
const {
  query,
  competenciasLocais,
  podeEditar,
  podeConcluirAvaliacao,
  habilitarConcluirAvaliacao,
  podeAprovarConsenso,
  habilitarAprovarConsenso,
  ehConsensoAprovado,
  carregando,
  salvandoAutomaticamente,
  aprovando,
  erroConcluir,
  erroAprovar,
  atualizarNotaDetalhada,
  salvarConsensoAgora,
  concluirAvaliacao,
  aprovarConsenso,
} = useConsensoDiagnostico(props.codSubprocesso, props.servidorTitulo);
const nomeServidorQuery = computed(() => query.data.value?.servidorNome ?? null);
const nomeServidorSubtitulo = computed(() =>
  servidorEhUsuarioLogado.value
    ? (perfilStore.usuarioNome ?? nomeServidorQuery.value ?? props.servidorNome ?? props.servidorTitulo)
    : (nomeServidorQuery.value ?? props.servidorNome ?? props.servidorTitulo),
);
const subtituloServidor = computed(() => `${nomeServidorSubtitulo.value} - ${props.servidorTitulo}`);

// « Alertas »
const erroMensagem = ref('');
const erroMensagemChave = ref(0);
const concluindoAvaliacao = ref(false);

function exibirErro(mensagem: string) {
  erroMensagemChave.value += 1;
  erroMensagem.value = mensagem;
}

function voltar() {
  void router.back();
}

const modalAprovarConsensoAberto = ref(false);

function abrirModalAprovarConsenso() {
  modalAprovarConsensoAberto.value = true;
}

async function confirmarAprovarConsenso() {
  try {
    await aprovarConsenso();
    modalAprovarConsensoAberto.value = false;
    registrarPendente(TEXTOS.diagnostico.SUCESSO_CONSENSO_APROVADO);
    if (contexto.value?.processoCodigo) {
      await router.push({
        name: 'Subprocesso',
        params: {
          codProcesso: String(contexto.value.processoCodigo),
          siglaUnidade: props.siglaUnidade,
        },
        query: {
          codSubprocesso: String(props.codSubprocesso),
        },
      });
      return;
    }
    void router.back();
  } catch {
    exibirErro(erroAprovar.value?.message ?? TEXTOS.diagnostico.ERRO_SALVAR);
  }
}

function consensoCompleto(item: ConsensoCompetenciaDetalhada): boolean {
  return item.chefiaImportancia !== null
    && item.chefiaDominio !== null
    && item.consensoImportancia !== null
    && item.consensoDominio !== null;
}

const modalConcluirAberto = ref(false);

function confirmarConcluirAvaliacao() {
  if (competenciasLocais.value.some((item) => !consensoCompleto(item))) {
    exibirErro(TEXTOS.diagnostico.ERRO_PREENCHIMENTO_CONSENSO_INCOMPLETO);
    return;
  }
  modalConcluirAberto.value = true;
}

async function confirmarConcluir() {
  try {
    await salvarConsensoAgora();
    concluindoAvaliacao.value = true;
    await concluirAvaliacao();
    modalConcluirAberto.value = false;
    registrarPendente(TEXTOS.diagnostico.SUCESSO_CONSENSO_CRIADO);
    if (contexto.value?.processoCodigo) {
      await router.push({
        name: 'Subprocesso',
        params: {
          codProcesso: String(contexto.value.processoCodigo),
          siglaUnidade: props.siglaUnidade,
        },
        query: {
          codSubprocesso: String(props.codSubprocesso),
        },
      });
      return;
    }
    void router.back();
  } catch {
    exibirErro(erroConcluir.value?.message ?? TEXTOS.diagnostico.ERRO_SALVAR);
  } finally {
    concluindoAvaliacao.value = false;
  }
}

const competenciasDetalhadasComDescricao = computed(() => {
  return competenciasLocais.value.map((c) => ({
    ...c,
    descricao: c.competenciaDescricao ?? '',
  }));
});
</script>

<style scoped>
.cursor-salvando,
.cursor-salvando * {
  cursor: wait !important;
}
</style>
