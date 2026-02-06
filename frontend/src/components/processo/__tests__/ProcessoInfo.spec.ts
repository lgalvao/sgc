import {describe, it, expect} from 'vitest';
import {mount} from '@vue/test-utils';
import ProcessoInfo from '../ProcessoInfo.vue';

describe('ProcessoInfo.vue', () => {
  it('renderiza as informações do processo', () => {
    const wrapper = mount(ProcessoInfo, {
      props: {
        tipoLabel: 'Mapeamento',
        situacaoLabel: 'Em andamento',
        dataLimite: '2023-12-31T00:00:00',
        numUnidades: 10,
        showUnidades: true
      }
    });

    expect(wrapper.text()).toContain('Tipo: Mapeamento');
    expect(wrapper.text()).toContain('Situação: Em andamento');
    expect(wrapper.text()).toContain('Data Limite:');
    expect(wrapper.text()).toContain('Unidades participantes: 10');
  });

  it('formatarData funciona corretamente', () => {
    const wrapper = mount(ProcessoInfo);
    const vm = wrapper.vm as any;
    expect(vm.formatarData('2023-01-01')).toContain('01/01/2023');
    expect(vm.formatarData('')).toBe('');
    expect(vm.formatarData(undefined)).toBe('');
  });
});
