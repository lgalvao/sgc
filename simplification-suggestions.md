# Sugestões de Simplificação do SGC

## Contexto e Premissas do Sistema

O Sistema de Gestão (SGC) opera sob restrições e características muito específicas que devem guiar todas as decisões de arquitetura e design de código:

- **Escopo Operacional:** Sistema intranet, de uso estritamente interno.
- **Volume de Acesso:** Baixíssima concorrência. Foi projetado para suportar no máximo de 5 a 10 usuários simultâneos.
- **Arquitetura Base:** Modular monolith (Monolito Modular).
- **Necessidade de Escalabilidade:** Praticamente nula em termos de tráfego web massivo ou processamento distribuído.

Diante deste cenário, **inúmeras diretrizes de engenharia de software focadas em aplicações de altíssima escalabilidade, arquiteturas orientadas a eventos, microserviços, e padrões como Onion/Hexagonal Architecture (Clean Architecture) super-modularizados simplesmente não se aplicam e são contraproducentes**.

O principal objetivo de design para este projeto deve ser a **simplicidade absoluta, clareza direta e facilidade de manutenção ("franqueza" do código)**. O código deve ser procedural onde faz sentido e ir direto ao ponto, evitando camadas artificiais.

Abaixo, detalhamos os principais focos de overengineering identificados no código atual e como simplificá-los.

---

## 1. Eliminação de Facades no Backend

**Diagnóstico:**
O sistema conta com diversas classes com o sufixo `*Facade.java`, tais como:
- `AtividadeFacade.java`
- `PainelFacade.java`
- `AlertaFacade.java`
- `RelatorioFacade.java`
- `LoginFacade.java`
- `UsuarioFacade.java`

**O Problema (Overengineering):**
Em um sistema deste porte (5-10 usuários), o padrão Facade foi aplicado de forma teórica. Em vez de simplificar a interação com subsistemas complexos, ele atua majoritariamente como uma camada de repasse inútil ("pass-through") entre `Controllers` e `Services`. Isso esconde a real lógica, força o desenvolvedor a abrir múltiplos arquivos para acompanhar uma chamada simples, e infla a base de código e os testes de unidade sem nenhum benefício prático.

**Ação de Simplificação:**
- **Remover as `Facades` completamente.**
- Se uma classe Facade realiza uma coordenação simples entre dois Services (ex: chamar `AlertaService` após `RelatorioService`), mova essa lógica diretamente para o `Controller` ou para o `Service` que detém o contexto primário da operação.
- Consolidar a regra de negócio nos Services e permitir que os Controllers interajam diretamente com eles.

---

## 2. Consolidação e Desfragmentação de Services (Ex: `subprocesso`)

**Diagnóstico:**
O domínio de negócio de "subprocesso" sofre de uma hiper-fragmentação, dividindo-se em pequenos e grandes serviços:
- `SubprocessoNotificacaoService.java`
- `SubprocessoTransicaoService.java` (Muito grande)
- `SubprocessoSituacaoService.java`
- `SubprocessoValidacaoService.java`
- `SubprocessoConsultaService.java`
- `SubprocessoService.java`

**O Problema (Overengineering):**
Esta é uma manifestação do "Single Responsibility Principle" levado ao extremo, fragmentando o fluxo de negócio. Quando `SubprocessoService` precisa chamar `SubprocessoValidacaoService` e `SubprocessoSituacaoService` apenas para validar uma regra trivial de negócio e atualizar o status, criam-se teias de injeção de dependências (`@Autowired` ou construtores gigantes) e o rastreamento do código fica exaustivo.

**Ação de Simplificação:**
- **Fundir serviços altamente acoplados.** `SubprocessoValidacaoService` e `SubprocessoSituacaoService` não possuem razão para existir de forma isolada; eles devem ser consolidados dentro do `SubprocessoService` (ou `SubprocessoTransicaoService`, dependendo da coesão).
- Validações específicas não precisam de uma Classe de Serviço injetável. Use **métodos privados** dentro do serviço principal ou aplique a lógica como métodos ricos diretamente na entidade de Domínio (`Subprocesso.java`), aproveitando o paradigma de POJOs simples.
- Manter o código linear e coeso em um lugar, preferindo arquivos de 300-500 linhas a 5 arquivos de 100 linhas fragmentados.

---

## 3. Acesso Direto de Controllers a Repositories para CRUD Básico

**Diagnóstico:**
O padrão atual tende a forçar o fluxo rígido: `Controller -> Service -> Repository`, independentemente da complexidade da operação.

**O Problema (Overengineering):**
Para operações de listagem simples (GETs para preencher dropdowns) ou consultas que não possuem nenhuma lógica de negócio além da própria query (ex: buscar um detalhe via ID), a camada de Serviço atua apenas como um "pass-through". Isso gera boilerplate inútil.

**Ação de Simplificação:**
- **Para operações simples de leitura, o Controller pode (e deve) chamar o Spring Data Repository diretamente.**
- Pode-se retornar os dados mapeados diretamente para DTOs ou utilizar projeções do banco de dados (Spring Data Projections).
- Esta abordagem, embora viole as regras estritas da *Clean Architecture*, é perfeitamente adequada, segura e recomendada para um sistema intranet de 5 usuários, economizando tempo de desenvolvimento e linhas de código mortas.

---

## 4. Simplificação de Views Complexas no Frontend

**Diagnóstico:**
O frontend em Vue apresenta arquivos `.vue` excessivamente monolíticos:
- `CadastroView.vue`
- `MapaVisualizacaoView.vue`
- `ProcessoDetalheView.vue`

**O Problema (Overengineering):**
Apesar da intenção de simplificar o backend, o frontend assumiu muita responsabilidade num único arquivo. A view lida com chamadas de API, mutações da store do Pinia, lógica reativa complexa, formatações de dados, regras de condição para exibição e a montagem do template propriamente dita. Isso torna o debug doloroso e a componentização nula.

**Ação de Simplificação:**
- **Extrair a lógica reativa e de controle para *Composables* (`use...`).**
- Criar arquivos como `useProcessoDetalhe.ts` para cuidar do fetch, tratamento de erros e gestão de estado local.
- O arquivo `.vue` deve ser focado puramente na camada de Apresentação (o Template UI) e na passagem de dados.
- Avaliar a remoção de Stores do Pinia se elas agem apenas como "pass-through" para a API e o estado não precisa ser compartilhado globalmente entre diferentes rotas. Use o estado local do Composable.

---

## 5. Auditoria e Remoção de Wrappers Visuais Finos

**Diagnóstico:**
O projeto possui componentes Vue como `LoadingButton.vue` e `TreeTable.vue`.

**O Problema (Overengineering):**
Muitas vezes, a equipe cria wrappers em torno de componentes de bibliotecas externas (como BootstrapVueNext) apenas para adicionar uma propriedade (como um estado de loading). Esses "wrappers finos" escondem a API rica do componente original, bloqueando o acesso a slots nativos e eventos, forçando atualizações no wrapper a cada nova necessidade de UI.

**Ação de Simplificação:**
- **Fazer uma triagem rigorosa de wrappers.** O componente `LoadingButton.vue`, por exemplo, deve ser reavaliado. Se a biblioteca de UI base já suporta loading ou se ele pode ser resolvido com um simples componente nativo `BButton` e diretivas Vue, o wrapper deve ser removido em prol do uso direto do componente da biblioteca.

---

## Conclusão e Diretriz de Ouro

Sistemas internos para poucos usuários devem otimizar a velocidade de manutenção e o baixo acoplamento cognitivo.

**A regra de ouro é: "O código deve dizer o que faz e fazer o que diz, no mesmo lugar".**

Evite:
- Padrões de projeto apenas pela teoria (Factories sem variação, Builders para objetos rasos, Facades).
- Interfaces com apenas uma implementação real.
- Camadas de abstração que não tomam decisões lógicas.

Priorize:
- Código linear e procedural em `Services`.
- Entidades de domínio claras.
- Redução agressiva de arquivos intermediários.
