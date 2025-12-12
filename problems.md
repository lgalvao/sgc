# Problemas Identificados nos Testes de Backend

Este documento resume os problemas encontrados durante a execução dos testes de backend e as correções aplicadas.

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

### 4. `CDU14IntegrationTest` (Resolvido)
**Erro A: 409 Conflict - "Unidade SUB-UNIT não possui mapa vigente"**
**Causa:** `UnidadeMapa` dependia de `Mapa` que dependia de `Subprocesso`. Mapas de teste não tinham subprocesso associado.
**Correção:** Atualizado `data.sql` para garantir que todo `Mapa` esteja associado a um `Subprocesso` válido. Adicionalmente, criados subprocessos e processos "FINALIZADOS" para unidades de teste (10, 102, 201) para evitar bloqueios ao iniciar novos processos.

**Erro B: 403 Forbidden - "Usuário não é o titular da unidade"**
**Causa:** Entidade `Unidade` é `@Immutable`, então `unidadeRepo.saveAll` não persistia o titular no H2.
**Correção:** Injetado `JdbcTemplate` no teste para realizar atualização direta na tabela `vw_unidade`.

### 5. `CDU12IntegrationTest` (Resolvido)
**Erro:** Falha na detecção de impactos (`$.temImpactos` false).
**Causa:** O setup do teste criava um `Mapa` mas não o associava à `Unidade` via `UnidadeMapaRepo`, fazendo com que o `ImpactoMapaService` não encontrasse um mapa vigente para comparação.
**Correção:** Injetado `UnidadeMapaRepo` e persistido o vínculo `UnidadeMapa` no `setUp`. Teste de borda ajustado para remover explicitamente o vínculo.

### 6. `CDU13IntegrationTest` e `CDU20IntegrationTest` (Resolvido)
**Erro:** Falha ao verificar sigla da unidade no histórico (`expected: "COSIS", but was: null`).
**Causa:** `AnaliseMapper` não estava populando o campo `unidadeSigla` no DTO, pois a entidade `Analise` possui apenas `unidadeCodigo`.
**Correção:** Convertido `AnaliseMapper` para classe abstrata e injetado `UnidadeRepo` para buscar a sigla a partir do código durante o mapeamento.

### 7. `CDU15IntegrationTest` (Resolvido)
**Erro:** JSON path inválido (`atividadesCodigos` não encontrado).
**Causa:** O campo no DTO `CompetenciaMapaDto` chamava-se `atividadesAssociadas`, mas o teste esperava `atividadesCodigos`.
**Correção:** Renomeado o campo `atividadesAssociadas` para `atividadesCodigos` no DTO `CompetenciaMapaDto` e atualizado suas referências no `MapaService`.

### 8. `AtividadeServiceReproductionTest` (Resolvido)
**Erro:** Exceção inesperada (`ErroAccessoNegado` em vez de `ErroEntidadeNaoEncontrada`).
**Causa:** O teste de reprodução falhava na verificação de titularidade antes de atingir o cenário de erro desejado (NPE/Entidade não encontrada).
**Correção:** Configurado o titular da unidade no mock para passar pela verificação de segurança.

### 9. `ProcessoServiceTest` (Resolvido)
**Erro:** `checarAcesso` retornando false.
**Causa:** O teste mockava `existsByProcessoCodigoAndUnidadeCodigo` (singular), mas o serviço foi refatorado para usar `existsByProcessoCodigoAndUnidadeCodigoIn` (lista) para suportar hierarquia.
**Correção:** Atualizado o mock para `existsByProcessoCodigoAndUnidadeCodigoIn`.

## Problemas Pendentes e Bloqueantes

Nenhum. Todos os testes de backend estão passando.
