<template>
  <BContainer class="mt-4">
    <div class="unidade-cabecalho w-100">
      <span class="unidade-sigla">{{ siglaUnidade }}</span>
      <span class="unidade-nome">{{ nomeUnidade }}</span>
    </div>

    <div class="d-flex justify-content-between align-items-center mb-3">
      <h2 class="mb-0">
        Atividades e conhecimentos
      </h2>
      <div class="d-flex gap-2">
        <BButton
            v-if="podeVerImpacto"
            data-testid="cad-atividades__btn-impactos-mapa"
            variant="outline-secondary"
            @click="abrirModalImpacto"
        >
          <i class="bi bi-arrow-right-circle me-2"/>{{ isRevisao ? 'Ver impactos' : 'Impacto no mapa' }}
        </BButton>
        <BButton
            data-testid="btn-vis-atividades-historico"
            variant="outline-info"
            @click="abrirModalHistoricoAnalise"
        >
          Histórico de análise
        </BButton>
        <BButton
            data-testid="btn-acao-devolver"
            title="Devolver para ajustes"
            variant="secondary"
            @click="devolverCadastro"
        >
          Devolver para ajustes
        </BButton>
        <BButton
            data-testid="btn-acao-analisar-principal"
            title="Validar"
            variant="success"
            @click="validarCadastro"
        >
          {{ perfilSelecionado === Perfil.ADMIN ? 'Homologar' : 'Registrar aceite' }}
        </BButton>
      </div>
    </div>

    <!-- Lista de atividades -->
    <BCard
        v-for="(atividade) in atividades"
        :key="atividade.codigo"
        class="mb-3 atividade-card"
        no-body
    >
      <BCardBody class="py-2">
        <div
            class="card-title d-flex align-items-center atividade-edicao-row position-relative group-atividade atividade-hover-row atividade-titulo-card"
        >
          <strong
              class="atividade-descricao"
              data-testid="txt-atividade-descricao"
          >{{ atividade.descricao }}</strong>
        </div>

        <!-- Conhecimentos da atividade -->
        <div class="mt-3 ms-3">
          <div
              v-for="(conhecimento) in atividade.conhecimentos"
              :key="conhecimento.codigo"
              class="d-flex align-items-center mb-2 group-conhecimento position-relative conhecimento-hover-row"
          >
            <span data-testid="txt-conhecimento-descricao">{{ conhecimento.descricao }}</span>
          </div>
        </div>
      </BCardBody>
    </BCard>

    <!-- Modal de Impacto no Mapa -->
    <ImpactoMapaModal
        v-if="codSubprocesso"
        :impacto="impactoMapa"
        :loading="loadingImpacto"
        :mostrar="mostrarModalImpacto"
        @fechar="fecharModalImpacto"
    />

    <!-- Modal de Histórico de Análise -->
    <HistoricoAnaliseModal
        :historico="historicoAnalises"
        :mostrar="mostrarModalHistoricoAnalise"
        @fechar="fecharModalHistoricoAnalise"
    />

    <!-- Modal de Validação -->
    <BModal
        v-model="mostrarModalValidar"
        :fade="false"
        :title="isHomologacao ? 'Homologação do cadastro de atividades e conhecimentos' : (isRevisao ? 'Aceite da revisão do cadastro' : 'Validação do cadastro')"
        centered
        hide-footer
    >
      <p>{{
          isHomologacao ? 'Confirma a homologação do cadastro de atividades e conhecimentos?' : (isRevisao ? 'Confirma o aceite da revisão do cadastro de atividades?' : 'Confirma o aceite do cadastro de atividades?')
        }}</p>
      <div
          v-if="!isHomologacao"
          class="mb-3"
      >
        <label
            class="form-label"
            for="observacaoValidacao"
        >Observação</label>
        <BFormTextarea
            id="observacaoValidacao"
            v-model="observacaoValidacao"
            data-testid="inp-aceite-cadastro-obs"
            rows="3"
        />
      </div>
      <template #footer>
        <BButton
            variant="secondary"
            @click="fecharModalValidar"
        >
          Cancelar
        </BButton>
        <BButton
            data-testid="btn-aceite-cadastro-confirmar"
            variant="success"
            @click="confirmarValidacao"
        >
          Confirmar
        </BButton>
      </template>
    </BModal>

    <!-- Modal de Devolução -->
    <BModal
        v-model="mostrarModalDevolver"
        :fade="false"
        :title="isRevisao ? 'Devolução da revisão do cadastro' : 'Devolução do cadastro'"
        centered
        hide-footer
    >
      <p>{{
          isRevisao ? 'Confirma a devolução da revisão do cadastro para ajustes?' : 'Confirma a devolução do cadastro para ajustes?'
        }}</p>
      <div class="mb-3">
        <label
            class="form-label"
            for="observacaoDevolucao"
        >Observação</label>
        <BFormTextarea
            id="observacaoDevolucao"
            v-model="observacaoDevolucao"
            data-testid="inp-devolucao-cadastro-obs"
            rows="3"
        />
      </div>
      <template #footer>
        <BButton
            variant="secondary"
            @click="fecharModalDevolver"
        >
          Cancelar
        </BButton>
        <BButton
            data-testid="btn-devolucao-cadastro-confirmar"
            variant="danger"
            @click="confirmarDevolucao"
        >
          Confirmar
        </BButton>
      </template>
    </BModal>
  </BContainer>
</template>

<script lang="ts" setup>
import {BButton, BCard, BCardBody, BContainer, BFormTextarea, BModal,} from "bootstrap-vue-next";
import {computed, onMounted, ref} from "vue";
import {useRouter} from "vue-router";
import HistoricoAnaliseModal from "@/components/HistoricoAnaliseModal.vue";
import ImpactoMapaModal from "@/components/ImpactoMapaModal.vue";
import {useAnalisesStore} from "@/stores/analises";
import {useAtividadesStore} from "@/stores/atividades";
import {usePerfilStore} from "@/stores/perfil";
import {useProcessosStore} from "@/stores/processos";
import {useSubprocessosStore} from "@/stores/subprocessos";
import {useUnidadesStore} from "@/stores/unidades";
import {useMapasStore} from "@/stores/mapas";
import {storeToRefs} from "pinia";
import {
  type AceitarCadastroRequest,
  type Atividade,
  type DevolverCadastroRequest,
  type HomologarCadastroRequest,
  Perfil,
  SituacaoSubprocesso,
  TipoProcesso,
  type Unidade,
} from "@/types/tipos";

const props = defineProps<{
  codProcesso: number | string;
  sigla: string;
}>();

const unidadeId = computed(() => props.sigla);
const codProcesso = computed(() => Number(props.codProcesso));

const atividadesStore = useAtividadesStore();
const unidadesStore = useUnidadesStore();
const processosStore = useProcessosStore();
const subprocessosStore = useSubprocessosStore();
const perfilStore = usePerfilStore();
const analisesStore = useAnalisesStore();
const mapasStore = useMapasStore();
const router = useRouter();

const mostrarModalImpacto = ref(false);
const mostrarModalValidar = ref(false);
const mostrarModalDevolver = ref(false);
const mostrarModalHistoricoAnalise = ref(false);
const observacaoValidacao = ref<string>("");
const observacaoDevolucao = ref<string>("");

const {impactoMapa} = storeToRefs(mapasStore);
const loadingImpacto = ref(false);

const unidade = computed(() => {
  function buscarUnidade(
      unidades: Unidade[],
      sigla: string,
  ): Unidade | undefined {
    for (const u of unidades) {
      if (u.sigla === sigla) return u;
      if (u.filhas && u.filhas.length) {
        const encontrada = buscarUnidade(u.filhas, sigla);
        if (encontrada) return encontrada;
      }
    }
  }

  return buscarUnidade(unidadesStore.unidades as Unidade[], unidadeId.value);
});

const siglaUnidade = computed(() => unidade.value?.sigla || unidadeId.value);
const nomeUnidade = computed(() =>
    unidade.value?.nome ? `${unidade.value.nome}` : "",
);
const perfilSelecionado = computed(() => perfilStore.perfilSelecionado);

const subprocesso = computed(() => {
  if (!processosStore.processoDetalhe) return null;
  return processosStore.processoDetalhe.unidades.find(
      (u) => u.sigla === unidadeId.value,
  );
});

const isHomologacao = computed(() => {
  if (!subprocesso.value) return false;
  const {situacaoSubprocesso} = subprocesso.value;
  return (
      perfilSelecionado.value === Perfil.ADMIN &&
                (situacaoSubprocesso === SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO ||
                situacaoSubprocesso === SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO ||
                situacaoSubprocesso === SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA ||
                situacaoSubprocesso === SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA)  );
});

const podeVerImpacto = computed(() => {
  if (!subprocesso.value || !perfilSelecionado.value) return false;
  const perfil = perfilSelecionado.value;
  const podeVer = perfil === Perfil.GESTOR || perfil === Perfil.ADMIN;
  const situacaoCorreta =
      subprocesso.value.situacaoSubprocesso === SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO ||
      subprocesso.value.situacaoSubprocesso === SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA;
  return podeVer && situacaoCorreta;
});

const codSubprocesso = computed(() => subprocesso.value?.codSubprocesso);

const atividades = computed<Atividade[]>(() => {
  if (codSubprocesso.value === undefined) return [];
  return atividadesStore.obterAtividadesPorSubprocesso(codSubprocesso.value) || [];
});

const processoAtual = computed(() => processosStore.processoDetalhe);
const isRevisao = computed(
    () => processoAtual.value?.tipo === TipoProcesso.REVISAO,
);

const historicoAnalises = computed(() => {
  if (!codSubprocesso.value) return [];
  return analisesStore.obterAnalisesPorSubprocesso(codSubprocesso.value);
});

onMounted(async () => {
  await processosStore.buscarProcessoDetalhe(codProcesso.value);
  if (codSubprocesso.value) {
    await atividadesStore.buscarAtividadesParaSubprocesso(codSubprocesso.value);
  }
});

function validarCadastro() {
  mostrarModalValidar.value = true;
}

function devolverCadastro() {
  mostrarModalDevolver.value = true;
}

async function confirmarValidacao() {
  if (!codSubprocesso.value || !perfilSelecionado.value) return;

  const commonRequest = {
    observacoes: observacaoValidacao.value,
  };

  let sucesso: boolean;

  if (isHomologacao.value) {
    const req: HomologarCadastroRequest = {
      observacoes: observacaoValidacao.value,
    };
    if (isRevisao.value) {
      sucesso = await subprocessosStore.homologarRevisaoCadastro(
          codSubprocesso.value,
          req,
      );
    } else {
      sucesso = await subprocessosStore.homologarCadastro(codSubprocesso.value, req);
    }

    if (sucesso) {
      fecharModalValidar();
      // Redirecionar para Detalhes do subprocesso (CDU-13 passo 11.7 e CDU-14 passo 12.4)
      await router.push({
        name: "Subprocesso",
        params: {
          codProcesso: props.codProcesso,
          siglaUnidade: props.sigla
        }
      });
    }
  } else {
    const req: AceitarCadastroRequest = {...commonRequest};
    if (isRevisao.value) {
      sucesso = await subprocessosStore.aceitarRevisaoCadastro(codSubprocesso.value, req);
    } else {
      sucesso = await subprocessosStore.aceitarCadastro(codSubprocesso.value, req);
    }

    if (sucesso) {
      fecharModalValidar();
      await router.push({name: "Painel"});
    }
  }
}

async function confirmarDevolucao() {
  if (!codSubprocesso.value || !perfilSelecionado.value) return;
  const req: DevolverCadastroRequest = {
    observacoes: observacaoDevolucao.value,
  };

  let sucesso: boolean;
  if (isRevisao.value) {
    sucesso = await subprocessosStore.devolverRevisaoCadastro(codSubprocesso.value, req);
  } else {
    sucesso = await subprocessosStore.devolverCadastro(codSubprocesso.value, req);
  }

  if (sucesso) {
    fecharModalDevolver();
    await router.push("/painel");
  }
}

function fecharModalValidar() {
  mostrarModalValidar.value = false;
  observacaoValidacao.value = "";
}

function fecharModalDevolver() {
  mostrarModalDevolver.value = false;
  observacaoDevolucao.value = "";
}

async function abrirModalImpacto() {
  mostrarModalImpacto.value = true;
  if (codSubprocesso.value) {
    loadingImpacto.value = true;
    try {
      await mapasStore.buscarImpactoMapa(codSubprocesso.value);
    } finally {
      loadingImpacto.value = false;
    }
  }
}

function fecharModalImpacto() {
  mostrarModalImpacto.value = false;
}

async function abrirModalHistoricoAnalise() {
  if (codSubprocesso.value) {
    await analisesStore.buscarAnalisesCadastro(codSubprocesso.value);
  }
  mostrarModalHistoricoAnalise.value = true;
}

function fecharModalHistoricoAnalise() {
  mostrarModalHistoricoAnalise.value = false;
}

defineExpose({
  mostrarModalHistoricoAnalise,
  mostrarModalValidar,
  mostrarModalDevolver,
});
</script>

<style>
.unidade-nome {
  color: var(--bs-body-color);
  opacity: 0.85;
  padding-right: 1rem;
}

.atividade-card {
  transition: box-shadow 0.2s;
}

.atividade-descricao {
  word-break: break-word;
  max-width: 100%;
  display: inline-block;
}

.atividade-titulo-card {
  background: var(--bs-light);
  border-bottom: 1px solid var(--bs-border-color);
  padding: 0.5rem 0.75rem;
  margin-left: -0.75rem;
  margin-right: -0.75rem;
  margin-top: -0.5rem;
  border-top-left-radius: 0.375rem;
  border-top-right-radius: 0.375rem;
}

.atividade-titulo-card .atividade-descricao {
  font-size: 1.1rem;
}

.unidade-cabecalho {
  font-size: 1.1rem;
  font-weight: 500;
  margin-bottom: 1.2rem;
  display: flex;
  gap: 0.5rem;
}

.unidade-sigla {
  background: var(--bs-light);
  color: var(--bs-dark);
  font-weight: bold;
  border-radius: 0.5rem;
  letter-spacing: 1px;
}

.unidade-nome {
  color: var(--bs-body-color);
  opacity: 0.85;
  padding-right: 1rem;
}

</style>
