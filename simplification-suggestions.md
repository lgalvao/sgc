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

## Sugestões de Ação Imediata (Backend - Java / Spring Boot)

1.  **Remoção de Facades Desnecessárias:**
    *   **Problema:** Existem diversas Facades que não orquestram múltiplos domínios ou isolam integrações complexas. Pelo contrário, elas atuam quase puramente como repassadoras (pass-through) para os serviços de domínio. Exemplos encontrados no repositório:
        *   `AlertaFacade` (`backend/src/main/java/sgc/alerta/AlertaFacade.java`)
        *   `UsuarioFacade` (`backend/src/main/java/sgc/organizacao/UsuarioFacade.java`)
        *   `AtividadeFacade` (`backend/src/main/java/sgc/mapa/AtividadeFacade.java`)
        *   `PainelFacade` (`backend/src/main/java/sgc/processo/painel/PainelFacade.java`)
        *   `LoginFacade` (`backend/src/main/java/sgc/seguranca/LoginFacade.java`)
        *   `RelatorioFacade` (`backend/src/main/java/sgc/relatorio/RelatorioFacade.java`)
    *   **Recomendação de Refatoração:**
        *   Transferir a lógica contida nestas classes diretamente para os Serviços correspondentes (ex: mover métodos de `AlertaFacade` para `AlertaService`).
        *   Ajustar os Controllers para injetarem e chamarem os Serviços de Domínio ou, em casos de leitura trivial, as próprias interfaces de Repositório (`Spring Data JPA`).
        *   Deletar as classes Facade do código.

2.  **Consolidação de Serviços Excessivamente Fragmentados:**
    *   **Problema:** Observa-se uma hiper-fragmentação na lógica de negócios de subprocessos. Classes como `SubprocessoService` (aprox. 19KB / 42KB em iterações anteriores) e `SubprocessoTransicaoService` (aprox. 32KB / 33KB) ainda dividem a responsabilidade do ciclo de vida das transições. Esse acoplamento força desenvolvedores a saltarem entre múltiplos arquivos para entender o fluxo único do negócio.
    *   **Recomendação de Refatoração:** Consolidar lógicas coesas. Em sistemas para poucos usuários simultâneos, concentrar operações de ciclo de vida, transição e regras de validação associadas ao `Subprocesso` em um único (ou significativamente menos) domínio coeso é melhor que separar estritamente cada pequena responsabilidade de orquestração em uma injeção de dependência separada.

## Sugestões de Ação Imediata (Frontend - Vue.js / TypeScript)

1.  **Remoção de Wrappers Visuais Pass-Through:**
    *   **Problema:** A existência de componentes como `LoadingButton.vue` (`frontend/src/components/comum/LoadingButton.vue`). Este componente se limita a envelopar o `BButton` (`bootstrap-vue-next`) apenas para repassar propriedades e controlar um `BSpinner`. No contexto de restrição de overengineering, wrappers muito finos acrescentam ruído na árvore do DOM virtual (Vue Devtools) e aumentam o número de arquivos para manter sem agregar abstrações significativas (sem reusabilidade sistêmica de negócio).
    *   **Recomendação de Refatoração:**
        *   Remover o arquivo `LoadingButton.vue`.
        *   Substituir seus usos por `<BButton>` nativo emparelhado com `<BSpinner>` in-line ou diretamente na view onde a reatividade/lógica de carregamento ocorre, achatando a árvore de componentes.