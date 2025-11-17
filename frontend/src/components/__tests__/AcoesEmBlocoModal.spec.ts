import { describe, it, expect, vi } from 'vitest';
import { mount } from '@vue/test-utils';
import AcoesEmBlocoModal from '../AcoesEmBlocoModal.vue';

const unidadesDisponiveis = [
  { sigla: 'UND1', nome: 'Unidade 1', situacao: 'Pendente' },
  { sigla: 'UND2', nome: 'Unidade 2', situacao: 'Pendente' },
];

describe('AcoesEmBlocoModal', () => {
  it("deve renderizar o título e o botão corretamente para a ação 'aceitar'", () => {
    const wrapper = mount(AcoesEmBlocoModal, {
      props: {
        mostrar: true,
        tipoAcao: 'aceitar',
        unidadesDisponiveis,
      },
    });

    expect(wrapper.find('.table').exists()).toBe(true);
    expect(wrapper.find('[data-testid="btn-confirmar-acao-bloco"]').text()).toContain('Aceitar');
  });

  it("deve renderizar o título e o botão corretamente para a ação 'homologar'", () => {
    const wrapper = mount(AcoesEmBlocoModal, {
      props: {
        mostrar: true,
        tipoAcao: 'homologar',
        unidadesDisponiveis,
      },
    });

    expect(wrapper.find('.table').exists()).toBe(true);
    expect(wrapper.find('[data-testid="btn-confirmar-acao-bloco"]').text()).toContain('Homologar');
  });

  it('deve pré-selecionar todas as unidades quando o modal é exibido', async () => {
    const wrapper = mount(AcoesEmBlocoModal, {
      props: {
        mostrar: false,
        tipoAcao: 'aceitar',
        unidadesDisponiveis,
      },
    });

    await wrapper.setProps({ mostrar: true });

    const checkboxes = wrapper.findAll<HTMLInputElement>('input[type="checkbox"]');
    expect(checkboxes).toHaveLength(2);
    checkboxes.forEach((checkbox) => {
      expect(checkbox.element.checked).toBe(true);
    });
  });

  it("deve emitir o evento 'fechar' ao clicar no botão de cancelar", async () => {
    const wrapper = mount(AcoesEmBlocoModal, {
      props: {
        mostrar: true,
        tipoAcao: 'aceitar',
        unidadesDisponiveis,
      },
    });

    await wrapper.find('[data-testid="btn-modal-cancelar"]').trigger('click');
    expect(wrapper.emitted()).toHaveProperty('fechar');
  });

  it("deve emitir o evento 'confirmar' com as unidades selecionadas", async () => {
    const wrapper = mount(AcoesEmBlocoModal, {
      props: {
        mostrar: false,
        tipoAcao: 'aceitar',
        unidadesDisponiveis,
      },
    });
    await wrapper.setProps({ mostrar: true });

    await wrapper.find('[data-testid="chk-unidade-UND2"]').setValue(false);
    await wrapper.find('[data-testid="btn-confirmar-acao-bloco"]').trigger('click');

    expect(wrapper.emitted()).toHaveProperty('confirmar');
    const payload = wrapper.emitted('confirmar')?.[0] as string[][];
    expect(payload[0]).toEqual(['UND1']);
  });

  it('deve exibir um alerta se nenhuma unidade for selecionada', async () => {
    const alertSpy = vi.spyOn(window, 'alert').mockImplementation(() => {});
    const wrapper = mount(AcoesEmBlocoModal, {
      props: {
        mostrar: false,
        tipoAcao: 'aceitar',
        unidadesDisponiveis,
      },
    });
    await wrapper.setProps({ mostrar: true });

    await wrapper.find('[data-testid="chk-unidade-UND1"]').setValue(false);
    await wrapper.find('[data-testid="chk-unidade-UND2"]').setValue(false);
    await wrapper.find('[data-testid="btn-confirmar-acao-bloco"]').trigger('click');

    expect(alertSpy).toHaveBeenCalledWith('Selecione ao menos uma unidade para processar.');
    expect(wrapper.emitted()).not.toHaveProperty('confirmar');

    alertSpy.mockRestore();
  });
});
