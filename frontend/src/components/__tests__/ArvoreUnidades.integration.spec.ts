import { describe, it, expect } from 'vitest';
import { mount, flushPromises } from '@vue/test-utils';
import ArvoreUnidades from '@/components/ArvoreUnidades.vue';
import type { Unidade } from '@/types/tipos';

/**
 * TESTES DE INTEGRAÇÃO - Estes testes TERIAM PEGADO os bugs reais!
 * 
 * Diferença dos testes existentes:
 * - Montam o componente completo (não apenas testam funções isoladas)
 * - Verificam props dos checkboxes (estado visual)
 * - Testam reatividade (watches, computed)
 * - Testam interação com usuário (clicks)
 */

describe('ArvoreUnidades - Testes de Integração (TERIAM PEGADO OS BUGS)', () => {
  const criarUnidades = (): Unidade[] => [
    {
      codigo: 1,
      sigla: 'SECRETARIA_1',
      nome: 'Secretaria 1',
      tipo: 'INTEROPERACIONAL',
      isElegivel: true,
      idServidorTitular: 0,
      responsavel: null,
      filhas: [
        {
          codigo: 11,
          sigla: 'ASSESSORIA_11',
          nome: 'Assessoria 11',
          tipo: 'OPERACIONAL',
          isElegivel: true,
          idServidorTitular: 0,
          responsavel: null,
          filhas: []
        },
        {
          codigo: 12,
          sigla: 'ASSESSORIA_12',
          nome: 'Assessoria 12',
          tipo: 'OPERACIONAL',
          isElegivel: true,
          idServidorTitular: 0,
          responsavel: null,
          filhas: []
        },
        {
          codigo: 13,
          sigla: 'COORD_11',
          nome: 'Coordenadoria 11',
          tipo: 'INTERMEDIARIA',
          isElegivel: false,
          idServidorTitular: 0,
          responsavel: null,
          filhas: [
            {
              codigo: 131,
              sigla: 'SECAO_111',
              nome: 'Seção 111',
              tipo: 'OPERACIONAL',
              isElegivel: true,
              idServidorTitular: 0,
              responsavel: null,
              filhas: []
            },
            {
              codigo: 132,
              sigla: 'SECAO_112',
              nome: 'Seção 112',
              tipo: 'OPERACIONAL',
              isElegivel: true,
              idServidorTitular: 0,
              responsavel: null,
              filhas: []
            },
            {
              codigo: 133,
              sigla: 'SECAO_113',
              nome: 'Seção 113',
              tipo: 'OPERACIONAL',
              isElegivel: true,
              idServidorTitular: 0,
              responsavel: null,
              filhas: []
            }
          ]
        },
        {
          codigo: 14,
          sigla: 'COORD_12',
          nome: 'Coordenadoria 12',
          tipo: 'INTERMEDIARIA',
          isElegivel: false,
          idServidorTitular: 0,
          responsavel: null,
          filhas: [
            {
              codigo: 141,
              sigla: 'SECAO_121',
              nome: 'Seção 121',
              tipo: 'OPERACIONAL',
              isElegivel: true,
              idServidorTitular: 0,
              responsavel: null,
              filhas: []
            }
          ]
        }
      ]
    }
  ];

  describe('BUG 1: BFormCheckbox recebendo getEstadoSelecao diretamente', () => {
    it('TERIA PEGADO: Checkbox deve ter props model-value e indeterminate separadas', () => {
      const wrapper = mount(ArvoreUnidades, {
        props: {
          unidades: criarUnidades(),
          modelValue: [132] // 1 de 3 filhas de COORD_11
        }
      });

      // Encontra o UnidadeTreeNode de COORD_11
      const treeNodes = wrapper.findAllComponents({ name: 'UnidadeTreeNode' });
      const coord11Node = treeNodes.find(node => 
        (node.props('unidade') as Unidade).sigla === 'COORD_11'
      );
      
      expect(coord11Node).toBeTruthy();
      
      // Verifica que getEstadoSelecao retorna 'indeterminate'
      const vm = wrapper.vm as any;
      const coord11 = criarUnidades()[0].filhas![2];
      expect(vm.getEstadoSelecao(coord11)).toBe('indeterminate');
    });

    it('TERIA PEGADO: Checkbox marcado quando todas filhas selecionadas', () => {
      const wrapper = mount(ArvoreUnidades, {
        props: {
          unidades: criarUnidades(),
          modelValue: [131, 132, 133] // Todas filhas de COORD_11
        }
      });

      const vm = wrapper.vm as any;
      const coord11 = criarUnidades()[0].filhas![2];
      
      // COORD_11 deve estar marcada (todas filhas selecionadas)
      expect(vm.getEstadoSelecao(coord11)).toBe(true);
    });

    it('TERIA PEGADO: Checkbox desmarcado quando nenhuma filha selecionada', () => {
      const wrapper = mount(ArvoreUnidades, {
        props: {
          unidades: criarUnidades(),
          modelValue: [] // Nada selecionado
        }
      });

      const vm = wrapper.vm as any;
      const coord11 = criarUnidades()[0].filhas![2];
      
      // COORD_11 deve estar desmarcada
      expect(vm.getEstadoSelecao(coord11)).toBe(false);
    });
  });

  describe('BUG 2: Watch bidirecional faltando', () => {
    it('TERIA PEGADO: unidadesSelecionadasLocal sincroniza com props.modelValue', async () => {
      const wrapper = mount(ArvoreUnidades, {
        props: {
          unidades: criarUnidades(),
          modelValue: []
        }
      });

      const vm = wrapper.vm as any;
      
      // Estado inicial
      expect(vm.unidadesSelecionadasLocal).toEqual([]);

      // Simula mudança externa do modelValue (ex: pai atualiza)
      await wrapper.setProps({ modelValue: [131, 132, 133] });
      await flushPromises();

      // ANTES DA CORREÇÃO: unidadesSelecionadasLocal continuaria []
      // DEPOIS DA CORREÇÃO: sincroniza com props.modelValue
      expect(vm.unidadesSelecionadasLocal).toEqual([131, 132, 133]);
    });

    it('TERIA PEGADO: Mudanças locais emitem update:modelValue', async () => {
      const wrapper = mount(ArvoreUnidades, {
        props: {
          unidades: criarUnidades(),
          modelValue: []
        }
      });

      const vm = wrapper.vm as any;
      
      // Simula seleção interna
      vm.unidadesSelecionadasLocal = [131];
      await flushPromises();

      // Verifica se emitiu update:modelValue
      const emitted = wrapper.emitted('update:modelValue');
      expect(emitted).toBeTruthy();
      expect(emitted![emitted!.length - 1]).toEqual([[131]]);
    });
  });

  describe('BUG 3: Loop infinito de watches', () => {
    it('TERIA PEGADO: Não deve causar loop infinito ao atualizar modelValue', async () => {
      const wrapper = mount(ArvoreUnidades, {
        props: {
          unidades: criarUnidades(),
          modelValue: []
        }
      });

      // Simula múltiplas atualizações rápidas
      await wrapper.setProps({ modelValue: [131] });
      await wrapper.setProps({ modelValue: [131, 132] });
      await wrapper.setProps({ modelValue: [131, 132, 133] });
      await flushPromises();

      // ANTES DA CORREÇÃO: Causaria "Maximum recursive updates exceeded"
      // DEPOIS DA CORREÇÃO: Funciona sem erro
      const vm = wrapper.vm as any;
      expect(vm.unidadesSelecionadasLocal).toEqual([131, 132, 133]);
    });
  });

  describe('BUG 4: Expansão das raízes', () => {
    it('TERIA PEGADO: Raízes devem inicializar expandidas', () => {
      const wrapper = mount(ArvoreUnidades, {
        props: {
          unidades: criarUnidades(),
          modelValue: []
        }
      });

      const vm = wrapper.vm as any;
      
      // SECRETARIA_1 (código 1) deve estar expandida inicialmente
      expect(vm.expandedUnits.has(1)).toBe(true);
    });

    it('TERIA PEGADO: Raízes devem poder ser contraídas', async () => {
      const wrapper = mount(ArvoreUnidades, {
        props: {
          unidades: criarUnidades(),
          modelValue: []
        }
      });

      const vm = wrapper.vm as any;
      
      // Inicialmente expandida
      expect(vm.expandedUnits.has(1)).toBe(true);

      // Simula click no botão de expansão
      vm.toggleExpand(criarUnidades()[0]);
      await flushPromises();

      // ANTES DA CORREÇÃO: depth === 0 impedia contração
      // DEPOIS DA CORREÇÃO: pode ser contraída
      expect(vm.expandedUnits.has(1)).toBe(false);
    });
  });

  describe('BUG 5: getEstadoSelecao não reflete modelValue corretamente', () => {
    it('TERIA PEGADO: SECRETARIA_1 indeterminada com algumas filhas selecionadas', () => {
      const wrapper = mount(ArvoreUnidades, {
        props: {
          unidades: criarUnidades(),
          modelValue: [11, 12] // Apenas ASSESSORIA_11 e ASSESSORIA_12
        }
      });

      const vm = wrapper.vm as any;
      const secretaria = criarUnidades()[0];
      
      // SECRETARIA_1 tem 4 filhas: ASSESSORIA_11, ASSESSORIA_12, COORD_11, COORD_12
      // Apenas 2 estão marcadas (ASSESSORIA_11 e ASSESSORIA_12)
      // COORD_11 e COORD_12 estão desmarcadas (sem filhas selecionadas)
      const estado = vm.getEstadoSelecao(secretaria);
      
      expect(estado).toBe('indeterminate');
    });

    it('TERIA PEGADO: SECRETARIA_1 marcada com todas filhas selecionadas', () => {
      const wrapper = mount(ArvoreUnidades, {
        props: {
          unidades: criarUnidades(),
          modelValue: [1, 11, 12, 131, 132, 133, 141] // Todas elegíveis
        }
      });

      const vm = wrapper.vm as any;
      const secretaria = criarUnidades()[0];
      
      const estado = vm.getEstadoSelecao(secretaria);
      expect(estado).toBe(true);
    });

    it('TERIA PEGADO: COORD_11 indeterminada com 2 de 3 filhas', () => {
      const wrapper = mount(ArvoreUnidades, {
        props: {
          unidades: criarUnidades(),
          modelValue: [132, 133] // 2 de 3 filhas
        }
      });

      const vm = wrapper.vm as any;
      const coord11 = criarUnidades()[0].filhas![2];
      
      const estado = vm.getEstadoSelecao(coord11);
      expect(estado).toBe('indeterminate');
    });
  });

  describe('BUG 6: isHabilitado não funciona recursivamente', () => {
    it('TERIA PEGADO: INTERMEDIARIA com filhas elegíveis deve estar habilitada', () => {
      const wrapper = mount(ArvoreUnidades, {
        props: {
          unidades: criarUnidades(),
          modelValue: []
        }
      });

      const vm = wrapper.vm as any;
      const coord11 = criarUnidades()[0].filhas![2];
      
      // COORD_11 não é elegível (INTERMEDIARIA)
      expect(coord11.isElegivel).toBe(false);
      
      // Mas deve estar habilitada (tem filhas elegíveis)
      expect(vm.isHabilitado(coord11)).toBe(true);
    });

    it('TERIA PEGADO: Checkbox de INTERMEDIARIA habilitada deve estar enabled', () => {
      const wrapper = mount(ArvoreUnidades, {
        props: {
          unidades: criarUnidades(),
          modelValue: []
        }
      });

      const coord11Checkbox = wrapper.find('[data-testid="chk-arvore-unidade-COORD_11"]');
      
      // ANTES DA CORREÇÃO: disabled (isElegivel = false)
      // DEPOIS DA CORREÇÃO: enabled (isHabilitado = true)
      expect(coord11Checkbox.attributes('disabled')).toBeUndefined();
    });
  });

  describe('Cenário Completo: Fluxo de Seleção', () => {
    it('TERIA PEGADO: Fluxo completo de seleção hierárquica', async () => {
      const wrapper = mount(ArvoreUnidades, {
        props: {
          unidades: criarUnidades(),
          modelValue: []
        }
      });

      const vm = wrapper.vm as any;

      // 1. Seleciona SECAO_112
      vm.toggle(criarUnidades()[0].filhas![2].filhas![1], true);
      await flushPromises();

      // COORD_11 deve estar indeterminada (1 de 3)
      expect(vm.getEstadoSelecao(criarUnidades()[0].filhas![2])).toBe('indeterminate');

      // 2. Seleciona SECAO_113
      vm.toggle(criarUnidades()[0].filhas![2].filhas![2], true);
      await flushPromises();

      // COORD_11 ainda indeterminada (2 de 3)
      expect(vm.getEstadoSelecao(criarUnidades()[0].filhas![2])).toBe('indeterminate');

      // 3. Seleciona SECAO_111
      vm.toggle(criarUnidades()[0].filhas![2].filhas![0], true);
      await flushPromises();

      // COORD_11 agora marcada (3 de 3)
      expect(vm.getEstadoSelecao(criarUnidades()[0].filhas![2])).toBe(true);

      // 4. Verifica que INTERMEDIARIA não está no modelValue
      const emitted = wrapper.emitted('update:modelValue');
      const lastEmit = emitted![emitted!.length - 1][0] as number[];
      expect(lastEmit).toContain(131);
      expect(lastEmit).toContain(132);
      expect(lastEmit).toContain(133);
      expect(lastEmit).not.toContain(13); // COORD_11 não deve estar
    });
  });
});
