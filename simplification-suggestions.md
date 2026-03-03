# Sugestões de Simplificação e Anti-Overengineering (SGC)

Este documento centraliza as diretrizes arquiteturais focadas na **simplificação radical** e na prevenção do overengineering no projeto SGC.

## Contexto do Sistema

O SGC é uma aplicação de escopo restrito, projetada para ser utilizada em uma intranet por no **máximo 5 a 10 usuários simultâneos**.

Por conta desse perfil de uso, a grande maioria das diretrizes aplicáveis a aplicações de alta escalabilidade, com múltiplas camadas de abstração e super-modularização **não se aplica**. O foco principal deve ser a manutenibilidade, a clareza do código e a redução de complexidade desnecessária. A adoção de práticas rígidas de separação de camadas deve ser evitada quando isso gerar apenas código boilerplate ou burocracia técnica (ex: mapeamento excessivo de dados entre camadas que já representam o mesmo conceito).

## Diretrizes Gerais e Back-end

* **Remoção de Facades Redundantes:** O padrão inicial de envolver todos os fluxos em classes `*Facade` gerou uma camada pass-through sem valor agregado em diversos módulos.
  * **Concluído:** `SubprocessoFacade` e `AnaliseFacade` já foram removidos, com a lógica sendo tratada diretamente pelo `SubprocessoService`.
  * **A Fazer:** Remover Facades remanescentes que não agregam valor (como `OrganizacaoFacade`, `UsuarioFacade` e `LoginFacade`).
  * **Nova Regra:** Os `Controllers` devem injetar diretamente os `Services` apropriados (ex: `UnidadeService`, `UsuarioService`).
* **Lógica e Validação:** Todas as validações de regras de negócio devem ser movidas dos `Controllers` para os `Services`. O Controller deve manter-se extremamente fino, apenas recebendo a requisição HTTP e repassando para o Service executar a lógica (mantendo coesão).
* **Testes de Integração vs Testes Unitários:** A estratégia de testes do backend favorece fortemente os Testes de Integração (usando `@SpringBootTest` e H2 em memória) sobre os Testes Unitários altamente "mockados" (e frequentemente frágeis). A maioria das suítes de testes complexas já foi migrada para `*IntegrationTest`.

## Diretrizes de Front-end

A mesma filosofia de redução de fragmentação e de complexidade desnecessária aplica-se ao front-end:

* **Gerenciamento de Estado Simplificado (Pinia):**
  * Estratégias complexas de gerenciamento de estado global e sincronização de dados/cache local são **overkill** devido ao baixo volume de usuários e acessos esporádicos.
  * Lojas Pinia excessivamente complexas (como `usuarios.ts` e `atividades.ts`) devem ser refatoradas.
  * Em vez de tentar replicar toda a base de dados em memória local do navegador, opte por chamadas diretas às APIs ou gerenciamento de estado local (`refs`, `composables`) atrelado estritamente à view atual.
* **Fim dos Mapeadores Manuais:**
  * Os diretórios e arquivos de `mappers` (anteriormente em `frontend/src/mappers/`) foram removidos.
  * **Nova Regra:** Os Services do front-end agora lidam diretamente com os DTOs crus (Raw DTOs) retornados pelo back-end, contendo o mínimo de lógica de transformação e mapeamento interno. A view/componente deve se adaptar ao formato de transporte.
* **Consolidação de Serviços Frontend:**
  * O combate à fragmentação inclui juntar lógicas altamente relacionadas para evitar importações e injeções cruzadas desnecessárias.
  * Exemplo prático: Os serviços `mapaService.ts` e `analiseService.ts` foram unificados no `subprocessoService.ts`.
