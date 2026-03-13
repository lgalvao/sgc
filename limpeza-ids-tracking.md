# Tracking de Limpeza de Identificadores (SGC)

Este arquivo monitora o progresso da migração de `id` para `codigo` por entidade/módulo.

### Status por Módulo (Backend)

| Módulo | Progresso | Observações |
| :--- | :--- | :--- |
| **Processo** | 100% (CONCLUÍDO) | Repositories, Services e Controllers migrados. |
| **Unidade** | 100% (CONCLUÍDO) | Repositories, Services e Controllers migrados. |
| **Alerta** | 100% (CONCLUÍDO) | Repositories, Services e Controllers migrados. |
| **Subprocesso** | 100% (CONCLUÍDO) | Repositories, Services e Controllers migrados. |
| **Mapa/Atividade** | 100% (CONCLUÍDO) | Repositories, Services e Controllers migrados. |
| **Segurança** | 100% (CONCLUÍDO) | PermissionEvaluator e UserDetails migrados. |

### Status por Camada (Geral)

| Camada | Progresso | Observações |
| :--- | :--- | :--- |
| **Backend Core** | 100% (CONCLUÍDO) | Varredura retornou 0 matches. |
| **Backend Tests** | 100% (CONCLUÍDO) | Compilando via `testClasses`. |
| **Frontend** | 100% (CONCLUÍDO) | Verificado via `grep_search`. |
| **Testes E2E** | 80% (EM ANDAMENTO) | Fixtures e Helpers sendo revisados. |
| **Documentação** | 50% (TODO) | README e MDs precisam de revisão. |

---

## Log de Atividades Recentes
- [2026-03-13] **BUILD SUCCESSFUL**: Sistema compilando após renomeações massivas de métodos e variáveis.
- [2026-03-13] Limpeza completa do `E2eController.java` (mais de 50 variáveis locais e métodos renomeados).
- [2026-03-13] Limpeza dos módulos **Subprocesso** e **Mapa** (Repositories, Services e Facades) concluída no Backend Core.
- [2026-03-13] Script `find_id_legacy.py` atualizado para suportar filtro por diretório.
- [2026-03-13] Decisão de design: Não utilizar pontes de compatibilidade JSON (`@JsonProperty("id")`). A migração será direta para `codigo`.
- [2026-03-13] Verificação de dependências: Confirmado uso de **Jackson 3** (`tools.jackson.*`).
- [2026-03-13] Limpeza do módulo **Alerta** (Entidade AlertaUsuario, Repository e Facade) concluída no Backend Core.
- [2026-03-13] Limpeza do módulo **Processo** (UnidadeProcesso, ProcessoRepo, ProcessoService, ProcessoController) concluída no Backend Core.
- [2026-03-13] Criação do script de varredura `find_id_legacy.py` e primeiro relatório consolidado.
- [2026-03-13] Estruturação do plano de migração e arquivo de tracking.
