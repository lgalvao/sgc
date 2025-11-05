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
          data-testid="input-descricao"
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
          data-testid="select-tipo"
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
          <ArvoreUnidades
            v-if="!unidadesStore.isLoading"
            v-model="unidadesSelecionadas"
            :unidades="unidadesStore.unidades"
            :desabilitadas="unidadesBloqueadas"
            :filtrarPor="unidadeElegivel"
          />
          <div v-else class="text-center py-3">
            <span class="spinner-border spinner-border-sm me-2"></span>
            Carregando unidades...
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
          data-testid="input-dataLimite"
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
            <p><strong>Descrição:</strong> {{ descricao }}</p>
            <p><strong>Tipo:</strong> {{ tipo }}</p>
            <p><strong>Unidades selecionadas:</strong> {{ unidadesSelecionadas.length }}</p>
            <hr>
            <p>
              Ao iniciar o processo, não será mais possível editá-lo ou removê-lo e todas as unidades participantes
              serão notificadas por e-mail.
            </p>
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
import {onMounted, ref, watch} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import {useProcessosStore} from '@/stores/processos'
import {useUnidadesStore} from '@/stores/unidades'
import {
  type AtualizarProcessoRequest,
  type CriarProcessoRequest,
  type Processo as ProcessoModel,
  TipoProcesso,
  type Unidade
} from '@/types/tipos'
import {useNotificacoesStore} from '@/stores/notificacoes'
import {TEXTOS} from '@/constants';
import * as processoService from '@/services/processoService';
import * as mapaService from '@/services/mapaService';
import {ServidoresService} from "@/services/servidoresService";
import ArvoreUnidades from '@/components/ArvoreUnidades.vue';

const unidadesSelecionadas = ref<number[]>([]) // Agora armazena o código da unidade
const descricao = ref<string>('')
const tipo = ref<string>('MAPEAMENTO') // Tipo agora é string, mapeado no backend
const dataLimite = ref<string>('')
const unidadesBloqueadas = ref<number[]>([]) // Unidades que já participam de processos ativos
const unidadesComMapaVigente = ref<number[]>([])
const unidadesComServidores = ref<number[]>([])
const router = useRouter()
const route = useRoute()
const processosStore = useProcessosStore()
const unidadesStore = useUnidadesStore()
const notificacoesStore = useNotificacoesStore()
const mostrarModalConfirmacao = ref(false)
const mostrarModalRemocao = ref(false)
const processoEditando = ref<ProcessoModel | null>(null)

/**
 * Extrai recursivamente todos os códigos de unidades de uma árvore hierárquica
 * @param unidades Array de UnidadeParticipante (pode ter filhos)
 * @returns Array com todos os códigos de unidades (raiz + filhos + netos...)
 */
function extrairCodigosUnidades(unidades: any[]): number[] {
  const codigos: number[] = [];
  for (const unidade of unidades) {
    codigos.push(unidade.codUnidade);
    if (unidade.filhos && unidade.filhos.length > 0) {
      codigos.push(...extrairCodigosUnidades(unidade.filhos));
    }
  }
  return codigos;
}

// Carregar processo se estiver editando
onMounted(async () => {
  // CRÍTICO: Carregar unidades primeiro
  await unidadesStore.fetchUnidades();
  console.log('[DEBUG Vue] Unidades da store carregadas:', unidadesStore.unidades.length);
  
  const codProcesso = route.query.codProcesso;
  if (codProcesso) {
    try {
      console.log('[DEBUG Vue] Carregando processo:', codProcesso);
      await processosStore.fetchProcessoDetalhe(Number(codProcesso));
      const processo = processosStore.processoDetalhe; // Obter o processo detalhado da store
      console.log('[DEBUG Vue] Processo carregado:', processo);
      if (processo) {
        processoEditando.value = processo;
        descricao.value = processo.descricao;
        tipo.value = processo.tipo;
        dataLimite.value = processo.dataLimite.split('T')[0]; // Formatar para 'YYYY-MM-DD'

        // Carregar unidades participantes do processo detalhe
        console.log('[DEBUG Vue] processo.unidades:', processo.unidades);
        // CORRIGIDO: extrair recursivamente todos os códigos (raiz + filhos + netos)
        unidadesSelecionadas.value = extrairCodigosUnidades(processo.unidades);
        console.log('[DEBUG Vue] unidadesSelecionadas após extração:', unidadesSelecionadas.value);
      }
    } catch (error) {
      notificacoesStore.erro('Erro ao carregar processo', 'Não foi possível carregar os detalhes do processo.');
      console.error('Erro ao carregar processo:', error);
    }
  }
})

// Buscar unidades bloqueadas e validar unidades quando o tipo de processo mudar
watch(tipo, async (novoTipo) => {
  try {
    const response = await fetch(`http://localhost:10000/api/processos/unidades-bloqueadas?tipo=${novoTipo}`);
    if (response.ok) {
      unidadesBloqueadas.value = await response.json();
      console.log('[DEBUG Vue] Unidades bloqueadas para tipo', novoTipo, ':', unidadesBloqueadas.value);
    }
  } catch (error) {
    console.error('Erro ao buscar unidades bloqueadas:', error);
  }
  
  // Carregar validações específicas por tipo
  await carregarUnidadesValidas(novoTipo);
}, { immediate: true });

// Função para carregar unidades válidas baseado no tipo de processo
async function carregarUnidadesValidas(tipoProcesso: string) {
  const todasUnidades = extrairTodasUnidadesCodigos(unidadesStore.unidades);
  
  if (tipoProcesso === 'REVISAO' || tipoProcesso === 'DIAGNOSTICO') {
    const resultados = await Promise.all(
      todasUnidades.map(async (codigo) => ({
        codigo,
        temMapa: await mapaService.verificarMapaVigente(codigo)
      }))
    );
    unidadesComMapaVigente.value = resultados.filter(r => r.temMapa).map(r => r.codigo);
    console.log('[DEBUG Vue] Unidades com mapa vigente:', unidadesComMapaVigente.value);
  } else {
    unidadesComMapaVigente.value = todasUnidades;
  }
  
  if (tipoProcesso === 'DIAGNOSTICO') {
    const resultados = await Promise.all(
      todasUnidades.map(async (codigo) => ({
        codigo,
        temServidores: await unidadeTemServidores(codigo)
      }))
    );
    unidadesComServidores.value = resultados.filter(r => r.temServidores).map(r => r.codigo);
    console.log('[DEBUG Vue] Unidades com servidores:', unidadesComServidores.value);
  } else {
    unidadesComServidores.value = todasUnidades;
  }
}

// Extrai todos os códigos de unidades da árvore (incluindo filhas)
function extrairTodasUnidadesCodigos(unidades: Unidade[]): number[] {
  const codigos: number[] = [];
  for (const unidade of unidades) {
    if (unidade.tipo !== 'INTERMEDIARIA') {
      codigos.push(unidade.codigo);
    }
    if (unidade.filhas && unidade.filhas.length > 0) {
      codigos.push(...extrairTodasUnidadesCodigos(unidade.filhas));
    }
  }
  return codigos;
}

// Verifica se a unidade deve estar visível/habilitada baseado no tipo de processo
function isUnidadeValida(codigo: number | undefined): boolean {
  if (codigo === undefined) {
    return false;
  }
  
  const tipoAtual = tipo.value;
  
  if (tipoAtual === 'REVISAO' || tipoAtual === 'DIAGNOSTICO') {
    if (!unidadesComMapaVigente.value.includes(codigo)) {
      return false;
    }
  }
  
  if (tipoAtual === 'DIAGNOSTICO') {
    if (!unidadesComServidores.value.includes(codigo)) {
      return false;
    }
  }
  
  return true;
}

// Verifica se a unidade deve estar desabilitada
function isUnidadeDesabilitada(codigo: number): boolean {
  // Desabilita apenas se estiver bloqueada (já em processo ativo)
  // Unidades intermediárias e não-elegíveis ficam habilitadas para selecionar filhas em grupo
  return isUnidadeBloqueada(codigo);
}

function isUnidadeBloqueada(codigo: number): boolean {
  // Não bloquear se estamos editando e a unidade já estava selecionada
  if (processoEditando.value && unidadesSelecionadas.value.includes(codigo)) {
    return false;
  }
  return unidadesBloqueadas.value.includes(codigo);
}

function limparCampos() {
  descricao.value = ''
  tipo.value = 'MAPEAMENTO'
  dataLimite.value = ''
  unidadesSelecionadas.value = []
}

function isUnidadeIntermediaria(codigo: number): boolean {
  const unidade = encontrarUnidadeRecursiva(codigo, unidadesStore.unidades);
  return unidade?.tipo === 'INTERMEDIARIA';
}

function encontrarUnidadeRecursiva(codigo: number, unidades: Unidade[]): Unidade | null {
  console.log('[DEBUG Vue] encontrarUnidadeRecursiva - codigo:', codigo, 'unidades:', unidades?.length);
  for (const unidade of unidades) {
    if (unidade.codigo === codigo) {
      console.log('[DEBUG Vue] Unidade encontrada:', unidade);
      return unidade;
    }
    if (unidade.filhas && unidade.filhas.length > 0) {
      const encontrada = encontrarUnidadeRecursiva(codigo, unidade.filhas);
      if (encontrada) {
        return encontrada;
      }
    }
  }
  console.log('[DEBUG Vue] Unidade não encontrada para codigo:', codigo);
  return null;
}

async function unidadeTemMapaVigente(codigo: number): Promise<boolean> {
  return await mapaService.verificarMapaVigente(codigo);
}

async function unidadeTemServidores(codigo: number): Promise<boolean> {
  try {
    const servidores = await ServidoresService.buscarServidoresPorUnidade(codigo);
    return servidores.length > 0;
  } catch (error) {
    console.warn(`[DEBUG Vue] Não foi possível verificar servidores para unidade ${codigo}:`, error);
    return false;
  }
}

// Validação simplificada - apenas filtra unidades intermediárias
// A validação de mapa vigente e servidores já está sendo feita ao exibir a árvore
async function validarUnidadesParaProcesso(tipoProcesso: string, unidadesSelecionadas: number[]): Promise<number[]> {
  console.log('[DEBUG Vue] validarUnidadesParaProcesso - tipos:', { tipoProcesso, unidadesSelecionadas });
  console.log('[DEBUG Vue] unidadesStore.unidades:', unidadesStore.unidades);
  
  const unidadesValidas = unidadesSelecionadas.filter(codigo => {
    const isIntermediaria = isUnidadeIntermediaria(codigo);
    console.log(`[DEBUG Vue] Unidade ${codigo} - intermediária: ${isIntermediaria}`);
    return !isIntermediaria;
  });
  
  console.log('[DEBUG Vue] Retornando unidadesValidas:', unidadesValidas);
  return unidadesValidas;
}

async function salvarProcesso() {
  console.log('[DEBUG Vue] salvarProcesso chamado');
  console.log('[DEBUG Vue] Dados atuais:', {
    descricao: descricao.value,
    tipo: tipo.value,
    dataLimite: dataLimite.value,
    unidades: unidadesSelecionadas.value.length,
    unidadesSelecionadas: unidadesSelecionadas.value
  });
  console.log('[DEBUG Vue] unidadesStore.unidades.length:', unidadesStore.unidades.length);
  console.log('[DEBUG Vue] unidadesStore.unidades:', unidadesStore.unidades);
  
  if (!descricao.value || !dataLimite.value || unidadesSelecionadas.value.length === 0) {
    notificacoesStore.erro(
        'Dados incompletos',
        'Preencha todos os campos e selecione ao menos uma unidade.'
    );
    return
  }

  try {
    console.log('[DEBUG Vue] Iniciando validação de unidades...');
    const unidadesFiltradas = await validarUnidadesParaProcesso(tipo.value, unidadesSelecionadas.value);
    console.log('[DEBUG Vue] Unidades filtradas:', unidadesFiltradas);

    if (unidadesFiltradas.length === 0) {
      console.warn('[DEBUG Vue] Nenhuma unidade válida após validação');
      notificacoesStore.erro(
          'Unidades inválidas',
          'Não é possível incluir em processos de revisão ou diagnóstico, unidades que ainda não passaram por processo de mapeamento.'
      );
      return
    }

    console.log('[DEBUG Vue] Prosseguindo com salvamento, processEditando:', !!processoEditando.value);
    
    if (processoEditando.value) {
      // Editando processo existente
      const request: AtualizarProcessoRequest = {
        codigo: processoEditando.value.codigo,
        descricao: descricao.value,
        tipo: tipo.value as TipoProcesso,
        dataLimiteEtapa1: `${dataLimite.value}T00:00:00`, // Formato ISO
        unidades: unidadesFiltradas
      };
      console.log('[DEBUG Vue] Atualizando processo:', request);
      await processoService.atualizarProcesso(processoEditando.value.codigo, request);
      console.log('[DEBUG Vue] Processo atualizado com sucesso');

      notificacoesStore.sucesso(
          'Processo alterado',
          'O processo foi alterado com sucesso!'
      );
    } else {
      // Criando novo processo
      const request: CriarProcessoRequest = {
        descricao: descricao.value,
        tipo: tipo.value as TipoProcesso,
        dataLimiteEtapa1: `${dataLimite.value}T00:00:00`, // Formato ISO
        unidades: unidadesFiltradas
      };
      console.log('[DEBUG Vue] Criando novo processo:', request);
      await processoService.criarProcesso(request);
      console.log('[DEBUG Vue] Novo processo criado com sucesso');

      notificacoesStore.sucesso(
          'Processo salvo',
          'O processo foi salvo com sucesso!'
      );
    }
    console.log('[DEBUG Vue] Redirecionando para painel...');
    await router.push('/painel');
    limparCampos();
  } catch (error) {
    console.error('[DEBUG Vue] Erro capturado:', error);
    notificacoesStore.erro('Erro ao salvar processo', 'Não foi possível salvar o processo. Verifique os dados e tente novamente.');
    console.error('Erro ao salvar processo:', error);
  }
}

async function abrirModalConfirmacao() {
  if (!descricao.value || !dataLimite.value || unidadesSelecionadas.value.length === 0) {
    notificacoesStore.erro(
        'Dados incompletos',
        'Preencha todos os campos e selecione ao menos uma unidade.'
    );
    return
  }
  const unidadesFiltradas = await validarUnidadesParaProcesso(tipo.value, unidadesSelecionadas.value);

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
  mostrarModalConfirmacao.value = false;
  if (processoEditando.value) {
    try {
      // Usa a action da store, passando os parâmetros necessários
      await processosStore.iniciarProcesso(
          processoEditando.value.codigo,
          tipo.value as TipoProcesso, // Garante a tipagem correta
          unidadesSelecionadas.value
      );
      notificacoesStore.sucesso(
          'Processo iniciado',
          'O processo foi iniciado com sucesso! Notificações enviadas às unidades.'
      );
      await router.push('/painel');
      limparCampos();
    } catch (error) {
      notificacoesStore.erro('Erro ao iniciar processo', 'Não foi possível iniciar o processo. Tente novamente.');
      console.error('Erro ao iniciar processo:', error);
    }
  } else {
    notificacoesStore.erro('Salve o processo', 'Você precisa salvar o processo antes de poder iniciá-lo.');
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
      await router.push('/painel');
    } catch (error) {
      notificacoesStore.erro('Erro ao remover processo', 'Não foi possível remover o processo. Tente novamente.');
      console.error('Erro ao remover processo:', error);
    }
  }
  fecharModalRemocao();
}

// Função para determinar se unidade é elegível para o processo
function unidadeElegivel(unidade: Unidade): boolean {
  // Unidades intermediárias nunca são elegíveis diretamente
  if (unidade.tipo === 'INTERMEDIARIA') {
    return false;
  }
  
  // Se estamos editando e a unidade já está selecionada, sempre mostrar
  if (processoEditando.value && unidadesSelecionadas.value.includes(unidade.codigo)) {
    return true;
  }
  
  // Para REVISAO e DIAGNOSTICO: apenas unidades com mapa vigente
  if (tipo.value === 'REVISAO' || tipo.value === 'DIAGNOSTICO') {
    return unidadesComMapaVigente.value.includes(unidade.codigo);
  }
  
  // Para MAPEAMENTO: unidades que não estejam em outro processo ativo
  return !unidadesBloqueadas.value.includes(unidade.codigo);
}

// Função auxiliar para verificar se unidade ou suas filhas são elegíveis
function temFilhasElegiveis(unidade: Unidade): boolean {
  if (unidadeElegivel(unidade)) {
    return true;
  }
  
  // Se a unidade não é elegível, verificar filhas
  if (unidade.filhas && unidade.filhas.length > 0) {
    return unidade.filhas.some(f => temFilhasElegiveis(f));
  }
  
  return false;
}

</script>