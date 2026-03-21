# Sugestões de Simplificação Arquitetural

Este documento consolida as diretrizes para evitar *overengineering* e excesso de complexidade no SGC.

## Contexto do Sistema

O SGC é uma ferramenta projetada para uso interno em uma intranet, atendendo a um número muito limitado de usuários simultâneos (no máximo 5 a 10 pessoas). Portanto, a imensa maioria dos padrões de projeto voltados para aplicações altamente escaláveis, multicamadas, ou super modularizadas **não se aplicam** a este projeto.

O foco absoluto deve ser na **simplicidade, clareza e código direto**.

## Diretrizes para o Backend

* **Fim de Interfaces com Implementação Única:** Evite criar interfaces genéricas (ex: `IUsuarioService`) quando haverá apenas uma implementação concreta. Use a classe concreta diretamente.
* **Prefira Código Procedural nos Services:** Concentre as regras de negócio em métodos sequenciais e claros nos Services, em vez de espalhá-las através de padrões de design complexos que dificultam a leitura do fluxo.
* **Minimize o Mapeamento de DTOs:** Em operações de leitura simples, evite mapeamentos manuais e repetitivos de DTOs. Prefira retornar as próprias Entidades (utilizando `@JsonView` e `@JsonIgnore` do Jackson para proteger campos sensíveis) a menos que o retorno agregue dados de múltiplas fontes ou exija uma transformação complexa. (ex: utilizar as próprias classes de domínio como `Processo`, restringindo quais campos expor com `@JsonView(ProcessoViews.Publica.class)` como já é feito em `ProcessoController`).
* **Mantenha um Monólito Coeso:** O sistema é um monólito. Evite fragmentá-lo em microserviços ou subprojetos Gradle excessivos. A coesão e facilidade de navegação no código valem mais do que uma separação artificial.
* **Evite Clean/Hexagonal/Onion Architecture:** Não introduza portas e adaptadores. Use anotações JPA diretamente nos modelos de domínio.
* **Acesso Direto a Repositories:** Para operações CRUD simples, os Controllers podem e devem acessar as interfaces do Spring Data (Repositories) diretamente, sem a obrigatoriedade de passar por um Service, se não houver regra de negócio associada. *(Nota: O SGC originalmente possuía uma regra arquitetural bloqueando isso; a diretriz atual a relaxa para operações puramente CRUD, desde que autorizadas)*.
* **Evite Builders e Factories Desnecessários:** Para objetos simples, utilize construtores padrão ou o recurso de Records do Java. Padrões de criação complexos só devem ser usados se a inicialização do objeto for genuinamente intrincada.
* **Remoção de Facades:** Camadas de Facade que atuam apenas como *pass-through* (ex: `PainelFacade`, `UsuarioFacade`, `AlertaFacade`, `RelatorioFacade`, `LoginFacade`, `AtividadeFacade`) são consideradas *overengineering*. A lógica de orquestração deve ser consolidada diretamente nos Services relevantes de domínio ou de aplicação.
* **Desfragmentação de Services:** Evite granularidade excessiva. Serviços muito específicos, como `SubprocessoTransicaoService` e `SubprocessoValidacaoService`, devem ser fundidos em um único serviço de domínio coeso, como `SubprocessoService`, reduzindo o acoplamento e o excesso de arquivos.

## Diretrizes para o Frontend

* **Remoção de Stores Pinia Pass-through:** Stores do Pinia (ex: `mapas.ts`, `atividades.ts`, `subprocessos.ts`, `processos.ts`, `usuarios.ts`, bem como `configuracoes.ts`) que atuam apenas como repasse de chamadas para APIs ou como cache temporário de dados de tela devem ser removidas. Em vez disso, utilize Composables padrão do Vue (ex: substituir `useConfiguracoesStore` por um `useConfiguracoes()`) ou `refs` locais com chamadas diretas para os arquivos da pasta `services/`.
* **Uso Restrito do Pinia:** O Pinia deve ser reservado exclusivamente para **estado verdadeiramente global e compartilhado** em toda a aplicação (ex: dados de autenticação do usuário logado, permissões globais, sistema de notificações/toasts).
* **Fim de Componentes Wrapper Vazios:** Evite criar componentes Vue que não adicionam nenhuma lógica de domínio ou estilização específica, e servem apenas para repassar `props` e eventos (ex: `v-bind="$attrs" v-on="$listeners"`, visto em `LoadingButton.vue`) para componentes base de bibliotecas de UI (como o BootstrapVueNext). Use os componentes base diretamente nas *views* quando possível.
