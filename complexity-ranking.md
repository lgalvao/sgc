# Ranking de Complexidade do Backend - SGC

Este relatório apresenta uma análise detalhada da complexidade do código backend, baseado em métricas do Jacoco e análise de complexidade ciclomática.

## Resumo Executivo

- **Total de Classes Analisadas:** 162
- **Complexidade Ciclomática Total:** 1596
- **Total de Branches:** 1231
- **Total de Linhas de Código:** 4449
- **Complexidade Média por Classe:** 9.85

## Metodologia

O **Complexity Score** é calculado através da fórmula:

```
Score = (Complexidade Ciclomática × 0.40) + 
        (Total de Branches × 0.30) + 
        (Linhas de Código ÷ 10 × 0.20) + 
        (Complexidade Média por Método × 5 × 0.10)
```

Esta fórmula pondera múltiplos fatores de complexidade:
- **Complexidade Ciclomática** (40%): Número de caminhos independentes no código
- **Branches** (30%): Pontos de decisão (if, switch, loops)
- **Linhas de Código** (20%): Tamanho da classe
- **Complexidade Média por Método** (10%): Densidade de complexidade

## Top 50 Classes Mais Complexas

| Rank | Classe | Pacote | Score | Complexity | Branches | Linhas | Métodos | Avg/Método | Categoria |
|------|--------|--------|-------|------------|----------|---------|---------|------------|------------|
| 1 | `SubprocessoWorkflowService` | `sgc.subprocesso.service.workflow` | 71.2 | 95 | 84 | 353 | 53 | 1.8 | Service/Facade |
| 2 | `SubprocessoAccessPolicy` | `sgc.seguranca.acesso` | 67.0 | 71 | 108 | 183 | 14 | 5.1 | Model/Entity |
| 3 | `UnidadeFacade` | `sgc.organizacao` | 59.3 | 89 | 64 | 184 | 57 | 1.6 | Service/Facade |
| 4 | `SubprocessoFacade` | `sgc.subprocesso.service` | 58.3 | 99 | 43 | 258 | 77 | 1.3 | Service/Facade |
| 5 | `PainelFacade` | `sgc.painel` | 41.3 | 46 | 64 | 104 | 14 | 3.3 | Service/Facade |
| 6 | `UsuarioFacade` | `sgc.organizacao` | 35.9 | 56 | 34 | 128 | 39 | 1.4 | Service/Facade |
| 7 | `EventoProcessoListener` | `sgc.processo.listener` | 30.3 | 36 | 41 | 122 | 15 | 2.4 | Listener |
| 8 | `ProcessoFacade` | `sgc.processo.service` | 29.8 | 44 | 30 | 122 | 29 | 1.5 | Service/Facade |
| 9 | `ImpactoMapaService` | `sgc.mapa.service` | 29.4 | 41 | 32 | 131 | 25 | 1.6 | Service/Facade |
| 10 | `ValidadorDadosOrgService` | `sgc.organizacao.service` | 26.9 | 33 | 38 | 57 | 14 | 2.4 | Service/Facade |
| 11 | `ProcessoInicializador` | `sgc.processo.service` | 25.7 | 30 | 36 | 79 | 11 | 2.7 | Model/Entity |
| 12 | `SubprocessoCrudService` | `sgc.subprocesso.service.crud` | 25.6 | 34 | 30 | 105 | 19 | 1.8 | Service/Facade |
| 13 | `MapaSalvamentoService` | `sgc.mapa.service` | 25.5 | 31 | 34 | 89 | 14 | 2.2 | Service/Facade |
| 14 | `SubprocessoEmailService` | `sgc.subprocesso.service.notificacao` | 24.8 | 28 | 35 | 57 | 7 | 4.0 | Service/Facade |
| 15 | `ProcessoDetalheBuilder` | `sgc.processo.service` | 23.4 | 25 | 34 | 82 | 8 | 3.1 | Model/Entity |
| 16 | `AlertaFacade` | `sgc.alerta` | 23.0 | 33 | 24 | 90 | 21 | 1.6 | Service/Facade |
| 17 | `CopiaMapaService` | `sgc.mapa.service` | 21.5 | 24 | 30 | 76 | 9 | 2.7 | Service/Facade |
| 18 | `LoginFacade` | `sgc.seguranca.login` | 20.9 | 27 | 26 | 69 | 14 | 1.9 | Service/Facade |
| 19 | `LimitadorTentativasLogin` | `sgc.seguranca.login` | 20.4 | 25 | 28 | 45 | 11 | 2.3 | Model/Entity |
| 20 | `SubprocessoValidacaoService` | `sgc.subprocesso.service.crud` | 18.5 | 22 | 24 | 70 | 10 | 2.2 | Service/Facade |
| 21 | `ProcessoAcessoService` | `sgc.processo.service` | 17.9 | 19 | 26 | 47 | 6 | 3.2 | Service/Facade |
| 22 | `Usuario` | `sgc.organizacao.model` | 16.4 | 20 | 22 | 36 | 9 | 2.2 | Model/Entity |
| 23 | `RelatorioFacade` | `sgc.relatorio.service` | 15.6 | 14 | 22 | 55 | 3 | 4.7 | Service/Facade |
| 24 | `AccessControlService` | `sgc.seguranca.acesso` | 14.5 | 16 | 18 | 33 | 4 | 4.0 | Service/Facade |
| 25 | `ProcessoValidador` | `sgc.processo.service` | 14.0 | 17 | 18 | 36 | 8 | 2.1 | Model/Entity |
| 26 | `RestExceptionHandler` | `sgc.comum.erros` | 13.3 | 21 | 10 | 61 | 16 | 1.3 | Model/Entity |
| 27 | `SubprocessoFactory` | `sgc.subprocesso.service.factory` | 13.2 | 15 | 16 | 68 | 7 | 2.1 | Model/Entity |
| 28 | `AtividadeService` | `sgc.mapa.service` | 12.8 | 19 | 12 | 45 | 13 | 1.5 | Service/Facade |
| 29 | `SubprocessoDetalheMapper` | `sgc.subprocesso.mapper` | 12.8 | 13 | 18 | 28 | 4 | 3.2 | Mapper |
| 30 | `AtividadeFacade` | `sgc.mapa.service` | 11.2 | 17 | 6 | 100 | 14 | 1.2 | Service/Facade |
| 31 | `NotificacaoEmailService` | `sgc.notificacao` | 11.0 | 14 | 12 | 44 | 8 | 1.8 | Service/Facade |
| 32 | `AtividadeAccessPolicy` | `sgc.seguranca.acesso` | 10.8 | 9 | 14 | 37 | 2 | 4.5 | Model/Entity |
| 33 | `ProcessoController` | `sgc.processo` | 10.6 | 20 | 4 | 43 | 18 | 1.1 | Controller |
| 34 | `SubprocessoCadastroController` | `sgc.subprocesso` | 10.2 | 18 | 4 | 61 | 16 | 1.1 | Controller |
| 35 | `GerenciadorJwt` | `sgc.seguranca.login` | 9.8 | 11 | 12 | 36 | 5 | 2.2 | Model/Entity |
| 36 | `MapaVisualizacaoService` | `sgc.mapa.service` | 9.8 | 12 | 10 | 57 | 7 | 1.7 | Service/Facade |
| 37 | `ConhecimentoService` | `sgc.mapa.service` | 9.1 | 18 | 2 | 39 | 17 | 1.1 | Service/Facade |
| 38 | `UsuarioPerfil` | `sgc.organizacao.model` | 9.0 | 10 | 12 | 8 | 4 | 2.5 | Model/Entity |
| 39 | `ProcessoConsultaService` | `sgc.processo.service` | 8.3 | 11 | 8 | 37 | 7 | 1.6 | Service/Facade |
| 40 | `CompetenciaService` | `sgc.mapa.service` | 8.3 | 16 | 2 | 36 | 15 | 1.1 | Service/Facade |
| 41 | `UnidadeController` | `sgc.organizacao` | 7.9 | 14 | 4 | 25 | 12 | 1.2 | Controller |
| 42 | `MapaFacade` | `sgc.mapa.service` | 7.8 | 15 | 2 | 31 | 14 | 1.1 | Service/Facade |
| 43 | `AccessAuditService` | `sgc.seguranca.acesso` | 7.4 | 9 | 8 | 16 | 4 | 2.2 | Service/Facade |
| 44 | `AnaliseFacade` | `sgc.analise` | 7.3 | 9 | 8 | 22 | 5 | 1.8 | Service/Facade |
| 45 | `UsuarioMapper` | `sgc.organizacao.mapper` | 7.3 | 8 | 8 | 33 | 4 | 2.0 | Mapper |
| 46 | `SubprocessoMapaController` | `sgc.subprocesso` | 7.2 | 15 | 0 | 35 | 15 | 1.0 | Controller |
| 47 | `HierarquiaService` | `sgc.organizacao.service` | 6.6 | 7 | 8 | 11 | 3 | 2.3 | Service/Facade |
| 48 | `MapaCompletoMapper` | `sgc.mapa.mapper` | 6.5 | 6 | 8 | 12 | 2 | 3.0 | Mapper |
| 49 | `Subprocesso` | `sgc.subprocesso.model` | 5.9 | 9 | 4 | 25 | 7 | 1.3 | Model/Entity |
| 50 | `TipoTransicao` | `sgc.subprocesso.eventos` | 5.9 | 7 | 6 | 21 | 4 | 1.8 | Model/Entity |

## Análise por Categoria

### Controller

Total de classes: 16

| Classe | Score | Complexity | Branches | Linhas | Cobertura Branches |
|--------|-------|------------|----------|--------|--------------------|
| `ProcessoController` | 10.6 | 20 | 4 | 43 | 75.0% |
| `SubprocessoCadastroController` | 10.2 | 18 | 4 | 61 | 100.0% |
| `UnidadeController` | 7.9 | 14 | 4 | 25 | 50.0% |
| `SubprocessoMapaController` | 7.2 | 15 | 0 | 35 | 100.0% |
| `SubprocessoCrudController` | 5.8 | 12 | 0 | 23 | 100.0% |
| `SubprocessoValidacaoController` | 5.4 | 11 | 0 | 26 | 100.0% |
| `AlertaController` | 5.2 | 7 | 4 | 14 | 100.0% |
| `LoginController` | 5.1 | 7 | 4 | 22 | 75.0% |
| `AtividadeController` | 4.0 | 8 | 0 | 16 | 100.0% |
| `AnaliseController` | 2.9 | 5 | 0 | 18 | 100.0% |

### DTO

Total de classes: 12

| Classe | Score | Complexity | Branches | Linhas | Cobertura Branches |
|--------|-------|------------|----------|--------|--------------------|
| `EntrarRequest` | 1.0 | 1 | 0 | 5 | 100.0% |
| `AutenticarRequest` | 1.0 | 1 | 0 | 4 | 100.0% |
| `IniciarProcessoRequest` | 0.9 | 1 | 0 | 1 | 100.0% |
| `CriarAtribuicaoTemporariaRequest` | 0.9 | 1 | 0 | 1 | 100.0% |
| `AtividadeResponse` | 0.9 | 1 | 0 | 1 | 100.0% |
| `CriarConhecimentoRequest` | 0.9 | 1 | 0 | 1 | 100.0% |
| `AtualizarConhecimentoRequest` | 0.9 | 1 | 0 | 1 | 100.0% |
| `ConhecimentoResponse` | 0.9 | 1 | 0 | 1 | 100.0% |
| `CriarAtividadeRequest` | 0.9 | 1 | 0 | 1 | 100.0% |
| `AtualizarAtividadeRequest` | 0.9 | 1 | 0 | 1 | 100.0% |

### Listener

Total de classes: 3

| Classe | Score | Complexity | Branches | Linhas | Cobertura Branches |
|--------|-------|------------|----------|--------|--------------------|
| `EventoProcessoListener` | 30.3 | 36 | 41 | 122 | 85.4% |
| `SubprocessoComunicacaoListener` | 4.3 | 5 | 4 | 13 | 100.0% |
| `SubprocessoMapaListener` | 1.4 | 2 | 0 | 3 | 100.0% |

### Mapper

Total de classes: 10

| Classe | Score | Complexity | Branches | Linhas | Cobertura Branches |
|--------|-------|------------|----------|--------|--------------------|
| `SubprocessoDetalheMapper` | 12.8 | 13 | 18 | 28 | 100.0% |
| `UsuarioMapper` | 7.3 | 8 | 8 | 33 | 100.0% |
| `MapaCompletoMapper` | 6.5 | 6 | 8 | 12 | 50.0% |
| `MapaAjusteMapper` | 5.9 | 6 | 6 | 33 | 83.3% |
| `SubprocessoMapper` | 5.6 | 7 | 6 | 7 | 100.0% |
| `AlertaMapper` | 3.6 | 5 | 2 | 17 | 100.0% |
| `AnaliseMapper` | 2.7 | 3 | 2 | 5 | 50.0% |
| `ConhecimentoMapper` | 2.6 | 3 | 2 | 4 | 100.0% |
| `ProcessoMapper` | 2.6 | 2 | 2 | 8 | 100.0% |
| `AtividadeMapper` | 0.9 | 1 | 0 | 1 | 100.0% |

### Model/Entity

Total de classes: 85

| Classe | Score | Complexity | Branches | Linhas | Cobertura Branches |
|--------|-------|------------|----------|--------|--------------------|
| `SubprocessoAccessPolicy` | 67.0 | 71 | 108 | 183 | 90.7% |
| `ProcessoInicializador` | 25.7 | 30 | 36 | 79 | 94.4% |
| `ProcessoDetalheBuilder` | 23.4 | 25 | 34 | 82 | 91.2% |
| `LimitadorTentativasLogin` | 20.4 | 25 | 28 | 45 | 96.4% |
| `Usuario` | 16.4 | 20 | 22 | 36 | 86.4% |
| `ProcessoValidador` | 14.0 | 17 | 18 | 36 | 94.4% |
| `RestExceptionHandler` | 13.3 | 21 | 10 | 61 | 90.0% |
| `SubprocessoFactory` | 13.2 | 15 | 16 | 68 | 93.8% |
| `AtividadeAccessPolicy` | 10.8 | 9 | 14 | 37 | 92.9% |
| `GerenciadorJwt` | 9.8 | 11 | 12 | 36 | 75.0% |

### Repository

Total de classes: 1

| Classe | Score | Complexity | Branches | Linhas | Cobertura Branches |
|--------|-------|------------|----------|--------|--------------------|
| `RepositorioComum` | 2.7 | 3 | 2 | 5 | 100.0% |

### Service/Facade

Total de classes: 35

| Classe | Score | Complexity | Branches | Linhas | Cobertura Branches |
|--------|-------|------------|----------|--------|--------------------|
| `SubprocessoWorkflowService` | 71.2 | 95 | 84 | 353 | 96.4% |
| `UnidadeFacade` | 59.3 | 89 | 64 | 184 | 89.1% |
| `SubprocessoFacade` | 58.3 | 99 | 43 | 258 | 95.3% |
| `PainelFacade` | 41.3 | 46 | 64 | 104 | 96.9% |
| `UsuarioFacade` | 35.9 | 56 | 34 | 128 | 88.2% |
| `ProcessoFacade` | 29.8 | 44 | 30 | 122 | 100.0% |
| `ImpactoMapaService` | 29.4 | 41 | 32 | 131 | 93.8% |
| `ValidadorDadosOrgService` | 26.9 | 33 | 38 | 57 | 92.1% |
| `SubprocessoCrudService` | 25.6 | 34 | 30 | 105 | 86.7% |
| `MapaSalvamentoService` | 25.5 | 31 | 34 | 89 | 94.1% |

## Top 20 Classes com Maior Complexidade por Método

Classes onde cada método é, em média, mais complexo:

| Rank | Classe | Avg Complexity/Método | Métodos | Total Complexity | Categoria |
|------|--------|-----------------------|---------|------------------|------------|
| 1 | `SubprocessoAccessPolicy` | 5.07 | 14 | 71 | Model/Entity |
| 2 | `RelatorioFacade` | 4.67 | 3 | 14 | Service/Facade |
| 3 | `AtividadeAccessPolicy` | 4.50 | 2 | 9 | Model/Entity |
| 4 | `SubprocessoEmailService` | 4.00 | 7 | 28 | Service/Facade |
| 5 | `AccessControlService` | 4.00 | 4 | 16 | Service/Facade |
| 6 | `PainelFacade` | 3.29 | 14 | 46 | Service/Facade |
| 7 | `SubprocessoDetalheMapper` | 3.25 | 4 | 13 | Mapper |
| 8 | `ProcessoAcessoService` | 3.17 | 6 | 19 | Service/Facade |
| 9 | `ProcessoDetalheBuilder` | 3.12 | 8 | 25 | Model/Entity |
| 10 | `PropriedadesAcessoAd` | 3.00 | 1 | 3 | Model/Entity |
| 11 | `MapaCompletoMapper` | 3.00 | 2 | 6 | Mapper |
| 12 | `ProcessoInicializador` | 2.73 | 11 | 30 | Model/Entity |
| 13 | `CopiaMapaService` | 2.67 | 9 | 24 | Service/Facade |
| 14 | `ConfigCorsProperties` | 2.50 | 2 | 5 | Model/Entity |
| 15 | `UsuarioPerfil` | 2.50 | 4 | 10 | Model/Entity |
| 16 | `EventoProcessoListener` | 2.40 | 15 | 36 | Listener |
| 17 | `ValidadorDadosOrgService` | 2.36 | 14 | 33 | Service/Facade |
| 18 | `HierarquiaService` | 2.33 | 3 | 7 | Service/Facade |
| 19 | `LimitadorTentativasLogin` | 2.27 | 11 | 25 | Model/Entity |
| 20 | `AccessAuditService` | 2.25 | 4 | 9 | Service/Facade |

## Top 20 Classes com Mais Branches

Classes com maior número de pontos de decisão:

| Rank | Classe | Branches | Covered | Missed | Coverage % | Categoria |
|------|--------|----------|---------|--------|------------|------------|
| 1 | `SubprocessoAccessPolicy` | 108 | 98 | 10 | 90.7% | Model/Entity |
| 2 | `SubprocessoWorkflowService` | 84 | 81 | 3 | 96.4% | Service/Facade |
| 3 | `UnidadeFacade` | 64 | 57 | 7 | 89.1% | Service/Facade |
| 4 | `PainelFacade` | 64 | 62 | 2 | 96.9% | Service/Facade |
| 5 | `SubprocessoFacade` | 43 | 41 | 2 | 95.3% | Service/Facade |
| 6 | `EventoProcessoListener` | 41 | 35 | 6 | 85.4% | Listener |
| 7 | `ValidadorDadosOrgService` | 38 | 35 | 3 | 92.1% | Service/Facade |
| 8 | `ProcessoInicializador` | 36 | 34 | 2 | 94.4% | Model/Entity |
| 9 | `SubprocessoEmailService` | 35 | 34 | 1 | 97.1% | Service/Facade |
| 10 | `UsuarioFacade` | 34 | 30 | 4 | 88.2% | Service/Facade |
| 11 | `MapaSalvamentoService` | 34 | 32 | 2 | 94.1% | Service/Facade |
| 12 | `ProcessoDetalheBuilder` | 34 | 31 | 3 | 91.2% | Model/Entity |
| 13 | `ImpactoMapaService` | 32 | 30 | 2 | 93.8% | Service/Facade |
| 14 | `SubprocessoCrudService` | 30 | 26 | 4 | 86.7% | Service/Facade |
| 15 | `CopiaMapaService` | 30 | 26 | 4 | 86.7% | Service/Facade |
| 16 | `ProcessoFacade` | 30 | 30 | 0 | 100.0% | Service/Facade |
| 17 | `LimitadorTentativasLogin` | 28 | 27 | 1 | 96.4% | Model/Entity |
| 18 | `LoginFacade` | 26 | 24 | 2 | 92.3% | Service/Facade |
| 19 | `ProcessoAcessoService` | 26 | 23 | 3 | 88.5% | Service/Facade |
| 20 | `AlertaFacade` | 24 | 23 | 1 | 95.8% | Service/Facade |

## Ranking Completo de Todas as Classes

Lista completa ordenada por Complexity Score:

| Rank | Classe Completa | Score | Complexity | Branches | Linhas | Métodos | Categoria |
|------|----------------|-------|------------|----------|--------|---------|------------|
| 1 | `sgc.subprocesso.service.workflow.SubprocessoWorkflowService` | 71.2 | 95 | 84 | 353 | 53 | Service/Facade |
| 2 | `sgc.seguranca.acesso.SubprocessoAccessPolicy` | 67.0 | 71 | 108 | 183 | 14 | Model/Entity |
| 3 | `sgc.organizacao.UnidadeFacade` | 59.3 | 89 | 64 | 184 | 57 | Service/Facade |
| 4 | `sgc.subprocesso.service.SubprocessoFacade` | 58.3 | 99 | 43 | 258 | 77 | Service/Facade |
| 5 | `sgc.painel.PainelFacade` | 41.3 | 46 | 64 | 104 | 14 | Service/Facade |
| 6 | `sgc.organizacao.UsuarioFacade` | 35.9 | 56 | 34 | 128 | 39 | Service/Facade |
| 7 | `sgc.processo.listener.EventoProcessoListener` | 30.3 | 36 | 41 | 122 | 15 | Listener |
| 8 | `sgc.processo.service.ProcessoFacade` | 29.8 | 44 | 30 | 122 | 29 | Service/Facade |
| 9 | `sgc.mapa.service.ImpactoMapaService` | 29.4 | 41 | 32 | 131 | 25 | Service/Facade |
| 10 | `sgc.organizacao.service.ValidadorDadosOrgService` | 26.9 | 33 | 38 | 57 | 14 | Service/Facade |
| 11 | `sgc.processo.service.ProcessoInicializador` | 25.7 | 30 | 36 | 79 | 11 | Model/Entity |
| 12 | `sgc.subprocesso.service.crud.SubprocessoCrudService` | 25.6 | 34 | 30 | 105 | 19 | Service/Facade |
| 13 | `sgc.mapa.service.MapaSalvamentoService` | 25.5 | 31 | 34 | 89 | 14 | Service/Facade |
| 14 | `sgc.subprocesso.service.notificacao.SubprocessoEmailService` | 24.8 | 28 | 35 | 57 | 7 | Service/Facade |
| 15 | `sgc.processo.service.ProcessoDetalheBuilder` | 23.4 | 25 | 34 | 82 | 8 | Model/Entity |
| 16 | `sgc.alerta.AlertaFacade` | 23.0 | 33 | 24 | 90 | 21 | Service/Facade |
| 17 | `sgc.mapa.service.CopiaMapaService` | 21.5 | 24 | 30 | 76 | 9 | Service/Facade |
| 18 | `sgc.seguranca.login.LoginFacade` | 20.9 | 27 | 26 | 69 | 14 | Service/Facade |
| 19 | `sgc.seguranca.login.LimitadorTentativasLogin` | 20.4 | 25 | 28 | 45 | 11 | Model/Entity |
| 20 | `sgc.subprocesso.service.crud.SubprocessoValidacaoService` | 18.5 | 22 | 24 | 70 | 10 | Service/Facade |
| 21 | `sgc.processo.service.ProcessoAcessoService` | 17.9 | 19 | 26 | 47 | 6 | Service/Facade |
| 22 | `sgc.organizacao.model.Usuario` | 16.4 | 20 | 22 | 36 | 9 | Model/Entity |
| 23 | `sgc.relatorio.service.RelatorioFacade` | 15.6 | 14 | 22 | 55 | 3 | Service/Facade |
| 24 | `sgc.seguranca.acesso.AccessControlService` | 14.5 | 16 | 18 | 33 | 4 | Service/Facade |
| 25 | `sgc.processo.service.ProcessoValidador` | 14.0 | 17 | 18 | 36 | 8 | Model/Entity |
| 26 | `sgc.comum.erros.RestExceptionHandler` | 13.3 | 21 | 10 | 61 | 16 | Model/Entity |
| 27 | `sgc.subprocesso.service.factory.SubprocessoFactory` | 13.2 | 15 | 16 | 68 | 7 | Model/Entity |
| 28 | `sgc.mapa.service.AtividadeService` | 12.8 | 19 | 12 | 45 | 13 | Service/Facade |
| 29 | `sgc.subprocesso.mapper.SubprocessoDetalheMapper` | 12.8 | 13 | 18 | 28 | 4 | Mapper |
| 30 | `sgc.mapa.service.AtividadeFacade` | 11.2 | 17 | 6 | 100 | 14 | Service/Facade |
| 31 | `sgc.notificacao.NotificacaoEmailService` | 11.0 | 14 | 12 | 44 | 8 | Service/Facade |
| 32 | `sgc.seguranca.acesso.AtividadeAccessPolicy` | 10.8 | 9 | 14 | 37 | 2 | Model/Entity |
| 33 | `sgc.processo.ProcessoController` | 10.6 | 20 | 4 | 43 | 18 | Controller |
| 34 | `sgc.subprocesso.SubprocessoCadastroController` | 10.2 | 18 | 4 | 61 | 16 | Controller |
| 35 | `sgc.seguranca.login.GerenciadorJwt` | 9.8 | 11 | 12 | 36 | 5 | Model/Entity |
| 36 | `sgc.mapa.service.MapaVisualizacaoService` | 9.8 | 12 | 10 | 57 | 7 | Service/Facade |
| 37 | `sgc.mapa.service.ConhecimentoService` | 9.1 | 18 | 2 | 39 | 17 | Service/Facade |
| 38 | `sgc.organizacao.model.UsuarioPerfil` | 9.0 | 10 | 12 | 8 | 4 | Model/Entity |
| 39 | `sgc.processo.service.ProcessoConsultaService` | 8.3 | 11 | 8 | 37 | 7 | Service/Facade |
| 40 | `sgc.mapa.service.CompetenciaService` | 8.3 | 16 | 2 | 36 | 15 | Service/Facade |
| 41 | `sgc.organizacao.UnidadeController` | 7.9 | 14 | 4 | 25 | 12 | Controller |
| 42 | `sgc.mapa.service.MapaFacade` | 7.8 | 15 | 2 | 31 | 14 | Service/Facade |
| 43 | `sgc.seguranca.acesso.AccessAuditService` | 7.4 | 9 | 8 | 16 | 4 | Service/Facade |
| 44 | `sgc.analise.AnaliseFacade` | 7.3 | 9 | 8 | 22 | 5 | Service/Facade |
| 45 | `sgc.organizacao.mapper.UsuarioMapper` | 7.3 | 8 | 8 | 33 | 4 | Mapper |
| 46 | `sgc.subprocesso.SubprocessoMapaController` | 7.2 | 15 | 0 | 35 | 15 | Controller |
| 47 | `sgc.organizacao.service.HierarquiaService` | 6.6 | 7 | 8 | 11 | 3 | Service/Facade |
| 48 | `sgc.mapa.mapper.MapaCompletoMapper` | 6.5 | 6 | 8 | 12 | 2 | Mapper |
| 49 | `sgc.subprocesso.model.Subprocesso` | 5.9 | 9 | 4 | 25 | 7 | Model/Entity |
| 50 | `sgc.subprocesso.eventos.TipoTransicao` | 5.9 | 7 | 6 | 21 | 4 | Model/Entity |
| 51 | `sgc.subprocesso.mapper.MapaAjusteMapper` | 5.9 | 6 | 6 | 33 | 3 | Mapper |
| 52 | `sgc.subprocesso.SubprocessoCrudController` | 5.8 | 12 | 0 | 23 | 12 | Controller |
| 53 | `sgc.subprocesso.mapper.SubprocessoMapper` | 5.6 | 7 | 6 | 7 | 4 | Mapper |
| 54 | `sgc.seguranca.login.FiltroJwt` | 5.5 | 6 | 6 | 15 | 3 | Model/Entity |
| 55 | `sgc.subprocesso.SubprocessoValidacaoController` | 5.4 | 11 | 0 | 26 | 11 | Controller |
| 56 | `sgc.comum.erros.ErroApi` | 5.4 | 8 | 4 | 17 | 6 | Model/Entity |
| 57 | `sgc.seguranca.config.ConfigCorsProperties` | 5.2 | 5 | 6 | 8 | 2 | Model/Entity |
| 58 | `sgc.alerta.AlertaController` | 5.2 | 7 | 4 | 14 | 4 | Controller |
| 59 | `sgc.seguranca.login.LoginController` | 5.1 | 7 | 4 | 22 | 5 | Controller |
| 60 | `sgc.notificacao.NotificacaoModelosService` | 5.1 | 9 | 0 | 51 | 9 | Service/Facade |
| 61 | `sgc.processo.service.ProcessoFinalizador` | 5.1 | 8 | 2 | 35 | 7 | Model/Entity |
| 62 | `sgc.seguranca.login.ClienteAcessoAd` | 4.7 | 6 | 4 | 18 | 4 | Model/Entity |
| 63 | `sgc.notificacao.NotificacaoEmailAsyncExecutor` | 4.7 | 5 | 4 | 33 | 3 | Model/Entity |
| 64 | `sgc.subprocesso.service.notificacao.SubprocessoComunicacaoListener` | 4.3 | 5 | 4 | 13 | 3 | Listener |
| 65 | `sgc.seguranca.acesso.ProcessoAccessPolicy` | 4.1 | 4 | 4 | 16 | 2 | Model/Entity |
| 66 | `sgc.comum.util.FormatadorData` | 4.1 | 5 | 4 | 4 | 3 | Model/Entity |
| 67 | `sgc.seguranca.acesso.MapaAccessPolicy` | 4.1 | 4 | 4 | 14 | 2 | Model/Entity |
| 68 | `sgc.mapa.AtividadeController` | 4.0 | 8 | 0 | 16 | 8 | Controller |
| 69 | `sgc.seguranca.acesso.AbstractAccessPolicy` | 4.0 | 8 | 0 | 16 | 8 | Model/Entity |
| 70 | `sgc.seguranca.login.PropriedadesAcessoAd` | 4.0 | 3 | 4 | 4 | 1 | Model/Entity |
| 71 | `sgc.seguranca.sanitizacao.DeserializadorHtmlSanitizado` | 3.9 | 4 | 4 | 6 | 2 | Model/Entity |
| 72 | `sgc.seguranca.config.ConfigSeguranca` | 3.8 | 7 | 0 | 23 | 7 | Model/Entity |
| 73 | `sgc.alerta.mapper.AlertaMapper` | 3.6 | 5 | 2 | 17 | 4 | Mapper |
| 74 | `sgc.comum.util.MonitoramentoAspect` | 3.1 | 4 | 2 | 12 | 3 | Model/Entity |
| 75 | `sgc.subprocesso.service.workflow.SubprocessoTransicaoService` | 2.9 | 4 | 0 | 39 | 4 | Service/Facade |
| 76 | `sgc.analise.AnaliseController` | 2.9 | 5 | 0 | 18 | 5 | Controller |
| 77 | `sgc.mapa.MapaController` | 2.7 | 5 | 0 | 12 | 5 | Controller |
| 78 | `sgc.organizacao.UsuarioController` | 2.7 | 5 | 0 | 10 | 5 | Controller |
| 79 | `sgc.configuracao.ConfiguracaoFacade` | 2.7 | 5 | 0 | 8 | 5 | Service/Facade |
| 80 | `sgc.analise.mapper.AnaliseMapper` | 2.7 | 3 | 2 | 5 | 2 | Mapper |
| 81 | `sgc.comum.repo.RepositorioComum` | 2.7 | 3 | 2 | 5 | 2 | Repository |
| 82 | `sgc.mapa.mapper.ConhecimentoMapper` | 2.6 | 3 | 2 | 4 | 2 | Mapper |
| 83 | `sgc.seguranca.sanitizacao.UtilSanitizacao` | 2.6 | 3 | 2 | 2 | 2 | Model/Entity |
| 84 | `sgc.processo.mapper.ProcessoMapper` | 2.6 | 2 | 2 | 8 | 1 | Mapper |
| 85 | `sgc.seguranca.acesso.Acao` | 2.5 | 2 | 0 | 61 | 2 | Model/Entity |
| 86 | `sgc.seguranca.config.JwtProperties` | 2.5 | 2 | 2 | 3 | 1 | Model/Entity |
| 87 | `sgc.organizacao.model.AtribuicaoTemporaria` | 2.4 | 2 | 2 | 1 | 1 | Model/Entity |
| 88 | `sgc.comum.erros.ErroNegocioBase` | 2.0 | 3 | 0 | 15 | 3 | Model/Entity |
| 89 | `sgc.comum.config.ConfigThymeleaf` | 2.0 | 3 | 0 | 13 | 3 | Model/Entity |
| 90 | `sgc.mapa.service.ImpactoMapaService.CompetenciaImpactoAcumulador` | 1.9 | 3 | 0 | 10 | 3 | Service/Facade |
| 91 | `sgc.mapa.model.Conhecimento` | 1.9 | 3 | 0 | 9 | 3 | Model/Entity |
| 92 | `sgc.organizacao.model.Unidade` | 1.9 | 3 | 0 | 8 | 3 | Model/Entity |
| 93 | `sgc.relatorio.service.PdfFactory` | 1.8 | 3 | 0 | 4 | 3 | Model/Entity |
| 94 | `sgc.subprocesso.model.Movimentacao` | 1.6 | 2 | 0 | 14 | 2 | Model/Entity |
| 95 | `sgc.processo.model.Processo` | 1.6 | 2 | 0 | 14 | 2 | Model/Entity |
| 96 | `sgc.seguranca.config.ConfigCors` | 1.5 | 2 | 0 | 11 | 2 | Model/Entity |
| 97 | `sgc.mapa.model.Competencia` | 1.5 | 2 | 0 | 9 | 2 | Model/Entity |
| 98 | `sgc.relatorio.controller.RelatorioController` | 1.5 | 2 | 0 | 8 | 2 | Controller |
| 99 | `sgc.organizacao.model.Perfil` | 1.4 | 2 | 0 | 6 | 2 | Model/Entity |
| 100 | `sgc.comum.config.ConfigOpenApi` | 1.4 | 2 | 0 | 5 | 2 | Model/Entity |
| 101 | `sgc.comum.erros.ErroEntidadeNaoEncontrada` | 1.4 | 2 | 0 | 4 | 2 | Model/Entity |
| 102 | `sgc.comum.erros.ErroValidacao` | 1.4 | 2 | 0 | 4 | 2 | Model/Entity |
| 103 | `sgc.painel.PainelController` | 1.4 | 2 | 0 | 4 | 2 | Controller |
| 104 | `sgc.subprocesso.listener.SubprocessoMapaListener` | 1.4 | 2 | 0 | 3 | 2 | Listener |
| 105 | `sgc.comum.util.Sleeper` | 1.4 | 2 | 0 | 3 | 2 | Model/Entity |
| 106 | `sgc.configuracao.ConfiguracaoController` | 1.3 | 2 | 0 | 2 | 2 | Controller |
| 107 | `sgc.subprocesso.model.SituacaoSubprocesso` | 1.3 | 1 | 0 | 21 | 1 | Model/Entity |
| 108 | `sgc.mapa.service.MapaSalvamentoService.ContextoSalvamento` | 1.0 | 1 | 0 | 7 | 1 | Service/Facade |
| 109 | `sgc.organizacao.model.TipoUnidade` | 1.0 | 1 | 0 | 6 | 1 | Model/Entity |
| 110 | `sgc.seguranca.acesso.SubprocessoAccessPolicy.RequisitoHierarquia` | 1.0 | 1 | 0 | 6 | 1 | Model/Entity |
| 111 | `sgc.mapa.model.Atividade` | 1.0 | 1 | 0 | 6 | 1 | Model/Entity |
| 112 | `sgc.seguranca.login.dto.EntrarRequest` | 1.0 | 1 | 0 | 5 | 1 | DTO |
| 113 | `sgc.analise.model.TipoAcaoAnalise` | 1.0 | 1 | 0 | 5 | 1 | Model/Entity |
| 114 | `sgc.processo.model.TipoProcesso` | 1.0 | 1 | 0 | 4 | 1 | Model/Entity |
| 115 | `sgc.processo.model.SituacaoProcesso` | 1.0 | 1 | 0 | 4 | 1 | Model/Entity |
| 116 | `sgc.seguranca.login.dto.AutenticarRequest` | 1.0 | 1 | 0 | 4 | 1 | DTO |
| 117 | `sgc.mapa.model.TipoImpactoAtividade` | 1.0 | 1 | 0 | 4 | 1 | Model/Entity |
| 118 | `sgc.processo.eventos.EventoProcessoCriado` | 1.0 | 1 | 0 | 3 | 1 | Model/Entity |
| 119 | `sgc.seguranca.login.ConfiguracaoAcessoAd` | 1.0 | 1 | 0 | 3 | 1 | Model/Entity |
| 120 | `sgc.organizacao.model.SituacaoUnidade` | 1.0 | 1 | 0 | 3 | 1 | Model/Entity |
| 121 | `sgc.analise.model.TipoAnalise` | 1.0 | 1 | 0 | 3 | 1 | Model/Entity |
| 122 | `sgc.mapa.model.TipoImpactoCompetencia` | 1.0 | 1 | 0 | 3 | 1 | Model/Entity |
| 123 | `sgc.processo.erros.ErroProcesso` | 0.9 | 1 | 0 | 2 | 1 | Model/Entity |
| 124 | `sgc.processo.erros.ErroUnidadesNaoDefinidas` | 0.9 | 1 | 0 | 2 | 1 | Model/Entity |
| 125 | `sgc.processo.erros.ErroProcessoEmSituacaoInvalida` | 0.9 | 1 | 0 | 2 | 1 | Model/Entity |
| 126 | `sgc.painel.erros.ErroParametroPainelInvalido` | 0.9 | 1 | 0 | 2 | 1 | Model/Entity |
| 127 | `sgc.subprocesso.erros.ErroMapaNaoAssociado` | 0.9 | 1 | 0 | 2 | 1 | Model/Entity |
| 128 | `sgc.subprocesso.erros.ErroAtividadesEmSituacaoInvalida` | 0.9 | 1 | 0 | 2 | 1 | Model/Entity |
| 129 | `sgc.subprocesso.erros.ErroMapaEmSituacaoInvalida` | 0.9 | 1 | 0 | 2 | 1 | Model/Entity |
| 130 | `sgc.comum.erros.ErroSituacaoInvalida` | 0.9 | 1 | 0 | 2 | 1 | Model/Entity |
| 131 | `sgc.comum.erros.ErroInterno` | 0.9 | 1 | 0 | 2 | 1 | Model/Entity |
| 132 | `sgc.comum.erros.ErroAccessoNegado` | 0.9 | 1 | 0 | 2 | 1 | Model/Entity |
| 133 | `sgc.comum.erros.ErroInvarianteViolada` | 0.9 | 1 | 0 | 2 | 1 | Model/Entity |
| 134 | `sgc.comum.erros.ErroEstadoImpossivel` | 0.9 | 1 | 0 | 2 | 1 | Model/Entity |
| 135 | `sgc.comum.erros.ErroAutenticacao` | 0.9 | 1 | 0 | 2 | 1 | Model/Entity |
| 136 | `sgc.comum.erros.ErroConfiguracao` | 0.9 | 1 | 0 | 2 | 1 | Model/Entity |
| 137 | `sgc.seguranca.login.LimitadorTentativasLogin.ErroMuitasTentativas` | 0.9 | 1 | 0 | 2 | 1 | Model/Entity |
| 138 | `sgc.processo.dto.IniciarProcessoRequest` | 0.9 | 1 | 0 | 1 | 1 | DTO |
| 139 | `sgc.mapa.evento.EventoMapaAlterado` | 0.9 | 1 | 0 | 1 | 1 | Model/Entity |
| 140 | `sgc.organizacao.dto.CriarAtribuicaoTemporariaRequest` | 0.9 | 1 | 0 | 1 | 1 | DTO |
| 141 | `sgc.e2e.E2eController.ProcessoFixtureRequest` | 0.9 | 1 | 0 | 1 | 1 | Controller |
| 142 | `sgc.mapa.dto.AtividadeResponse` | 0.9 | 1 | 0 | 1 | 1 | DTO |
| 143 | `sgc.mapa.dto.CriarConhecimentoRequest` | 0.9 | 1 | 0 | 1 | 1 | DTO |
| 144 | `sgc.mapa.dto.AtualizarConhecimentoRequest` | 0.9 | 1 | 0 | 1 | 1 | DTO |
| 145 | `sgc.mapa.dto.ConhecimentoResponse` | 0.9 | 1 | 0 | 1 | 1 | DTO |
| 146 | `sgc.mapa.dto.CriarAtividadeRequest` | 0.9 | 1 | 0 | 1 | 1 | DTO |
| 147 | `sgc.mapa.dto.AtualizarAtividadeRequest` | 0.9 | 1 | 0 | 1 | 1 | DTO |
| 148 | `sgc.comum.model.EntidadeBase` | 0.9 | 1 | 0 | 1 | 1 | Model/Entity |
| 149 | `sgc.comum.erros.ErroNegocio` | 0.9 | 1 | 0 | 1 | 1 | Model/Entity |
| 150 | `sgc.analise.dto.CriarAnaliseRequest` | 0.9 | 1 | 0 | 1 | 1 | DTO |
| 151 | `sgc.analise.dto.CriarAnaliseCommand` | 0.9 | 1 | 0 | 1 | 1 | Model/Entity |
| 152 | `sgc.seguranca.login.ClienteAcessoAd.AutenticarRequest` | 0.9 | 1 | 0 | 1 | 1 | DTO |
| 153 | `sgc.seguranca.login.GerenciadorJwt.JwtClaims` | 0.9 | 1 | 0 | 1 | 1 | Model/Entity |
| 154 | `sgc.seguranca.login.dto.UsuarioAcessoAd.LotacaoAd` | 0.9 | 1 | 0 | 1 | 1 | Model/Entity |
| 155 | `sgc.seguranca.login.dto.UsuarioAcessoAd` | 0.9 | 1 | 0 | 1 | 1 | Model/Entity |
| 156 | `sgc.comum.config.ConfigAplicacao` | 0.9 | 1 | 0 | 1 | 1 | Model/Entity |
| 157 | `sgc.mapa.mapper.AtividadeMapper` | 0.9 | 1 | 0 | 1 | 1 | Mapper |
| 158 | `sgc.subprocesso.service.workflow.SubprocessoTransicaoService.RegistrarWorkflowReq` | 0.9 | 1 | 0 | 1 | 1 | Service/Facade |
| 159 | `sgc.seguranca.acesso.MapaAccessPolicy.RegrasAcaoMapa` | 0.9 | 1 | 0 | 1 | 1 | Model/Entity |
| 160 | `sgc.seguranca.acesso.ProcessoAccessPolicy.RegrasAcaoProcesso` | 0.9 | 1 | 0 | 1 | 1 | Model/Entity |
| 161 | `sgc.seguranca.acesso.AtividadeAccessPolicy.RegrasAcaoAtividade` | 0.9 | 1 | 0 | 1 | 1 | Model/Entity |
| 162 | `sgc.seguranca.acesso.SubprocessoAccessPolicy.RegrasAcao` | 0.9 | 1 | 0 | 1 | 1 | Model/Entity |

## Notas

- **Complexity**: Complexidade ciclomática (número de caminhos independentes)
- **Branches**: Pontos de decisão no código (if, switch, loops, etc.)
- **Linhas**: Total de linhas de código (cobertas + não cobertas)
- **Métodos**: Total de métodos na classe
- **Avg/Método**: Complexidade média por método
- **Coverage %**: Percentual de branches cobertos por testes

---

*Relatório gerado automaticamente a partir dos dados do Jacoco*
