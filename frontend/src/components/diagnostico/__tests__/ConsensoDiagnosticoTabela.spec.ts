import {describe, expect, it} from 'vitest';
import {mount} from '@vue/test-utils';
import ConsensoDiagnosticoTabela from '../ConsensoDiagnosticoTabela.vue';

const competencias = [
    {
        competenciaCodigo: 10,
        descricao: 'Competência A',
        competenciaDescricao: 'Competência A',
        servidorImportancia: 3,
        servidorDominio: 4,
        chefiaImportancia: 3,
        chefiaDominio: 4,
        consensoImportancia: 3,
        consensoDominio: 4,
    },
];

function montar(props?: Record<string, unknown>) {
    return mount(ConsensoDiagnosticoTabela, {
        props: {
            competencias,
            ehConsensoAprovado: false,
            habilitarConcluirAvaliacao: true,
            podeEditar: true,
            ...props,
        },
        global: {
            stubs: {
                BCard: {template: '<section><slot /></section>'},
                BFormSelect: {
                    props: ['options', 'modelValue'],
                    emits: ['update:modelValue'],
                    template: '<select :value="modelValue" v-bind="$attrs" @change="$emit(\'update:modelValue\', $event.target.value)"><option v-for="opcao in options" :key="String(opcao.value)" :value="opcao.value ?? \'\'">{{ opcao.text }}</option></select>',
                },
            },
        },
    });
}

describe('ConsensoDiagnosticoTabela', () => {
    it('normaliza o valor emitido ao atualizar nota', async () => {
        const wrapper = montar();

        await wrapper.get('[data-testid="consenso-chefia-importancia-10"]').setValue('5');

        expect(wrapper.emitted('atualizarNota')).toEqual([
            [10, {origem: 'chefia', campo: 'importancia', valor: 5}],
        ]);
    });

    it('mantem a tabela em leitura quando a edicao nao e permitida', () => {
        const wrapper = montar({
            podeEditar: false,
        });

        expect(wrapper.find('select').exists()).toBe(false);
        expect(wrapper.text()).toContain('Competência A');
        expect(wrapper.text()).toContain('3');
        expect(wrapper.text()).toContain('4');
    });
});
