import {describe, expect, it, vi} from 'vitest';
import {carregarContextoSubprocessoInicial} from '@/composables/useContextoSubprocesso';

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
