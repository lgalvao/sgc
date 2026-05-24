import {describe, expect, it, vi, beforeEach} from 'vitest';
import {mount} from '@vue/test-utils';
import SubprocessoModal from '../SubprocessoModal.vue';

describe('SubprocessoModal.vue', () => {
    beforeEach(() => {
        vi.useFakeTimers();
        vi.setSystemTime(new Date('2026-05-13T12:00:00'));
    });

    const stubs = {
        ModalPadrao: {
            props: ['modelValue', 'titulo', 'loading'],
            template: '<div v-if="modelValue"><h1>{{ titulo }}</h1><slot /><button @click="$emit(\'confirmar\')">OK</button></div>'
        },
        BFormGroup: {
            props: ['invalidFeedback', 'state'],
            template: '<div><slot name="label" /><slot /><slot name="description" /><span>{{ invalidFeedback }}</span></div>'
        },
        InputData: {
            props: ['modelValue', 'min'],
            template: '<input :value="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)" />'
        }
    };

    it('preenche valor inicial quando aberto', async () => {
        const dataAtual = new Date('2026-06-01T12:00:00');
        const wrapper = mount(SubprocessoModal, {
            props: {
                mostrarModal: false,
                dataLimiteAtual: dataAtual,
                dataFimEtapaAnterior: null,
                ultimaDataLimiteSubprocesso: null,
                etapaAtual: 1
            },
            global: { stubs }
        });
        
        await wrapper.setProps({ mostrarModal: true });
        const vm = wrapper.vm as any;
        expect(vm.novaDataLimite).toBe('2026-06-01');
    });

    it('valida data limite minima e erros de validacao', async () => {
        const amanha = '2026-05-14';
        const dataFimEtapaAnterior = new Date('2026-05-20T12:00:00');
        const ultimaDataLimite = new Date('2026-05-25T12:00:00');

        const wrapper = mount(SubprocessoModal, {
            props: {
                mostrarModal: true,
                dataLimiteAtual: null,
                dataFimEtapaAnterior: dataFimEtapaAnterior,
                ultimaDataLimiteSubprocesso: ultimaDataLimite,
                etapaAtual: 2
            },
            global: { stubs }
        });
        
        const vm = wrapper.vm as any;
        
        // Minima should be 2026-05-25 (max of amanha, dataFimEtapaAnterior+1, ultimaDataLimite)
        expect(vm.dataLimiteMinima).toBe('2026-05-25');

        // Test invalid data (before ultimaDataLimite)
        vm.novaDataLimite = '2026-05-24';
        await wrapper.vm.$nextTick();
        expect(vm.mensagemErroDataLimite).toContain('maior ou igual à última data limite');

        // Test invalid data (equal to dataFimEtapaAnterior)
        // Adjust ultimaDataLimite to be before dataFimEtapaAnterior to test this specific branch
        await wrapper.setProps({ ultimaDataLimiteSubprocesso: new Date('2026-05-15T12:00:00') });
        vm.novaDataLimite = '2026-05-20';
        await wrapper.vm.$nextTick();
        expect(vm.mensagemErroDataLimite).toContain('maior que a data de fim da etapa anterior');
        
        // Test empty
        await wrapper.find('button').trigger('click');
        vm.novaDataLimite = '';
        await wrapper.vm.$nextTick();
        expect(vm.mensagemErroDataLimite).toContain('obrigatória');
        
        // Test in the past
        await wrapper.setProps({ ultimaDataLimiteSubprocesso: null });
        vm.novaDataLimite = '2026-05-12';
        await wrapper.vm.$nextTick();
        expect(vm.mensagemErroDataLimite).toContain('data futura');
    });

    it('emite confirmarAlteracao quando data eh valida', async () => {
        const wrapper = mount(SubprocessoModal, {
            props: {
                mostrarModal: true,
                dataLimiteAtual: null,
                dataFimEtapaAnterior: null,
                ultimaDataLimiteSubprocesso: null,
                etapaAtual: 1
            },
            global: { stubs }
        });
        
        const vm = wrapper.vm as any;
        vm.novaDataLimite = '2026-06-01';
        await wrapper.vm.$nextTick();
        
        await wrapper.find('button').trigger('click');
        expect(wrapper.emitted('confirmarAlteracao')![0]).toEqual(['2026-06-01']);
    });
});