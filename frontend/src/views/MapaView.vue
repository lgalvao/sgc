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
          :loading-impacto="loadingImpacto"
          :loading-sugestoes-visualizacao="loadingSugestoesVisualizacao"
          :mostrar-acao-principal-mapa="mostrarAcaoPrincipalMapa"
          :mostrar-apresentar-sugestoes="mostrarApresentarSugestoes"
          :mostrar-devolver-mapa="mostrarDevolverMapa"
          :mostrar-disponibilizar-mapa="mostrarDisponibilizarMapa"
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
          @ver-sugestoes="verSugestoes"
      />

      <BAlert
          v-if="erroMapaExibido"
          :key="erroMapaExibido"
          :model-value="true"
          dismissible
          no-fade
          show
          variant="danger"
          @dismissed="limparErroMapa"
      >
        {{ erroMapaExibido }}
      </BAlert>

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
                @remover-atividade="(competenciaId, codAtividade) => removerAtividadeAssociada(competenciaId, codAtividade)"
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
          :impactos="impactos ?? null"
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
          :ultima-data-limite-subprocesso="subprocesso?.ultimaDataLimiteSubprocesso"
          @disponibilizar="disponibilizarMapa"
          @confirmar-aceitacao="confirmarAceitacao"
          @confirmar-devolucao="handleConfirmarDevolucao"
          @confirmar-exclusao-competencia="confirmarExclusaoCompetencia"
          @confirmar-sugestoes="handleConfirmarSugestoes"
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
import {BAlert, BButton} from "bootstrap-vue-next";
import LayoutPadrao from '@/components/layout/LayoutPadrao.vue';
import EmptyState from "@/components/comum/EmptyState.vue";
import MapaAcoesHeader from "@/components/mapa/MapaAcoesHeader.vue";
import MapaModaisRoot from "@/components/mapa/modais/MapaModaisRoot.vue";
import CompetenciaCard from "@/components/mapa/CompetenciaCard.vue";
import MapaSomenteLeitura from "@/components/mapa/MapaSomenteLeitura.vue";
import CarregamentoPagina from "@/components/comum/CarregamentoPagina.vue";
import {computed, onMounted, reactive, ref, toRefs, unref} from "vue";
import {useRouter} from "vue-router";
import {useAcesso} from "@/composables/acesso";
import {useFluxoMapa} from "@/composables/useFluxoMapa";
import {useFormErrors} from '@/composables/useFormErrors';
import {useMapaCompetenciasMutacoes} from "@/composables/useMapaCompetenciasMutacoes";
import {useImpactoMapaModal} from "@/composables/useImpactoMapaModal";
import {useMapas} from "@/composables/useMapas";
import {useNotification} from "@/composables/useNotification";
import {useToastStore} from "@/stores/toast";
import {useSubprocessoStore} from "@/stores/subprocesso";
import {usePerfilStore} from "@/stores/perfil";
import {useInvalidacaoNavegacao} from "@/composables/useInvalidacaoNavegacao";
import {listarAnalisesValidacao} from "@/services/analiseService";
import {useValidacaoFormulario} from "@/composables/useValidacaoFormulario";
import {useMapaOrquestracao} from "@/composables/useMapaOrquestracao";
import {useMapaSugestoes} from "@/composables/useMapaSugestoes";
import {useMapaAnaliseFluxo} from "@/views/mapaAnaliseFluxo";
import {useMapaDisponibilizacao} from "@/views/mapaDisponibilizacao";
import {normalizarErro} from "@/utils/apiError";
import type {Analise, MapaCompleto,} from "@/types/tipos";
import {Perfil} from "@/types/tipos";
import {TEXTOS} from "@/constants/textos";

const props = defineProps<{ codProcesso: number | string; sigla: string; codSubprocesso?: number }>();
const router = useRouter();
const fluxoMapa = useFluxoMapa();
const carregandoFluxoMapa = computed(() => unref(fluxoMapa.carregando) ?? false);
const {notify} = useNotification();
const toastStore = useToastStore();
const subprocessoStore = useSubprocessoStore();
const perfilStore = usePerfilStore();
const {invalidarCachesSubprocesso} = useInvalidacaoNavegacao();
const subprocesso = computed(() => subprocessoStore.contextoEdicao?.detalhes ?? null);

const {
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
  acaoPrincipalMapa
} = useAcesso(subprocesso);

const usarMenuAcoesMapa = computed(() => {
  return mostrarApresentarSugestoes.value
      || mostrarValidarMapa.value
      || mostrarDevolverMapa.value
      || Boolean(acaoPrincipalMapa.value?.mostrar)
      || mostrarDisponibilizarMapa.value;
});
const esconderEdicaoMapaParaAdmin = computed(() =>
  perfilStore.perfilSelecionado === Perfil.ADMIN
  && podeEditarMapa.value
  && !habilitarEditarMapa.value
);
const modoSomenteLeitura = computed(() => !podeEditarMapa.value || esconderEdicaoMapaParaAdmin.value);
const mostrarAcaoPrincipalMapa = computed(() => Boolean(acaoPrincipalMapa.value?.mostrar));
const habilitarAcaoPrincipalMapa = computed(() => acaoPrincipalMapa.value?.habilitar ?? false);
const rotuloAcaoPrincipalMapa = computed(() => acaoPrincipalMapa.value?.rotuloBotao ?? TEXTOS.mapa.LABEL_HOMOLOGAR);

const {
  carregandoInicial,
  codigoSubprocesso,
  unidade,
  carregarContextoInicial,
} = useMapaOrquestracao(props);

const mapasStore = useMapas(codigoSubprocesso);
const {impactoMapa: impactos, erro: erroMapa} = mapasStore;

const atividades = computed(() => mapasStore.mapaCompleto.value?.atividades ?? []);
const competencias = computed(() => mapasStore.mapaCompleto.value?.competencias ?? []);
const mapaSomenteLeitura = computed(() => mapasStore.mapaCompleto.value);
const estadoModais = reactive({
  mostrarModalAceitar: false,
  mostrarModalValidar: false,
  mostrarModalDevolucao: false,
  mostrarModalHistorico: false,
  mostrarModalDisponibilizar: false,
});
const {
  mostrarModalAceitar,
  mostrarModalValidar,
  mostrarModalDevolucao,
  mostrarModalHistorico,
  mostrarModalDisponibilizar,
} = toRefs(estadoModais);
const observacaoDevolucao = ref("");
const analisesCadastro = ref<Analise[]>([]);
const historicoAnalise = computed(() => analisesCadastro.value);

const {
  validarSubmissao,
  resetarValidacao,
  deveExibirErro,
  focarPrimeiroErroInvalido
} = useValidacaoFormulario();

const {
  sugestoes,
  sugestoesVisualizacao,
  loadingSugestoesVisualizacao,
  loadingSugestoesEnvio,
  mostrarModalSugestoes,
  mostrarModalVerSugestoes,
  verSugestoes,
  fecharModalVerSugestoes,
  abrirModalSugestoes,
  handleConfirmarSugestoes,
} = useMapaSugestoes({
  codigoSubprocesso,
  notify,
  concluirAcaoPainel,
  validarSubmissao,
  focarPrimeiroErroInvalido,
  resetarValidacao
});

const mensagemErroDevolucao = computed(() => {
  return deveExibirErro(!observacaoDevolucao.value.trim()) ? "A justificativa é obrigatória para a devolução." : "";
});
const mensagemErroSugestoes = computed(() => {
  return deveExibirErro(!sugestoes.value.trim()) ? "As sugestões são obrigatórias." : "";
});

async function concluirAcaoPainel(mensagem: string, fecharModal: () => void) {
  fecharModal();
  toastStore.setPending(mensagem);
  invalidarCachesSubprocesso({incluirPainel: true, incluirProcesso: true});
  await router.push({name: "Painel"});
}

const {
  mostrarModalImpacto,
  loadingImpacto,
  abrirModalImpacto,
  fecharModalImpacto,
} = useImpactoMapaModal(codigoSubprocesso, (codigo) => mapasStore.buscarImpactoMapa(codigo));

const {
  abrirModalAceitar,
  fecharModalAceitar,
  abrirModalValidar,
  abrirModalDevolucao,
  confirmarValidacao,
  confirmarAceitacao,
  handleConfirmarDevolucao,
  fecharModalHistorico,
  verHistorico,
} = useMapaAnaliseFluxo({
  codigoSubprocesso,
  acaoPrincipalMapa,
  mostrarModalAceitar,
  mostrarModalValidar,
  mostrarModalDevolucao,
  mostrarModalHistorico,
  observacaoDevolucao,
  analisesCadastro,
  resetarValidacao,
  validarSubmissao,
  focarPrimeiroErroInvalido,
  concluirAcaoPainel,
  notify,
  listarAnalisesCadastro: listarAnalisesValidacao,
  validarMapa: fluxoMapa.validarMapa,
  homologarMapa: fluxoMapa.homologarMapa,
  aceitarMapa: fluxoMapa.aceitarMapa,
  devolverMapa: fluxoMapa.devolverMapa,
});

async function executarComSubprocesso(
    callback: (id: number) => Promise<void>
) {
  const codSubp = codigoSubprocesso.value;
  if (!codSubp) throw new Error("Invariante violada: codigoSubprocesso não carregado");
  await callback(codSubp);
}

const contextoEdicaoAtual = computed(() => subprocessoStore.contextoEdicao);
const sincronizarMapaStore = (mapaAtualizado: MapaCompleto | null | undefined) =>
    sincronizarMapaContexto(mapaAtualizado, codigoSubprocesso.value, mapasStore.definirMapaCompleto, contextoEdicaoAtual);

onMounted(async () => {
  const sucesso = await carregarContextoInicial();
  if (!sucesso) {
    if (subprocessoStore.erroIntegracaoContexto) {
      notify(subprocessoStore.erroIntegracaoContexto.mensagem, 'danger');
    } else {
      notify('Falha grave ao resolver subprocesso para o mapa. A ocorrência deve ser auditada.', 'danger');
    }
  }
});

const codigosAtividadesAssociadas = computed(() => {
  return new Set(
      competencias.value.flatMap((competencia) =>
          competencia.atividades.map((atividade) => atividade.codigo)
      )
  );
});
const atividadesSemCompetencia = computed(() => {
  if (atividades.value.length === 0) {
    return [];
  }

  return atividades.value.filter((atividade) => !codigosAtividadesAssociadas.value.has(atividade.codigo));
});

const existeCompetenciaSemAtividade = computed(() => {
  return competencias.value.some((competencia) => competencia.atividades.length === 0);
});

const {errors: fieldErrors, setFromErroNormalizado, clearErrors} = useFormErrors([
  'descricao',
  'atividades',
  'atividadesCodigos',
  'dataLimite',
  'observacoes',
  'generic'
]);

function sincronizarErrosAtividades() {
  if (fieldErrors.value.atividadesCodigos) {
    fieldErrors.value.atividades = fieldErrors.value.atividadesCodigos;
  }
}

function aplicarErroNormalizado(error: ReturnType<typeof normalizarErro> | null) {
  setFromErroNormalizado(error);
  sincronizarErrosAtividades();
}

const {
  competenciaSendoEditada,
  mostrarModalCriarNovaCompetencia,
  mostrarModalExcluirCompetencia,
  competenciaParaExcluir,
  loadingCompetencia,
  loadingExclusao,
  abrirModalCriarLimpo,
  fecharModalCriarNovaCompetencia,
  iniciarEdicaoCompetencia,
  adicionarCompetenciaEFecharModal,
  excluirCompetencia,
  confirmarExclusaoCompetencia,
  removerAtividadeAssociada,
} = useMapaCompetenciasMutacoes({
  codigoSubprocesso,
  competencias,
  fluxoMapa,
  notify,
  clearErrors,
  aplicarErroNormalizado,
  sincronizarMapa: sincronizarMapaStore,
});

const {
  erroValidacaoMapa,
  loadingDisponibilizacao,
  notificacaoDisponibilizacao,
  abrirModalDisponibilizar,
  fecharModalDisponibilizar,
  disponibilizarMapa,
  limparErroMapa,
  sincronizarMapaContexto,
} = useMapaDisponibilizacao({
  competencias,
  existeCompetenciaSemAtividade,
  atividadesSemCompetencia,
  mostrarModalDisponibilizar,
  clearErrors,
  executarComSubprocesso,
  disponibilizarMapaFluxo: fluxoMapa.disponibilizarMapa,
  concluirAcaoPainel,
  aplicarErroNormalizado,
});
const erroMapaExibido = computed(() => erroValidacaoMapa.value || erroMapa.value);

</script>
