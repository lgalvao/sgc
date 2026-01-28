# Plano de Refatoração do Frontend - SGC

Este documento detalha o plano de ação para resolver os problemas de arquitetura, duplicação e vazamento de lógica identificados na análise técnica.

## Objetivos
- Reduzir o débito técnico e a complexidade dos componentes.
- Garantir que a lógica de negócio seja governada pelo Backend.
- Melhorar a testabilidade e reusabilidade do código.
- Maximizar o aproveitamento das capacidades existentes no Backend.

---

## 1. Centralização da Lógica de Permissões e Ações
**Problema:** O frontend calcula manualmente se um botão deve aparecer ou se uma ação é permitida, ignorando o potencial do Backend.

### Ações:
- **Consumo Integral do `SubprocessoPermissoesDto`**: Aproveitar que o backend já retorna um objeto completo de booleanos. 
    - Remover cálculos manuais em `useVisMapaLogic.ts` e `VisMapa.vue`.
    - Substituir lógica local por referências ao objeto `permissoes` vindo da API.
- **Uso de Flags de Ação em Processos**: Utilizar os campos `podeFinalizar`, `podeHomologarCadastro` e `podeHomologarMapa` do `ProcessoDetalheDto` em `ProcessoView.vue`.
- **Simplificação de "Mais Ações"**: Condicionar a exibição de itens do menu dropdown estritamente aos flags de permissão do backend.

---

## 2. Aproveitamento de Respostas Enriquecidas da API
**Problema:** O frontend faz chamadas redundantes para recarregar dados após operações simples (ex: recarregar lista após deletar um item).

### Ações:
- **Operações Atômicas**: Utilizar o `AtividadeOperacaoResponse` da API em todas as mutações de atividades e conhecimentos.
    - O backend já retorna `atividadesAtualizadas` e o status do subprocesso.
    - Remover chamadas manuais de "re-fetch" nos Stores e Composables após `POST/PUT/DELETE`.
- **Sincronização de Estado Local**: Garantir que o Store do Pinia aceite o `AtividadeOperacaoResponse` para atualizar o cache local instantaneamente, reduzindo latência percebida e tráfego de rede.

---

## 3. Consolidação de UI e Utilidade
**Problema:** Formatação e manipulação de árvores duplicadas entre Backend e Frontend.

### Ações:
- **Preferência por Dados Formatados**: O backend já envia campos como `dataLimiteFormatada`, `situacaoLabel` e `tipoLabel`.
    - Parar de usar funções de formatação de data locais (JS) onde o backend já provê o texto pronto.
- **Unificação do `flattenTree`**:
    - Remover as implementações locais de `flatten` dentro de `ProcessoView.vue`.
    - Utilizar o `@/utils/index.ts -> flattenTree` para todas as lógicas de árvore (como seleção em bloco).

---

## 4. Decomposição de Componentes (De-bloating)
**Problema:** Componentes como `AtividadeItem.vue` possuem responsabilidades excessivas.

### Ações:
- **Extração de `ConhecimentoItem.vue`**: Isolar a lógica de exibição e edição de um único conhecimento.
- **Criação de `InlineEditor.vue`**: Criar um componente genérico para edição de texto "clique-para-editar" que gerencie o estado de toggle e botões de salvar/cancelar.
- **Padronização de Modais**: Aproveitar componentes de feedback genéricos para reduzir o boilerplate de modais CRUD em cada View.

---

## 5. Refinamento de Composables de Lógica
**Problema:** Composables muito grandes gerindo múltiplos estados de UI simultâneos.

### Ações:
- **Separação de Preocupações**: Isolar a lógica de "Diálogo/Feedback" da lógica de "Comunicação com API".
- **Encapsulamento nos Stores**: Delegar a orquestração completa da operação (Chamada API -> Atualização de Cache -> Notificação de Sucesso) para o Store de Pinia.

---

## 6. Cronogramas e Prioridades

| Fase | Descrição | Prioridade |
| :--- | :--- | :--- |
| **Fase 1** | **Estratégia de Permissões**: Mapear `SubprocessoPermissoesDto` e flags do `ProcessoDetalheDto` em todas as Views. | Crítica |
| **Fase 2** | **Otimização de Mutação**: Implementar o consumo de `AtividadeOperacaoResponse` para eliminar re-loads desnecessários. | Alta |
| **Fase 3** | **Limpeza de UI**: Remover formatações de data locais e unificar o `flattenTree`. | Média |
| **Fase 4** | **Arquitetura de Componentes**: Criar `InlineEditor` e decompor `AtividadeItem`. | Média |

---

## Conclusão
Aproveitando as capacidades já existentes nas APIs do SGC (Permissões, Respostas Atômicas e Formatação), podemos reduzir o código do frontend em aproximadamente 20-30%, tornando-o um verdadeiro "cliente magro" focado apenas em apresentação e experiência do usuário.
