import {describe, expect, it, vi} from 'vitest';
import {
  carregarContextoSubprocessoInicial,
  diagnosticarCarregamentoContextoSubprocessoInicial,
} from '@/composables/useContextoSubprocesso';

function criarStoreMock() {
  return {
    garantirContextoEdicao: vi.fn(),
    garantirContextoEdicaoPorProcessoEUnidade: vi.fn(),
  };
}

describe('carregarContextoSubprocessoInicial', () => {
  it('deve carregar contexto por processo e unidade', async () => {
    const contexto = {detalhes: {codigo: 25}} as any;
    const store = criarStoreMock();
    store.garantirContextoEdicaoPorProcessoEUnidade.mockResolvedValue({codigo: 25, contexto});

    const resultado = await carregarContextoSubprocessoInicial({
      codProcesso: 77,
      siglaUnidade: 'SUGEP',
      store,
    });

    expect(store.garantirContextoEdicao).not.toHaveBeenCalled();
    expect(store.garantirContextoEdicaoPorProcessoEUnidade).toHaveBeenCalledWith(77, 'SUGEP');
    expect(resultado).toEqual({codigo: 25, contexto});
  });

  it('deve ignorar carregamento direto por código de subprocesso', async () => {
    const contexto = {detalhes: {codigo: 99}} as any;
    const store = criarStoreMock();
    store.garantirContextoEdicaoPorProcessoEUnidade.mockResolvedValue({codigo: 99, contexto});

    const resultado = await carregarContextoSubprocessoInicial({
      codProcesso: 88,
      siglaUnidade: 'SEPLAN',
      store,
    });

    expect(store.garantirContextoEdicao).not.toHaveBeenCalled();
    expect(store.garantirContextoEdicaoPorProcessoEUnidade).toHaveBeenCalledWith(88, 'SEPLAN');
    expect(resultado).toEqual({codigo: 99, contexto});
  });

  it('deve retornar null quando processo e unidade não encontrarem contexto', async () => {
    const store = criarStoreMock();
    store.garantirContextoEdicaoPorProcessoEUnidade.mockResolvedValue(null);

    const resultado = await carregarContextoSubprocessoInicial({
      codProcesso: 1,
      siglaUnidade: 'GAB',
      store,
    });

    expect(store.garantirContextoEdicao).not.toHaveBeenCalled();
    expect(store.garantirContextoEdicaoPorProcessoEUnidade).toHaveBeenCalledWith(1, 'GAB');
    expect(resultado).toBeNull();
  });
});

describe('diagnosticarCarregamentoContextoSubprocessoInicial', () => {
  it('deve retornar sucesso quando encontrar contexto', async () => {
    const resultadoMock = {codigo: 1, contexto: {} as any};
    const store = criarStoreMock();
    store.garantirContextoEdicaoPorProcessoEUnidade.mockResolvedValue(resultadoMock);

    const resultado = await diagnosticarCarregamentoContextoSubprocessoInicial({
      codProcesso: 1,
      siglaUnidade: 'U',
      store,
    });

    expect(resultado).toEqual({tipo: 'sucesso', resultado: resultadoMock});
  });

  it('deve retornar cancelado quando a requisição for cancelada', async () => {
    const store = {
      ...criarStoreMock(),
      erroIntegracaoContexto: {code: 'REQUEST_CANCELADA'},
    };
    store.garantirContextoEdicaoPorProcessoEUnidade.mockResolvedValue(null);

    const resultado = await diagnosticarCarregamentoContextoSubprocessoInicial({
      codProcesso: 1,
      siglaUnidade: 'U',
      store: store as any,
    });

    expect(resultado).toEqual({tipo: 'cancelado'});
  });

  it('deve retornar erroIntegracao quando houver outro erro', async () => {
    const erroMock = {code: 'ERRO_INTERNO'} as any;
    const store = {
      ...criarStoreMock(),
      erroIntegracaoContexto: erroMock,
    };
    store.garantirContextoEdicaoPorProcessoEUnidade.mockResolvedValue(null);

    const resultado = await diagnosticarCarregamentoContextoSubprocessoInicial({
      codProcesso: 1,
      siglaUnidade: 'U',
      store: store as any,
    });

    expect(resultado).toEqual({tipo: 'erroIntegracao', erro: erroMock});
  });

  it('deve retornar ausencia quando não encontrar nada e não houver erro', async () => {
    const store = criarStoreMock();
    store.garantirContextoEdicaoPorProcessoEUnidade.mockResolvedValue(null);

    const resultado = await diagnosticarCarregamentoContextoSubprocessoInicial({
      codProcesso: 1,
      siglaUnidade: 'U',
      store,
    });

    expect(resultado).toEqual({tipo: 'ausencia'});
  });
});
