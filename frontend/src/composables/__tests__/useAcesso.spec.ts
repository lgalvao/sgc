import {describe, expect, it} from 'vitest';
import {useAcesso} from '../useAcesso';
import {ref} from 'vue';
import type {SubprocessoDetalhe} from '@/types/tipos';

describe('useAcesso', () => {
  it('deve retornar false por padrao quando permissoes sao nulas', () => {
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

  it('deve retornar false por padrao quando permissoes esta vazio', () => {
    const subprocesso = ref<SubprocessoDetalhe>({ permissoes: {} } as any);
    const acesso = useAcesso(subprocesso);

    expect(acesso.podeVerSugestoes.value).toBe(false);
    expect(acesso.habilitarAcessoCadastro.value).toBe(false);
  });

  it('deve mapear permissoes corretamente a partir do backend', () => {
    const subprocesso = ref<SubprocessoDetalhe>({
      permissoes: {
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
      }
    } as unknown as SubprocessoDetalhe);
    
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
    const subprocesso = {
      permissoes: {
        podeEditarCadastro: true
      }
    } as unknown as SubprocessoDetalhe;

    const acesso = useAcesso(subprocesso);
    expect(acesso.podeEditarCadastro.value).toBe(true);
    expect(acesso.podeDisponibilizarCadastro.value).toBe(false); // default
  });

  it('deve calcular podeAnalisarCadastro e podeAnalisarMapa corretamente', () => {
    const subprocesso = { permissoes: { podeDevolverCadastro: true, podeDevolverMapa: true } } as any;
    const acesso = useAcesso(subprocesso);
    expect(acesso.podeAnalisarCadastro.value).toBe(true);
    expect(acesso.podeAnalisarMapa.value).toBe(true);

    const subprocesso2 = { permissoes: { podeAceitarCadastro: true, podeAceitarMapa: true } } as any;
    const acesso2 = useAcesso(subprocesso2);
    expect(acesso2.podeAnalisarCadastro.value).toBe(true);
    expect(acesso2.podeAnalisarMapa.value).toBe(true);

    const subprocesso3 = { permissoes: { podeHomologarCadastro: true, podeHomologarMapa: true } } as any;
    const acesso3 = useAcesso(subprocesso3);
    expect(acesso3.podeAnalisarCadastro.value).toBe(true);
    expect(acesso3.podeAnalisarMapa.value).toBe(true);
  });

  it('deve retornar permissoes adicionais como podeVerSugestoes e habilitarAcessoCadastro', () => {
    const subprocesso = { permissoes: { podeVerSugestoes: true, habilitarAcessoCadastro: true } } as any;
    const acesso = useAcesso(subprocesso);
    expect(acesso.podeVerSugestoes.value).toBe(true);
    expect(acesso.habilitarAcessoCadastro.value).toBe(true);
  });

  it('deve calcular habilitadores baseados na mesmaUnidade', () => {
    const subprocesso = {
      permissoes: {
        mesmaUnidade: true,
        podeEditarCadastro: true,
        podeDisponibilizarCadastro: true,
        podeDevolverCadastro: true,
        podeAceitarCadastro: true,
        podeHomologarCadastro: true,
        podeEditarMapa: true,
        podeDisponibilizarMapa: true,
        podeValidarMapa: true,
        podeApresentarSugestoes: true,
        podeDevolverMapa: true,
        podeAceitarMapa: true,
        podeHomologarMapa: true,
      }
    } as any;
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

    // Test false case for mesmaUnidade
    const subprocessoFalse = { permissoes: { mesmaUnidade: false, podeEditarCadastro: true } } as any;
    const acessoFalse = useAcesso(subprocessoFalse);
    expect(acessoFalse.habilitarEditarCadastro.value).toBe(false);
  });
});
