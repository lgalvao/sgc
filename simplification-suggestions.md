# Sugestões de Simplificação Arquitetural - SGC

Este documento contém recomendações para simplificar a arquitetura e reduzir a complexidade e fragmentação no projeto SGC. O sistema é voltado para uso interno (intranet) por no máximo 5 a 10 usuários simultâneos. Nesse contexto restrito, regras de design de software pensadas para alta escalabilidade, distribuição e modularização agressiva constituem **overengineering** e devem ser evitadas.

## Diretrizes de Combate ao Overengineering (Sistema Intranet 5-10 Usuários)

A arquitetura deve privilegiar o "caminho mais curto" entre a requisição e o banco de dados. Menos camadas significam menos código para ler, entender e manter.

1.  **Acesso Direto a Repositórios a partir dos Controllers:**
    *   **Diretriz:** Controllers estão **autorizados e encorajados** a acessar as interfaces `Spring Data JPA` diretamente para operações CRUD básicas, listagens ou verificações pontuais.
    *   **Justificativa:** Criar um `Service` que apenas chama `repo.findById()` ou `repo.findAll()` não adiciona valor de negócio, apenas "pass-through" desnecessário e boilerplate.

2.  **Proibição de Facades (Backend):**
    *   **Diretriz:** Padrões como Facades não devem ser utilizados. A lógica de negócio deve pertencer aos Serviços de domínio. Se um serviço precisar chamar outro, ele deve injetá-lo diretamente ou as lógicas devem ser fundidas, evitando camadas orquestradoras sem comportamento próprio.
    *   **Justificativa:** Facades muitas vezes se tornam gargalos ou apenas repassam chamadas, inflando o número de classes necessárias para acompanhar o fluxo mental de uma requisição.

3.  **Consolidação de Serviços (Evite a Fragmentação):**
    *   **Diretriz:** Agrupe lógicas coesas da mesma entidade em um único `Service`.
    *   **Justificativa:** Múltiplos serviços muito granulares (ex: `ValidacaoService`, `TransicaoService`, `ConsultaService` para a mesma entidade) diluem o fluxo e exigem injeções circulares e muita navegação. Métodos privados dentro de uma classe maior (e bem estruturada) são preferíveis à divisão excessiva em dezenas de classes.

4.  **Devolução de Entidades JPA (Sem DTOs para Leituras Simples):**
    *   **Diretriz:** Para endpoints simples de leitura (`GET`) onde não há exposição de senhas, dados sensíveis ou perigos de *N+1*, é perfeitamente aceitável retornar a entidade do Hibernate diretamente.
    *   **Justificativa:** Mapeadores e DTOs excessivos geram trabalho braçal. Se usar DTOs, prefira `Java Records`. Bibliotecas genéricas de mapeamento e Mappers explícitos podem ser cortados na maioria dos casos simples.

5.  **Rejeição de Clean Architecture, Hexagonal ou Onion:**
    *   **Diretriz:** O modelo do Hibernate (`@Entity`) é o nosso único modelo de domínio. Não devem ser criadas separações artificiais entre Entidades do Banco de Dados e Objetos de Domínio Puros.
    *   **Justificativa:** O benefício de trocar o banco de dados futuramente não compensa o custo imediato e permanente de manter múltiplos modelos paralelos.

6.  **Stores do Pinia (Frontend) Apenas para Estado Global:**
    *   **Diretriz:** Não crie Stores do Pinia apenas para centralizar chamadas `axios` ou para uso em apenas uma tela.
    *   **Justificativa:** Se a requisição não impacta a sessão de outras telas, faça a chamada usando um simples *Composable* Vue (`useFetch`, `useProcessos`) ou mesmo métodos simples no próprio componente `.vue`.

7.  **Evitar Wrappers Visuais Finos (Frontend):**
    *   **Diretriz:** Não crie componentes no Vue apenas para encapsular uma prop do Bootstrap. Use as tags da biblioteca (`BootstrapVueNext`) nativamente sempre que possível.
    *   **Justificativa:** Mantém a árvore do Virtual DOM rasa e reduz a quantidade de arquivos para manter.

---

## Foco de Ação: Situação Atual no Backend

Com base no código da aplicação, há vários pontos de melhoria alinhados a estas diretrizes:

### 1. Eliminação de Facades Desnecessárias

Foram identificadas diversas classes com o sufixo `Facade` que consistem principalmente em código de orquestração pass-through.
*   **Problema:** Em `AtividadeController.java`, a injeção é em `AtividadeFacade.java`. A facade apenas delega chamadas para `MapaManutencaoService` (ex: `mapaManutencaoService.atividadeCodigo(codAtividade)`). Além de `AtividadeFacade`, existem `LoginFacade`, `PainelFacade`, `AlertaFacade`, `RelatorioFacade` e `UsuarioFacade`.
*   **Ação Recomendada:** Remover essas classes `*Facade.java`. A lógica de autorização presente nas Facades pode ser tratada através de `@PreAuthorize` nos Controllers ou consolidada no Serviço principal. Os Controllers podem depender diretamente de `MapaManutencaoService` ou `UsuarioService`.

### 2. Fragmentação Extrema: O Domínio de Subprocesso

*   **Problema:** A pasta `backend/src/main/java/sgc/subprocesso/service` abriga uma explosão de classes separadas:
    *   `SubprocessoService.java` (19KB)
    *   `SubprocessoTransicaoService.java` (32KB)
    *   `SubprocessoConsultaService.java` (21KB)
    *   `SubprocessoValidacaoService.java` (10KB)
    *   `SubprocessoNotificacaoService.java`
    *   `SubprocessoSituacaoService.java`
*   **Ação Recomendada:** Proceder com uma consolidação radical. Integrar métodos de Validação e Transição ao `SubprocessoService`. Para as consultas, o Controller poderia até mesmo bater direto em `SubprocessoRepository`. Deve-se unificar as lógicas que mutam estado para dentro de um `SubprocessoService` unificado e forte. A divisão em "sub-serviços" que injetam um ao outro gera complexidade de ciclo de vida do Spring.

### 3. Remoção de Mapeamentos e Builders Desnecessários
*   **Problema:** A base de código está com dezenas de `Dto.java` e `Request.java` até para cenários rasos, às vezes associados a Builders ou Factory patterns.
*   **Ação Recomendada:** Avaliar quais DTOs são usados apenas de pass-through. Nos métodos `GET` em que não se vaza propriedades (como senhas), migrar o retorno direto para as classes `@Entity`. Onde o DTO for de fato necessário, converter todas as classes simples para *Records*. Evitar instanciar Objetos Puros com Builders complexos se setters funcionam.

---

## Foco de Ação: Situação Atual no Frontend

### 1. Remoção do Wrapper Pass-Through: `LoadingButton.vue`

*   **Problema:** O arquivo `frontend/src/components/comum/LoadingButton.vue` é um mero envelope para o componente nativo `<BButton>` do BootstrapVueNext, encapsulando condicionalmente um `<BSpinner>` com base numa prop `loading`. Isso adiciona uma camada de componente desnecessária em dezenas de referências e propaga *slots* pass-through.
*   **Ação Recomendada:** Eliminar `LoadingButton.vue`. O componente consumiria diretamente:
    ```vue
    <BButton :disabled="loading" ...>
      <BSpinner v-if="loading" class="me-1" small />
      Texto Original
    </BButton>
    ```
    Isso simplifica a árvore de componentes e remove a manutenção do utilitário.

### 2. Substituição de Stores do Pinia

*   **Problema:** Alguns repositórios de Store (como em `frontend/src/stores/subprocessos.ts` ou `perfil.ts`) podem estar atuando de forma síncrona com o servidor ou mantendo dados de cache sem necessidade, replicando responsabilidades que já poderiam estar resolvidas nas chamadas nativas de um Service simples (`authService.ts`, etc).
*   **Ação Recomendada:** Migrar o acesso a rotas específicas da API diretamente em "Composables" puros Vue (ex: `useSubprocessoListagem()`), que mantêm *state* local apenas durante o ciclo de vida do componente, limitando as Stores globais à autenticação/usuário logado, se necessário.

---

O respeito a essas orientações simplificadas garante uma base de código menor e muito mais rastreável para a escala real do sistema, eliminando a carga cognitiva do overengineering.