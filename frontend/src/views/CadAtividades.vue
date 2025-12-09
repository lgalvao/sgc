<template>
  <BContainer class="mt-4">
    <div class="d-flex align-items-center mb-3">
      <BButton
        variant="link"
        class="p-0 me-3 text-decoration-none"
        data-testid="btn-cad-atividades-voltar"
        @click="router.back()"
      >
        <i class="bi bi-arrow-left fs-4" />
      </BButton>
      <div class="fs-5 d-flex align-items-center gap-2">
        <span>{{ siglaUnidade }} - {{ nomeUnidade }}</span>
        <span
          v-if="subprocesso"
          :class="badgeClass(subprocesso.situacaoSubprocesso)"
          class="badge fs-6"
          data-testid="cad-atividades__txt-badge-situacao"
        >{{ subprocesso.situacaoLabel || situacaoLabel(subprocesso.situacaoSubprocesso) }}</span>
      </div>
    </div>

    <div class="d-flex justify-content-between align-items-center mb-3">
      <h1 class="mb-0 display-6">
        Atividades e conhecimentos
      </h1>

      <div class="d-flex gap-2">
        <BButton
          v-if="podeVerImpacto"
          variant="outline-secondary"
          data-testid="cad-atividades__btn-impactos-mapa"
          @click="abrirModalImpacto"
        >
          <i class="bi bi-arrow-right-circle me-2" />Impacto no mapa
        </BButton>
        <BButton
          v-if="isChefe && historicoAnalises.length > 0"
          variant="outline-info"
          data-testid="btn-cad-atividades-historico"
          @click="abrirModalHistorico"
        >
          Histórico de análise
        </BButton>
        <BButton
          v-if="isChefe"
          variant="outline-primary"
          data-testid="btn-cad-atividades-importar"
          title="Importar"
          @click="mostrarModalImportar = true"
        >
          Importar atividades
        </BButton>
        <BButton
          v-if="isChefe"
          variant="outline-success"
          data-testid="btn-cad-atividades-disponibilizar"
          title="Disponibilizar"
          @click="disponibilizarCadastro"
        >
          Disponibilizar
        </BButton>
      </div>
    </div>

    <BForm
      class="row g-2 align-items-center mb-4"
      data-testid="form-nova-atividade"
      @submit.prevent="adicionarAtividade"
    >
      <BCol>
        <BFormInput
          v-model="novaAtividade"
          data-testid="inp-nova-atividade"
          placeholder="Nova atividade"
          type="text"
          aria-label="Nova atividade"
        />
      </BCol>
      <BCol cols="auto">
        <BButton
          variant="outline-primary"
          size="sm"
          data-testid="btn-adicionar-atividade"
          title="Adicionar atividade"
          type="submit"
          :disabled="!codSubrocesso"
        >
          <i
            class="bi bi-save"
          />
        </BButton>
      </BCol>
    </BForm>

    <BCard
      v-for="(atividade, idx) in atividades"
      :key="atividade.codigo || idx"
      class="mb-3 atividade-card"
      no-body
    >
      <BCardBody class="py-2">
        <div
          class="card-title d-flex align-items-center atividade-edicao-row position-relative group-atividade atividade-hover-row atividade-titulo-card"
        >
          <template v-if="editandoAtividade === atividade.codigo">
            <BFormInput
              v-model="atividadeEditada"
              class="me-2 atividade-edicao-input"
              data-testid="inp-editar-atividade"
              aria-label="Editar atividade"
            />
            <BButton
              variant="outline-success"
              size="sm"
              class="me-1 botao-acao"
              data-testid="btn-salvar-edicao-atividade"
              title="Salvar"
              @click="salvarEdicaoAtividade(atividade.codigo)"
            >
              <i class="bi bi-save" />
            </BButton>
            <BButton
              variant="outline-secondary"
              size="sm"
              class="botao-acao"
              data-testid="btn-cancelar-edicao-atividade"
              title="Cancelar"
              @click="cancelarEdicaoAtividade()"
            >
              <i class="bi bi-x" />
            </BButton>
          </template>

          <template v-else>
            <strong
              class="atividade-descricao"
              data-testid="cad-atividades__txt-atividade-descricao"
            >{{ atividade?.descricao }}</strong>
            <div class="d-inline-flex align-items-center gap-1 ms-3 botoes-acao-atividade fade-group">
              <BButton
                variant="outline-primary"
                size="sm"
                class="botao-acao"
                data-testid="btn-editar-atividade"
                title="Editar"
                @click="iniciarEdicaoAtividade(atividade.codigo, atividade.descricao)"
              >
                <i
                  class="bi bi-pencil"
                />
              </BButton>
              <BButton
                variant="outline-danger"
                size="sm"
                class="botao-acao"
                data-testid="btn-remover-atividade"
                title="Remover"
                @click="removerAtividade(idx)"
              >
                <i
                  class="bi bi-trash"
                />
              </BButton>
            </div>
          </template>
        </div>

        <div class="mt-3 ms-3">
          <div
            v-for="(conhecimento, cidx) in atividade.conhecimentos"
            :key="conhecimento.id"
            class="d-flex align-items-center mb-2 group-conhecimento position-relative conhecimento-hover-row"
          >
            <template v-if="conhecimentoEmEdicao && conhecimentoEmEdicao.conhecimentoId === conhecimento.id">
                <BFormInput
                  v-model="conhecimentoEmEdicao.descricao"
                  class="me-2"
                  size="sm"
                  data-testid="inp-editar-conhecimento"
                  aria-label="Editar conhecimento"
                />
                <BButton
                  variant="outline-success"
                  size="sm"
                  class="me-1 botao-acao"
                  data-testid="btn-salvar-edicao-conhecimento"
                  title="Salvar"
                  @click="salvarEdicaoConhecimento"
                >
                  <i class="bi bi-save" />
                </BButton>
                <BButton
                  variant="outline-secondary"
                  size="sm"
                  class="botao-acao"
                  data-testid="btn-cancelar-edicao-conhecimento"
                  title="Cancelar"
                  @click="cancelarEdicaoConhecimento"
                >
                  <i class="bi bi-x" />
                </BButton>
            </template>
            <template v-else>
                <span data-testid="cad-atividades__txt-conhecimento-descricao">{{ conhecimento?.descricao }}</span>
                <div class="d-inline-flex align-items-center gap-1 ms-3 botoes-acao fade-group">
                  <BButton
                    variant="outline-primary"
                    size="sm"
                    class="botao-acao"
                    data-testid="btn-editar-conhecimento"
                    title="Editar"
                    @click="iniciarEdicaoConhecimento(atividade.codigo, conhecimento)"
                  >
                    <i class="bi bi-pencil" />
                  </BButton>
                  <BButton
                    variant="outline-danger"
                    size="sm"
                    class="botao-acao"
                    data-testid="btn-remover-conhecimento"
                    title="Remover"
                    @click="removerConhecimento(idx, cidx)"
                  >
                    <i class="bi bi-trash" />
                  </BButton>
                </div>
            </template>
          </div>
          <BForm
            class="row g-2 align-items-center"
            data-testid="form-novo-conhecimento"
            @submit.prevent="adicionarConhecimento(idx)"
          >
            <BCol>
              <BFormInput
                v-model="atividade.novoConhecimento"
                size="sm"
                data-testid="inp-novo-conhecimento"
                placeholder="Novo conhecimento"
                type="text"
                aria-label="Novo conhecimento"
              />
            </BCol>
            <BCol cols="auto">
              <BButton
                variant="outline-secondary"
                size="sm"
                data-testid="btn-adicionar-conhecimento"
                title="Adicionar Conhecimento"
                type="submit"
              >
                <i
                  class="bi bi-save"
                />
              </BButton>
            </BCol>
          </BForm>
        </div>
      </BCardBody>
    </BCard>

    <ImportarAtividadesModal
      :mostrar="mostrarModalImportar"
      :cod-subrocesso-destino="codSubrocesso"
      @fechar="mostrarModalImportar = false"
      @importar="handleImportAtividades"
    />

    <ImpactoMapaModal
      :id-processo="codProcesso"
      :mostrar="mostrarModalImpacto"
      :sigla-unidade="siglaUnidade"
      @fechar="fecharModalImpacto"
    />

    <BModal
      v-model="mostrarModalConfirmacao"
      :fade="false"
      :title="isRevisao ? 'Disponibilização da revisão do cadastro' : 'Disponibilização do cadastro'"
      centered
      hide-footer
    >
      <template #default>
        <p>
          {{
            isRevisao ? 'Confirma a finalização da revisão e a disponibilização do cadastro?' : 'Confirma a finalização e a disponibilização do cadastro?'
          }} Essa ação bloqueia a edição e habilita a análise do cadastro por unidades superiores.
        </p>
      </template>
      <template #footer>
        <BButton
          variant="secondary"
          @click="fecharModalConfirmacao"
        >
          Cancelar
        </BButton>
        <BButton
          variant="success"
          data-testid="btn-confirmar-disponibilizacao"
          @click="confirmarDisponibilizacao"
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
      <div class="table-responsive">
        <table
          class="table table-striped"
          data-testid="cad-atividades__tbl-historico-analise"
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
              v-for="(analise, index) in historicoAnalises"
              :key="analise.codigo"
              :data-testid="`row-historico-${index}`"
            >
              <td :data-testid="`cell-data-${index}`">{{ formatarData(analise.dataHora) }}</td>
              <td :data-testid="`cell-unidade-${index}`">{{ 'unidade' in analise ? analise.unidade : analise.unidadeSigla }}</td>
              <td :data-testid="`cell-resultado-${index}`">{{ formatarAcaoAnalise(analise.acao || analise.resultado) }}</td>
              <td :data-testid="`cell-observacao-${index}`">{{ analise.observacoes || '-' }}</td>
            </tr>
          </tbody>
        </table>
      </div>
      <template #footer>
        <BButton
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
import {BButton, BCard, BCardBody, BCol, BContainer, BForm, BFormInput, BModal} from "bootstrap-vue-next";
import {computed, onMounted, ref} from "vue";
import {useRouter} from "vue-router";
import {badgeClass, situacaoLabel} from "@/utils";
import ImpactoMapaModal from "@/components/ImpactoMapaModal.vue";
import ImportarAtividadesModal from "@/components/ImportarAtividadesModal.vue";
import {usePerfil} from "@/composables/usePerfil";
import {useAnalisesStore} from "@/stores/analises";
import {useAtividadesStore} from "@/stores/atividades";
import {useMapasStore} from "@/stores/mapas";
import {useFeedbackStore} from "@/stores/feedback";

import {useProcessosStore} from "@/stores/processos";
import {useSubprocessosStore} from "@/stores/subprocessos";
import {useUnidadesStore} from "@/stores/unidades";
import {
  type Atividade,
  type Conhecimento,
  type CriarAtividadeRequest,
  type CriarConhecimentoRequest,
  Perfil,
  SituacaoSubprocesso,
  TipoProcesso,
} from "@/types/tipos";

interface AtividadeComEdicao extends Atividade {
  novoConhecimento?: string;
}

interface ConhecimentoEdicao {
  atividadeId: number;
  conhecimentoId: number;
  descricao: string;
}

const props = defineProps<{
  codProcesso: number | string;
  sigla: string;
}>();

const unidadeId = computed(() => props.sigla);
const codProcesso = computed(() => Number(props.codProcesso));

const atividadesStore = useAtividadesStore();
const unidadesStore = useUnidadesStore();
const processosStore = useProcessosStore();
const subprocessosStore = useSubprocessosStore();
const analisesStore = useAnalisesStore();
const feedbackStore = useFeedbackStore();

const router = useRouter();

useMapasStore();

const unidade = computed(() => unidadesStore.unidade);

const siglaUnidade = computed(() => unidade.value?.sigla || props.sigla);
const nomeUnidade = computed(() =>
  unidade.value?.nome ? `${unidade.value.nome}` : "",
);
const novaAtividade = ref("");
const codSubrocesso = computed(
  () =>
    processosStore.processoDetalhe?.unidades.find(
      (u) => u.sigla === unidadeId.value,
    )?.codSubprocesso,
);

const codMapa = computed(
  () =>
    processosStore.processoDetalhe?.unidades.find(
      (u) => u.sigla === unidadeId.value,
    )?.mapaCodigo,
);

const atividades = computed<AtividadeComEdicao[]>({
  get: () => {
    if (codSubrocesso.value === undefined) return [];
    const result = atividadesStore
      .obterAtividadesPorSubprocesso(codSubrocesso.value)
      .map((a) => ({ ...a, novoConhecimento: "" }));
    return result;
  },
  set: () => {},
});

const processoAtual = computed(() => processosStore.processoDetalhe);
const isRevisao = computed(
  () => processoAtual.value?.tipo === TipoProcesso.REVISAO,
);

async function adicionarAtividade() {
  if (novaAtividade.value?.trim() && codMapa.value && codSubrocesso.value) {
    const request: CriarAtividadeRequest = {
      descricao: novaAtividade.value.trim(),
    };
    await atividadesStore.adicionarAtividade(
      codSubrocesso.value,
      codMapa.value,
      request,
    );
    novaAtividade.value = "";
    // Status do subprocesso já foi atualizado pela store
  }
}

async function removerAtividade(idx: number) {
  if (!codSubrocesso.value) return;
  const atividadeRemovida = atividades.value[idx];
  if (
    confirm(
      "Confirma a remoção desta atividade e todos os conhecimentos associados?",
    )
  ) {
    await atividadesStore.removerAtividade(
      codSubrocesso.value,
      atividadeRemovida.codigo,
    );
    // Status do subprocesso já foi atualizado pela store
  }
}

async function adicionarConhecimento(idx: number) {
  if (!codSubrocesso.value) return;
  const atividade = atividades.value[idx];
  if (atividade.novoConhecimento?.trim()) {
    const request: CriarConhecimentoRequest = {
      descricao: atividade.novoConhecimento.trim(),
    };
    await atividadesStore.adicionarConhecimento(
      codSubrocesso.value,
      atividade.codigo,
      request,
    );
    atividade.novoConhecimento = "";
    // Status do subprocesso já foi atualizado pela store
  }
}

async function removerConhecimento(idx: number, cidx: number) {
  if (!codSubrocesso.value) return;
  const atividade = atividades.value[idx];
  const conhecimentoRemovido = atividade.conhecimentos[cidx];
  if (confirm("Confirma a remoção deste conhecimento?")) {
    await atividadesStore.removerConhecimento(
      codSubrocesso.value,
      atividade.codigo,
      conhecimentoRemovido.id,
    );
  }
}

const conhecimentoEmEdicao = ref<ConhecimentoEdicao | null>(null);

function iniciarEdicaoConhecimento(atividadeId: number, conhecimento: Conhecimento) {
  conhecimentoEmEdicao.value = {
    atividadeId,
    conhecimentoId: conhecimento.id,
    descricao: conhecimento.descricao,
  };
}

function cancelarEdicaoConhecimento() {
  conhecimentoEmEdicao.value = null;
}

async function salvarEdicaoConhecimento() {
  if (!codSubrocesso.value || !conhecimentoEmEdicao.value) return;

  if (conhecimentoEmEdicao.value.descricao.trim()) {
      const conhecimentoAtualizado: Conhecimento = {
        id: conhecimentoEmEdicao.value.conhecimentoId,
        descricao: conhecimentoEmEdicao.value.descricao.trim(),
      };
      await atividadesStore.atualizarConhecimento(
        codSubrocesso.value,
        conhecimentoEmEdicao.value.atividadeId,
        conhecimentoEmEdicao.value.conhecimentoId,
        conhecimentoAtualizado,
      );
  }
  cancelarEdicaoConhecimento();
}

const editandoAtividade = ref<number | null>(null);
const atividadeEditada = ref("");

function iniciarEdicaoAtividade(id: number, valorAtual: string) {
  editandoAtividade.value = id;
  atividadeEditada.value = valorAtual;
}

async function salvarEdicaoAtividade(id: number) {
  if (String(atividadeEditada.value).trim() && codSubrocesso.value) {
    const atividadeOriginal = atividades.value.find((a) => a.codigo === id);
    if (atividadeOriginal) {
      const atividadeAtualizada: Atividade = {
        ...atividadeOriginal,
        descricao: atividadeEditada.value.trim(),
      };
      await atividadesStore.atualizarAtividade(
        codSubrocesso.value,
        id,
        atividadeAtualizada,
      );
    }
  }
  cancelarEdicaoAtividade();
}

function cancelarEdicaoAtividade() {
  editandoAtividade.value = null;
  atividadeEditada.value = "";
}

async function handleImportAtividades() {
  mostrarModalImportar.value = false;
  if (codSubrocesso.value) {
    await atividadesStore.buscarAtividadesParaSubprocesso(codSubrocesso.value);
  }
  feedbackStore.show(
    "Importação Concluída",
    "As atividades foram importadas para o seu mapa.",
    "success"
  );
}

const { perfilSelecionado } = usePerfil();

const isChefe = computed(() => perfilSelecionado.value === Perfil.CHEFE);

const subprocesso = computed(() => {
  if (!processosStore.processoDetalhe) return null;
  return processosStore.processoDetalhe.unidades.find(
    (u) => u.sigla === unidadeId.value,
  );
});

const podeVerImpacto = computed(() => {
  if (!subprocesso.value) return false;

  const situacao = subprocesso.value.situacaoSubprocesso;

  // 3.1. CHEFE
  if (isChefe.value) {
    return situacao === SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO ||
           (isRevisao.value && situacao === SituacaoSubprocesso.NAO_INICIADO);
  }
  
  // 3.2. GESTOR ou ADMIN (Visualização)
  // Note: This component is also used by ADMIN/GESTOR to view/edit if they are "editing" 
  // but usually they use VisAtividades.vue for read-only.
  // However, if they land here, we should support it.
  if (perfilSelecionado.value === Perfil.GESTOR || perfilSelecionado.value === Perfil.ADMIN) {
      // Check location logic would be needed here, but for now checking status:
      return situacao === SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA;
  }

  return false;
});

const mostrarModalImpacto = ref(false);
const mostrarModalImportar = ref(false);
const mostrarModalConfirmacao = ref(false);
const mostrarModalHistorico = ref(false);



onMounted(async () => {
  await unidadesStore.buscarUnidade(props.sigla);
  await processosStore.buscarProcessoDetalhe(codProcesso.value);
  if (codSubrocesso.value) {
    await atividadesStore.buscarAtividadesParaSubprocesso(codSubrocesso.value);
    await analisesStore.buscarAnalisesCadastro(codSubrocesso.value);
  }
});



const historicoAnalises = computed(() => {
  if (!codSubrocesso.value) return [];
  return analisesStore.obterAnalisesPorSubprocesso(codSubrocesso.value);
});

function formatarData(data: string): string {
  return new Date(data).toLocaleString("pt-BR");
}

function abrirModalHistorico() {
  mostrarModalHistorico.value = true;
}

function fecharModalHistorico() {
  mostrarModalHistorico.value = false;
}

function formatarAcaoAnalise(acao: string | undefined): string {
  if (!acao) return '';
  switch (acao) {
    case 'DEVOLUCAO_MAPEAMENTO':
    case 'DEVOLUCAO_REVISAO':
      return 'Devolução';
    case 'ACEITE_MAPEAMENTO':
    case 'ACEITE_REVISAO':
      return 'Aceite';
    default:
      return acao;
  }
}

function disponibilizarCadastro() {
  const sub = subprocesso.value;
  const situacaoEsperada = isRevisao.value
    ? SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO
    : SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO;

  if (!sub || sub.situacaoSubprocesso !== situacaoEsperada) {
    feedbackStore.show(
      "Ação não permitida",
      `Ação permitida apenas na situação: "${situacaoEsperada}".`,
      "danger"
    );
    return;
  }

  // Backend valida atividades sem conhecimento via SubprocessoCadastroController
  mostrarModalConfirmacao.value = true;
}

function fecharModalConfirmacao() {
  mostrarModalConfirmacao.value = false;
}

async function confirmarDisponibilizacao() {
  if (!codSubrocesso.value) return;

  let sucesso = false;
  if (isRevisao.value) {
    sucesso = await subprocessosStore.disponibilizarRevisaoCadastro(codSubrocesso.value);
  } else {
    sucesso = await subprocessosStore.disponibilizarCadastro(codSubrocesso.value);
  }

  fecharModalConfirmacao();
  if (sucesso) {
    await router.push("/painel");
  }
}

function abrirModalImpacto() {
  mostrarModalImpacto.value = true;
}

function fecharModalImpacto() {
  mostrarModalImpacto.value = false;
}
</script>

<style>
.atividade-edicao-input {
  flex-grow: 1;
  min-width: 0;
}

.atividade-card {
  transition: box-shadow 0.2s;
}

.atividade-card:hover {
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.07);
}

.botoes-acao-atividade,
.botoes-acao {
  opacity: 0;
  pointer-events: none;
  transition: opacity 0.2s;
}

.atividade-hover-row:hover .botoes-acao-atividade,
.conhecimento-hover-row:hover .botoes-acao {
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

.fade-group {
  transition: opacity 0.2s;
}

.atividade-descricao {
  word-break: break-word;
  max-width: 100%;
  display: inline-block;
}

.conhecimento-hover-row:hover span {
  font-weight: bold;
}

.atividade-hover-row:hover .atividade-descricao {
  font-weight: bold;
}

.atividade-titulo-card {
  background: var(--bs-light);
  border-bottom: 1px solid var(--bs-border-color);
  padding: 0.5rem 0.75rem;
  margin-left: -0.75rem;
  margin-right: -0.75rem;
  margin-top: -0.5rem;
  border-top-left-radius: 0.375rem;
  border-top-right-radius: 0.375rem;
}


</style>

