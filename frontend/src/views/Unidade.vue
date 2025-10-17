<template>
  <div class="container mt-4">
    <div class="d-flex justify-content-between align-items-center mb-3">
      <div>
        <button
          v-if="perfilStore.perfilSelecionado === 'ADMIN'"
          class="btn btn-outline-primary"
          data-testid="btn-criar-atribuicao"
          @click="irParaCriarAtribuicao"
        >
          Criar atribuição
        </button>
      </div>
    </div>

    <div
      v-if="unidadeComResponsavelDinamico"
      class="card mb-4"
    >
      <div class="card-body">
        <h2 class="display-6 mb-3">
          {{ unidadeComResponsavelDinamico.sigla }} - {{
            unidadeComResponsavelDinamico.nome
          }}
        </h2>
        <p><strong>Titular:</strong> {{ titularDetalhes?.nome }}</p>
        <p class="ms-3">
          <i class="bi bi-telephone-fill me-2" />{{ titularDetalhes?.ramal }}
          <i class="bi bi-envelope-fill ms-3 me-2" />{{ titularDetalhes?.email }}
        </p>
        <template
          v-if="unidadeComResponsavelDinamico.responsavel &&
            unidadeComResponsavelDinamico.responsavel.idServidor &&
            unidadeComResponsavelDinamico.responsavel.idServidor !== unidadeComResponsavelDinamico.idServidorTitular"
        >
          <p><strong>Responsável:</strong> {{ responsavelDetalhes?.nome }}</p>
          <p class="ms-3">
            <i class="bi bi-telephone-fill me-2" />{{ responsavelDetalhes?.ramal }}
            <i class="bi bi-envelope-fill ms-3 me-2" />{{ responsavelDetalhes?.email }}
          </p>
        </template>
        <button
          v-if="mapaVigente"
          class="btn btn-outline-success"
          @click="visualizarMapa"
        >
          <i
            class="bi bi-file-earmark-spreadsheet me-2"
          />Mapa vigente
        </button>
      </div>
    </div>
    <div v-else>
      <p>Unidade não encontrada.</p>
    </div>

    <div
      v-if="unidadeComResponsavelDinamico && unidadeComResponsavelDinamico.filhas && unidadeComResponsavelDinamico.filhas.length > 0"
      class="mt-5"
    >
      <TreeTable
        :columns="colunasTabela"
        :data="dadosFormatadosSubordinadas"
        :hide-headers="true"
        title="Unidades Subordinadas"
        @row-click="navegarParaUnidadeSubordinada"
      />
    </div>
  </div>
</template>

<script lang="ts" setup>
import {computed} from 'vue'
import {useRouter} from 'vue-router'
import {useUnidadesStore} from '@/stores/unidades'
import {usePerfilStore} from '@/stores/perfil'
import {useServidoresStore} from '@/stores/servidores'
import {useMapasStore} from '@/stores/mapas'
import TreeTable from '../components/TreeTable.vue'
import {Mapa, Servidor, TipoResponsabilidade, Unidade} from '@/types/tipos';
import {useAtribuicaoTemporariaStore} from '@/stores/atribuicoes'

const props = defineProps<{ siglaUnidade: string }>();

const router = useRouter()
const sigla = computed(() => props.siglaUnidade)
const unidadesStore = useUnidadesStore()
const perfilStore = usePerfilStore()
const servidoresStore = useServidoresStore()
const mapasStore = useMapasStore()
const atribuicaoTemporariaStore = useAtribuicaoTemporariaStore() // Instanciar

const unidadeOriginal = computed<Unidade | null>(() => unidadesStore.pesquisarUnidade(sigla.value) || null)

const unidadeComResponsavelDinamico = computed<Unidade | null>(() => {
  const unidade = unidadeOriginal.value;
  if (!unidade) return null;

  const atribuicoes = atribuicaoTemporariaStore.getAtribuicoesPorUnidade(unidade.sigla);
  const hoje = new Date();

  // Encontrar a atribuição temporária vigente
  const atribuicaoVigente = atribuicoes.find(atrb => {
    const dataInicio = new Date(atrb.dataInicio);
    const dataTermino = new Date(atrb.dataTermino);
    return hoje >= dataInicio && hoje <= dataTermino;
  });

  if (atribuicaoVigente) {
    // Retorna uma nova unidade com o responsável da atribuição temporária
    return {
      ...unidade,
      responsavel: atribuicaoVigente.servidor
    };
  }

  // Se não houver atribuição temporária vigente, retorna a unidade original
  return unidade;
});
const titularDetalhes = computed<Servidor | null>(() => {
  if (unidadeOriginal.value && unidadeOriginal.value.idServidorTitular) {
    return servidoresStore.getServidorById(unidadeOriginal.value.idServidorTitular) || null;
  }
  return null;
});

const responsavelDetalhes = computed<Servidor | null>(() => {
  if (!unidadeComResponsavelDinamico.value || !unidadeComResponsavelDinamico.value.responsavel || !unidadeComResponsavelDinamico.value.responsavel.codigo) {
    return null;
  }
  return servidoresStore.getServidorById(unidadeComResponsavelDinamico.value.responsavel.codigo) || null;
});

const mapaVigente = computed<Mapa | undefined>(() => mapasStore.getMapaVigentePorUnidade(sigla.value))

function irParaCriarAtribuicao() {
  router.push({path: `/unidade/${sigla.value}/atribuicao`})
}

const dadosFormatadosSubordinadas = computed(() => {
  if (!unidadeComResponsavelDinamico.value || !unidadeComResponsavelDinamico.value.filhas || unidadeComResponsavelDinamico.value.filhas.length === 0) return []
  return formatarDadosParaArvore(unidadeComResponsavelDinamico.value.filhas)
})

const colunasTabela = [{key: 'nome', label: 'Unidade'}]

interface UnidadeFormatada {
  id: string;
  nome: string;
  expanded: boolean;
  children?: UnidadeFormatada[];
}

function formatarDadosParaArvore(dados: Unidade[]): UnidadeFormatada[] {
  if (!dados) return []

  return dados.map(item => {
    const children = item.filhas ? formatarDadosParaArvore(item.filhas) : []
    return {
      id: item.sigla,
      nome: item.sigla + ' - ' + item.nome,
      expanded: true,
      ...(children.length > 0 && {children})
    }
  })
}

function navegarParaUnidadeSubordinada(item: { id: unknown }) {
  if (item && typeof item.id === 'string') router.push({path: `/unidade/${item.id}`});
}


function visualizarMapa() {
  if (mapaVigente.value) {
    router.push({
      name: 'SubprocessoVisMapa',
      params: {
        idProcesso: mapaVigente.value.idProcesso,
        siglaUnidade: sigla.value
      }
    });
  }
}

</script>