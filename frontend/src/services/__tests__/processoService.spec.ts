import {beforeEach, describe, expect, it, vi} from 'vitest';
import * as processoService from '../processoService';
import apiClient from '@/axios-setup';
import {AtualizarProcessoRequest, CriarProcessoRequest, TipoProcesso} from '@/types/tipos';

vi.mock('@/axios-setup');

describe('processoService', () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    describe('Mappers', () => {
        it('deve mapear UnidadeParticipanteDto com e sem campos opcionais', () => {
            // Com todos os campos preenchidos
            const dtoCompleto = {
                codUnidade: 1,
                codSubprocesso: 10,
                situacaoSubprocesso: 'MAPEAMENTO_MAPA_EM_ANDAMENTO',
                dataLimite: '2024-12-31',
                filhos: [{
                    codUnidade: 2,
                    codSubprocesso: 20
                }]
            };

            const frontendCompleto = processoService.mapUnidadeParticipanteDtoToFrontend(dtoCompleto as any);
            expect(frontendCompleto.codUnidade).toBe(1);
            expect(frontendCompleto.codSubprocesso).toBe(10);
            expect(frontendCompleto.situacaoSubprocesso).toBe('MAPEAMENTO_MAPA_EM_ANDAMENTO');
            expect(frontendCompleto.dataLimite).toBe('2024-12-31');
            expect(frontendCompleto.filhos.length).toBe(1);
            expect(frontendCompleto.filhos[0].codUnidade).toBe(2);

            // Faltando campos opcionais
            const dtoIncompleto = {
                codUnidade: 3,
            };
            const frontendIncompleto = processoService.mapUnidadeParticipanteDtoToFrontend(dtoIncompleto as any);
            expect(frontendIncompleto.codUnidade).toBe(3);
            expect(frontendIncompleto.codSubprocesso).toBe(0);
            expect(frontendIncompleto.situacaoSubprocesso).toBe('NAO_INICIADO');
            expect(frontendIncompleto.dataLimite).toBe('');
            expect(frontendIncompleto.filhos).toEqual([]);
        });

        it('deve mapear ProcessoDetalheDto com e sem unidades/resumos', () => {
            // Completo
            const dtoCompleto = {
                codigo: 1,
                unidades: [{ codUnidade: 1 }],
                resumoSubprocessos: [{ codSubprocesso: 1 }]
            };
            const frontendCompleto = processoService.mapProcessoDetalheDtoToFrontend(dtoCompleto as any);
            expect(frontendCompleto.unidades.length).toBe(1);
            expect(frontendCompleto.unidades[0].codUnidade).toBe(1);
            expect(frontendCompleto.resumoSubprocessos.length).toBe(1);

            // Incompleto
            const dtoIncompleto = {
                codigo: 2,
            };
            const frontendIncompleto = processoService.mapProcessoDetalheDtoToFrontend(dtoIncompleto as any);
            expect(frontendIncompleto.codigo).toBe(2);
            expect(frontendIncompleto.unidades).toEqual([]);
            expect(frontendIncompleto.resumoSubprocessos).toEqual([]);
        });
    });

    it('buscarUnidadesParaImportacao deve mapear propriedades opcionais com fallback', async () => {
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
                localizacaoAtualCodigo: 1000
            },
            {
                codUnidade: 2,
                // Simulando campos faltando que vão cair no nullish coalescing `??`
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

        expect(result[1].nome).toBe('');
        expect(result[1].sigla).toBe('');
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

    it('obterProcessoPorCodigo deve fazer requisição GET', async () => {
        const codProcesso = 1;
        const responseData = {codigo: 1, descricao: 'Teste'};
        vi.mocked(apiClient.get).mockResolvedValue({data: responseData});

        const result = await processoService.obterProcessoPorCodigo(codProcesso);

        expect(apiClient.get).toHaveBeenCalledWith(`/processos/${codProcesso}`);
        expect(result).toEqual(responseData);
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

    it('obterDetalhesProcesso deve fazer requisição GET', async () => {
        const codProcesso = 1;
        const responseData = {codigo: 1, descricao: 'Detalhes'};
        vi.mocked(apiClient.get).mockResolvedValue({data: responseData});

        const result = await processoService.obterDetalhesProcesso(codProcesso);

        expect(apiClient.get).toHaveBeenCalledWith(`/processos/${codProcesso}/detalhes`);
        expect(result).toEqual(responseData);
    });

    it('processarAcaoEmBloco deve fazer requisição POST', async () => {
        const payload = {
            codProcesso: 1,
            unidades: ['U1'],
            tipoAcao: 'aceitar' as const,
            unidadeUsuario: 'U2'
        };
        await processoService.processarAcaoEmBloco(payload);
        expect(apiClient.post).toHaveBeenCalledWith(`/processos/${payload.codProcesso}/acoes-em-bloco`, payload);
    });

    it('executarAcaoEmBloco deve fazer requisição POST com ação em maiúsculo', async () => {
        await processoService.executarAcaoEmBloco(1, {
            unidadeCodigos: [10],
            acao: 'aceitar',
            dataLimite: '2026-12-31'
        });
        expect(apiClient.post).toHaveBeenCalledWith('/processos/1/acao-em-bloco', {
            unidadeCodigos: [10],
            acao: 'ACEITAR',
            dataLimite: '2026-12-31'
        });
    });

    it('buscarSubprocessosElegiveis deve fazer requisição GET', async () => {
        const codProcesso = 1;
        const responseData = [{codSubprocesso: 1, siglaUnidade: 'U1'}];
        vi.mocked(apiClient.get).mockResolvedValue({data: responseData});

        const result = await processoService.buscarSubprocessosElegiveis(codProcesso);

        expect(apiClient.get).toHaveBeenCalledWith(`/processos/${codProcesso}/subprocessos-elegiveis`);
        expect(result).toEqual(responseData);
    });

    it('alterarDataLimiteSubprocesso deve fazer requisição POST', async () => {
        const codSubprocesso = 1;
        const dados = {novaData: '2024-12-31'};
        await processoService.alterarDataLimiteSubprocesso(codSubprocesso, dados);
        expect(apiClient.post).toHaveBeenCalledWith(`/subprocessos/${codSubprocesso}/data-limite`, {
            data: dados.novaData
        });
    });

    it('apresentarSugestoes deve fazer requisição POST', async () => {
        const codSubprocesso = 1;
        const dados = {sugestoes: 'Texto'};
        await processoService.apresentarSugestoes(codSubprocesso, dados);
        expect(apiClient.post).toHaveBeenCalledWith(`/subprocessos/${codSubprocesso}/apresentar-sugestoes`, {
            texto: dados.sugestoes
        });
    });

    it('validarMapa deve fazer requisição POST', async () => {
        const codSubprocesso = 1;
        await processoService.validarMapa(codSubprocesso);
        expect(apiClient.post).toHaveBeenCalledWith(`/subprocessos/${codSubprocesso}/validar-mapa`);
    });

    it('homologarValidacao deve fazer requisição POST', async () => {
        const codSubprocesso = 1;
        const dados = {texto: 'Obs'};
        await processoService.homologarValidacao(codSubprocesso, dados);
        expect(apiClient.post).toHaveBeenCalledWith(`/subprocessos/${codSubprocesso}/homologar-validacao`, dados);
    });

    it('aceitarValidacao deve fazer requisição POST', async () => {
        const codSubprocesso = 1;
        const dados = {texto: 'Obs'};
        await processoService.aceitarValidacao(codSubprocesso, dados);
        expect(apiClient.post).toHaveBeenCalledWith(`/subprocessos/${codSubprocesso}/aceitar-validacao`, dados);
    });

    it('devolverValidacao deve fazer requisição POST', async () => {
        const codSubprocesso = 1;
        const dados = {justificativa: 'Erro'};
        await processoService.devolverValidacao(codSubprocesso, dados);
        expect(apiClient.post).toHaveBeenCalledWith(`/subprocessos/${codSubprocesso}/devolver-validacao`, dados);
    });

    it('buscarSubprocessos deve fazer requisição GET', async () => {
        const codProcesso = 1;
        const responseData = [{codigo: 1, situacao: 'CRIADO'}];
        vi.mocked(apiClient.get).mockResolvedValue({data: responseData});

        const result = await processoService.buscarSubprocessos(codProcesso);

        expect(apiClient.get).toHaveBeenCalledWith(`/processos/${codProcesso}/subprocessos`);
        expect(result).toEqual(responseData);
    });

    it('buscarContextoCompleto deve fazer requisição GET', async () => {
        const codProcesso = 1;
        const responseData = {codigo: 1, contexto: 'completo'};
        vi.mocked(apiClient.get).mockResolvedValue({data: responseData});

        const result = await processoService.buscarContextoCompleto(codProcesso);

        expect(apiClient.get).toHaveBeenCalledWith(`/processos/${codProcesso}/contexto-completo`);
        expect(result).toEqual(responseData);
    });

    it('reabrirCadastro deve fazer requisição POST', async () => {
        const codSubprocesso = 1;
        const justificativa = 'Erro';
        await processoService.reabrirCadastro(codSubprocesso, justificativa);
        expect(apiClient.post).toHaveBeenCalledWith(`/subprocessos/${codSubprocesso}/reabrir-cadastro`, {justificativa});
    });

    it('reabrirRevisaoCadastro deve fazer requisição POST', async () => {
        const codSubprocesso = 1;
        const justificativa = 'Erro';
        await processoService.reabrirRevisaoCadastro(codSubprocesso, justificativa);
        expect(apiClient.post).toHaveBeenCalledWith(`/subprocessos/${codSubprocesso}/reabrir-revisao-cadastro`, {justificativa});
    });

    it('enviarLembrete deve fazer requisição POST', async () => {
        const codProcesso = 1;
        const unidadeCodigo = 2;
        await processoService.enviarLembrete(codProcesso, unidadeCodigo);
        expect(apiClient.post).toHaveBeenCalledWith(`/processos/${codProcesso}/enviar-lembrete`, {unidadeCodigo});
    });
});
