<template>
  <LayoutPadrao>
    <PageHeader title="Relatório: Mapas vigentes">
      <template #actions>
        <BButton variant="outline-secondary" to="/relatorios">
          <i class="bi bi-arrow-left me-1"/> Voltar
        </BButton>
      </template>
    </PageHeader>

    <BCard class="mb-4">
      <div class="d-flex flex-column gap-3">
        <BFormGroup label-for="arvore-unidades-mapas">
          <template #label>
            Unidades participantes <span aria-hidden="true" class="text-danger">*</span>
          </template>
          <div class="border rounded p-3 container-arvore" data-testid="container-arvore-unidades-mapas">
            <ArvoreUnidades
              id="arvore-unidades-mapas"
              v-model="unidadesSelecionadas"
              :unidades="unidadesDisponiveis"
            />
          </div>
        </BFormGroup>

        <div class="d-flex flex-wrap gap-2">
            <BButton
              :disabled="carregando || !temUnidadesSelecionadas"
              variant="success"
              data-testid="btn-gerar-html-mapas"
              @click="gerarRelatorio"
            >
              <BSpinner v-if="carregando" small class="me-1" />
              <i v-else class="bi bi-search me-1" />
              {{ TEXTOS.relatorios.BOTAO_GERAR }}
            </BButton>
            <BButton
              :disabled="carregando || !temUnidadesSelecionadas"
              variant="outline-danger"
              data-testid="btn-gerar-mapas"
              @click="exportarPdf"
            >
              <BSpinner v-if="carregando" small class="me-1" />
              <i v-else class="bi bi-file-earmark-pdf me-1" />
              PDF
            </BButton>
        </div>
      </div>
    </BCard>

    <div v-if="carregando && relatorioMapas.length === 0" class="text-center py-5">
      <BSpinner variant="primary"/>
    </div>

    <template v-else-if="relatorioMapas.length > 0">
      <div class="d-flex flex-column gap-3">
        <BCard
            v-for="mapa in relatorioMapas"
            :key="mapa.codigoUnidade"
            class="relatorio-mapas__card shadow-sm"
            no-body
            data-testid="card-relatorio-mapas"
        >
          <BCardBody>
            <BCardTitle class="mb-3 relatorio-mapas__cabecalho">
              <span class="relatorio-mapas__titulo">{{ mapa.siglaUnidade }}</span>
              <span class="relatorio-mapas__subtitulo">{{ mapa.nomeUnidade }}</span>
            </BCardTitle>

            <div class="d-flex flex-column gap-2">
              <section
                  v-for="competencia in mapa.competencias"
                  :key="competencia.codigo"
                  class="relatorio-mapas__competencia"
              >
                <h6 class="mb-2 relatorio-mapas__secao">{{ competencia.descricao }}</h6>

                <div
                    v-for="atividade in competencia.atividades"
                    :key="atividade.codigo"
                    class="relatorio-mapas__atividade"
                >
                  <div class="relatorio-mapas__atividade-titulo">{{ atividade.descricao }}</div>
                  <ul v-if="atividade.conhecimentos.length > 0" class="relatorio-mapas__conhecimentos">
                    <li v-for="conhecimento in atividade.conhecimentos" :key="conhecimento.codigo">
                      {{ conhecimento.descricao }}
                    </li>
                  </ul>
                </div>
              </section>
            </div>
          </BCardBody>
        </BCard>
      </div>
    </template>
  </LayoutPadrao>
</template>

<script lang="ts" setup>
import {computed, onMounted, ref} from "vue";
import {BButton, BCard, BCardBody, BCardTitle, BFormGroup, BSpinner} from "bootstrap-vue-next";
import LayoutPadrao from "@/components/layout/LayoutPadrao.vue";
import PageHeader from "@/components/layout/PageHeader.vue";
import {useRelatoriosStore} from "@/stores/relatorios";
import {TEXTOS} from "@/constants/textos";
import ArvoreUnidades from "@/components/unidade/ArvoreUnidades.vue";
import type {Unidade} from "@/types/tipos";
import {useNotification} from "@/composables/useNotification";
import {buscarCodigosUnidadesComMapaVigente, buscarTodasUnidades, mapUnidadesArray} from "@/services/unidadeService";

const relatoriosStore = useRelatoriosStore();
const { notify } = useNotification();

const unidadesDisponiveis = ref<Unidade[]>([]);
const unidadesSelecionadas = ref<number[]>([]);
const carregando = ref(false);
const relatorioMapas = computed(() => relatoriosStore.relatorioMapas);
const temUnidadesSelecionadas = computed(() => unidadesSelecionadas.value.length > 0);

function aplicarElegibilidadeMapaVigente(unidades: Unidade[], codigosElegiveis: Set<number>): Unidade[] {
  return unidades.map(unidade => ({
    ...unidade,
    isElegivel: codigosElegiveis.has(unidade.codigo),
    filhas: unidade.filhas ? aplicarElegibilidadeMapaVigente(unidade.filhas, codigosElegiveis) : []
  }));
}

function filtrarArvorePorMapaVigente(unidades: Unidade[]): Unidade[] {
  return unidades
      .map((unidade): Unidade | null => {
        const filhasFiltradas = unidade.filhas ? filtrarArvorePorMapaVigente(unidade.filhas) : [];
        const manterUnidade = unidade.isElegivel === true || filhasFiltradas.length > 0;

        if (!manterUnidade) {
          return null;
        }

        return {
          ...unidade,
          filhas: filhasFiltradas
        };
      })
      .filter((unidade): unidade is Unidade => unidade !== null);
}

async function carregarUnidades() {
  try {
    const [arvore, codigosComMapa] = await Promise.all([
      buscarTodasUnidades(),
      buscarCodigosUnidadesComMapaVigente()
    ]);
    const unidadesComElegibilidade = aplicarElegibilidadeMapaVigente(
      mapUnidadesArray(arvore as Unidade[]),
      new Set(codigosComMapa)
    );
    unidadesDisponiveis.value = filtrarArvorePorMapaVigente(unidadesComElegibilidade);
  } catch {
    notify("Erro ao carregar unidades", "danger");
  }
}

async function exportarPdf() {
  if (!temUnidadesSelecionadas.value) {
    return;
  }

  carregando.value = true;
  await relatoriosStore.exportarMapasPdf(unidadesSelecionadas.value);
  carregando.value = false;
  if (relatoriosStore.lastError) {
    notify(TEXTOS.relatorios.ERRO_GERAR, "danger");
  }
}

async function gerarRelatorio() {
  if (!temUnidadesSelecionadas.value) {
    return;
  }

  carregando.value = true;
  await relatoriosStore.buscarRelatorioMapas(unidadesSelecionadas.value);
  carregando.value = false;
  if (relatoriosStore.lastError) {
    notify(TEXTOS.relatorios.ERRO_BUSCA, "danger");
  }
}

onMounted(() => {
  relatoriosStore.limparRelatorio();
  carregarUnidades();
});
</script>

<style scoped>
.relatorio-mapas__arvore {
  max-height: 22rem;
  overflow: auto;
  padding: 0.75rem;
  border: 1px solid #d7dee8;
  border-radius: 0.65rem;
  background: #fff;
}

.container-arvore {
  overflow-x: hidden;
  max-height: 22rem;
  overflow-y: auto;
  background: #fff;
}

.relatorio-mapas__card {
  border: 1px solid #c8d1dc;
  background: #fff;
}

.relatorio-mapas__cabecalho {
  display: flex;
  flex-direction: column;
  gap: 0.2rem;
  padding-bottom: 0.85rem;
  margin-bottom: 0.9rem;
  border-bottom: 1px solid #d7dee8;
}

.relatorio-mapas__titulo {
  color: #16365f;
  font-size: 1.45rem;
  font-weight: 700;
  letter-spacing: 0.01em;
}

.relatorio-mapas__subtitulo {
  color: #4b5d73;
  font-size: 0.95rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.03em;
}

.relatorio-mapas__competencia {
  padding: 0.95rem 1rem;
  border: 1px solid #dde4ec;
  border-radius: 0.65rem;
  background: #fff;
}

.relatorio-mapas__secao {
  color: #1d3557;
  font-size: 1.08rem;
  font-weight: 700;
  line-height: 1.35;
}

.relatorio-mapas__atividade + .relatorio-mapas__atividade {
  margin-top: 0.7rem;
}

.relatorio-mapas__atividade-titulo {
  color: #1f2937;
  font-size: 0.98rem;
  font-weight: 600;
  line-height: 1.4;
}

.relatorio-mapas__conhecimentos {
  margin: 0.35rem 0 0;
  padding-left: 1.2rem;
  color: #374151;
}

.relatorio-mapas__conhecimentos li + li {
  margin-top: 0.18rem;
}

@media (max-width: 768px) {
  .relatorio-mapas__titulo {
    font-size: 1.2rem;
  }

  .relatorio-mapas__competencia {
    padding: 0.8rem 0.85rem;
  }
}
</style>
