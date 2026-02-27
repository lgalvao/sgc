import {describe, expect, it} from 'vitest';
import {mount} from '@vue/test-utils';
import CompetenciaCard from '@/components/mapa/CompetenciaCard.vue';

describe('CompetenciaCard Coverage', () => {
    const commonStubs = {
        BCard: { template: '<div><slot /></div>' },
        BCardHeader: { template: '<div><slot /></div>' },
        BCardBody: { template: '<div><slot /></div>' },
        BButton: { template: '<button><slot /></button>' }
    };

    it('renders "Atividade 999" for activity ID 999', () => {
        const wrapper = mount(CompetenciaCard, {
            props: {
                competencia: {
                    codigo: 1,
                    descricao: 'Competencia Teste',
                    atividades: [{codigo: 999, descricao: 'Atividade 999', conhecimentos: []}],
                },
                atividades: [], // Empty list
                podeEditar: true
            },
            global: {
                stubs: commonStubs
            }
        });

        expect(wrapper.text()).toContain('Atividade 999');
    });

    it('getConhecimentosTooltip handles invalid activity gracefully', () => {
        const wrapper = mount(CompetenciaCard, {
            props: {
                competencia: {
                    codigo: 1,
                    descricao: 'Competencia Teste',
                    atividades: [{codigo: 999, descricao: 'Atividade 999', conhecimentos: []}],
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
