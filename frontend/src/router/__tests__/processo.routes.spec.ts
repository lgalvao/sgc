import {describe, expect, it, vi} from 'vitest';
import processoRoutes from '@/router/processo.routes';
import {Error} from "storybook/internal/components";

vi.mock('@/views/ProcessoCadastroView.vue', () => ({ default: { name: 'ProcessoCadastroView' } }));
vi.mock('@/views/ProcessoDetalheView.vue', () => ({ default: { name: 'ProcessoDetalheView' } }));
vi.mock('@/views/SubprocessoView.vue', () => ({ default: { name: 'SubprocessoView' } }));
vi.mock('@/views/MapaView.vue', () => ({ default: { name: 'MapaView' } }));
vi.mock('@/views/CadastroView.vue', () => ({ default: { name: 'CadastroView' } }));
describe('processo.routes.ts', () => {
    it('deve definir as rotas corretamente', () => {
        expect(processoRoutes).toHaveLength(5);
        const paths = processoRoutes.map(r => r.path);
        expect(paths).toContain('/processo/cadastro');
        expect(paths).toContain('/processo/:codProcesso');
        expect(paths).toContain('/processo/:codProcesso/:siglaUnidade');
    });

    it('deve tratar corretamente as props da rota para Subprocesso', () => {
        const route = processoRoutes.find(r => r.name === 'Subprocesso');
        expect(route).toBeDefined();
        expect(route?.meta?.keepAlive).toBe(true);

        if (route && typeof route.props === 'function') {
            const props = route.props({
                params: {codProcesso: '123', siglaUnidade: 'TEST'}
            } as any);
            expect(props).toEqual({codProcesso: 123, siglaUnidade: 'TEST'});
        } else {
            throw new Error('Props deve ser uma função');
        }
    });

    it('deve tratar corretamente as props da rota para SubprocessoMapa', () => {
        const route = processoRoutes.find(r => r.name === 'SubprocessoMapa');

        if (route && typeof route.props === 'function') {
            const props = route.props({
                params: {codProcesso: '456', siglaUnidade: 'ABC'}
            } as any);
            expect(props).toEqual({codProcesso: 456, sigla: 'ABC'});
        } else {
            throw new Error('Props deve ser uma função');
        }
    });

    it('deve repassar codSubprocesso pela query quando presente', () => {
        const subprocesso = processoRoutes.find(r => r.name === 'Subprocesso');
        const mapa = processoRoutes.find(r => r.name === 'SubprocessoMapa');
        const cadastro = processoRoutes.find(r => r.name === 'SubprocessoCadastro');

        if (
            !subprocesso || typeof subprocesso.props !== 'function'
            || !mapa || typeof mapa.props !== 'function'
            || !cadastro || typeof cadastro.props !== 'function'
        ) {
            throw new Error('Props deve ser uma função');
        }

        expect(subprocesso.props({
            params: {codProcesso: '123', siglaUnidade: 'TEST'},
            query: {codSubprocesso: '77'},
        } as any)).toEqual({codProcesso: 123, siglaUnidade: 'TEST', codSubprocesso: 77});

        expect(mapa.props({
            params: {codProcesso: '456', siglaUnidade: 'ABC'},
            query: {codSubprocesso: '88'},
        } as any)).toEqual({codProcesso: 456, sigla: 'ABC', codSubprocesso: 88});

        expect(cadastro.props({
            params: {codProcesso: '789', siglaUnidade: 'CAD'},
            query: {codSubprocesso: '99'},
        } as any)).toEqual({codProcesso: 789, sigla: 'CAD', codSubprocesso: 99});
    });

    it('deve manter em cache as views de detalhe de processo e subprocesso', () => {
        const processo = processoRoutes.find(r => r.name === 'Processo');
        const subprocesso = processoRoutes.find(r => r.name === 'Subprocesso');

        expect(processo?.meta?.keepAlive).toBe(true);
        expect(subprocesso?.meta?.keepAlive).toBe(true);
    });

    it('deve tratar corretamente as props da rota para SubprocessoMapa', () => {
        const route = processoRoutes.find(r => r.name === 'SubprocessoMapa');
        if (route && typeof route.props === 'function') {
            const props = route.props({
                params: {codProcesso: '789', siglaUnidade: 'XYZ'}
            } as any);
            expect(props).toEqual({codProcesso: 789, sigla: 'XYZ'});
        } else {
            throw new Error('Props deve ser uma função');
        }
    });

    it('deve tratar corretamente as props da rota para SubprocessoCadastro', () => {
        const route = processoRoutes.find(r => r.name === 'SubprocessoCadastro');
        if (route && typeof route.props === 'function') {
            const props = route.props({
                params: {codProcesso: '101', siglaUnidade: 'CAD'}
            } as any);
            expect(props).toEqual({codProcesso: 101, sigla: 'CAD'});
        } else {
            throw new Error('Props deve ser uma função');
        }
    });

    it('deve tratar corretamente as props da rota para SubprocessoCadastro', () => {
        const route = processoRoutes.find(r => r.name === 'SubprocessoCadastro');
        if (route && typeof route.props === 'function') {
            const props = route.props({
                params: {codProcesso: '102', siglaUnidade: 'VIS'}
            } as any);
            expect(props).toEqual({codProcesso: 102, sigla: 'VIS'});
        } else {
            throw new Error('Props deve ser uma função');
        }
    });

    it('deve carregar componentes sob demanda (lazy load)', async () => {
        const cadProcesso = processoRoutes.find(r => r.name === 'CadProcesso');
        // @ts-expect-error - testing internal property
        const component = await cadProcesso?.component();
        expect(component.default).toBeDefined();
    }, 30000);
});
