# Sugestões de Simplificação Arquitetural - SGC

Este documento apresenta uma análise de sobre-engenharia e fragmentação excessiva no código do SGC, considerando que o sistema é uma aplicação para intranet com um baixo volume de acessos simultâneos (máximo de 5 a 10 usuários).
Diretrizes aplicáveis a sistemas em larga escala não se justificam aqui, e a complexidade arquitetural pode ser consideravelmente reduzida.

## 1. Backend

O backend (Java/Spring Boot) apresenta um alto grau de fragmentação de serviços e a adoção de padrões que apenas delegam chamadas, resultando em complexidade ciclomática elevada e código boilerplate desnecessário.

### 1.1 Excesso de Fragmentação em Serviços (`sgc.subprocesso.service`)
A lógica de negócio de `Subprocesso` está distribuída por múltiplos serviços pequenos:
* `SubprocessoNotificacaoService`
* `SubprocessoSituacaoService`
* `SubprocessoTransicaoService`
* `SubprocessoValidacaoService`
* `SubprocessoService`

**Recomendação:**
Dado o escopo reduzido da aplicação, essa separação granular aumenta a dificuldade de rastrear fluxos de negócio. Esses serviços paralelos devem ser consolidados de volta para o `SubprocessoService`, mantendo os métodos privados bem estruturados. A separação só se justificaria se as classes tivessem milhares de linhas ou responsabilidades radicalmente distintas que exijam injeção condicional.

### 1.2 Camadas de "Facade" Desnecessárias
Existem várias classes de fachada que funcionam primariamente como *pass-throughs* para delegar métodos entre *Controllers* e *Services* sem agregar lógica de negócio substancial.
* `UsuarioFacade` (delega as operações para `UsuarioService` e `ResponsavelUnidadeService`)
* `PainelFacade` (atua como orquestrador simples para listar processos e alertas)
* `AlertaFacade`

**Recomendação:**
Remover essas classes de fachada. O Controller (ex: `PainelController`, `UsuarioController`) deve orquestrar chamadas de múltiplos serviços caso necessário (ex: chamar `UsuarioService` para buscar o perfil e `ResponsavelUnidadeService` de maneira direta) ou mesmo interagir diretamente com Repositórios para consultas simples de leitura (ex: Listagens CRUD). A camada de Facade só agrega valor em integrações externas complexas ou sistemas massivos (ex: Strangler Fig).

### 1.3 Acesso Direto a Repositórios
Muitos Controllers passam por Services apenas para realizar operações simples como `findById` ou `findAll`.

**Recomendação:**
Permitir que Controllers injetem e chamem Repositórios Spring Data (`JpaRepository`) diretamente para as operações CRUD simples ou buscas isoladas de banco de dados. Criar métodos no Service apenas para fluxos de negócio que exigem validações complexas, regras de transição de estado, notificações (envio de email) e persistências transacionais acopladas a várias entidades.

## 2. Frontend

No frontend (Vue/TypeScript), a lógica está excessivamente dividida entre Stores do Pinia, Composables e chamadas diretas a Serviços, gerando certa duplicidade e código pass-through.

### 2.1 Lojas Pinia Pass-through (`src/stores`)
As lojas do Pinia como `configuracoes.ts` atuam apenas como um wrapper para encapsular chamadas ao `configuracaoService`. Elas guardam pouco estado útil global, utilizando apenas um array de referências sendo sincronizado com a API esporadicamente. O mesmo ocorre com os *composables* que apenas delegam estado sem lógica complexa (ex: `useMapas.ts`).

**Recomendação:**
O Pinia deve ser utilizado apenas para estados verdadeiramente globais e reativos da aplicação que muitas páginas ou componentes em locais distantes da árvore precisam acessar e modificar em tempo real (ex: dados temporários de navegação, usuário autenticado de uma master view).
Para gerenciar cache de configurações simples ou chamadas de API pontuais, utilize diretamente *Composables* (`useAsyncAction` em conjunto com a injeção do service) nas Views que necessitam dessa informação, eliminando a dependência de Stores.

### 2.2 Componentes de Empacotamento Pobre
A busca por modularização pode levar a criação de pequenos componentes Vue.js cujo único propósito seja repassar *props* ou *events* para um componente UI subjacente (como um wrapper simples do BootstrapVueNext) sem adicionar novos comportamentos.

**Recomendação:**
Substituir componentes "pass-through" e focar na construção direta nas Views usando componentes do `BootstrapVueNext`, a não ser que exista de fato o encapsulamento de uma lógica de regra de apresentação (condicionais, computações) de negócio ou UI corporativa complexa e repetitiva.

## 3. Resumo da Ação

Para uma equipe focada num sistema interno com poucos usuários simultâneos:
1. **Unificar lógica de negócio:** Mescle serviços fragmentados no backend que tratem da mesma entidade principal.
2. **Remova os "Middlemen":** Apague Facades; faça o Controller falar com os Serviços ou diretamente com Repositórios para consultas de leitura.
3. **Pinia apenas quando estritamente necessário:** Reduza as stores que são *wrappers* de serviços Axios no Frontend e direcione para Composables pontuais.

## 4. Evitar Padrões Excessivos e Foco em Simplicidade

Dado o escopo restrito do sistema (intranet, poucos usuários), a utilização de design patterns avançados ou camadas adicionais de abstração de persistência pode ser contraproducente.

* **Evitar Interfaces de Uso Único**: Em Java, é comum a prática de criar uma interface `IUserService` e implementar como `UserServiceImpl`. A menos que existam múltiplas implementações reais (ex: polimorfismo genuíno, ou necessidade forte para testes onde Mockito não atende), deve-se usar a classe concreta diretamente.
* **Simplificar Mapeamentos (DTOs)**: Evitar ao máximo bibliotecas complexas de mapeamento como MapStruct ou Dozer para casos simples. Quando os DTOs são muito similares à entidade, métodos de leitura simples (ex: listagens) do Controller podem retornar as entidades JPA diretamente (se devidamente configuradas as exclusões de laços no JSON com `@JsonIgnore`), reduzindo boilerplate de mapeamento. Se um DTO for necessário, métodos estáticos `fromEntity` ou "Records" nativos do Java 17+ são mais legíveis.
* **Monolito Coeso**: Não há justificativa para fragmentação em microsserviços ou até mesmo uma estrutura excessivamente dividida de pacotes que emula sub-módulos independentes sem real necessidade.
* **Manter Hexagonal e Clean Architecture Fora do Escopo**: Para a maioria dos endpoints e telas, um simples fluxo de 3 camadas (Controller -> Service -> Repository) ou até 2 camadas (Controller -> Repository para CRUD simples) é mais que suficiente e fácil de manter.
