import {describe, expect, it} from 'vitest';
import {useAcesso} from '../useAcesso';
import {ref} from 'vue';
import type {PermissoesSubprocesso, SubprocessoDetalhe} from '@/types/tipos';
import {SituacaoSubprocesso, TipoProcesso} from '@/types/tipos';

function criarPermissoes(parciais: Partial<PermissoesSubprocesso> = {}): PermissoesSubprocesso {
  return {
    podeEditarCadastro: false,
    podeDisponibilizarCadastro: false,
    podeDevolverCadastro: false,
    podeAceitarCadastro: false,
    podeHomologarCadastro: false,
    podeEditarMapa: false,
    podeDisponibilizarMapa: false,
    podeValidarMapa: false,
    podeApresentarSugestoes: false,
    podeVerSugestoes: false,
    podeDevolverMapa: false,
    podeAceitarMapa: false,
    podeHomologarMapa: false,
    podeVisualizarImpacto: false,
    podeAlterarDataLimite: false,
    podeReabrirCadastro: false,
    podeReabrirRevisao: false,
    podeEnviarLembrete: false,
    mesmaUnidade: false,
    habilitarAcessoCadastro: false,
    habilitarAcessoMapa: false,
    habilitarEditarCadastro: false,
    habilitarDisponibilizarCadastro: false,
    habilitarDevolverCadastro: false,
    habilitarAceitarCadastro: false,
    habilitarHomologarCadastro: false,
    habilitarEditarMapa: false,
    habilitarDisponibilizarMapa: false,
    habilitarValidarMapa: false,
    habilitarApresentarSugestoes: false,
    habilitarDevolverMapa: false,
    habilitarAceitarMapa: false,
    habilitarHomologarMapa: false,
    ...parciais,
  };
}

function criarSubprocesso(parciais: Partial<SubprocessoDetalhe> = {}): SubprocessoDetalhe {
    return {
      codigo: 1,
      unidade: {codigo: 10, nome: 'Unidade', sigla: 'UND'},
      titular: null,
      responsavel: null,
    situacao: SituacaoSubprocesso.NAO_INICIADO,
      localizacaoAtual: 'UND',
      processoDescricao: 'Processo',
      dataCriacaoProcesso: '2024-01-01T00:00:00',
      ultimaDataLimiteSubprocesso: '2025-01-01T00:00:00',
    tipoProcesso: TipoProcesso.MAPEAMENTO,
      prazoEtapaAtual: '2025-01-01T00:00:00',
      isEmAndamento: true,
    etapaAtual: 1,
    movimentacoes: [],
    elementosProcesso: [],
    permissoes: criarPermissoes(),
    ...parciais,
  };
}

describe('useAcesso', () => {
  it('deve retornar false por padrao quando permissoes sao nulas', () => {
    const subprocesso = ref<SubprocessoDetalhe | null>(null);
    const acesso = useAcesso(subprocesso);

    expect(acesso.podeEditarCadastro.value).toBe(false);
  });

  it('deve retornar false por padrao quando subprocesso existe sem detalhe carregado', () => {
    const subprocesso = ref<SubprocessoDetalhe | null>(null);
    const acesso = useAcesso(subprocesso);

    expect(acesso.podeEditarCadastro.value).toBe(false);
    expect(acesso.podeDisponibilizarCadastro.value).toBe(false);
    expect(acesso.podeDevolverCadastro.value).toBe(false);
    expect(acesso.podeAceitarCadastro.value).toBe(false);
    expect(acesso.podeHomologarCadastro.value).toBe(false);
    expect(acesso.podeEditarMapa.value).toBe(false);
    expect(acesso.podeDisponibilizarMapa.value).toBe(false);
    expect(acesso.podeValidarMapa.value).toBe(false);
    expect(acesso.podeApresentarSugestoes.value).toBe(false);
    expect(acesso.podeDevolverMapa.value).toBe(false);
    expect(acesso.podeAceitarMapa.value).toBe(false);
    expect(acesso.podeHomologarMapa.value).toBe(false);
    expect(acesso.podeVisualizarImpacto.value).toBe(false);
    expect(acesso.podeAlterarDataLimite.value).toBe(false);
    expect(acesso.podeReabrirCadastro.value).toBe(false);
    expect(acesso.podeReabrirRevisao.value).toBe(false);
    expect(acesso.podeEnviarLembrete.value).toBe(false);
    expect(acesso.habilitarAcessoMapa.value).toBe(false);
    expect(acesso.podeVerSugestoes.value).toBe(false);
    expect(acesso.habilitarAcessoCadastro.value).toBe(false);
  });

  it('deve retornar false por padrao quando permissoes estao falsas', () => {
    const subprocesso = ref(criarSubprocesso());
    const acesso = useAcesso(subprocesso);

    expect(acesso.podeVerSugestoes.value).toBe(false);
    expect(acesso.habilitarAcessoCadastro.value).toBe(false);
  });

  it('deve mapear permissoes corretamente a partir do backend', () => {
    const subprocesso = ref(criarSubprocesso({
      permissoes: criarPermissoes({
        podeEditarCadastro: true,
        podeDisponibilizarCadastro: true,
        podeDevolverCadastro: false,
        podeAceitarCadastro: true,
        podeHomologarCadastro: false,
        podeEditarMapa: true,
        podeDisponibilizarMapa: false,
        podeValidarMapa: true,
        podeApresentarSugestoes: false,
        podeDevolverMapa: true,
        podeAceitarMapa: false,
        podeHomologarMapa: true,
        podeVisualizarImpacto: true,
        podeAlterarDataLimite: false,
        podeReabrirCadastro: true,
        podeReabrirRevisao: false,
        podeEnviarLembrete: true,
        habilitarAcessoMapa: true
      }),
    }));
    
    const acesso = useAcesso(subprocesso);

    expect(acesso.podeEditarCadastro.value).toBe(true);
    expect(acesso.podeDisponibilizarCadastro.value).toBe(true);
    expect(acesso.podeDevolverCadastro.value).toBe(false);
    expect(acesso.podeAceitarCadastro.value).toBe(true);
    expect(acesso.podeHomologarCadastro.value).toBe(false);
    expect(acesso.podeEditarMapa.value).toBe(true);
    expect(acesso.podeDisponibilizarMapa.value).toBe(false);
    expect(acesso.podeValidarMapa.value).toBe(true);
    expect(acesso.podeApresentarSugestoes.value).toBe(false);
    expect(acesso.podeDevolverMapa.value).toBe(true);
    expect(acesso.podeAceitarMapa.value).toBe(false);
    expect(acesso.podeHomologarMapa.value).toBe(true);
    expect(acesso.podeVisualizarImpacto.value).toBe(true);
    expect(acesso.podeAlterarDataLimite.value).toBe(false);
    expect(acesso.podeReabrirCadastro.value).toBe(true);
    expect(acesso.podeReabrirRevisao.value).toBe(false);
    expect(acesso.podeEnviarLembrete.value).toBe(true);
    expect(acesso.habilitarAcessoMapa.value).toBe(true);
  });

  it('deve lidar com input não-ref (objeto direto)', () => {
    const subprocesso = criarSubprocesso({
      permissoes: criarPermissoes({
        podeEditarCadastro: true
      }),
    });

    const acesso = useAcesso(subprocesso);
    expect(acesso.podeEditarCadastro.value).toBe(true);
    expect(acesso.podeDisponibilizarCadastro.value).toBe(false); // default
  });

  it('deve calcular podeAnalisarCadastro e podeAnalisarMapa corretamente', () => {
    const subprocesso = criarSubprocesso({permissoes: criarPermissoes({podeDevolverCadastro: true, podeDevolverMapa: true})});
    const acesso = useAcesso(subprocesso);
    expect(acesso.podeAnalisarCadastro.value).toBe(true);
    expect(acesso.podeAnalisarMapa.value).toBe(true);

    const subprocesso2 = criarSubprocesso({permissoes: criarPermissoes({podeAceitarCadastro: true, podeAceitarMapa: true})});
    const acesso2 = useAcesso(subprocesso2);
    expect(acesso2.podeAnalisarCadastro.value).toBe(true);
    expect(acesso2.podeAnalisarMapa.value).toBe(true);

    const subprocesso3 = criarSubprocesso({permissoes: criarPermissoes({podeHomologarCadastro: true, podeHomologarMapa: true})});
    const acesso3 = useAcesso(subprocesso3);
    expect(acesso3.podeAnalisarCadastro.value).toBe(true);
    expect(acesso3.podeAnalisarMapa.value).toBe(true);
  });

  it('deve retornar permissoes adicionais como podeVerSugestoes e habilitarAcessoCadastro', () => {
    const subprocesso = criarSubprocesso({permissoes: criarPermissoes({podeVerSugestoes: true, habilitarAcessoCadastro: true})});
    const acesso = useAcesso(subprocesso);
    expect(acesso.podeVerSugestoes.value).toBe(true);
    expect(acesso.habilitarAcessoCadastro.value).toBe(true);
  });

  it('deve usar habilitadores enviados pelo backend', () => {
    const subprocesso = criarSubprocesso({
      permissoes: criarPermissoes({
        habilitarEditarCadastro: true,
        habilitarDisponibilizarCadastro: true,
        habilitarDevolverCadastro: true,
        habilitarAceitarCadastro: true,
        habilitarHomologarCadastro: true,
        habilitarEditarMapa: true,
        habilitarDisponibilizarMapa: true,
        habilitarValidarMapa: true,
        habilitarApresentarSugestoes: true,
        habilitarDevolverMapa: true,
        habilitarAceitarMapa: true,
        habilitarHomologarMapa: true,
      }),
    });
    const acesso = useAcesso(subprocesso);

    expect(acesso.habilitarEditarCadastro.value).toBe(true);
    expect(acesso.habilitarDisponibilizarCadastro.value).toBe(true);
    expect(acesso.habilitarDevolverCadastro.value).toBe(true);
    expect(acesso.habilitarAceitarCadastro.value).toBe(true);
    expect(acesso.habilitarHomologarCadastro.value).toBe(true);
    expect(acesso.habilitarEditarMapa.value).toBe(true);
    expect(acesso.habilitarDisponibilizarMapa.value).toBe(true);
    expect(acesso.habilitarValidarMapa.value).toBe(true);
    expect(acesso.habilitarApresentarSugestoes.value).toBe(true);
    expect(acesso.habilitarDevolverMapa.value).toBe(true);
    expect(acesso.habilitarAceitarMapa.value).toBe(true);
    expect(acesso.habilitarHomologarMapa.value).toBe(true);

    const subprocessoFalse = criarSubprocesso({permissoes: criarPermissoes({podeEditarCadastro: true, habilitarEditarCadastro: false})});
    const acessoFalse = useAcesso(subprocessoFalse);
    expect(acessoFalse.habilitarEditarCadastro.value).toBe(false);
  });

  it('deve calcular analisadores de mapa e flags de habilitação de mapa', () => {
    const subprocesso = criarSubprocesso({
      permissoes: criarPermissoes({
        podeDevolverMapa: true,
        podeAceitarMapa: false,
        podeHomologarMapa: false,
        podeEditarMapa: true,
        podeDisponibilizarMapa: true,
        podeValidarMapa: true,
        podeApresentarSugestoes: true,
        habilitarDevolverMapa: true,
        habilitarAceitarMapa: false,
        habilitarHomologarMapa: false,
        habilitarEditarMapa: true,
        habilitarDisponibilizarMapa: true,
        habilitarValidarMapa: true,
        habilitarApresentarSugestoes: true,
      }),
    });
    const acesso = useAcesso(subprocesso);

    expect(acesso.podeAnalisarMapa.value).toBe(true);
    expect(acesso.habilitarDevolverMapa.value).toBe(true);
    expect(acesso.habilitarAceitarMapa.value).toBe(false);
    expect(acesso.habilitarHomologarMapa.value).toBe(false);
    expect(acesso.habilitarEditarMapa.value).toBe(true);
    expect(acesso.habilitarDisponibilizarMapa.value).toBe(true);
    expect(acesso.habilitarValidarMapa.value).toBe(true);
    expect(acesso.habilitarApresentarSugestoes.value).toBe(true);
  });
});
