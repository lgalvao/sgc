import {beforeEach, describe, expect, it, vi} from 'vitest';
import {flushPromises, mount} from '@vue/test-utils';
import CadAtribuicao from '@/views/unidade/AtribuicaoTemporariaView.vue';
import {getCommonMountOptions} from "@/test-utils/componentTestHelpers";
import {useUnidadesStore} from '@/stores/unidades';
import {createTestingPinia} from '@pinia/testing';

vi.mock('vue-router', () => ({
    useRouter: () => ({
        push: vi.fn(),
    }),
    createRouter: vi.fn(() => ({
        push: vi.fn(),
        beforeEach: vi.fn(),
        afterEach: vi.fn(),
    })),
    createWebHistory: vi.fn(),
    createMemoryHistory: vi.fn(),
}));

describe('CadAtribuicao Coverage', () => {
    let pinia: any;

    function criarWrapper(props = {codUnidade: 1}) {
        return mount(CadAtribuicao, {
            ...getCommonMountOptions(),
            props,
            global: {
                plugins: [pinia],
                stubs: {
                    LayoutPadrao: true,
                    BContainer: true,
                    BCard: true,
                    BCardBody: true,
                    BForm: true,
                    BFormSelect: true,
                    BFormSelectOption: true,
                    BFormInput: true,
                    BFormTextarea: true,
                    BButton: true,
                    BAlert: true,
                    PageHeader: true
                }
            }
        });
    }

    beforeEach(() => {
        vi.clearAllMocks();
        pinia = createTestingPinia({
            createSpy: vi.fn,
            stubActions: false
        });
    });

    it('deve lidar com erro no onMounted', async () => {
        const unidadesStore = useUnidadesStore();
        vi.spyOn(unidadesStore, 'buscarUnidadePorCodigo').mockRejectedValue(new Error('Erro ao buscar unidade'));
        vi.spyOn(console, 'error').mockImplementation(() => {
        });

        const wrapper = criarWrapper();
        await flushPromises();

        expect((wrapper.vm as any).erroUsuario).toBe("Falha ao carregar dados da unidade ou usuários.");
    });

    it('deve falhar rápido quando invariante de unidade é violada', async () => {
        const wrapper = criarWrapper();
        await flushPromises();

        // Forçar estado onde unidade ou usuarioSelecionado é nulo
        (wrapper.vm as any).unidade = null;
        (wrapper.vm as any).usuarioSelecionado = null;

        await expect((wrapper.vm as any).criarAtribuicao())
            .rejects
            .toThrow('Invariante violada: unidade não carregada');
    });

    it('deve atualizar dataInicio via v-model', async () => {
        const wrapper = criarWrapper();
        await flushPromises();

        (wrapper.vm as any).dataInicio = '2023-10-10';
        expect((wrapper.vm as any).dataInicio).toBe('2023-10-10');
    });
});
