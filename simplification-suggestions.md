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

## Arquitetura e Padrões de Projeto

* **Uso de Interfaces:** Evite a criação de interfaces com apenas uma implementação (ex: `IMeuServico` com `MeuServicoImpl`). Utilize a classe concreta diretamente, a menos que o polimorfismo seja estritamente necessário em tempo de execução.
* **Complexidade vs Procedural:** Prefira código simples e procedural nos `Services` em vez de aplicar padrões de projeto altamente abstraídos (como Command, Strategy, etc.) de forma prematura e desnecessária.
* **Excesso de DTOs:** Evite mapeamentos sucessivos e excessivos de DTOs. Onde for prático e seguro, especialmente em operações de leitura simples, retorne os dados com o mínimo de transformações.

## Estrutura de Repositório e Módulos

* **Monolito Coeso:** É desencorajada a tentativa de dividir o back-end em microsserviços ou múltiplos subprojetos granulares no Gradle. O SGC deve permanecer um Monolito Coeso, dada a sua base de usuários pequena.
* **Fragmentação de Arquivos:** Evite fragmentar lógica altamente relacionada em dezenas de arquivos minúsculos. Um arquivo coeso ligeiramente maior é preferível a múltiplos arquivos pequenos que exigem muita navegação contextual.

## Diretrizes de Front-end

A mesma filosofia de redução de fragmentação e de complexidade desnecessária aplica-se ao front-end:

* **Gerenciamento de Estado Simplificado (Pinia):**
  * Estratégias complexas de gerenciamento de estado global e sincronização de dados/cache local são **overkill** devido ao baixo volume de usuários e acessos esporádicos.
  * **Remoção de Stores Pass-through:** Lojas Pinia que atuam meramente como proxies para chamadas de API ou caches redundantes (como `atividades.ts`, `mapas.ts`, `subprocessos.ts`, `processos.ts` e `usuarios.ts`) adicionam fragmentação e complexidade desnecessárias. Elas devem ser progressivamente removidas ou refatoradas.
  * Em vez de tentar replicar toda a base de dados em memória local do navegador, opte por chamadas diretas aos Services usando estado local (ex: `refs` ou `composables`) atrelado estritamente à view/componente atual.
  * **Nova Regra:** O Pinia deve ser reservado estritamente para **estado verdadeiramente global** (ex: dados de sessão do usuário logado, estado de autenticação, notificações/toasts globais da UI).
* **Fim dos Mapeadores Manuais:**
  * Os diretórios e arquivos de `mappers` (anteriormente em `frontend/src/mappers/`) foram removidos.
  * **Nova Regra:** Os Services do front-end agora lidam diretamente com os DTOs crus (Raw DTOs) retornados pelo back-end, contendo o mínimo de lógica de transformação e mapeamento interno. A view/componente deve se adaptar ao formato de transporte.
* **Consolidação de Serviços Frontend:**
  * O combate à fragmentação inclui juntar lógicas altamente relacionadas para evitar importações e injeções cruzadas desnecessárias.
  * Exemplo prático: Os serviços `mapaService.ts` e `analiseService.ts` foram unificados no `subprocessoService.ts`.
* **Componentes Wrapper Desnecessários:** Evite criar componentes "pass-through" (wrappers) que apenas repassam propriedades e eventos para outro componente sem adicionar lógica ou valor real. Use o componente original.
* **Estado Local e Composables:** Reforçando o ponto do Pinia: utilize `ref`s e a Composition API localmente dentro de componentes, ou agrupe lógica em `composables` (`useFuncao.ts`), em vez de recorrer a estados globais complexos para gerenciar variáveis da tela atual.
