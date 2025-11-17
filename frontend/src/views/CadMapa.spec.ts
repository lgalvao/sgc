import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { createTestingPinia } from '@pinia/testing'
import CadMapa from './CadMapa.vue'
import { useMapasStore } from '@/stores/mapas'
import { useRoute } from 'vue-router'
import { BFormTextarea, BFormCheckbox, BFormInput } from 'bootstrap-vue-next';

vi.mock('vue-router', () => ({
    useRoute: vi.fn(),
    useRouter: vi.fn(() => ({
        push: vi.fn(),
    })),
    createRouter: vi.fn(() => ({
        beforeEach: vi.fn(),
        afterEach: vi.fn(),
    })),
    createWebHistory: vi.fn(),
}));

import { useProcessosStore } from '@/stores/processos'

describe('CadMapa.vue', () => {
    beforeEach(() => {
        vi.mocked(useRoute).mockReturnValue({
            params: {
                codProcesso: '1',
                siglaUnidade: 'TEST',
            },
        } as any)
    })

    const globalComponents = {
        global: {
            plugins: [createTestingPinia({ createSpy: vi.fn })],
            components: {
                BFormTextarea,
                BFormCheckbox,
                BFormInput,
            },
            stubs: {
                'b-form-select-option': true,
                'ImpactoMapaModal': true,
            }
        },
    };

    it('renders the component', () => {
        const wrapper = mount(CadMapa, globalComponents)
        expect(wrapper.exists()).toBe(true)
    })

    it('opens the create competency modal when the button is clicked', async () => {
        const wrapper = mount(CadMapa, globalComponents)

        const processosStore = useProcessosStore()
        processosStore.processoDetalhe = { unidades: [{ sigla: 'TEST', codUnidade: 1 }] } as any

        await wrapper.vm.$nextTick()

        await wrapper.find('[data-testid="btn-abrir-criar-competencia"]').trigger('click')

        expect(wrapper.find('[data-testid="criar-competencia-modal"]').exists()).toBe(true)
    })

    it('calls the disponibilizarMapa action when the disponibilizar button is clicked', async () => {
        const wrapper = mount(CadMapa, {
            global: {
                plugins: [createTestingPinia({
                    createSpy: vi.fn,
                    initialState: {
                        mapas: {
                            mapaCompleto: {
                                competencias: [{
                                    codigo: 1,
                                    descricao: 'Competency 1',
                                    atividadesAssociadas: [1]
                                }]
                            }
                        }
                    }
                })],
                components: {
                    BFormTextarea,
                    BFormCheckbox,
                    BFormInput,
                },
                stubs: {
                    'ImpactoMapaModal': true,
                }
            },
        })

        const processosStore = useProcessosStore()
        processosStore.processoDetalhe = { unidades: [{ sigla: 'TEST', codUnidade: 1 }] } as any

        const mapasStore = useMapasStore()

        await wrapper.vm.$nextTick()

        await wrapper.find('[data-testid="btn-disponibilizar-page"]').trigger('click')
        await wrapper.find('[data-testid="input-data-limite"]').setValue('2025-12-31')
        await wrapper.find('[data-testid="btn-disponibilizar"]').trigger('click')

        expect(mapasStore.disponibilizarMapa).toHaveBeenCalled()
    })
})
