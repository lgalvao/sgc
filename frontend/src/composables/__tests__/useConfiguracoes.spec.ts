import {beforeEach, describe, expect, it, vi} from 'vitest';
import {PiniaColada} from '@pinia/colada';
import {mount} from '@vue/test-utils';
import {defineComponent} from 'vue';
import {useConfiguracoes} from '../useConfiguracoes';
import * as configuracaoService from '@/services/configuracaoService';
import {criarPiniaDeTeste} from '@/test-utils/storeTestHelpers';

vi.mock('@/services/configuracaoService', () => ({
    buscarConfiguracoes: vi.fn(),
    salvarConfiguracoes: vi.fn()
}));

describe('useConfiguracoes', () => {
    function montarComposable() {
        const pinia = criarPiniaDeTeste();
        let composable!: ReturnType<typeof useConfiguracoes>;

        mount(defineComponent({
            setup() {
                composable = useConfiguracoes();
                return () => null;
            },
        }), {
            global: {
                plugins: [pinia, [PiniaColada, {}]],
            },
        });

        return composable;
    }

    beforeEach(() => {
        vi.clearAllMocks();
        localStorage.clear();
    });

    it('carregarConfiguracoes deve preencher configuracoes em caso de sucesso', async () => {
        const composable = montarComposable();
        const mockData = [
            {codigo: 1, chave: 'DIAS_INATIVACAO_PROCESSO', descricao: 'Desc', valor: '60'}
        ];
        vi.mocked(configuracaoService.buscarConfiguracoes).mockResolvedValue(mockData);

        await composable.carregarConfiguracoes();

        expect(configuracaoService.buscarConfiguracoes).toHaveBeenCalled();
        expect(composable.configuracoes.value).toEqual(mockData);
        expect(composable.erro.value).toBeNull();
        expect(composable.carregandoConfiguracoes.value).toBe(false);
    });

    it('carregarConfiguracoes deve definir erro em caso de falha', async () => {
        const composable = montarComposable();
        vi.mocked(configuracaoService.buscarConfiguracoes).mockRejectedValue(new Error('Erro API'));

        await composable.carregarConfiguracoes();

        expect(composable.configuracoes.value).toEqual([]);
        expect(composable.erro.value).toBe('Erro API');
        expect(composable.carregandoConfiguracoes.value).toBe(false);
    });

    it('salvarConfiguracoes deve atualizar configuracoes em caso de sucesso', async () => {
        const composable = montarComposable();
        const novosParametros = [
            {codigo: 1, chave: 'DIAS_INATIVACAO_PROCESSO', descricao: 'Desc', valor: '90'}
        ];
        vi.mocked(configuracaoService.salvarConfiguracoes).mockResolvedValue(novosParametros);

        const result = await composable.salvarConfiguracoes(novosParametros);

        expect(configuracaoService.salvarConfiguracoes).toHaveBeenCalledWith(novosParametros);
        expect(composable.configuracoes.value).toEqual(novosParametros);
        expect(result).toBe(true);
        expect(composable.erro.value).toBeNull();
    });

    it('salvarConfiguracoes deve definir erro em caso de falha', async () => {
        const composable = montarComposable();
        const novosParametros = [
            {codigo: 1, chave: 'DIAS_INATIVACAO_PROCESSO', descricao: 'Desc', valor: '90'}
        ];
        vi.mocked(configuracaoService.salvarConfiguracoes).mockRejectedValue(new Error('Erro API'));

        const result = await composable.salvarConfiguracoes(novosParametros);

        expect(result).toBe(false);
        expect(composable.erro.value).toBe('Erro API');
    });

    it('obterDiasInativacaoProcesso deve retornar valor configurado ou padrao', async () => {
        expect(montarComposable().obterDiasInativacaoProcesso()).toBe(30);

        const composableConfigurado = montarComposable();
        vi.mocked(configuracaoService.buscarConfiguracoes).mockResolvedValue([
            {codigo: 1, chave: 'DIAS_INATIVACAO_PROCESSO', descricao: 'Desc', valor: '45'}
        ]);
        await composableConfigurado.carregarConfiguracoes();
        expect(composableConfigurado.obterDiasInativacaoProcesso()).toBe(45);

        const composableInvalido = montarComposable();
        vi.mocked(configuracaoService.buscarConfiguracoes).mockResolvedValue([
            {codigo: 1, chave: 'DIAS_INATIVACAO_PROCESSO', descricao: 'Desc', valor: 'abc'}
        ]);
        await composableInvalido.carregarConfiguracoes();
        expect(composableInvalido.obterDiasInativacaoProcesso()).toBe(30);
    });

    it('obterDiasAlertaNovo deve retornar valor configurado ou padrao', async () => {
        expect(montarComposable().obterDiasAlertaNovo()).toBe(3);

        const composableConfigurado = montarComposable();
        vi.mocked(configuracaoService.buscarConfiguracoes).mockResolvedValue([
            {codigo: 1, chave: 'DIAS_ALERTA_NOVO', descricao: 'Desc', valor: '7'}
        ]);
        await composableConfigurado.carregarConfiguracoes();
        expect(composableConfigurado.obterDiasAlertaNovo()).toBe(7);

        const composableInvalido = montarComposable();
        vi.mocked(configuracaoService.buscarConfiguracoes).mockResolvedValue([
            {codigo: 1, chave: 'DIAS_ALERTA_NOVO', descricao: 'Desc', valor: 'xyz'}
        ]);
        await composableInvalido.carregarConfiguracoes();
        expect(composableInvalido.obterDiasAlertaNovo()).toBe(3);
    });


});
