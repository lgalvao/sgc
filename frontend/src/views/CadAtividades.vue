<template>
  <BContainer class="mt-4">
    <div class="d-flex align-items-center mb-3">
      <BButton
          class="p-0 me-3 text-decoration-none"
          data-testid="btn-cad-atividades-voltar"
          variant="link"
          @click="router.back()"
      >
        <i class="bi bi-arrow-left fs-4"/>
      </BButton>
      <div class="fs-5 d-flex align-items-center gap-2">
        <span>{{ siglaUnidade }} - {{ nomeUnidade }}</span>
        <span
            v-if="subprocesso"
            :class="badgeClass(subprocesso.situacaoSubprocesso)"
            class="badge fs-6"
            data-testid="cad-atividades__txt-badge-situacao"
        >{{ situacaoLabel(subprocesso.situacaoSubprocesso) }}</span>
      </div>
    </div>

    <div class="d-flex justify-content-between align-items-center mb-3">
      <h1 class="mb-0 display-6">
        Atividades e conhecimentos
      </h1>

      <div class="d-flex gap-2">
        <BButton
            v-if="podeVerImpacto"
            data-testid="cad-atividades__btn-impactos-mapa"
            variant="outline-secondary"
            @click="abrirModalImpacto"
        >
          <i class="bi bi-arrow-right-circle me-2"/>Impacto no mapa
        </BButton>
        <BButton
            v-if="isChefe && historicoAnalises.length > 0"
            data-testid="btn-cad-atividades-historico"
            variant="outline-info"
            @click="abrirModalHistorico"
        >
          Histórico de análise
        </BButton>
        <BButton
            v-if="isChefe"
            data-testid="btn-cad-atividades-importar"
            title="Importar"
            variant="outline-primary"
            @click="mostrarModalImportar = true"
        >
          Importar atividades
        </BButton>
        <BButton
            v-if="!!permissoes?.podeDisponibilizarCadastro"
            data-testid="btn-cad-atividades-disponibilizar"
            title="Disponibilizar"
            variant="outline-success"
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
            aria-label="Nova atividade"
            data-testid="inp-nova-atividade"
            placeholder="Nova atividade"
            type="text"
        />
      </BCol>
      <BCol cols="auto">
        <BButton
            :disabled="!codSubrocesso || !permissoes?.podeEditarMapa"
            data-testid="btn-adicionar-atividade"
            size="sm"
            title="Adicionar atividade"
            type="submit"
            variant="outline-primary"
        >
          <i
              class="bi bi-save"
          />
        </BButton>
      </BCol>
    </BForm>

    <div v-for="(atividade, idx) in atividades" :key="atividade.codigo || idx">
      <AtividadeItem
          :atividade="atividade"
          :pode-editar="!!permissoes?.podeEditarMapa"
          @atualizar-atividade="(desc) => salvarEdicaoAtividade(atividade.codigo, desc)"
          @remover-atividade="() => removerAtividade(idx)"
          @adicionar-conhecimento="(desc) => adicionarConhecimento(idx, desc)"
          @atualizar-conhecimento="(idC, desc) => salvarEdicaoConhecimento(atividade.codigo, idC, desc)"
          @remover-conhecimento="(idC) => removerConhecimento(idx, idC)"
      />
    </div>

    <ImportarAtividadesModal
        :cod-subrocesso-destino="codSubrocesso"
        :mostrar="mostrarModalImportar"
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
            data-testid="btn-confirmar-disponibilizacao"
            variant="success"
            @click="confirmarDisponibilizacao"
        >
          Confirmar
        </BButton>
      </template>
    </BModal>

    <BModal
        v-model="mostrarModalHistorico"
        :fade="false"
        centered
        hide-footer
        size="lg"
        title="Histórico de Análise"
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
            <td :data-testid="`cell-unidade-${index}`">{{
                'unidade' in analise ? analise.unidade : analise.unidadeSigla
              }}
            </td>
            <td :data-testid="`cell-resultado-${index}`">{{
                formatarAcaoAnalise(analise.acao || analise.resultado)
              }}
            </td>
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

    <BModal
        v-model="mostrarModalErros"
        centered
        header-bg-variant="warning"
        header-text-variant="dark"
        hide-footer
        title="Pendências para disponibilização"
    >
      <div v-if="errosValidacao.length > 0">
        <p class="mb-3">Corrija as seguintes pendências antes de disponibilizar:</p>
        <ul class="list-group">
          <li v-for="(erro, index) in errosValidacao" :key="index" class="list-group-item list-group-item-warning">
            <div class="fw-bold">
              {{ erro.tipo === 'ATIVIDADE_SEM_CONHECIMENTO' ? 'Atividade sem conhecimento' : erro.tipo }}
            </div>
            <div v-if="erro.descricaoAtividade" class="small text-muted">{{ erro.descricaoAtividade }}</div>
            <div>{{ erro.mensagem }}</div>
          </li>
        </ul>
      </div>
      <template #footer>
        <BButton data-testid="btn-fechar-modal-pendencias" variant="secondary" @click="mostrarModalErros = false">
          Fechar
        </BButton>
      </template>
    </BModal>
  </BContainer>
</template>

<script lang="ts" setup>
import {BButton, BCol, BContainer, BForm, BFormInput, BModal} from "bootstrap-vue-next";
import {computed, onMounted, ref} from "vue";
import {useRouter} from "vue-router";
import {badgeClass, situacaoLabel} from "@/utils";
import ImpactoMapaModal from "@/components/ImpactoMapaModal.vue";
import ImportarAtividadesModal from "@/components/ImportarAtividadesModal.vue";
import {usePerfil} from "@/composables/usePerfil";
import {useAnalisesStore} from "@/stores/analises";
import {useAtividadesStore} from "@/stores/atividades";
import {useMapasStore} from "@/stores/mapas";
import AtividadeItem from "@/components/AtividadeItem.vue";
import {useFeedbackStore} from "@/stores/feedback";

import {useProcessosStore} from "@/stores/processos";
import {useSubprocessosStore} from "@/stores/subprocessos";
import {useUnidadesStore} from "@/stores/unidades";
import {
  type Atividade,
  type Conhecimento,
  type CriarAtividadeRequest,
  type CriarConhecimentoRequest,
  type ErroValidacao,
  Perfil,
  SituacaoSubprocesso,
  type SubprocessoPermissoes,
  TipoProcesso,
} from "@/types/tipos";
import * as subprocessoService from "@/services/subprocessoService";


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

// Função helper para buscar unidade recursivamente na árvore
function buscarUnidadeNaArvore(unidades: any[], sigla: string): any {
    for (const unidade of unidades) {
        if (unidade.sigla === sigla) {
            return unidade;
        }
        if (unidade.filhos && unidade.filhos.length > 0) {
            const encontrada = buscarUnidadeNaArvore(unidade.filhos, sigla);
            if (encontrada) return encontrada;
        }
    }
    return null;
}

const codSubrocesso = computed(() => {
    if (!processosStore.processoDetalhe?.unidades) return undefined;
    const unidadeEncontrada = buscarUnidadeNaArvore(
        processosStore.processoDetalhe.unidades,
        unidadeId.value
    );
    return unidadeEncontrada?.codSubprocesso;
});

const codMapa = computed(() => {
    if (!processosStore.processoDetalhe?.unidades) return undefined;
    const unidadeEncontrada = buscarUnidadeNaArvore(
        processosStore.processoDetalhe.unidades,
        unidadeId.value
    );
    return unidadeEncontrada?.mapaCodigo;
});

const atividades = computed({
  get: () => {
    if (codSubrocesso.value === undefined) return [];
    return atividadesStore.obterAtividadesPorSubprocesso(codSubrocesso.value);
  },
  set: () => {
  },
});

const processoAtual = computed(() => processosStore.processoDetalhe);
const isRevisao = computed(
    () => processoAtual.value?.tipo === TipoProcesso.REVISAO,
);

const permissoes = ref<SubprocessoPermissoes | null>(null);

async function adicionarAtividade() {
  if (novaAtividade.value?.trim() && codMapa.value && codSubrocesso.value) {
    const request: CriarAtividadeRequest = {
      descricao: novaAtividade.value.trim(),
    };
    const status = await atividadesStore.adicionarAtividade(
        codSubrocesso.value,
        codMapa.value,
        request,
    );
    novaAtividade.value = "";
    if (status) {
      processosStore.atualizarStatusSubprocesso(codSubrocesso.value, status);
    }
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
    const status = await atividadesStore.removerAtividade(
        codSubrocesso.value,
        atividadeRemovida.codigo,
    );
    if (status) {
      processosStore.atualizarStatusSubprocesso(codSubrocesso.value, status);
    }
  }
}

async function adicionarConhecimento(idx: number, descricao: string) {
  if (!codSubrocesso.value) return;
  const atividade = atividades.value[idx];
  if (descricao.trim()) {
    const request: CriarConhecimentoRequest = {
      descricao: descricao.trim(),
    };
    const status = await atividadesStore.adicionarConhecimento(
        codSubrocesso.value,
        atividade.codigo,
        request,
    );
    if (status) {
      processosStore.atualizarStatusSubprocesso(codSubrocesso.value, status);
    }
  }
}

async function removerConhecimento(idx: number, idConhecimento: number) {
  if (!codSubrocesso.value) return;
  const atividade = atividades.value[idx];
  if (confirm("Confirma a remoção deste conhecimento?")) {
    const status = await atividadesStore.removerConhecimento(
        codSubrocesso.value,
        atividade.codigo,
        idConhecimento,
    );
    if (status) {
      processosStore.atualizarStatusSubprocesso(codSubrocesso.value, status);
    }
  }
}

async function salvarEdicaoConhecimento(atividadeId: number, conhecimentoId: number, descricao: string) {
  if (!codSubrocesso.value) return;

  if (descricao.trim()) {
    const conhecimentoAtualizado: Conhecimento = {
      id: conhecimentoId,
      descricao: descricao.trim(),
    };
    const status = await atividadesStore.atualizarConhecimento(
        codSubrocesso.value,
        atividadeId,
        conhecimentoId,
        conhecimentoAtualizado,
    );
    if (status) {
      processosStore.atualizarStatusSubprocesso(codSubrocesso.value, status);
    }
  }
}

async function salvarEdicaoAtividade(id: number, descricao: string) {
  if (descricao.trim() && codSubrocesso.value) {
    const atividadeOriginal = atividades.value.find((a) => a.codigo === id);
    if (atividadeOriginal) {
      const atividadeAtualizada: Atividade = {
        ...atividadeOriginal,
        descricao: descricao.trim(),
      };
      const status = await atividadesStore.atualizarAtividade(
          codSubrocesso.value,
          id,
          atividadeAtualizada,
      );
      if (status) {
        processosStore.atualizarStatusSubprocesso(codSubrocesso.value, status);
      }
    }
  }
}

async function handleImportAtividades() {
  mostrarModalImportar.value = false;
  if (codSubrocesso.value) {
    await atividadesStore.buscarAtividadesParaSubprocesso(codSubrocesso.value);
    const status = await subprocessoService.obterStatus(codSubrocesso.value);
    processosStore.atualizarStatusSubprocesso(codSubrocesso.value, status);
  }
  feedbackStore.show(
      "Importação Concluída",
      "As atividades foram importadas para o seu mapa.",
      "success"
  );
}

const {perfilSelecionado} = usePerfil();

const isChefe = computed(() => perfilSelecionado.value === Perfil.CHEFE);

const subprocesso = computed(() => {
  if (!processosStore.processoDetalhe) return null;
  return processosStore.processoDetalhe.unidades.find(
      (u) => u.sigla === unidadeId.value,
  );
});

const podeVerImpacto = computed(() => !!permissoes.value?.podeVisualizarImpacto);

const mostrarModalImpacto = ref(false);
const mostrarModalImportar = ref(false);
const mostrarModalConfirmacao = ref(false);

const mostrarModalHistorico = ref(false);
const mostrarModalErros = ref(false);
const errosValidacao = ref<ErroValidacao[]>([]);


onMounted(async () => {
  await unidadesStore.buscarUnidade(props.sigla);
  await processosStore.buscarProcessoDetalhe(codProcesso.value);
  if (codSubrocesso.value) {
    await atividadesStore.buscarAtividadesParaSubprocesso(codSubrocesso.value);
    await analisesStore.buscarAnalisesCadastro(codSubrocesso.value);
    permissoes.value = await subprocessoService.obterPermissoes(codSubrocesso.value);
  } else {
    console.error('[CadAtividades] ERRO: codSubprocesso está undefined!');
    console.error('[CadAtividades] Não foi possível carregar atividades e permissões');
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

async function disponibilizarCadastro() {
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

  // Backend valida atividades sem conhecimento via SubprocessoCadastroController/DTO
  if (codSubrocesso.value) {
    try {
      const resultado = await subprocessoService.validarCadastro(codSubrocesso.value);
      if (resultado.valido) {
        mostrarModalConfirmacao.value = true;
      } else {
        errosValidacao.value = resultado.erros;
        mostrarModalErros.value = true;
      }
    } catch {
      feedbackStore.show("Erro na validação", "Não foi possível validar o cadastro.", "danger");
    }
  }
}

function fecharModalConfirmacao() {
  mostrarModalConfirmacao.value = false;
}

async function confirmarDisponibilizacao() {
  if (!codSubrocesso.value) return;

  let sucesso: boolean;
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
