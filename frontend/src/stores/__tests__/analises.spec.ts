import {beforeEach, describe, expect, it, vi} from 'vitest';
import {createPinia, setActivePinia} from 'pinia';
import {useAnalisesStore} from '../analises';
import * as analiseService from '@/services/analiseService';
import {AnaliseCadastro, AnaliseValidacao} from '@/types/tipos';

vi.mock('@/services/analiseService', () => ({
    listarAnalisesCadastro: vi.fn(),
    listarAnalisesValidacao: vi.fn(),
}));

describe('useAnalisesStore', () => {
    let store: ReturnType<typeof useAnalisesStore>;

    beforeEach(() => {
        setActivePinia(createPinia());
        store = useAnalisesStore();
        vi.clearAllMocks();
    });

    it('should initialize with an empty map for analyses', () => {
        expect(store.analisesPorSubprocesso).toBeInstanceOf(Map);
        expect(store.analisesPorSubprocesso.size).toBe(0);
    });

    describe('getters', () => {
        it('getAnalisesPorSubprocesso should return an empty array if no analyses are present for the subprocess', () => {
            const result = store.getAnalisesPorSubprocesso(123);
            expect(result).toEqual([]);
        });

        it('getAnalisesPorSubprocesso should return the correct analyses for a given subprocess', () => {
            const mockAnalises: (AnaliseCadastro | AnaliseValidacao)[] = [
                {codigo: 1, dataHora: '2023-01-01T12:00:00Z', observacoes: 'Obs 1'},
                {codigo: 2, dataHora: '2023-01-02T12:00:00Z', observacoes: 'Obs 2'},
            ];
            const idSubprocesso = 123;
            store.analisesPorSubprocesso.set(idSubprocesso, mockAnalises);

            const result = store.getAnalisesPorSubprocesso(idSubprocesso);
            expect(result).toEqual(mockAnalises);
        });
    });

    describe('actions', () => {
        const idSubprocesso = 123;
        const mockAnalisesCadastro: AnaliseCadastro[] = [
            {codigo: 1, dataHora: '2023-01-01T10:00:00Z', observacoes: 'Cadastro 1', acao: 'ACEITE', unidadeSigla: 'ABC', analista: 'Analista 1'},
        ];
        const mockAnalisesValidacao: AnaliseValidacao[] = [
            {codigo: 2, dataHora: '2023-01-02T10:00:00Z', observacoes: 'Validacao 1', acao: 'DEVOLUCAO', unidadeSigla: 'DEF', analista: 'Analista 2'},
        ];

        it('fetchAnalisesCadastro should call the service and update the state', async () => {
            vi.mocked(analiseService.listarAnalisesCadastro).mockResolvedValue(mockAnalisesCadastro);

            await store.fetchAnalisesCadastro(idSubprocesso);

            expect(analiseService.listarAnalisesCadastro).toHaveBeenCalledWith(idSubprocesso);
            expect(store.getAnalisesPorSubprocesso(idSubprocesso)).toEqual(mockAnalisesCadastro);
        });

        it('fetchAnalisesValidacao should call the service and update the state', async () => {
            vi.mocked(analiseService.listarAnalisesValidacao).mockResolvedValue(mockAnalisesValidacao);

            await store.fetchAnalisesValidacao(idSubprocesso);

            expect(analiseService.listarAnalisesValidacao).toHaveBeenCalledWith(idSubprocesso);
            expect(store.getAnalisesPorSubprocesso(idSubprocesso)).toEqual(mockAnalisesValidacao);
        });

        it('should merge results when fetching both cadastro and validacao analyses', async () => {
            vi.mocked(analiseService.listarAnalisesCadastro).mockResolvedValue(mockAnalisesCadastro);
            vi.mocked(analiseService.listarAnalisesValidacao).mockResolvedValue(mockAnalisesValidacao);

            // Fetch cadastro first
            await store.fetchAnalisesCadastro(idSubprocesso);
            expect(store.getAnalisesPorSubprocesso(idSubprocesso)).toEqual(mockAnalisesCadastro);

            // Then fetch validacao
            await store.fetchAnalisesValidacao(idSubprocesso);

            const expected = [...mockAnalisesCadastro, ...mockAnalisesValidacao];
            expect(store.getAnalisesPorSubprocesso(idSubprocesso)).toEqual(expect.arrayContaining(expected));
            expect(store.getAnalisesPorSubprocesso(idSubprocesso).length).toBe(2);
        });
    });
});