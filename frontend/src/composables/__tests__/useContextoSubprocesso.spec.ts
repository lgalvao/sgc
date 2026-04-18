import {describe, expect, it, vi} from 'vitest';
import {carregarContextoSubprocessoInicial} from '@/composables/useContextoSubprocesso';

function criarStoreMock() {
  return {
    garantirContextoEdicao: vi.fn(),
    garantirContextoEdicaoPorProcessoEUnidade: vi.fn(),
  };
}

describe('carregarContextoSubprocessoInicial', () => {
  it('deve priorizar codSubprocesso da query quando válido', async () => {
    const contexto = {detalhes: {codigo: 10}} as any;
    const store = criarStoreMock();
    store.garantirContextoEdicao.mockResolvedValue(contexto);

    const resultado = await carregarContextoSubprocessoInicial({
      codProcesso: 77,
      siglaUnidade: 'SUGEP',
      codSubprocessoQuery: '10',
      store,
    });

    expect(store.garantirContextoEdicao).toHaveBeenCalledWith(10);
    expect(store.garantirContextoEdicaoPorProcessoEUnidade).not.toHaveBeenCalled();
    expect(resultado).toEqual({codigo: 10, contexto});
  });

  it('deve usar processo+unidade quando query for inválida', async () => {
    const contexto = {detalhes: {codigo: 25}} as any;
    const store = criarStoreMock();
    store.garantirContextoEdicaoPorProcessoEUnidade.mockResolvedValue({codigo: 25, contexto});

    const resultado = await carregarContextoSubprocessoInicial({
      codProcesso: 88,
      siglaUnidade: 'SEPLAN',
      codSubprocessoQuery: 'invalido',
      store,
    });

    expect(store.garantirContextoEdicao).not.toHaveBeenCalled();
    expect(store.garantirContextoEdicaoPorProcessoEUnidade).toHaveBeenCalledWith(88, 'SEPLAN');
    expect(resultado).toEqual({codigo: 25, contexto});
  });

  it('deve cair para processo+unidade quando query válida não encontrar contexto', async () => {
    const store = criarStoreMock();
    store.garantirContextoEdicao.mockResolvedValue(null);
    store.garantirContextoEdicaoPorProcessoEUnidade.mockResolvedValue({
      codigo: 99,
      contexto: {detalhes: {codigo: 99}},
    });

    const resultado = await carregarContextoSubprocessoInicial({
      codProcesso: 1,
      siglaUnidade: 'GAB',
      codSubprocessoQuery: '15',
      store,
    });

    expect(store.garantirContextoEdicao).toHaveBeenCalledWith(15);
    expect(store.garantirContextoEdicaoPorProcessoEUnidade).toHaveBeenCalledWith(1, 'GAB');
    expect(resultado).toEqual({
      codigo: 99,
      contexto: {detalhes: {codigo: 99}},
    });
  });

  it('deve retornar null quando query e fallback não encontrarem contexto', async () => {
    const store = criarStoreMock();
    store.garantirContextoEdicao.mockResolvedValue(null);
    store.garantirContextoEdicaoPorProcessoEUnidade.mockResolvedValue(null);

    const resultado = await carregarContextoSubprocessoInicial({
      codProcesso: 1,
      siglaUnidade: 'GAB',
      codSubprocessoQuery: '15',
      store,
    });

    expect(resultado).toBeNull();
  });
});
