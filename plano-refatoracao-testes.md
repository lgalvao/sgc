# Plano de Refatoração dos Testes E2E

Este documento apresenta um plano detalhado para refatorar os testes E2E do projeto SGC, com o objetivo de torná-los mais semânticos, claros e alinhados às boas práticas estabelecidas.

## 1. Contexto e Objetivos

Com base na análise dos arquivos de teste e nas regras/padrões documentados, identificamos oportunidades para melhorar:

1. **Legibilidade e Semântica**: Tornar os testes mais expressivos e autoexplicativos
2. **Consistência**: Padronizar o uso de seletores, funções auxiliares e constantes
3. **Manutenibilidade**: Reduzir duplicações e melhorar a organização do código de teste
4. **Alinhamento com Boas Práticas**: Seguir os padrões estabelecidos no projeto

## 2. Análise dos Problemas Atuais

### 2.1. Duplicação de Funções
- Função `navegarParaCadastroAtividades` duplicada em `auxiliares-teste.ts` e `auxiliares-navegacao.ts`

### 2.2. Inconsistência no Uso de Seletores
- Mistura de métodos: `getByRole`, `getByTestId`, seletores por texto
- Alguns seletores poderiam ser mais específicos para evitar ambiguidades

### 2.3. Nomes de Funções Auxiliares
- Alguns nomes poderiam ser mais descritivos sobre o que realmente fazem
- Funções complexas poderiam ser divididas em partes menores

### 2.4. Uso Inconsistente de Constantes
- Algumas strings estão hardcoded em vez de usar as constantes já definidas

## 3. Propostas de Melhoria

### 3.1. Organização dos Arquivos Auxiliares

#### 3.1.1. Renomear e Reorganizar Arquivos
- `auxiliares-teste.ts` → `auxiliares-verificacoes.ts` (funções de verificação e asserções)
- `auxiliares-acoes.ts` → `auxiliares-acoes.ts` (manter como está)
- `auxiliares-navegacao.ts` → `auxiliares-navegacao.ts` (manter como está)
- Criar `auxiliares-utils.ts` para funções genéricas como `gerarNomeUnico`

#### 3.1.2. Eliminar Duplicações
- Remover a função duplicada `navegarParaCadastroAtividades`
- Manter apenas uma implementação no arquivo apropriado

### 3.2. Padronização de Seletores

#### 3.2.1. Priorizar `data-testid`
- Usar consistentemente `data-testid` para elementos interativos
- Adicionar `data-testid` nos componentes Vue quando necessário

#### 3.2.2. Seletores Mais Específicos
- Encadear seletores para maior precisão
- Exemplo: `page.getByTestId('tabela-processos').getByRole('row', { name: /texto/ })`

### 3.3. Melhoria nos Nomes das Funções Auxiliares

#### 3.3.1. Funções de Verificação
- `verificarUrl` → `esperarUrl` (já existe, usar consistentemente)
- `esperarElementoVisivel` → manter
- `esperarElementoInvisivel` → manter
- `esperarMensagemSucesso` → manter
- `esperarMensagemErro` → manter

#### 3.3.2. Funções de Navegação
- `navegarParaCriacaoProcesso` → manter
- `navegarParaDetalhesProcesso` → manter
- `navegarParaCadastroAtividades` → manter (na versão única)

#### 3.3.3. Funções de Ação
- `finalizarProcesso` → manter
- `disponibilizarCadastro` → manter
- `homologarItem` → manter
- `devolverParaAjustes` → manter
- `validarMapa` → manter
- `criarCompetencia` → manter
- `iniciarProcesso` → manter

### 3.4. Uso Consistente de Constantes

#### 3.4.1. Textos
- Substituir strings hardcoded por constantes de `TEXTOS`
- Exemplo: Substituir `"Finalizar processo"` por `TEXTOS.FINALIZAR_PROCESSO`

#### 3.4.2. Seletores CSS
- Substituir seletores CSS hardcoded por constantes de `SELETORES_CSS`
- Exemplo: Substituir `'.modal.show'` por `SELETORES_CSS.MODAL_VISIVEL`

### 3.5. Refatoração de Funções Complexas

#### 3.5.1. Dividir Funções Longas
- Dividir funções auxiliares complexas em partes mentras e mais focadas
- Exemplo: `devolverParaAjustes` poderia ser dividida em partes menores

#### 3.5.2. Criar Funções Auxiliares para Padrões Comuns
- Criar funções para verificar notificações
- Criar funções para interagir com modais de forma padronizada

### 3.6. Melhorias nos Testes Específicos

#### 3.6.1. Estrutura dos Testes
- Manter o padrão de `describe` para agrupar funcionalidades relacionadas
- Usar nomes descritivos para os blocos `test`

#### 3.6.2. Nomes dos Testes
- Continuar usando o padrão "deve [ação] quando [condição]"
- Ser mais específico sobre o que está sendo testado

#### 3.6.3. Comentários e Referências
- Adicionar comentários para referenciar itens específicos dos CDUs quando relevante
- Exemplo: `// item 9.1 - deve enviar notificações por email`

## 4. Etapas de Implementação

### 4.1. Etapa 1: Organização dos Arquivos Auxiliares
1. Criar novo arquivo `auxiliares-utils.ts`
2. Mover funções genéricas para `auxiliares-utils.ts`
3. Remover duplicação da função `navegarParaCadastroAtividades`
4. Renomear `auxiliares-teste.ts` para `auxiliares-verificacoes.ts`

### 4.2. Etapa 2: Padronização de Seletores
1. Identificar seletores hardcoded nos arquivos de teste
2. Substituir por seletores usando `data-testid` quando possível
3. Encadear seletores para maior precisão
4. Adicionar `data-testid` nos componentes Vue quando necessário

### 4.3. Etapa 3: Uso Consistente de Constantes
1. Identificar strings hardcoded nos arquivos auxiliares
2. Substituir por constantes apropriadas de `TEXTOS` e `SELETORES_CSS`
3. Verificar que todas as substituições estão corretas

### 4.4. Etapa 4: Refatoração de Funções
1. Dividir funções complexas em partes menores
2. Criar novas funções auxiliares para padrões comuns
3. Melhorar nomes de funções quando necessário

### 4.5. Etapa 5: Atualização dos Testes
1. Atualizar importações após reorganização dos arquivos
2. Verificar que todos os testes continuam passando
3. Ajustar testes para usar os seletores e funções padronizados

## 5. Critérios de Aceitação

1. Todos os testes E2E devem continuar passando após as refatorações
2. Código deve estar alinhado com as boas práticas documentadas
3. Funções auxiliares devem ter nomes descritivos e focados
4. Seletores devem ser consistentes e precisos
5. Constantes devem ser usadas em vez de strings hardcoded
6. Estrutura dos arquivos deve ser mais organizada e sem duplicações

## 6. Verificação e Validação

Após cada etapa de refatoração:
1. Executar `npm run lint` para verificar estilo do código
2. Executar `npm run typecheck` para verificar tipos
3. Executar `npm run test:e2e` para verificar que os testes continuam passando
4. Revisar manualmente as mudanças para garantir qualidade

## 7. Considerações Finais

Esta refatoração visa melhorar a qualidade e manutenibilidade dos testes E2E sem alterar sua funcionalidade. O foco está em tornar o código mais legível, consistente e alinhado com as práticas recomendadas do projeto. A implementação deve ser feita de forma incremental, verificando constantemente que não há regressões.