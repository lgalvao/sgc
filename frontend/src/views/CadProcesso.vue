<template>
  <div class="container mt-4">
    <h2>Cadastro de processo</h2>

    <form class="mt-4 col-md-6 col-sm-8 col-12">
      <div class="mb-3">
        <label
            class="form-label"
            for="descricao"
        >Descrição</label>
        <input
            id="descricao"
            v-model="descricao"
            class="form-control"
            placeholder="Descreva o processo"
            type="text"
        >
      </div>

      <div class="mb-3">
        <label
            class="form-label"
            for="tipo"
        >Tipo</label>
        <select
            id="tipo"
            v-model="tipo"
            class="form-select"
        >
          <option
              v-for="tipoOption in TipoProcesso"
              :key="tipoOption"
              :value="tipoOption"
          >
            {{ tipoOption }}
          </option>
        </select>
      </div>

      <div class="mb-3">
        <label class="form-label">Unidades participantes</label>
        <div class="border rounded p-3">
          <div>
            <template
                v-for="unidade in unidadesStore.unidades"
                :key="unidade.sigla"
            >
              <div
                  :style="{ marginLeft: '0' }"
                  class="form-check"
              >
                <!--suppress HtmlUnknownAttribute -->
                <input
                    :id="`chk-${unidade.sigla}`"
                    :checked="getEstadoSelecao(unidade) === true"
                    class="form-check-input"
                    type="checkbox"
                    :indeterminate="getEstadoSelecao(unidade) === 'indeterminate'"
                    @change="() => toggleUnidade(unidade)"
                >
                <label
                    :for="`chk-${unidade.sigla}`"
                    class="form-check-label ms-2"
                >
                  <strong>{{ unidade.sigla }}</strong> - {{ unidade.nome }}
                </label>
              </div>
              <div
                  v-if="unidade.filhas && unidade.filhas.length"
                  class="ms-4"
              >
                <template
                    v-for="filha in unidade.filhas"
                    :key="filha.sigla"
                >
                  <div class="form-check">
                    <!--suppress HtmlUnknownAttribute -->
                    <input
                        :id="`chk-${filha.sigla}`"
                        :checked="getEstadoSelecao(filha) === true"
                        class="form-check-input"
                        type="checkbox"
                        :indeterminate="getEstadoSelecao(filha) === 'indeterminate'"
                        @change="() => toggleUnidade(filha)"
                    >
                    <label
                        :for="`chk-${filha.sigla}`"
                        class="form-check-label ms-2"
                    >
                      <strong>{{ filha.sigla }}</strong> - {{ filha.nome }}
                    </label>
                  </div>

                  <div
                      v-if="filha.filhas && filha.filhas.length"
                      class="ms-4"
                  >
                    <div
                        v-for="neta in filha.filhas"
                        :key="neta.sigla"
                        class="form-check"
                    >
                      <input
                          :id="`chk-${neta.sigla}`"
                          :checked="isChecked(neta.sigla)"
                          class="form-check-input"
                          type="checkbox"
                          @change="() => toggleUnidade(neta)"
                      >
                      <label
                          :for="`chk-${neta.sigla}`"
                          class="form-check-label ms-2"
                      >
                        <strong>{{ neta.sigla }}</strong> - {{ neta.nome }}
                      </label>
                    </div>
                  </div>
                </template>
              </div>
            </template>
          </div>
        </div>
      </div>

      <div class="mb-3">
        <label
            class="form-label"
            for="dataLimite"
        >Data limite</label>
        <input
            id="dataLimite"
            v-model="dataLimite"
            class="form-control"
            type="date"
        >
      </div>
      <button
          class="btn btn-primary"
          type="button"
          @click="salvarProcesso"
      >
        Salvar
      </button>
      <button
          class="btn btn-success ms-2"
          data-testid="btn-iniciar-processo"
          type="button"
          @click="abrirModalConfirmacao"
      >
        Iniciar processo
      </button>
      <button
          v-if="processoEditando"
          class="btn btn-danger ms-2"
          type="button"
          @click="abrirModalRemocao"
      >
        Remover
      </button>
      <router-link
          class="btn btn-secondary ms-2"
          to="/painel"
      >
        Cancelar
      </router-link>
    </form>

    <!-- Modal de confirmação CDU-05 -->
    <div
        v-if="mostrarModalConfirmacao"
        class="modal fade show"
        style="display: block;"
        tabindex="-1"
    >
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">
              Iniciar processo
            </h5>
            <button
                type="button"
                class="btn-close"
                @click="fecharModalConfirmacao"
            />
          </div>
          <div class="modal-body">
            <p>Ao iniciar o processo, não será mais possível editá-lo ou removê-lo e todas as unidades participantes
              serão notificadas por e-mail.</p>
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
                class="btn btn-primary"
                @click="confirmarIniciarProcesso"
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

    <!-- Modal de confirmação de remoção -->
    <div
        v-if="mostrarModalRemocao"
        class="modal fade show"
        style="display: block;"
        tabindex="-1"
    >
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">
              Iniciar processo
            </h5>
            <button
                type="button"
                class="btn-close"
                @click="fecharModalRemocao"
            />
          </div>
          <div class="modal-body">
            <p>Remover o processo '{{ descricao }}'? Esta ação não poderá ser desfeita.</p>
          </div>
          <div class="modal-footer">
            <button
                type="button"
                class="btn btn-secondary"
                @click="fecharModalRemocao"
            >
              Cancelar
            </button>
            <button
                type="button"
                class="btn btn-danger"
                @click="confirmarRemocao"
            >
              Remover
            </button>
          </div>
        </div>
      </div>
    </div>
    <div
        v-if="mostrarModalRemocao"
        class="modal-backdrop fade show"
    />
  </div>
</template>

<script lang="ts" setup>
import {onMounted, ref} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import {useProcessosStore} from '@/stores/processos'
import {useUnidadesStore} from '@/stores/unidades'
import {useMapasStore} from '@/stores/mapas'
import {useServidoresStore} from '@/stores/servidores'
import {useAlertasStore} from '@/stores/alertas'
import {AtualizarProcessoRequest, CriarProcessoRequest, Processo as ProcessoModel} from '../mappers/processos'
import {Unidade} from '../mappers/sgrh' // Reutilizando a interface Unidade do sgrh
import {useNotificacoesStore} from '@/stores/notificacoes'
import {TEXTOS} from '@/constants';
import * as processoService from '@/services/processoService';
import {TipoProcesso} from "@/types/tipos"; // Importar o serviço de processo

const unidadesSelecionadas = ref<number[]>([]) // Agora armazena o código da unidade
const descricao = ref<string>('')
const tipo = ref<string>('MAPEAMENTO') // Tipo agora é string, mapeado no backend
const dataLimite = ref<string>('')
const router = useRouter()
const route = useRoute()
const processosStore = useProcessosStore()
const unidadesStore = useUnidadesStore()
const mapasStore = useMapasStore()
const servidoresStore = useServidoresStore()
const alertasStore = useAlertasStore()
const notificacoesStore = useNotificacoesStore()
const mostrarModalConfirmacao = ref(false)
const mostrarModalRemocao = ref(false)
const processoEditando = ref<ProcessoModel | null>(null)

const tiposProcessoDisponiveis = ref<string[]>(['MAPEAMENTO', 'REVISAO', 'DIAGNOSTICO']);

// Carregar processo se estiver editando
onMounted(async () => {
  const idProcesso = route.query.idProcesso;
  if (idProcesso) {
    try {
      const processo = await processoService.obterProcessoPorId(Number(idProcesso));
      if (processo) {
        processoEditando.value = processo;
        descricao.value = processo.descricao;
        tipo.value = processo.tipo;
        dataLimite.value = processo.dataLimite.split('T')[0]; // Formatar para 'YYYY-MM-DD'

        // TODO: Carregar unidades participantes do processo detalhe
        // Por enquanto, vamos assumir que o processo retornado por obterProcessoPorId
        // não contém as unidades participantes para seleção na tela de edição.
        // Isso será ajustado quando usarmos obterDetalhesProcesso.
      }
    } catch (error) {
      notificacoesStore.erro('Erro ao carregar processo', 'Não foi possível carregar os detalhes do processo.');
      console.error('Erro ao carregar processo:', error);
    }
  }
})

function limparCampos() {
  descricao.value = ''
  tipo.value = 'MAPEAMENTO'
  dataLimite.value = ''
  unidadesSelecionadas.value = []
}

// TODO: Ajustar a lógica de validação de unidades para usar os códigos das unidades
function isUnidadeIntermediaria(codigo: number): boolean {
  // Lógica de verificação de unidade intermediária
  return false;
}

// TODO: Ajustar a lógica de validação de unidades para usar os códigos das unidades
function unidadeTemMapaVigente(codigo: number): boolean {
  // Lógica de verificação de mapa vigente
  return true;
}

// TODO: Ajustar a lógica de validação de unidades para usar os códigos das unidades
function unidadeTemServidores(codigo: number): boolean {
  // Lógica de verificação de servidores
  return true;
}

function validarUnidadesParaProcesso(tipoProcesso: string, unidadesSelecionadas: number[]): number[] {
  let unidadesValidas = unidadesSelecionadas.filter(codigo => !isUnidadeIntermediaria(codigo));

  if (tipoProcesso === 'REVISAO' || tipoProcesso === 'DIAGNOSTICO') {
    unidadesValidas = unidadesValidas.filter(codigo => unidadeTemMapaVigente(codigo));
  }

  if (tipoProcesso === 'DIAGNOSTICO') {
    unidadesValidas = unidadesValidas.filter(codigo => unidadeTemServidores(codigo));
  }

  return unidadesValidas;
}

async function salvarProcesso() {
  if (!descricao.value || !dataLimite.value || unidadesSelecionadas.value.length === 0) {
    notificacoesStore.erro(
        'Dados incompletos',
        'Preencha todos os campos e selecione ao menos uma unidade.'
    );
    return
  }

  const unidadesFiltradas = validarUnidadesParaProcesso(tipo.value, unidadesSelecionadas.value);

  if (unidadesFiltradas.length === 0) {
    notificacoesStore.erro(
        'Unidades inválidas',
        'Não é possível incluir em processos de revisão ou diagnóstico, unidades que ainda não passaram por processo de mapeamento.'
    );
    return
  }

  try {
    if (processoEditando.value) {
      // Editando processo existente
      const request: AtualizarProcessoRequest = {
        codigo: processoEditando.value.codigo,
        descricao: descricao.value,
        tipo: tipo.value,
        dataLimiteEtapa1: `${dataLimite.value}T00:00:00`, // Formato ISO
        unidades: unidadesFiltradas
      };
      await processoService.atualizarProcesso(processoEditando.value.codigo, request);

      notificacoesStore.sucesso(
          'Processo alterado',
          'O processo foi alterado com sucesso!'
      );
    } else {
      // Criando novo processo
      const request: CriarProcessoRequest = {
        descricao: descricao.value,
        tipo: tipo.value,
        dataLimiteEtapa1: `${dataLimite.value}T00:00:00`, // Formato ISO
        unidades: unidadesFiltradas
      };
      await processoService.criarProcesso(request);

      notificacoesStore.sucesso(
          'Processo salvo',
          'O processo foi salvo com sucesso!'
      );
    }
    router.push('/painel');
    limparCampos();
  } catch (error) {
    notificacoesStore.erro('Erro ao salvar processo', 'Não foi possível salvar o processo. Verifique os dados e tente novamente.');
    console.error('Erro ao salvar processo:', error);
  }
}

function abrirModalConfirmacao() {
  if (!descricao.value || !dataLimite.value || unidadesSelecionadas.value.length === 0) {
    notificacoesStore.erro(
        'Dados incompletos',
        'Preencha todos os campos e selecione ao menos uma unidade.'
    );
    return
  }

  const unidadesFiltradas = validarUnidadesParaProcesso(tipo.value, unidadesSelecionadas.value);

  if (unidadesFiltradas.length === 0) {
    notificacoesStore.erro(
        'Unidades inválidas',
        'Não é possível incluir em processos de revisão ou diagnóstico, unidades que ainda não passaram por processo de mapeamento.'
    );
    return
  }

  mostrarModalConfirmacao.value = true
}

function fecharModalConfirmacao() {
  mostrarModalConfirmacao.value = false
}

async function confirmarIniciarProcesso() {
  mostrarModalConfirmacao.value = false
  if (processoEditando.value) {
    try {
      // TODO: O endpoint de iniciar processo no backend espera um tipo e uma lista de unidades
      // Por enquanto, vamos chamar sem as unidades, mas isso precisa ser ajustado.
      await processoService.iniciarProcesso(processoEditando.value.codigo, tipo.value, unidadesSelecionadas.value);
      notificacoesStore.sucesso(
          'Processo iniciado',
          'O processo foi iniciado com sucesso! Notificações enviadas às unidades.'
      );
      router.push('/painel');
      limparCampos();
    } catch (error) {
      notificacoesStore.erro('Erro ao iniciar processo', 'Não foi possível iniciar o processo. Tente novamente.');
      console.error('Erro ao iniciar processo:', error);
    }
  } else {
    notificacoesStore.erro('Erro', 'Não é possível iniciar um processo não salvo.');
  }
}

function abrirModalRemocao() {
  mostrarModalRemocao.value = true
}

function fecharModalRemocao() {
  mostrarModalRemocao.value = false
}

async function confirmarRemocao() {
  if (processoEditando.value) {
    try {
      await processoService.excluirProcesso(processoEditando.value.codigo);
      notificacoesStore.adicionarNotificacao({
        tipo: 'success',
        titulo: 'Processo removido',
        mensagem: `${TEXTOS.PROCESSO_REMOVIDO_INICIO}${descricao.value}${TEXTOS.PROCESSO_REMOVIDO_FIM}`,
        testId: 'notificacao-remocao'
      });
      router.push('/painel');
    } catch (error) {
      notificacoesStore.erro('Erro ao remover processo', 'Não foi possível remover o processo. Tente novamente.');
      console.error('Erro ao remover processo:', error);
    }
  }
  fecharModalRemocao();
}

// Funções de manipulação de unidades (serão ajustadas em uma etapa posterior)
function getTodasSubunidades(unidade: Unidade): number[] {
  let subunidades: number[] = []
  // TODO: Implementar lógica para obter subunidades por código
  return subunidades
}

function isFolha(unidade: Unidade): boolean {
  // TODO: Implementar lógica para verificar se é folha
  return true;
}

function isChecked(codigo: number): boolean {
  return unidadesSelecionadas.value.includes(codigo)
}

function getEstadoSelecao(unidade: Unidade): boolean | 'indeterminate' {
  const selfSelected = isChecked(unidade.codigo)

  if (isFolha(unidade)) {
    return selfSelected
  }

  const subunidades = getTodasSubunidades(unidade)
  const selecionadas = subunidades.filter(codigo => isChecked(codigo)).length

  if (selecionadas === 0) {
    return selfSelected
  }
  if (selecionadas === subunidades.length) return true
  return 'indeterminate'
}

function toggleUnidade(unidade: Unidade) {
  // TODO: Implementar lógica de toggle de unidade por código
  const todasSubunidades = [unidade.codigo, ...getTodasSubunidades(unidade)]
  const todasEstaoSelecionadas = todasSubunidades.every(codigo => isChecked(codigo))

  if (todasEstaoSelecionadas) {
    unidadesSelecionadas.value = unidadesSelecionadas.value.filter(
        codigo => !todasSubunidades.includes(codigo)
    )
  } else {
    todasSubunidades.forEach(codigo => {
      if (!unidadesSelecionadas.value.includes(codigo)) {
        unidadesSelecionadas.value.push(codigo)
      }
    })
  }
}