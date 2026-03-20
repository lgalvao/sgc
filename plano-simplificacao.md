# Plano Consolidado de Simplificação

Documento consolidado e confirmado a partir das propostas anteriores e da validação do código atual do projeto.

## Objetivo

Simplificar o monólito sem romper regras já consolidadas do SGC, reduzindo camadas pass-through, mapeamentos desnecessários e fragmentação artificial, mas preservando:

- regras de segurança e autorização;
- contratos de API que ainda dependem de DTOs;
- clareza de responsabilidade entre classes;
- testes e restrições arquiteturais já codificadas no repositório.

## Princípios confirmados

### 1. Backend: remover camadas intermediárias sem valor real

Confirmado:

- Eliminar facades que apenas orquestram chamadas triviais ou duplicam responsabilidades já existentes em services.
- Fazer controllers dependerem diretamente de services de domínio.
- Consolidar lógica onde hoje há excesso de repasse entre classes.

Não aprovado:

- Controllers acessarem `Repo` diretamente.

Justificativa:

- O projeto possui regra arquitetural automatizada que proíbe controllers de acessarem repositories.
- No SGC, services concentram regras de negócio, autorização implícita, composição entre módulos e transações.

Diretriz prática:

- Quando houver service ou facade puramente pass-through, simplificar a cadeia no nível de service, sem pular para repository no controller.

### 2. Backend: reduzir fragmentação excessiva, sem forçar service único

Confirmado:

- Reduzir a fragmentação artificial nos domínios de `Subprocesso` e `Mapa`.
- Unificar classes quando a separação atual só aumenta navegação, acoplamento e custo de manutenção.

Não aprovado como regra absoluta:

- Obrigar cada domínio a terminar em um único `Service`.

Diretriz prática:

- Manter apenas classes com responsabilidade clara e justificável.
- Fundir services quando a divisão atual for burocrática.
- Preservar separações que ainda isolem fluxos complexos ou regras distintas de forma útil.

### 3. Backend: reduzir DTOs em leituras simples

Confirmado:

- Remover mapeamentos manuais desnecessários em retornos simples.
- Preferir expor entidades com `@JsonView` e `@JsonIgnore` quando isso for suficiente e seguro.
- Manter DTOs quando houver:
  - agregação de dados;
  - proteção de campos;
  - prevenção de recursão;
  - necessidade de contrato próprio de API;
  - composição que não deva vazar do modelo de domínio.

Diretriz prática:

- A simplificação deve ser guiada por payload real e risco de exposição indevida, não por remoção indiscriminada de DTO.

### 4. Frontend: remover stores pass-through

Confirmado:

- Stores que só mantêm estado de tela, cache local ou repasse de chamadas para `services/` devem migrar para composables ou estado local do componente.
- Pinia deve ficar concentrado no que é efetivamente compartilhado entre múltiplas áreas da aplicação ou representa estado global durável.

Diretriz prática:

- Para fluxos de página única, preferir `ref`, `reactive` e composables.
- Para autenticação, perfil, feedback global e estados amplamente compartilhados, manter store.

### 5. Frontend: evitar wrappers sem responsabilidade própria

Confirmado:

- Remover componentes que apenas repassam props e eventos para componentes base do BootstrapVueNext sem agregar comportamento, composição visual ou regra de domínio.

Diretriz prática:

- Só manter wrapper quando ele encapsular comportamento reutilizável real, semântica de domínio ou estrutura visual relevante.

### 6. Filosofia arquitetural para o projeto

Confirmado:

- Evitar arquiteturas e padrões que aumentem complexidade sem benefício proporcional ao contexto do SGC.
- Evitar interfaces de implementação única sem motivo concreto.
- Evitar factories, builders e camadas extras quando a solução direta for mais clara.
- Manter o monólito coeso, com responsabilidades visíveis e navegação simples.

## Estado atual validado

Os seguintes pontos foram confirmados no código:

- As facades `PainelFacade`, `UsuarioFacade`, `AlertaFacade`, `RelatorioFacade`, `LoginFacade` e `AtividadeFacade` existem.
- Há fragmentação real em `SubprocessoService`, `SubprocessoTransicaoService`, `SubprocessoValidacaoService`, `SubprocessoNotificacaoService`.
- Há fragmentação real em `MapaVisualizacaoService`, `MapaSalvamentoService`, `ImpactoMapaService`, `MapaManutencaoService` e `CopiaMapaService`.
- `frontend/src/composables/useProcessos.ts` hoje ainda é um wrapper da store `frontend/src/stores/processos.ts`.
- A migração do fluxo de `Processo` no frontend ainda está incompleta.
- O backend já usa `@JsonView` em múltiplos controllers e entidades, então a simplificação por serialização controlada já é compatível com o padrão atual.

## Pendências consolidadas

## 1. Processo: frontend

- Refatorar `frontend/src/composables/useProcessos.ts` para manter estado local e chamar `services/` diretamente, deixando de ser apenas um wrapper da store.
- Migrar os consumidores diretos de `useProcessosStore()` para o composable ou para estado local.
- Atualizar os testes afetados pela remoção da store compartilhada.
- Remover `frontend/src/stores/processos.ts` apenas depois que não houver mais dependências diretas ou indiretas.
- Revisar stores relacionadas que hoje dependem de `processos.ts`, especialmente `frontend/src/stores/subprocessos.ts`.

## 2. Processo: backend

- Revisar o payload público de `Processo`.
- Ajustar serialização com `@JsonView` e `@JsonIgnore` onde houver campo exposto sem necessidade.
- Remover mapeamento manual desnecessário em retornos simples do módulo `Processo`.
- Manter DTOs como `ProcessoDetalheDto` somente onde ainda houver necessidade real de agregação, composição de contexto ou proteção de contrato.

## 3. Backend: consolidação estrutural

- Eliminar facades desnecessárias do backend:
  - `PainelFacade`
  - `UsuarioFacade`
  - `AlertaFacade`
  - `RelatorioFacade`
  - `LoginFacade`
  - `AtividadeFacade`
- Reabsorver as responsabilidades dessas facades em services de domínio ou services de aplicação já existentes.
- Reduzir a fragmentação de serviços nos domínios `Subprocesso` e `Mapa`, mantendo apenas divisões com ganho real de clareza.
- Não introduzir acesso direto de controller a repository durante essa consolidação.

## 4. Frontend: consolidação estrutural

- Auditar e remover stores pass-through que ainda funcionam como estado de tela ou cache local:
  - `frontend/src/stores/mapas.ts`
  - `frontend/src/stores/subprocessos.ts`
  - `frontend/src/stores/configuracoes.ts`
- Auditar componentes wrapper sem lógica própria e substituí-los por uso direto de componentes base quando apropriado.

## Ordem recomendada

1. Concluir a migração do fluxo de `Processo` no frontend, incluindo composable, views, componentes e testes.
2. Ajustar a serialização e os retornos simples do backend de `Processo`.
3. Consolidar facades e reduzir fragmentação artificial nos services do backend.
4. Auditar e remover stores pass-through e wrappers restantes no frontend.

## Critérios de conclusão

O plano será considerado concluído quando:

- não houver mais dependência relevante de facades elimináveis;
- o fluxo de `Processo` no frontend não depender mais de `frontend/src/stores/processos.ts`;
- os retornos simples do backend tiverem sido simplificados sem exposição indevida de campos;
- `Subprocesso` e `Mapa` não estiverem mais divididos em services artificiais sem ganho claro;
- testes de backend e frontend cobrirem os fluxos afetados pela simplificação.
