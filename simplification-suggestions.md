# Sugestões de Simplificação para Sistema Intranet (5-10 Usuários)

Este documento descreve áreas de overengineering identificadas no sistema SGC e propõe simplificações radicais adequadas ao contexto de um sistema pequeno de intranet.

---

## 1. Backend: Eliminar Camada de Facade Pass-Through

**Problema:** A classe `SubprocessoFacade` atua meramente como um "proxy" para mais de 10 serviços diferentes (`SubprocessoCrudService`, `SubprocessoValidacaoService`, etc.), sem adicionar lógica de orquestração significativa na maioria dos métodos. Isso obriga a criar/alterar arquivos em duplicidade para cada nova funcionalidade.

**Solução:**
- **Remover `SubprocessoFacade`**.
- Injetar os serviços especializados diretamente nos Controllers (`SubprocessoCrudController`, `SubprocessoCadastroController`).
- Para os poucos casos onde há orquestração real, mover essa lógica para um `SubprocessoService` central ou manter no Controller se for simples.

## 2. Backend: Consolidar Serviços Fragmentados

**Problema:** O módulo `subprocesso` possui uma explosão de serviços pequenos (`crud/`, `workflow/`, `validacao/`, `factory/`, `notificacao/`). Para um sistema deste porte, essa granularidade é excessiva e dificulta a navegação.

**Solução:**
- **Fundir serviços correlatos.**
    - `SubprocessoCrudService` + `SubprocessoValidacaoService` + `SubprocessoFactory` → `SubprocessoService` (Core).
    - `SubprocessoCadastroWorkflowService` + `SubprocessoMapaWorkflowService` + `SubprocessoAdminWorkflowService` → `SubprocessoWorkflowService`.
- Manter apenas 2-3 serviços principais por módulo.

## 3. Backend: Remover Eventos Assíncronos (Spring Events)

**Problema:** O uso de `ApplicationEventPublisher` para desacoplar módulos (ex: `EventoProcessoIniciado`, `EventoTransicaoSubprocesso`) introduz complexidade de rastreamento e debugging desnecessária para um sistema monolítico pequeno. A assincronicidade não traz ganho de performance perceptível para 10 usuários.

**Solução:**
- **Substituir por chamadas diretas.**
- Em vez de publicar `EventoProcessoIniciado`, o `ProcessoService` deve chamar diretamente `EmailService.enviarNotificacaoInicio(...)`.
- Remover listeners e classes de evento.

## 4. Backend: Simplificar Mappers e DTOs

**Problema:** O uso de MapStruct com métodos default complexos (ex: `MapaAjusteMapper.mapCompetencias`) obscurece a lógica de transformação. Além disso, há DTOs redundantes que apenas espelham entidades.

**Solução:**
- **Eliminar MapStruct para lógicas complexas:** Escrever métodos de conversão simples em Java (ex: `MapaAjusteAssembler` ou métodos estáticos no DTO).
- **Usar `@JsonView` nas Entidades:** Para leituras (GET), retornar a Entidade diretamente configurada com `@JsonView` para ignorar campos sensíveis ou ciclos, eliminando DTOs de Response (`SubprocessoDetalheResponse`, etc.).
- **Records para DTOs de Escrita:** Usar Java `record` para `*Request` DTOs, eliminando boilerplate de getters/setters/builders.

## 5. Frontend: Eliminar Camada de Mappers Manuais

**Problema:** O frontend possui arquivos de mapper (ex: `frontend/src/mappers/processos.ts`) que convertem manualmente campos do backend para tipos do frontend. Isso é retrabalho.

**Solução:**
- **Padronizar a API:** Garantir que o backend retorne o JSON exatamente no formato que o frontend espera (camelCase, estrutura correta).
- **Tipagem Direta:** Usar os tipos gerados (ou interfaces manuais em `types/`) que espelhem a resposta da API.
- **Remover arquivos de mapper:** O `service` ou `store` deve receber os dados e usá-los diretamente.

## 6. Frontend: Simplificar Service Layer

**Problema:** Arquivos como `processoService.ts` são apenas wrappers finos para o `axios`. Embora seja uma boa prática geral, para um app pequeno, isso pode ser simplificado.

**Solução (Opcional):**
- **Injetar API no Store:** As Pinia Stores podem chamar `apiClient` diretamente.
- **Manter Service apenas para lógica real:** Se o service apenas faz `return apiClient.get(...)`, considere mover para a store para reduzir um arquivo por módulo.

---

## Resumo da Simplificação

| Componente | Estado Atual | Estado Proposto | Redução Estimada |
| :--- | :--- | :--- | :--- |
| **Arquitetura** | Controller -> Facade -> 10 Services | Controller -> 2 Services | -1 Camada, -8 Arquivos |
| **Comunicação** | Eventos Assíncronos (Publish/Listen) | Chamadas Diretas de Método | -10+ Classes de Evento/Listener |
| **Dados** | Entity -> MapStruct -> DTO -> JSON -> Axios -> Frontend Mapper -> Frontend Type | Entity (@JsonView) -> JSON -> Axios -> Frontend Type | -50% Classes DTO/Mapper |
| **Frontend** | View -> Store -> Service -> API | View -> Store -> API | -15 Arquivos de Service |
