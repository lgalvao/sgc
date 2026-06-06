import {beforeEach, describe, expect, it, vi} from 'vitest';
import {createApp} from 'vue';
import {createPinia} from 'pinia';
import {PiniaColada} from '@pinia/colada';
import * as diagnosticoService from '@/services/diagnosticoService';
import {
    chaveAutoavaliacao,
    chaveConsenso,
    chaveContexto,
    chaveEquipe,
    chaveUnidade,
    criarContextoSessaoDiagnostico,
    useDiagnosticoContexto,
    useInvalidacaoDiagnosticoContexto,
} from '../useDiagnosticoContexto';

const invalidateQueriesMock = vi.fn();

vi.mock('@pinia/colada', async () => {
    const atual = await vi.importActual<typeof import('@pinia/colada')>('@pinia/colada');
    return {
        ...atual,
        useQueryCache: () => ({
            invalidateQueries: invalidateQueriesMock,
        }),
    };
});

vi.mock('@/stores/perfil', () => ({
    usePerfilStore: () => ({
        usuarioCodigo: '151515',
        perfilSelecionado: 'CHEFE',
        unidadeSelecionada: 12,
    }),
}));

vi.mock('@/services/diagnosticoService', () => ({
    obterContextoDiagnostico: vi.fn(),
}));

function withSetup<T>(composable: () => T) {
    let result: T;
    const app = createApp({
        setup() {
            result = composable();
            return () => null;
        },
    });
    const pinia = createPinia();
    app.use(pinia);
    app.use(PiniaColada);
    app.mount(document.createElement('div'));
    return [result!, app] as const;
}

describe('useDiagnosticoContexto', () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    it('deve montar contexto de sessão e chaves estáveis', () => {
        const perfilStore = {
            usuarioCodigo: '151515',
            perfilSelecionado: 'CHEFE',
            unidadeSelecionada: 12,
        } as any;

        const contexto = criarContextoSessaoDiagnostico(perfilStore);
        expect(contexto).toEqual(['151515', 'CHEFE', '12']);
        expect(chaveContexto(10, contexto)).toEqual(['diagnostico-competencias', 'contexto', '151515', 'CHEFE', '12', 10]);
        expect(chaveAutoavaliacao(10, contexto)).toEqual(['diagnostico-competencias', 'autoavaliacao', '151515', 'CHEFE', '12', 10]);
        expect(chaveEquipe(10, contexto)).toEqual(['diagnostico-competencias', 'equipe', '151515', 'CHEFE', '12', 10]);
        expect(chaveUnidade(10, contexto)).toEqual(['diagnostico-competencias', 'unidade', '151515', 'CHEFE', '12', 10]);
        expect(chaveConsenso(10, contexto)).toEqual(['diagnostico-competencias', 'consenso', '151515', 'CHEFE', '12', 10, 'usuario-logado']);
        expect(chaveConsenso(10, contexto, '242426')).toEqual(['diagnostico-competencias', 'consenso', '151515', 'CHEFE', '12', 10, '242426']);
    });

    it('deve consultar o contexto pelo serviço', async () => {
        vi.mocked(diagnosticoService.obterContextoDiagnostico).mockResolvedValue({
            unidadeSigla: 'ASSESSORIA_12',
        } as any);

        const [query, app] = withSetup(() => useDiagnosticoContexto(77));
        const res = await query.refetch();

        expect(res.data?.unidadeSigla).toBe('ASSESSORIA_12');
        expect(diagnosticoService.obterContextoDiagnostico).toHaveBeenCalledWith(77);
        app.unmount();
    });

    it('deve invalidar a query de contexto pelo helper dedicado', () => {
        const [invalidacao, app] = withSetup(() => useInvalidacaoDiagnosticoContexto());

        invalidacao.invalidarContexto(88);

        expect(invalidateQueriesMock).toHaveBeenCalledWith({
            key: ['diagnostico-competencias', 'contexto', '151515', 'CHEFE', '12', 88],
            exact: true,
        });
        app.unmount();
    });
});
