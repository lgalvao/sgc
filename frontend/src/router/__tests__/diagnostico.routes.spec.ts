import {describe, expect, it, vi} from 'vitest';
import diagnosticoRoutes from '../diagnostico.routes';

vi.mock('@/views/AutoavaliacaoDiagnosticoView.vue', () => ({default: {}}));
vi.mock('@/views/SituacaoCapacitacaoDiagnosticoView.vue', () => ({default: {}}));
vi.mock('@/views/ConsensoDiagnosticoView.vue', () => ({default: {}}));
vi.mock('@/views/DiagnosticoUnidadeView.vue', () => ({default: {}}));

describe('diagnostico.routes', () => {
    it('deve expor as rotas do fluxo de diagnóstico com props normalizadas', async () => {
        expect(diagnosticoRoutes).toHaveLength(4);

        const autoavaliacao = diagnosticoRoutes[0];
        expect(autoavaliacao.name).toBe('AutoavaliacaoDiagnostico');
        expect((autoavaliacao.props as any)({
            params: {codSubprocesso: '41', siglaUnidade: 'ASSESSORIA_12'},
        } as any)).toEqual({
            codSubprocesso: 41,
            siglaUnidade: 'ASSESSORIA_12',
        });
        await expect((autoavaliacao.component as any)()).resolves.toBeDefined();

        const situacoesCapacitacao = diagnosticoRoutes[1];
        expect(situacoesCapacitacao.name).toBe('SituacaoCapacitacaoDiagnostico');
        expect((situacoesCapacitacao.props as any)({
            params: {codSubprocesso: '42', siglaUnidade: 'ASSESSORIA_13'},
        } as any)).toEqual({
            codSubprocesso: 42,
            siglaUnidade: 'ASSESSORIA_13',
        });
        await expect((situacoesCapacitacao.component as any)()).resolves.toBeDefined();

        const consenso = diagnosticoRoutes[2];
        expect(consenso.name).toBe('ConsensoDiagnostico');
        expect((consenso.props as any)({
            params: {codSubprocesso: '44', siglaUnidade: 'ASSESSORIA_15', servidorTitulo: '242426'},
        } as any)).toEqual({
            codSubprocesso: 44,
            siglaUnidade: 'ASSESSORIA_15',
            servidorTitulo: '242426',
        });
        await expect((consenso.component as any)()).resolves.toBeDefined();

        const unidade = diagnosticoRoutes[3];
        expect(unidade.name).toBe('DiagnosticoUnidade');
        expect((unidade.props as any)({
            params: {codSubprocesso: '45', siglaUnidade: 'ASSESSORIA_16'},
        } as any)).toEqual({
            codSubprocesso: 45,
            siglaUnidade: 'ASSESSORIA_16',
        });
        await expect((unidade.component as any)()).resolves.toBeDefined();
    });
});
