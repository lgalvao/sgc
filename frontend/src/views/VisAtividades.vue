<template>
  <div class="container mt-4">
    <div class="unidade-cabecalho w-100">
      <span class="unidade-sigla">{{ siglaUnidade }}</span>
      <span class="unidade-nome">{{ nomeUnidade }}</span>
    </div>

    <div class="d-flex justify-content-between align-items-center mb-3">
      <h2 class="mb-0">
        Atividades e conhecimentos
      </h2>
      <div class="d-flex gap-2">
        <button
          v-if="podeVerImpacto"
          class="btn btn-outline-secondary"
          @click="abrirModalImpacto"
        >
          <i class="bi bi-arrow-right-circle me-2" />{{ isRevisao ? 'Ver impactos' : 'Impacto no mapa' }}
        </button>
        <button
          class="btn btn-outline-info"
          @click="abrirModalHistoricoAnalise"
        >
          Histórico de análise
        </button>
        <button
          class="btn btn-secondary"
          data-testid="btn-devolver"
          title="Devolver para ajustes"
          @click="devolverCadastro"
        >
          Devolver para ajustes
        </button>
        <button
          class="btn btn-success"
          data-testid="btn-acao-principal-analise"
          title="Validar"
          @click="validarCadastro"
        >
          {{ perfilSelecionado === Perfil.ADMIN ? 'Homologar' : 'Registrar aceite' }}
        </button>
      </div>
    </div>

    <!-- Lista de atividades -->
    <div
      v-for="(atividade) in atividades"
      :key="atividade.codigo"
      class="card mb-3 atividade-card"
    >
      <div class="card-body py-2">
        <div
          class="card-title d-flex align-items-center atividade-edicao-row position-relative group-atividade atividade-hover-row atividade-titulo-card"
        >
          <strong
            class="atividade-descricao"
            data-testid="atividade-descricao"
          >{{ atividade.descricao }}</strong>
        </div>

        <!-- Conhecimentos da atividade -->
        <div class="mt-3 ms-3">
          <div
            v-for="(conhecimento) in atividade.conhecimentos"
            :key="conhecimento.id"
            class="d-flex align-items-center mb-2 group-conhecimento position-relative conhecimento-hover-row"
          >
            <span data-testid="conhecimento-descricao">{{ conhecimento.descricao }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Modal de Impacto no Mapa -->
    <ImpactoMapaModal
      :id-processo="codProcesso"
      :mostrar="mostrarModalImpacto"
      :sigla-unidade="siglaUnidade"
      @fechar="fecharModalImpacto"
    />

    <!-- Modal de Histórico de Análise -->
    <HistoricoAnaliseModal
      :cod-subrocesso="codSubrocesso"
      :mostrar="mostrarModalHistoricoAnalise"
      @fechar="fecharModalHistoricoAnalise"
    />

    <!-- Modal de Validação -->
    <div
      v-if="mostrarModalValidar"
      class="modal fade show"
      style="display: block;"
      tabindex="-1"
    >
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">
              {{ isHomologacao ? 'Homologação do cadastro de atividades e conhecimentos' : (isRevisao ? 'Aceite da revisão do cadastro' : 'Validação do cadastro') }}
            </h5>
            <button
              type="button"
              class="btn-close"
              @click="fecharModalValidar"
            />
          </div>
          <div class="modal-body">
            <p>{{ isHomologacao ? 'Confirma a homologação do cadastro de atividades e conhecimentos?' : (isRevisao ? 'Confirma o aceite da revisão do cadastro de atividades?' : 'Confirma o aceite do cadastro de atividades?') }}</p>
            <div
              v-if="!isHomologacao"
              class="mb-3"
            >
              <label
                class="form-label"
                for="observacaoValidacao"
              >Observação</label>
              <b-form-textarea
                id="observacaoValidacao"
                v-model="observacaoValidacao"
                data-testid="input-observacao-aceite"
                rows="3"
              />
            </div>
          </div>
          <div class="modal-footer">
            <button
              type="button"
              class="btn btn-secondary"
              @click="fecharModalValidar"
            >
              Cancelar
            </button>
            <button
              type="button"
              class="btn btn-success"
              data-testid="btn-modal-confirmar-aceite"
              @click="confirmarValidacao"
            >
              Confirmar
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Modal de Devolução -->
    <div
      v-if="mostrarModalDevolver"
      class="modal fade show"
      style="display: block;"
      tabindex="-1"
    >
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">
              {{ isRevisao ? 'Devolução da revisão do cadastro' : 'Devolução do cadastro' }}
            </h5>
            <button
              type="button"
              class="btn-close"
              @click="fecharModalDevolver"
            />
          </div>
          <div class="modal-body">
            <p>{{ isRevisao ? 'Confirma a devolução da revisão do cadastro para ajustes?' : 'Confirma a devolução do cadastro para ajustes?' }}</p>
            <div class="mb-3">
              <label
                class="form-label"
                for="observacaoDevolucao"
              >Observação</label>
              <b-form-textarea
                id="observacaoDevolucao"
                v-model="observacaoDevolucao"
                data-testid="input-observacao-devolucao"
                rows="3"
              />
            </div>
          </div>
          <div class="modal-footer">
            <button
              type="button"
              class="btn btn-secondary"
              @click="fecharModalDevolver"
            >
              Cancelar
            </button>
            <button
              type="button"
              class="btn btn-danger"
              data-testid="btn-modal-confirmar-devolucao"
              @click="confirmarDevolucao"
            >
              Confirmar
            </button>
          </div>
        </div>
      </div>
    </div>

    <div
      v-if="mostrarModalValidar || mostrarModalDevolver"
      class="modal-backdrop fade show"
    />
  </div>
</template>

<script lang="ts" setup>
import {computed, onMounted, ref} from 'vue'
import {usePerfilStore} from '@/stores/perfil';
import {useAtividadesStore} from '@/stores/atividades';
import {useUnidadesStore} from '@/stores/unidades';
import {useProcessosStore} from '@/stores/processos';
import {useRouter} from 'vue-router';
import {
  AceitarCadastroRequest,
  Atividade,
  DevolverCadastroRequest,
  HomologarCadastroRequest,
  Perfil,
  SituacaoSubprocesso,
  TipoProcesso,
  Unidade
} from '@/types/tipos';
import ImpactoMapaModal from '@/components/ImpactoMapaModal.vue'
import HistoricoAnaliseModal from '@/components/HistoricoAnaliseModal.vue'
import {useSubprocessosStore} from "@/stores/subprocessos";

const props = defineProps<{
  codProcesso: number | string,
  sigla: string
}>()

const unidadeId = computed(() => props.sigla)
const codProcesso = computed(() => Number(props.codProcesso))

const atividadesStore = useAtividadesStore()
const unidadesStore = useUnidadesStore()
const processosStore = useProcessosStore()
const subprocessosStore = useSubprocessosStore()
const perfilStore = usePerfilStore()
const router = useRouter()

const mostrarModalImpacto = ref(false)
const mostrarModalValidar = ref(false)
const mostrarModalDevolver = ref(false)
const mostrarModalHistoricoAnalise = ref(false)
const observacaoValidacao = ref<string>('')
const observacaoDevolucao = ref<string>('')

const unidade = computed(() => {
  function buscarUnidade(unidades: Unidade[], sigla: string): Unidade | undefined {
    for (const u of unidades) {
      if (u.sigla === sigla) return u
      if (u.filhas && u.filhas.length) {
        const encontrada = buscarUnidade(u.filhas, sigla)
        if (encontrada) return encontrada
      }
    }
  }

  return buscarUnidade(unidadesStore.unidades as Unidade[], unidadeId.value)
})

const siglaUnidade = computed(() => unidade.value?.sigla || unidadeId.value)
const nomeUnidade = computed(() => (unidade.value?.nome ? `${unidade.value.nome}` : ''))
const perfilSelecionado = computed(() => perfilStore.perfilSelecionado);

const subprocesso = computed(() => {
  if (!processosStore.processoDetalhe) return null;
  return processosStore.processoDetalhe.unidades.find(u => u.sigla === unidadeId.value);
});

const isHomologacao = computed(() => {
    if (!subprocesso.value) return false;
    const {situacaoSubprocesso} = subprocesso.value;
    return perfilSelecionado.value === Perfil.ADMIN && (situacaoSubprocesso === SituacaoSubprocesso.AGUARDANDO_HOMOLOGACAO_ATIVIDADES || situacaoSubprocesso === SituacaoSubprocesso.AGUARDANDO_HOMOLOGACAO_MAPA);
});

const podeVerImpacto = computed(() => {
  if (!subprocesso.value || !perfilSelecionado.value) return false;
  const perfil = perfilSelecionado.value;
  const podeVer = perfil === Perfil.GESTOR || perfil === Perfil.ADMIN;
  const situacaoCorreta = subprocesso.value.situacaoSubprocesso === SituacaoSubprocesso.ATIVIDADES_REVISADAS;
  return podeVer && situacaoCorreta;
});

const codSubrocesso = computed(() => subprocesso.value?.codUnidade);

const atividades = computed<Atividade[]>(() => {
  if (codSubrocesso.value === undefined) return []
  return atividadesStore.getAtividadesPorSubprocesso(codSubrocesso.value) || []
})

const processoAtual = computed(() => processosStore.processoDetalhe);
const isRevisao = computed(() => processoAtual.value?.tipo === TipoProcesso.REVISAO);

onMounted(async () => {
  await processosStore.fetchProcessoDetalhe(codProcesso.value);
  if (codSubrocesso.value) {
    await atividadesStore.fetchAtividadesParaSubprocesso(codSubrocesso.value);
  }
});

function validarCadastro() {
  mostrarModalValidar.value = true;
}

function devolverCadastro() {
  mostrarModalDevolver.value = true;
}

async function confirmarValidacao() {
  if (!codSubrocesso.value || !perfilSelecionado.value) return;

  const commonRequest = {
    observacoes: observacaoValidacao.value,
  };

  if (isHomologacao.value) {
    const req: HomologarCadastroRequest = { ...commonRequest };
    if (isRevisao.value) {
        await subprocessosStore.homologarRevisaoCadastro(codSubrocesso.value, req);
    } else {
        await subprocessosStore.homologarCadastro(codSubrocesso.value, req);
    }
  } else {
      const req: AceitarCadastroRequest = { ...commonRequest };
      if (isRevisao.value) {
          await subprocessosStore.aceitarRevisaoCadastro(codSubrocesso.value, req);
      } else {
          await subprocessosStore.aceitarCadastro(codSubrocesso.value, req);
      }
  }

  fecharModalValidar();
  await router.push('/painel');
}

async function confirmarDevolucao() {
  if (!codSubrocesso.value || !perfilSelecionado.value) return;
  const req: DevolverCadastroRequest = {
    motivo: '', // Adicionar esta linha
    observacoes: observacaoDevolucao.value,
  };

  if (isRevisao.value) {
      await subprocessosStore.devolverRevisaoCadastro(codSubrocesso.value, req);
  } else {
      await subprocessosStore.devolverCadastro(codSubrocesso.value, req);
  }

  fecharModalDevolver();
  await router.push('/painel');
}

function fecharModalValidar() {
  mostrarModalValidar.value = false;
  observacaoValidacao.value = '';
}

function fecharModalDevolver() {
  mostrarModalDevolver.value = false;
  observacaoDevolucao.value = '';
}

function abrirModalImpacto() {
  mostrarModalImpacto.value = true;
}

function fecharModalImpacto() {
  mostrarModalImpacto.value = false;
}

function abrirModalHistoricoAnalise() {
  mostrarModalHistoricoAnalise.value = true;
}

function fecharModalHistoricoAnalise() {
  mostrarModalHistoricoAnalise.value = false;
}
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