import {beforeEach, describe, expect, it, vi} from 'vitest';
import {flushPromises, mount} from '@vue/test-utils';
import OcupacoesCriticasDiagnostico from '@/views/OcupacoesCriticasDiagnostico.vue';
import {diagnosticoService} from '@/services/diagnosticoService';
import {getCommonMountOptions, setupComponentTest} from "@/test-utils/componentTestHelpers";

// Mocks
const { mockRouteParams } = vi.hoisted(() => {
    return { mockRouteParams: { value: { codSubprocesso: '10' } } };
});

vi.mock('vue-router', async (importOriginal) => {
    const actual: any = await importOriginal();
    return {
        ...actual,
        useRoute: () => ({
            params: mockRouteParams.value,
        }),
    };
});

vi.mock('@/services/diagnosticoService', () => ({
    diagnosticoService: {
        buscarDiagnostico: vi.fn(),
        salvarOcupacao: vi.fn(),
    },
}));

describe('OcupacoesCriticasDiagnostico.vue', () => {
    const context = setupComponentTest();

    const mockAvaliacaoGap = {
        competenciaCodigo: 1,
        competenciaDescricao: 'Comp 1',
        importanciaLabel: 'Alto',
        dominioLabel: 'Baixo',
        gap: 2,
    };

    const mockAvaliacaoNoGap = {
        competenciaCodigo: 2,
        competenciaDescricao: 'Comp 2',
        importanciaLabel: 'Alto',
        dominioLabel: 'Alto',
        gap: 0,
    };

    const mockServidor = {
        nome: 'Servidor 1',
        tituloEleitoral: '111',
        avaliacoes: [mockAvaliacaoGap, mockAvaliacaoNoGap],
        ocupacoes: []
    };

    const mockServidorSemGaps = {
        nome: 'Servidor 2',
        tituloEleitoral: '222',
        avaliacoes: [mockAvaliacaoNoGap],
        ocupacoes: []
    };

    const mockDiagnostico = {
        situacao: 'EM_ANDAMENTO',
        servidores: [mockServidor, mockServidorSemGaps]
    };

    const createWrapper = (initialState = {}, stubs = {}) => {
        return mount(OcupacoesCriticasDiagnostico, {
            ...getCommonMountOptions(
                {
                    unidades: { unidade: { sigla: 'TEST', nome: 'Unidade Teste' } },
                    ...initialState
                },
                {
                    BContainer: { template: '<div><slot /></div>' },
                    BAlert: { template: '<div><slot /></div>' },
                    BButton: { template: '<button><slot /></button>' },
                    BSpinner: { template: '<div data-testid="spinner"></div>' },
                    BFormSelect: {
                        template: '<select :value="modelValue" @change="$emit(\'update:modelValue\', $event.target.value); $emit(\'change\', $event.target.value)"><option value="AC">A capacitar</option></select>',
                        props: ['modelValue', 'options']
                    },
                    ...stubs
                }
            )
        });
    };

    beforeEach(() => {
        vi.clearAllMocks();
        mockRouteParams.value = { codSubprocesso: '10' };
        (diagnosticoService.buscarDiagnostico as any).mockResolvedValue(mockDiagnostico);
    });

    it('renders loading state initially', async () => {
        context.wrapper = createWrapper();
        expect(diagnosticoService.buscarDiagnostico).toHaveBeenCalledWith(10);
    });

    it('filters and displays only servers/competencies with Gap >= 2', async () => {
        context.wrapper = createWrapper();
        await flushPromises();

        // Should show Servidor 1
        expect(context.wrapper.text()).toContain('Servidor 1');
        expect(context.wrapper.text()).toContain('Comp 1');

        // Should NOT show Servidor 2 (no gaps)
        expect(context.wrapper.text()).not.toContain('Servidor 2');
        // Should NOT show Comp 2 (gap 0) for Servidor 1
        expect(context.wrapper.text()).not.toContain('Comp 2');
    });

    it('saves occupation status on change', async () => {
        context.wrapper = createWrapper();
        await flushPromises();

        const select = context.wrapper.find('select');

        // Simulate selection
        await select.setValue('AC'); // 'A capacitar'

        expect(diagnosticoService.salvarOcupacao).toHaveBeenCalledWith(10, '111', 1, 'AC');
    });

    it('handles empty state', async () => {
        const mockDiagnosticoVazio = { ...mockDiagnostico, servidores: [mockServidorSemGaps] };
        (diagnosticoService.buscarDiagnostico as any).mockResolvedValue(mockDiagnosticoVazio);

        context.wrapper = createWrapper();
        await flushPromises();

        expect(context.wrapper.text()).toContain('Nenhuma ocupação crítica identificada');
    });
});
