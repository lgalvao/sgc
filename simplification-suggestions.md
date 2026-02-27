# Sugestões de Simplificação

**Objetivo:** Reduzir o excesso de engenharia ("overengineering") e a fragmentação em um aplicativo de intranet de pequena escala (5-10 usuários simultâneos).

## 1. Backend (Spring Boot)

### 1.1 Remover Camada de Facade (Redundante)
A aplicação possui classes `Facade` que atuam meramente como "pass-through" (repasse) para os Services, adicionando indireção desnecessária sem encapsular lógica de fluxo real.
- **Alvos:** `OrganizacaoFacade`, `UsuarioFacade`, `LoginFacade`.
- **Ação:** Injetar e utilizar diretamente `UnidadeService`, `UsuarioService`, `ResponsavelUnidadeService` e `HierarquiaService` nos Controllers.
- **Benefício:** Menos arquivos para manter, navegação de código mais direta, stack traces menores.

### 1.2 Simplificar Segurança (Separar RBAC de Regras de Negócio)
O `SgcPermissionEvaluator` mistura verificação de papéis (RBAC), hierarquia e estado do fluxo de trabalho (ex: "Processo Finalizado").
- **Problema:** Torna a segurança difícil de testar e acopla a camada Web (Security) com regras de domínio complexas.
- **Ação:**
    1. Usar `@PreAuthorize("hasRole('ADMIN')")` apenas para verificações estáticas de papel.
    2. Mover regras de estado/localização ("Posso editar este subprocesso?") para dentro dos métodos de serviço (`subprocessoService.validarEdicao(id, usuario)`).
- **Benefício:** Testes de unidade mais simples para regras de negócio (sem precisar de mock de SecurityContext) e segurança declarativa mais limpa.

### 1.3 Eliminar Mapeamento Manual e DTOs Espelho
Muitos DTOs (ex: `UnidadeDto`) são cópias quase idênticas das Entidades JPA.
- **Ação:** Para leituras (GET), retornar projeções (Interfaces/Records) do Spring Data ou, em casos simples de apenas leitura, a própria Entidade (com `@JsonIgnore` nas relações LAZY).
- **Ferramentas:** Remover MapStruct e mappers manuais onde o DTO é apenas um espelho.
- **Contexto:** Para 5-10 usuários, o overhead de serializar uma entidade JPA (com os devidos cuidados de Lazy Loading) é insignificante comparado ao custo de manter centenas de DTOs e Mappers.

## 2. Frontend (Vue/TypeScript)

### 2.1 Reduzir Complexidade dos Stores (Pinia)
O store `usuarios.ts` (e outros 13 stores) implementa cache manual, tratamento de erro complexo e estado global para dados que muitas vezes são locais de uma página.
- **Ação:** Utilizar "Composables" simples para busca de dados (`useFetchUsuarios`) ou uma biblioteca de *Server State* como **TanStack Query (Vue Query)**.
- **Benefício:** Elimina a necessidade de gerenciar `isLoading`, `error`, e cache manualmente em cada store. O código de um store de 50 linhas vira uma chamada de 3 linhas.

### 2.2 Unificar Tipos e Mappers
O frontend possui tipos manuais em `tipos.ts`, `dtos.ts` e mappers manuais dentro de serviços (ex: `usuarioService.ts` com `mapVWUsuarioToUsuario`).
- **Ação:** Gerar tipos TypeScript automaticamente a partir do Backend (ex: usando `openapi-typescript` ou similar) e remover a camada de tradução manual.
- **Benefício:** Garantia de tipo em tempo de compilação entre Backend e Frontend; fim dos erros de "campo renomeado no Java que quebrou o JS".

### 2.3 Simplificar Serviços
Serviços como `usuarioService.ts` misturam chamadas API com lógica de transformação de dados.
- **Ação:** Se usar Vue Query, o "Service" pode ser apenas uma função que retorna `api.get('/usuarios')`. A transformação de dados deve ser evitada ou feita no Backend (BFF - Backend for Frontend) se for complexa.

## 3. Infraestrutura e Processos

### 3.1 Ajustar Quality Gates para a Realidade
O projeto roda ferramentas pesadas como **Pitest** (Testes de Mutação) e exige 98% de cobertura (Jacoco).
- **Observação:** Para um time pequeno e software interno, manter 98% de cobertura e rodar testes de mutação pode consumir mais tempo de CI/CD e manutenção de testes do que o desenvolvimento de features.
- **Sugestão:** Relaxar a cobertura para 80% (focando no Core Domain) e rodar o Pitest apenas manualmente ou em nightly builds, não em cada commit/PR.

### 3.2 Remover Dependências "Enterprise" Desnecessárias
- Revisar a necessidade de `MapStruct`, `Lombok` (se for apenas para Getter/Setter, Records do Java 21 podem substituir DTOs imutáveis) e validações excessivas de arquitetura (`ArchUnit`) se o time for disciplinado e pequeno.
