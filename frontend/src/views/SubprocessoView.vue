<template>
  <BContainer class="mt-4">
    <div v-if="subprocesso">
      <SubprocessoHeader
          :pode-alterar-data-limite="subprocesso.permissoes.podeAlterarDataLimite"
          :pode-reabrir-cadastro="subprocesso.permissoes.podeReabrirCadastro"
          :pode-reabrir-revisao="subprocesso.permissoes.podeReabrirRevisao"
          :pode-enviar-lembrete="subprocesso.permissoes.podeEnviarLembrete"
          :processo-descricao="subprocesso.processoDescricao || ''"
          :responsavel-email="subprocesso.responsavel?.email || ''"
          :responsavel-nome="subprocesso.responsavel?.nome || ''"
          :responsavel-ramal="subprocesso.responsavel?.ramal || ''"
          :situacao="subprocesso.situacaoLabel"
          :titular-email="subprocesso.titular?.email || ''"
          :titular-nome="subprocesso.titular?.nome || ''"
          :titular-ramal="subprocesso.titular?.ramal || ''"
          :unidade-nome="subprocesso.unidade.nome"
          :unidade-sigla="subprocesso.unidade.sigla"
          @alterar-data-limite="abrirModalAlterarDataLimite"
          @reabrir-cadastro="abrirModalReabrirCadastro"
          @reabrir-revisao="abrirModalReabrirRevisao"
          @enviar-lembrete="confirmarEnviarLembrete"
      />

      <SubprocessoCards
          v-if="codSubprocesso"
          :cod-processo="props.codProcesso"
          :cod-subprocesso="codSubprocesso"
          :mapa="mapa"
          :permissoes="subprocesso.permissoes || { podeEditarMapa: true, podeVisualizarMapa: true, podeVisualizarDiagnostico: true, podeAlterarDataLimite: false, podeDisponibilizarCadastro: false, podeDevolverCadastro: false, podeAceitarCadastro: false, podeVisualizarImpacto: false, podeVerPagina: true, podeRealizarAutoavaliacao: false, podeDisponibilizarMapa: false }"
          :sigla-unidade="props.siglaUnidade"
          :situacao="subprocesso.situacao"
          :tipo-processo="subprocesso.tipoProcesso || TipoProcesso.MAPEAMENTO"
      />

      <TabelaMovimentacoes :movimentacoes="movimentacoes"/>
    </div>
    <div v-else-if="subprocessosStore.lastError" class="text-center py-5">
      <BAlert :model-value="true" variant="danger">
        {{ subprocessosStore.lastError.message || "Erro ao carregar subprocesso." }}
      </BAlert>
    </div>
    <div v-else class="text-center py-5">
      <BSpinner label="Carregando informações da unidade..." variant="primary" />
      <p class="mt-2 text-muted">Carregando informações da unidade...</p>
    </div>
  </BContainer>

  <SubprocessoModal
      :data-limite-atual="dataLimite"
      :etapa-atual="subprocesso?.etapaAtual || null"
      :mostrar-modal="mostrarModalAlterarDataLimite"
      @fechar-modal="fecharModalAlterarDataLimite"
      @confirmar-alteracao="confirmarAlteracaoDataLimite"
  />

  <!-- Modal para reabrir cadastro/revisão -->
  <BModal
      v-model="mostrarModalReabrir"
      :title="tipoReabertura === 'cadastro' ? 'Reabrir cadastro' : 'Reabrir Revisão'"
      centered
      @ok="confirmarReabertura"
  >
    <p>Informe a justificativa para reabrir o {{ tipoReabertura === 'cadastro' ? 'cadastro' : 'revisão de cadastro' }}:</p>
    <BFormTextarea
        v-model="justificativaReabertura"
        data-testid="inp-justificativa-reabrir"
        placeholder="Justificativa obrigatória..."
        rows="3"
    />
    <template #footer>
      <BButton variant="secondary" @click="fecharModalReabrir">Cancelar</BButton>
      <BButton 
          :disabled="!justificativaReabertura.trim()"
          data-testid="btn-confirmar-reabrir"
          variant="warning" 
          @click="confirmarReabertura"
      >
        Confirmar Reabertura
      </BButton>
    </template>
  </BModal>
</template>

<script lang="ts" setup>
import {BAlert, BButton, BContainer, BFormTextarea, BModal, BSpinner} from "bootstrap-vue-next";
import {computed, onMounted, ref} from "vue";
import SubprocessoCards from "@/components/SubprocessoCards.vue";
import SubprocessoHeader from "@/components/SubprocessoHeader.vue";
import SubprocessoModal from "@/components/SubprocessoModal.vue";
import TabelaMovimentacoes from "@/components/TabelaMovimentacoes.vue";
import {useMapasStore} from "@/stores/mapas";
import {useFeedbackStore} from "@/stores/feedback";
import {enviarLembrete, reabrirCadastro, reabrirRevisaoCadastro} from "@/services/processoService";

import {useSubprocessosStore} from "@/stores/subprocessos";
import {type Movimentacao, type SubprocessoDetalhe, TipoProcesso,} from "@/types/tipos";

const props = defineProps<{ codProcesso: number; siglaUnidade: string }>();

const subprocessosStore = useSubprocessosStore();

const mapaStore = useMapasStore();
const feedbackStore = useFeedbackStore();

const mostrarModalAlterarDataLimite = ref(false);
const mostrarModalReabrir = ref(false);
const tipoReabertura = ref<'cadastro' | 'revisao'>('cadastro');
const justificativaReabertura = ref('');
const codSubprocesso = ref<number | null>(null);

const subprocesso = computed<SubprocessoDetalhe | null>(
    () => subprocessosStore.subprocessoDetalhe,
);
const mapa = computed(() => mapaStore.mapaCompleto);
const movimentacoes = computed<Movimentacao[]>(
    () => subprocesso.value?.movimentacoes || [],
);
const dataLimite = computed(() =>
    subprocesso.value?.prazoEtapaAtual
        ? new Date(subprocesso.value.prazoEtapaAtual)
        : new Date(),
);

onMounted(async () => {
  const id = await subprocessosStore.buscarSubprocessoPorProcessoEUnidade(
      props.codProcesso,
      props.siglaUnidade,
  );

  if (id) {
    codSubprocesso.value = id;
    await subprocessosStore.buscarSubprocessoDetalhe(id);
    await mapaStore.buscarMapaCompleto(id);
  }
});

function abrirModalAlterarDataLimite() {
  if (subprocesso.value?.permissoes.podeAlterarDataLimite) {
    mostrarModalAlterarDataLimite.value = true;
  } else {
    feedbackStore.show("Ação não permitida", "Você não tem permissão para alterar a data limite.", "danger");
  }
}

function fecharModalAlterarDataLimite() {
  mostrarModalAlterarDataLimite.value = false;
}

async function confirmarAlteracaoDataLimite(novaData: string) {
  if (!novaData || !subprocesso.value) {
    return;
  }

  try {
    await subprocessosStore.alterarDataLimiteSubprocesso(
        subprocesso.value.unidade.codigo,
        {novaData},
    );
    fecharModalAlterarDataLimite();
    feedbackStore.show("Data limite alterada", "A data limite foi alterada com sucesso!", "success");
  } catch {
    feedbackStore.show("Erro ao alterar data limite", "Não foi possível alterar a data limite.", "danger");
  }
}

// CDU-32/33: Reabrir cadastro/revisão
function abrirModalReabrirCadastro() {
  tipoReabertura.value = 'cadastro';
  justificativaReabertura.value = '';
  mostrarModalReabrir.value = true;
}

function abrirModalReabrirRevisao() {
  tipoReabertura.value = 'revisao';
  justificativaReabertura.value = '';
  mostrarModalReabrir.value = true;
}

function fecharModalReabrir() {
  mostrarModalReabrir.value = false;
  justificativaReabertura.value = '';
}

async function confirmarReabertura() {
  if (!codSubprocesso.value || !justificativaReabertura.value.trim()) {
    feedbackStore.show("Erro", "Justificativa é obrigatória.", "danger");
    return;
  }

  try {
    if (tipoReabertura.value === 'cadastro') {
      await reabrirCadastro(codSubprocesso.value, justificativaReabertura.value);
      feedbackStore.show("Cadastro reaberto", "O cadastro foi reaberto com sucesso.", "success");
    } else {
      await reabrirRevisaoCadastro(codSubprocesso.value, justificativaReabertura.value);
      feedbackStore.show("Revisão reaberta", "A revisão de cadastro foi reaberta com sucesso.", "success");
    }
    fecharModalReabrir();
    // Recarregar dados do subprocesso
    await subprocessosStore.buscarSubprocessoDetalhe(codSubprocesso.value);
  } catch {
    feedbackStore.show("Erro", "Não foi possível reabrir. Tente novamente.", "danger");
  }
}

// CDU-34: Enviar lembrete
async function confirmarEnviarLembrete() {
  if (!subprocesso.value) return;

  try {
    await enviarLembrete(props.codProcesso, subprocesso.value.unidade.codigo);
    feedbackStore.show("Lembrete enviado", "O lembrete de prazo foi enviado com sucesso.", "success");
  } catch {
    feedbackStore.show("Erro", "Não foi possível enviar o lembrete.", "danger");
  }
}
</script>
