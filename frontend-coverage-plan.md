# Plano de Cobertura de Testes Frontend

O objetivo deste plano é aumentar a cobertura de testes do frontend para 95%, com foco especial em branches, funções e linhas de código não cobertas.

A cobertura atual é:
- **Linhas**: 85.96% (Meta: 95%)
- **Funções**: 81.08% (Meta: 95%)
- **Declarações**: 84.67% (Meta: 95%)
- **Branches**: 76.82% (Meta: 95%)

## Áreas Prioritárias para Melhoria

As seguintes áreas foram identificadas como tendo baixa cobertura e serão o foco principal:

### 1. Views (Componentes de Página)

As views são os maiores ofensores em termos de cobertura, especialmente as mais complexas.

#### `src/views/ProcessoView.vue` (38.84% Linhas)
- **Problema**: Baixa cobertura em tratamento de ações e estados complexos do processo.
- **Ação**:
  - Criar testes para verificar o comportamento de todos os botões de ação e suas condições de visibilidade.
  - Testar fluxos de erro nas chamadas de serviço.
  - Cobrir lógica de navegação e redirecionamento.

#### `src/views/ConfiguracoesView.vue` (38.88% Linhas)
- **Problema**: Lógica de formulário e validações pouco testada.
- **Ação**:
  - Testar envio de formulário com dados válidos e inválidos.
  - Verificar tratamento de erros da API.
  - Testar interações de UI (ex: toggles, selects).

#### `src/views/VisSubprocessoView.vue` (53.03% Linhas)
- **Problema**: Exibição de detalhes do subprocesso e suas abas/seções não totalmente cobertas.
- **Ação**:
  - Testar renderização condicional baseada no estado do subprocesso.
  - Simular interações com elementos da visualização.

#### `src/views/CadAtividades.vue` (77.5% Linhas)
- **Problema**: Componente complexo com muita lógica de manipulação de lista e validação.
- **Ação**:
  - Reforçar testes de adição, edição e remoção de atividades.
  - Testar validações de negócio (ex: atividade sem competência).
  - Cobrir casos de erro ao salvar.

#### `src/views/VisAtividades.vue` (82.53% Linhas)
- **Problema**: Similar ao cadastro, mas focado em visualização e aprovação/rejeição.
- **Ação**:
  - Testar fluxos de aprovação e rejeição de atividades.
  - Cobrir filtros e ordenação se houver.

### 2. Stores (Pinia)

Algumas stores têm baixa cobertura, o que é crítico pois contêm lógica de negócio e estado global.

#### `src/stores/configuracoes.ts` (37.93% Linhas)
- **Problema**: Actions de carregamento e salvamento de configurações não testadas.
- **Ação**:
  - Criar testes unitários para a store isoladamente.
  - Mockar serviços e verificar mutações de estado.

#### `src/stores/subprocessos.ts` (68.42% Linhas)
- **Problema**: Lógica complexa de gerenciamento de estado de subprocessos e suas listas.
- **Ação**:
  - Testar actions de busca, criação e atualização.
  - Verificar getters complexos se houver.

#### `src/stores/alertas.ts` (87.5% Linhas)
- **Problema**: Branches não cobertos (50%).
- **Ação**:
  - Testar caminhos alternativos em actions (ex: marcar como lido com falha).

### 3. Componentes UI

Alguns componentes reutilizáveis precisam de atenção.

#### `src/components/ProcessoHeader.vue` (68.42% Linhas)
- **Problema**: Lógica de exibição de status e ações no cabeçalho.
- **Ação**:
  - Testar todas as variações de props e estados do processo.

#### `src/components/AcaoBloco.vue` (73.91% Linhas)
- **Problema**: Componente genérico de ação, provável falta de cobertura em eventos emitidos.
- **Ação**:
  - Verificar emissão de eventos corretos ao clicar.

### 4. Utilitários e Serviços

Embora com cobertura geral alta, alguns arquivos específicos precisam de ajustes.

#### `src/services/usuarioService.ts` (83.33% Linhas)
- **Problema**: Tratamento de erro ou métodos menos usados.
- **Ação**:
  - Identificar linhas não cobertas e adicionar testes específicos.

#### `src/mappers/processos.ts` (66.66% Branches)
- **Problema**: Mapeamento condicional pode estar falhando em cobrir todos os casos.
- **Ação**:
  - Criar testes de unidade para o mapper com diferentes payloads de entrada.

## Estratégia de Execução

1. **Correção de Testes Existentes**: Garantir que todos os testes atuais passem (já realizado com a correção do `AtividadeItem.spec.ts`).
2. **Priorização por Impacto**: Começar pelas Views (`ProcessoView`, `ConfiguracoesView`) pois representam fluxos principais do usuário e têm a menor cobertura.
3. **Testes de Unidade para Stores**: Fácil ganho de cobertura e garante estabilidade do estado. Focar em `configuracoes.ts` e `subprocessos.ts`.
4. **Refinamento de Componentes**: Atacar componentes isolados (`ProcessoHeader`, `AcaoBloco`) para ganhos incrementais.
5. **Cobertura de Branches**: Revisar utilitários e mappers para garantir que condicionais (`if/else`) sejam testados.

## Ferramentas

- `vitest`: Executor de testes.
- `vue-test-utils`: Para montar componentes.
- `@pinia/testing`: Para mockar stores em testes de componentes.
- Cobertura via `v8` (já configurado).

## Meta Final

Atingir >95% em todas as métricas de cobertura (Statements, Branches, Functions, Lines).
