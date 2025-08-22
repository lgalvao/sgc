<template>
  <div class="container mt-4">
    <div class="unidade-cabecalho w-100">
      <span class="unidade-sigla">{{ siglaUnidade }}</span>
      <span class="unidade-nome">{{ nomeUnidade }}</span>
    </div>

    <div class="d-flex justify-content-between align-items-center mb-3">
      <h2 class="mb-0">Atividades e conhecimentos</h2>
      <div class="d-flex gap-2">
        <button class="btn btn-outline-secondary" @click="irParaImpactoMapa">
          <i class="bi bi-arrow-right-circle me-2"></i>Impacto no mapa
        </button>
        <button class="btn btn-secondary" title="Devolver para ajustes" @click="devolverCadastro">
          Devolver para ajustes
        </button>
        <button class="btn btn-success" title="Validar" @click="validarCadastro">
          Validar
        </button>
      </div>
    </div>

    <!-- Lista de atividades -->
    <div v-for="(atividade, idx) in atividades" :key="atividade.id || idx" class="card mb-3 atividade-card">
      <div class="card-body py-2">
        <div
            class="card-title d-flex align-items-center atividade-edicao-row position-relative group-atividade atividade-hover-row atividade-titulo-card">
          <strong class="atividade-descricao" data-testid="atividade-descricao">{{ atividade.descricao }}</strong>
        </div>

        <!-- Conhecimentos da atividade -->
        <div class="mt-3 ms-3">
          <div v-for="(conhecimento) in atividade.conhecimentos" :key="conhecimento.id"
               class="d-flex align-items-center mb-2 group-conhecimento position-relative conhecimento-hover-row">
            <span data-testid="conhecimento-descricao">{{ conhecimento.descricao }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import {computed} from 'vue'
import {useAtividadesStore} from '@/stores/atividades'
import {useUnidadesStore} from '@/stores/unidades'
import {useProcessosStore} from '@/stores/processos'
import {Atividade, Subprocesso, Unidade} from '@/types/tipos'

const props = defineProps<{
  idProcesso: number | string,
  sigla: string
}>()

const unidadeId = computed(() => props.sigla)
const idProcesso = computed(() => Number(props.idProcesso))

const atividadesStore = useAtividadesStore()
const unidadesStore = useUnidadesStore()
const processosStore = useProcessosStore()

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

const idSubprocesso = computed(() => {
  const Subprocesso = (processosStore.subprocessos as Subprocesso[]).find(
      pu => pu.idProcesso === idProcesso.value && pu.unidade === unidadeId.value
  );
  return Subprocesso?.id;
});

const atividades = computed<Atividade[]>(() => {
  if (idSubprocesso.value === undefined) return []
  return atividadesStore.getAtividadesPorSubprocesso(idSubprocesso.value) || []
})

function validarCadastro() {
  // Lógica para validar o cadastro

}

function devolverCadastro() {
  // Lógica para devolver o cadastro

}

function irParaImpactoMapa() {
  if (idProcesso.value && siglaUnidade.value) {
    router.push({
      name: 'SubprocessoImpactoMapa',
      params: {
        idProcesso: idProcesso.value,
        siglaUnidade: siglaUnidade.value
      }
    });
  }
}
</script>

<style>
.unidade-nome {
  color: #222;
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

.unidade-cabecalho {
  font-size: 1.1rem;
  font-weight: 500;
  margin-bottom: 1.2rem;
  display: flex;
  gap: 0.5rem;
}

.unidade-sigla {
  background: #f8fafc;
  color: #333;
  font-weight: bold;
  border-radius: 0.5rem;
  letter-spacing: 1px;
}

.unidade-nome {
  color: #222;
  opacity: 0.85;
  padding-right: 1rem;
}

</style>