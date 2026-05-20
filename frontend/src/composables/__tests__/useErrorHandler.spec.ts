import {describe, expect, it, vi} from 'vitest';
import {useErrorHandler} from '../useErrorHandler';

describe('useErrorHandler', () => {
    it('inicializa com ultimoErro como null', () => {
        const {ultimoErro} = useErrorHandler();
        expect(ultimoErro.value).toBeNull();
    });

    it('limparErro limpa o erro', () => {
        const {ultimoErro, limparErro} = useErrorHandler();
        ultimoErro.value = {
            tipo: 'validacao',
            mensagem: 'Erro de validação'
        };
        limparErro();
        expect(ultimoErro.value).toBeNull();
    });

    it('executarComTratamentoDeErros limpa erro antes de executar', async () => {
        const {ultimoErro, executarComTratamentoDeErros} = useErrorHandler();
        ultimoErro.value = {
            tipo: 'validacao',
            mensagem: 'Erro anterior'
        };

        await executarComTratamentoDeErros(async () => {
            // Verificar que erro foi limpo
            expect(ultimoErro.value).toBeNull();
            return 'sucesso';
        });
    });

    it('executarComTratamentoDeErros retorna resultado quando bem-sucedido', async () => {
        const {executarComTratamentoDeErros} = useErrorHandler();
        const resultado = await executarComTratamentoDeErros(async () => {
            return {data: 'teste'};
        });
        expect(resultado).toEqual({data: 'teste'});
    });

    it('executarComTratamentoDeErros captura e normaliza erro', async () => {
        const {ultimoErro, executarComTratamentoDeErros} = useErrorHandler();
        const erro = new Error('Erro de teste');

        await expect(
            executarComTratamentoDeErros(async () => {
                throw erro;
            })
        ).rejects.toThrow('Erro de teste');

        expect(ultimoErro.value).not.toBeNull();
        expect(ultimoErro.value?.mensagem).toBe('Erro de teste');
    });

    it('executarComTratamentoDeErros chama callback onError quando fornecido', async () => {
        const {executarComTratamentoDeErros} = useErrorHandler();
        const onError = vi.fn();
        const erro = new Error('Erro de teste');

        await expect(
            executarComTratamentoDeErros(async () => {
                throw erro;
            }, onError)
        ).rejects.toThrow();

        expect(onError).toHaveBeenCalledOnce();
        expect(onError).toHaveBeenCalledWith(
            expect.objectContaining({
                mensagem: 'Erro de teste'
            })
        );
    });

    it('executarComTratamentoDeErros não chama callback quando não há erro', async () => {
        const {executarComTratamentoDeErros} = useErrorHandler();
        const onError = vi.fn();

        await executarComTratamentoDeErros(async () => {
            return 'sucesso';
        }, onError);

        expect(onError).not.toHaveBeenCalled();
    });

    it('executarComTratamentoDeErros re-lança o erro original', async () => {
        const {executarComTratamentoDeErros} = useErrorHandler();
        const erroOriginal = new Error('Erro original');

        await expect(
            executarComTratamentoDeErros(async () => {
                throw erroOriginal;
            })
        ).rejects.toBe(erroOriginal);
    });
});
