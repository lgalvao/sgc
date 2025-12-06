<template>
  <BContainer class="mt-4">
    <div class="d-flex justify-content-between align-items-center mb-3">
      <div class="display-6">
        Mapa de competências técnicas
      </div>
      <div class="d-flex gap-2">
        <BButton
          v-if="podeValidar"
          variant="outline-warning"
          title="Apresentar sugestões"
          data-testid="btn-mapa-sugestoes"
          @click="abrirModalSugestoes"
        >
          Apresentar sugestões
        </BButton>
        <BButton
          v-if="podeValidar"
          variant="outline-success"
          title="Validar mapa"
          data-testid="btn-mapa-validar"
          @click="abrirModalValidar"
        >
          Validar
        </BButton>

        <BButton
          v-if="podeValidar && temHistoricoAnalise"
          variant="outline-secondary"
          title="Histórico de análise"
          data-testid="btn-mapa-historico"
          @click="verHistorico"
        >
          Histórico de análise
        </BButton>

        <BButton
          v-if="podeAnalisar"
          v-show="podeVerSugestoes"
          variant="outline-info"
          title="Ver sugestões"
          data-testid="btn-mapa-ver-sugestoes"
          @click="verSugestoes"
        >
          Ver sugestões
        </BButton>
        <BButton
          v-if="podeAnalisar"
          variant="outline-secondary"
          title="Histórico de análise"
          data-testid="btn-mapa-historico-gestor"
          @click="verHistorico"
        >
          Histórico de análise
        </BButton>
        <BButton
          v-if="podeAnalisar"
          variant="outline-danger"
          title="Devolver para ajustes"
          data-testid="btn-mapa-devolver"
          @click="abrirModalDevolucao"
        >
          Devolver para ajustes
        </BButton>
        <BButton
          v-if="podeAnalisar"
          variant="outline-success"
          data-testid="btn-mapa-homologar-aceite"
          title="Aceitar"
          @click="abrirModalAceitar"
        >
          {{ perfilSelecionado === 'ADMIN' ? 'Homologar' : 'Registrar aceite' }}
        </BButton>
      </div>
    </div>

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
        <div v-if="!mapa || mapa.competencias.length === 0">
          Nenhuma competência cadastrada.
        </div>
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
      :mostrar-modal="mostrarModalAceitar"
      :perfil="perfilSelecionado || undefined"
      @fechar-modal="fecharModalAceitar"
      @confirmar-aceitacao="confirmarAceitacao"
    />

    <BModal
      v-model="mostrarModalSugestoes"
      :fade="false"
      title="Apresentar Sugestões"
      centered
      hide-footer
    >
      <div class="mb-3">
        <label
          for="sugestoesTextarea"
          class="form-label"
        >Sugestões para o mapa de competências:</label>
        <BFormTextarea
          id="sugestoesTextarea"
          v-model="sugestoes"
          rows="5"
          placeholder="Digite suas sugestões para o mapa de competências..."
          data-testid="inp-sugestoes-mapa-texto"
        />
      </div>
      <template #footer>
        <BButton
          variant="secondary"
          data-testid="btn-sugestoes-mapa-cancelar"
          @click="fecharModalSugestoes"
        >
          Cancelar
        </BButton>
        <BButton
          variant="primary"
          data-testid="btn-sugestoes-mapa-confirmar"
          @click="confirmarSugestoes"
        >
          Confirmar
        </BButton>
      </template>
    </BModal>

    <BModal
      v-model="mostrarModalVerSugestoes"
      :fade="false"
      title="Sugestões"
      centered
      hide-footer
    >
      <div class="mb-3">
        <label class="form-label">Sugestões registradas para o mapa de competências:</label>
        <BFormTextarea
          v-model="sugestoesVisualizacao"
          rows="5"
          readonly
          data-testid="txt-ver-sugestoes-mapa"
        />
      </div>
      <template #footer>
        <BButton
          variant="secondary"
          data-testid="btn-ver-sugestoes-mapa-fechar"
          @click="fecharModalVerSugestoes"
        >
          Fechar
        </BButton>
      </template>
    </BModal>

    <BModal
      v-model="mostrarModalValidar"
      :fade="false"
      title="Validar Mapa de Competências"
      centered
      hide-footer
    >
      <p>Confirma a validação do mapa de competências? Essa ação habilitará a análise por unidades superiores.</p>
      <template #footer>
        <BButton
          variant="secondary"
          data-testid="btn-validar-mapa-cancelar"
          @click="fecharModalValidar"
        >
          Cancelar
        </BButton>
        <BButton
          variant="success"
          data-testid="btn-validar-mapa-confirmar"
          @click="confirmarValidacao"
        >
          Validar
        </BButton>
      </template>
    </BModal>

    <BModal
      v-model="mostrarModalDevolucao"
      :fade="false"
      title="Devolução"
      centered
      hide-footer
    >
      <p>Confirma a devolução da validação do mapa para ajustes?</p>
      <div class="mb-3">
        <label
          for="observacaoDevolucao"
          class="form-label"
        >Observação:</label>
        <BFormTextarea
          id="observacaoDevolucao"
          v-model="observacaoDevolucao"
          rows="3"
          placeholder="Digite observações sobre a devolução..."
          data-testid="inp-devolucao-mapa-obs"
        />
      </div>
      <template #footer>
        <BButton
          variant="secondary"
          data-testid="btn-devolucao-mapa-cancelar"
          @click="fecharModalDevolucao"
        >
          Cancelar
        </BButton>
        <BButton
          variant="danger"
          data-testid="btn-devolucao-mapa-confirmar"
          @click="confirmarDevolucao"
        >
          Confirmar
        </BButton>
      </template>
    </BModal>

    <BModal
      v-model="mostrarModalHistorico"
      :fade="false"
      title="Histórico de Análise"
      centered
      size="lg"
      hide-footer
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
          variant="secondary"
          data-testid="btn-historico-analise-fechar"
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
      subprocesso.value?.situacaoSubprocesso ===
      SituacaoSubprocesso.MAPA_DISPONIBILIZADO
  );
});

const podeAnalisar = computed(() => {
  return (
      (perfilSelecionado.value === "GESTOR" ||
          perfilSelecionado.value === "ADMIN") &&
      (subprocesso.value?.situacaoSubprocesso ===
          SituacaoSubprocesso.MAPA_VALIDADO ||
          subprocesso.value?.situacaoSubprocesso ===
          SituacaoSubprocesso.MAPA_COM_SUGESTOES)
  );
});

const podeVerSugestoes = computed(() => {
  return (
      subprocesso.value?.situacaoSubprocesso ===
      SituacaoSubprocesso.MAPA_COM_SUGESTOES
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
  // TODO: Fix sugestoes access - property doesn't exist on MapaVisualizacao
  // sugestoesVisualizacao.value = mapa.value?.sugestoes || "Nenhuma sugestão registrada.";
  mostrarModalVerSugestoes.value = true;
}

function verHistorico() {
  abrirModalHistorico();
}

async function confirmarSugestoes() {
  if (!codSubprocesso.value) return;
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

    await router.push({
      name: "Subprocesso",
      params: {codProcesso: codProcesso.value, siglaUnidade: sigla.value},
    });
  } catch {
    feedbackStore.show(
      "Erro ao apresentar sugestões",
      "Ocorreu um erro. Tente novamente.",
      "danger"
    );
  }
}

async function confirmarValidacao() {
  if (!codSubprocesso.value) return;
  try {
    await processosStore.validarMapa(codSubprocesso.value);

    fecharModalValidar();

    feedbackStore.show(
      "Mapa validado",
      "Mapa validado e submetido para análise da unidade superior",
      "success"
    );

    await router.push({
      name: "Subprocesso",
      params: {codProcesso: codProcesso.value, siglaUnidade: sigla.value},
    });
  } catch {
    feedbackStore.show(
      "Erro ao validar mapa",
      "Ocorreu um erro. Tente novamente.",
      "danger"
    );
  }
}

async function confirmarAceitacao(observacoes?: string) {
  if (!codSubprocesso.value) return;

  const perfil = perfilSelecionado.value;
  const isHomologacao = perfil === "ADMIN";
  const tipoProcesso = processo.value?.tipo;

  if (isHomologacao) {
    if (tipoProcesso === TipoProcesso.REVISAO) {
      await subprocessosStore.homologarRevisaoCadastro(codSubprocesso.value, {
        observacoes: observacoes || "",
      });
    } else {
      await processosStore.homologarValidacao(codSubprocesso.value);
    }
  } else {
    await subprocessosStore.aceitarRevisaoCadastro(codSubprocesso.value, {
      observacoes: observacoes || "",
    });
  }

  fecharModalAceitar();
  await router.push({
    name: "Subprocesso",
    params: { codProcesso: codProcesso.value, siglaUnidade: sigla.value },
  });
}

async function confirmarDevolucao() {
  if (!codSubprocesso.value) return;

  await subprocessosStore.devolverRevisaoCadastro(codSubprocesso.value, {
    observacoes: observacaoDevolucao.value,
  });

  fecharModalDevolucao();
  await router.push({name: "Painel"});
}
</script>

