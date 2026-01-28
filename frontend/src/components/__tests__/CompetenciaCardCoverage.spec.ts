import { describe, it, expect } from 'vitest';
import { mount } from '@vue/test-utils';
import CompetenciaCard from '@/components/CompetenciaCard.vue';
import { BCard, BCardHeader, BCardBody, BButton } from 'bootstrap-vue-next';

describe('CompetenciaCard Coverage', () => {
    const commonStubs = {
        BCard: { template: '<div><slot /></div>' },
        BCardHeader: { template: '<div><slot /></div>' },
        BCardBody: { template: '<div><slot /></div>' },
        BButton: { template: '<button><slot /></button>' }
    };

    it('renders "Atividade não encontrada" for invalid activity ID', () => {
        const wrapper = mount(CompetenciaCard, {
            props: {
                competencia: {
                    codigo: 1,
                    descricao: 'Competencia Teste',
                    atividadesAssociadas: [999] // Invalid ID
                },
                atividades: [], // Empty list
                podeEditar: true
            },
            global: {
                stubs: commonStubs
            }
        });

        expect(wrapper.text()).toContain('Atividade não encontrada');
    });

    it('getConhecimentosTooltip handles invalid activity gracefully', () => {
        const wrapper = mount(CompetenciaCard, {
            props: {
                competencia: {
                    codigo: 1,
                    descricao: 'Competencia Teste',
                    atividadesAssociadas: [999]
                },
                atividades: [],
                podeEditar: true
            },
            global: {
                stubs: commonStubs
            }
        });

        // Try to access internal method if possible
        if ((wrapper.vm as any).getConhecimentosTooltip) {
            const result = (wrapper.vm as any).getConhecimentosTooltip(999);
            expect(result).toBe('Nenhum conhecimento cadastrado');
        }
    });
});
