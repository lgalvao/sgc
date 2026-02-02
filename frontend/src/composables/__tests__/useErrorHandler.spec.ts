import {describe, expect, it, vi} from 'vitest';
import {useErrorHandler} from '../useErrorHandler';

describe('useErrorHandler', () => {
  it('inicializa com lastError como null', () => {
    const { lastError } = useErrorHandler();
    expect(lastError.value).toBeNull();
  });

  it('clearError limpa o erro', () => {
    const { lastError, clearError } = useErrorHandler();
    lastError.value = {
      kind: 'validation',
      message: 'Erro de validação'
    };
    clearError();
    expect(lastError.value).toBeNull();
  });

  it('withErrorHandling limpa erro antes de executar', async () => {
    const { lastError, withErrorHandling } = useErrorHandler();
    lastError.value = {
      kind: 'validation',
      message: 'Erro anterior'
    };

    await withErrorHandling(async () => {
      // Verificar que erro foi limpo
      expect(lastError.value).toBeNull();
      return 'sucesso';
    });
  });

  it('withErrorHandling retorna resultado quando bem-sucedido', async () => {
    const { withErrorHandling } = useErrorHandler();
    const resultado = await withErrorHandling(async () => {
      return { data: 'teste' };
    });
    expect(resultado).toEqual({ data: 'teste' });
  });

  it('withErrorHandling captura e normaliza erro', async () => {
    const { lastError, withErrorHandling } = useErrorHandler();
    const erro = new Error('Erro de teste');

    await expect(
      withErrorHandling(async () => {
        throw erro;
      })
    ).rejects.toThrow('Erro de teste');

    expect(lastError.value).not.toBeNull();
    expect(lastError.value?.message).toBe('Erro de teste');
  });

  it('withErrorHandling chama callback onError quando fornecido', async () => {
    const { withErrorHandling } = useErrorHandler();
    const onError = vi.fn();
    const erro = new Error('Erro de teste');

    await expect(
      withErrorHandling(async () => {
        throw erro;
      }, onError)
    ).rejects.toThrow();

    expect(onError).toHaveBeenCalledOnce();
    expect(onError).toHaveBeenCalledWith(
      expect.objectContaining({
        message: 'Erro de teste'
      })
    );
  });

  it('withErrorHandling não chama callback quando não há erro', async () => {
    const { withErrorHandling } = useErrorHandler();
    const onError = vi.fn();

    await withErrorHandling(async () => {
      return 'sucesso';
    }, onError);

    expect(onError).not.toHaveBeenCalled();
  });

  it('withErrorHandling re-lança o erro original', async () => {
    const { withErrorHandling } = useErrorHandler();
    const erroOriginal = new Error('Erro original');

    await expect(
      withErrorHandling(async () => {
        throw erroOriginal;
      })
    ).rejects.toBe(erroOriginal);
  });
});
