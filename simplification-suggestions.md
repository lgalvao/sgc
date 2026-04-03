# Sugestões de Simplificação para o SGC

Considerando o contexto do sistema (uma aplicação de intranet com uso simultâneo de no máximo 5 a 10 usuários), muitos padrões recomendados para sistemas altamente escaláveis e complexos acabam introduzindo **complexidade acidental**. O objetivo deve ser reduzir camadas e manter o fluxo o mais direto possível.

Aqui estão as principais oportunidades de desburocratização e simplificação:

## 1. Eliminar a Camada de "Facades" no Backend
O sistema possui classes como `AtividadeFacade`, `PainelFacade`, `AlertaFacade`, `RelatorioFacade`, `UsuarioFacade` e `LoginFacade`. Em uma aplicação de baixo tráfego, as *Facades* frequentemente agem apenas como intermediários redundantes (*pass-through*), repetindo chamadas entre os Controllers e os Services.
* **Ação Recomendada:** Remover os *Facades*. Os *Controllers* devem injetar e chamar diretamente os *Services* (ou até mesmo os *Repositories* para consultas simples).

## 2. Acesso Direto a Repositories pelos Controllers (Para CRUD/Leituras Simples)
Não há necessidade de criar um método no *Service* cujo único propósito seja chamar `repository.findAll()` ou `repository.findById()`.
* **Ação Recomendada:** Para operações de leitura trivial (onde não há regra de negócio, autorização complexa ou transacionalidade que justifique), permita que os *Controllers* acessem os *Repositories* do Spring Data diretamente.

## 3. Minimizar Mapeamentos DTO Desnecessários
O uso rigoroso de DTOs para todas as requisições e respostas adiciona verbosidade. Em consultas onde a entidade JPA não possui atributos sensíveis ou ciclos de dependência (`@OneToMany` pesados que possam causar problemas de serialização via Jackson), o retorno direto da entidade pode ser perfeitamente adequado e mais rápido de manter.
* **Ação Recomendada:** Evite criar DTOs e *Mappers* para operações de leitura puras. Retorne as entidades diretamente onde for seguro e prático. Mantenha DTOs apenas como objetos de comando (*Requests*) ou para composição de dados complexos que englobam múltiplas entidades.

## 4. Evitar a Fragmentação de Serviços
Dividir domínios simples em muitos serviços granulares (ex: `SubprocessoTransicaoService`, `SubprocessoValidacaoService`, `SubprocessoService`) aumenta o acoplamento e dificulta o rastreamento da regra de negócio, forçando navegação excessiva entre arquivos.
* **Ação Recomendada:** Consolidar lógicas coesas no mesmo serviço de domínio. Use classes de serviço mais robustas e procedurais ao invés de fragmentá-las artificialmente.

## 5. Abandono de "Clean Architecture" / "Hexagonal" Rígida
Para 5-10 usuários, isolar totalmente as entidades do banco de dados das regras de negócio gera um esforço que não se paga.
* **Ação Recomendada:** Manter uma arquitetura monolítica modular simples. Use anotações JPA diretamente nos modelos de domínio e deixe que o *framework* (Spring Boot + Hibernate) cuide de gerenciar o estado transacional. Não crie *Ports*, *Adapters* ou *UseCases* puristas.

## 6. Remover Wrappers Visuais "Finos" no Frontend
Componentes Vue que agem puramente como *proxies* para componentes de bibliotecas baseadass geram um overhead mental. Um exemplo é o `LoadingButton.vue`, que apenas embrulha um `BButton` com um `BSpinner`.
* **Ação Recomendada:** Avaliar o abandono de abstrações rasas como essas e usar diretamente os componentes nativos (`BButton` com *spinner*) nos formulários e *views*.

## 7. Eliminar Interfaces de Implementação Única
Criar uma interface (ex: `UsuarioService`) para ter apenas uma implementação (`UsuarioServiceImpl`) é uma herança do Java antigo (pré-Spring Boot e proxies baseados em classes).
* **Ação Recomendada:** Usar classes concretas diretamente. O Spring consegue criar *proxies* (CGLIB) em classes concretas sem a necessidade de interfaces. Caso existam pacotes com interfaces que só possuem uma classe de implementação, elas devem ser removidas.

## 8. Cuidado com Stores Intermediários e "Pass-through" no Pinia
Muitas vezes, *Stores* do Pinia são criados apenas para manter um estado global espelho das respostas da API, o que introduz um "segundo estado da verdade" e complicações com invalidação de cache.
* **Ação Recomendada:** Se o dado não precisa ser reativo em múltiplas páginas simultaneamente ou não há necessidade de retenção entre rotas, prefira buscar (*fetch*) os dados diretamente dentro do `<script setup>` do componente correspondente (ou *composable* local), sem armazená-los obrigatoriamente no Pinia.

## Conclusão
A arquitetura deve priorizar a **legibilidade procedimental** e a **rastreabilidade direta**. A melhor resposta à pergunta "O que esse botão faz?" é poder clicar no código e chegar à regra de banco de dados no menor número de saltos possível.
