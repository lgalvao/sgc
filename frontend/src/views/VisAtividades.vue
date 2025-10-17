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
      v-for="(atividade, idx) in atividades"
      :key="atividade.codigo || idx"
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
            :key="conhecimento.codigo"
            class="d-flex align-items-center mb-2 group-conhecimento position-relative conhecimento-hover-row"
          >
            <span data-testid="conhecimento-descricao">{{ conhecimento.descricao }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Modal de Impacto no Mapa -->
    <ImpactoMapaModal
      :id-processo="idProcesso"
      :mostrar="mostrarModalImpacto"
      :sigla-unidade="siglaUnidade"
      @fechar="fecharModalImpacto"
    />

    <!-- Modal de Histórico de Análise -->
    <HistoricoAnaliseModal
      :id-subprocesso="idSubprocesso"
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
              <textarea
                id="observacaoValidacao"
                v-model="observacaoValidacao"
                class="form-control"
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

    <!-- Modal de Homologação Sem Impacto -->
    <div
      v-if="mostrarModalHomologacaoSemImpacto"
      class="modal fade show"
      style="display: block;"
      tabindex="-1"
    >
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">
              Homologação do mapa de competências
            </h5>
            <button
              type="button"
              class="btn-close"
              @click="fecharModalHomologacaoSemImpacto"
            />
          </div>
          <div class="modal-body">
            <p>A revisão do cadastro não produziu nenhum impacto no mapa de competência da unidade. Confirma a manutenção do mapa de competências vigente?</p>
          </div>
          <div class="modal-footer">
            <button
              type="button"
              class="btn btn-secondary"
              @click="fecharModalHomologacaoSemImpacto"
            >
              Cancelar
            </button>
            <button
              type="button"
              class="btn btn-success"
              @click="confirmarHomologacaoSemImpacto"
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
              <textarea
                id="observacaoDevolucao"
                v-model="observacaoDevolucao"
                class="form-control"
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
      v-if="mostrarModalValidar || mostrarModalDevolver || mostrarModalHomologacaoSemImpacto"
      class="modal-backdrop fade show"
    />
  </div>
</template>

<script lang="ts" setup>
import {computed, ref} from 'vue'
import {usePerfilStore} from '@/stores/perfil';
import {useAtividadesStore} from '@/stores/atividades'
import {useUnidadesStore} from '@/stores/unidades'
import {useProcessosStore} from '@/stores/processos'
import {useNotificacoesStore} from '@/stores/notificacoes'
import {useAlertasStore} from '@/stores/alertas'
import {useAnalisesStore} from '@/stores/analises' // Adicionado
import {useRouter} from 'vue-router'
import {Atividade, Perfil, Processo, ResultadoAnalise, Subprocesso, Unidade} from '@/types/tipos'
import ImpactoMapaModal from '@/components/ImpactoMapaModal.vue'
import HistoricoAnaliseModal from '@/components/HistoricoAnaliseModal.vue'
import {URL_SISTEMA} from '@/constants';

const props = defineProps<{
  idProcesso: number | string,
  sigla: string
}>()

const unidadeId = computed(() => props.sigla)
const idProcesso = computed(() => Number(props.idProcesso))

const atividadesStore = useAtividadesStore()
const unidadesStore = useUnidadesStore()
const processosStore = useProcessosStore()
const notificacoesStore = useNotificacoesStore()
const alertasStore = useAlertasStore()
const analisesStore = useAnalisesStore() // Adicionado
const perfilStore = usePerfilStore()
const router = useRouter()

const mostrarModalImpacto = ref(false)
const mostrarModalValidar = ref(false)
const mostrarModalDevolver = ref(false)
const mostrarModalHistoricoAnalise = ref(false)
const mostrarModalHomologacaoSemImpacto = ref(false)
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
const unidadeSuperior = computed<string>(() => unidadesStore.getUnidadeImediataSuperior(siglaUnidade.value) || '');

const isHomologacao = computed(() => perfilStore.perfilSelecionado === Perfil.ADMIN && unidadeSuperior.value === 'SEDOC');

const subprocesso = computed(() => {
  if (!processosStore.processoDetalhe) return null;
  return processosStore.processoDetalhe.unidades.find(u => u.sigla === unidadeId.value);
});

const podeVerImpacto = computed(() => {
  if (!subprocesso.value || !perfilStore.perfilSelecionado) return false;

  const perfil = perfilStore.perfilSelecionado;
  const podeVer = perfil === Perfil.GESTOR || perfil === Perfil.ADMIN;
  const situacaoCorreta = subprocesso.value.situacao === 'Revisão do cadastro disponibilizada';
  const localizacaoCorreta = subprocesso.value.unidadeAtual === perfilStore.unidadeSelecionada;

  return podeVer && situacaoCorreta && localizacaoCorreta;
});

import {onMounted} from 'vue'

const idSubprocesso = computed(() => subprocesso.value?.codUnidade);

const atividades = computed<Atividade[]>(() => {
  if (idSubprocesso.value === undefined) return []
  return atividadesStore.getAtividadesPorSubprocesso(idSubprocesso.value) || []
})

const processoAtual = computed(() => processosStore.processoDetalhe);

onMounted(async () => {
  await processosStore.fetchProcessoDetalhe(idProcesso.value);
  if (idSubprocesso.value) {
    await atividadesStore.fetchAtividadesParaSubprocesso(idSubprocesso.value);
  }
});

const isRevisao = computed(() => processoAtual.value?.tipo === 'Revisão');

function validarCadastro() {
  // A lógica de validação complexa agora reside no backend.
  // O frontend apenas abre o modal de confirmação.
  mostrarModalValidar.value = true;
}

function devolverCadastro() {
  mostrarModalDevolver.value = true;
}

async function confirmarValidacao() {
    if (!idSubprocesso.value) return;
    // A lógica de negócio será tratada pelo backend.
    // O frontend apenas chama a action apropriada.
    // Ex: await subprocessosStore.aceitarCadastro(idSubprocesso.value, observacaoValidacao.value);
    notificacoesStore.sucesso('Ação registrada', 'A análise foi registrada com sucesso.');
    fecharModalValidar();
    await router.push('/painel');
}

async function confirmarDevolucao() {
    if (!idSubprocesso.value) return;
    // Ex: await subprocessosStore.devolverCadastro(idSubprocesso.value, observacaoDevolucao.value);
    notificacoesStore.sucesso('Devolução registrada', 'O cadastro foi devolvido para ajustes.');
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
    // A lógica de impacto agora deve ser verificada no backend.
    // O botão pode chamar um endpoint que retorna se há impacto ou não.
    notificacoesStore.info("Verificação de Impacto", 'Esta funcionalidade será conectada ao backend.');
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

function fecharModalHomologacaoSemImpacto() {
  mostrarModalHomologacaoSemImpacto.value = false;
}

function confirmarHomologacaoSemImpacto() {
  if (!subprocesso.value) return;
  
  // 12.2.4 - Alterar situação para 'Mapa homologado'
  subprocesso.value.situacaoSubprocesso = 'Mapa homologado';
  
  notificacoesStore.sucesso('Homologação efetivada', 'O mapa de competências vigente foi mantido!');
  fecharModalHomologacaoSemImpacto();
  router.push(`/processo/${idProcesso.value}/${siglaUnidade.value}`);
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