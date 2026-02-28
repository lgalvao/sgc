# Sugestões de Simplificação

**Objetivo:** Reduzir o excesso de engenharia ("overengineering") e a fragmentação em um aplicativo de intranet de
pequena escala (5-10 usuários simultâneos).

Embora algumas simplificações já tenham sido aplicadas (como a consolidação de serviços no frontend e a remoção de "
Facades"), ainda existem várias oportunidades para reduzir a carga de manutenção e melhorar a velocidade de
desenvolvimento.

## 1. Simplificação do Frontend (Vue/TypeScript)

### 1.1 Reavaliar o Uso do Padrão Store (Pinia)

O diretório `frontend/src/stores` contém 14 arquivos diferentes (`mapas.ts`, `subprocessos.ts`, `atividades.ts`, etc.).
Para um aplicativo CRUD simples, espelhar o estado do servidor em stores globais complexos geralmente é um exagero e
pode levar a bugs difíceis de "dados obsoletos" (stale data).

**Recomendação:**

- Avalie se o gerenciamento de estado global é realmente necessário para todas essas entidades.
- Se os dados são apenas lidos e ocasionalmente atualizados, considere fazer o fetch diretamente nos componentes ou
  usar "composables" simples.
- Ferramentas como `TanStack Query` (Vue Query) costumam ser melhores para gerenciar cache de estado do servidor do que
  stores customizados do Pinia.

## 2. Simplificação do Backend (Spring Boot)

### 2.1 Extrair Regras de Negócio do Controller

Atualmente, o `SubprocessoController` possui regras de negócio vazando para a camada web. Por exemplo, no endpoint
`/cadastro/disponibilizar`, o Controller está ativamente buscando atividades no banco (
`subprocessoService.obterAtividadesSemConhecimento`) e lançando exceções (`ErroValidacao`) se a regra falhar.

**Recomendação:**

- **Mover validações para o Service:** O Controller deve apenas delegar a intenção do usuário (
  `subprocessoService.disponibilizarCadastro(codSubprocesso, usuario)`).
- **Isolar a Web Layer:** Qualquer verificação de "falta de conhecimento na atividade" ou "situação inválida" deve estar
  encapsulada dentro do método do Service, que por sua vez lança a exceção adequada baseada no Domínio. Isso facilita
  muito escrever testes unitários para as regras de negócio sem envolver o contexto HTTP.

### 2.3 Refinar a Fronteira de Segurança vs. Regra de Negócio

O `SgcPermissionEvaluator` executa verificações complexas de estado de negócio (ex: verificar a `SituacaoSubprocesso` ou
se um Processo está finalizado) para determinar se um usuário tem permissão.

**Recomendação:**

- A camada `@PreAuthorize` deve idealmente responder apenas: "O Usuário X possui a Role/Localização Y para acessar o
  Recurso Z?".
- Regras de negócio como "Posso editar este subprocesso agora?" (que dependem de sua Situacao e sua Localização) devem
  ser aplicadas e validadas dentro da camada de **Service**. Isso torna a lógica de domínio muito mais fácil de testar
  unitariamente fora do contexto do Spring Security.

### 2.4 Mover Validações de Disponibilização para o Service

Atualmente, `SubprocessoController.disponibilizarCadastro` e `disponibilizarRevisao` buscam ativamente por atividades sem conhecimento (`obterAtividadesSemConhecimento`) e lançam `ErroValidacao` com uma lista formatada. Isso vaza a regra de negócio para o Controller.

**Recomendação:**
- Mover a lógica que formata o payload de erro para `SubprocessoService.validarRequisitosNegocioParaDisponibilizacao`.
- Simplificar os métodos do Controller para apenas chamarem o Service, removendo a lógica de validação da camada Web.

### 2.5 Remover "Facades" Restantes

O uso de "Facades" (`OrganizacaoFacade`, `UsuarioFacade`, `LoginFacade`) é redundante para uma aplicação deste porte. Eles apenas delegam chamadas para os serviços subjacentes, adicionando uma camada desnecessária.

**Recomendação:**
- Remover `OrganizacaoFacade`, `UsuarioFacade` e `LoginFacade`.
- Atualizar os Controllers (ex: `UsuarioController`, `UnidadeController`) para injetarem e utilizarem diretamente os Services apropriados (`UsuarioService`, `UnidadeService`, `UnidadeHierarquiaService`, `ResponsavelUnidadeService`).

### 1.2 Simplificar Stores Redundantes do Pinia

A arquitetura atual possui dezenas de stores do Pinia (14 stores, como `usuarios.ts`, `alertas.ts`, etc.) que espelham o estado do servidor com mapeadores manuais complexos, adicionando considerável overhead em um aplicativo projetado para pouquíssimos usuários.

**Recomendação:**
- Substituir stores complexos (como `usuarios.ts`) e seus mapeadores manuais por chamadas diretas de API via os serviços já unificados do frontend ou pelo uso de gerenciamento de estado mais leve.
- Componentes devem buscar dados dos serviços de domínio (ex: `usuarioService.ts`) e gerenciar o estado localmente ou através de abstrações simples.
