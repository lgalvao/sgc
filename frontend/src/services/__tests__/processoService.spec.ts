import {beforeEach, describe, expect, it, vi} from 'vitest';
import * as processoService from '../processo';
import apiClient from '@/axios-setup';
import {AtualizarProcessoRequest, CriarProcessoRequest, TipoProcesso} from '@/types/tipos';

vi.mock('@/axios-setup');

describe('processoService', () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    function criarDetalheProcesso() {
        return {
            codigo: 1,
            descricao: 'Processo teste',
            tipo: TipoProcesso.MAPEAMENTO,
            situacao: 'CRIADO',
            dataLimite: '2024-12-31',
            dataCriacao: '2024-01-01',
            dataFinalizacao: undefined,
            podeFinalizar: false,
            podeHomologarCadastro: false,
            podeHomologarMapa: false,
            podeAceitarCadastroBloco: false,
            podeDisponibilizarMapaBloco: false,
            unidades: [],
            resumoSubprocessos: [],
            acoesBloco: []
        };
    }

    it('buscarUnidadesParaImportacao deve mapear unidades para importacao', async () => {
        const codProcesso = 1;
        const responseData = [
            {
                nome: 'Unidade 1',
                sigla: 'U1',
                codUnidade: 1,
                codSubprocesso: 10,
                codUnidadeSuperior: null,
                situacaoSubprocesso: 'ATIVO',
                dataLimite: '2024-12-31',
                mapaCodigo: 100,
                localizacaoAtualCodigo: 1000,
                filhos: []
            },
            {
                nome: 'Unidade 2',
                sigla: 'U2',
                codUnidade: 2,
                codSubprocesso: null,
                codUnidadeSuperior: null,
                situacaoSubprocesso: null,
                dataLimite: null,
                mapaCodigo: null,
                localizacaoAtualCodigo: null,
                filhos: []
            }
        ];

        vi.mocked(apiClient.get).mockResolvedValue({data: responseData});

        const result = await processoService.buscarUnidadesParaImportacao(codProcesso);

        expect(apiClient.get).toHaveBeenCalledWith(`/processos/${codProcesso}/unidades-importacao`);

        expect(result[0].nome).toBe('Unidade 1');
        expect(result[0].sigla).toBe('U1');
        expect(result[0].codSubprocesso).toBe(10);
        expect(result[0].dataLimite).toBe('2024-12-31');
        expect(result[0].mapaCodigo).toBe(100);
        expect(result[0].localizacaoAtualCodigo).toBe(1000);

        expect(result[1].nome).toBe('Unidade 2');
        expect(result[1].sigla).toBe('U2');
        expect(result[1].codSubprocesso).toBe(0);
        expect(result[1].dataLimite).toBeUndefined();
        expect(result[1].mapaCodigo).toBeUndefined();
        expect(result[1].localizacaoAtualCodigo).toBeUndefined();
    });

    it('criarProcesso deve fazer requisição POST', async () => {
        const request: CriarProcessoRequest = {
            descricao: 'Teste',
            tipo: TipoProcesso.MAPEAMENTO,
            dataLimiteEtapa1: '2024-01-01',
            unidades: []
        };
        const responseData = {codigo: 1, ...request};
        vi.mocked(apiClient.post).mockResolvedValue({data: responseData});

        const result = await processoService.criarProcesso(request);

        expect(apiClient.post).toHaveBeenCalledWith('/processos', request);
        expect(result).toEqual(responseData);
    });

    it('buscarProcessosFinalizados deve fazer requisição GET', async () => {
        const responseData = [{codigo: 1, descricao: 'Finalizado'}];
        vi.mocked(apiClient.get).mockResolvedValue({data: responseData});

        const result = await processoService.buscarProcessosFinalizados();

        expect(apiClient.get).toHaveBeenCalledWith('/processos/finalizados');
        expect(result).toEqual(responseData);
    });

    it('iniciarProcesso deve fazer requisição POST', async () => {
        const codProcesso = 1;
        const tipo = TipoProcesso.MAPEAMENTO;
        const codigosUnidades = [1, 2];

        await processoService.iniciarProcesso(codProcesso, tipo, codigosUnidades);

        expect(apiClient.post).toHaveBeenCalledWith(`/processos/${codProcesso}/iniciar`, {
            tipo,
            unidades: codigosUnidades
        });
    });

    it('finalizarProcesso deve fazer requisição POST', async () => {
        const codProcesso = 1;
        await processoService.finalizarProcesso(codProcesso);
        expect(apiClient.post).toHaveBeenCalledWith(`/processos/${codProcesso}/finalizar`);
    });

    it('atualizarProcesso deve fazer requisição POST', async () => {
        const codProcesso = 1;
        const request: AtualizarProcessoRequest = {
            codigo: 1,
            descricao: 'Atualizado',
            tipo: TipoProcesso.MAPEAMENTO,
            dataLimiteEtapa1: '2024-01-01',
            unidades: []
        };
        const responseData = {...request};
        vi.mocked(apiClient.post).mockResolvedValue({data: responseData});

        const result = await processoService.atualizarProcesso(codProcesso, request);

        expect(apiClient.post).toHaveBeenCalledWith(`/processos/${codProcesso}/atualizar`, request);
        expect(result).toEqual(responseData);
    });

    it('excluirProcesso deve fazer requisição POST', async () => {
        const codProcesso = 1;
        await processoService.excluirProcesso(codProcesso);
        expect(apiClient.post).toHaveBeenCalledWith(`/processos/${codProcesso}/excluir`);
    });

    it('excluirProcessoCompleto deve fazer requisição POST', async () => {
        const codProcesso = 1;
        await processoService.excluirProcessoCompleto(codProcesso);
        expect(apiClient.post).toHaveBeenCalledWith(`/processos/${codProcesso}/excluir-completo`);
    });

    it('obterDetalhesProcesso deve fazer requisição GET', async () => {
        const codProcesso = 1;
        const responseData = criarDetalheProcesso();
        vi.mocked(apiClient.get).mockResolvedValue({data: responseData});

        const result = await processoService.obterDetalhesProcesso(codProcesso);

        expect(apiClient.get).toHaveBeenCalledWith(`/processos/${codProcesso}/detalhes`);
        expect(result).toMatchObject(responseData);
    });

    it('executarAcaoEmBloco deve fazer requisição POST com ação explícita', async () => {
        await processoService.executarAcaoEmBloco(1, {
            unidadeCodigos: [10],
            acao: 'ACEITAR',
            dataLimite: '2026-12-31'
        });
        expect(apiClient.post).toHaveBeenCalledWith('/processos/1/acao-em-bloco', {
            unidadeCodigos: [10],
            acao: 'ACEITAR',
            dataLimite: '2026-12-31'
        });
    });

    it('buscarContextoCompleto deve fazer requisição GET', async () => {
        const codProcesso = 1;
        const responseData = criarDetalheProcesso();
        vi.mocked(apiClient.get).mockResolvedValue({data: responseData});

        const result = await processoService.buscarContextoCompleto(codProcesso);

        expect(apiClient.get).toHaveBeenCalledWith(`/processos/${codProcesso}/contexto-completo`);
        expect(result).toEqual(responseData);
    });

    it('enviarLembrete deve fazer requisição POST', async () => {
        const codProcesso = 1;
        const unidadeCodigo = 2;
        await processoService.enviarLembrete(codProcesso, unidadeCodigo);
        expect(apiClient.post).toHaveBeenCalledWith(`/processos/${codProcesso}/enviar-lembrete`, {unidadeCodigo});
    });
});
