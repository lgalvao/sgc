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
        podeEnviarLembrete: true
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
});
