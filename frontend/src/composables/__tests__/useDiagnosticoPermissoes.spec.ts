import {describe, expect, it, vi} from 'vitest';
import {createApp} from 'vue';
import {createPinia} from 'pinia';
import {PiniaColada} from '@pinia/colada';
import * as contextoService from '@/services/subprocessoServiceContexto';
import {useDiagnosticoPermissoes} from '../useDiagnosticoPermissoes';

vi.mock('@/services/subprocessoServiceContexto', () => ({
    buscarContextoEdicao: vi.fn(),
}));

vi.mock('@/composables/acesso', () => ({
    useAcesso: () => ({
        podeCriarConsenso: true,
        habilitarConcluirDiagnostico: false,
        habilitarValidarDiagnostico: true,
    }),
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
    it('deve consultar contexto de edição e expor acesso consolidado', async () => {
        vi.mocked(contextoService.buscarContextoEdicao).mockResolvedValue({
            detalhes: {codigo: 77},
        } as any);

        const [composable, app] = withSetup(() => useDiagnosticoPermissoes(77));
        const res = await composable.queryContextoEdicao.refetch();

        expect(contextoService.buscarContextoEdicao).toHaveBeenCalledWith(77);
        expect(res.data?.detalhes.codigo).toBe(77);
        expect(composable.subprocesso.value).toEqual({codigo: 77});
        expect(composable.podeCriarConsenso).toBe(true);
        expect(composable.habilitarValidarDiagnostico).toBe(true);
        app.unmount();
    });
});
