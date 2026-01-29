# Relatório de Cobertura de Testes Unitários (Backend)

**Data:** 29/01/2026
**Total de Classes:** 282
**Com Testes Unitários:** 87
**Sem Testes Unitários:** 195
**Cobertura (Arquivos):** 30.85%

## Detalhamento por Categoria

### Controllers (13/16 testados)
**Faltando Testes (3):**
- `sgc/configuracao/ConfiguracaoController.java`
- `sgc/painel/PainelController.java`
- `sgc/relatorio/controller/RelatorioController.java`

### Facades (11/13 testados)
**Faltando Testes (2):**
- `sgc/mapa/service/AtividadeFacade.java`
- `sgc/subprocesso/service/workflow/SubprocessoWorkflowFacade.java`

### Services (25/34 testados)
**Faltando Testes (9):**
- `sgc/alerta/AlertaService.java`
- `sgc/analise/AnaliseService.java`
- `sgc/configuracao/ConfiguracaoService.java`
- `sgc/e2e/mock/NotificacaoEmailServiceMock.java`
- `sgc/mapa/service/ImpactoMapaService.java`
- `sgc/organizacao/service/UnidadeMapaService.java`
- `sgc/processo/service/ProcessoRepositoryService.java`
- `sgc/seguranca/acesso/AbstractAccessPolicy.java`
- `sgc/seguranca/acesso/AccessPolicy.java`

### Mappers (11/13 testados)
**Faltando Testes (2):**
- `sgc/processo/mapper/ProcessoDetalheMapper.java`
- `sgc/subprocesso/mapper/SubprocessoMapper.java`

### Models (7/34 testados)
**Faltando Testes (27):**
- `sgc/alerta/model/Alerta.java`
- `sgc/alerta/model/AlertaUsuario.java`
- `sgc/analise/model/Analise.java`
- `sgc/analise/model/TipoAcaoAnalise.java`
- `sgc/analise/model/TipoAnalise.java`
- `sgc/configuracao/model/Parametro.java`
- `sgc/mapa/model/Atividade.java`
- `sgc/mapa/model/Competencia.java`
- `sgc/mapa/model/Conhecimento.java`
- `sgc/mapa/model/Mapa.java`
- `sgc/mapa/model/TipoImpactoAtividade.java`
- `sgc/mapa/model/TipoImpactoCompetencia.java`
- `sgc/notificacao/model/Notificacao.java`
- `sgc/organizacao/model/Administrador.java`
- `sgc/organizacao/model/AtribuicaoTemporaria.java`
- `sgc/organizacao/model/Perfil.java`
- `sgc/organizacao/model/SituacaoUnidade.java`
- `sgc/organizacao/model/TipoUnidade.java`
- `sgc/organizacao/model/UnidadeMapa.java`
- `sgc/organizacao/model/UsuarioPerfil.java`
- `sgc/organizacao/model/UsuarioPerfilId.java`
- `sgc/organizacao/model/VinculacaoUnidade.java`
- `sgc/organizacao/model/VinculacaoUnidadeId.java`
- `sgc/processo/model/AcaoProcesso.java`
- `sgc/processo/model/SituacaoProcesso.java`
- `sgc/processo/model/TipoProcesso.java`
- `sgc/subprocesso/model/SituacaoSubprocesso.java`

### Repositories (0/20 testados)
**Faltando Testes (20):**
- `sgc/alerta/model/AlertaRepo.java`
- `sgc/alerta/model/AlertaUsuarioRepo.java`
- `sgc/analise/model/AnaliseRepo.java`
- `sgc/comum/repo/RepositorioComum.java`
- `sgc/configuracao/model/ParametroRepo.java`
- `sgc/mapa/model/AtividadeRepo.java`
- `sgc/mapa/model/CompetenciaRepo.java`
- `sgc/mapa/model/ConhecimentoRepo.java`
- `sgc/mapa/model/MapaRepo.java`
- `sgc/notificacao/model/NotificacaoRepo.java`
- `sgc/organizacao/model/AdministradorRepo.java`
- `sgc/organizacao/model/AtribuicaoTemporariaRepo.java`
- `sgc/organizacao/model/UnidadeMapaRepo.java`
- `sgc/organizacao/model/UnidadeRepo.java`
- `sgc/organizacao/model/UsuarioPerfilRepo.java`
- `sgc/organizacao/model/UsuarioRepo.java`
- `sgc/organizacao/model/VinculacaoUnidadeRepo.java`
- `sgc/processo/model/ProcessoRepo.java`
- `sgc/subprocesso/model/MovimentacaoRepo.java`
- `sgc/subprocesso/model/SubprocessoRepo.java`

### DTOs (1/80 testados)
**Faltando Testes (79):**
- `sgc/alerta/dto/AlertaDto.java`
- `sgc/analise/dto/AnaliseHistoricoDto.java`
- `sgc/analise/dto/AnaliseValidacaoHistoricoDto.java`
- `sgc/analise/dto/CriarAnaliseRequest.java`
- `sgc/mapa/dto/AtividadeImpactadaDto.java`
- `sgc/mapa/dto/AtividadeResponse.java`
- `sgc/mapa/dto/AtualizarAtividadeRequest.java`
- `sgc/mapa/dto/AtualizarConhecimentoRequest.java`
- `sgc/mapa/dto/CompetenciaImpactadaDto.java`
- `sgc/mapa/dto/CompetenciaMapaDto.java`
- `sgc/mapa/dto/ConhecimentoResponse.java`
- `sgc/mapa/dto/CriarAtividadeRequest.java`
- `sgc/mapa/dto/CriarConhecimentoRequest.java`
- `sgc/mapa/dto/ImpactoMapaDto.java`
- `sgc/mapa/dto/MapaCompletoDto.java`
- `sgc/mapa/dto/MapaDto.java`
- `sgc/mapa/dto/SalvarMapaRequest.java`
- `sgc/mapa/dto/visualizacao/AtividadeDto.java`
- `sgc/mapa/dto/visualizacao/CompetenciaDto.java`
- `sgc/mapa/dto/visualizacao/ConhecimentoDto.java`
- `sgc/mapa/dto/visualizacao/MapaVisualizacaoDto.java`
- `sgc/notificacao/dto/EmailDto.java`
- `sgc/organizacao/dto/AdministradorDto.java`
- `sgc/organizacao/dto/AtribuicaoTemporariaDto.java`
- `sgc/organizacao/dto/CriarAtribuicaoTemporariaRequest.java`
- `sgc/organizacao/dto/PerfilDto.java`
- `sgc/organizacao/dto/PerfilUnidadeDto.java`
- `sgc/organizacao/dto/ResponsavelDto.java`
- `sgc/organizacao/dto/UnidadeDto.java`
- `sgc/organizacao/dto/UsuarioDto.java`
- `sgc/processo/dto/AcaoEmBlocoRequest.java`
- `sgc/processo/dto/AtualizarProcessoRequest.java`
- `sgc/processo/dto/CriarProcessoRequest.java`
- `sgc/processo/dto/EnviarLembreteRequest.java`
- `sgc/processo/dto/IniciarProcessoRequest.java`
- `sgc/processo/dto/ProcessoContextoDto.java`
- `sgc/processo/dto/ProcessoDetalheDto.java`
- `sgc/processo/dto/ProcessoDto.java`
- `sgc/processo/dto/ProcessoResumoDto.java`
- `sgc/processo/dto/SubprocessoElegivelDto.java`
- `sgc/seguranca/login/dto/AutenticarRequest.java`
- `sgc/seguranca/login/dto/AutorizarRequest.java`
- `sgc/seguranca/login/dto/EntrarRequest.java`
- `sgc/seguranca/login/dto/EntrarResponse.java`
- `sgc/seguranca/login/dto/PerfilUnidadeDto.java`
- `sgc/subprocesso/dto/AceitarCadastroRequest.java`
- `sgc/subprocesso/dto/AlterarDataLimiteRequest.java`
- `sgc/subprocesso/dto/AnaliseValidacaoDto.java`
- `sgc/subprocesso/dto/ApresentarSugestoesRequest.java`
- `sgc/subprocesso/dto/AtividadeAjusteDto.java`
- `sgc/subprocesso/dto/AtividadeOperacaoResponse.java`
- `sgc/subprocesso/dto/AtividadeVisualizacaoDto.java`
- `sgc/subprocesso/dto/AtualizarSubprocessoRequest.java`
- `sgc/subprocesso/dto/CompetenciaAjusteDto.java`
- `sgc/subprocesso/dto/CompetenciaRequest.java`
- `sgc/subprocesso/dto/ConhecimentoAjusteDto.java`
- `sgc/subprocesso/dto/ConhecimentoVisualizacaoDto.java`
- `sgc/subprocesso/dto/ContextoEdicaoDto.java`
- `sgc/subprocesso/dto/CriarSubprocessoRequest.java`
- `sgc/subprocesso/dto/DevolverCadastroRequest.java`
- `sgc/subprocesso/dto/DevolverValidacaoRequest.java`
- `sgc/subprocesso/dto/DisponibilizarMapaRequest.java`
- `sgc/subprocesso/dto/ErroValidacaoDto.java`
- `sgc/subprocesso/dto/HomologarCadastroRequest.java`
- `sgc/subprocesso/dto/ImportarAtividadesRequest.java`
- `sgc/subprocesso/dto/MensagemResponse.java`
- `sgc/subprocesso/dto/MovimentacaoDto.java`
- `sgc/subprocesso/dto/ProcessarEmBlocoRequest.java`
- `sgc/subprocesso/dto/ReabrirProcessoRequest.java`
- `sgc/subprocesso/dto/ResponsavelDto.java`
- `sgc/subprocesso/dto/SalvarAjustesRequest.java`
- `sgc/subprocesso/dto/SubmeterMapaAjustadoRequest.java`
- `sgc/subprocesso/dto/SubprocessoCadastroDto.java`
- `sgc/subprocesso/dto/SubprocessoDetalheDto.java`
- `sgc/subprocesso/dto/SubprocessoDto.java`
- `sgc/subprocesso/dto/SubprocessoPermissoesDto.java`
- `sgc/subprocesso/dto/SubprocessoSituacaoDto.java`
- `sgc/subprocesso/dto/SugestoesDto.java`
- `sgc/subprocesso/dto/ValidacaoCadastroDto.java`

### Others (19/72 testados)
**Faltando Testes (53):**
- `sgc/Sgc.java`
- `sgc/analise/dto/CriarAnaliseCommand.java`
- `sgc/comum/config/Config.java`
- `sgc/comum/config/ConfigAplicacao.java`
- `sgc/comum/config/ConfigOpenApi.java`
- `sgc/comum/config/ConfigThymeleaf.java`
- `sgc/comum/erros/ErroAccessoNegado.java`
- `sgc/comum/erros/ErroApi.java`
- `sgc/comum/erros/ErroAutenticacao.java`
- `sgc/comum/erros/ErroConfiguracao.java`
- `sgc/comum/erros/ErroEntidadeNaoEncontrada.java`
- `sgc/comum/erros/ErroEstadoImpossivel.java`
- `sgc/comum/erros/ErroInterno.java`
- `sgc/comum/erros/ErroInvarianteViolada.java`
- `sgc/comum/erros/ErroNegocio.java`
- `sgc/comum/erros/ErroNegocioBase.java`
- `sgc/comum/erros/ErroSituacaoInvalida.java`
- `sgc/comum/erros/ErroSubApi.java`
- `sgc/comum/erros/ErroValidacao.java`
- `sgc/e2e/E2eSecurityConfig.java`
- `sgc/mapa/dto/ResultadoOperacaoConhecimento.java`
- `sgc/mapa/evento/EventoAtividadeAtualizada.java`
- `sgc/mapa/evento/EventoAtividadeCriada.java`
- `sgc/mapa/evento/EventoAtividadeExcluida.java`
- `sgc/mapa/evento/EventoMapaAlterado.java`
- `sgc/painel/erros/ErroParametroPainelInvalido.java`
- `sgc/processo/erros/ErroProcesso.java`
- `sgc/processo/erros/ErroProcessoEmSituacaoInvalida.java`
- `sgc/processo/erros/ErroUnidadesNaoDefinidas.java`
- `sgc/processo/eventos/EventoProcessoAtualizado.java`
- `sgc/processo/eventos/EventoProcessoCriado.java`
- `sgc/processo/eventos/EventoProcessoExcluido.java`
- `sgc/processo/eventos/EventoProcessoFinalizado.java`
- `sgc/processo/eventos/EventoProcessoIniciado.java`
- `sgc/relatorio/service/ErroRelatorio.java`
- `sgc/relatorio/service/PdfFactory.java`
- `sgc/seguranca/acesso/Acao.java`
- `sgc/seguranca/config/ConfigCorsProperties.java`
- `sgc/seguranca/config/ConfigSeguranca.java`
- `sgc/seguranca/login/ConfiguracaoAcessoAd.java`
- `sgc/seguranca/login/FiltroJwt.java`
- `sgc/seguranca/login/PropriedadesAcessoAd.java`
- `sgc/seguranca/sanitizacao/SanitizarHtml.java`
- `sgc/seguranca/sanitizacao/UtilSanitizacao.java`
- `sgc/subprocesso/erros/ErroAtividadesEmSituacaoInvalida.java`
- `sgc/subprocesso/erros/ErroMapaEmSituacaoInvalida.java`
- `sgc/subprocesso/erros/ErroMapaNaoAssociado.java`
- `sgc/subprocesso/eventos/EventoSubprocessoAtualizado.java`
- `sgc/subprocesso/eventos/EventoSubprocessoCriado.java`
- `sgc/subprocesso/eventos/EventoSubprocessoExcluido.java`
- `sgc/subprocesso/eventos/EventoTransicaoSubprocesso.java`
- `sgc/subprocesso/listener/SubprocessoMapaListener.java`
- `sgc/subprocesso/service/notificacao/SubprocessoComunicacaoListener.java`

