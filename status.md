# Status do Refatoramento Modulith

## Contexto
Um grande refatoramento nos módulos Modulith foi realizado para expor entidades comumente usadas. Isso causou a quebra de compilação e testes no backend, principalmente devido à remoção de associações diretas em entidades (ex: `Subprocesso.getMapa()`, `Subprocesso.getProcesso()`) em favor de referências por ID ou lookups via repositório.

## Progresso Atual

### Arquivos Corrigidos
Os seguintes arquivos foram refatorados para injetar repositórios (`MapaRepo`, `ProcessoRepo`) e buscar as entidades necessárias ao invés de navegar pelo grafo de objetos:

*   **Mappers:**
    *   `SubprocessoMapper`
    *   `SubprocessoDetalheMapper`
    *   `MapaAjusteMapper`
*   **Repositórios:**
    *   `MapaRepo` (adicionado `findBySubprocessoCodigo`)
    *   `AtividadeRepo` (queries JPQL atualizadas)
*   **Serviços:**
    *   `SubprocessoFactory`
    *   `SubprocessoMapaWorkflowService`
    *   `SubprocessoConsultaService`
    *   `SubprocessoNotificacaoService`
*   **DTOs:**
    *   `SugestoesDto`
    *   `MapaAjusteDto`
*   **Testes:**
    *   Início da correção em `CDU15IntegrationTest`, `CDU18IntegrationTest` e `AtividadeFluxoIntegrationTest`.

### Arquivos com Erros de Compilação Pendentes
A compilação (`./gradlew classes`) ainda falha. Os erros principais são "cannot find symbol" para métodos removidos (`getMapa()`, `getProcesso()`, `setMapa()`) e importações ausentes.

*   **`sgc.subprocesso.internal.service.SubprocessoDtoService`**
    *   Faltam imports (`sgc.mapa.api.model.Mapa`).
    *   Uso de `sp.getMapa()` precisa ser substituído por lookup no `MapaRepo`.
*   **`sgc.subprocesso.internal.service.SubprocessoPermissoesService`**
    *   Uso de `sp.getProcesso()` e `sp.getMapa()`.
*   **`sgc.processo.internal.service.ProcessoService`**
    *   Uso de `subprocesso.getMapa()`.
*   **`sgc.atividade.AtividadeService`**
    *   Uso de `subprocesso.getMapa()` e `subprocesso.getProcesso()`.
*   **`sgc.subprocesso.internal.service.SubprocessoCadastroWorkflowService`**
    *   Uso de `sp.getMapa()`.
*   **`sgc.processo.internal.service.ProcessoDetalheBuilder`**
    *   Uso de `sp.getMapa()`.
*   **`sgc.subprocesso.internal.SubprocessoMapaController`**
    *   Chamadas para métodos refatorados podem precisar de ajuste nos argumentos.
*   **`sgc.mapa.internal.service.MapaVisualizacaoService`**
    *   Uso de `subprocesso.getMapa()`.

## Próximos Passos
1.  **Corrigir Imports:** Adicionar imports faltantes (ex: `Mapa`, `Processo`) nos arquivos listados acima.
2.  **Refatorar Serviços Restantes:**
    *   Injetar `MapaRepo` e `ProcessoRepo` onde necessário.
    *   Substituir chamadas `getMapa()` por `mapaRepo.findBySubprocessoCodigo(...)`.
    *   Substituir chamadas `getProcesso()` por `processoRepo.findById(sp.getProcessoCodigo())`.
3.  **Verificar Compilação:** Executar `./gradlew classes` até obter sucesso.
4.  **Corrigir Testes:** Finalizar a adaptação dos testes de integração para refletir a nova estrutura (ex: configurar relacionamentos via IDs nos fixtures/setups).
5.  **Validar Modulith:** Executar testes de arquitetura para garantir que as regras de dependência entre módulos estão sendo respeitadas.
