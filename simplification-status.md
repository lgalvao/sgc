# Status da Simplificação - SGC

Este documento rastreia o progresso das tarefas de simplificação propostas em `simplification-suggestions.md`.

## 1. Backend: Eliminar Camada de Facade Pass-Through

- [x] **Remover `SubprocessoFacade`**.
- [x] Injetar serviços especializados diretamente nos Controllers.
- [x] Mover orquestração real para `SubprocessoService` ou manter no Controller se simples.

## 2. Backend: Consolidar Serviços Fragmentados

- [x] **Fundir serviços correlatos.**
    - [x] `SubprocessoCrudService` + `SubprocessoValidacaoService` + `SubprocessoFactory` + `SubprocessoContextoService` + `SubprocessoAtividadeService` + `SubprocessoAjusteMapaService` + `SubprocessoPermissaoCalculator` → `SubprocessoService` (Core).
    - [x] `SubprocessoCadastroWorkflowService` + `SubprocessoMapaWorkflowService` + `SubprocessoAdminWorkflowService` + `SubprocessoTransicaoService` → `SubprocessoWorkflowService`.
- [x] Manter apenas 2-3 serviços principais por módulo.

## 3. Backend: Remover Eventos Assíncronos (Spring Events)

- [x] **Substituir por chamadas diretas.**
- [x] Remover listeners e classes de evento (`TipoTransicao` movido para model).

## 4. Backend: Simplificar Mappers e DTOs

- [ ] Eliminar MapStruct para lógicas complexas.
- [ ] Usar `@JsonView` nas Entidades.
- [ ] Records para DTOs de Escrita.

## 5. Frontend: Eliminar Camada de Mappers Manuais

- [ ] Padronizar a API.
- [ ] Tipagem Direta.
- [ ] Remover arquivos de mapper.

## 6. Frontend: Simplificar Service Layer

- [ ] Injetar API no Store.
- [ ] Manter Service apenas para lógica real.
