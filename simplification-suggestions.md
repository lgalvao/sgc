# Sugestões de Simplificação Arquitetural - SGC

Este documento contém recomendações para simplificar a arquitetura e reduzir a complexidade e fragmentação no projeto SGC, focando no contexto de um sistema intranet para 5-10 usuários simultâneos, conforme as diretrizes estabelecidas.

## Diretrizes de Combate ao Overengineering (Sistema Intranet 5-10 Usuários)

Dado o contexto estrito do SGC (intranet, 5-10 usuários simultâneos), muitas diretrizes aplicáveis a sistemas altamente escaláveis e super-modularizados **não se aplicam**. A arquitetura deve privilegiar a simplicidade extrema, a legibilidade direta e a ausência de camadas desnecessárias.

*   **Proibição de Facades e Camadas Pass-Through:** Evite criar `Facades` (como `PainelFacade`, `UsuarioFacade`, ou `AtividadeFacade`). A lógica de negócio deve residir diretamente nos Serviços de domínio. Camadas intermediárias que apenas repassam chamadas aumentam a carga cognitiva e dificultam a manutenção.
*   **Acesso Direto a Repositórios:** Controllers estão **autorizados e encorajados** a acessar diretamente as interfaces de Repositório (`Spring Data JPA`) para operações CRUD básicas ou leituras triviais. Não crie um Serviço apenas para delegar um `findById` ou `findAll`.
*   **Rejeição de Arquiteturas Complexas:** Padrões como Onion Architecture, Hexagonal Architecture (Ports and Adapters) e Clean Architecture estrita adicionam abstrações (interfaces, mapeamentos de domínio-para-entidade) que são injustificadas. Utilize o modelo de Entidades JPA diretamente como o modelo de domínio principal.
*   **Procedural vs Orientação a Objetos Pura:** Para lógicas de negócio diretas, abrace métodos procedurais claros nos Serviços. Evite criar hierarquias complexas de herança, múltiplos Design Patterns (Factories, Builders para objetos simples) ou abstrações genéricas projetadas para casos de uso que não existem.
*   **Consolidação de Serviços:** Evite a "explosão de classes". Agrupe lógicas altamente coesas em um mesmo Serviço. Por exemplo, unifique `SubprocessoTransicaoService` e `SubprocessoValidacaoService` para dentro de `SubprocessoService` em vez de fragmentá-los, a menos que o arquivo se torne intragovernável.
*   **Pinia Stores Essenciais (Frontend):** Reduza o uso de Pinia Stores que atuam apenas como "pass-through" (repassadores) para chamadas de API (`axios`). Utilize Composables simples (e.g., `useProcessos`) ou faça as chamadas diretamente nos componentes/views. O Pinia deve ser reservado para estado genuinamente global e compartilhado.
*   **Wrappers Visuais e Componentes de Pass-through:** Questione a necessidade de componentes Vue que apenas envelopam bibliotecas de UI (como `BButton`) sem adicionar valor semântico ou lógico significativo (e.g., `LoadingButton.vue`). Use os componentes originais sempre que possível para manter a árvore do Vue rasa.
*   **Devolução de Entidades JPA (Leituras Simples):** DTOs continuam importantes para fronteiras externas de mutação (Criação/Atualização), mas para leituras simples (GET) onde não há risco de exposição de dados ou N+1, o Controller pode retornar a entidade JPA diretamente para evitar mapeamentos (boilerplate) redundantes. Se usar DTOs, prefira Java Records estritos.

## Situação Atual (Backend - Java / Spring Boot)

1.  **Consolidação de Serviços do Subprocesso:**
    *   **Problema:** Alta fragmentação e tamanho excessivo nas classes `SubprocessoService` (42KB), `SubprocessoTransicaoService` (33KB), `SubprocessoConsultaService`, etc. A lógica de negócio relacionada a subprocessos está muito dispersa e acoplada.
    *   **Solução:** Consolidar a lógica coesa em serviços de domínio mais diretos.
        *   Avaliar a real necessidade de separar `SubprocessoTransicaoService` e `SubprocessoService`. Uma abordagem mais simples reduz a navegação mental.
    *   **Ação:** Refatorar o pacote `sgc.subprocesso.service` fundindo serviços afins.

2.  **Acesso Direto ao Repositório (CRUD Simples):**
    *   **Problema:** Camadas de serviço atuando apenas como repassadoras.
    *   **Solução:** Permitir que os Controllers (ex: `ProcessoController`, `SubprocessoController`) injetem e utilizem diretamente as interfaces de Repository.

3.  **Remoção de Facades Desnecessárias:**
    *   **Problema:** Facades como `AtividadeFacade` (`backend/src/main/java/sgc/mapa/AtividadeFacade.java`), `PainelFacade`, `AlertaFacade`, `RelatorioFacade`, `LoginFacade`, e `UsuarioFacade` adicionam indireção sem abstração significativa. Elas atuam quase unicamente como "pass-through", transferindo chamadas diretamente aos serviços subjacentes sem adicionar valor claro de negócio.
    *   **Solução:** Mover a lógica da Facade diretamente para os serviços de domínio relevantes. Por exemplo, unificar `LoginFacade` com o serviço de autenticação principal, ou mover operações específicas de `AtividadeFacade` para `MapaManutencaoService`.

## Situação Atual (Frontend - Vue.js / TypeScript)

1.  **Remoção de Wrappers Visuais Finos:**
    *   **Problema:** Componentes como `LoadingButton.vue` são wrappers finos sobre `BButton` (`frontend/src/components/comum/LoadingButton.vue`), adicionando sobrecarga na árvore do Vue sem muita justificativa, e com baixa reusabilidade. A mesma lógica é repetida em diversas views.
    *   **Solução:** Remover `LoadingButton.vue` e usar `<BButton>` nativo diretamente com um `<BSpinner>` na aplicação para manter a árvore de componentes plana e simples. A complexidade do wrapper não compensa seu benefício no contexto de um sistema pequeno.

2.  **Redução de Complexidade em Views:**
    *   **Problema:** Views muito extensas (ex: `ProcessoDetalheView.vue` e `MapaView.vue`).
    *   **Solução:** Extrair lógica reativa complexa para composables específicos da view.