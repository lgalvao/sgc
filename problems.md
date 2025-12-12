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

## Problemas Pendentes

Os testes abaixo continuam falhando e requerem investigação aprofundada da lógica de negócio ou configuração de segurança.

### 1. `CDU06IntegrationTest` (3 falhas)
**Erro:** HTTP 403 Forbidden (esperado 200 OK).
**Testes Afetados:**
- `testPodeHomologarCadastro_true`
- `testPodeHomologarMapa_true`
- `testPodeFinalizar_false_semAdmin`
**Análise:** O método `ProcessoService.checarAcesso` retorna `false` mesmo quando o usuário (configurado via `setupSecurityContext`) deveria ter permissão. A configuração de segurança com `MockMvc` foi atualizada para usar `.with(authentication(auth))`, mas a validação interna do serviço ainda falha, possivelmente devido a inconsistências no mock de `SgrhService` ou na propagação do contexto de segurança para o bean proxied.

### 2. `CDU14IntegrationTest` (7 falhas)
**Erro:** HTTP 409 Conflict (esperado 200 OK).
**Análise:** Indica que a operação está sendo tentada em um estado inválido do processo/subprocesso. Provavelmente relacionado à máquina de estados ou validações de transição que não estão satisfeitas pelos dados de teste.

### 3. `CDU12IntegrationTest` (4 falhas)
**Erro:** Falha na asserção do JSON `$.temImpactos` (esperado `true`, recebido `false`).
**Análise:** A lógica de detecção de impactos no mapa de competências não está identificando as alterações simuladas no teste.

### 4. Outros
- `CDU13IntegrationTest`: Falha de dados (null vs esperado).
- `CDU15IntegrationTest`: Estrutura JSON incorreta ou dados não salvos.
- `CDU20IntegrationTest`: Retorno nulo inesperado.
- `ProcessoServiceTest`: Falha em teste unitário de lógica de acesso.
