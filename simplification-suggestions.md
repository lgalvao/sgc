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
