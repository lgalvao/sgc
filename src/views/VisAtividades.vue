<template>
  <div class="container mt-4">
    <div class="unidade-cabecalho w-100">
      <span class="unidade-sigla">{{ siglaUnidade }}</span>
      <span class="unidade-nome">{{ nomeUnidade }}</span>
    </div>

    <div class="d-flex justify-content-between align-items-center mb-3">
      <h2 class="mb-0">
        Atividades e conhecimentos
      </h2>
      <div class="d-flex gap-2">
        <button
            class="btn btn-outline-secondary"
            @click="abrirModalImpacto"
        >
          <i class="bi bi-arrow-right-circle me-2"/>{{ isRevisao ? 'Ver impactos' : 'Impacto no mapa' }}
        </button>
        <button
            class="btn btn-secondary"
            title="Devolver para ajustes"
            @click="devolverCadastro"
        >
          Devolver para ajustes
        </button>
        <button
            class="btn btn-success"
            title="Validar"
            @click="validarCadastro"
        >
          {{ isRevisao ? 'Registrar aceite' : 'Validar' }}
        </button>
      </div>
    </div>

    <!-- Lista de atividades -->
    <div
        v-for="(atividade, idx) in atividades"
        :key="atividade.id || idx"
        class="card mb-3 atividade-card"
    >
      <div class="card-body py-2">
        <div
            class="card-title d-flex align-items-center atividade-edicao-row position-relative group-atividade atividade-hover-row atividade-titulo-card"
        >
          <strong
              class="atividade-descricao"
              data-testid="atividade-descricao"
          >{{ atividade.descricao }}</strong>
        </div>

        <!-- Conhecimentos da atividade -->
        <div class="mt-3 ms-3">
          <div
              v-for="(conhecimento) in atividade.conhecimentos"
              :key="conhecimento.id"
              class="d-flex align-items-center mb-2 group-conhecimento position-relative conhecimento-hover-row"
          >
            <span data-testid="conhecimento-descricao">{{ conhecimento.descricao }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Modal de Impacto no Mapa -->
    <ImpactoMapaModal
        :id-processo="idProcesso"
        :mostrar="mostrarModalImpacto"
        :sigla-unidade="siglaUnidade"
        @fechar="fecharModalImpacto"
    />

    <!-- Modal de Validação -->
    <div
        v-if="mostrarModalValidar"
        class="modal fade show"
        style="display: block;"
        tabindex="-1"
    >
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">
              {{ isRevisao ? 'Aceite da revisão do cadastro' : 'Validação do cadastro' }}
            </h5>
            <button
                type="button"
                class="btn-close"
                @click="fecharModalValidar"
            />
          </div>
          <div class="modal-body">
            <p>{{ isRevisao ? 'Confirma o aceite da revisão do cadastro de atividades?' : 'Confirma o aceite do cadastro de atividades?' }}</p>
            <div class="mb-3">
              <label
                  class="form-label"
                  for="observacaoValidacao"
              >Observação (opcional)</label>
              <textarea
                  id="observacaoValidacao"
                  v-model="observacaoValidacao"
                  class="form-control"
                  rows="3"
              />
            </div>
          </div>
          <div class="modal-footer">
            <button
                type="button"
                class="btn btn-secondary"
                @click="fecharModalValidar"
            >
              Cancelar
            </button>
            <button
                type="button"
                class="btn btn-success"
                @click="confirmarValidacao"
            >
              Confirmar
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Modal de Devolução -->
    <div
        v-if="mostrarModalDevolver"
        class="modal fade show"
        style="display: block;"
        tabindex="-1"
    >
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">
              {{ isRevisao ? 'Devolução da revisão do cadastro' : 'Devolução do cadastro' }}
            </h5>
            <button
                type="button"
                class="btn-close"
                @click="fecharModalDevolver"
            />
          </div>
          <div class="modal-body">
            <p>{{ isRevisao ? 'Confirma a devolução da revisão do cadastro para ajustes?' : 'Confirma a devolução do cadastro para ajustes?' }}</p>
            <div class="mb-3">
              <label
                  class="form-label"
                  for="observacaoDevolucao"
              >Observação (opcional)</label>
              <textarea
                  id="observacaoDevolucao"
                  v-model="observacaoDevolucao"
                  class="form-control"
                  rows="3"
              />
            </div>
          </div>
          <div class="modal-footer">
            <button
                type="button"
                class="btn btn-secondary"
                @click="fecharModalDevolver"
            >
              Cancelar
            </button>
            <button
                type="button"
                class="btn btn-danger"
                @click="confirmarDevolucao"
            >
              Confirmar
            </button>
          </div>
        </div>
      </div>
    </div>

    <div
        v-if="mostrarModalValidar || mostrarModalDevolver"
        class="modal-backdrop fade show"
    />
  </div>
</template>

<script lang="ts" setup>
import {computed, ref} from 'vue'
import {useAtividadesStore} from '@/stores/atividades'
import {useUnidadesStore} from '@/stores/unidades'
import {useProcessosStore} from '@/stores/processos'
import {useRevisaoStore} from '@/stores/revisao'
import {useNotificacoesStore} from '@/stores/notificacoes'
import {useAlertasStore} from '@/stores/alertas'
import {useRouter} from 'vue-router'
import {Atividade, Processo, Subprocesso, Unidade} from '@/types/tipos'
import ImpactoMapaModal from '@/components/ImpactoMapaModal.vue'

const props = defineProps<{
  idProcesso: number | string,
  sigla: string
}>()

const unidadeId = computed(() => props.sigla)
const idProcesso = computed(() => Number(props.idProcesso))

const atividadesStore = useAtividadesStore()
const unidadesStore = useUnidadesStore()
const processosStore = useProcessosStore()
const revisaoStore = useRevisaoStore()
const notificacoesStore = useNotificacoesStore()
const alertasStore = useAlertasStore()
const router = useRouter()

const mostrarModalImpacto = ref(false)
const mostrarModalValidar = ref(false)
const mostrarModalDevolver = ref(false)
const observacaoValidacao = ref('')
const observacaoDevolucao = ref('')

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

const processoAtual = computed<Processo | null>(() => {
  if (!idSubprocesso.value) return null;
  return (processosStore.processos as Processo[]).find(p => p.id === idProcesso.value) || null;
});

const isRevisao = computed(() => processoAtual.value?.tipo === 'Revisão');

function validarCadastro() {
  mostrarModalValidar.value = true;
}

function devolverCadastro() {
  mostrarModalDevolver.value = true;
}

function confirmarValidacao() {
  if (!idSubprocesso.value) return;

  const unidadeSuperior = unidadesStore.getUnidadeImediataSuperior(siglaUnidade.value);
  const isRevisao = processoAtual.value?.tipo === 'Revisão';

  // Alterar situação do subprocesso
  const subprocessoIndex = processosStore.subprocessos.findIndex(pu => pu.id === idSubprocesso.value);
  if (subprocessoIndex !== -1) {
    let novaSituacao: string;
    if (isRevisao) {
      novaSituacao = unidadeSuperior === 'SEDOC' ? 'Revisão do cadastro homologada' : 'Revisão do cadastro aceita';
    } else {
      novaSituacao = unidadeSuperior === 'SEDOC' ? 'Cadastro homologado' : 'Cadastro aceito';
    }
    processosStore.subprocessos[subprocessoIndex].situacao = novaSituacao;
    processosStore.subprocessos[subprocessoIndex].unidadeAtual = unidadeSuperior || 'SEDOC';
  }

  // Registrar movimentação
  processosStore.addMovement({
    idSubprocesso: idSubprocesso.value,
    unidadeOrigem: siglaUnidade.value,
    unidadeDestino: unidadeSuperior || 'SEDOC',
    descricao: isRevisao ? 'Revisão do cadastro de atividades e conhecimentos aceita' : 'Cadastro de atividades e conhecimentos aceito'
  });


  // Enviar notificação por e-mail
  const assunto = isRevisao
    ? `SGC: Revisão do cadastro de atividades e conhecimentos aceita: ${siglaUnidade.value}`
    : `SGC: Cadastro de atividades e conhecimentos aceito: ${siglaUnidade.value}`;

  const corpo = isRevisao
    ? `A revisão do cadastro de atividades e conhecimentos da unidade ${siglaUnidade.value} no processo ${processoAtual.value?.descricao || 'N/A'} foi aceita.`
    : `O cadastro de atividades e conhecimentos da unidade ${siglaUnidade.value} no processo ${processoAtual.value?.descricao || 'N/A'} foi aceito.`;

  notificacoesStore.email(assunto, `Responsável pela ${unidadeSuperior}`, corpo);

  // Criar alerta
  alertasStore.criarAlerta({
    idProcesso: idProcesso.value,
    unidadeOrigem: siglaUnidade.value,
    unidadeDestino: unidadeSuperior || 'SEDOC',
    descricao: isRevisao
      ? `Revisão do cadastro de atividades/conhecimentos da unidade ${siglaUnidade.value} aceita`
      : `Cadastro de atividades/conhecimentos da unidade ${siglaUnidade.value} aceito`,
    dataHora: new Date()
  });

  const mensagemSucesso = isRevisao ? 'Revisão do cadastro aceita' : 'Cadastro validado';

  notificacoesStore.sucesso(
      mensagemSucesso,
      'A análise foi registrada com sucesso!'
  );

  fecharModalValidar();
  router.push('/painel');
}

function confirmarDevolucao() {
  if (!idSubprocesso.value) return;

  const isRevisao = processoAtual.value?.tipo === 'Revisão';

  // Alterar situação do subprocesso
  const subprocessoIndex = processosStore.subprocessos.findIndex(pu => pu.id === idSubprocesso.value);
  if (subprocessoIndex !== -1) {
    processosStore.subprocessos[subprocessoIndex].situacao = isRevisao ? 'Revisão do cadastro em andamento' : 'Cadastro em andamento';
    processosStore.subprocessos[subprocessoIndex].unidadeAtual = siglaUnidade.value;
  }

  // Registrar movimentação
  processosStore.addMovement({
    idSubprocesso: idSubprocesso.value,
    unidadeOrigem: 'SEDOC', // Unidade que está fazendo a análise
    unidadeDestino: siglaUnidade.value,
    descricao: isRevisao ? 'Devolução da revisão do cadastro de atividades e conhecimentos para ajustes' : 'Devolução do cadastro de atividades e conhecimentos para ajustes'
  });

  // Enviar notificação por e-mail
  const assunto = isRevisao
    ? `SGC: Revisão do cadastro de atividades e conhecimentos devolvida: ${siglaUnidade.value}`
    : `SGC: Cadastro de atividades e conhecimentos devolvido: ${siglaUnidade.value}`;

  const corpo = isRevisao
    ? `A revisão do cadastro de atividades e conhecimentos da unidade ${siglaUnidade.value} no processo ${processoAtual.value?.descricao || 'N/A'} foi devolvida para ajustes.`
    : `O cadastro de atividades e conhecimentos da unidade ${siglaUnidade.value} no processo ${processoAtual.value?.descricao || 'N/A'} foi devolvido para ajustes.`;

  notificacoesStore.email(assunto, `Responsável pela ${siglaUnidade.value}`, corpo);

  // Criar alerta
  alertasStore.criarAlerta({
    idProcesso: idProcesso.value,
    unidadeOrigem: 'SEDOC',
    unidadeDestino: siglaUnidade.value,
    descricao: isRevisao
      ? `Revisão do cadastro de atividades/conhecimentos da unidade ${siglaUnidade.value} devolvida para ajustes`
      : `Cadastro de atividades/conhecimentos da unidade ${siglaUnidade.value} devolvido para ajustes`,
    dataHora: new Date()
  });

  const mensagemSucesso = isRevisao ? 'Revisão do cadastro devolvida' : 'Cadastro devolvido';

  notificacoesStore.sucesso(
      mensagemSucesso,
      'O cadastro foi devolvido para ajustes!'
  );

  fecharModalDevolver();
  router.push('/painel');
}

function fecharModalValidar() {
  mostrarModalValidar.value = false;
  observacaoValidacao.value = '';
}

function fecharModalDevolver() {
  mostrarModalDevolver.value = false;
  observacaoDevolucao.value = '';
}

function abrirModalImpacto() {
  if (isRevisao.value) {
    revisaoStore.setMudancasParaImpacto(revisaoStore.mudancasRegistradas);
  } else {
    // Para mapeamento inicial, não há mudanças para mostrar
    revisaoStore.setMudancasParaImpacto([]);
  }
  mostrarModalImpacto.value = true;
}

function fecharModalImpacto() {
  mostrarModalImpacto.value = false;
  revisaoStore.setMudancasParaImpacto([]);
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