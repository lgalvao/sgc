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
          data-testid="apresentar-sugestoes-btn"
          @click="abrirModalSugestoes"
        >
          Apresentar sugestões
        </BButton>
        <BButton
          v-if="podeValidar"
          variant="outline-success"
          title="Validar mapa"
          data-testid="validar-btn"
          @click="abrirModalValidar"
        >
          Validar
        </BButton>

        <BButton
          v-if="podeValidar && temHistoricoAnalise"
          variant="outline-secondary"
          title="Histórico de análise"
          data-testid="historico-analise-btn"
          @click="verHistorico"
        >
          Histórico de análise
        </BButton>

        <BButton
          v-if="podeAnalisar"
          v-show="podeVerSugestoes"
          variant="outline-info"
          title="Ver sugestões"
          data-testid="ver-sugestoes-btn"
          @click="verSugestoes"
        >
          Ver sugestões
        </BButton>
        <BButton
          v-if="podeAnalisar"
          variant="outline-secondary"
          title="Histórico de análise"
          data-testid="historico-analise-btn-gestor"
          @click="verHistorico"
        >
          Histórico de análise
        </BButton>
        <BButton
          v-if="podeAnalisar"
          variant="outline-danger"
          title="Devolver para ajustes"
          data-testid="devolver-ajustes-btn"
          @click="abrirModalDevolucao"
        >
          Devolver para ajustes
        </BButton>
        <BButton
          v-if="podeAnalisar"
          variant="outline-success"
          data-testid="btn-registrar-aceite-homologar"
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
          data-testid="unidade-info"
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
          data-testid="competencia-block"
          no-body
        >
          <BCardBody class="py-2">
            <div
              class="card-title fs-5 d-flex align-items-center position-relative competencia-titulo-card"
            >
              <strong
                class="competencia-descricao"
                data-testid="competencia-descricao"
              > {{ comp.descricao }}</strong>
            </div>
            <div class="d-flex flex-wrap gap-2 mt-2 ps-3">
              <div
                v-for="atv in comp.atividades"
                :key="atv.codigo"
              >
                <BCard
                  class="atividade-associada-card-item d-flex flex-column group-atividade-associada"
                  data-testid="atividade-item"
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
                      data-testid="conhecimento-item"
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
          data-testid="sugestoes-textarea"
        />
      </div>
      <template #footer>
        <BButton
          variant="secondary"
          data-testid="modal-apresentar-sugestoes-cancelar"
          @click="fecharModalSugestoes"
        >
          Cancelar
        </BButton>
        <BButton
          variant="primary"
          data-testid="modal-apresentar-sugestoes-confirmar"
          @click="confirmarSugestoes"
        >
          Confirmar
        </BButton>
      </template>
    </BModal>

    <BModal
      v-model="mostrarModalVerSugestoes"
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
          data-testid="sugestoes-visualizacao-textarea"
        />
      </div>
      <template #footer>
        <BButton
          variant="secondary"
          data-testid="modal-sugestoes-fechar"
          @click="fecharModalVerSugestoes"
        >
          Fechar
        </BButton>
      </template>
    </BModal>

    <BModal
      v-model="mostrarModalValidar"
      title="Validar Mapa de Competências"
      centered
      hide-footer
    >
      <p>Confirma a validação do mapa de competências? Essa ação habilitará a análise por unidades superiores.</p>
      <template #footer>
        <BButton
          variant="secondary"
          data-testid="modal-validar-cancelar"
          @click="fecharModalValidar"
        >
          Cancelar
        </BButton>
        <BButton
          variant="success"
          data-testid="modal-validar-confirmar"
          @click="confirmarValidacao"
        >
          Validar
        </BButton>
      </template>
    </BModal>

    <BModal
      v-model="mostrarModalDevolucao"
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
          data-testid="observacao-devolucao-textarea"
        />
      </div>
      <template #footer>
        <BButton
          variant="secondary"
          data-testid="modal-devolucao-cancelar"
          @click="fecharModalDevolucao"
        >
          Cancelar
        </BButton>
        <BButton
          variant="danger"
          data-testid="modal-devolucao-confirmar"
          @click="confirmarDevolucao"
        >
          Confirmar
        </BButton>
      </template>
    </BModal>

    <BModal
      v-model="mostrarModalHistorico"
      title="Histórico de Análise"
      centered
      size="lg"
      hide-footer
    >
      <table
        class="table table-striped"
        data-testid="tabela-historico"
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
            data-testid="historico-item"
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
          data-testid="modal-historico-fechar"
          @click="fecharModalHistorico"
        >
          Fechar
        </BButton>
      </template>
    </BModal>
  </BContainer>
</template>

<script lang="ts" setup>
import {BButton, BCard, BCardBody, BContainer, BFormTextarea, BModal, useToast,} from "bootstrap-vue-next";
import {storeToRefs} from "pinia";
import {computed, onMounted, ref} from "vue";
import {useRoute, useRouter} from "vue-router";
import AceitarMapaModal from "@/components/AceitarMapaModal.vue";
import {usePerfil} from "@/composables/usePerfil";
import {useAnalisesStore} from "@/stores/analises";
import {useMapasStore} from "@/stores/mapas";

import {useProcessosStore} from "@/stores/processos";
import {useSubprocessosStore} from "@/stores/subprocessos";
import {useUnidadesStore} from "@/stores/unidades";
import {SituacaoSubprocesso, type Unidade} from "@/types/tipos";

const route = useRoute();
const router = useRouter();
const sigla = computed(() => route.params.siglaUnidade as string);
const codProcesso = computed(() => Number(route.params.codProcesso));
const unidadesStore = useUnidadesStore();
const mapaStore = useMapasStore();
const processosStore = useProcessosStore();
const toast = useToast(); // Instantiate toast

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

const unidade = computed<Unidade | null>(() => {
  function buscarUnidade(unidades: Unidade[], sigla: string): Unidade | null {
    for (const unidade of unidades) {
      if (unidade.sigla === sigla) return unidade;
      if (unidade.filhas && unidade.filhas.length) {
        const encontrada = buscarUnidade(unidade.filhas, sigla);
        if (encontrada) return encontrada;
      }
    }
    return null;
  }

  return buscarUnidade(unidadesStore.unidades as Unidade[], sigla.value);
});

const subprocesso = computed(() => {
  if (!processosStore.processoDetalhe) return null;
  return processosStore.processoDetalhe.unidades.find(
      (u) => u.sigla === sigla.value,
  );
});

const codSubprocesso = computed(() => subprocesso.value?.codUnidade);

onMounted(async () => {
  await processosStore.buscarProcessoDetalhe(codProcesso.value);
  if (codSubprocesso.value) {
    await mapaStore.buscarMapaVisualizacao(codSubprocesso.value);
  }
});

const podeValidar = computed(() => {
  return (
      perfilSelecionado.value === "CHEFE" &&
      subprocesso.value?.situacaoSubprocesso ===
      SituacaoSubprocesso.MAPEAMENTO_CONCLUIDO
  );
});

const podeAnalisar = computed(() => {
  return (
      (perfilSelecionado.value === "GESTOR" ||
          perfilSelecionado.value === "ADMIN") &&
      (subprocesso.value?.situacaoSubprocesso ===
          SituacaoSubprocesso.MAPA_VALIDADO ||
          subprocesso.value?.situacaoSubprocesso ===
          SituacaoSubprocesso.AGUARDANDO_AJUSTES_MAPA)
  );
});

const podeVerSugestoes = computed(() => {
  return (
      subprocesso.value?.situacaoSubprocesso ===
      SituacaoSubprocesso.AGUARDANDO_AJUSTES_MAPA
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

    toast.create({
        title: "Sugestões apresentadas",
        body: "Sugestões submetidas para análise da unidade superior",
        props: { variant: 'success', value: true },
    });

    await router.push({
      name: "Subprocesso",
      params: {codProcesso: codProcesso.value, siglaUnidade: sigla.value},
    });
  } catch {
    toast.create({
        title: "Erro ao apresentar sugestões",
        body: "Ocorreu um erro. Tente novamente.",
        props: { variant: 'danger', value: true },
    });
  }
}

async function confirmarValidacao() {
  if (!codSubprocesso.value) return;
  try {
    await processosStore.validarMapa(codSubprocesso.value);

    fecharModalValidar();

    toast.create({
        title: "Mapa validado",
        body: "Mapa validado e submetido para análise da unidade superior",
        props: { variant: 'success', value: true },
    });

    await router.push({
      name: "Subprocesso",
      params: {codProcesso: codProcesso.value, siglaUnidade: sigla.value},
    });
  } catch {
    toast.create({
        title: "Erro ao validar mapa",
        body: "Ocorreu um erro. Tente novamente.",
        props: { variant: 'danger', value: true },
    });
  }
}

async function confirmarAceitacao(observacoes?: string) {
  if (!codSubprocesso.value) return;

  const perfil = perfilSelecionado.value;
  const isHomologacao = perfil === "ADMIN";

  if (isHomologacao) {
    await subprocessosStore.homologarRevisaoCadastro(codSubprocesso.value, {
      observacoes: observacoes || "",
    });
  } else {
    await subprocessosStore.aceitarRevisaoCadastro(codSubprocesso.value, {
      observacoes: observacoes || "",
    });
  }

  fecharModalAceitar();
  await router.push({name: "Painel"});
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
