# Sugestões de Simplificação Arquitetural - SGC

Este documento contém recomendações para simplificar a arquitetura e reduzir a complexidade e fragmentação no projeto SGC, focando no contexto de um sistema intranet para 5-10 usuários simultâneos.

## Diretrizes de Combate ao Overengineering

Muitas diretrizes de sistemas altamente escaláveis não se aplicam a um projeto de intranet restrito como o SGC. A arquitetura deve focar na extrema simplicidade e na ausência de camadas intermediárias.

*   **Acesso Direto a Repositórios:** Controllers estão **autorizados** a injetar e acessar interfaces de Repositório (`Spring Data JPA`) diretamente para operações CRUD ou de busca, sem necessidade de pass-through de serviços.
*   **Abolição de Facades e Camadas Intermediárias:** Não utilizar o padrão `Facade`. Classes cuja única função seja delegar chamadas de Controllers para Services ou coordenar chamadas básicas devem ser extintas. A lógica deve ir para o Controller ou ser absorvida pelo Serviço raiz.
*   **Entidades Diretas vs DTOs:** Para respostas simples de GET, especialmente em estruturas não-complexas sem risco de lazy-loading massivo ou exposição indevida, **Controllers podem retornar Entidades JPA diretamente**. DTOs (preferencialmente `records` no Java) devem ser usados essencialmente para payloads de mutação (Criação/Atualização).
*   **Procedural no Domínio:** Privilegiar a legibilidade procedural. Evitar criar interfaces genéricas com uma única implementação, evitar heranças profundas ou Padrões de Projeto abstratos para lógicas diretas.
*   **Evite Wrappers UI no Frontend:** Eliminar componentes Vue que apenas englobam bibliotecas de UI base (como BootstrapVueNext) sem adicionar valor semântico. Usar a biblioteca base diretamente para manter a DOM e a árvore do Vue planas.
*   **Stores Pinia Enxutas:** Utilizar as Stores estritamente para estado compartilhado em escopo global da aplicação. Fetch de dados e regras de tela devem viver em Composables específicos da funcionalidade que injetam estado direto onde for preciso.

---

## Situação Atual & Backlog de Simplificação (Backend - Java)

### 1. Extinção das Facades de Pass-Through
As Facades atuais agem predominantemente como camadas de delegação, engordando a base de código e aumentando os níveis de indireção.

**Ação:** Remover as seguintes classes, realocando sua lógica para os Controllers que as chamam ou para os Services adjacentes:
*   `AtividadeFacade.java`
*   `AlertaFacade.java`
*   `PainelFacade.java`
*   `UsuarioFacade.java`
*   `RelatorioFacade.java`
*   `LoginFacade.java`

### 2. Consolidação de Serviços de Subprocesso
O pacote `sgc.subprocesso.service` foi segmentado excessivamente. A divisão pretendia reduzir o tamanho dos arquivos, mas gerou alto acoplamento e fragmentação.

**Problema:** `SubprocessoTransicaoService` tem ~32KB e `SubprocessoService` tem ~19KB, além de outros serviços (`ConsultaService`, `ValidacaoService`).
**Ação:** Refatorar, agrupando funções altamente coesas. Centralizar as lógicas de negócio centrais dentro de `SubprocessoService` ou separá-las não de forma mecânica (por tamanho), mas por responsabilidade estrita e isolada.

---

## Situação Atual & Backlog de Simplificação (Frontend - Vue.js)

### 1. Remoção do Wrapper Fino `LoadingButton.vue`
**Problema:** O componente `frontend/src/components/comum/LoadingButton.vue` funciona primariamente como um envelope fino sobre o `<BButton>`. A lógica de estado de `loading` é trivial (exibir spinner e desabilitar) e pode ser tratada nativamente nos componentes ou com um composable genérico de botão (se realmente houver lógica cruzada a preservar).
**Ação:** Substituir todas as importações de `LoadingButton` por `<BButton>` nos arquivos `.vue` (ex: `CadastroView`, `ParametrosView`, `AtividadesCadastroView`, etc). Excluir o arquivo `LoadingButton.vue` e seus respectivos arquivos de teste/história (`LoadingButton.spec.ts`, `LoadingButton.stories.ts`).

### 2. Substituição Progressiva do Store de Processos por Composables
A lógica que só é utilizada na árvore de componentes de `Processo` (e que não necessita ser retida quando o usuário navega para outras seções do sistema) deve abandonar o `Pinia` e migrar para funções com estado utilizando Vue Composables, instanciados nos níveis adequados.

---

## Objetivos e Métricas de Conclusão
- Redução quantificável da contagem de arquivos totais ao mesclar as `Facades`.
- Redução na profundidade da árvore do Vue DevTools após remover wrappers finos de UI.
- Garantir que todos os testes passem (manter `coverage` como base de segurança da refatoração).