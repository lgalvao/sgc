<template>
  <BContainer class="mt-4">
    <PageHeader title="Mapa de competências técnicas">
      <template #actions>
        <BButton
            v-if="podeValidar"
            data-testid="btn-mapa-sugestoes"
            title="Apresentar sugestões"
            variant="outline-warning"
            @click="abrirModalSugestoes"
        >
          Apresentar sugestões
        </BButton>
        <BButton
            v-if="podeValidar"
            data-testid="btn-mapa-validar"
            title="Validar mapa"
            variant="outline-success"
            @click="abrirModalValidar"
        >
          Validar
        </BButton>

        <BButton
            v-if="podeValidar && temHistoricoAnalise"
            data-testid="btn-mapa-historico"
            title="Histórico de análise"
            variant="outline-secondary"
            @click="verHistorico"
        >
          Histórico de análise
        </BButton>

        <BButton
            v-if="podeAnalisar"
            v-show="podeVerSugestoes"
            data-testid="btn-mapa-ver-sugestoes"
            title="Ver sugestões"
            variant="outline-info"
            @click="verSugestoes"
        >
          Ver sugestões
        </BButton>
        <BButton
            v-if="podeAnalisar"
            data-testid="btn-mapa-historico-gestor"
            title="Histórico de análise"
            variant="outline-secondary"
            @click="verHistorico"
        >
          Histórico de análise
        </BButton>
        <BButton
            v-if="podeAnalisar"
            data-testid="btn-mapa-devolver"
            title="Devolver para ajustes"
            variant="outline-danger"
            @click="abrirModalDevolucao"
        >
          Devolver para ajustes
        </BButton>
        <BButton
            v-if="podeAnalisar"
            data-testid="btn-mapa-homologar-aceite"
            title="Aceitar"
            variant="outline-success"
            @click="abrirModalAceitar"
        >
          {{ perfilSelecionado === 'ADMIN' ? 'Homologar' : 'Registrar aceite' }}
        </BButton>
      </template>
    </PageHeader>

    <div v-if="unidade">
      <div class="mb-5 d-flex align-items-center">
        <div
            class="fs-5"
            data-testid="txt-header-unidade"
        >
          {{ unidade.sigla }} - {{ unidade.nome }}
        </div>
      </div>

      <div class="mb-4 mt-3">
        <EmptyState
            v-if="!mapa || mapa.competencias.length === 0"
            icon="bi-journal-x"
            title="Nenhuma competência cadastrada"
            description="Este mapa ainda não possui competências registradas."
        />
        <BCard
            v-for="comp in mapa?.competencias"
            :key="comp.codigo"
            class="mb-3 competencia-card"
            data-testid="vis-mapa__card-competencia"
            no-body
        >
          <BCardBody class="py-2">
            <div
                class="card-title fs-5 d-flex align-items-center position-relative competencia-titulo-card"
            >
              <strong
                  class="competencia-descricao"
                  data-testid="vis-mapa__txt-competencia-descricao"
              > {{ comp.descricao }}</strong>
            </div>
            <div class="d-flex flex-wrap gap-2 mt-2 ps-3">
              <div
                  v-for="atv in comp.atividades"
                  :key="atv.codigo"
              >
                <BCard
                    class="atividade-associada-card-item d-flex flex-column group-atividade-associada"
                    data-testid="card-atividade-associada"
                    no-body
                >
                  <BCardBody class="d-flex align-items-center py-1 px-2">
                    <span class="atividade-associada-descricao me-2">{{ atv.descricao }}</span>
                  </BCardBody>
                  <div class="conhecimentos-atividade px-2 pb-2 ps-3">
                    <span
                        v-for="conhecimento in atv.conhecimentos"
                        :key="conhecimento.descricao"
                        class="me-3 mb-1"
                        data-testid="txt-conhecimento-item"
                    >
                      {{ conhecimento.descricao }}
                    </span>
                  </div>
                </BCard>
              </div>
            </div>
          </BCardBody>
        </BCard>
      </div>
    </div>
    <div v-else>
      <p>Unidade não encontrada.</p>
    </div>

    <AceitarMapaModal
        :loading="isLoading"
        :mostrar-modal="mostrarModalAceitar"
        :perfil="perfilSelecionado || undefined"
        @fechar-modal="fecharModalAceitar"
        @confirmar-aceitacao="confirmarAceitacao"
    />

    <ModalConfirmacao
        v-model="mostrarModalSugestoes"
        :loading="isLoading"
        ok-title="Confirmar"
        titulo="Apresentar Sugestões"
        test-id-confirmar="btn-sugestoes-mapa-confirmar"
        test-id-cancelar="btn-sugestoes-mapa-cancelar"
        @confirmar="confirmarSugestoes"
        @shown="() => sugestoesTextareaRef?.$el?.focus()"
    >
      <div class="mb-3">
        <label
            class="form-label"
            for="sugestoesTextarea"
        >Sugestões para o mapa de competências:</label>
        <BFormTextarea
            id="sugestoesTextarea"
            ref="sugestoesTextareaRef"
            v-model="sugestoes"
            autofocus
            data-testid="inp-sugestoes-mapa-texto"
            placeholder="Digite suas sugestões para o mapa de competências..."
            rows="5"
        />
      </div>
    </ModalConfirmacao>

    <BModal
        v-model="mostrarModalVerSugestoes"
        :fade="false"
        centered
        hide-footer
        title="Sugestões"
    >
      <div class="mb-3">
        <label
            class="form-label"
            for="sugestoesVisualizacao"
        >Sugestões registradas para o mapa de competências:</label>
        <BFormTextarea
            id="sugestoesVisualizacao"
            v-model="sugestoesVisualizacao"
            data-testid="txt-ver-sugestoes-mapa"
            readonly
            rows="5"
        />
      </div>
      <template #footer>
        <BButton
            data-testid="btn-ver-sugestoes-mapa-fechar"
            variant="secondary"
            @click="fecharModalVerSugestoes"
        >
          Fechar
        </BButton>
      </template>
    </BModal>

    <ModalConfirmacao
        v-model="mostrarModalValidar"
        :loading="isLoading"
        ok-title="Validar"
        test-id-confirmar="btn-validar-mapa-confirmar"
        test-id-cancelar="btn-validar-mapa-cancelar"
        titulo="Validar Mapa de Competências"
        variant="success"
        @confirmar="confirmarValidacao"
    >
      <p>Confirma a validação do mapa de competências? Essa ação habilitará a análise por unidades superiores.</p>
    </ModalConfirmacao>

    <ModalConfirmacao
        v-model="mostrarModalDevolucao"
        :loading="isLoading"
        ok-title="Confirmar"
        test-id-confirmar="btn-devolucao-mapa-confirmar"
        test-id-cancelar="btn-devolucao-mapa-cancelar"
        titulo="Devolução"
        variant="danger"
        @confirmar="confirmarDevolucao"
        @shown="() => observacaoDevolucaoRef?.$el?.focus()"
    >
      <p>Confirma a devolução da validação do mapa para ajustes?</p>
      <div class="mb-3">
        <label
            class="form-label"
            for="observacaoDevolucao"
        >Observação:</label>
        <BFormTextarea
            id="observacaoDevolucao"
            ref="observacaoDevolucaoRef"
            v-model="observacaoDevolucao"
            autofocus
            data-testid="inp-devolucao-mapa-obs"
            placeholder="Digite observações sobre a devolução..."
            rows="3"
        />
      </div>
    </ModalConfirmacao>

    <BModal
        v-model="mostrarModalHistorico"
        :fade="false"
        centered
        hide-footer
        size="lg"
        title="Histórico de Análise"
    >
      <table
          class="table table-striped"
          data-testid="tbl-historico-analise"
      >
        <thead>
        <tr>
          <th>Data/Hora</th>
          <th>Unidade</th>
          <th>Resultado</th>
          <th>Observações</th>
        </tr>
        </thead>
        <tbody>
        <tr
            v-for="item in historicoAnalise"
            :key="item.codigo"
            data-testid="row-historico"
        >
          <td>{{ item.data }}</td>
          <td>{{ item.unidade }}</td>
          <td>{{ item.resultado }}</td>
          <td>{{ item.observacoes }}</td>
        </tr>
        </tbody>
      </table>
      <template #footer>
        <BButton
            data-testid="btn-historico-analise-fechar"
            variant="secondary"
            @click="fecharModalHistorico"
        >
          Fechar
        </BButton>
      </template>
    </BModal>
  </BContainer>
</template>

<script lang="ts" setup>
import {BButton, BCard, BCardBody, BContainer, BFormTextarea, BModal,} from "bootstrap-vue-next";
import EmptyState from "@/components/EmptyState.vue";
import ModalConfirmacao from "@/components/ModalConfirmacao.vue";
import PageHeader from "@/components/layout/PageHeader.vue";
import {storeToRefs} from "pinia";
import {computed, onMounted, ref} from "vue";
import {useRoute, useRouter} from "vue-router";
import AceitarMapaModal from "@/components/AceitarMapaModal.vue";
import {usePerfil} from "@/composables/usePerfil";
import {useAnalisesStore} from "@/stores/analises";
import {useMapasStore} from "@/stores/mapas";
import {useFeedbackStore} from "@/stores/feedback";

import {useProcessosStore} from "@/stores/processos";
import {useSubprocessosStore} from "@/stores/subprocessos";
import {useUnidadesStore} from "@/stores/unidades";
import {SituacaoSubprocesso, TipoProcesso, type Unidade} from "@/types/tipos";

const route = useRoute();
const router = useRouter();
const sigla = computed(() => route.params.siglaUnidade as string);
const codProcesso = computed(() => Number(route.params.codProcesso));
const unidadesStore = useUnidadesStore();
const mapaStore = useMapasStore();
const processosStore = useProcessosStore();
const feedbackStore = useFeedbackStore();

const analisesStore = useAnalisesStore();
const subprocessosStore = useSubprocessosStore();
const {perfilSelecionado} = usePerfil();

const {mapaVisualizacao: mapa} = storeToRefs(mapaStore);

const mostrarModalAceitar = ref(false);
const mostrarModalSugestoes = ref(false);
const mostrarModalVerSugestoes = ref(false);
const mostrarModalValidar = ref(false);
const mostrarModalDevolucao = ref(false);
const mostrarModalHistorico = ref(false);
const sugestoes = ref("");
const sugestoesVisualizacao = ref("");
const observacaoDevolucao = ref("");
const isLoading = ref(false);
const sugestoesTextareaRef = ref<InstanceType<typeof BFormTextarea> | null>(null);
const observacaoDevolucaoRef = ref<InstanceType<typeof BFormTextarea> | null>(null);

const unidade = computed<Unidade | null>(() => unidadesStore.unidade);

const subprocesso = computed(() => {
  if (!processosStore.processoDetalhe) return null;
  return processosStore.processoDetalhe.unidades.find(
      (u) => u.sigla === sigla.value,
  );
});

const processo = computed(() => processosStore.processoDetalhe);
const codSubprocesso = computed(() => subprocesso.value?.codSubprocesso);

onMounted(async () => {
  await unidadesStore.buscarUnidade(sigla.value);
  await processosStore.buscarProcessoDetalhe(codProcesso.value);
  if (codSubprocesso.value) {
    await mapaStore.buscarMapaVisualizacao(codSubprocesso.value);
  }
});

const podeValidar = computed(() => {
  return (
      perfilSelecionado.value === "CHEFE" &&
      (subprocesso.value?.situacaoSubprocesso === SituacaoSubprocesso.MAPEAMENTO_MAPA_DISPONIBILIZADO ||
          subprocesso.value?.situacaoSubprocesso === SituacaoSubprocesso.REVISAO_MAPA_DISPONIBILIZADO)
  );
});

const podeAnalisar = computed(() => {
  const situacao = subprocesso.value?.situacaoSubprocesso;
  const isGestorOrAdmin = perfilSelecionado.value === "GESTOR" || perfilSelecionado.value === "ADMIN";
  const isValidado =
      situacao === SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO ||
      situacao === SituacaoSubprocesso.REVISAO_MAPA_VALIDADO;
  const isComSugestoes =
      situacao === SituacaoSubprocesso.MAPEAMENTO_MAPA_COM_SUGESTOES ||
      situacao === SituacaoSubprocesso.REVISAO_MAPA_COM_SUGESTOES;

  return isGestorOrAdmin && (isValidado || isComSugestoes);
});

const podeVerSugestoes = computed(() => {
  const situacao = subprocesso.value?.situacaoSubprocesso;
  return (
      situacao === SituacaoSubprocesso.MAPEAMENTO_MAPA_COM_SUGESTOES ||
      situacao === SituacaoSubprocesso.REVISAO_MAPA_COM_SUGESTOES
  );
});

const temHistoricoAnalise = computed(() => {
  return historicoAnalise.value.length > 0;
});

const historicoAnalise = computed(() => {
  if (!codSubprocesso.value) return [];

  return analisesStore
      .obterAnalisesPorSubprocesso(codSubprocesso.value)
      .map((analise) => ({
        codigo: analise.codigo,
        data: new Date(analise.dataHora).toLocaleString("pt-BR"),
        unidade: (analise as any).unidadeSigla || (analise as any).unidade,
        resultado: analise.resultado,
        observacoes: analise.observacoes || "",
      }));
});

function abrirModalAceitar() {
  mostrarModalAceitar.value = true;
}

function fecharModalAceitar() {
  mostrarModalAceitar.value = false;
}

function abrirModalSugestoes() {
  // if (mapa.value?.sugestoes) {
  //   sugestoes.value = mapa.value.sugestoes
  // }
  mostrarModalSugestoes.value = true;
}

function fecharModalSugestoes() {
  mostrarModalSugestoes.value = false;
  sugestoes.value = "";
}

function fecharModalVerSugestoes() {
  mostrarModalVerSugestoes.value = false;
  sugestoesVisualizacao.value = "";
}

function abrirModalValidar() {
  mostrarModalValidar.value = true;
}

function fecharModalValidar() {
  mostrarModalValidar.value = false;
}

function abrirModalDevolucao() {
  mostrarModalDevolucao.value = true;
}

function fecharModalDevolucao() {
  mostrarModalDevolucao.value = false;
  observacaoDevolucao.value = "";
}

function abrirModalHistorico() {
  mostrarModalHistorico.value = true;
}

function fecharModalHistorico() {
  mostrarModalHistorico.value = false;
}

function verSugestoes() {
  sugestoesVisualizacao.value = mapa.value?.sugestoes || "Nenhuma sugestão registrada.";
  mostrarModalVerSugestoes.value = true;
}

function verHistorico() {
  abrirModalHistorico();
}

async function confirmarSugestoes() {
  if (!codSubprocesso.value) return;
  isLoading.value = true;
  try {
    await processosStore.apresentarSugestoes(codSubprocesso.value, {
      sugestoes: sugestoes.value,
    });

    fecharModalSugestoes();

    feedbackStore.show(
        "Sugestões apresentadas",
        "Sugestões submetidas para análise da unidade superior",
        "success"
    );

    await router.push({name: "Painel"});
  } catch {
    feedbackStore.show(
        "Erro ao apresentar sugestões",
        "Ocorreu um erro. Tente novamente.",
        "danger"
    );
  } finally {
    isLoading.value = false;
  }
}

async function confirmarValidacao() {
  if (!codSubprocesso.value) return;
  isLoading.value = true;
  try {
    await processosStore.validarMapa(codSubprocesso.value);

    fecharModalValidar();

    feedbackStore.show(
        "Mapa validado",
        "Mapa validado e submetido para análise da unidade superior",
        "success"
    );

    await router.push({name: "Painel"});
  } catch {
    feedbackStore.show(
        "Erro ao validar mapa",
        "Ocorreu um erro. Tente novamente.",
        "danger"
    );
  } finally {
    isLoading.value = false;
  }
}

async function confirmarAceitacao(observacoes?: string) {
  if (!codSubprocesso.value) return;

  isLoading.value = true;
  const perfil = perfilSelecionado.value;
  const isHomologacao = perfil === "ADMIN";
  const tipoProcesso = processo.value?.tipo;

  try {
    if (isHomologacao) {
      if (tipoProcesso === TipoProcesso.REVISAO) {
        await subprocessosStore.homologarRevisaoCadastro(codSubprocesso.value, {
          observacoes: observacoes || "",
        });
      } else {
        await processosStore.homologarValidacao(codSubprocesso.value);
      }
    } else {
      // GESTOR: aceitar validação para MAPEAMENTO ou REVISAO
      await processosStore.aceitarValidacao(codSubprocesso.value, {
        observacoes: observacoes || "",
      });
    }

    fecharModalAceitar();
    // CDU-20 step 9.9: redireciona para Painel
    await router.push({name: "Painel"});
  } catch (error) {
    console.error(error);
    feedbackStore.show("Erro", "Erro ao realizar a operação.", "danger");
  } finally {
    isLoading.value = false;
  }
}

async function confirmarDevolucao() {
  if (!codSubprocesso.value) return;

  isLoading.value = true;
  try {
    await subprocessosStore.devolverRevisaoCadastro(codSubprocesso.value, {
      observacoes: observacaoDevolucao.value,
    });

    fecharModalDevolucao();
    await router.push({name: "Painel"});
  } catch (error) {
    console.error(error);
    feedbackStore.show("Erro", "Erro ao devolver.", "danger");
  } finally {
    isLoading.value = false;
  }
}
</script>
