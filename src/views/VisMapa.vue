<template>
  <div class="container mt-4">
    <div class="d-flex justify-content-between align-items-center mb-3">
      <div class="display-6">Mapa de competências técnicas</div>
      <div class="d-flex gap-2">
        <button class="btn btn-outline-secondary" title="Devolver para ajustes" @click="rejeitarMapa">
          Devolver para ajustes
        </button>
        <button class="btn btn-outline-success" title="Aceitar" @click="abrirModalAceitar">
          Aceitar
        </button>
      </div>
    </div>

    <div v-if="unidade">
      <div class="mb-5 d-flex align-items-center">
        <div class="fs-5">{{ unidade.sigla }} - {{ unidade.nome }}</div>
      </div>

      <div class="mb-4 mt-3">
        <div v-if="competencias.length === 0">Nenhuma competência cadastrada.</div>
        <div v-for="comp in competencias" :key="comp.id" class="card mb-3 competencia-card"
             data-testid="competencia-item">
          <div class="card-body py-2">
            <div
                class="card-title fs-5 d-flex align-items-center position-relative competencia-titulo-card">
              <strong class="competencia-descricao" data-testid="competencia-descricao"> {{ comp.descricao }}</strong>
            </div>
            <div class="d-flex flex-wrap gap-2 mt-2 ps-3">
              <div v-for="atvId in comp.atividadesAssociadas" :key="atvId">
                <div v-if="getAtividadeCompleta(atvId)"
                     class="card atividade-associada-card-item d-flex flex-column group-atividade-associada">
                  <div class="card-body d-flex align-items-center py-1 px-2">
                    <span class="atividade-associada-descricao me-2">{{ getAtividadeCompleta(atvId)?.descricao }}</span>
                  </div>
                  <div class="conhecimentos-atividade px-2 pb-2 ps-3">
                    <span v-for="conhecimento in getConhecimentosAtividade(atvId)" :key="conhecimento.id"
                          class="me-3 mb-1">
                      {{ conhecimento.descricao }}
                    </span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <div v-else>
      <p>Unidade não encontrada.</p>
    </div>

    <AceitarMapaModal
      :mostrarModal="mostrarModalAceitar"
      @fecharModal="fecharModalAceitar"
      @confirmarAceitacao="confirmarAceitacao"
    />
  </div>
</template>

<script lang="ts" setup>
import {computed, ref} from 'vue'
import {useRoute, useRouter} from 'vue-router'

import {useMapasStore} from '@/stores/mapas'
import {useUnidadesStore} from '@/stores/unidades'
import {useAtividadesStore} from "@/stores/atividades";
import {useProcessosStore} from "@/stores/processos";
import {useNotificacoesStore} from "@/stores/notificacoes";
import {Atividade, Competencia, Conhecimento, Unidade} from '@/types/tipos';
import AceitarMapaModal from '@/components/AceitarMapaModal.vue';

const route = useRoute()
const router = useRouter()
const sigla = computed(() => route.params.siglaUnidade as string)
const idProcesso = computed(() => Number(route.params.idProcesso))
const unidadesStore = useUnidadesStore()
const mapaStore = useMapasStore()
const atividadesStore = useAtividadesStore()
const processosStore = useProcessosStore()
const notificacoesStore = useNotificacoesStore()

// Estados reativos para o modal
const mostrarModalAceitar = ref(false)

const unidade = computed<Unidade | null>(() => {
  function buscarUnidade(unidades: Unidade[], sigla: string): Unidade | null {
    for (const unidade of unidades) {
      if (unidade.sigla === sigla) return unidade
      if (unidade.filhas && unidade.filhas.length) {
        const encontrada = buscarUnidade(unidade.filhas, sigla)
        if (encontrada) return encontrada
      }
    }
    return null
  }

  return buscarUnidade(unidadesStore.unidades as Unidade[], sigla.value)
})

const idSubprocesso = computed(() => {
  const Subprocesso = processosStore.subprocessos.find(
      (pu: any) => pu.idProcesso === idProcesso.value && pu.unidade === sigla.value
  );
  return Subprocesso?.id;
});

const atividades = computed<Atividade[]>(() => {
  if (typeof idSubprocesso.value !== 'number') {
    return []
  }
  return atividadesStore.getAtividadesPorSubprocesso(idSubprocesso.value) || []
})

const mapa = computed(() => mapaStore.mapas.find(m => m.unidade === sigla.value && m.idProcesso === idProcesso.value))
const competencias = computed<Competencia[]>(() => mapa.value ? mapa.value.competencias : [])

function getAtividadeCompleta(id: number): Atividade | undefined {
  return atividades.value.find(a => a.id === id)
}

function getConhecimentosAtividade(id: number): Conhecimento[] {
  const atividade = getAtividadeCompleta(id)
  return atividade ? atividade.conhecimentos : []
}

function abrirModalAceitar() {
  mostrarModalAceitar.value = true
}

function fecharModalAceitar() {
  mostrarModalAceitar.value = false
}

async function confirmarAceitacao(observacao: string) {
  try {
    await processosStore.aceitarMapa({
      idProcesso: idProcesso.value,
      unidade: sigla.value,
      observacao: observacao || undefined
    })

    fecharModalAceitar()

    // Determinar mensagem baseada na unidade superior
    const unidadeSuperior = unidadesStore.getUnidadeImediataSuperior(sigla.value)
    let mensagem = 'Mapa aceito e submetido para análise da unidade superior'
    if (unidadeSuperior === 'SEDOC') {
      mensagem = 'Mapa homologado'
    }

    notificacoesStore.sucesso('Mapa aceito', mensagem)

    // Redirecionar para a tela de detalhes do subprocesso
    router.push({
      name: 'Subprocesso',
      params: { idProcesso: idProcesso.value, siglaUnidade: sigla.value }
    })

  } catch (error) {
    console.error('Erro ao aceitar mapa:', error)
    notificacoesStore.erro(
      'Erro ao aceitar mapa',
      'Ocorreu um erro ao aceitar o mapa. Tente novamente.'
    )
  }
}

async function rejeitarMapa() {
  try {
    await processosStore.rejeitarMapa({
      idProcesso: idProcesso.value,
      unidade: sigla.value
    })

    notificacoesStore.sucesso(
      'Mapa devolvido',
      'Mapa devolvido à unidade subordinada, para ajustes'
    )

    // Redirecionar para a tela de detalhes do subprocesso
    router.push({
      name: 'Subprocesso',
      params: { idProcesso: idProcesso.value, siglaUnidade: sigla.value }
    })

  } catch (error) {
    console.error('Erro ao rejeitar mapa:', error)
    notificacoesStore.erro(
      'Erro ao devolver mapa',
      'Ocorreu um erro ao devolver o mapa. Tente novamente.'
    )
  }
}
</script>

<style scoped>
.competencia-card {
  transition: box-shadow 0.2s;
}

.competencia-titulo-card {
  background: #f8fafc;
  border-bottom: 1px solid #e3e8ee;
  padding: 0.5rem 0.75rem;
  margin-left: -0.75rem;
  margin-right: -0.75rem;
  margin-top: -0.5rem;
  border-top-left-radius: 0.375rem;
  border-top-right-radius: 0.375rem;
  width: calc(100% + 1.5rem);
}

.competencia-titulo-card .competencia-descricao {
  font-size: 1.1rem;
}

.atividade-associada-card-item {
  background-color: transparent;
}

.atividade-associada-descricao {
  color: #495057;
  font-weight: bold;
}

.conhecimentos-atividade {
  margin-top: 0.25rem;
  font-size: 0.9rem;
}

.conhecimentos-atividade {
  border-radius: 0.25rem;
  font-weight: normal;
}
</style>