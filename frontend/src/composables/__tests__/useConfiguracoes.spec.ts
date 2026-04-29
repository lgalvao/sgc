import {beforeEach, describe, expect, it, vi} from 'vitest';
import {useConfiguracoes} from '../useConfiguracoes';
import * as configuracaoService from '@/services/configuracaoService';

vi.mock('@/services/configuracaoService', () => ({
    buscarConfiguracoes: vi.fn(),
    salvarConfiguracoes: vi.fn()
}));

describe('useConfiguracoes', () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    it('carregarConfiguracoes deve preencher configuracoes em caso de sucesso', async () => {
        const composable = useConfiguracoes();
        const mockData = [
            {codigo: 1, chave: 'DIAS_INATIVACAO_PROCESSO', descricao: 'Desc', valor: '60'}
        ];
        vi.mocked(configuracaoService.buscarConfiguracoes).mockResolvedValue(mockData);

        await composable.carregarConfiguracoes();

        expect(configuracaoService.buscarConfiguracoes).toHaveBeenCalled();
        expect(composable.configuracoes.value).toEqual(mockData);
        expect(composable.error.value).toBeNull();
        expect(composable.loading.value).toBe(false);
    });

    it('carregarConfiguracoes deve definir erro em caso de falha', async () => {
        const composable = useConfiguracoes();
        vi.mocked(configuracaoService.buscarConfiguracoes).mockRejectedValue(new Error('Erro API'));

        await composable.carregarConfiguracoes();

        expect(composable.configuracoes.value).toEqual([]);
        expect(composable.error.value).toBe('Erro API');
        expect(composable.loading.value).toBe(false);
    });

    it('salvarConfiguracoes deve atualizar configuracoes em caso de sucesso', async () => {
        const composable = useConfiguracoes();
        const novosParametros = [
            {codigo: 1, chave: 'DIAS_INATIVACAO_PROCESSO', descricao: 'Desc', valor: '90'}
        ];
        vi.mocked(configuracaoService.salvarConfiguracoes).mockResolvedValue(novosParametros);

        const result = await composable.salvarConfiguracoes(novosParametros);

        expect(configuracaoService.salvarConfiguracoes).toHaveBeenCalledWith(novosParametros);
        expect(composable.configuracoes.value).toEqual(novosParametros);
        expect(result).toBe(true);
        expect(composable.error.value).toBeNull();
    });

    it('salvarConfiguracoes deve definir erro em caso de falha', async () => {
        const composable = useConfiguracoes();
        const novosParametros = [
            {codigo: 1, chave: 'DIAS_INATIVACAO_PROCESSO', descricao: 'Desc', valor: '90'}
        ];
        vi.mocked(configuracaoService.salvarConfiguracoes).mockRejectedValue(new Error('Erro API'));

        const result = await composable.salvarConfiguracoes(novosParametros);

        expect(result).toBe(false);
        expect(composable.error.value).toBe('Erro API');
    });

    it('getValor deve retornar valor correto ou padrao', () => {
        const composable = useConfiguracoes();
        composable.configuracoes.value = [
            {codigo: 1, chave: 'TESTE_KEY', descricao: 'Desc', valor: 'valor_teste'}
        ];

        expect(composable.getValor('TESTE_KEY')).toBe('valor_teste');
        expect(composable.getValor('KEY_INEXISTENTE', 'padrao')).toBe('padrao');
    });

    it('getDiasInativacaoProcesso deve retornar valor configurado ou padrao', () => {
        const composable = useConfiguracoes();
        expect(composable.getDiasInativacaoProcesso()).toBe(30);

        composable.configuracoes.value = [
            {codigo: 1, chave: 'DIAS_INATIVACAO_PROCESSO', descricao: 'Desc', valor: '45'}
        ];
        expect(composable.getDiasInativacaoProcesso()).toBe(45);

        composable.configuracoes.value = [
            {codigo: 1, chave: 'DIAS_INATIVACAO_PROCESSO', descricao: 'Desc', valor: 'abc'}
        ];
        expect(composable.getDiasInativacaoProcesso()).toBe(30);
    });

    it('getDiasAlertaNovo deve retornar valor configurado ou padrao', () => {
        const composable = useConfiguracoes();
        expect(composable.getDiasAlertaNovo()).toBe(3);

        composable.configuracoes.value = [
            {codigo: 1, chave: 'DIAS_ALERTA_NOVO', descricao: 'Desc', valor: '7'}
        ];
        expect(composable.getDiasAlertaNovo()).toBe(7);

        composable.configuracoes.value = [
            {codigo: 1, chave: 'DIAS_ALERTA_NOVO', descricao: 'Desc', valor: 'xyz'}
        ];
        expect(composable.getDiasAlertaNovo()).toBe(3);
    });
});
