# Auditoria de consistencia de nomenclatura

Gerado em: 2026-05-20T18:53:13.088Z
Base: /home/runner/work/sgc/sgc

## Indicadores

- Arquivos analisados: 1128
- Tipos fora do padrao PascalCase: 0
- Membros fora do padrao camelCase: 321
- Parametros fora do padrao camelCase: 7
- Parametros com uso de 'id': 5
- Pacotes Java fora de lowercase.dotted: 0

## Formatos de arquivos por extensao

| Extensao | Formatos encontrados |
|---|---|
| .java | PascalCase: 556, kebab-case: 25 |
| .ts | outro: 247, minusculo: 58, kebab-case: 21, camelCase: 73 |
| .js | minusculo: 13, outro: 3, kebab-case: 37 |
| .vue | PascalCase: 95 |

## Exemplos de divergencias

### Tipos fora de PascalCase
- Nenhum encontrado

### Membros fora de camelCase
- findBySubprocesso_CodigoOrderByDataHoraCriacaoDesc(Long subprocessoCodigo, Pageable pageable) (outro) em backend/src/main/java/sgc/alerta/model/NotificacaoEmailRepo.java
- deleteByMapa_Codigo(Long mapaCodigo) (outro) em backend/src/main/java/sgc/mapa/model/CompetenciaRepo.java
- findByAtividade_Codigo(Long atividadeCodigo) (outro) em backend/src/main/java/sgc/mapa/model/ConhecimentoRepo.java
- findByMapa_Codigo(Long mapaCodigo) (outro) em backend/src/main/java/sgc/subprocesso/model/SubprocessoRepo.java
- marcarComoLidos_quandoSucesso_deveRetornarOk() (outro) em backend/src/test/java/sgc/alerta/AlertaControllerTest.java
- marcarComoLidos_quandoListaVazia_deveRetornarOk() (outro) em backend/src/test/java/sgc/alerta/AlertaControllerTest.java
- listarAlertas_quandoSucesso_deveRetornarListaDeAlertas() (outro) em backend/src/test/java/sgc/alerta/AlertaControllerTest.java
- listarNaoLidos_quandoSucesso_deveRetornarListaDeAlertas() (outro) em backend/src/test/java/sgc/alerta/AlertaControllerTest.java
- marcarComoLidos_Duplicatas() (outro) em backend/src/test/java/sgc/alerta/AlertaFacadeTest.java
- deveLimparProcessoCompleto_SemDados() (outro) em backend/src/test/java/sgc/e2e/E2eControllerTest.java
- deveLimparProcessoCompleto_ErroConexao() (outro) em backend/src/test/java/sgc/e2e/E2eControllerTest.java
- deveLimparProcessoCompleto_DataSourceNull() (outro) em backend/src/test/java/sgc/e2e/E2eControllerTest.java
- limparTabela_UsaDelete() (outro) em backend/src/test/java/sgc/e2e/E2eControllerTest.java
- criarProcessoFixture_UnidadeNaoEncontrada() (outro) em backend/src/test/java/sgc/e2e/E2eControllerTest.java
- criarProcessoFixture_FalhaIniciar() (outro) em backend/src/test/java/sgc/e2e/E2eControllerTest.java
- criarProcessoFixture_FalhaRecarregar() (outro) em backend/src/test/java/sgc/e2e/E2eControllerTest.java
- testLoginCompleto_sucessoUsuarioUnicoPerfil() (outro) em backend/src/test/java/sgc/integracao/CDU01IntegrationTest.java
- testLoginCompleto_sucessoUsuarioMultiplosPerfis() (outro) em backend/src/test/java/sgc/integracao/CDU01IntegrationTest.java
- testEntrar_falhaUsuarioNaoAutenticado() (outro) em backend/src/test/java/sgc/integracao/CDU01IntegrationTest.java
- testEntrar_falhaUnidadeInexistente() (outro) em backend/src/test/java/sgc/integracao/CDU01IntegrationTest.java

### Parametros com `id`
- id em exibirScreenshot(@PathVariable UUID id) (backend/src/main/java/sgc/feedback/FeedbackController.java)
- id em obterScreenshot(UUID id) (backend/src/main/java/sgc/feedback/FeedbackService.java)
- id em unidadeComId(Long id) (backend/src/test/java/sgc/fixture/UnidadeFixture.java)
- testId em obterAcaoBloco(page: Page, testId: string) (e2e/helpers/helpers-processos.ts)
- competenciaId em removerAtividadeAssociada(competenciaId: number, codigoAtividade: number) (frontend/src/composables/useMapaCompetenciasMutacoes.ts)

### Pacotes Java fora do padrao
- Nenhum encontrado
