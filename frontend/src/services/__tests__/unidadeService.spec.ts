import {beforeEach, describe, expect, it, vi} from 'vitest';
import {
    buscarArvoreComElegibilidade,
    buscarArvoreUnidade,
    buscarCodigosUnidadesComMapaVigente,
    buscarCodigosUnidadesSemHistoricoMapa,
    buscarDiagnosticoOrganizacional,
    buscarReferenciaMapaVigente,
    buscarTodasUnidades,
    buscarUnidadePorCodigo
} from '../unidadeService';
import * as apiUtils from '@/utils/apiUtils';

vi.mock('@/utils/apiUtils', () => ({
    apiGet: vi.fn(),
}));

describe('unidadeService', () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    describe('API calls', () => {
        it('deve chamar buscarTodasUnidades corretamente', async () => {
            vi.mocked(apiUtils.apiGet).mockResolvedValueOnce([]);
            await buscarTodasUnidades();
            expect(apiUtils.apiGet).toHaveBeenCalledWith('/unidades');
        });

        it('deve chamar buscarUnidadePorCodigo corretamente', async () => {
            vi.mocked(apiUtils.apiGet).mockResolvedValueOnce({subunidades: []});
            await buscarUnidadePorCodigo(123);
            expect(apiUtils.apiGet).toHaveBeenCalledWith('/unidades/123');
        });

        it('deve chamar buscarArvoreComElegibilidade com processo', async () => {
            vi.mocked(apiUtils.apiGet).mockResolvedValueOnce([]);
            await buscarArvoreComElegibilidade('MAPEAMENTO', 10);
            expect(apiUtils.apiGet).toHaveBeenCalledWith('/unidades/arvore-com-elegibilidade?tipoProcesso=MAPEAMENTO&codProcesso=10');
        });

        it('deve chamar buscarArvoreComElegibilidade sem processo', async () => {
            vi.mocked(apiUtils.apiGet).mockResolvedValueOnce([]);
            await buscarArvoreComElegibilidade('MAPEAMENTO');
            expect(apiUtils.apiGet).toHaveBeenCalledWith('/unidades/arvore-com-elegibilidade?tipoProcesso=MAPEAMENTO');
        });

        it('deve chamar buscarArvoreUnidade corretamente', async () => {
            vi.mocked(apiUtils.apiGet).mockResolvedValueOnce({subunidades: []});
            await buscarArvoreUnidade(123);
            expect(apiUtils.apiGet).toHaveBeenCalledWith('/unidades/123/arvore');
        });

        it('deve chamar buscarReferenciaMapaVigente corretamente', async () => {
            await buscarReferenciaMapaVigente(123);
            expect(apiUtils.apiGet).toHaveBeenCalledWith('/unidades/123/mapa-vigente/referencia');
        });

        it('deve chamar buscarDiagnosticoOrganizacional corretamente', async () => {
            vi.mocked(apiUtils.apiGet).mockResolvedValueOnce({possuiViolacoes: false});
            await buscarDiagnosticoOrganizacional();
            expect(apiUtils.apiGet).toHaveBeenCalledWith('/unidades/diagnostico-organizacional');
        });

        it('deve chamar buscarCodigosUnidadesComMapaVigente corretamente', async () => {
            vi.mocked(apiUtils.apiGet).mockResolvedValueOnce([1, 2]);
            await buscarCodigosUnidadesComMapaVigente();
            expect(apiUtils.apiGet).toHaveBeenCalledWith('/unidades/com-mapa-vigente');
        });

        it('deve chamar buscarCodigosUnidadesSemHistoricoMapa corretamente', async () => {
            vi.mocked(apiUtils.apiGet).mockResolvedValueOnce([3, 4]);
            await buscarCodigosUnidadesSemHistoricoMapa();
            expect(apiUtils.apiGet).toHaveBeenCalledWith('/unidades/sem-historico-mapa');
        });
    });

    describe('mapeamento interno', () => {
        it('deve mapear a árvore de unidades ao buscar todas', async () => {
            vi.mocked(apiUtils.apiGet).mockResolvedValueOnce([
                {
                    codigo: 1,
                    nome: 'Teste',
                    sigla: 'TST',
                    subunidades: [{codigo: 2, nome: 'Filha', sigla: 'FIL', subunidades: []}]
                }
            ]);

            const result = await buscarTodasUnidades();

            expect(result).toEqual([
                {
                    codigo: 1,
                    nome: 'Teste',
                    sigla: 'TST',
                    tipo: undefined,
                    isElegivel: undefined,
                    tituloTitular: undefined,
                    tipoResponsabilidade: undefined,
                    titular: null,
                    responsavel: null,
                    dataInicioResponsabilidade: null,
                    dataFimResponsabilidade: null,
                    filhas: [
                        {
                            codigo: 2,
                            nome: 'Filha',
                            sigla: 'FIL',
                            tipo: undefined,
                            isElegivel: undefined,
                            tituloTitular: undefined,
                            tipoResponsabilidade: undefined,
                            titular: null,
                            responsavel: null,
                            dataInicioResponsabilidade: null,
                            dataFimResponsabilidade: null,
                            filhas: []
                        }
                    ]
                }
            ]);
        });

        it('deve mapear unidade com responsavel e titular ao buscar por codigo', async () => {
            vi.mocked(apiUtils.apiGet).mockResolvedValueOnce({
                codigo: 1,
                nome: 'Teste',
                sigla: 'TST',
                tipo: 'Setor',
                isElegivel: true,
                responsavel: {
                    nome: 'Resp',
                    tituloEleitoral: '123',
                    matricula: 'M1',
                    email: 'r@r',
                    ramal: '1'
                },
                titular: {
                    nome: 'Tit',
                    tituloEleitoral: '456',
                    matricula: 'M2',
                    email: 't@t',
                    ramal: '2'
                },
                subunidades: []
            });

            const result = await buscarUnidadePorCodigo(1);

            expect(result.codigo).toBe(1);
            expect(result.sigla).toBe('TST');
            expect(result.tipo).toBe('Setor');
            expect(result.nome).toBe('Teste');
            expect(result.isElegivel).toBe(true);
            expect(result.responsavel?.nome).toBe('Resp');
            expect(result.titular?.nome).toBe('Tit');
            expect(result.filhas).toEqual([]);
        });
    });
});
