<template>
  <BContainer class="mt-4">
    <div class="fs-5 w-100 mb-3">
      {{ siglaUnidade }} - {{ nomeUnidade }}
    </div>

    <div class="d-flex justify-content-between align-items-center mb-3">
      <h1 class="mb-0 display-6">
        Atividades e conhecimentos
      </h1>

      <div class="d-flex gap-2">
        <BButton
          v-if="podeVerImpacto"
          variant="outline-secondary"
          data-testid="impactos-mapa-button"
          @click="abrirModalImpacto"
        >
          <i class="bi bi-arrow-right-circle me-2" />Impacto no mapa
        </BButton>
        <BButton
          v-if="isChefe && historicoAnalises.length > 0"
          variant="outline-info"
          @click="abrirModalHistorico"
        >
          Histórico de análise
        </BButton>
        <BButton
          v-if="isChefe"
          variant="outline-primary"
          title="Importar"
          @click="mostrarModalImportar = true"
        >
          Importar atividades
        </BButton>
        <BButton
          v-if="isChefe"
          variant="outline-success"
          data-testid="btn-disponibilizar"
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
          data-testid="input-nova-atividade"
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
              data-testid="input-editar-atividade"
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
              data-testid="atividade-descricao"
            >{{ atividade.descricao }}</strong>
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
            <template v-if="conhecimentoEmEdicao?.conhecimentoId === conhecimento.id">
                <BFormInput
                  v-model="conhecimentoEmEdicao.descricao"
                  class="me-2"
                  size="sm"
                  data-testid="input-editar-conhecimento"
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
                <span data-testid="conhecimento-descricao">{{ conhecimento.descricao }}</span>
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
                data-testid="input-novo-conhecimento"
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
        <BAlert
          v-if="atividadesSemConhecimento.length > 0"
          variant="warning"
          :model-value="true"
        >
          <strong>Atenção:</strong> As seguintes atividades não têm conhecimentos associados:
          <ul>
            <li
              v-for="atividade in atividadesSemConhecimento"
              :key="atividade.codigo"
            >
              {{ atividade.descricao }}
            </li>
          </ul>
        </BAlert>
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
      title="Histórico de Análise"
      centered
      size="lg"
      hide-footer
    >
      <div class="table-responsive">
        <table
          class="table table-striped"
          data-testid="historico-analise-tabela"
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
              v-for="analise in historicoAnalises"
              :key="analise.codigo"
            >
              <td>{{ formatarData(analise.dataHora) }}</td>
              <td>{{ 'unidade' in analise ? analise.unidade : analise.unidadeSigla }}</td>
              <td>{{ analise.resultado }}</td>
              <td>{{ analise.observacoes || '-' }}</td>
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
import {BAlert, BButton, BCard, BCardBody, BCol, BContainer, BForm, BFormInput, BModal,} from "bootstrap-vue-next";
import {computed, onMounted, ref} from "vue";
import {useRouter} from "vue-router";
import ImpactoMapaModal from "@/components/ImpactoMapaModal.vue";
import ImportarAtividadesModal from "@/components/ImportarAtividadesModal.vue";
import {usePerfil} from "@/composables/usePerfil";
import {useAnalisesStore} from "@/stores/analises";
import {useAtividadesStore} from "@/stores/atividades";
import {useMapasStore} from "@/stores/mapas";
import {useNotificacoesStore} from "@/stores/notificacoes";
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
const notificacoesStore = useNotificacoesStore();
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
    )?.codUnidade,
);

const atividades = computed<AtividadeComEdicao[]>({
  get: () => {
    if (codSubrocesso.value === undefined) return [];
    return atividadesStore
      .obterAtividadesPorSubprocesso(codSubrocesso.value)
      .map((a) => ({ ...a, novoConhecimento: "" }));
  },
  set: () => {},
});

const processoAtual = computed(() => processosStore.processoDetalhe);
const isRevisao = computed(
  () => processoAtual.value?.tipo === TipoProcesso.REVISAO,
);

async function adicionarAtividade() {
  if (novaAtividade.value?.trim() && codSubrocesso.value) {
    const request: CriarAtividadeRequest = {
      descricao: novaAtividade.value.trim(),
    };
    await atividadesStore.adicionarAtividade(codSubrocesso.value, request);
    novaAtividade.value = "";
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
  notificacoesStore.sucesso(
    "Importação Concluída",
    "As atividades foram importadas para o seu mapa.",
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
  if (!isChefe.value || !subprocesso.value) return false;
  return (
    subprocesso.value.situacaoSubprocesso ===
    SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO
  );
});

const mostrarModalImpacto = ref(false);
const mostrarModalImportar = ref(false);
const mostrarModalConfirmacao = ref(false);
const mostrarModalHistorico = ref(false);
const atividadesSemConhecimento = ref<Atividade[]>([]);



onMounted(async () => {
  await unidadesStore.buscarUnidade(props.sigla);
  await processosStore.buscarProcessoDetalhe(codProcesso.value);
  if (codSubrocesso.value) {
    await atividadesStore.buscarAtividadesParaSubprocesso(codSubrocesso.value);
    await analisesStore.buscarAnalisesCadastro(codSubrocesso.value);
  }
});

function validarAtividades(): Atividade[] {
  return atividades.value.filter(
    (atividade) => atividade.conhecimentos.length === 0,
  );
}

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

function disponibilizarCadastro() {
  const sub = subprocesso.value;
  const situacaoEsperada = isRevisao.value
    ? SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO
    : SituacaoSubprocesso.CADASTRO_EM_ANDAMENTO;

  if (!sub || sub.situacaoSubprocesso !== situacaoEsperada) {
    notificacoesStore.erro(
      "Ação não permitida",
      `Ação permitida apenas na situação: "${situacaoEsperada}".`,
    );
    return;
  }

  atividadesSemConhecimento.value = validarAtividades();
  if (atividadesSemConhecimento.value.length > 0) {
    const atividadesDescricoes = atividadesSemConhecimento.value
      .map((a) => `- ${a.descricao}`)
      .join("\n");
    notificacoesStore.aviso(
      "Atividades Incompletas",
      `As seguintes atividades não têm conhecimentos associados:\n${atividadesDescricoes}`,
    );
    return;
  }

  mostrarModalConfirmacao.value = true;
}

function fecharModalConfirmacao() {
  mostrarModalConfirmacao.value = false;
  atividadesSemConhecimento.value = [];
}

async function confirmarDisponibilizacao() {
  if (!codSubrocesso.value) return;

  if (isRevisao.value) {
    await subprocessosStore.disponibilizarRevisaoCadastro(codSubrocesso.value);
  } else {
    await subprocessosStore.disponibilizarCadastro(codSubrocesso.value);
  }

  fecharModalConfirmacao();
  await router.push("/painel");
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

.atividade-titulo-card .atividade-descricao {
  font-size: 1.1rem;
}

</style>
