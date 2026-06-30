import {describe, expect, it, vi} from 'vitest';
import {createApp} from 'vue';
import {createPinia} from 'pinia';
import {PiniaColada} from '@pinia/colada';
import * as contextoService from '@/services/subprocessoServiceContexto';
import {useDiagnosticoPermissoes} from '../useDiagnosticoPermissoes';

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
    it('deve consultar permissões e expor acesso consolidado', async () => {
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
        app.unmount();
    });
});
