# Problemas Identificados nos Testes de Backend

Este documento resume os problemas encontrados durante a execução dos testes de backend e as correções aplicadas ou pendentes.

## Correções Aplicadas

### 1. Falha em `E2eFixtureEndpointTest` (Resolvido)
**Erro:** `Table "UNIDADE" not found`.
**Causa:** O script `e2e/setup/seed.sql` tentava inserir dados na tabela `sgc.unidade`, mas o esquema H2 (usado no perfil `e2e`) define a tabela como `sgc.vw_unidade` (para simular a view do Oracle).
**Correção:** Atualizado `e2e/setup/seed.sql` para usar os nomes de tabelas corretos (`sgc.vw_unidade`, `sgc.vw_usuario`, `sgc.vw_usuario_perfil_unidade`) e ajustar os nomes das colunas (`titulo_eleitoral` -> `titulo`, `unidade_codigo` -> `unidade_lot_codigo` para usuário). Também corrigido o valor de situação `CONCLUIDO` para `MAPEAMENTO_MAPA_HOMOLOGADO` na tabela `subprocesso`.

### 2. NPE em `ProcessoDetalheMapperCustom` (Resolvido)
**Erro:** `NullPointerException` ao mapear detalhes do processo para testes com usuários mockados (Admin).
**Causa:** O método `isCurrentUserChefeOuCoordenador` acessava `attr.getUnidade().getCodigo().equals(...)`. Em testes com `@WithMockAdmin`, a unidade mockada não possui ID (`codigo` é null), causando NPE.
**Correção:** Alterado para usar `java.util.Objects.equals(attr.getUnidade().getCodigo(), unidade.getCodigo())`.

### 3. `CDU06IntegrationTest` (Resolvido)
**Erro Original:** HTTP 403 Forbidden.
**Causa:** O método `setupSecurityContext` no teste não inicializava corretamente as atribuições de perfil (`UsuarioPerfil`) no objeto `Usuario`. Como resultado, `getAuthorities()` retornava uma lista vazia ou incorreta, falhando as verificações de segurança baseadas em role (`hasRole`).
**Correção:** Atualizado o método `setupSecurityContext` em `CDU06IntegrationTest.java` para criar explicitamente um `Set<UsuarioPerfil>`, populá-lo com a unidade e perfil corretos, e atribuí-lo ao usuário mock via `setAtribuicoes`.

## Problemas Pendentes e Bloqueantes

### 1. `CDU14IntegrationTest` (8 falhas)
Este teste de integração enfrenta múltiplos problemas estruturais relacionados à configuração dos dados de teste e mapeamento de entidades.

**Erro A: 409 Conflict - "Unidade SUB-UNIT não possui mapa vigente"**
*   **Causa:** O serviço `ProcessoService` falha ao recuperar o `UnidadeMapa` (mapa vigente) da unidade 102 (`SUB-UNIT`).
*   **Análise Técnica:** Embora o registro exista na tabela `UNIDADE_MAPA`, a entidade JPA `UnidadeMapa` tem um relacionamento `@ManyToOne` com `Mapa` (`mapaVigente`). A entidade `Mapa`, por sua vez, possui um relacionamento obrigatório (`optional = false`) com `Subprocesso`.
*   **Raiz do Problema:** O script de dados de teste (`data.sql`) insere mapas (ex: ID 1004) sem associá-los a um subprocesso (coluna `subprocesso_codigo` é NULL). Quando o Hibernate tenta buscar o `UnidadeMapa`, ele executa um `INNER JOIN` com a tabela `SUBPROCESSO` devido à restrição de não-nulidade na entidade `Mapa`. Como o subprocesso não existe, o join falha e a consulta retorna vazio, levando o serviço a lançar `ErroProcesso` (409).
*   **Solução Necessária:** Atualizar `data.sql` para garantir que todo `Mapa` de teste esteja associado a um `Subprocesso` válido, ou ajustar a entidade `Mapa` se a associação não for estritamente obrigatória em todos os cenários.

**Erro B: 403 Forbidden - "Usuário não é o titular da unidade"**
*   **Causa:** O método `SubprocessoWorkflowService.validarSubprocessoParaDisponibilizacao` verifica se o usuário logado é o titular da unidade. A verificação falha com a mensagem "Titular é não definido".
*   **Análise Técnica:** O teste tenta definir o titular da unidade no método `setUp` chamando `unidade.setTituloTitular(...)` e `unidadeRepo.saveAll(...)`. No entanto, a entidade `Unidade` está anotada com `@Immutable` (pois reflete uma VIEW de banco de dados).
*   **Raiz do Problema:** O Hibernate ignora silenciosamente quaisquer atualizações (UPDATEs) em entidades `@Immutable`. Portanto, a alteração do titular no teste não é persistida no banco H2. Quando o serviço recarrega a unidade do banco, o campo `titulo_titular` permanece `NULL` (valor original do `data.sql`), causando a negação de acesso.
*   **Solução Necessária:** Utilizar `JdbcTemplate` nos testes para realizar atualizações diretas via SQL, contornando a restrição do Hibernate para fins de configuração de cenário de teste.

### 2. `CDU12IntegrationTest` (4 falhas)
**Erro:** Falha na asserção `$.temImpactos` (esperado `true`, recebido `false`).
**Análise:** A lógica de detecção de impactos (`ImpactoMapaService`) não está identificando as alterações simuladas (ex: remoção de atividade). Isso pode ser causado por transações de teste que isolam as alterações de dados da lógica de verificação, ou por dados de teste que não satisfazem as condições complexas de comparação de mapas (versão anterior vs atual).

### 3. Outros Testes com Falhas
*   `CDU13IntegrationTest`: Falha de dados (null vs esperado).
*   `CDU15IntegrationTest`: Estrutura JSON incorreta.
*   `CDU20IntegrationTest`: Retorno nulo inesperado.
