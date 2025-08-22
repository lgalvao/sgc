<template>
  <div class="container mt-4">
    <div v-if="unidadeComResponsavelDinamico" class="card mb-4">
      <div class="card-body">
        <p class="text-muted small mb-1">Processo: {{ processoAtual?.descricao }}</p>
        <h2 class="display-6 mb-3">{{ unidadeComResponsavelDinamico.sigla }} - {{
            unidadeComResponsavelDinamico.nome
          }}</h2>
        <p>
          <span class="fw-bold me-1">Situação:</span>
          <span :class="badgeClass(situacaoUnidadeNoProcesso)" class="badge">{{ situacaoUnidadeNoProcesso }}</span>
        </p>

        <p><strong>Titular:</strong> {{ titularDetalhes?.nome }}</p>
        <p class="ms-3">
          <i class="bi bi-telephone-fill me-2"></i>{{ titularDetalhes?.ramal }}
          <i class="bi bi-envelope-fill ms-3 me-2"></i>{{ titularDetalhes?.email }}
        </p>

        <template
            v-if="unidadeComResponsavelDinamico.responsavel && unidadeComResponsavelDinamico.responsavel.idServidorResponsavel && unidadeComResponsavelDinamico.responsavel.idServidorResponsavel !== unidadeComResponsavelDinamico.idServidorTitular">
          <p><strong>Responsável:</strong> {{ responsavelDetalhes?.nome }}</p>
          <p class="ms-3">
            <i class="bi bi-telephone-fill me-2"></i>{{ responsavelDetalhes?.ramal }}
            <i class="bi bi-envelope-fill ms-3 me-2"></i>{{ responsavelDetalhes?.email }}
          </p>
        </template>

        <p v-if="SubprocessoDetalhes">
          <strong>Unidade atual:</strong> {{ SubprocessoDetalhes.unidadeAtual || 'Não informado' }}
        </p>
      </div>
    </div>
    <div v-else>
      <p>Unidade não encontrada.</p>
    </div>

    <div class="row">
      <template v-if="processoAtual?.tipo === TipoProcesso.MAPEAMENTO || processoAtual?.tipo === TipoProcesso.REVISAO">
        <section class="col-md-4 mb-3">
          <div class="card h-100 card-actionable" @click="irParaAtividadesConhecimentos">
            <div class="card-body">
              <h5 class="card-title">Atividades e conhecimentos</h5>
              <p class="card-text text-muted">Cadastro de atividades e conhecimentos da unidade</p>
              <div class="d-flex justify-content-between align-items-center">
                <span class="badge bg-secondary">Cadastro disponibilizado</span>
              </div>
            </div>
          </div>
        </section>

        <section class="col-md-4 mb-3">
          <div class="card h-100 card-actionable" :class="{ 'disabled-card': !mapa }" @click="navegarParaMapa()">
            <div class="card-body">
              <h5 class="card-title">Mapa de Competências</h5>
              <p class="card-text text-muted">Mapa de competências técnicas da unidade</p>
              <div v-if="mapa">
                <div v-if="mapa.situacao === 'em_andamento'">
                  <span class="badge bg-warning text-dark">Em andamento</span>
                </div>
                <div v-else-if="mapa.situacao === 'disponivel_validacao'">
                  <span class="badge bg-success">Disponibilizado</span>
                </div>
              </div>
              <div v-else>
                <span class="badge bg-secondary">Não disponibilizado</span>
              </div>
            </div>
          </div>
        </section>
      </template>

      <template v-else-if="processoAtual?.tipo === TipoProcesso.DIAGNOSTICO">
        <section class="col-md-4 mb-3">
          <div class="card h-100 card-actionable">
            <div class="card-body">
              <h5 class="card-title">Diagnóstico da Equipe</h5>
              <p class="card-text text-muted">Diagnóstico das competências pelos servidores da unidade</p>
              <div class="d-flex justify-content-between align-items-center">
                <span class="badge bg-secondary">Não disponibilizado</span>
              </div>
            </div>
          </div>
        </section>

        <section class="col-md-4 mb-3">
          <div class="card h-100 card-actionable">
            <div class="card-body">
              <h5 class="card-title">Ocupações Críticas</h5>
              <p class="card-text text-muted">Identificação das ocupações críticas da unidade</p>
              <div class="d-flex justify-content-between align-items-center">
                <span class="badge bg-secondary">Não disponibilizado</span>
              </div>
            </div>
          </div>
        </section>
      </template>

    </div>
  </div>
</template>

<script lang="ts" setup>
import {computed} from 'vue'
import {useRouter} from 'vue-router'
import {storeToRefs} from 'pinia'
import {useUnidadesStore} from '@/stores/unidades'
import {useAtribuicaoTemporariaStore} from '@/stores/atribuicaoTemporaria'
import {useMapasStore} from '@/stores/mapas'
import {useServidoresStore} from '@/stores/servidores'
import {useProcessosStore} from '@/stores/processos'
import {usePerfilStore} from '@/stores/perfil' // Importar usePerfilStore
import {Mapa, Processo, Servidor, Subprocesso, TipoProcesso, TipoResponsabilidade, Unidade} from "@/types/tipos";

const props = defineProps<{ idProcesso: number; siglaUnidade: string }>();

const router = useRouter()
const idProcesso = computed(() => props.idProcesso)
const siglaParam = computed<string | undefined>(() => props.siglaUnidade)
const unidadesStore = useUnidadesStore()
const atribuicaoTemporariaStore = useAtribuicaoTemporariaStore(); // Instanciar
const mapaStore = useMapasStore()
const servidoresStore = useServidoresStore()
const processosStore = useProcessosStore()
const perfilStore = usePerfilStore() // Instanciar perfilStore
const {processos} = storeToRefs(processosStore)


const SubprocessoDetalhes = computed<Subprocesso | undefined>(() => {
  return processosStore.getUnidadesDoProcesso(idProcesso.value).find((pu: Subprocesso) => pu.unidade === siglaParam.value);
});

const processoAtual = computed<Processo | null>(() => {
  if (!SubprocessoDetalhes.value) return null;
  return (processos.value as Processo[]).find(p => p.id === SubprocessoDetalhes.value?.idProcesso) || null;
});

const sigla = computed<string | undefined>(() => {
  return SubprocessoDetalhes.value?.unidade;
});

const unidadeOriginal = computed<Unidade | null>(() => {
  if (!sigla.value) {

    return null;
  }
  const unidadeEncontrada = unidadesStore.pesquisarUnidade(sigla.value);

  if (unidadeEncontrada && unidadeEncontrada.sigla && unidadeEncontrada.nome) {
    return unidadeEncontrada as Unidade;
  }

  return null;
});

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
      responsavel: {
        idServidorResponsavel: atribuicaoVigente.idServidor,
        tipo: TipoResponsabilidade.ATRIBUICAO, // Usar o enum
        dataInicio: new Date(atribuicaoVigente.dataInicio),
        dataFim: new Date(atribuicaoVigente.dataTermino),
      }
    };
  }

  // Se não houver atribuição temporária vigente, retorna a unidade original
  return unidade;
});

const titularDetalhes = computed<Servidor | null>(() => {
  if (unidadeComResponsavelDinamico.value && unidadeComResponsavelDinamico.value.idServidorTitular) {
    return servidoresStore.getServidorById(unidadeComResponsavelDinamico.value.idServidorTitular) || null;
  }
  return null;
});

const responsavelDetalhes = computed<Servidor | null>(() => {
  if (!unidadeComResponsavelDinamico.value || !unidadeComResponsavelDinamico.value.responsavel || !unidadeComResponsavelDinamico.value.responsavel.idServidorResponsavel) {
    return null;
  }
  return servidoresStore.getServidorById(unidadeComResponsavelDinamico.value.responsavel.idServidorResponsavel) || null;
});

const situacaoUnidadeNoProcesso = computed<string>(() => {
  return SubprocessoDetalhes.value?.situacao || 'Não informado';
});

const mapa = computed<Mapa | null>(() => {
  if (!unidadeComResponsavelDinamico.value || !processoAtual.value) return null;
  return mapaStore.getMapaByUnidadeId(unidadeComResponsavelDinamico.value.sigla, processoAtual.value.id) || null;
});

function badgeClass(situacao: string): string {
  if (situacao === 'Aguardando' || situacao === 'Em andamento' || situacao === 'Aguardando validação') return 'bg-warning text-dark'
  if (situacao === 'Finalizado' || situacao === 'Validado') return 'bg-success'
  if (situacao === 'Devolvido') return 'bg-danger'
  return 'bg-secondary'
}

function navegarParaMapa() {
  if (!sigla.value || !processoAtual.value || !mapa.value) {
    return;
  }

  const params = {idProcesso: processoAtual.value.id, siglaUnidade: sigla.value};

  if (perfilStore.perfilSelecionado === 'ADMIN') {
    router.push({name: 'SubprocessoMapa', params});
  } else {
    router.push({name: 'SubprocessoVisMapa', params});
  }
}

function irParaAtividadesConhecimentos() {
  if (!sigla.value || !processoAtual.value) {
    return;
  }

  const params = {idProcesso: processoAtual.value.id, siglaUnidade: sigla.value};

  // Verifica se o perfil é CHEFE e se a unidade do subprocesso é a unidade selecionada do perfil
  if (perfilStore.perfilSelecionado === 'CHEFE' && perfilStore.unidadeSelecionada === sigla.value) {
    router.push({name: 'SubprocessoCadastro', params}); // Abre CadAtividades.vue
  } else {
    router.push({name: 'SubprocessoVisCadastro', params}); // Abre VisAtividades.vue
  }
}
</script>

<style scoped>
.card-actionable {
  cursor: pointer;
  transition: transform 0.2s ease-in-out, box-shadow 0.2s ease-in-out;
}

.card-actionable:hover {
  transform: translateY(-5px);
  box-shadow: 0 0.5rem 1rem rgba(0, 0, 0, 0.15);
}

.card-actionable.disabled-card {
  pointer-events: none;
  opacity: 0.6;
}
</style>