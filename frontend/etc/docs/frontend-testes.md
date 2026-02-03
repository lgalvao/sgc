# Guia de Testes Frontend - SGC

Este documento define os padrões e melhores práticas para testes no frontend do SGC, utilizando Vitest.

## 1. Visão Geral

- **Framework:** [Vitest](https://vitest.dev/)
- **Ferramentas:** `@vue/test-utils` (montagem de componentes), `jsdom` (ambiente)
- **Escopo:**
    - **Unitários:** Testar funções isoladas (utils, mappers) e componentes simples.
    - **Integração:** Testar Stores e Views (montando o componente e mockando serviços/stores).

## 2. Estrutura e Localização

- **Localização:** Arquivos `*.spec.ts` ou `*.test.ts` próximos ao código fonte ou em diretórios `__tests__`.
- **Execução:** `npm run test:unit`

## 3. Padrões de Implementação

### 3.1. Testando Stores (Pinia)

Testes de store devem validar:

1. Estado inicial
2. Actions (mudança de estado)
3. Tratamento de erros (mock de serviços)

**Exemplo:**

```typescript
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { setActivePinia, createPinia } from 'pinia';
import { useProcessosStore } from '@/stores/processos';
import * as processoService from '@/services/processoService';

// Mock do módulo de serviço
vi.mock('@/services/processoService');

describe('useProcessosStore', () => {
    beforeEach(() => {
        setActivePinia(createPinia());
    });
    
    it('deve buscar processos com sucesso', async () => {
        // Arrange
        const mockProcessos = [{ codigo: 1, descricao: 'Teste' }];
        vi.mocked(processoService.listar).mockResolvedValue(mockProcessos);
        
        // Act
        const store = useProcessosStore();
        await store.buscarProcessos();
        
        // Assert
        expect(store.processos).toEqual(mockProcessos);
        expect(store.isLoading).toBe(false);
    });
    
    it('deve tratar erro ao buscar processos', async () => {
        // Arrange
        vi.mocked(processoService.listar).mockRejectedValue(new Error('Erro'));
        
        // Act
        const store = useProcessosStore();
        
        // Assert
        await expect(store.buscarProcessos()).rejects.toThrow();
        expect(store.lastError).not.toBeNull();
    });
});
```

### 3.2. Testando Componentes

- Use `data-testid` para selecionar elementos, garantindo que o teste não quebre com mudanças de CSS ou estrutura.
- Valide eventos emitidos (`emitted()`).
- Valide renderização condicional.

**Exemplo:**

```typescript
import { mount } from '@vue/test-utils';
import ProcessoCard from '@/components/ProcessoCard.vue';

describe('ProcessoCard', () => {
    it('deve emitir evento ao clicar no botão', async () => {
        const wrapper = mount(ProcessoCard, {
            props: {
                processo: { codigo: 1, descricao: 'Teste', situacao: 'CRIADO' }
            }
        });
        
        await wrapper.find('[data-testid="btn-iniciar"]').trigger('click');
        
        expect(wrapper.emitted('iniciar')).toBeTruthy();
        expect(wrapper.emitted('iniciar')![0]).toEqual([1]);
    });
});
```

## 4. Convenções

- **Nomenclatura:** `deve{Acao}Quando{Condicao}`
- **Mocking:** Use `vi.mock` para isolar dependências externas (axios, serviços).
- **Setup:** Use `beforeEach` para limpar estado (Pinia, mocks).
