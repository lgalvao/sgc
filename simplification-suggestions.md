# Sugestões de Simplificação do SGC

Considerando as restrições do projeto:
- Sistema intranet
- No máximo 5-10 usuários simultâneos
- Modular monolith
- Baixa complexidade/escalabilidade necessária

Muitas diretrizes voltadas para sistemas de altíssima escalabilidade, microserviços, onion/hexagonal architecture e super modularização não se aplicam a este contexto. O objetivo principal deve ser **simplicidade, clareza e franqueza**. O código deve ir direto ao ponto.

Abaixo, algumas sugestões baseadas na exploração da base de código:

## 1. Eliminação de Facades no Backend

**Diagnóstico:** O sistema conta com múltiplas classes `*Facade.java`:
- `AtividadeFacade.java`
- `PainelFacade.java`
- `AlertaFacade.java`
- `RelatorioFacade.java`
- `LoginFacade.java`
- `UsuarioFacade.java`

**Problema:** Em um sistema desse escopo, o padrão Facade costuma introduzir apenas uma camada de repasse inútil ("pass-through") entre Controllers e Services, sem adicionar regra de negócio ou benefício real. Isso quebra a legibilidade (o desenvolvedor precisa abrir um arquivo a mais só para ver a chamada de repasse) e infla o número de testes sem ganho prático de qualidade.

**Sugestão:**
- Excluir as `Facades`.
- Se a Facade estava apenas unindo dois Services diferentes (ex: Orquestração de Alerta e Relatorio), essa pequena orquestração pode ficar diretamente no Controller, ou em um dos Services principais.
- Controllers podem (e devem, nesse cenário) acessar os Services (ou até mesmo Repositories para CRUDs simples) diretamente.

## 2. Desfragmentação de Services (`subprocesso`)

**Diagnóstico:** O domínio de `subprocesso` está altamente fragmentado em vários services menores:
- `SubprocessoNotificacaoService.java`
- `SubprocessoTransicaoService.java` (Muito grande: 873 linhas)
- `SubprocessoSituacaoService.java`
- `SubprocessoValidacaoService.java`
- `SubprocessoConsultaService.java` (Médio: 571 linhas)
- `SubprocessoService.java` (Médio: 497 linhas)

**Problema:** Essa separação forçada baseada em "padrões de modularização extrema" dificulta seguir um fluxo de negócio. Muitas vezes, um service chama o outro apenas para validações triviais, criando ciclos de dependência ou injetando vários services. O `plano-simplificacao.md` já aponta esforços nessa área.

**Sugestão:**
- Consolidar serviços menores que lidam com a mesma Entidade e regras altamente acopladas. Ex: Fundir `SubprocessoValidacaoService` com `SubprocessoService` ou `SubprocessoTransicaoService` (após enxugar este último extraindo métodos/funções privadas concisas).
- No lugar de criar Classes de Serviço separadas para validações simples (ex: `SubprocessoValidacaoService`), use métodos privados dentro do serviço principal ou métodos de domínio dentro da própria entidade JPA (`Subprocesso`), o que aproxima os dados do comportamento de forma rica e sem injeção de dependências pesadas.

## 3. Acesso Direto de Controllers a Repositories para CRUD Básico

**Diagnóstico:** O projeto tende a forçar a passagem Controller -> Service -> Repository mesmo quando não há lógica de negócio envolvida (ex. buscar dados ou listas simples para UI).

**Problema:** Muito "boiler plate" inútil.

**Sugestão:**
- Quando for apenas uma leitura (GET) e não houver orquestração complexa de regras ou restrições que já não estejam nas queries, permitir que o Controller (ou consulta específica) chame o Spring Data Repository diretamente. Retornar DTOs de leitura ou projeções do DB. O mapeamento direto economiza linhas de código e não fere a mantenabilidade de sistemas pequenos.
- Isso se opõe abertamente a arquiteturas Onion/Hexagonais, o que é plenamente aceitável neste contexto.

## 4. Simplificação de Views Complexas no Frontend

**Diagnóstico:** Muitas Views no Vue são excessivamente grandes:
- `CadastroView.vue` (674 linhas)
- `MapaVisualizacaoView.vue` (522 linhas)
- `ProcessoDetalheView.vue` (504 linhas)

**Problema:** Concentrar fetch de API, mutação de Pinia stores, lógica reativa, formatação de dados e o template em si cria um acoplamento ruim. O componente fica difícil de testar e debugar.

**Sugestão:**
- Adotar **Composables específicos para a View**. Em vez de ter um `const dados = ref(...)` e funções no `script setup` gigante, crie um `useCadastroView.ts` ou `useProcessoDetalhe.ts` ao lado do `.vue`.
- Mova a lógica de estado, fetch, transformação e regras para esse composable.
- A View (`.vue`) fica focada quase exclusivamente na UI (template) e repasse de dados, simplificando imensamente os testes do front e garantindo manutenibilidade.
- Reduza a dependência de Stores globais (Pinia) quando o estado só importa a uma única View (ex: pass-through Pinia stores). Mantenha o estado local no composable.

## 5. Auditoria de Wrappers Visuais Finos (ex: LoadingButton.vue)

**Diagnóstico:** Existência de componentes "finos" como `LoadingButton.vue` e `TreeTable.vue`.

**Problema:** Muitas vezes o time de frontend cria "wrappers" de componentes do BootstrapVueNext apenas para adicionar um prop de loading, criando componentes cujo contrato é fraco e esconde os slots/events originais (ex: `BButton`).

**Sugestão:**
- Auditoria de Wrappers. Se o `LoadingButton.vue` só faz um `v-if="loading"` e repassa o resto, prefira remover o componente customizado e usar nativamente as capacidades do `BButton` nos lugares de uso, ou ao menos simplificá-lo de forma que ele não mascare os slots e eventos do componente base. O plano atual já prevê esta avaliação.

## Conclusão Geral

A recomendação fundamental é focar em **código procedural em Services** para lógicas complexas e uso de **POJOs/Domain Entities** simples e explícitas. Evitar padrões complexos (Factories sem necessidade, Facades, Builders extensos de objetos simples, Interfaces com apenas 1 implementação) e "limpar" o código retirando camadas artificiais que dificultam achar o lugar exato da execução.
