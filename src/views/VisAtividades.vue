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
          v-if="podeVerImpacto"
          class="btn btn-outline-secondary"
          @click="abrirModalImpacto"
        >
          <i class="bi bi-arrow-right-circle me-2" />{{ isRevisao ? 'Ver impactos' : 'Impacto no mapa' }}
        </button>
        <button
          class="btn btn-outline-info"
          @click="abrirModalHistoricoAnalise"
        >
          Histórico de análise
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
          {{ perfilSelecionado === Perfil.ADMIN ? 'Homologar' : 'Registrar aceite' }}
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

    <!-- Modal de Histórico de Análise -->
    <HistoricoAnaliseModal
      :id-subprocesso="idSubprocesso"
      :mostrar="mostrarModalHistoricoAnalise"
      @fechar="fecharModalHistoricoAnalise"
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
              {{ isHomologacao ? 'Homologação do cadastro de atividades e conhecimentos' : (isRevisao ? 'Aceite da revisão do cadastro' : 'Validação do cadastro') }}
            </h5>
            <button
              type="button"
              class="btn-close"
              @click="fecharModalValidar"
            />
          </div>
          <div class="modal-body">
            <p>{{ isHomologacao ? 'Confirma a homologação do cadastro de atividades e conhecimentos?' : (isRevisao ? 'Confirma o aceite da revisão do cadastro de atividades?' : 'Confirma o aceite do cadastro de atividades?') }}</p>
            <div
              v-if="!isHomologacao"
              class="mb-3"
            >
              <label
                class="form-label"
                for="observacaoValidacao"
              >Observação</label>
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

    <!-- Modal de Homologação Sem Impacto -->
    <div
      v-if="mostrarModalHomologacaoSemImpacto"
      class="modal fade show"
      style="display: block;"
      tabindex="-1"
    >
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">
              Homologação do mapa de competências
            </h5>
            <button
              type="button"
              class="btn-close"
              @click="fecharModalHomologacaoSemImpacto"
            />
          </div>
          <div class="modal-body">
            <p>A revisão do cadastro não produziu nenhum impacto no mapa de competência da unidade. Confirma a manutenção do mapa de competências vigente?</p>
          </div>
          <div class="modal-footer">
            <button
              type="button"
              class="btn btn-secondary"
              @click="fecharModalHomologacaoSemImpacto"
            >
              Cancelar
            </button>
            <button
              type="button"
              class="btn btn-success"
              @click="confirmarHomologacaoSemImpacto"
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
              >Observação</label>
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
      v-if="mostrarModalValidar || mostrarModalDevolver || mostrarModalHomologacaoSemImpacto"
      class="modal-backdrop fade show"
    />
  </div>
</template>

<script lang="ts" setup>
import {computed, ref} from 'vue'
import {usePerfilStore} from '@/stores/perfil';
import {useAtividadesStore} from '@/stores/atividades'
import {useUnidadesStore} from '@/stores/unidades'
import {useProcessosStore} from '@/stores/processos'
import {useRevisaoStore} from '@/stores/revisao'
import {useNotificacoesStore} from '@/stores/notificacoes'
import {useAlertasStore} from '@/stores/alertas'
import {useAnalisesStore} from '@/stores/analises' // Adicionado
import {useRouter} from 'vue-router'
import {Atividade, Perfil, Processo, ResultadoAnalise, Subprocesso, Unidade} from '@/types/tipos'
import ImpactoMapaModal from '@/components/ImpactoMapaModal.vue'
import HistoricoAnaliseModal from '@/components/HistoricoAnaliseModal.vue'
import {URL_SISTEMA} from '@/constants';

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
const analisesStore = useAnalisesStore() // Adicionado
const perfilStore = usePerfilStore()
const router = useRouter()

const mostrarModalImpacto = ref(false)
const mostrarModalValidar = ref(false)
const mostrarModalDevolver = ref(false)
const mostrarModalHistoricoAnalise = ref(false)
const mostrarModalHomologacaoSemImpacto = ref(false)
const observacaoValidacao = ref<string>('')
const observacaoDevolucao = ref<string>('')

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

const perfilSelecionado = computed(() => perfilStore.perfilSelecionado);
const unidadeSuperior = computed<string>(() => unidadesStore.getUnidadeImediataSuperior(siglaUnidade.value) || '');

const isHomologacao = computed(() => perfilStore.perfilSelecionado === Perfil.ADMIN && unidadeSuperior.value === 'SEDOC');

const subprocesso = computed(() => {
  if (!idSubprocesso.value) return null;
  return (processosStore.subprocessos as Subprocesso[]).find(p => p.id === idSubprocesso.value);
});

const podeVerImpacto = computed(() => {
  if (!subprocesso.value || !perfilStore.perfilSelecionado) return false;

  const perfil = perfilStore.perfilSelecionado;
  const podeVer = perfil === Perfil.GESTOR || perfil === Perfil.ADMIN;
  const situacaoCorreta = subprocesso.value.situacao === 'Revisão do cadastro disponibilizada';
  const localizacaoCorreta = subprocesso.value.unidadeAtual === perfilStore.unidadeSelecionada;

  return podeVer && situacaoCorreta && localizacaoCorreta;
});

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
  const perfilUsuario = perfilStore.perfilSelecionado;
  const unidadeSuperior = unidadesStore.getUnidadeImediataSuperior(siglaUnidade.value);
  const isRevisao = processoAtual.value?.tipo === 'Revisão';
  const temImpactos = revisaoStore.mudancasRegistradas.length > 0;
  
  // Se é ADMIN homologando revisão sem impactos, mostrar modal específico
  if (perfilUsuario === Perfil.ADMIN && unidadeSuperior === 'SEDOC' && isRevisao && !temImpactos) {
    mostrarModalHomologacaoSemImpacto.value = true;
  } else {
    mostrarModalValidar.value = true;
  }
}

function devolverCadastro() {
  mostrarModalDevolver.value = true;
}

function confirmarValidacao() {
  if (!idSubprocesso.value) return;

  const unidadeAnalise = perfilStore.unidadeSelecionada;
  const unidadeSubprocesso = siglaUnidade.value;
  const unidadeSuperior = unidadesStore.getUnidadeImediataSuperior(unidadeSubprocesso);
  const isRevisao = processoAtual.value?.tipo === 'Revisão';
  const perfilUsuario = perfilStore.perfilSelecionado;

  if (perfilUsuario === Perfil.ADMIN && unidadeSuperior === 'SEDOC') {
    // Verificar se há impactos no mapa
    const temImpactos = revisaoStore.mudancasRegistradas.length > 0;
    
    if (!temImpactos && isRevisao) {
      // 12.2 - Homologação sem impactos
      processosStore.addMovement({
        idSubprocesso: idSubprocesso.value,
        unidadeOrigem: 'SEDOC',
        unidadeDestino: 'SEDOC',
        descricao: 'Mapa de competências mantido (sem impactos)'
      });
      
      const subprocessoIndex = processosStore.subprocessos.findIndex(pu => pu.id === idSubprocesso.value);
      if (subprocessoIndex !== -1) {
        processosStore.subprocessos[subprocessoIndex].situacao = 'Mapa homologado';
      }
    } else {
      // 12.3 - Homologação com impactos
      processosStore.addMovement({
        idSubprocesso: idSubprocesso.value,
        unidadeOrigem: 'SEDOC',
        unidadeDestino: 'SEDOC',
        descricao: isRevisao ? 'Cadastro de atividades e conhecimentos homologado' : 'Cadastro de atividades e conhecimentos homologado'
      });
      
      const subprocessoIndex = processosStore.subprocessos.findIndex(pu => pu.id === idSubprocesso.value);
      if (subprocessoIndex !== -1) {
        processosStore.subprocessos[subprocessoIndex].situacao = isRevisao ? 'Revisão do cadastro homologada' : 'Cadastro homologado';
      }
    }

    notificacoesStore.sucesso('Homologação efetivada', 'O cadastro foi homologado com sucesso!');
    fecharModalValidar();
    router.push(`/processo/${idProcesso.value}/${siglaUnidade.value}`);
  } else {
    // 10. Aceitar (perfil GESTOR)
    // 10.5. Registrar análise de cadastro
    analisesStore.registrarAnalise({
      idSubprocesso: idSubprocesso.value,
      dataHora: new Date(),
      unidade: unidadeAnalise,
      resultado: ResultadoAnalise.ACEITE,
      observacao: observacaoValidacao.value!
    });

    // 10.6. Registrar movimentação
    const unidadeDestinoStr: string = (unidadesStore.getUnidadeImediataSuperior(unidadeSubprocesso) || 'SEDOC')!;
    processosStore.addMovement({
      idSubprocesso: idSubprocesso.value,
      unidadeOrigem: unidadeAnalise,
      unidadeDestino: unidadeDestinoStr,
      descricao: isRevisao ? 'Revisão do cadastro de atividades e conhecimentos aceita' : 'Cadastro de atividades e conhecimentos aceito' // Adicionado
    });

    // Alterar situação do subprocesso
    const subprocessoIndex = processosStore.subprocessos.findIndex(pu => pu.id === idSubprocesso.value);
    if (subprocessoIndex !== -1) {
      processosStore.subprocessos[subprocessoIndex].situacao = isRevisao ? 'Revisão do cadastro aceita' : 'Cadastro aceito';
      processosStore.subprocessos[subprocessoIndex].unidadeAtual = unidadeSuperior || 'SEDOC';
    }

    // 10.7. Enviar notificação por e-mail
    const assuntoEmail = `SGC: Cadastro de atividades e conhecimentos da ${unidadeSubprocesso} submetido para análise`;
    const corpoEmailParte1 = `Prezado(a) responsável pela ${unidadeSuperior},`;
    const descricaoProcesso: string = processoAtual.value ? processoAtual.value.descricao : 'N/A';
    const corpoEmailParte2 = 'O cadastro de atividades e conhecimentos da ' + unidadeSubprocesso + ' no processo ' + descricaoProcesso + ' foi submetido para análise por essa unidade.';
    const corpoEmailParte3 = `A análise já pode ser realizada no O sistema de Gestão de Competências: ${URL_SISTEMA}.`;
    const corpoEmail = `${corpoEmailParte1}\n\n${corpoEmailParte2}\n${corpoEmailParte3}`;

    const unidadeResponsavel: string = unidadesStore.getUnidadeImediataSuperior(siglaUnidade.value) || '';
    notificacoesStore.email(assuntoEmail, 'Responsável pela ' + unidadeResponsavel, corpoEmail);

    // 10.8. Criar alerta
    const unidadeDestinoAlertaStr: string = unidadesStore.getUnidadeImediataSuperior(unidadeSubprocesso) || 'SEDOC';
    alertasStore.criarAlerta({
      idProcesso: idProcesso.value,
      unidadeOrigem: unidadeAnalise,
      unidadeDestino: unidadeDestinoAlertaStr,
      descricao: `Cadastro de atividades e conhecimentos da unidade ${unidadeSubprocesso} submetido para análise`,
      dataHora: new Date()
    });

    notificacoesStore.sucesso('Aceite registrado', 'A análise foi registrada com sucesso!');
    fecharModalValidar();
    router.push('/painel'); // 10.9. Redirecionar para o Painel
  }
}

function confirmarDevolucao() {
  if (!idSubprocesso.value) return;

  const isRevisao = processoAtual.value?.tipo === 'Revisão';
  const unidadeAnalise = perfilStore.unidadeSelecionada; // Unidade do usuário logado
  const unidadeSubprocesso = siglaUnidade.value; // Unidade do subprocesso que está sendo analisado

  // 9.5. Registrar análise de cadastro
  analisesStore.registrarAnalise({
    idSubprocesso: idSubprocesso.value,
    dataHora: new Date(),
    unidade: unidadeAnalise,
    resultado: ResultadoAnalise.DEVOLUCAO,
    observacao: observacaoDevolucao.value!
  });

  // 9.7. Registrar movimentação
  processosStore.addMovement({
    idSubprocesso: idSubprocesso.value,
    unidadeOrigem: unidadeAnalise,
    unidadeDestino: unidadeSubprocesso,
    descricao: isRevisao ? 'Devolução da revisão do cadastro de atividades e conhecimentos para ajustes' : 'Devolução do cadastro de atividades e conhecimentos para ajustes'
  });

  // Alterar situação do subprocesso
  const subprocessoIndex = processosStore.subprocessos.findIndex(pu => pu.id === idSubprocesso.value);
  if (subprocessoIndex !== -1) {
    processosStore.subprocessos[subprocessoIndex].situacao = isRevisao ? 'Revisão do cadastro em andamento' : 'Cadastro em andamento';
    processosStore.subprocessos[subprocessoIndex].unidadeAtual = unidadeSubprocesso;

    // 10.8. Se a unidade de devolução for a própria unidade do subprocesso, apagar dataFimEtapa1
    if (unidadeSubprocesso === siglaUnidade.value) {
      processosStore.subprocessos[subprocessoIndex].dataFimEtapa1 = null;
    }
  }

  // 9.9. Enviar notificação por e-mail
  const assuntoEmail = `SGC: Cadastro de atividades e conhecimentos da ${unidadeSubprocesso} devolvido para ajustes`;
  const descricaoProcesso: string = processoAtual.value ? processoAtual.value.descricao : 'N/A';
  const corpoEmail = 'Prezado(a) responsável pela ' + unidadeSubprocesso + '\\n\\nO cadastro de atividades e conhecimentos da ' + unidadeSubprocesso + ' no processo ' + descricaoProcesso + ' foi devolvido para ajustes.\\nAcompanhe o processo no O sistema de Gestão de Competências: ' + URL_SISTEMA + '.';

  notificacoesStore.email(assuntoEmail, `Responsável pela ${unidadeSubprocesso}`, corpoEmail);

  // 9.10. Criar alerta
  alertasStore.criarAlerta({
    idProcesso: idProcesso.value,
    unidadeOrigem: unidadeAnalise,
    unidadeDestino: unidadeSubprocesso,
    descricao: `Cadastro de atividades e conhecimentos da unidade ${unidadeSubprocesso} devolvido para ajustes`,
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
  if (revisaoStore.mudancasRegistradas.length === 0) {
    notificacoesStore.info("Impacto no mapa", 'Nenhum impacto no mapa da unidade.');
    return;
  }

  revisaoStore.setMudancasParaImpacto(revisaoStore.mudancasRegistradas);
  mostrarModalImpacto.value = true;
}

function fecharModalImpacto() {
  mostrarModalImpacto.value = false;
  revisaoStore.setMudancasParaImpacto([]);
}

function abrirModalHistoricoAnalise() {
  mostrarModalHistoricoAnalise.value = true;
}

function fecharModalHistoricoAnalise() {
  mostrarModalHistoricoAnalise.value = false;
}

function fecharModalHomologacaoSemImpacto() {
  mostrarModalHomologacaoSemImpacto.value = false;
}

function confirmarHomologacaoSemImpacto() {
  if (!idSubprocesso.value) return;
  
  // 12.2.4 - Alterar situação para 'Mapa homologado'
  const subprocessoIndex = processosStore.subprocessos.findIndex(pu => pu.id === idSubprocesso.value);
  if (subprocessoIndex !== -1) {
    processosStore.subprocessos[subprocessoIndex].situacao = 'Mapa homologado';
  }
  
  notificacoesStore.sucesso('Homologação efetivada', 'O mapa de competências vigente foi mantido!');
  fecharModalHomologacaoSemImpacto();
  router.push(`/processo/${idProcesso.value}/${siglaUnidade.value}`);
}
</script>

<style>
.unidade-nome {
  color: var(--bs-body-color);
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

.unidade-cabecalho {
  font-size: 1.1rem;
  font-weight: 500;
  margin-bottom: 1.2rem;
  display: flex;
  gap: 0.5rem;
}

.unidade-sigla {
  background: var(--bs-light);
  color: var(--bs-dark);
  font-weight: bold;
  border-radius: 0.5rem;
  letter-spacing: 1px;
}

.unidade-nome {
  color: var(--bs-body-color);
  opacity: 0.85;
  padding-right: 1rem;
}

</style>