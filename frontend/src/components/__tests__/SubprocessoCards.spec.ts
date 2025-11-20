import { describe, expect, it, vi, beforeEach } from 'vitest';
import { mount } from '@vue/test-utils';
import SubprocessoCards from '../SubprocessoCards.vue';
import { type Mapa, TipoProcesso, type Unidade, SubprocessoPermissoes } from '@/types/tipos';
import { situacaoLabel } from '@/utils';

const pushMock = vi.fn();

vi.mock('vue-router', () => ({
  useRouter: () => ({
    push: pushMock,
    currentRoute: {
      value: {
        params: {
          codSubprocesso: '123',
        },
      },
    },
  }),
}));

describe('SubprocessoCards.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  const defaultPermissoes: SubprocessoPermissoes = {
    podeVerPagina: true,
    podeEditarMapa: true,
    podeVisualizarMapa: true,
    podeDisponibilizarCadastro: true,
    podeDevolverCadastro: true,
    podeAceitarCadastro: true,
    podeVisualizarDiagnostico: true,
    podeAlterarDataLimite: true,
    podeVisualizarImpacto: true,
  };

  const mountComponent = (props: { tipoProcesso: TipoProcesso; mapa: Mapa | null; situacao?: string; permissoes?: SubprocessoPermissoes }) => {
    return mount(SubprocessoCards, {
      props: {
        permissoes: defaultPermissoes,
        ...props,
      },
    });
  };

  const mockMapa: Mapa = {
    codigo: 1,
    descricao: 'mapa de teste',
    unidade: { sigla: 'UNID_TESTE' } as Unidade,
    situacao: 'em_andamento',
    codProcesso: 1,
    competencias: [],
    dataCriacao: new Date().toISOString(),
    dataDisponibilizacao: null,
    dataFinalizacao: null,
  };

  describe('Navigation Logic', () => {
    it('deve navegar para SubprocessoCadastro ao clicar no card de atividades (edição)', async () => {
      const wrapper = mountComponent({
        tipoProcesso: TipoProcesso.MAPEAMENTO,
        mapa: null,
        situacao: 'Mapa disponibilizado',
        permissoes: { ...defaultPermissoes, podeEditarMapa: true }
      });

      const card = wrapper.find('[data-testid="atividades-card"]');
      await card.trigger('click');

      expect(pushMock).toHaveBeenCalledWith({
        name: 'SubprocessoCadastro',
        params: { codSubprocesso: '123' }
      });
    });

    it('deve navegar para SubprocessoVisCadastro ao clicar no card de atividades (visualização)', async () => {
      const wrapper = mountComponent({
        tipoProcesso: TipoProcesso.MAPEAMENTO,
        mapa: null,
        situacao: 'Mapa disponibilizado',
        permissoes: { ...defaultPermissoes, podeEditarMapa: false, podeVisualizarMapa: true }
      });

      const card = wrapper.find('[data-testid="atividades-card-vis"]');
      await card.trigger('click');

      expect(pushMock).toHaveBeenCalledWith({
        name: 'SubprocessoVisCadastro',
        params: { codSubprocesso: '123' }
      });
    });

    it('deve navegar para SubprocessoMapa ao clicar no card de mapa', async () => {
      const wrapper = mountComponent({
        tipoProcesso: TipoProcesso.MAPEAMENTO,
        mapa: mockMapa,
        situacao: 'Mapa disponibilizado',
        permissoes: { ...defaultPermissoes, podeVisualizarMapa: true }
      });

      const card = wrapper.find('[data-testid="mapa-card"]');
      await card.trigger('click');

      expect(pushMock).toHaveBeenCalledWith({
        name: 'SubprocessoMapa',
        params: { codSubprocesso: '123' }
      });
    });

    it('deve navegar para DiagnosticoEquipe ao clicar no card de diagnostico', async () => {
        const wrapper = mountComponent({
            tipoProcesso: TipoProcesso.DIAGNOSTICO,
            mapa: null,
            situacao: 'Em andamento',
            permissoes: { ...defaultPermissoes, podeVisualizarDiagnostico: true }
        });

        const card = wrapper.find('[data-testid="diagnostico-card"]');
        await card.trigger('click');

        expect(pushMock).toHaveBeenCalledWith({
            name: 'DiagnosticoEquipe',
            params: { codSubprocesso: '123' }
        });
    });

    it('deve navegar para OcupacoesCriticas ao clicar no card de ocupações', async () => {
        const wrapper = mountComponent({
            tipoProcesso: TipoProcesso.DIAGNOSTICO,
            mapa: null,
            situacao: 'Em andamento',
        });

        const card = wrapper.find('[data-testid="ocupacoes-card"]');
        await card.trigger('click');

        expect(pushMock).toHaveBeenCalledWith({
            name: 'OcupacoesCriticas',
            params: { codSubprocesso: '123' }
        });
    });
  });

  describe('Rendering Logic', () => {
      it('não deve renderizar card de mapa se não puder visualizar', () => {
           const wrapper = mountComponent({
              tipoProcesso: TipoProcesso.MAPEAMENTO,
              mapa: mockMapa,
              situacao: 'Mapa disponibilizado',
              permissoes: { ...defaultPermissoes, podeVisualizarMapa: false }
          });

          expect(wrapper.find('[data-testid="mapa-card"]').exists()).toBe(false);
      });
  });

});
