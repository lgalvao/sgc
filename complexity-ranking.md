# Ranking de Complexidade do Backend - SGC

Este relatório apresenta uma análise detalhada da complexidade do código backend, baseado em métricas do Jacoco e análise de complexidade ciclomática.

## Resumo Executivo

- **Total de Classes Analisadas:** 66
- **Complexidade Ciclomática Total:** 1415
- **Total de Branches:** 1259
- **Total de Linhas de Código:** 3598
- **Complexidade Média por Classe:** 21.44

## Metodologia

O **Complexity Score** é calculado através da fórmula:

```
Score = (Complexidade Ciclomática × 0.40) + 
        (Total de Branches × 0.30) + 
        (Linhas de Código ÷ 10 × 0.20) + 
        (Complexidade Média por Método × 5 × 0.10)
```

## Top 50 Classes Mais Complexas

| Rank | Classe | Pacote | Score | Complexity | Branches | Linhas | Métodos | Avg/Método | Categoria |
|------|--------|--------|-------|------------|----------|---------|---------|------------|------------|
| 1 | `SubprocessoService` | `sgc.subprocesso.service` | 171.0 | 211 | 253 | 470 | 84 | 2.5 | Service/Facade |
| 2 | `ProcessoService` | `sgc.processo.service` | 122.7 | 154 | 178 | 326 | 65 | 2.4 | Service/Facade |
| 3 | `SubprocessoTransicaoService` | `sgc.subprocesso.service` | 81.4 | 103 | 102 | 428 | 52 | 2.0 | Service/Facade |
| 4 | `SgcPermissionEvaluator` | `sgc.seguranca` | 46.1 | 56 | 68 | 85 | 18 | 3.1 | Model/Entity |
| 5 | `ImpactoMapaService` | `sgc.mapa.service` | 42.4 | 55 | 54 | 159 | 27 | 2.0 | Service/Facade |
| 6 | `SubprocessoValidacaoService` | `sgc.subprocesso.service` | 39.0 | 46 | 56 | 126 | 18 | 2.6 | Service/Facade |
| 7 | `UnidadeHierarquiaService` | `sgc.organizacao.service` | 37.8 | 51 | 48 | 102 | 27 | 1.9 | Service/Facade |
| 8 | `PainelFacade` | `sgc.processo.painel` | 35.8 | 42 | 52 | 104 | 16 | 2.6 | Service/Facade |
| 9 | `MapaManutencaoService` | `sgc.mapa.service` | 33.7 | 61 | 18 | 167 | 52 | 1.2 | Service/Facade |
| 10 | `AlertaFacade` | `sgc.alerta` | 32.0 | 42 | 40 | 114 | 22 | 1.9 | Service/Facade |
| 11 | `MapaSalvamentoService` | `sgc.mapa.service` | 26.6 | 32 | 36 | 94 | 14 | 2.3 | Service/Facade |
| 12 | `SubprocessoController` | `sgc.subprocesso` | 25.3 | 54 | 2 | 130 | 53 | 1.0 | Controller |
| 13 | `LimitadorTentativasLogin` | `sgc.seguranca.login` | 24.2 | 31 | 32 | 56 | 15 | 2.1 | Model/Entity |
| 14 | `SubprocessoNotificacaoService` | `sgc.subprocesso.service` | 21.5 | 26 | 28 | 81 | 12 | 2.2 | Service/Facade |
| 15 | `CopiaMapaService` | `sgc.mapa.service` | 21.3 | 27 | 26 | 85 | 14 | 1.9 | Service/Facade |
| 16 | `UsuarioFacade` | `sgc.organizacao` | 18.8 | 30 | 16 | 64 | 22 | 1.4 | Service/Facade |
| 17 | `LoginFacade` | `sgc.seguranca` | 17.8 | 23 | 22 | 52 | 12 | 1.9 | Service/Facade |
| 18 | `RestExceptionHandler` | `sgc.comum.erros` | 16.7 | 27 | 12 | 83 | 21 | 1.3 | Model/Entity |
| 19 | `ResponsavelUnidadeService` | `sgc.organizacao.service` | 16.0 | 22 | 16 | 81 | 14 | 1.6 | Service/Facade |
| 20 | `Subprocesso` | `sgc.subprocesso.model` | 15.7 | 19 | 22 | 17 | 8 | 2.4 | Model/Entity |
| 21 | `LoginController` | `sgc.seguranca.login` | 14.7 | 16 | 20 | 48 | 6 | 2.7 | Controller |
| 22 | `ProcessoController` | `sgc.processo` | 14.7 | 26 | 8 | 65 | 22 | 1.2 | Controller |
| 23 | `GerenciadorJwt` | `sgc.seguranca.login` | 14.2 | 16 | 18 | 63 | 7 | 2.3 | Model/Entity |
| 24 | `AtividadeFacade` | `sgc.mapa` | 13.5 | 21 | 10 | 72 | 16 | 1.3 | Service/Facade |
| 25 | `FiltroJwt` | `sgc.seguranca.login` | 12.8 | 13 | 18 | 28 | 4 | 3.3 | Model/Entity |
| 26 | `UsuarioService` | `sgc.organizacao.service` | 9.6 | 18 | 4 | 30 | 16 | 1.1 | Service/Facade |
| 27 | `SubprocessoSituacaoService` | `sgc.subprocesso.service` | 9.3 | 10 | 12 | 23 | 4 | 2.5 | Service/Facade |
| 28 | `UsuarioPerfil` | `sgc.organizacao.model` | 8.9 | 8 | 12 | 6 | 2 | 4.0 | Model/Entity |
| 29 | `HierarquiaService` | `sgc.organizacao.service` | 8.7 | 11 | 10 | 20 | 6 | 1.8 | Service/Facade |
| 30 | `UnidadeController` | `sgc.organizacao` | 8.3 | 15 | 4 | 27 | 13 | 1.2 | Controller |
| 31 | `EmailService` | `sgc.alerta` | 8.3 | 11 | 8 | 35 | 7 | 1.6 | Service/Facade |
| 32 | `RelatorioFacade` | `sgc.relatorio` | 7.8 | 10 | 6 | 64 | 7 | 1.4 | Service/Facade |
| 33 | `AcaoPermissao` | `sgc.seguranca` | 7.3 | 8 | 8 | 35 | 4 | 2.0 | Model/Entity |
| 34 | `EntidadeBase` | `sgc.comum.model` | 6.5 | 7 | 8 | 5 | 3 | 2.3 | Model/Entity |
| 35 | `UsuarioController` | `sgc.organizacao` | 5.4 | 8 | 4 | 16 | 6 | 1.3 | Controller |
| 36 | `EmailModelosService` | `sgc.alerta` | 5.3 | 7 | 4 | 31 | 5 | 1.4 | Service/Facade |
| 37 | `UnidadeService` | `sgc.organizacao.service` | 5.2 | 11 | 0 | 15 | 11 | 1.0 | Service/Facade |
| 38 | `AtividadeController` | `sgc.mapa` | 5.1 | 9 | 2 | 19 | 8 | 1.1 | Controller |
| 39 | `MapaVisualizacaoService` | `sgc.mapa.service` | 4.8 | 6 | 4 | 24 | 4 | 1.5 | Service/Facade |
| 40 | `AlertaService` | `sgc.alerta` | 4.8 | 10 | 0 | 14 | 10 | 1.0 | Service/Facade |
| 41 | `ClienteAcessoAd` | `sgc.seguranca.login` | 4.7 | 6 | 4 | 19 | 4 | 1.5 | Model/Entity |
| 42 | `ClienteAcessoAdE2e` | `sgc.seguranca.login` | 4.5 | 6 | 4 | 7 | 4 | 1.5 | Model/Entity |
| 43 | `FormatadorData` | `sgc.comum.util` | 4.1 | 5 | 4 | 4 | 3 | 1.7 | Model/Entity |
| 44 | `DeserializadorHtmlSanitizado` | `sgc.seguranca.sanitizacao` | 3.9 | 4 | 4 | 6 | 2 | 2.0 | Model/Entity |
| 45 | `MonitoramentoAspect` | `sgc.comum.util` | 3.1 | 4 | 2 | 12 | 3 | 1.3 | Model/Entity |
| 46 | `MapaController` | `sgc.mapa` | 2.8 | 5 | 0 | 16 | 5 | 1.0 | Controller |
| 47 | `AlertaController` | `sgc.alerta` | 2.0 | 3 | 0 | 14 | 3 | 1.0 | Controller |
| 48 | `ImpactoMapaService.CompetenciaImpactoAcumulador` | `sgc.mapa.service` | 1.9 | 3 | 0 | 10 | 3 | 1.0 | Service/Facade |
| 49 | `RelatorioController` | `sgc.relatorio` | 1.9 | 3 | 0 | 9 | 3 | 1.0 | Controller |
| 50 | `PdfFactory` | `sgc.relatorio` | 1.8 | 3 | 0 | 4 | 3 | 1.0 | Model/Entity |

## Análise por Categoria

### Controller

Total de classes: 10

| Classe | Score | Complexity | Branches | Linhas | Cobertura Branches |
|--------|-------|------------|----------|--------|--------------------|
| `SubprocessoController` | 25.3 | 54 | 2 | 130 | 100.0% |
| `LoginController` | 14.7 | 16 | 20 | 48 | 95.0% |
| `ProcessoController` | 14.7 | 26 | 8 | 65 | 100.0% |
| `UnidadeController` | 8.3 | 15 | 4 | 27 | 50.0% |
| `UsuarioController` | 5.4 | 8 | 4 | 16 | 75.0% |
| `AtividadeController` | 5.1 | 9 | 2 | 19 | 100.0% |
| `MapaController` | 2.8 | 5 | 0 | 16 | 100.0% |
| `AlertaController` | 2.0 | 3 | 0 | 14 | 100.0% |
| `RelatorioController` | 1.9 | 3 | 0 | 9 | 100.0% |
| `PainelController` | 1.5 | 2 | 0 | 9 | 100.0% |

### DTO

Total de classes: 1

| Classe | Score | Complexity | Branches | Linhas | Cobertura Branches |
|--------|-------|------------|----------|--------|--------------------|
| `ValidacaoCadastroDto.Erro` | 0.9 | 1 | 0 | 1 | 100.0% |

### Model/Entity

Total de classes: 27

| Classe | Score | Complexity | Branches | Linhas | Cobertura Branches |
|--------|-------|------------|----------|--------|--------------------|
| `SgcPermissionEvaluator` | 46.1 | 56 | 68 | 85 | 95.6% |
| `LimitadorTentativasLogin` | 24.2 | 31 | 32 | 56 | 84.4% |
| `RestExceptionHandler` | 16.7 | 27 | 12 | 83 | 91.7% |
| `Subprocesso` | 15.7 | 19 | 22 | 17 | 95.5% |
| `GerenciadorJwt` | 14.2 | 16 | 18 | 63 | 94.4% |
| `FiltroJwt` | 12.8 | 13 | 18 | 28 | 100.0% |
| `UsuarioPerfil` | 8.9 | 8 | 12 | 6 | 100.0% |
| `AcaoPermissao` | 7.3 | 8 | 8 | 35 | 100.0% |
| `EntidadeBase` | 6.5 | 7 | 8 | 5 | 100.0% |
| `ClienteAcessoAd` | 4.7 | 6 | 4 | 19 | 100.0% |

### Service/Facade

Total de classes: 28

| Classe | Score | Complexity | Branches | Linhas | Cobertura Branches |
|--------|-------|------------|----------|--------|--------------------|
| `SubprocessoService` | 171.0 | 211 | 253 | 470 | 90.9% |
| `ProcessoService` | 122.7 | 154 | 178 | 326 | 86.5% |
| `SubprocessoTransicaoService` | 81.4 | 103 | 102 | 428 | 84.3% |
| `ImpactoMapaService` | 42.4 | 55 | 54 | 159 | 98.1% |
| `SubprocessoValidacaoService` | 39.0 | 46 | 56 | 126 | 98.2% |
| `UnidadeHierarquiaService` | 37.8 | 51 | 48 | 102 | 93.8% |
| `PainelFacade` | 35.8 | 42 | 52 | 104 | 92.3% |
| `MapaManutencaoService` | 33.7 | 61 | 18 | 167 | 83.3% |
| `AlertaFacade` | 32.0 | 42 | 40 | 114 | 97.5% |
| `MapaSalvamentoService` | 26.6 | 32 | 36 | 94 | 97.2% |

## Top 20 Classes com Maior Complexidade por Método

| Rank | Classe | Avg Complexity/Método | Métodos | Total Complexity | Categoria |
|------|--------|-----------------------|---------|------------------|------------|
| 1 | `UsuarioPerfil` | 4.00 | 2 | 8 | Model/Entity |
| 2 | `FiltroJwt` | 3.25 | 4 | 13 | Model/Entity |
| 3 | `SgcPermissionEvaluator` | 3.11 | 18 | 56 | Model/Entity |
| 4 | `LoginController` | 2.67 | 6 | 16 | Controller |
| 5 | `PainelFacade` | 2.63 | 16 | 42 | Service/Facade |
| 6 | `SubprocessoValidacaoService` | 2.56 | 18 | 46 | Service/Facade |
| 7 | `SubprocessoService` | 2.51 | 84 | 211 | Service/Facade |
| 8 | `SubprocessoSituacaoService` | 2.50 | 4 | 10 | Service/Facade |
| 9 | `Subprocesso` | 2.38 | 8 | 19 | Model/Entity |
| 10 | `ProcessoService` | 2.37 | 65 | 154 | Service/Facade |
| 11 | `EntidadeBase` | 2.33 | 3 | 7 | Model/Entity |
| 12 | `MapaSalvamentoService` | 2.29 | 14 | 32 | Service/Facade |
| 13 | `GerenciadorJwt` | 2.29 | 7 | 16 | Model/Entity |
| 14 | `SubprocessoNotificacaoService` | 2.17 | 12 | 26 | Service/Facade |
| 15 | `LimitadorTentativasLogin` | 2.07 | 15 | 31 | Model/Entity |
| 16 | `ImpactoMapaService` | 2.04 | 27 | 55 | Service/Facade |
| 17 | `AcaoPermissao` | 2.00 | 4 | 8 | Model/Entity |
| 18 | `DeserializadorHtmlSanitizado` | 2.00 | 2 | 4 | Model/Entity |
| 19 | `SubprocessoTransicaoService` | 1.98 | 52 | 103 | Service/Facade |
| 20 | `CopiaMapaService` | 1.93 | 14 | 27 | Service/Facade |

## Top 20 Classes com Mais Branches

| Rank | Classe | Branches | Covered | Missed | Coverage % | Categoria |
|------|--------|----------|---------|--------|------------|------------|
| 1 | `SubprocessoService` | 253 | 230 | 23 | 90.9% | Service/Facade |
| 2 | `ProcessoService` | 178 | 154 | 24 | 86.5% | Service/Facade |
| 3 | `SubprocessoTransicaoService` | 102 | 86 | 16 | 84.3% | Service/Facade |
| 4 | `SgcPermissionEvaluator` | 68 | 65 | 3 | 95.6% | Model/Entity |
| 5 | `SubprocessoValidacaoService` | 56 | 55 | 1 | 98.2% | Service/Facade |
| 6 | `ImpactoMapaService` | 54 | 53 | 1 | 98.1% | Service/Facade |
| 7 | `PainelFacade` | 52 | 48 | 4 | 92.3% | Service/Facade |
| 8 | `UnidadeHierarquiaService` | 48 | 45 | 3 | 93.8% | Service/Facade |
| 9 | `AlertaFacade` | 40 | 39 | 1 | 97.5% | Service/Facade |
| 10 | `MapaSalvamentoService` | 36 | 35 | 1 | 97.2% | Service/Facade |
| 11 | `LimitadorTentativasLogin` | 32 | 27 | 5 | 84.4% | Model/Entity |
| 12 | `SubprocessoNotificacaoService` | 28 | 22 | 6 | 78.6% | Service/Facade |
| 13 | `CopiaMapaService` | 26 | 25 | 1 | 96.2% | Service/Facade |
| 14 | `Subprocesso` | 22 | 21 | 1 | 95.5% | Model/Entity |
| 15 | `LoginFacade` | 22 | 20 | 2 | 90.9% | Service/Facade |
| 16 | `LoginController` | 20 | 19 | 1 | 95.0% | Controller |
| 17 | `MapaManutencaoService` | 18 | 15 | 3 | 83.3% | Service/Facade |
| 18 | `GerenciadorJwt` | 18 | 17 | 1 | 94.4% | Model/Entity |
| 19 | `FiltroJwt` | 18 | 18 | 0 | 100.0% | Model/Entity |
| 20 | `ResponsavelUnidadeService` | 16 | 11 | 5 | 68.8% | Service/Facade |
