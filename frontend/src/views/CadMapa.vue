<template>
  <BContainer class="mt-4">
    <div class="fs-5 mb-3">
      {{ unidade?.sigla }} - {{ unidade?.nome }}
      <span class="ms-3" data-testid="txt-badge-situacao">{{
          subprocessosStore.subprocessoDetalhe?.situacaoLabel || situacaoLabel(subprocessosStore.subprocessoDetalhe?.situacao)
        }}</span>
    </div>

    <div class="d-flex justify-content-between align-items-center mb-3">
      <div class="display-6 mb-3">
        Mapa de competências técnicas
      </div>
      <div class="d-flex gap-2">
        <BButton
            v-if="podeVerImpacto"
            data-testid="cad-mapa__btn-impactos-mapa"
            variant="outline-secondary"
            @click="abrirModalImpacto"
        >
          <i class="bi bi-arrow-right-circle me-2"/>Impacto no mapa
        </BButton>
        <BButton
            :disabled="competencias.length === 0"
            data-testid="btn-cad-mapa-disponibilizar"
            variant="outline-success"
            @click="abrirModalDisponibilizar"
        >
          Disponibilizar
        </BButton>
      </div>
    </div>

    <div v-if="unidade">
      <div class="mb-4 mt-3">
        <BButton
            class="mb-3"
            data-testid="btn-abrir-criar-competencia"
            variant="outline-primary"
            @click="abrirModalCriarLimpo"
        >
          <i class="bi bi-plus-lg"/> Criar competência
        </BButton>

        <BCard
            v-for="comp in competencias"
            :key="comp.codigo"
            class="mb-2 competencia-card"
            data-testid="cad-mapa__card-competencia"
            no-body
        >
          <BCardBody>
            <div
                class="card-title fs-5 d-flex align-items-center competencia-edicao-row position-relative competencia-hover-row competencia-titulo-card"
            >
              <strong
                  class="competencia-descricao"
                  data-testid="cad-mapa__txt-competencia-descricao"
              > {{ comp.descricao }}</strong>
              <div class="ms-auto d-inline-flex align-items-center gap-1 botoes-acao">
                <BButton
                    v-b-tooltip.hover
                    class="botao-acao"
                    data-testid="btn-editar-competencia"
                    size="sm"
                    title="Editar"
                    variant="outline-primary"
                    @click="iniciarEdicaoCompetencia(comp)"
                >
                  <i class="bi bi-pencil"/>
                </BButton>
                <BButton
                    v-b-tooltip.hover
                    class="botao-acao"
                    data-testid="btn-excluir-competencia"
                    size="sm"
                    title="Excluir"
                    variant="outline-danger"
                    @click="excluirCompetencia(comp.codigo)"
                >
                  <i class="bi bi-trash"/>
                </BButton>
              </div>
            </div>
            <div class="d-flex flex-wrap gap-2 mt-2">
              <BCard
                  v-for="atvId in comp.atividadesAssociadas"
                  :key="atvId"
                  class="atividade-associada-card-item d-flex align-items-center group-atividade-associada"
                  no-body
              >
                <BCardBody class="d-flex align-items-center">
                  <span class="atividade-associada-descricao me-2 d-flex align-items-center">
                    {{ descricaoAtividade(atvId) }}
                    <span
                        v-if="(getAtividadeCompleta(atvId)?.conhecimentos.length ?? 0) > 0"
                        v-b-tooltip.html.top="getConhecimentosTooltip(atvId)"
                        class="badge bg-secondary ms-2"
                        data-testid="cad-mapa__txt-badge-conhecimentos-1"
                    >
                      {{ getAtividadeCompleta(atvId)?.conhecimentos.length }}
                    </span>
                  </span>
                  <BButton
                      v-b-tooltip.hover
                      class="botao-acao-inline"
                      data-testid="btn-remover-atividade-associada"
                      size="sm"
                      title="Remover Atividade"
                      variant="outline-secondary"
                      @click="removerAtividadeAssociada(comp.codigo, atvId)"
                  >
                    <i class="bi bi-trash"/>
                  </BButton>
                </BCardBody>
              </BCard>
            </div>
          </BCardBody>
        </BCard>
      </div>
    </div>
    <div v-else>
      <p>Unidade não encontrada.</p>
    </div>

    <!-- Modal de Criar Nova Competência -->
    <BModal
        v-model="mostrarModalCriarNovaCompetencia"
        :fade="false"
        :title="competenciaSendoEditada ? 'Edição de competência' : 'Criação de competência'"
        centered
        data-testid="mdl-criar-competencia"
        size="lg"
        @hidden="fecharModalCriarNovaCompetencia"
    >
      <!-- Conteúdo do card movido para cá -->
      <div class="mb-4">
        <h5>Descrição</h5>
        <div class="mb-2">
          <BFormTextarea
              v-model="novaCompetencia.descricao"
              data-testid="inp-criar-competencia-descricao"
              placeholder="Descreva a competência"
              rows="3"
          />
        </div>
      </div>

      <div class="mb-4">
        <h5>Atividades</h5>
        <div class="d-flex flex-wrap gap-2">
          <BCard
              v-for="atividade in atividades"
              :key="atividade.codigo"
              :class="atividadesSelecionadas.includes(atividade.codigo) ? 'atividade-card-item checked' : 'atividade-card-item'"
              :data-testid="atividadesSelecionadas.includes(atividade.codigo) ? 'atividade-associada' : 'atividade-nao-associada'"
              no-body
          >
            <BCardBody class="d-flex align-items-center">
              <BFormCheckbox
                  :id="`atv-${atividade.codigo}`"
                  v-model="atividadesSelecionadas"
                  :value="atividade.codigo"
                  data-testid="chk-criar-competencia-atividade"
              >
                {{ atividade.descricao }}
                <span
                    v-if="atividade.conhecimentos.length > 0"
                    v-b-tooltip.html.right="getConhecimentosModal(atividade)"
                    class="badge bg-secondary ms-2"
                    data-testid="cad-mapa__txt-badge-conhecimentos-2"
                >
                  {{ atividade.conhecimentos.length }}
                </span>
              </BFormCheckbox>
            </BCardBody>
          </BCard>
        </div>
      </div>
      <template #footer>
        <BButton
            variant="secondary"
            @click="fecharModalCriarNovaCompetencia"
        >
          Cancelar
        </BButton>
        <BButton
            v-b-tooltip.hover
            :disabled="atividadesSelecionadas.length === 0 || !novaCompetencia.descricao"
            data-testid="btn-criar-competencia-salvar"
            title="Criar Competência"
            variant="primary"
            @click="adicionarCompetenciaEFecharModal"
        >
          <i class="bi bi-save"/> Salvar
        </BButton>
      </template>
    </BModal>

    <!-- Modal de Disponibilizar -->
    <BModal
        v-model="mostrarModalDisponibilizar"
        :fade="false"
        centered
        data-testid="mdl-disponibilizar-mapa"
        title="Disponibilização do mapa de competências"
        @hidden="fecharModalDisponibilizar"
    >
      <div class="mb-3">
        <label
            class="form-label"
            for="dataLimite"
        >Data limite para validação</label>
        <BFormInput
            id="dataLimite"
            v-model="dataLimiteValidacao"
            data-testid="inp-disponibilizar-mapa-data"
            type="date"
        />
      </div>
      <div class="mb-3">
        <label
            class="form-label"
            for="observacoes"
        >Observações</label>
        <BFormTextarea
            id="observacoes"
            v-model="observacoesDisponibilizacao"
            data-testid="inp-disponibilizar-mapa-obs"
            placeholder="Digite observações sobre a disponibilização..."
            rows="3"
        />
      </div>
      <BAlert
          v-if="notificacaoDisponibilizacao"
          :fade="false"
          :model-value="true"
          class="mt-3"
          data-testid="alert-disponibilizar-mapa"
          variant="info"
      >
        {{ notificacaoDisponibilizacao }}
      </BAlert>
      <template #footer>
        <BButton
            data-testid="btn-disponibilizar-mapa-cancelar"
            variant="secondary"
            @click="fecharModalDisponibilizar"
        >
          Cancelar
        </BButton>
        <BButton
            :disabled="!dataLimiteValidacao"
            data-testid="btn-disponibilizar-mapa-confirmar"
            variant="success"
            @click="disponibilizarMapa"
        >
          Disponibilizar
        </BButton>
      </template>
    </BModal>

    <!-- Modal de Exclusão de Competência -->
    <BModal
        v-model="mostrarModalExcluirCompetencia"
        :fade="false"
        cancel-title="Cancelar"
        centered
        data-testid="mdl-excluir-competencia"
        ok-title="Confirmar"
        ok-variant="danger"
        title="Exclusão de competência"
        @hidden="fecharModalExcluirCompetencia"
        @ok="confirmarExclusaoCompetencia"
    >
      <p>Confirma a exclusão da competência "{{ competenciaParaExcluir?.descricao }}"?</p>
    </BModal>

    <ImpactoMapaModal
        :id-processo="codProcesso"
        :mostrar="mostrarModalImpacto"
        :sigla-unidade="siglaUnidade"
        @fechar="fecharModalImpacto"
    />
  </BContainer>
</template>

<script lang="ts" setup>
import {
  BAlert,
  BButton,
  BCard,
  BCardBody,
  BContainer,
  BFormCheckbox,
  BFormInput,
  BFormTextarea,
  BModal,
} from "bootstrap-vue-next";
import {storeToRefs} from "pinia";
import {computed, onMounted, ref} from "vue";
import {useRoute, useRouter} from "vue-router";
import ImpactoMapaModal from "@/components/ImpactoMapaModal.vue";
import {usePerfil} from "@/composables/usePerfil";
import {situacaoLabel} from "@/utils";
import {useAtividadesStore} from "@/stores/atividades";
import {useMapasStore} from "@/stores/mapas";
import {useSubprocessosStore} from "@/stores/subprocessos";
import {useUnidadesStore} from "@/stores/unidades";
import type {Atividade, Competencia} from "@/types/tipos";

const route = useRoute();
const router = useRouter();
const mapasStore = useMapasStore();
const {mapaCompleto} = storeToRefs(mapasStore);
const atividadesStore = useAtividadesStore();
const subprocessosStore = useSubprocessosStore();
const unidadesStore = useUnidadesStore();
usePerfil();

const codProcesso = computed(() => Number(route.params.codProcesso));
const siglaUnidade = computed(() => String(route.params.siglaUnidade));

const podeVerImpacto = computed(() => {
  return (
      subprocessosStore.subprocessoDetalhe?.permissoes?.podeVisualizarImpacto ||
      false
  );
});

const mostrarModalImpacto = ref(false);

function abrirModalImpacto() {
  mostrarModalImpacto.value = true;
}

function fecharModalImpacto() {
  mostrarModalImpacto.value = false;
}

const unidade = computed(() => unidadesStore.unidade);
const codSubrocesso = ref<number | null>(null);

onMounted(async () => {
  // Carrega unidade diretamente
  await unidadesStore.buscarUnidade(siglaUnidade.value);

  // Busca ID do subprocesso
  const id = await subprocessosStore.buscarSubprocessoPorProcessoEUnidade(
      codProcesso.value,
      siglaUnidade.value,
  );

  if (id) {
    codSubrocesso.value = id;
    await Promise.all([
      mapasStore.buscarMapaCompleto(id),
      subprocessosStore.buscarSubprocessoDetalhe(id),
      atividadesStore.buscarAtividadesParaSubprocesso(id),
    ]);
  }
});

const atividades = computed<Atividade[]>(() => {
  if (typeof codSubrocesso.value !== "number") {
    return [];
  }
  return atividadesStore.obterAtividadesPorSubprocesso(codSubrocesso.value) || [];
});

const competencias = computed(() => mapaCompleto.value?.competencias || []);
const atividadesSelecionadas = ref<number[]>([]);
const novaCompetencia = ref({descricao: ""});

const competenciaSendoEditada = ref<Competencia | null>(null);

const mostrarModalCriarNovaCompetencia = ref(false);
const mostrarModalDisponibilizar = ref(false);
const mostrarModalExcluirCompetencia = ref(false);
const competenciaParaExcluir = ref<Competencia | null>(null);
const dataLimiteValidacao = ref("");
const observacoesDisponibilizacao = ref("");
const notificacaoDisponibilizacao = ref("");

function abrirModalDisponibilizar() {
  mostrarModalDisponibilizar.value = true;
}

function abrirModalCriarNovaCompetencia(competenciaParaEditar?: Competencia) {
  mostrarModalCriarNovaCompetencia.value = true;
  if (competenciaParaEditar) {
    novaCompetencia.value.descricao = competenciaParaEditar.descricao;
    atividadesSelecionadas.value = [
      ...(competenciaParaEditar.atividadesAssociadas || []),
    ];
    competenciaSendoEditada.value = competenciaParaEditar;
  } else {
    novaCompetencia.value.descricao = "";
    atividadesSelecionadas.value = [];
    competenciaSendoEditada.value = null;
  }
}

function abrirModalCriarLimpo() {
  competenciaSendoEditada.value = null;
  abrirModalCriarNovaCompetencia();
}

function fecharModalCriarNovaCompetencia() {
  mostrarModalCriarNovaCompetencia.value = false;
}

function iniciarEdicaoCompetencia(competencia: Competencia) {
  competenciaSendoEditada.value = competencia;
  abrirModalCriarNovaCompetencia(competencia);
}

function descricaoAtividade(codigo: number): string {
  const atv = atividades.value.find((a) => a.codigo === codigo);
  return atv ? atv.descricao : "Atividade não encontrada";
}

function getConhecimentosTooltip(atividadeId: number): string {
  const atividade = atividades.value.find((a) => a.codigo === atividadeId);
  if (!atividade || !atividade.conhecimentos.length) {
    return "Nenhum conhecimento cadastrado";
  }

  const conhecimentosHtml = atividade.conhecimentos
      .map((c) => `<div class="mb-1">• ${c.descricao}</div>`)
      .join("");

  return `<div class="text-start"><strong>Conhecimentos:</strong><br>${conhecimentosHtml}</div>`;
}

function getAtividadeCompleta(codigo: number): Atividade | undefined {
  return atividades.value.find((a) => a.codigo === codigo);
}

function getConhecimentosModal(atividade: Atividade): string {
  if (!atividade.conhecimentos.length) {
    return "Nenhum conhecimento";
  }

  const conhecimentosHtml = atividade.conhecimentos
      .map((c) => `<div class="mb-1">• ${c.descricao}</div>`)
      .join("");

  return `<div class="text-start"><strong>Conhecimentos:</strong><br>${conhecimentosHtml}</div>`;
}

async function adicionarCompetenciaEFecharModal() {
  // Validações agora são feitas no backend via Bean Validation
  const competencia: Competencia = {
    // deixe undefined para novas competências e permita backend atribuir o id
    codigo: competenciaSendoEditada.value?.codigo ?? undefined,
    descricao: novaCompetencia.value.descricao,
    atividadesAssociadas: atividadesSelecionadas.value,
  } as any;

  try {
    if (competenciaSendoEditada.value) {
      await mapasStore.atualizarCompetencia(codSubrocesso.value as number, competencia);
    } else {
      await mapasStore.adicionarCompetencia(codSubrocesso.value as number, competencia);
    }

    // Recarregar mapa completo para garantir que novas competências tenham seus códigos
    await mapasStore.buscarMapaCompleto(codSubrocesso.value as number);
  } finally {
    // Limpar formulário e fechar modal independente do resultado (o feedbackStore já notifica erros)
    novaCompetencia.value.descricao = "";
    atividadesSelecionadas.value = [];
    competenciaSendoEditada.value = null;

    fecharModalCriarNovaCompetencia();
  }
}

function excluirCompetencia(codigo: number) {
  const competencia = competencias.value.find((comp) => comp.codigo === codigo);
  if (competencia) {
    competenciaParaExcluir.value = competencia;
    mostrarModalExcluirCompetencia.value = true;
  }
}

function confirmarExclusaoCompetencia() {
  if (competenciaParaExcluir.value) {
    mapasStore.removerCompetencia(
        codSubrocesso.value as number,
        competenciaParaExcluir.value.codigo,
    );
    fecharModalExcluirCompetencia();
  }
}

function fecharModalExcluirCompetencia() {
  mostrarModalExcluirCompetencia.value = false;
  competenciaParaExcluir.value = null;
}

function removerAtividadeAssociada(competenciaId: number, atividadeId: number) {
  const competencia = competencias.value.find(
      (comp) => comp.codigo === competenciaId,
  );
  if (competencia) {
    const competenciaAtualizada = {
      ...competencia,
      atividadesAssociadas: competencia.atividadesAssociadas.filter(
          (id) => id !== atividadeId,
      ),
    };
    mapasStore.atualizarCompetencia(
        codSubrocesso.value as number,
        competenciaAtualizada,
    );
  }
}

async function disponibilizarMapa() {
  if (!codSubrocesso.value) return;

  try {
    await mapasStore.disponibilizarMapa(codSubrocesso.value, {
      dataLimite: dataLimiteValidacao.value,
      observacoes: observacoesDisponibilizacao.value,
    });
    // Recarregar mapa para atualizar a situacao exibida
    await mapasStore.buscarMapaCompleto(codSubrocesso.value as number);
    await subprocessosStore.buscarSubprocessoDetalhe(codSubrocesso.value as number);
    fecharModalDisponibilizar();
    await router.push({name: "Painel"});
  } catch {
    // O erro já é tratado e notificado pelo store
  }
}

function fecharModalDisponibilizar() {
  mostrarModalDisponibilizar.value = false;
  observacoesDisponibilizacao.value = "";
  notificacaoDisponibilizacao.value = "";
}
</script>

<style scoped>
.competencia-card {
  transition: box-shadow 0.2s;
}

.competencia-card:hover {
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.07);
}

.botoes-acao {
  opacity: 1; /* sempre visível para evitar flakiness em testes */
  pointer-events: auto;
  transition: opacity 0.2s;
}

.competencia-hover-row:hover .botoes-acao {
  opacity: 1;
  pointer-events: auto;
}

.botao-acao {
  width: 2rem;
  height: 2rem;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0;
  font-size: 1.1rem;
  border-width: 2px;
  transition: background 0.15s, border-color 0.15s, color 0.15s;
  margin-left: 0;
  margin-right: 0;
}

.botao-acao:focus,
.botao-acao:hover {
  background: var(--bs-primary-bg-subtle);
  box-shadow: 0 0 0 2px var(--bs-primary);
}

.competencia-descricao {
  word-break: break-word;
  max-width: 100%;
  display: inline-block;
}

.competencia-hover-row:hover .competencia-descricao {
  font-weight: bold;
}

.competencia-edicao-row {
  width: 100%;
  justify-content: flex-start;
}

.competencia-titulo-card {
  background: var(--bs-light);
  border-bottom: 1px solid var(--bs-border-color);
  padding: 0.5rem 0.75rem;
  margin-left: -0.75rem;
  margin-right: -0.75rem;
  margin-top: -0.5rem;
  border-top-left-radius: 0.375rem;
  border-top-right-radius: 0.375rem;
  /* Ajuste para preencher a largura total do card */
  width: calc(100% + 1.5rem); /* 100% + 2 * 0.75rem (padding horizontal) */
}

.competencia-titulo-card .competencia-descricao {
  font-size: 1.1rem;
}

.atividade-card-item {
  cursor: pointer;
  border: 1px solid var(--bs-border-color);
  border-radius: 0.375rem;
  transition: all 0.2s ease-in-out;
  background-color: var(--bs-body-bg);
}

.atividade-card-item:hover {
  border-color: var(--bs-primary);
  box-shadow: 0 0 0 0.25rem var(--bs-primary);
}

.atividade-card-item.checked {
  background-color: var(--bs-primary-bg-subtle);
  border-color: var(--bs-primary);
}

.atividade-card-item .form-check-label {
  cursor: pointer;
  padding: 0.25rem 0;
}

.atividade-card-item.checked .form-check-label {
  font-weight: bold;
  color: var(--bs-primary);
}

.atividade-card-item .card-body {
  padding: 0.5rem 0.75rem;
}

.atividade-associada-card-item {
  border: 1px solid var(--bs-border-color);
  border-radius: 0.375rem;
  background-color: var(--bs-secondary-bg);
}

.atividade-associada-descricao {
  font-size: 0.85rem;
  color: var(--bs-body-color);
}

.botao-acao-inline {
  width: 1.5rem;
  height: 1.5rem;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0;
  font-size: 0.8rem;
  border-width: 1px;
  transition: background 0.15s, border-color 0.15s, color 0.15s;
}

</style>
