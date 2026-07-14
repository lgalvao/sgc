import {describe, expect, it} from 'vitest';
import {mount} from '@vue/test-utils';
import ConsensoDiagnosticoAcoes from '../ConsensoDiagnosticoAcoes.vue';

function montar(props?: Record<string, unknown>) {
    return mount(ConsensoDiagnosticoAcoes, {
        props: {
            aprovando: false,
            concluindoAvaliacao: false,
            habilitarAprovarConsenso: true,
            habilitarConcluirAvaliacao: true,
            podeAprovarConsenso: true,
            podeConcluirAvaliacao: true,
            ...props,
        },
        global: {
            stubs: {
                BButton: {
                    emits: ['click'],
                    template: '<button :data-testid="$attrs[\'data-testid\']" :disabled="$attrs.disabled" @click="$emit(\'click\')"><slot /></button>',
                },
                BSpinner: {template: '<span class="spinner" />'},
            },
        },
    });
}

describe('ConsensoDiagnosticoAcoes', () => {
    it('emite eventos do cabeçalho', async () => {
        const wrapper = montar();

        await wrapper.get('[data-testid="btn-concluir-avaliacao"]').trigger('click');
        await wrapper.get('[data-testid="btn-aprovar-consenso"]').trigger('click');
        await wrapper.findAll('button')[2].trigger('click');

        expect(wrapper.emitted('concluirAvaliacao')).toHaveLength(1);
        expect(wrapper.emitted('aprovarConsenso')).toHaveLength(1);
        expect(wrapper.emitted('voltar')).toHaveLength(1);
    });

    it('oculta o botao aprovar sem permissao, inclusive para o servidor logado', () => {
        const wrapper = montar({
            podeAprovarConsenso: false,
        });

        expect(wrapper.find('[data-testid="btn-aprovar-consenso"]').exists()).toBe(false);
    });
});
