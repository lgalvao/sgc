import {ref, type Ref} from 'vue';
import logger from '@/utils/logger';
import {TEXTOS} from '@/constants/textos';
import {
  aceitarValidacao as aceitarValidacaoService,
  devolverValidacao as devolverValidacaoService,
  homologarValidacao as homologarValidacaoService,
  validarMapa as validarMapaService,
} from '@/services/processoService';

type NotificacaoMapa = (mensagem: string, variante: 'success' | 'danger' | 'warning' | 'info') => void;

interface AcaoPrincipalMapa {
  codigo: 'ACEITAR' | 'HOMOLOGAR';
  mensagemSucesso: string;
}

interface ParametrosUseMapaAcoesAnalise {
  codSubprocesso: Ref<number | null>;
  acaoPrincipalMapa: Ref<AcaoPrincipalMapa | null | undefined>;
  concluirAcaoPainel: (mensagem: string, fecharModal: () => void) => Promise<void>;
  notify: NotificacaoMapa;
}

export function useMapaAcoesAnalise({
  codSubprocesso,
  acaoPrincipalMapa,
  concluirAcaoPainel,
  notify,
}: ParametrosUseMapaAcoesAnalise) {
  const mostrarModalAceitar = ref(false);
  const mostrarModalValidar = ref(false);
  const mostrarModalDevolucao = ref(false);
  const observacaoDevolucao = ref('');
  const isLoading = ref(false);

  async function executarComLoading(acao: () => Promise<void>) {
    isLoading.value = true;
    try {
      await acao();
    } finally {
      isLoading.value = false;
    }
  }

  async function confirmarValidacao() {
    if (!codSubprocesso.value) return;
    try {
      await executarComLoading(async () => {
        await validarMapaService(codSubprocesso.value!);
        await concluirAcaoPainel(TEXTOS.sucesso.MAPA_VALIDADO_SUBMETIDO, fecharModalValidar);
      });
    } catch {
      notify(TEXTOS.mapa.ERRO_VALIDAR, 'danger');
    }
  }

  async function confirmarAceitacao(observacao = '') {
    if (!codSubprocesso.value) return;

    const acao = acaoPrincipalMapa.value;
    if (!acao) return;

    try {
      await executarComLoading(async () => {
        if (acao.codigo === 'HOMOLOGAR') {
          await homologarValidacaoService(codSubprocesso.value!, {texto: observacao});
        } else {
          await aceitarValidacaoService(codSubprocesso.value!, {texto: observacao});
        }
        await concluirAcaoPainel(acao.mensagemSucesso, fecharModalAceitar);
      });
    } catch (error) {
      logger.error(error);
      notify(TEXTOS.comum.ERRO_OPERACAO, 'danger');
    }
  }

  async function confirmarDevolucao() {
    if (!codSubprocesso.value) return;
    try {
      await executarComLoading(async () => {
        await devolverValidacaoService(codSubprocesso.value!, {
          justificativa: observacaoDevolucao.value,
        });
        await concluirAcaoPainel(TEXTOS.sucesso.DEVOLUCAO_REALIZADA, fecharModalDevolucao);
      });
    } catch (error) {
      logger.error(error);
      notify(TEXTOS.mapa.ERRO_DEVOLVER, 'danger');
    }
  }

  function abrirModalAceitar() {
    mostrarModalAceitar.value = true;
  }

  function fecharModalAceitar() {
    mostrarModalAceitar.value = false;
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
    observacaoDevolucao.value = '';
  }

  return {
    mostrarModalAceitar,
    mostrarModalValidar,
    mostrarModalDevolucao,
    observacaoDevolucao,
    isLoading,
    confirmarValidacao,
    confirmarAceitacao,
    confirmarDevolucao,
    abrirModalAceitar,
    fecharModalAceitar,
    abrirModalValidar,
    fecharModalValidar,
    abrirModalDevolucao,
    fecharModalDevolucao,
  };
}
