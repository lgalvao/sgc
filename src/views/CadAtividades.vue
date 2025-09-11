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
      :key="atividade.id || idx"
      class="card mb-3 atividade-card"
    >
      <div class="card-body py-2">
        <div
          class="card-title d-flex align-items-center atividade-edicao-row position-relative group-atividade atividade-hover-row atividade-titulo-card"
        >
          <template v-if="editandoAtividade === atividade.id">
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
              @click="salvarEdicaoAtividade(atividade.id)"
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
                @click="iniciarEdicaoAtividade(atividade.id, atividade.descricao)"
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
            <template v-if="editandoConhecimento.idxAtividade === idx && editandoConhecimento.idxConhecimento === cidx">
              <input
                v-model="conhecimentoEditado"
                class="form-control form-control-sm me-2 conhecimento-edicao-input"
                data-testid="input-editar-conhecimento"
                style="max-width: 300px;"
              >
              <button
                class="btn btn-sm btn-outline-success me-1 botao-acao"
                data-bs-toggle="tooltip"
                data-testid="btn-salvar-edicao-conhecimento"
                title="Salvar"
                @click="salvarEdicaoConhecimento(idx, cidx)"
              >
                <i class="bi bi-save" />
              </button>
              <button
                class="btn btn-sm btn-outline-secondary botao-acao"
                data-bs-toggle="tooltip"
                data-testid="btn-cancelar-edicao-conhecimento"
                title="Cancelar"
                @click="cancelarEdicaoConhecimento"
              >
                <i class="bi bi-x" />
              </button>
            </template>
            <template v-else>
              <span data-testid="conhecimento-descricao">{{ conhecimento.descricao }}</span>
              <div class="d-inline-flex align-items-center gap-1 ms-3 botoes-acao fade-group">
                <button
                  class="btn btn-sm btn-outline-primary botao-acao"
                  data-bs-toggle="tooltip"
                  data-testid="btn-editar-conhecimento"
                  title="Editar"
                  @click="iniciarEdicaoConhecimento(idx, cidx, conhecimento.descricao)"
                >
                  <i
                    class="bi bi-pencil"
                  />
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
            </template>
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
                  :key="atividade.id"
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
                    :key="analise.id"
                  >
                    <td>{{ formatarData(analise.dataHora) }}</td>
                    <td>{{ analise.unidade }}</td>
                    <td>{{ analise.resultado }}</td>
                    <td>{{ analise.observacao || '-' }}</td>
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
  </div>
</template>

<script lang="ts" setup>
import {computed, onMounted, onUnmounted, ref, watch} from 'vue'
import {Modal} from 'bootstrap' // Importar Modal do Bootstrap
import {usePerfil} from '@/composables/usePerfil'
import {useAtividadesStore} from '@/stores/atividades'
import {useUnidadesStore} from '@/stores/unidades'
import {useProcessosStore} from '@/stores/processos'
import {TipoMudanca, useRevisaoStore} from '@/stores/revisao'
import {useMapasStore} from '@/stores/mapas'
import {useAlertasStore} from '@/stores/alertas'
import {useAnalisesStore} from '@/stores/analises'
import {Atividade, Perfil, Processo, SituacaoProcesso, Subprocesso, TipoProcesso, Unidade} from '@/types/tipos'
import {useNotificacoesStore} from '@/stores/notificacoes'
import {useRouter} from 'vue-router'
import ImpactoMapaModal from '@/components/ImpactoMapaModal.vue'
import ImportarAtividadesModal from '@/components/ImportarAtividadesModal.vue'

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
const revisaoStore = useRevisaoStore()
const alertasStore = useAlertasStore()
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

const siglaUnidade = computed(() => unidade.value?.sigla || unidadeId.value)

const nomeUnidade = computed(() => (unidade.value?.nome ? `${unidade.value.nome}` : ''))

const novaAtividade = ref('')

const idSubprocesso = computed(() => {
  const Subprocesso = (processosStore.subprocessos as Subprocesso[]).find(
      pu => pu.idProcesso === idProcesso.value && pu.unidade === unidadeId.value
  );
  return Subprocesso?.id;
});

const atividades = computed<AtividadeComEdicao[]>({
  get: () => {
    if (idSubprocesso.value === undefined) return []
    const storeAtividades = atividadesStore.getAtividadesPorSubprocesso(idSubprocesso.value) || []
    return storeAtividades.map((a: Atividade) => ({...a, novoConhecimento: ''}))
  },
  set: (val: AtividadeComEdicao[]) => {
    if (idSubprocesso.value === undefined) return
    const storeVal = val.map(a => {
      const {novoConhecimento: _, ...rest} = a
      return rest
    })
    atividadesStore.setAtividades(idSubprocesso.value, storeVal)
  }
})

const processoAtual = computed<Processo | null>(() => {
  if (!idSubprocesso.value) return null;
  return (processosStore.processos as Processo[]).find(p => p.id === idProcesso.value) || null;
});

const isRevisao = computed(() => processoAtual.value?.tipo === TipoProcesso.REVISAO);

function adicionarAtividade() {
  if (novaAtividade.value?.trim() && idSubprocesso.value !== undefined) {
    const novaAtividadeObj = {
      id: Date.now(),
      descricao: novaAtividade.value.trim(),
      idSubprocesso: idSubprocesso.value,
      conhecimentos: [],
    };
    atividadesStore.adicionarAtividade(novaAtividadeObj);
    revisaoStore.registrarMudanca({
      tipo: TipoMudanca.AtividadeAdicionada,
      idAtividade: novaAtividadeObj.id,
      descricaoAtividade: novaAtividadeObj.descricao,
    });
    verificarEAlterarSituacao();
    novaAtividade.value = '';
    notificacoesStore.sucesso('Atividade adicionada', 'A atividade foi adicionada com sucesso.');
  }
}

function removerAtividade(idx: number) {
  const atividadeRemovida = atividades.value[idx];
  if (confirm('Confirma a remoção desta atividade e todos os conhecimentos associados?')) {
    const idsImpactados = revisaoStore.obterIdsCompetenciasImpactadas(atividadeRemovida.id, siglaUnidade.value, idProcesso.value);
    atividadesStore.removerAtividade(atividadeRemovida.id);
    revisaoStore.registrarMudanca({
      tipo: TipoMudanca.AtividadeRemovida,
      idAtividade: atividadeRemovida.id,
      descricaoAtividade: atividadeRemovida.descricao,
      competenciasImpactadasIds: idsImpactados,
    });
    verificarEAlterarSituacao();
  }
}

function adicionarConhecimento(idx: number) {
  const atividade = atividades.value[idx];
  if (atividade.novoConhecimento?.trim()) {
    const novoConhecimentoObj = {
      id: Date.now(),
      descricao: atividade.novoConhecimento.trim()
    };
    const idsImpactados = revisaoStore.obterIdsCompetenciasImpactadas(atividade.id, siglaUnidade.value, idProcesso.value);
    atividadesStore.adicionarConhecimento(atividade.id, novoConhecimentoObj, idsImpactados);
    verificarEAlterarSituacao();
    atividade.novoConhecimento = '';
    notificacoesStore.sucesso('Conhecimento adicionado', 'O conhecimento foi adicionado com sucesso.');
  }
}

function removerConhecimento(idx: number, cidx: number) {
  const atividade = atividades.value[idx];
  const conhecimentoRemovido = atividade.conhecimentos[cidx];
  if (confirm('Confirma a remoção deste conhecimento?')) {
    const idsImpactados = revisaoStore.obterIdsCompetenciasImpactadas(atividade.id, siglaUnidade.value, idProcesso.value);
    atividadesStore.removerConhecimento(atividade.id, conhecimentoRemovido.id, idsImpactados);
    verificarEAlterarSituacao();
  }
}

const editandoConhecimento = ref<{ idxAtividade: number | null, idxConhecimento: number | null }>({
  idxAtividade: null,
  idxConhecimento: null
})
const conhecimentoEditado = ref('')

function iniciarEdicaoConhecimento(idxAtividade: number, idxConhecimento: number, valorAtual: string) {
  editandoConhecimento.value = {idxAtividade, idxConhecimento}
  conhecimentoEditado.value = valorAtual
}

function salvarEdicaoConhecimento(idxAtividade: number, idxConhecimento: number) {
  if (conhecimentoEditado.value) {
    const newAtividades = [...atividades.value];
    const newConhecimentos = [...newAtividades[idxAtividade].conhecimentos];
    const conhecimentoOriginal = newConhecimentos[idxConhecimento];
    const valorAntigo = conhecimentoOriginal ? conhecimentoOriginal.descricao : '';

    // Get impacted competencies before updating the store
    const idsImpactados = revisaoStore.obterIdsCompetenciasImpactadas(newAtividades[idxAtividade].id, siglaUnidade.value, idProcesso.value);

    newConhecimentos[idxConhecimento] = {...newConhecimentos[idxConhecimento], descricao: conhecimentoEditado.value};
    newAtividades[idxAtividade] = {...newAtividades[idxAtividade], conhecimentos: newConhecimentos};
    atividades.value = newAtividades;

    revisaoStore.registrarMudanca({
      tipo: TipoMudanca.ConhecimentoAlterado,
      idAtividade: newAtividades[idxAtividade].id,
      idConhecimento: newConhecimentos[idxConhecimento].id,
      descricaoAtividade: newAtividades[idxAtividade].descricao,
      descricaoConhecimento: conhecimentoEditado.value,
      valorAntigo: valorAntigo,
      valorNovo: conhecimentoEditado.value,
      competenciasImpactadasIds: idsImpactados,
    });


  }
  cancelarEdicaoConhecimento()
}

function cancelarEdicaoConhecimento() {
  editandoConhecimento.value = {idxAtividade: null, idxConhecimento: null}
  conhecimentoEditado.value = ''
}

const editandoAtividade = ref<number | null>(null)
const atividadeEditada = ref('')

function iniciarEdicaoAtividade(id: number, valorAtual: string) {
  editandoAtividade.value = id
  atividadeEditada.value = valorAtual
}

function salvarEdicaoAtividade(id: number) {
  if (String(atividadeEditada.value).trim()) {
    const newAtividades = [...atividades.value];
    const atividadeIndex = newAtividades.findIndex(a => a.id === id);
    if (atividadeIndex !== -1) {
      const atividadeOriginal = atividadesStore.atividades.find(a => a.id === id);
      const valorAntigo = atividadeOriginal ? atividadeOriginal.descricao : '';

      // Get impacted competencies before updating the store
      const idsImpactados = revisaoStore.obterIdsCompetenciasImpactadas(id, siglaUnidade.value, idProcesso.value);

      newAtividades[atividadeIndex].descricao = String(atividadeEditada.value);
      atividades.value = newAtividades;

      revisaoStore.registrarMudanca({
        tipo: TipoMudanca.AtividadeAlterada,
        idAtividade: id,
        descricaoAtividade: String(atividadeEditada.value),
        valorAntigo: valorAntigo,
        valorNovo: String(atividadeEditada.value),
        competenciasImpactadasIds: idsImpactados,
      });


    }
  }
  cancelarEdicaoAtividade()
}

function cancelarEdicaoAtividade() {
  editandoAtividade.value = null
  atividadeEditada.value = ''
}

function handleImportAtividades(atividadesImportadas: Atividade[]) {
  if (idSubprocesso.value === undefined) return;

  const novasAtividades = atividadesImportadas.map(atividade => ({
    ...atividade,
    idSubprocesso: idSubprocesso.value as number,
  }));

  atividadesStore.adicionarMultiplasAtividades(novasAtividades);


}

const {perfilSelecionado} = usePerfil()

const isChefe = computed(() => perfilSelecionado.value === Perfil.CHEFE)

const subprocesso = computed(() => {
  if (!idSubprocesso.value) return null;
  return (processosStore.subprocessos as Subprocesso[]).find(p => p.id === idSubprocesso.value);
});

const podeVerImpacto = computed(() => {
  if (!isChefe.value || !subprocesso.value) return false;
  return subprocesso.value.situacao === 'Revisão do cadastro em andamento';
});

// Variáveis reativas para o modal de importação
const processoSelecionado = ref<Processo | null>(null)
const processoSelecionadoId = ref<number | null>(null)
const unidadesParticipantes = ref<Subprocesso[]>([])
const unidadeSelecionada = ref<Subprocesso | null>(null)
const unidadeSelecionadaId = ref<number | null>(null)
const atividadesParaImportar = ref<Atividade[]>([])

const mostrarModalImpacto = ref(false)
const mostrarModalImportar = ref(false)
const mostrarModalConfirmacao = ref(false)
const mostrarModalHistorico = ref(false)
const atividadesSemConhecimento = ref<Atividade[]>([])

const modalElement = ref<HTMLElement | null>(null)
const cleanupBackdrop = () => {
  const backdrop = document.querySelector('.modal-backdrop');
  if (backdrop) {
    backdrop.remove();
  }
  document.body.classList.remove('modal-open');
  document.body.style.overflow = '';
  document.body.style.paddingRight = '';
};

onMounted(async () => {
  modalElement.value = document.getElementById('importarAtividadesModal');
  modalElement.value?.addEventListener('hidden.bs.modal', cleanupBackdrop);

  const processoAtual = processosStore.processos.find(p => p.id === idProcesso.value);
  if (processoAtual && processoAtual.tipo === TipoProcesso.REVISAO) {
    await atividadesStore.fetchAtividadesPorSubprocesso(idSubprocesso.value as number);
    const atividadesAtuais = atividadesStore.getAtividadesPorSubprocesso(idSubprocesso.value as number);
    atividadesStore.setAtividadesSnapshot(JSON.parse(JSON.stringify(atividadesAtuais)));
  }
});

onUnmounted(() => {
  modalElement.value?.removeEventListener('hidden.bs.modal', cleanupBackdrop);
  revisaoStore.limparMudancas();
});

watch(processoSelecionadoId, (newId) => {
  if (newId) {
    const processo = processosDisponiveis.value.find(p => p.id === newId)
    if (processo) {
      selecionarProcesso(processo)
    }
  } else {
    selecionarProcesso(null)
  }
})

watch(unidadeSelecionadaId, (newId) => {
  if (newId) {
    const unidade = unidadesParticipantes.value.find(u => u.id === newId)
    if (unidade) {
      selecionarUnidade(unidade)
    }
  } else {
    selecionarUnidade(null)
  }
})

// Computed property para processos disponíveis para importação
const processosDisponiveis = computed<Processo[]>(() => {
  return processosStore.processos.filter(p =>
      (p.tipo === TipoProcesso.MAPEAMENTO || p.tipo === TipoProcesso.REVISAO) && p.situacao === SituacaoProcesso.FINALIZADO
  )
})

// Funções do modal


function selecionarProcesso(processo: Processo | null) {
  processoSelecionado.value = processo
  unidadesParticipantes.value = processo ? processosStore.getUnidadesDoProcesso(processo.id) : []
  unidadeSelecionada.value = null // Reseta a unidade ao trocar de processo
  unidadeSelecionadaId.value = null
}

async function selecionarUnidade(unidadePu: Subprocesso | null) {
  unidadeSelecionada.value = unidadePu
  if (unidadePu) {
    await atividadesStore.fetchAtividadesPorSubprocesso(unidadePu.id)
    const atividadesDaOutraUnidade = atividadesStore.getAtividadesPorSubprocesso(unidadePu.id)
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

function formatarData(data: Date): string {
  return data.toLocaleString('pt-BR')
}

function abrirModalHistorico() {
  mostrarModalHistorico.value = true
}

function fecharModalHistorico() {
  mostrarModalHistorico.value = false
}

function disponibilizarCadastro() {
  // 1. Verificação da situação do subprocesso
  const subprocesso = (processosStore.subprocessos as Subprocesso[]).find(
      pu => pu.idProcesso === idProcesso.value && pu.unidade === unidadeId.value
  );

  if (!subprocesso || subprocesso.situacao !== 'Revisão do cadastro em andamento') {
    notificacoesStore.erro(
        'Erro na Disponibilização',
        'A disponibilização só pode ser feita quando o subprocesso está na situação "Revisão do cadastro em andamento".'
    );
    return;
  }

  // 2. Validação de atividades sem conhecimento
  atividadesSemConhecimento.value = validarAtividades();
  if (atividadesSemConhecimento.value.length > 0) {
    const atividadesDescricoes = atividadesSemConhecimento.value.map(a => `- ${a.descricao}`).join('\n');
    notificacoesStore.erro(
        'Atividades sem Conhecimento',
        `As seguintes atividades não têm conhecimentos associados e precisam ser ajustadas antes da disponibilização:\n${atividadesDescricoes}`
    );
    return;
  }

  mostrarModalConfirmacao.value = true;
}

const confirmacaoModalRef = ref<HTMLElement | null>(null); // Adicionar a referência

function fecharModalConfirmacao() {
  if (confirmacaoModalRef.value) {
    const modalInstance = Modal.getInstance(confirmacaoModalRef.value) || new Modal(confirmacaoModalRef.value);
    modalInstance.hide();
  }
  mostrarModalConfirmacao.value = false; // Manter para consistência do estado reativo
  atividadesSemConhecimento.value = [];
}

function obterUnidadeSuperior(): string | null {
  const buscarPai = (unidades: Unidade[], siglaFilha: string): string | null => {
    for (const unidade of unidades) {
      if (unidade.filhas && unidade.filhas.some(f => f.sigla === siglaFilha)) {
        return unidade.sigla;
      }
      const paiEncontradoEmFilhas = buscarPai(unidade.filhas, siglaFilha);
      if (paiEncontradoEmFilhas) {
        return paiEncontradoEmFilhas;
      }
    }
    return null;
  };

  const paiSigla = buscarPai(unidadesStore.unidades as Unidade[], siglaUnidade.value);
  return paiSigla || 'SEDOC'; // Retorna 'SEDOC' se não encontrar um pai (unidade raiz ou não encontrada)
}

async function confirmarDisponibilizacao() {
  if (!idSubprocesso.value) return;

  const isRevisao = processoAtual.value?.tipo === TipoProcesso.REVISAO;
  const unidadeSuperior = obterUnidadeSuperior();

  // Para processos de revisão, verificar se há impactos no mapa
  if (isRevisao && revisaoStore.mudancasRegistradas.length === 0) {
    notificacoesStore.aviso(
        'Atenção',
        'Não foram detectadas alterações no cadastro. Considere se realmente é necessário prosseguir com a revisão.'
    );
  }

  // Alterar situação do subprocesso
  const subprocessoIndex = processosStore.subprocessos.findIndex(pu => pu.id === idSubprocesso.value);
  if (subprocessoIndex !== -1) {
    processosStore.subprocessos[subprocessoIndex].situacao = isRevisao ? 'Revisão do cadastro disponibilizada' : 'Cadastro disponibilizado';

    // Definir data/hora de conclusão da etapa 1
    processosStore.subprocessos[subprocessoIndex].dataFimEtapa1 = new Date();
  }

  // Registrar movimentação
  if (unidadeSuperior) {
    processosStore.addMovement({
      idSubprocesso: idSubprocesso.value,
      unidadeOrigem: siglaUnidade.value,
      unidadeDestino: unidadeSuperior,
      descricao: isRevisao ? 'Disponibilização da revisão do cadastro de atividades' : 'Disponibilização do cadastro de atividades'
    });
  }

  // Enviar notificação por e-mail
  const assunto = isRevisao
      ? `SGC: Revisão do cadastro de atividades e conhecimentos disponibilizada: ${siglaUnidade.value}`
      : `SGC: Cadastro de atividades e conhecimentos disponibilizado: ${siglaUnidade.value}`;

  const corpo = isRevisao
      ? `A unidade ${siglaUnidade.value} concluiu a revisão e disponibilizou seu cadastro de atividades e conhecimentos do processo ${processoAtual.value?.descricao || 'N/A'}. A análise desse cadastro já pode ser realizada no sistema.`
      : `A unidade ${siglaUnidade.value} disponibilizou o cadastro de atividades e conhecimentos do processo ${processoAtual.value?.descricao || 'N/A'}. A análise desse cadastro já pode ser realizada no sistema.`;

  notificacoesStore.email(assunto, `Responsável pela ${unidadeSuperior}`, corpo);

  // Criar alerta
  alertasStore.criarAlerta({
    idProcesso: idProcesso.value,
    unidadeOrigem: siglaUnidade.value,
    unidadeDestino: unidadeSuperior || 'SEDOC',
    descricao: `Cadastro de atividades e conhecimentos da unidade ${siglaUnidade.value} disponibilizado para análise`,
    dataHora: new Date()
  });

  // Excluir o histórico de análise do subprocesso (CDU-09, item 15)
  if (idSubprocesso.value) {
    analisesStore.removerAnalisesPorSubprocesso(idSubprocesso.value);
  }

  // Adicionar mensagem de sucesso
  notificacoesStore.sucesso('Sucesso', 'Revisão do cadastro de atividades disponibilizada');

  fecharModalConfirmacao();
  await router.push('/painel');
}

function abrirModalImpacto() {
  if (revisaoStore.mudancasRegistradas.length === 0) {
    notificacoesStore.info("Impacto", 'Nenhum impacto no mapa da unidade.');
    return;
  }

  if (idProcesso.value && siglaUnidade.value) {
    revisaoStore.setMudancasParaImpacto(revisaoStore.mudancasRegistradas);
    mostrarModalImpacto.value = true;
  }
}

function fecharModalImpacto() {
  mostrarModalImpacto.value = false;
  revisaoStore.setMudancasParaImpacto([]);
}

function verificarEAlterarSituacao() {
  if (subprocesso.value?.situacao === 'Não iniciado' && idSubprocesso.value) {
    const novaSituacao = isRevisao.value ? 'Revisão do cadastro em andamento' : 'Cadastro em andamento';
    const subprocessoIndex = processosStore.subprocessos.findIndex(sp => sp.id === idSubprocesso.value);
    if (subprocessoIndex !== -1) {
      processosStore.subprocessos[subprocessoIndex].situacao = novaSituacao;
    }
  }
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

.group-atividade:hover .botoes-acao-atividade,
.group-conhecimento:hover .botoes-acao {
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
  background: #f0f4fa;
  box-shadow: 0 0 0 2px #e3f0ff;
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
  background: #f8fafc;
  border-bottom: 1px solid #e3e8ee;
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