import {describe, it, expect} from 'vitest';
import {mount} from '@vue/test-utils';
import RelatorioCards from '../RelatorioCards.vue';

describe('RelatorioCards.vue', () => {
  it('emite eventos ao clicar nos cards', async () => {
    const wrapper = mount(RelatorioCards, {
      props: {
        mapasVigentesCount: 5,
        diagnosticosGapsCount: 10,
        processosFiltradosCount: 3
      },
      global: {
        stubs: {
          'b-row': { template: '<div><slot /></div>' },
          'b-col': { template: '<div><slot /></div>' },
          'b-card': { template: '<div class="card" @click="$emit(\'click\')"><slot /></div>' }
        }
      }
    });

    await wrapper.find('[data-testid="card-relatorio-mapas"]').trigger('click');
    expect(wrapper.emitted('abrir-mapas-vigentes')).toBeTruthy();

    await wrapper.find('[data-testid="card-relatorio-gaps"]').trigger('click');
    expect(wrapper.emitted('abrir-diagnosticos-gaps')).toBeTruthy();

    await wrapper.find('[data-testid="card-relatorio-andamento"]').trigger('click');
    expect(wrapper.emitted('abrir-andamento-geral')).toBeTruthy();
  });
});
