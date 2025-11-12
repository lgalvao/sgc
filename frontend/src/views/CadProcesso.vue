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
            :desabilitadas="unidadesDesabilitadas"
            :filtrar-por="unidadeElegivel"
          />
          <div
            v-else
            class="text-center py-3"
          >
            <span class="spinner-border spinner-border-sm me-2" />
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
import {computed, nextTick, onMounted, ref, watch} from 'vue'
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
import { buscarUsuariosPorUnidade } from "@/services/usuarioService";
import ArvoreUnidades from '@/components/ArvoreUnidades.vue';

const unidadesSelecionadas = ref<number[]>([])
const descricao = ref<string>('')
const tipo = ref<string>('MAPEAMENTO')
const dataLimite = ref<string>('')
const unidadesBloqueadas = ref<number[]>([])
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

onMounted(async () => {
  await unidadesStore.fetchUnidades();
  await carregarUnidadesValidas(tipo.value);

  const codProcesso = route.query.codProcesso;
  if (codProcesso) {
    try {
      await processosStore.fetchProcessoDetalhe(Number(codProcesso));
      const processo = processosStore.processoDetalhe;
      if (processo) {
        processoEditando.value = processo;
        descricao.value = processo.descricao;
        tipo.value = processo.tipo;
        dataLimite.value = processo.dataLimite.split('T')[0];

        await carregarUnidadesValidas(processo.tipo);

        unidadesSelecionadas.value = extrairCodigosUnidades(processo.unidades);

        await nextTick();
      }
    } catch (error) {
      notificacoesStore.erro('Erro ao carregar processo', 'Não foi possível carregar os detalhes do processo.');
      console.error('Erro ao carregar processo:', error);
    }
  }
})

watch(tipo, async (novoTipo) => {
  if (unidadesStore.unidades.length === 0) {
    return;
  }

  try {
    const response = await fetch(`http://localhost:10000/api/processos/unidades-bloqueadas?tipo=${novoTipo}`);
    if (response.ok) {
      unidadesBloqueadas.value = await response.json();
    }
  } catch (error) {
    console.error('Erro ao buscar unidades bloqueadas:', error);
  }

  await carregarUnidadesValidas(novoTipo);
});

// TODO: Esta função executa múltiplas chamadas de API em um laço, o que é ineficiente.
// A lógica de validação de unidades (verificar mapa vigente, existência de servidores)
// deve ser movida para um único endpoint no backend que receba a lista de unidades
// e retorne apenas as que são válidas para o tipo de processo.
async function carregarUnidadesValidas(tipoProcesso: string) {
  const todasUnidades = extrairTodasUnidadesCodigos(unidadesStore.unidades);

  // Para MAPEAMENTO, todas as unidades são elegíveis por padrão.
  if (tipoProcesso === 'MAPEAMENTO') {
    unidadesComMapaVigente.value = todasUnidades;
    unidadesComServidores.value = todasUnidades;
    return;
  }

  // Para REVISAO e DIAGNOSTICO, precisamos verificar o mapa vigente.
  const mapaChecks = todasUnidades.map(codigo =>
    unidadeTemMapaVigente(codigo).then(temMapa => ({ codigo, temMapa }))
  );
  const mapaResultados = await Promise.all(mapaChecks);
  unidadesComMapaVigente.value = mapaResultados.filter(r => r.temMapa).map(r => r.codigo);

  // Para DIAGNOSTICO, precisamos verificar também os servidores.
  if (tipoProcesso === 'DIAGNOSTICO') {
    const servidorChecks = todasUnidades.map(codigo =>
      unidadeTemServidores(codigo).then(temServidores => ({ codigo, temServidores }))
    );
    const servidorResultados = await Promise.all(servidorChecks);
    unidadesComServidores.value = servidorResultados.filter(r => r.temServidores).map(r => r.codigo);
  } else {
    // Para REVISAO, não é necessário verificar servidores.
    unidadesComServidores.value = todasUnidades;
  }
}

// Extrai todos os códigos de unidades da árvore (incluindo filhas)
function extrairTodasUnidadesCodigos(unidades: Unidade[]): number[] {
  const codigos: number[] = [];
  for (const unidade of unidades) {
    codigos.push(unidade.codigo);
    if (unidade.filhas && unidade.filhas.length > 0) {
      codigos.push(...extrairTodasUnidadesCodigos(unidade.filhas));
    }
  }
  return codigos;
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
  for (const unidade of unidades) {
    if (unidade.codigo === codigo) {
      return unidade;
    }
    if (unidade.filhas && unidade.filhas.length > 0) {
      const encontrada = encontrarUnidadeRecursiva(codigo, unidade.filhas);
      if (encontrada) {
        return encontrada;
      }
    }
  }
  return null;
}

async function unidadeTemMapaVigente(codigo: number): Promise<boolean> {
  return await mapaService.verificarMapaVigente(codigo);
}

async function unidadeTemServidores(codigo: number): Promise<boolean> {
  try {
    const usuarios = await buscarUsuariosPorUnidade(codigo);
    return usuarios.length > 0;
  } catch (error) {
    console.warn(`[DEBUG Vue] Não foi possível verificar usuários para unidade ${codigo}:`, error);
    return false;
  }
}

async function validarUnidadesParaProcesso(tipoProcesso: string, unidadesSelecionadas: number[]): Promise<number[]> {
  const unidadesValidas = unidadesSelecionadas.filter(codigo => {
    const isIntermediaria = isUnidadeIntermediaria(codigo);
    return !isIntermediaria;
  });

  return unidadesValidas;
}

async function salvarProcesso() {
  if (!descricao.value || !dataLimite.value || unidadesSelecionadas.value.length === 0) {
    notificacoesStore.erro(
        'Dados incompletos',
        'Preencha todos os campos e selecione ao menos uma unidade.'
    );
    return;
  }

  try {
    const unidadesFiltradas = await validarUnidadesParaProcesso(tipo.value, unidadesSelecionadas.value);

    if (unidadesFiltradas.length === 0) {
      notificacoesStore.erro(
          'Unidades inválidas',
          'Não é possível incluir em processos de revisão ou diagnóstico, unidades que ainda não passaram por processo de mapeamento.'
      );
      return;
    }

    if (processoEditando.value) {
      const request: AtualizarProcessoRequest = {
        codigo: processoEditando.value.codigo,
        descricao: descricao.value,
        tipo: tipo.value as TipoProcesso,
        dataLimiteEtapa1: `${dataLimite.value}T00:00:00`,
        unidades: unidadesFiltradas
      };
      await processosStore.atualizarProcesso(processoEditando.value.codigo, request);
      notificacoesStore.sucesso('Processo alterado', 'O processo foi alterado!');
      await router.push('/painel');
    } else {
      const request: CriarProcessoRequest = {
        descricao: descricao.value,
        tipo: tipo.value as TipoProcesso,
        dataLimiteEtapa1: `${dataLimite.value}T00:00:00`,
        unidades: unidadesFiltradas
      };
      const novoProcesso = await processosStore.criarProcesso(request);
      notificacoesStore.sucesso('Processo salvo', 'O processo foi salvo!');
      await router.push(`/processo/${novoProcesso.codigo}`);
    }
    limparCampos();
  } catch (error) {
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
  if (!processoEditando.value) {
    notificacoesStore.erro('Salve o processo', 'Você precisa salvar o processo antes de poder iniciá-lo.');
    return;
  }

  try {
    await processosStore.iniciarProcesso(
        processoEditando.value.codigo,
        tipo.value as TipoProcesso,
        unidadesSelecionadas.value
    );
    notificacoesStore.sucesso(
        'Processo iniciado',
        'O processo foi iniciado! Notificações enviadas às unidades.'
    );
    await router.push('/painel');
    limparCampos();
  } catch (error) {
    notificacoesStore.erro('Erro ao iniciar processo', 'Não foi possível iniciar o processo. Tente novamente.');
    console.error('Erro ao iniciar processo:', error);
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

const unidadesStatus = computed(() => {
  const todasUnidades = extrairTodasUnidadesCodigos(unidadesStore.unidades);
  const desabilitadas: number[] = [];
  const elegiveis: number[] = [];
  const unidadesProtegidas = processoEditando.value ? unidadesSelecionadas.value : [];

  for (const codigo of todasUnidades) {
    let isElegivel = true;
    let isDesabilitada = false;

    if (unidadesProtegidas.includes(codigo)) {
      elegiveis.push(codigo);
      continue;
    }

    if (unidadesBloqueadas.value.includes(codigo)) {
      isElegivel = false;
      isDesabilitada = true;
    }

    if (tipo.value === 'REVISAO' || tipo.value === 'DIAGNOSTICO') {
      if (!unidadesComMapaVigente.value.includes(codigo)) {
        isElegivel = false;
        isDesabilitada = true;
      }
    }

    if (tipo.value === 'DIAGNOSTICO') {
      if (!unidadesComServidores.value.includes(codigo)) {
        isElegivel = false;
        isDesabilitada = true;
      }
    }

    if (isElegivel) {
      elegiveis.push(codigo);
    }
    if (isDesabilitada) {
      desabilitadas.push(codigo);
    }
  }

  return {
    desabilitadas,
    elegiveis
  };
});

const unidadesDesabilitadas = computed(() => unidadesStatus.value.desabilitadas);

function unidadeElegivel(unidade: Unidade): boolean {
  return unidadesStatus.value.elegiveis.includes(unidade.codigo);
}

defineExpose({
  unidadesSelecionadas,
  unidadesBloqueadas,
  unidadesComMapaVigente,
  unidadesComServidores,
  unidadesStatus,
  unidadeElegivel,
});

</script>
