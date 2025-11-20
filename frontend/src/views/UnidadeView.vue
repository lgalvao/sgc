<template>
  <BContainer class="mt-4">
    <div class="d-flex justify-content-between align-items-center mb-3">
      <div>
        <BButton
          v-if="perfilStore.perfilSelecionado === 'ADMIN'"
          variant="outline-primary"
          data-testid="btn-criar-atribuicao"
          @click="irParaCriarAtribuicao"
        >
          Criar atribuição
        </BButton>
      </div>
    </div>

    <BCard
      v-if="unidadeComResponsavelDinamico"
      class="mb-4"
    >
      <BCardBody>
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
            unidadeComResponsavelDinamico.responsavel.codigo &&
            unidadeComResponsavelDinamico.responsavel.codigo !== unidadeComResponsavelDinamico.idServidorTitular"
        >
          <p><strong>Responsável:</strong> {{ responsavelDetalhes?.nome }}</p>
          <p class="ms-3">
            <i class="bi bi-telephone-fill me-2" />{{ responsavelDetalhes?.ramal }}
            <i class="bi bi-envelope-fill ms-3 me-2" />{{ responsavelDetalhes?.email }}
          </p>
        </template>
        <BButton
          v-if="mapaVigente"
          variant="outline-success"
          @click="visualizarMapa"
        >
          <i
            class="bi bi-file-earmark-spreadsheet me-2"
          />Mapa vigente
        </BButton>
      </BCardBody>
    </BCard>
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
  </BContainer>
</template>

<script lang="ts" setup>
import {computed, onMounted} from 'vue'
import {useRouter} from 'vue-router'
import {useUnidadesStore} from '@/stores/unidades'
import {usePerfilStore} from '@/stores/perfil'
import {useUsuariosStore} from '@/stores/usuarios'
import {useMapasStore} from '@/stores/mapas'
import TreeTable from '../components/TreeTableView.vue'
import {MapaCompleto, Usuario, Unidade, Responsavel} from '@/types/tipos';
import {useAtribuicaoTemporariaStore} from '@/stores/atribuicoes'
import {
  BContainer,
  BButton,
  BCard,
  BCardBody
} from 'bootstrap-vue-next';

const props = defineProps<{ codUnidade: number }>();

const router = useRouter()
const codigo = computed(() => props.codUnidade)
const unidadesStore = useUnidadesStore()
const perfilStore = usePerfilStore()
const usuariosStore = useUsuariosStore()
const mapasStore = useMapasStore()
const atribuicaoTemporariaStore = useAtribuicaoTemporariaStore()

onMounted(async () => {
  await unidadesStore.fetchUnidadesParaProcesso('');
});

const unidadeOriginal = computed<Unidade | null>(() => unidadesStore.pesquisarUnidadePorCodigo(codigo.value) || null)

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
    const responsavel: Responsavel = {
      codigo: atribuicaoVigente.servidor.codigo,
      nome: atribuicaoVigente.servidor.nome,
      tituloEleitoral: atribuicaoVigente.servidor.tituloEleitoral,
      unidade: atribuicaoVigente.servidor.unidade,
      email: atribuicaoVigente.servidor.email,
      ramal: atribuicaoVigente.servidor.ramal,
      usuarioTitulo: atribuicaoVigente.servidor.nome,
      unidadeCodigo: atribuicaoVigente.unidade.codigo,
      idServidor: atribuicaoVigente.servidor.codigo,
      tipo: 'TEMPORARIO',
      dataInicio: atribuicaoVigente.dataInicio,
      dataFim: atribuicaoVigente.dataFim,
    };
    // Retorna uma nova unidade com o responsável da atribuição temporária
    return {
      ...unidade,
      responsavel,
    };
  }

  // Se não houver atribuição temporária vigente, retorna a unidade original
  return unidade;
});
const titularDetalhes = computed<Usuario | null>(() => {
  if (unidadeOriginal.value && unidadeOriginal.value.idServidorTitular) {
    return usuariosStore.getUsuarioById(unidadeOriginal.value.idServidorTitular) || null;
  }
  return null;
});

const responsavelDetalhes = computed<Usuario | null>(() => {
  if (!unidadeComResponsavelDinamico.value || !unidadeComResponsavelDinamico.value.responsavel || !unidadeComResponsavelDinamico.value.responsavel.codigo) {
    return null;
  }
  return usuariosStore.getUsuarioById(unidadeComResponsavelDinamico.value.responsavel.codigo) || null;
});

const mapaVigente = computed<MapaCompleto | null>(() => {
  return mapasStore.mapaCompleto;
})

function irParaCriarAtribuicao() {
  router.push({path: `/unidade/${codigo.value}/atribuicao`})
}

const dadosFormatadosSubordinadas = computed(() => {
  if (!unidadeComResponsavelDinamico.value || !unidadeComResponsavelDinamico.value.filhas || unidadeComResponsavelDinamico.value.filhas.length === 0) return []
  return formatarDadosParaArvore(unidadeComResponsavelDinamico.value.filhas)
})

const colunasTabela = [{key: 'nome', label: 'Unidade'}]

interface UnidadeFormatada {
  id: number;
  nome: string;
  expanded: boolean;
  children?: UnidadeFormatada[];
}

function formatarDadosParaArvore(dados: Unidade[]): UnidadeFormatada[] {
  if (!dados) return []

  return dados.map(item => {
    const children = item.filhas ? formatarDadosParaArvore(item.filhas) : []
    return {
      id: item.codigo,
      nome: item.sigla + ' - ' + item.nome,
      expanded: true,
      ...(children.length > 0 && {children})
    }
  })
}

function navegarParaUnidadeSubordinada(item: { id: any }) {
  if (item && typeof item.id === 'number') router.push({path: `/unidade/${item.id}`});
}


function visualizarMapa() {
  if (mapaVigente.value && unidadeOriginal.value) {
    router.push({
      name: 'SubprocessoVisMapa',
      params: {
        codProcesso: mapaVigente.value.subprocessoCodigo,
        siglaUnidade: unidadeOriginal.value.sigla
      }
    });
  }
}

</script>