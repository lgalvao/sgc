<template>
  <div class="container mt-4">

    <div class="fs-5 w-100 mb-3">
      {{ siglaUnidade }} - {{ nomeUnidade }}
    </div>

    <div class="d-flex justify-content-between align-items-center mb-3">
      <h1 class="mb-0 display-6">Atividades e conhecimentos</h1>

      <div class="d-flex gap-2">
        <button class="btn btn-outline-secondary" @click="abrirModalImpacto">
          <i class="bi bi-arrow-right-circle me-2"></i>Impacto no mapa
        </button>
        <button v-if="isChefe" class="btn btn-outline-primary" @click="mostrarModalImportar = true" title="Importar">
          Importar atividades
        </button>
        <button class="btn btn-outline-success" data-bs-toggle="tooltip" title="Disponibilizar"
                @click="disponibilizarCadastro">
          Disponibilizar
        </button>
      </div>
    </div>

    <!-- Adicionar atividade -->
    <form class="row g-2 align-items-center mb-4" @submit.prevent="adicionarAtividade">
      <div class="col">
        <input v-model="novaAtividade" class="form-control" data-testid="input-nova-atividade"
               placeholder="Nova atividade"
               type="text"/>
      </div>
      <div class="col-auto">
        <button class="btn btn-outline-primary btn-sm" data-bs-toggle="tooltip" data-testid="btn-adicionar-atividade"
                title="Adicionar atividade"
                type="submit"><i
            class="bi bi-save"></i></button>
      </div>
    </form>

    <!-- Lista de atividades -->
    <div v-for="(atividade, idx) in atividades" :key="atividade.id || idx" class="card mb-3 atividade-card">
      <div class="card-body py-2">
        <div
            class="card-title d-flex align-items-center atividade-edicao-row position-relative group-atividade atividade-hover-row atividade-titulo-card">
          <template v-if="editandoAtividade === atividade.id">
            <input v-model="atividadeEditada" class="form-control me-2 atividade-edicao-input"
                   data-testid="input-editar-atividade"/>
            <button class="btn btn-sm btn-outline-success me-1 botao-acao" data-bs-toggle="tooltip"
                    data-testid="btn-salvar-edicao-atividade"
                    title="Salvar" @click="salvarEdicaoAtividade(atividade.id)"><i class="bi bi-save"></i>
            </button>
            <button class="btn btn-sm btn-outline-secondary botao-acao" data-bs-toggle="tooltip"
                    data-testid="btn-cancelar-edicao-atividade"
                    title="Cancelar" @click="cancelarEdicaoAtividade()"><i class="bi bi-x"></i>
            </button>
          </template>

          <template v-else>
            <strong class="atividade-descricao" data-testid="atividade-descricao">{{ atividade.descricao }}</strong>
            <div class="d-inline-flex align-items-center gap-1 ms-3 botoes-acao-atividade fade-group">
              <button class="btn btn-sm btn-outline-primary botao-acao"
                      data-bs-toggle="tooltip" data-testid="btn-editar-atividade"
                      title="Editar" @click="iniciarEdicaoAtividade(atividade.id, atividade.descricao)"><i
                  class="bi bi-pencil"></i></button>
              <button class="btn btn-sm btn-outline-danger botao-acao" data-bs-toggle="tooltip"
                      data-testid="btn-remover-atividade"
                      title="Remover" @click="removerAtividade(idx)"><i
                  class="bi bi-trash"></i></button>
            </div>
          </template>
        </div>

        <!-- Conhecimentos da atividade -->
        <div class="mt-3 ms-3">
          <div v-for="(conhecimento, cidx) in atividade.conhecimentos" :key="conhecimento.id"
               class="d-flex align-items-center mb-2 group-conhecimento position-relative conhecimento-hover-row">
            <template v-if="editandoConhecimento.idxAtividade === idx && editandoConhecimento.idxConhecimento === cidx">
              <input v-model="conhecimentoEditado" class="form-control form-control-sm me-2 conhecimento-edicao-input"
                     data-testid="input-editar-conhecimento" style="max-width: 300px;"/>
              <button class="btn btn-sm btn-outline-success me-1 botao-acao"
                      data-bs-toggle="tooltip"
                      data-testid="btn-salvar-edicao-conhecimento"
                      title="Salvar" @click="salvarEdicaoConhecimento(idx, cidx)"><i class="bi bi-save"></i>
              </button>
              <button class="btn btn-sm btn-outline-secondary botao-acao" data-bs-toggle="tooltip"
                      data-testid="btn-cancelar-edicao-conhecimento"
                      title="Cancelar" @click="cancelarEdicaoConhecimento"><i class="bi bi-x"></i>
              </button>
            </template>
            <template v-else>
              <span data-testid="conhecimento-descricao">{{ conhecimento.descricao }}</span>
              <div class="d-inline-flex align-items-center gap-1 ms-3 botoes-acao fade-group">
                <button class="btn btn-sm btn-outline-primary botao-acao"
                        data-bs-toggle="tooltip"
                        data-testid="btn-editar-conhecimento" title="Editar"
                        @click="iniciarEdicaoConhecimento(idx, cidx, conhecimento.descricao)"><i
                    class="bi bi-pencil"></i></button>
                <button class="btn btn-sm btn-outline-danger botao-acao" data-bs-toggle="tooltip"
                        data-testid="btn-remover-conhecimento"
                        title="Remover" @click="removerConhecimento(idx, cidx)"><i class="bi bi-trash"></i>
                </button>
              </div>
            </template>
          </div>
          <form class="row g-2 align-items-center" @submit.prevent="adicionarConhecimento(idx)">
            <div class="col">
              <input v-model="atividade.novoConhecimento" class="form-control form-control-sm"
                     data-testid="input-novo-conhecimento"
                     placeholder="Novo conhecimento" type="text"/>
            </div>
            <div class="col-auto">
              <button class="btn btn-outline-secondary btn-sm" data-bs-toggle="tooltip"
                      data-testid="btn-adicionar-conhecimento"
                      title="Adicionar Conhecimento" type="submit"><i
                  class="bi bi-save"></i></button>
            </div>
          </form>
        </div>
      </div>
    </div>

    <!-- Modais -->
    <ImportarAtividadesModal
        :mostrar="mostrarModalImportar"
        @fechar="mostrarModalImportar = false"
        @importar="handleImportAtividades"/>

    <ImpactoMapaModal
        :id-processo="idProcesso"
        :mostrar="mostrarModalImpacto"
        :sigla-unidade="siglaUnidade"
        @fechar="fecharModalImpacto"/>
  </div>
</template>

<script lang="ts" setup>
import {computed, onMounted, onUnmounted, ref, watch} from 'vue'

import {usePerfil} from '@/composables/usePerfil'
import {useAtividadesStore} from '@/stores/atividades'
import {useUnidadesStore} from '@/stores/unidades'
import {useProcessosStore} from '@/stores/processos'
import {TipoMudanca, useRevisaoStore} from '@/stores/revisao'
import {useMapasStore} from '@/stores/mapas'
import {Atividade, Perfil, Processo, SituacaoProcesso, Subprocesso, TipoProcesso, Unidade} from '@/types/tipos'
import {useNotificacoesStore} from '@/stores/notificacoes'
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
const mapasStore = useMapasStore()
const notificacoesStore = useNotificacoesStore()

// Helper function to get impacted competency IDs
function getImpactedCompetencyIds(atividadeId: number): number[] {
  const impactedIds: number[] = [];
  const currentMap = mapasStore.getMapaByUnidadeId(siglaUnidade.value, idProcesso.value);

  if (currentMap) {
    currentMap.competencias.forEach(comp => {
      if (comp.atividadesAssociadas.includes(atividadeId)) {
        impactedIds.push(comp.id);
      }
    });
  }
  return impactedIds;
}

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

function adicionarAtividade() {
  console.log('CadAtividades: Função adicionarAtividade() executada!'); // Este log aparece
  console.log('CadAtividades: novaAtividade.value:', novaAtividade.value); // Adicionado para depuração
  console.log('CadAtividades: idSubprocesso.value:', idSubprocesso.value); // Adicionado para depuração

  if (novaAtividade.value && idSubprocesso.value !== undefined) {
    const novaAtividadeObj = {
      id: Date.now(),
      descricao: novaAtividade.value,
      idSubprocesso: idSubprocesso.value,
      conhecimentos: [],
    };
    atividadesStore.adicionarAtividade(novaAtividadeObj);
    revisaoStore.registrarMudanca({
      tipo: TipoMudanca.AtividadeAdicionada,
      idAtividade: novaAtividadeObj.id,
      descricaoAtividade: novaAtividadeObj.descricao,
    });
    console.log('CadAtividades: Após registrar mudança:', revisaoStore.mudancasRegistradas); // Adicionado para depuração

    // Notificação de sucesso
    notificacoesStore.sucesso(
        'Atividade adicionada',
        `A atividade "${novaAtividade.value}" foi adicionada com sucesso!`
    );

    novaAtividade.value = '';
  }
}

function removerAtividade(idx: number) {
  const atividadeRemovida = atividades.value[idx];
  const impactedCompetencyIds = getImpactedCompetencyIds(atividadeRemovida.id);
  atividadesStore.removerAtividade(atividadeRemovida.id);
  revisaoStore.registrarMudanca({
    tipo: TipoMudanca.AtividadeRemovida,
    idAtividade: atividadeRemovida.id,
    descricaoAtividade: atividadeRemovida.descricao,
    competenciasImpactadasIds: impactedCompetencyIds,
  });

  // Notificação de sucesso
  notificacoesStore.sucesso(
      'Atividade removida',
      `A atividade "${atividadeRemovida.descricao}" foi removida com sucesso!`
  );
}

function adicionarConhecimento(idx: number) {
  const atividade = atividades.value[idx];
  if (atividade.novoConhecimento?.trim()) {
    const novoConhecimentoObj = {
      id: Date.now(),
      descricao: atividade.novoConhecimento
    };
    const impactedCompetencyIds = getImpactedCompetencyIds(atividade.id);
    atividadesStore.adicionarConhecimento(atividade.id, novoConhecimentoObj, impactedCompetencyIds);

    // Notificação de sucesso
    notificacoesStore.sucesso(
        'Conhecimento adicionado',
        `O conhecimento "${atividade.novoConhecimento}" foi adicionado com sucesso!`
    );

    // Limpar o campo
    atividade.novoConhecimento = '';
  }
}

function removerConhecimento(idx: number, cidx: number) {
  const atividade = atividades.value[idx];
  const conhecimentoRemovido = atividade.conhecimentos[cidx];
  const impactedCompetencyIds = getImpactedCompetencyIds(atividade.id);
  atividadesStore.removerConhecimento(atividade.id, conhecimentoRemovido.id, impactedCompetencyIds);

  // Notificação de sucesso
  notificacoesStore.sucesso(
      'Conhecimento removido',
      `O conhecimento "${conhecimentoRemovido.descricao}" foi removido com sucesso!`
  );
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
    const impactedCompetencyIds = getImpactedCompetencyIds(newAtividades[idxAtividade].id);

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
      competenciasImpactadasIds: impactedCompetencyIds,
    });

    // Notificação de sucesso
    notificacoesStore.sucesso(
        'Conhecimento editado',
        `O conhecimento foi alterado para "${conhecimentoEditado.value}" com sucesso!`
    );
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
      const impactedCompetencyIds = getImpactedCompetencyIds(id);

      newAtividades[atividadeIndex].descricao = String(atividadeEditada.value);
      atividades.value = newAtividades;

      revisaoStore.registrarMudanca({
        tipo: TipoMudanca.AtividadeAlterada,
        idAtividade: id,
        descricaoAtividade: String(atividadeEditada.value),
        valorAntigo: valorAntigo,
        valorNovo: String(atividadeEditada.value),
        competenciasImpactadasIds: impactedCompetencyIds,
      });

      // Notificação de sucesso
      notificacoesStore.sucesso(
          'Atividade editada',
          `A atividade foi alterada para "${atividadeEditada.value}" com sucesso!`
      );
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

  // Notificação de sucesso
  notificacoesStore.sucesso(
      'Atividades importadas',
      `${novasAtividades.length} atividade(s) foi(ram) importada(s) com sucesso!`
  );
}

const {perfilSelecionado} = usePerfil()

const isChefe = computed(() => perfilSelecionado.value === Perfil.CHEFE)

// Variáveis reativas para o modal de importação
const processoSelecionado = ref<Processo | null>(null)
const processoSelecionadoId = ref<number | null>(null)
const unidadesParticipantes = ref<Subprocesso[]>([])
const unidadeSelecionada = ref<Subprocesso | null>(null)
const unidadeSelecionadaId = ref<number | null>(null)
const atividadesParaImportar = ref<Atividade[]>([])

const mostrarModalImpacto = ref(false)
const mostrarModalImportar = ref(false)

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


function disponibilizarCadastro() {
  // Simulação de disponibilização do cadastro
  // Em um sistema real, isso enviaria os dados para validação

  notificacoesStore.sucesso(
      'Cadastro disponibilizado',
      'O cadastro de atividades e conhecimentos foi disponibilizado para validação com sucesso!'
  );
}

function abrirModalImpacto() {
  if (idProcesso.value && siglaUnidade.value) {
    revisaoStore.setMudancasParaImpacto(revisaoStore.mudancasRegistradas);
    mostrarModalImpacto.value = true;
  }
}

function fecharModalImpacto() {
  mostrarModalImpacto.value = false;
  revisaoStore.setMudancasParaImpacto([]);
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