import {beforeEach, describe, expect, it, vi} from 'vitest';
import {createApp} from 'vue';
import {createPinia} from 'pinia';
import {PiniaColada} from '@pinia/colada';
import * as contextoService from '@/services/subprocessoServiceContexto';
import {useDiagnosticoPermissoes} from '../useDiagnosticoPermissoes';
import {STALE_TIME_CONTROLADO_POR_INVALIDACAO} from '../cachePolicy';
import {ref} from 'vue';

const {useQueryMock} = vi.hoisted(() => ({
    useQueryMock: vi.fn(),
}));

vi.mock('@pinia/colada', async () => {
    const atual = await vi.importActual<typeof import('@pinia/colada')>('@pinia/colada');
    return {
        ...atual,
        useQuery: useQueryMock,
    };
});

vi.mock('@/services/subprocessoServiceContexto', () => ({
    buscarPermissoesSubprocesso: vi.fn(),
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

describe('useDiagnosticoPermissoes', () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    it('deve consultar permissões e expor acesso consolidado', async () => {
        const data = ref<any>(null);
        useQueryMock.mockImplementation((options) => ({
            data,
            refetch: async () => {
                const resultado = await options.query();
                data.value = resultado;
                return {data: resultado};
            },
        }));

        vi.mocked(contextoService.buscarPermissoesSubprocesso).mockResolvedValue({
            podeCriarConsenso: true,
            habilitarConcluirDiagnostico: false,
            habilitarValidarDiagnostico: true,
        } as any);

        const [composable, app] = withSetup(() => useDiagnosticoPermissoes(77));
        const res = await composable.queryPermissoes.refetch();

        expect(contextoService.buscarPermissoesSubprocesso).toHaveBeenCalledWith(77);
        expect(res.data?.podeCriarConsenso).toBe(true);
        expect(composable.podeCriarConsenso.value).toBe(true);
        expect(composable.habilitarValidarDiagnostico.value).toBe(true);
        expect(useQueryMock).toHaveBeenCalledWith(expect.objectContaining({
            staleTime: STALE_TIME_CONTROLADO_POR_INVALIDACAO,
        }));
        app.unmount();
    });

    it('usa permissões já conhecidas sem depender de nova consulta', async () => {
        useQueryMock.mockImplementation(() => ({
            data: {value: null},
            refetch: vi.fn(),
        }));

        const permissoesConhecidas = {
            podeCriarConsenso: true,
            podeConcluirDiagnostico: true,
            habilitarConcluirDiagnostico: false,
            habilitarValidarDiagnostico: true,
        } as any;

        const [composable, app] = withSetup(() => useDiagnosticoPermissoes(77, permissoesConhecidas));
        const opcoesQuery = useQueryMock.mock.calls[0][0];

        expect(opcoesQuery.enabled()).toBe(false);
        expect(composable.podeCriarConsenso.value).toBe(true);
        expect(composable.podeConcluirDiagnostico.value).toBe(true);
        expect(composable.habilitarValidarDiagnostico.value).toBe(true);
        expect(contextoService.buscarPermissoesSubprocesso).not.toHaveBeenCalled();
        app.unmount();
    });
});
