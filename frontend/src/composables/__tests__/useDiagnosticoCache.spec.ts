import {beforeEach, describe, expect, it, vi} from 'vitest';

const invalidateQueriesMock = vi.fn();

vi.mock('@pinia/colada', () => ({
    useQueryCache: () => ({
        invalidateQueries: invalidateQueriesMock,
    }),
}));

vi.mock('@/stores/perfil', () => ({
    usePerfilStore: () => ({
        usuarioCodigo: '151515',
        perfilSelecionado: 'CHEFE',
        unidadeSelecionada: 12,
    }),
}));

describe('useDiagnosticoCache', () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    it('deve invalidar contexto, autoavaliação, equipe, unidade e consenso com chave exata e contexto de sessão', async () => {
        const {useCacheDiagnostico} = await import('../useDiagnosticoCache');
        const cache = useCacheDiagnostico();

        cache.invalidarContexto(10);
        cache.invalidarAutoavaliacao(10);
        cache.invalidarEquipe(10);
        cache.invalidarUnidade(10);
        cache.invalidarConsenso(10, '242426');

        expect(invalidateQueriesMock).toHaveBeenCalledWith({
            key: ['diagnostico-competencias', 'contexto', '151515', 'CHEFE', '12', 10],
            exact: true,
        });
        expect(invalidateQueriesMock).toHaveBeenCalledWith({
            key: ['diagnostico-competencias', 'autoavaliacao', '151515', 'CHEFE', '12', 10],
            exact: true,
        });
        expect(invalidateQueriesMock).toHaveBeenCalledWith({
            key: ['diagnostico-competencias', 'equipe', '151515', 'CHEFE', '12', 10],
            exact: true,
        });
        expect(invalidateQueriesMock).toHaveBeenCalledWith({
            key: ['diagnostico-competencias', 'unidade', '151515', 'CHEFE', '12', 10],
            exact: true,
        });
        expect(invalidateQueriesMock).toHaveBeenCalledWith({
            key: ['diagnostico-competencias', 'consenso', '151515', 'CHEFE', '12', 10, '242426'],
            exact: true,
        });
    });

    it('deve invalidar o fluxo completo do diagnóstico', async () => {
        const {useCacheDiagnostico} = await import('../useDiagnosticoCache');
        const cache = useCacheDiagnostico();

        cache.invalidarFluxoCompleto(20);

        expect(invalidateQueriesMock).toHaveBeenCalledTimes(3);
        expect(invalidateQueriesMock).toHaveBeenNthCalledWith(1, {
            key: ['diagnostico-competencias', 'contexto', '151515', 'CHEFE', '12', 20],
            exact: true,
        });
        expect(invalidateQueriesMock).toHaveBeenNthCalledWith(2, {
            key: ['diagnostico-competencias', 'equipe', '151515', 'CHEFE', '12', 20],
            exact: true,
        });
        expect(invalidateQueriesMock).toHaveBeenNthCalledWith(3, {
            key: ['diagnostico-competencias', 'unidade', '151515', 'CHEFE', '12', 20],
            exact: true,
        });
    });
});
