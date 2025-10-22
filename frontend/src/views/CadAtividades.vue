<template>
  <div class="container mt-4">
    <div class="fs-5 w-100 mb-3">
      {{ siglaUnidade }} - {{ nomeUnidade }}
    </div>

    <div class="d-flex justify-content-between align-items-center mb-3">
      <h1 class="mb-0 display-6">
        Atividades e conhecimentos
      </h1>

      <div class="d-flex gap-2">
        <button
          v-if="podeVerImpacto"
          class="btn btn-outline-secondary"
          @click="abrirModalImpacto"
        >
          <i class="bi bi-arrow-right-circle me-2" />Impacto no mapa
        </button>
        <button
          v-if="isChefe && historicoAnalises.length > 0"
          class="btn btn-outline-info"
          @click="abrirModalHistorico"
        >
          Histórico de análise
        </button>
        <button
          v-if="isChefe"
          class="btn btn-outline-primary"
          title="Importar"
          @click="mostrarModalImportar = true"
        >
          Importar atividades
        </button>
        <button
          v-if="isChefe"
          class="btn btn-outline-success"
          data-testid="btn-disponibilizar"
          data-bs-toggle="tooltip"
          title="Disponibilizar"
          @click="disponibilizarCadastro"
        >
          Disponibilizar
        </button>
      </div>
    </div>

    <!-- Adicionar atividade -->
    <form
      class="row g-2 align-items-center mb-4"
      @submit.prevent="adicionarAtividade"
    >
      <div class="col">
        <input
          v-model="novaAtividade"
          class="form-control"
          data-testid="input-nova-atividade"
          placeholder="Nova atividade"
          type="text"
          aria-label="Nova atividade"
        >
      </div>
      <div class="col-auto">
        <button
          class="btn btn-outline-primary btn-sm"
          data-bs-toggle="tooltip"
          data-testid="btn-adicionar-atividade"
          title="Adicionar atividade"
          type="submit"
        >
          <i
            class="bi bi-save"
          />
        </button>
      </div>
    </form>

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
          <template v-if="editandoAtividade === atividade.codigo">
            <input
              v-model="atividadeEditada"
              class="form-control me-2 atividade-edicao-input"
              data-testid="input-editar-atividade"
              aria-label="Editar atividade"
            >
            <button
              class="btn btn-sm btn-outline-success me-1 botao-acao"
              data-bs-toggle="tooltip"
              data-testid="btn-salvar-edicao-atividade"
              title="Salvar"
              @click="salvarEdicaoAtividade(atividade.codigo)"
            >
              <i class="bi bi-save" />
            </button>
            <button
              class="btn btn-sm btn-outline-secondary botao-acao"
              data-bs-toggle="tooltip"
              data-testid="btn-cancelar-edicao-atividade"
              title="Cancelar"
              @click="cancelarEdicaoAtividade()"
            >
              <i class="bi bi-x" />
            </button>
          </template>

          <template v-else>
            <strong
              class="atividade-descricao"
              data-testid="atividade-descricao"
            >{{ atividade.descricao }}</strong>
            <div class="d-inline-flex align-items-center gap-1 ms-3 botoes-acao-atividade fade-group">
              <button
                class="btn btn-sm btn-outline-primary botao-acao"
                data-bs-toggle="tooltip"
                data-testid="btn-editar-atividade"
                title="Editar"
                @click="iniciarEdicaoAtividade(atividade.codigo, atividade.descricao)"
              >
                <i
                  class="bi bi-pencil"
                />
              </button>
              <button
                class="btn btn-sm btn-outline-danger botao-acao"
                data-bs-toggle="tooltip"
                data-testid="btn-remover-atividade"
                title="Remover"
                @click="removerAtividade(idx)"
              >
                <i
                  class="bi bi-trash"
                />
              </button>
            </div>
          </template>
        </div>

        <!-- Conhecimentos da atividade -->
        <div class="mt-3 ms-3">
          <div
            v-for="(conhecimento, cidx) in atividade.conhecimentos"
            :key="conhecimento.id"
            class="d-flex align-items-center mb-2 group-conhecimento position-relative conhecimento-hover-row"
          >
            <span data-testid="conhecimento-descricao">{{ conhecimento.descricao }}</span>
            <div class="d-inline-flex align-items-center gap-1 ms-3 botoes-acao fade-group">
              <button
                class="btn btn-sm btn-outline-primary botao-acao"
                data-bs-toggle="tooltip"
                data-testid="btn-editar-conhecimento"
                title="Editar"
                @click="abrirModalEdicaoConhecimento(conhecimento)"
              >
                <i class="bi bi-pencil" />
              </button>
              <button
                class="btn btn-sm btn-outline-danger botao-acao"
                data-bs-toggle="tooltip"
                data-testid="btn-remover-conhecimento"
                title="Remover"
                @click="removerConhecimento(idx, cidx)"
              >
                <i class="bi bi-trash" />
              </button>
            </div>
          </div>
          <form
            class="row g-2 align-items-center"
            @submit.prevent="adicionarConhecimento(idx)"
          >
            <div class="col">
              <input
                v-model="atividade.novoConhecimento"
                class="form-control form-control-sm"
                data-testid="input-novo-conhecimento"
                placeholder="Novo conhecimento"
                type="text"
                aria-label="Novo conhecimento"
              >
            </div>
            <div class="col-auto">
              <button
                class="btn btn-outline-secondary btn-sm"
                data-bs-toggle="tooltip"
                data-testid="btn-adicionar-conhecimento"
                title="Adicionar Conhecimento"
                type="submit"
              >
                <i
                  class="bi bi-save"
                />
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>

    <!-- Modais -->
    <ImportarAtividadesModal
      :mostrar="mostrarModalImportar"
      :id-subprocesso-destino="idSubprocesso"
      @fechar="mostrarModalImportar = false"
      @importar="handleImportAtividades"
    />

    <ImpactoMapaModal
      :id-processo="idProcesso"
      :mostrar="mostrarModalImpacto"
      :sigla-unidade="siglaUnidade"
      @fechar="fecharModalImpacto"
    />

    <!-- Modal de Confirmação de Disponibilização -->
    <div
      v-if="mostrarModalConfirmacao"
      ref="confirmacaoModalRef"
      class="modal fade show"
      style="display: block;"
      tabindex="-1"
    >
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">
              {{ isRevisao ? 'Disponibilização da revisão do cadastro' : 'Disponibilização do cadastro' }}
            </h5>
            <button
              type="button"
              class="btn-close"
              @click="fecharModalConfirmacao"
            />
          </div>
          <div class="modal-body">
            <p>
              {{
                isRevisao ? 'Confirma a finalização da revisão e a disponibilização do cadastro?' : 'Confirma a finalização e a disponibilização do cadastro?'
              }} Essa ação bloqueia a edição e habilita a análise do cadastro por unidades superiores.
            </p>
            <div
              v-if="atividadesSemConhecimento.length > 0"
              class="alert alert-warning"
            >
              <strong>Atenção:</strong> As seguintes atividades não têm conhecimentos associados:
              <ul>
                <li
                  v-for="atividade in atividadesSemConhecimento"
                  :key="atividade.codigo"
                >
                  {{ atividade.descricao }}
                </li>
              </ul>
            </div>
          </div>
          <div class="modal-footer">
            <button
              type="button"
              class="btn btn-secondary"
              @click="fecharModalConfirmacao"
            >
              Cancelar
            </button>
            <button
              type="button"
              class="btn btn-success"
              @click="confirmarDisponibilizacao"
            >
              Confirmar
            </button>
          </div>
        </div>
      </div>
    </div>
    <div
      v-if="mostrarModalConfirmacao"
      class="modal-backdrop fade show"
    />

    <!-- Modal de Histórico de Análise -->
    <div
      v-if="mostrarModalHistorico"
      class="modal fade show"
      style="display: block;"
      tabindex="-1"
    >
      <div class="modal-dialog modal-dialog-centered modal-lg">
        <div class="modal-content">
          <div class="modal-header">
            <h5
              class="modal-title"
              data-testid="modal-historico-analise-titulo"
            >
              Histórico de Análise
            </h5>
            <button
              type="button"
              class="btn-close"
              @click="fecharModalHistorico"
            />
          </div>
          <div class="modal-body">
            <div class="table-responsive">
              <table
                class="table table-striped"
                data-testid="historico-analise-tabela"
              >
                <thead>
                  <tr>
                    <th>Data/Hora</th>
                    <th>Unidade</th>
                    <th>Resultado</th>
                    <th>Observações</th>
                  </tr>
                </thead>
                <tbody>
                  <tr
                    v-for="analise in historicoAnalises"
                    :key="analise.codigo"
                  >
                    <td>{{ formatarData(analise.dataHora) }}</td>
                    <td>{{ 'unidade' in analise ? analise.unidade : analise.unidadeSigla }}</td>
                    <td>{{ analise.resultado }}</td>
                    <td>{{ analise.observacoes || '-' }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
          <div class="modal-footer">
            <button
              type="button"
              class="btn btn-secondary"
              @click="fecharModalHistorico"
            >
              Fechar
            </button>
          </div>
        </div>
      </div>
    </div>
    <div
      v-if="mostrarModalHistorico"
      class="modal-backdrop fade show"
    />

    <!-- Modal de Edição de Conhecimento -->
    <EditarConhecimentoModal
      :mostrar="mostrarModalEdicaoConhecimento"
      :conhecimento="conhecimentoSendoEditado as Conhecimento"
      @fechar="fecharModalEdicaoConhecimento"
      @salvar="salvarEdicaoConhecimento"
    />
  </div>
</template>

<script lang="ts" setup>
import {computed, onMounted, ref, watch} from 'vue'
import {Modal} from 'bootstrap'
import {usePerfil} from '@/composables/usePerfil'
import {useAtividadesStore} from '@/stores/atividades'
import {useUnidadesStore} from '@/stores/unidades'
import {useProcessosStore} from '@/stores/processos'
import {useMapasStore} from '@/stores/mapas'
import {useAnalisesStore} from '@/stores/analises'
import {useSubprocessosStore} from '@/stores/subprocessos'
import {
  type Atividade,
  type Conhecimento,
  type CriarAtividadeRequest,
  type CriarConhecimentoRequest,
  Perfil,
  type ProcessoResumo,
  SituacaoSubprocesso,
  TipoProcesso,
  Unidade,
  type UnidadeParticipante
} from '@/types/tipos'
import {useNotificacoesStore} from '@/stores/notificacoes'
import {useRouter} from 'vue-router'
import ImpactoMapaModal from '@/components/ImpactoMapaModal.vue'
import ImportarAtividadesModal from '@/components/ImportarAtividadesModal.vue'
import EditarConhecimentoModal from '@/components/EditarConhecimentoModal.vue'

interface AtividadeComEdicao extends Atividade {
  novoConhecimento?: string;
}

const props = defineProps<{
  idProcesso: number | string,
  sigla: string
}>()

const unidadeId = computed(() => props.sigla)
const idProcesso = computed(() => Number(props.idProcesso))

const atividadesStore = useAtividadesStore()
const unidadesStore = useUnidadesStore()
const processosStore = useProcessosStore()
const subprocessosStore = useSubprocessosStore()
const analisesStore = useAnalisesStore()
const notificacoesStore = useNotificacoesStore()
const router = useRouter()
useMapasStore()

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

const siglaUnidade = computed(() => unidade.value?.sigla || props.sigla)
const nomeUnidade = computed(() => (unidade.value?.nome ? `${unidade.value.nome}` : ''))
const novaAtividade = ref('')
const idSubprocesso = computed(() => processosStore.processoDetalhe?.unidades.find(u => u.sigla === unidadeId.value)?.codUnidade);

const atividades = computed<AtividadeComEdicao[]>({
  get: () => {
    if (idSubprocesso.value === undefined) return []
    return atividadesStore.getAtividadesPorSubprocesso(idSubprocesso.value).map(a => ({...a, novoConhecimento: ''}));
  },
  set: () => {
    // Setter intencionalmente vazio para evitar mutações diretas.
  }
})

const processoAtual = computed(() => processosStore.processoDetalhe);
const isRevisao = computed(() => processoAtual.value?.tipo === TipoProcesso.REVISAO);

async function adicionarAtividade() {
  if (novaAtividade.value?.trim() && idSubprocesso.value) {
    const request: CriarAtividadeRequest = {
      descricao: novaAtividade.value.trim(),
    };
    await atividadesStore.adicionarAtividade(idSubprocesso.value, request);
    novaAtividade.value = '';
  }
}

async function removerAtividade(idx: number) {
  if (!idSubprocesso.value) return;
  const atividadeRemovida = atividades.value[idx];
  if (confirm('Confirma a remoção desta atividade e todos os conhecimentos associados?')) {
    await atividadesStore.removerAtividade(idSubprocesso.value, atividadeRemovida.codigo);
  }
}

async function adicionarConhecimento(idx: number) {
  if (!idSubprocesso.value) return;
  const atividade = atividades.value[idx];
  if (atividade.novoConhecimento?.trim()) {
    const request: CriarConhecimentoRequest = {
      descricao: atividade.novoConhecimento.trim()
    };
    await atividadesStore.adicionarConhecimento(idSubprocesso.value, atividade.codigo, request);
    atividade.novoConhecimento = '';
  }
}

async function removerConhecimento(idx: number, cidx: number) {
  if (!idSubprocesso.value) return;
  const atividade = atividades.value[idx];
  const conhecimentoRemovido = atividade.conhecimentos[cidx];
  if (confirm('Confirma a remoção deste conhecimento?')) {
    await atividadesStore.removerConhecimento(idSubprocesso.value, atividade.codigo, conhecimentoRemovido.id);
  }
}

const mostrarModalEdicaoConhecimento = ref(false)
const conhecimentoSendoEditado = ref<Conhecimento | null>(null)

function abrirModalEdicaoConhecimento(conhecimento: Conhecimento) {
  conhecimentoSendoEditado.value = { ...conhecimento };
  mostrarModalEdicaoConhecimento.value = true
}

function fecharModalEdicaoConhecimento() {
  mostrarModalEdicaoConhecimento.value = false
  conhecimentoSendoEditado.value = null
}

async function salvarEdicaoConhecimento(conhecimentoId: number, novaDescricao: string) {
  if (!idSubprocesso.value) return;
  const atividade = atividades.value.find(a => a.conhecimentos.some(c => c.id === conhecimentoId));
  if (atividade) {
    const conhecimento = atividade.conhecimentos.find(c => c.id === conhecimentoId);
    if (conhecimento) {
      const conhecimentoAtualizado: Conhecimento = {...conhecimento, descricao: novaDescricao};
      await atividadesStore.atualizarConhecimento(idSubprocesso.value, atividade.codigo, conhecimentoId, conhecimentoAtualizado);
    }
  }
  fecharModalEdicaoConhecimento();
}

const editandoAtividade = ref<number | null>(null)
const atividadeEditada = ref('')

function iniciarEdicaoAtividade(id: number, valorAtual: string) {
  editandoAtividade.value = id
  atividadeEditada.value = valorAtual
}

async function salvarEdicaoAtividade(id: number) {
  if (String(atividadeEditada.value).trim() && idSubprocesso.value) {
    const atividadeOriginal = atividades.value.find(a => a.codigo === id);
    if (atividadeOriginal) {
      const atividadeAtualizada: Atividade = {...atividadeOriginal, descricao: atividadeEditada.value.trim()};
      await atividadesStore.atualizarAtividade(idSubprocesso.value, id, atividadeAtualizada);
    }
  }
  cancelarEdicaoAtividade();
}

function cancelarEdicaoAtividade() {
  editandoAtividade.value = null
  atividadeEditada.value = ''
}

async function handleImportAtividades() {
  mostrarModalImportar.value = false;
  notificacoesStore.sucesso('Importação Concluída', 'As atividades foram importadas para o seu mapa.');
  // A store já foi atualizada pela ação de importação,
  // então não precisamos buscar os dados novamente aqui.
}

const {perfilSelecionado} = usePerfil()

const isChefe = computed(() => perfilSelecionado.value === Perfil.CHEFE)

const subprocesso = computed(() => {
  if (!processosStore.processoDetalhe) return null;
  return processosStore.processoDetalhe.unidades.find(u => u.sigla === unidadeId.value);
});

const podeVerImpacto = computed(() => {
  if (!isChefe.value || !subprocesso.value) return false;
  return subprocesso.value.situacaoSubprocesso === SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO;
});

const processoSelecionado = ref<ProcessoResumo | null>(null)
const processoSelecionadoId = ref<number | null>(null)
const unidadesParticipantes = ref<UnidadeParticipante[]>([])
const unidadeSelecionada = ref<UnidadeParticipante | null>(null)
const unidadeSelecionadaId = ref<number | null>(null)
const atividadesParaImportar = ref<Atividade[]>([])

const mostrarModalImpacto = ref(false)
const mostrarModalImportar = ref(false)
const mostrarModalConfirmacao = ref(false)
const mostrarModalHistorico = ref(false)
const atividadesSemConhecimento = ref<Atividade[]>([])

const confirmacaoModalRef = ref<HTMLElement | null>(null);

onMounted(async () => {
  await processosStore.fetchProcessoDetalhe(idProcesso.value);
  if (idSubprocesso.value) {
    await atividadesStore.fetchAtividadesParaSubprocesso(idSubprocesso.value);
    await analisesStore.fetchAnalisesCadastro(idSubprocesso.value)
  }
});

watch(processoSelecionadoId, (newId) => {
  if (newId) {
    const processo = processosDisponiveis.value.find(p => p.codigo === newId)
    if (processo) {
      selecionarProcesso(processo)
    }
  } else {
    selecionarProcesso(null)
  }
})

watch(unidadeSelecionadaId, (newId) => {
  if (newId) {
    const unidade = unidadesParticipantes.value.find(u => u.codUnidade === newId)
    if (unidade) {
      selecionarUnidade(unidade)
    }
  } else {
    selecionarUnidade(null)
  }
})

const processosDisponiveis = computed<ProcessoResumo[]>(() => {
  return processosStore.processosPainel.filter(p =>
      (p.tipo === TipoProcesso.MAPEAMENTO || p.tipo === TipoProcesso.REVISAO) && p.situacao === 'FINALIZADO'
  )
})

async function selecionarProcesso(processo: ProcessoResumo | null) {
  processoSelecionado.value = processo
  if (processo) {
    await processosStore.fetchProcessoDetalhe(processo.codigo);
    unidadesParticipantes.value = processosStore.processoDetalhe?.unidades || [];
  } else {
    unidadesParticipantes.value = [];
  }
  unidadeSelecionada.value = null
  unidadeSelecionadaId.value = null
}

async function selecionarUnidade(unidadePu: UnidadeParticipante | null) {
  unidadeSelecionada.value = unidadePu
  if (unidadePu) {
    await atividadesStore.fetchAtividadesParaSubprocesso(unidadePu.codUnidade)
    const atividadesDaOutraUnidade = atividadesStore.getAtividadesPorSubprocesso(unidadePu.codUnidade)
    atividadesParaImportar.value = atividadesDaOutraUnidade ? [...atividadesDaOutraUnidade] : []
  } else {
    atividadesParaImportar.value = []
  }
}

function validarAtividades(): Atividade[] {
  return atividades.value.filter(atividade => atividade.conhecimentos.length === 0);
}

const historicoAnalises = computed(() => {
  if (!idSubprocesso.value) return []
  return analisesStore.getAnalisesPorSubprocesso(idSubprocesso.value)
})

function formatarData(data: string): string {
  return new Date(data).toLocaleString('pt-BR')
}

function abrirModalHistorico() {
  mostrarModalHistorico.value = true
}

function fecharModalHistorico() {
  mostrarModalHistorico.value = false
}

function disponibilizarCadastro() {
  const sub = subprocesso.value;
  const situacaoEsperada = isRevisao.value ? SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO : SituacaoSubprocesso.CADASTRO_EM_ANDAMENTO;

  if (!sub || sub.situacaoSubprocesso !== situacaoEsperada) {
    notificacoesStore.erro('Ação não permitida', `Ação permitida apenas na situação: "${situacaoEsperada}".`);
    return;
  }

  atividadesSemConhecimento.value = validarAtividades();
  if (atividadesSemConhecimento.value.length > 0) {
    const atividadesDescricoes = atividadesSemConhecimento.value.map(a => `- ${a.descricao}`).join('\n');
    notificacoesStore.aviso('Atividades Incompletas', `As seguintes atividades não têm conhecimentos associados:\n${atividadesDescricoes}`);
  }

  mostrarModalConfirmacao.value = true;
}

function fecharModalConfirmacao() {
  if (confirmacaoModalRef.value) {
    const modalInstance = Modal.getInstance(confirmacaoModalRef.value);
    modalInstance?.hide();
  }
  mostrarModalConfirmacao.value = false;
  atividadesSemConhecimento.value = [];
}

async function confirmarDisponibilizacao() {
  if (!idSubprocesso.value) return;

  if (isRevisao.value) {
    await subprocessosStore.disponibilizarRevisaoCadastro(idSubprocesso.value);
  } else {
    await subprocessosStore.disponibilizarCadastro(idSubprocesso.value);
  }

  fecharModalConfirmacao();
  await router.push('/painel');
}

function abrirModalImpacto() {
  mostrarModalImpacto.value = true;
}

function fecharModalImpacto() {
  mostrarModalImpacto.value = false;
}
</script>

<style>
.atividade-edicao-input {
  flex-grow: 1;
  min-width: 0;
}

.atividade-card {
  transition: box-shadow 0.2s;
}

.atividade-card:hover {
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.07);
}

.botoes-acao-atividade,
.botoes-acao {
  opacity: 0;
  pointer-events: none;
  transition: opacity 0.2s;
}

.atividade-hover-row:hover .botoes-acao-atividade,
.conhecimento-hover-row:hover .botoes-acao {
  opacity: 1;
  pointer-events: auto;
}

.botao-acao {
  width: 2rem;
  height: 2rem;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0;
  font-size: 1.1rem;
  border-width: 2px;
  transition: background 0.15s, border-color 0.15s, color 0.15s;
  margin-left: 0;
  margin-right: 0;
}

.botao-acao:focus,
.botao-acao:hover {
  background: var(--bs-primary-bg-subtle);
  box-shadow: 0 0 0 2px var(--bs-primary);
}

.fade-group {
  transition: opacity 0.2s;
}

.atividade-descricao {
  word-break: break-word;
  max-width: 100%;
  display: inline-block;
}

.conhecimento-hover-row:hover span {
  font-weight: bold;
}

.atividade-hover-row:hover .atividade-descricao {
  font-weight: bold;
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

</style>