<template>
  <div class="container mt-4">
    <h2>Cadastro de processo</h2>

    <form class="mt-4 col-md-6 col-sm-8 col-12">
      <div class="mb-3">
        <label
          class="form-label"
          for="descricao"
        >Descrição</label>
        <input
          id="descricao"
          v-model="descricao"
          class="form-control"
          placeholder="Descreva o processo"
          type="text"
        >
      </div>

      <div class="mb-3">
        <label
          class="form-label"
          for="tipo"
        >Tipo</label>
        <select
          id="tipo"
          v-model="tipo"
          class="form-select"
        >
          <option
            v-for="tipoOption in TipoProcesso"
            :key="tipoOption"
            :value="tipoOption"
          >
            {{ tipoOption }}
          </option>
        </select>
      </div>

      <div class="mb-3">
        <label class="form-label">Unidades participantes</label>
        <div class="border rounded p-3">
          <div>
            <template
              v-for="unidade in unidadesStore.unidades"
              :key="unidade.sigla"
            >
              <div
                :style="{ marginLeft: '0' }"
                class="form-check"
              >
                <!--suppress HtmlUnknownAttribute -->
                <input
                  :id="`chk-${unidade.sigla}`"
                  :checked="getEstadoSelecao(unidade) === true"
                  class="form-check-input"
                  type="checkbox"
                  :indeterminate="getEstadoSelecao(unidade) === 'indeterminate'"
                  @change="() => toggleUnidade(unidade)"
                >
                <label
                  :for="`chk-${unidade.sigla}`"
                  class="form-check-label ms-2"
                >
                  <strong>{{ unidade.sigla }}</strong> - {{ unidade.nome }}
                </label>
              </div>
              <div
                v-if="unidade.filhas && unidade.filhas.length"
                class="ms-4"
              >
                <template
                  v-for="filha in unidade.filhas"
                  :key="filha.sigla"
                >
                  <div class="form-check">
                    <!--suppress HtmlUnknownAttribute -->
                    <input
                      :id="`chk-${filha.sigla}`"
                      :checked="getEstadoSelecao(filha) === true"
                      class="form-check-input"
                      type="checkbox"
                      :indeterminate="getEstadoSelecao(filha) === 'indeterminate'"
                      @change="() => toggleUnidade(filha)"
                    >
                    <label
                      :for="'chk-' + filha.sigla"
                      class="form-check-label ms-2"
                    >
                      <strong>{{ filha.sigla }}</strong> - {{ filha.nome }}
                    </label>
                  </div>

                  <div
                    v-if="filha.filhas && filha.filhas.length"
                    class="ms-4"
                  >
                    <div
                      v-for="neta in filha.filhas"
                      :key="neta.sigla"
                      class="form-check"
                    >
                      <input
                        :id="'chk-' + neta.sigla"
                        :checked="isChecked(neta.sigla)"
                        class="form-check-input"
                        type="checkbox"
                        @change="() => toggleUnidade(neta)"
                      >
                      <label
                        :for="'chk-' + neta.sigla"
                        class="form-check-label ms-2"
                      >
                        <strong>{{ neta.sigla }}</strong> - {{ neta.nome }}
                      </label>
                    </div>
                  </div>
                </template>
              </div>
            </template>
          </div>
        </div>
      </div>

      <div class="mb-3">
        <label
          class="form-label"
          for="dataLimite"
        >Data limite</label>
        <input
          id="dataLimite"
          v-model="dataLimite"
          class="form-control"
          type="date"
        >
      </div>
      <button
        class="btn btn-primary"
        type="button"
        @click="salvarProcesso"
      >
        Salvar
      </button>
      <button
        class="btn btn-success ms-2"
        data-testid="btn-iniciar-processo"
        type="button"
        @click="abrirModalConfirmacao"
      >
        Iniciar processo
      </button>
      <button
        v-if="processoEditando"
        class="btn btn-danger ms-2"
        type="button"
        @click="abrirModalRemocao"
      >
        Remover
      </button>
      <router-link
        class="btn btn-secondary ms-2"
        to="/painel"
      >
        Cancelar
      </router-link>
    </form>

    <!-- Modal de confirmação CDU-05 -->
    <div
      v-if="mostrarModalConfirmacao"
      class="modal fade show"
      style="display: block;"
      tabindex="-1"
    >
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">
              Iniciar processo
            </h5>
            <button
              type="button"
              class="btn-close"
              @click="fecharModalConfirmacao"
            />
          </div>
          <div class="modal-body">
            <p>Ao iniciar o processo, não será mais possível editá-lo ou removê-lo e todas as unidades participantes serão notificadas por e-mail.</p>
          </div>
          <div class="modal-footer">
            <button
              type="button"
              class="btn btn-secondary"
              @click="fecharModalConfirmacao"
            >
              Cancelar
            </button>
            <button
              type="button"
              class="btn btn-primary"
              @click="confirmarIniciarProcesso"
            >
              Confirmar
            </button>
          </div>
        </div>
      </div>
    </div>
    <div
      v-if="mostrarModalConfirmacao"
      class="modal-backdrop fade show"
    />

    <!-- Modal de confirmação de remoção -->
    <div
      v-if="mostrarModalRemocao"
      class="modal fade show"
      style="display: block;"
      tabindex="-1"
    >
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">
              Iniciar processo
            </h5>
            <button
              type="button"
              class="btn-close"
              @click="fecharModalRemocao"
            />
          </div>
          <div class="modal-body">
            <p>Remover o processo '{{ descricao }}'? Esta ação não poderá ser desfeita.</p>
          </div>
          <div class="modal-footer">
            <button
              type="button"
              class="btn btn-secondary"
              @click="fecharModalRemocao"
            >
              Cancelar
            </button>
            <button
              type="button"
              class="btn btn-danger"
              @click="confirmarRemocao"
            >
              Remover
            </button>
          </div>
        </div>
      </div>
    </div>
    <div
      v-if="mostrarModalRemocao"
      class="modal-backdrop fade show"
    />
  </div>
</template>

<script lang="ts" setup>
import {onMounted, ref} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import {useProcessosStore} from '@/stores/processos'
import {useUnidadesStore} from '@/stores/unidades'
import {useMapasStore} from '@/stores/mapas'
import {useServidoresStore} from '@/stores/servidores'
import {useAlertasStore} from '@/stores/alertas'
import {Processo, SituacaoProcesso, TipoProcesso, Unidade, UnidadeSnapshot} from '@/types/tipos'
import {generateUniqueId} from '@/utils'
import {useNotificacoesStore} from '@/stores/notificacoes'
import {SITUACOES_SUBPROCESSO} from '@/constants/situacoes';
import {TEXTOS} from '@/constants';

const unidadesSelecionadas = ref<string[]>([])
const descricao = ref<string>('')
const tipo = ref<TipoProcesso>(TipoProcesso.MAPEAMENTO)
const dataLimite = ref<string>('')
const router = useRouter()
const route = useRoute()
const processosStore = useProcessosStore()
const unidadesStore = useUnidadesStore()
const mapasStore = useMapasStore()
const servidoresStore = useServidoresStore()
const alertasStore = useAlertasStore()
const notificacoesStore = useNotificacoesStore()
const mostrarModalConfirmacao = ref(false)
const mostrarModalRemocao = ref(false)
const processoEditando = ref<Processo | null>(null)

// Carregar processo se estiver editando
onMounted(() => {
  const idProcesso = route.query.idProcesso;
  if (idProcesso) {
    const processo = processosStore.processos.find(p => p.id === Number(idProcesso));
    if (processo) {
      processoEditando.value = processo;
      descricao.value = processo.descricao;
      tipo.value = processo.tipo;
      dataLimite.value = processo.dataLimite.toISOString().split('T')[0];
      
      // Carregar unidades participantes
      const subprocessos = processosStore.getUnidadesDoProcesso(processo.id);
      unidadesSelecionadas.value = subprocessos.map(sp => sp.unidade);
    }
  }
})

function limparCampos() {
  descricao.value = ''
  tipo.value = TipoProcesso.MAPEAMENTO
  dataLimite.value = ''
  unidadesSelecionadas.value = []
}

function isUnidadeIntermediaria(sigla: string): boolean {
  const unidade = unidadesStore.pesquisarUnidade(sigla);
  return !!(unidade && unidade.tipo === 'INTERMEDIARIA');
}

function unidadeTemMapaVigente(sigla: string): boolean {
  return !!mapasStore.getMapaVigentePorUnidade(sigla);
}

function unidadeTemServidores(sigla: string): boolean {
  return servidoresStore.servidores.filter(s => s.unidade === sigla).length > 0;
}

function validarUnidadesParaProcesso(tipo: TipoProcesso, unidadesSelecionadas: string[]): string[] {
  let unidadesValidas = unidadesSelecionadas.filter(sigla => !isUnidadeIntermediaria(sigla));

  if (tipo === TipoProcesso.REVISAO || tipo === TipoProcesso.DIAGNOSTICO) {
    unidadesValidas = unidadesValidas.filter(sigla => unidadeTemMapaVigente(sigla));
  }

  if (tipo === TipoProcesso.DIAGNOSTICO) {
    unidadesValidas = unidadesValidas.filter(sigla => unidadeTemServidores(sigla));
  }

  return unidadesValidas;
}

function salvarProcesso() {
   if (!descricao.value || !dataLimite.value || unidadesSelecionadas.value.length === 0) {
     notificacoesStore.erro(
       'Dados incompletos',
       'Preencha todos os campos e selecione ao menos uma unidade.'
     );
     return
   }

   const unidadesFiltradas = validarUnidadesParaProcesso(tipo.value, unidadesSelecionadas.value);

   if (unidadesFiltradas.length === 0) {
     notificacoesStore.erro(
       'Unidades inválidas',
       'Não é possível incluir em processos de revisão ou diagnóstico, unidades que ainda não passaram por processo de mapeamento.'
     );
     return
   }

   if (processoEditando.value) {
     // Editando processo existente
     processosStore.editarProcesso({
       id: processoEditando.value.id,
       descricao: descricao.value,
       tipo: tipo.value,
       dataLimite: new Date(dataLimite.value),
       unidades: unidadesFiltradas
     });
     
     notificacoesStore.sucesso(
       'Processo alterado',
       'O processo foi alterado com sucesso!'
     );
   } else {
     // Criando novo processo
     const novoidProcesso = processosStore.processos.length + 1;

     const novossubprocessosObjetos = unidadesFiltradas.map((unidadeSigla) => ({
       id: generateUniqueId(),
       idProcesso: novoidProcesso,
       unidade: unidadeSigla,
       dataLimiteEtapa1: new Date(dataLimite.value),
       dataLimiteEtapa2: new Date(dataLimite.value),
       dataFimEtapa1: null,
       dataFimEtapa2: null,
       unidadeAtual: unidadeSigla,
       unidadeAnterior: null,
       situacao: SITUACOES_SUBPROCESSO.NAO_INICIADO,
       movimentacoes: [],
       analises: []
     }));

     const novo = {
       id: novoidProcesso,
       descricao: descricao.value,
       tipo: tipo.value,
       dataLimite: new Date(dataLimite.value),
       situacao: SituacaoProcesso.CRIADO,
       dataFinalizacao: null
     };
     processosStore.adicionarProcesso(novo);
     processosStore.adicionarsubprocessos(novossubprocessosObjetos);

     notificacoesStore.sucesso(
       'Processo salvo',
       'O processo foi salvo com sucesso!'
     );
   }

   router.push('/painel');
   limparCampos();
}

function abrirModalConfirmacao() {
   if (!descricao.value || !dataLimite.value || unidadesSelecionadas.value.length === 0) {
     notificacoesStore.erro(
       'Dados incompletos',
       'Preencha todos os campos e selecione ao menos uma unidade.'
     );
     return
   }

   const unidadesFiltradas = validarUnidadesParaProcesso(tipo.value, unidadesSelecionadas.value);

   if (unidadesFiltradas.length === 0) {
     notificacoesStore.erro(
       'Unidades inválidas',
       'Não é possível incluir em processos de revisão ou diagnóstico, unidades que ainda não passaram por processo de mapeamento.'
     );
     return
   }

   mostrarModalConfirmacao.value = true
}

function fecharModalConfirmacao() {
  mostrarModalConfirmacao.value = false
}

function confirmarIniciarProcesso() {
  mostrarModalConfirmacao.value = false
  iniciarProcesso()
}

function abrirModalRemocao() {
  mostrarModalRemocao.value = true
}

function fecharModalRemocao() {
  mostrarModalRemocao.value = false
}

function confirmarRemocao() {
  if (processoEditando.value) {
    processosStore.removerProcesso(processoEditando.value.id)
    notificacoesStore.sucesso(
      'Processo removido',
      `${TEXTOS.PROCESSO_REMOVIDO_INICIO}${descricao.value}${TEXTOS.PROCESSO_REMOVIDO_FIM}`
    )
    router.push('/painel')
  }
  fecharModalRemocao()
}

function iniciarProcesso() {
   if (!descricao.value || !dataLimite.value || unidadesSelecionadas.value.length === 0) {
     notificacoesStore.erro(
       'Dados incompletos',
       'Preencha todos os campos e selecione ao menos uma unidade.'
     );
     return
   }

   const unidadesFiltradas = validarUnidadesParaProcesso(tipo.value, unidadesSelecionadas.value);

   if (unidadesFiltradas.length === 0) {
     notificacoesStore.erro(
       'Unidades inválidas',
       'Não é possível incluir em processos de revisão ou diagnóstico, unidades que ainda não passaram por processo de mapeamento.'
     );
     return
   }

   const novoidProcesso = processosStore.processos.length + 1;

   // Criar subprocessos com situações corretas conforme PDF
   const novossubprocessosObjetos = unidadesFiltradas.map((unidadeSigla) => {
     let situacaoInicial = SITUACOES_SUBPROCESSO.NAO_INICIADO;

     if (tipo.value === TipoProcesso.REVISAO) {
       situacaoInicial = SITUACOES_SUBPROCESSO.NAO_INICIADO; // Para revisão também começa como 'Não iniciado'
     }

     return {
       id: generateUniqueId(),
       idProcesso: novoidProcesso,
       unidade: unidadeSigla,
       dataLimiteEtapa1: new Date(dataLimite.value),
       dataLimiteEtapa2: null,
       dataFimEtapa1: null,
       dataFimEtapa2: null,
       unidadeAtual: unidadeSigla,
       unidadeAnterior: null,
       situacao: situacaoInicial,
       observacoes: '',
       sugestoes: '',
       movimentacoes: [],
       analises: [],
       idMapaCopiado: undefined
     };
   });

   const novo = {
     id: novoidProcesso,
     descricao: descricao.value,
     tipo: tipo.value,
     dataLimite: new Date(dataLimite.value),
     situacao: SituacaoProcesso.EM_ANDAMENTO,
     dataFinalizacao: null,
     unidadesSnapshot: buildUnidadesSnapshot(unidadesFiltradas)
   };

   processosStore.adicionarProcesso(novo);
   processosStore.adicionarsubprocessos(novossubprocessosObjetos);

   if (tipo.value === TipoProcesso.MAPEAMENTO) {
     // CDU-04 (item 10): criar mapa vazio para cada unidade participante
     unidadesFiltradas.forEach(unidadeSigla => {
       const novoMapa = {
         id: generateUniqueId(),
         unidade: unidadeSigla,
         idProcesso: novoidProcesso,
         competencias: [],
         situacao: 'em_andamento',
         dataCriacao: new Date(),
         dataDisponibilizacao: null,
         dataFinalizacao: null
       };
       mapasStore.adicionarMapa(novoMapa);
     });
   } else if (tipo.value === TipoProcesso.REVISAO) {
     // CDU-05 (item 10): copiar mapa vigente para o novo processo de revisão
     unidadesFiltradas.forEach(unidadeSigla => {
       const mapaVigente = mapasStore.getMapaVigentePorUnidade(unidadeSigla);
       if (mapaVigente) {
         const novoMapa = {
           ...mapaVigente,
           id: generateUniqueId(),
           idProcesso: novoidProcesso,
           dataCriacao: new Date(),
           dataDisponibilizacao: null,
           dataFinalizacao: null,
           situacao: 'em_andamento'
         };
         mapasStore.adicionarMapa(novoMapa);
         
         // Vincular referência do mapa copiado ao subprocesso
         const subprocesso = novossubprocessosObjetos.find(sp => sp.unidade === unidadeSigla);
         if (subprocesso) {
           subprocesso.idMapaCopiado = mapaVigente.id;
         }
       }
     });
   }

   // Movimentações iniciais serão registradas por enviarNotificacoesIniciarProcesso()

   enviarNotificacoesIniciarProcesso(novo, unidadesFiltradas);

   notificacoesStore.sucesso(
     'Processo iniciado',
     'O processo foi iniciado com sucesso! Notificações enviadas às unidades.'
   );

   router.push('/painel')
   limparCampos()
}

function enviarNotificacoesIniciarProcesso(processo: Processo, unidadesParticipantes: string[]) {
  const isRevisao = processo.tipo === TipoProcesso.REVISAO;
  const assunto = isRevisao
    ? `SGC: Início de processo de revisão do mapa de competências`
    : `SGC: Início de processo de mapeamento de competências`;

  unidadesParticipantes.forEach(unidadeSigla => {
    const unidade = unidadesStore.pesquisarUnidade(unidadeSigla);
    if (!unidade) return;

    const isOperacionalOuInteroperacional = unidade.tipo === 'OPERACIONAL' || unidade.tipo === 'INTEROPERACIONAL';
    const isIntermediaria = unidade.tipo === 'INTERMEDIARIA';

    if (isOperacionalOuInteroperacional) {
      // Notificação para unidades operacionais/interoperacionais
      const corpo = isRevisao
        ? `Comunicamos o início do processo ${processo.descricao} para a sua unidade. Já é possível realizar a revisão do seu cadastro de atividades e conhecimentos no sistema. O prazo para conclusão desta etapa do processo é ${processo.dataLimite.toLocaleDateString('pt-BR')}.`
        : `Comunicamos o início do processo ${processo.descricao} para a sua unidade. Já é possível realizar o cadastro de atividades e conhecimentos no sistema. O prazo para conclusão desta etapa do processo é ${processo.dataLimite.toLocaleDateString('pt-BR')}.`;

      notificacoesStore.email(assunto, `Responsável pela ${unidadeSigla}`, corpo);

      // Criar alerta
      alertasStore.criarAlerta({
        idProcesso: processo.id,
        unidadeOrigem: 'SEDOC',
        unidadeDestino: unidadeSigla,
        descricao: 'Início do processo',
        dataHora: new Date()
      });

      // Registrar movimentação
      const subprocesso = processosStore.subprocessos.find(sp => sp.idProcesso === processo.id && sp.unidade === unidadeSigla);
      if (subprocesso) {
        processosStore.addMovement({
          idSubprocesso: subprocesso.id,
          unidadeOrigem: 'SEDOC',
          unidadeDestino: unidadeSigla,
          descricao: 'Processo iniciado'
        });
      }
    }

    if (isIntermediaria || unidade.tipo === 'INTEROPERACIONAL') {
      // Para unidades intermediárias, buscar unidades subordinadas
      const unidadesSubordinadas = unidadesStore.getUnidadesSubordinadas(unidadeSigla)
        .filter(sigla => unidadesParticipantes.includes(sigla));

      if (unidadesSubordinadas.length > 0) {
        const siglasSubordinadas = unidadesSubordinadas.join(', ');

        const corpo = isRevisao
          ? `Comunicamos o início do processo ${processo.descricao} nas unidades ${siglasSubordinadas}. Estas unidades já podem iniciar a revisão do cadastro de atividades e conhecimentos. À medida que estas revisões forem sendo disponibilizadas, será possível visualizar e realizar a sua validação. O prazo para conclusão desta etapa do processo é ${processo.dataLimite.toLocaleDateString('pt-BR')}. Acompanhe o processo no sistema.`
          : `Comunicamos o início do processo ${processo.descricao} nas unidades ${siglasSubordinadas}. Estas unidades já podem iniciar o cadastro de atividades e conhecimentos. À medida que estes cadastros forem sendo disponibilizados, será possível visualizar e realizar a sua validação. O prazo para conclusão desta etapa do processo é ${processo.dataLimite.toLocaleDateString('pt-BR')}. Acompanhe o processo no sistema.`;

        notificacoesStore.email(assunto, `Responsável pela ${unidadeSigla}`, corpo);

        // Criar alerta para unidade intermediária
        alertasStore.criarAlerta({
          idProcesso: processo.id,
          unidadeOrigem: 'SEDOC',
          unidadeDestino: unidadeSigla,
          descricao: 'Início do processo em unidade(s) subordinada(s)',
          dataHora: new Date()
        });
      }
    }
  });
}

function getTodasSubunidades(unidade: Unidade): string[] {
  let subunidades: string[] = []
  if (unidade.filhas && unidade.filhas.length) {
    unidade.filhas.forEach(filha => {
      subunidades.push(filha.sigla)
      subunidades = [...subunidades, ...getTodasSubunidades(filha)]
    })
  }
  return subunidades
}

function isFolha(unidade: Unidade): boolean {
  return !unidade.filhas || unidade.filhas.length === 0
}

function isChecked(sigla: string): boolean {
  return unidadesSelecionadas.value.includes(sigla)
}

function getEstadoSelecao(unidade: Unidade): boolean | 'indeterminate' {
  const selfSelected = isChecked(unidade.sigla)

  if (isFolha(unidade)) {
    return selfSelected
  }

  const subunidades = getTodasSubunidades(unidade)
  const selecionadas = subunidades.filter(sigla => isChecked(sigla)).length

  if (selecionadas === 0) {
    // Permitir que a raiz interoperacional apareça selecionada mesmo sem filhas marcadas (regra 2.3.2.5)
    return selfSelected
  }
  if (selecionadas === subunidades.length) return true
  // Caso parcialmente selecionada (algumas filhas) ou a própria raiz esteja selecionada com parte das filhas
  return 'indeterminate'
}

function toggleUnidade(unidade: Unidade) {
  const unidadeEhInteroperacional = unidade.tipo === 'INTEROPERACIONAL'

  if (unidadeEhInteroperacional) {
    // Regra 2.3.2.5: a raiz interoperacional pode ser selecionada sem propagar para as subordinadas
    if (isChecked(unidade.sigla)) {
      // Desmarca apenas a própria unidade
      unidadesSelecionadas.value = unidadesSelecionadas.value.filter(sigla => sigla !== unidade.sigla)
    } else {
      // Marca apenas a própria unidade
      unidadesSelecionadas.value.push(unidade.sigla)
    }
    return
  }

  const todasSubunidades = [unidade.sigla, ...getTodasSubunidades(unidade)]
  const todasEstaoSelecionadas = todasSubunidades.every(sigla => isChecked(sigla))

  if (todasEstaoSelecionadas) {
    // Desseleciona a unidade e todas as subunidades
    unidadesSelecionadas.value = unidadesSelecionadas.value.filter(
      sigla => !todasSubunidades.includes(sigla)
    )
  } else {
    // Seleciona a unidade e todas as subunidades
    todasSubunidades.forEach(sigla => {
      if (!unidadesSelecionadas.value.includes(sigla)) {
        unidadesSelecionadas.value.push(sigla)
      }
    })
  }
}

function buildUnidadesSnapshot(unidadesSelecionadas: string[]): UnidadeSnapshot[] {
  // Get the root units that are selected or have selected descendants
  const roots = unidadesStore.unidades.filter(u =>
    unidadesSelecionadas.includes(u.sigla) ||
    getTodasSubunidades(u).some(s => unidadesSelecionadas.includes(s))
  );

  return roots.map(u => buildSnapshotRec(u, unidadesSelecionadas));
}

function buildSnapshotRec(unidade: Unidade, selected: string[]): UnidadeSnapshot {
  const filhas = unidade.filhas?.filter(f =>
    selected.includes(f.sigla) ||
    getTodasSubunidades(f).some(s => selected.includes(s))
  ).map(f => buildSnapshotRec(f, selected)) || [];

  return {
    sigla: unidade.sigla,
    tipo: unidade.tipo,
    filhas
  };
}


</script>

<style scoped>
input[type="checkbox"]:indeterminate {
  background-color: var(--bs-primary);
  border-color: var(--bs-primary);
  background-image: url("data:image/svg+xml,%3csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 20 20'%3e%3cpath fill='none' stroke='%23fff' stroke-linecap='round' stroke-linejoin='round' stroke-width='2' d='M6 10h8'/%3e%3c/svg%3e");
}



.ms-4 {
  border-left: 1px dashed var(--bs-border-color);
  padding-left: 1rem;
  margin-left: 0.5rem;
}




</style>