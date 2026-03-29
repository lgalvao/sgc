# Plano de Priorizacao de Testes Unitarios

## P1: Criticos (Logica de Negocio e Seguranca)

Estas classes contem regras de negocio, validacoes, seguranca ou orquestracao complexa. A falta de testes aqui representa alto risco.

- [ ] `sgc/subprocesso/service/SubprocessoNotificacaoService.java`
- [ ] `sgc/subprocesso/service/SubprocessoService.java`
- [ ] `sgc/subprocesso/service/SubprocessoSituacaoService.java`
- [ ] `sgc/subprocesso/service/SubprocessoTransicaoService.java`

## P2: Importantes (Integracao e Contratos)

Controladores e mappers. Importantes para garantir que a API respeite os contratos e que os dados sejam transformados corretamente.

_Nenhum arquivo encontrado._

## P3: Baixa Prioridade (Dados e Infraestrutura)

DTOs, modelos, repositorios e configuracoes. Geralmente cobertos por testes de integracao ou seguros por natureza.

- [ ] `sgc/alerta/dto/AlertaDto.java`
- [ ] `sgc/alerta/model/Alerta.java`
- [ ] `sgc/alerta/model/AlertaRepo.java`
- [ ] `sgc/alerta/model/AlertaUsuario.java`
- [ ] `sgc/alerta/model/AlertaUsuarioRepo.java`
- [ ] `sgc/alerta/model/Notificacao.java`
- [ ] `sgc/alerta/model/NotificacaoRepo.java`
- [ ] `sgc/comum/ComumDtos.java`
- [ ] `sgc/comum/config/Config.java`
- [ ] `sgc/comum/config/ConfigAplicacao.java`
- [ ] `sgc/comum/config/ConfigThymeleaf.java`
- [ ] `sgc/comum/erros/ErroAcessoNegado.java`
- [ ] `sgc/comum/erros/ErroApi.java`
- [ ] `sgc/comum/erros/ErroAutenticacao.java`
- [ ] `sgc/comum/erros/ErroConfiguracao.java`
- [ ] `sgc/comum/erros/ErroEntidadeNaoEncontrada.java`
- [ ] `sgc/comum/erros/ErroInterno.java`
- [ ] `sgc/comum/erros/ErroNegocio.java`
- [ ] `sgc/comum/erros/ErroNegocioBase.java`
- [ ] `sgc/comum/erros/ErroSubApi.java`
- [ ] `sgc/comum/erros/ErroValidacao.java`
- [ ] `sgc/comum/Mensagens.java`
- [ ] `sgc/comum/model/ComumRepo.java`
- [ ] `sgc/comum/model/ComumViews.java`
- [ ] `sgc/comum/model/TituloEleitoral.java`
- [ ] `sgc/e2e/E2eSecurityConfig.java`
- [ ] `sgc/mapa/dto/AtividadeDto.java`
- [ ] `sgc/mapa/dto/AtividadeImpactadaDto.java`
- [ ] `sgc/mapa/dto/AtividadeMapaDto.java`
- [ ] `sgc/mapa/dto/AtualizarAtividadeRequest.java`
- [ ] `sgc/mapa/dto/AtualizarConhecimentoRequest.java`
- [ ] `sgc/mapa/dto/AtualizarMapaRequest.java`
- [ ] `sgc/mapa/dto/CompetenciaImpactadaDto.java`
- [ ] `sgc/mapa/dto/CompetenciaMapaDto.java`
- [ ] `sgc/mapa/dto/ConhecimentoResumoDto.java`
- [ ] `sgc/mapa/dto/CriarAtividadeRequest.java`
- [ ] `sgc/mapa/dto/CriarConhecimentoRequest.java`
- [ ] `sgc/mapa/dto/CriarMapaRequest.java`
- [ ] `sgc/mapa/dto/MapaCompletoDto.java`
- [ ] `sgc/mapa/dto/MapaResumoDto.java`
- [ ] `sgc/mapa/dto/MapaVisualizacaoResponse.java`
- [ ] `sgc/mapa/dto/ResultadoOperacaoConhecimento.java`
- [ ] `sgc/mapa/dto/SalvarMapaRequest.java`
- [ ] `sgc/mapa/model/Atividade.java`
- [ ] `sgc/mapa/model/AtividadeRepo.java`
- [ ] `sgc/mapa/model/CompetenciaRepo.java`
- [ ] `sgc/mapa/model/ConhecimentoRepo.java`
- [ ] `sgc/mapa/model/Mapa.java`
- [ ] `sgc/mapa/model/MapaRepo.java`
- [ ] `sgc/mapa/model/MapaViews.java`
- [ ] `sgc/mapa/model/TipoImpactoAtividade.java`
- [ ] `sgc/mapa/model/TipoImpactoCompetencia.java`
- [ ] `sgc/organizacao/dto/AdministradorDto.java`
- [ ] `sgc/organizacao/dto/AtribuicaoDto.java`
- [ ] `sgc/organizacao/dto/CriarAtribuicaoRequest.java`
- [ ] `sgc/organizacao/dto/ResponsavelDto.java`
- [ ] `sgc/organizacao/dto/UnidadeDto.java`
- [ ] `sgc/organizacao/dto/UnidadeResponsavelDto.java`
- [ ] `sgc/organizacao/dto/UsuarioConsultaDto.java`
- [ ] `sgc/organizacao/dto/UsuarioResumoDto.java`
- [ ] `sgc/organizacao/model/Administrador.java`
- [ ] `sgc/organizacao/model/AdministradorRepo.java`
- [ ] `sgc/organizacao/model/AtribuicaoTemporaria.java`
- [ ] `sgc/organizacao/model/AtribuicaoTemporariaRepo.java`
- [ ] `sgc/organizacao/model/OrganizacaoViews.java`
- [ ] `sgc/organizacao/model/Perfil.java`
- [ ] `sgc/organizacao/model/Responsabilidade.java`
- [ ] `sgc/organizacao/model/ResponsabilidadeRepo.java`
- [ ] `sgc/organizacao/model/SituacaoUnidade.java`
- [ ] `sgc/organizacao/model/TipoUnidade.java`
- [ ] `sgc/organizacao/model/UnidadeMapa.java`
- [ ] `sgc/organizacao/model/UnidadeMapaRepo.java`
- [ ] `sgc/organizacao/model/UnidadeRepo.java`
- [ ] `sgc/organizacao/model/Usuario.java`
- [ ] `sgc/organizacao/model/UsuarioPerfil.java`
- [ ] `sgc/organizacao/model/UsuarioPerfilId.java`
- [ ] `sgc/organizacao/model/UsuarioPerfilRepo.java`
- [ ] `sgc/organizacao/model/UsuarioRepo.java`
- [ ] `sgc/organizacao/model/VinculacaoUnidade.java`
- [ ] `sgc/parametros/model/ConfiguracaoViews.java`
- [ ] `sgc/parametros/model/Parametro.java`
- [ ] `sgc/parametros/model/ParametroRepo.java`
- [ ] `sgc/parametros/ParametroDto.java`
- [ ] `sgc/parametros/ParametroRequest.java`
- [ ] `sgc/processo/dto/AcaoEmBlocoRequest.java`
- [ ] `sgc/processo/dto/AtualizarProcessoRequest.java`
- [ ] `sgc/processo/dto/CriarProcessoRequest.java`
- [ ] `sgc/processo/dto/EnviarLembreteRequest.java`
- [ ] `sgc/processo/dto/IniciarProcessoRequest.java`
- [ ] `sgc/processo/dto/ProcessoResumoDto.java`
- [ ] `sgc/processo/dto/SubprocessoElegivelDto.java`
- [ ] `sgc/processo/model/AcaoProcesso.java`
- [ ] `sgc/processo/model/ProcessoViews.java`
- [ ] `sgc/processo/model/SituacaoProcesso.java`
- [ ] `sgc/processo/model/TipoProcesso.java`
- [ ] `sgc/processo/model/UnidadeProcesso.java`
- [ ] `sgc/processo/model/UnidadeProcessoId.java`
- [ ] `sgc/relatorio/RelatorioAndamentoDto.java`
- [ ] `sgc/seguranca/AcaoPermissao.java`
- [ ] `sgc/seguranca/config/ConfigCorsProperties.java`
- [ ] `sgc/seguranca/config/ConfigSeguranca.java`
- [ ] `sgc/seguranca/dto/AutenticarRequest.java`
- [ ] `sgc/seguranca/dto/AutorizarRequest.java`
- [ ] `sgc/seguranca/dto/EntrarRequest.java`
- [ ] `sgc/seguranca/dto/EntrarResponse.java`
- [ ] `sgc/seguranca/dto/PerfilUnidadeDto.java`
- [ ] `sgc/seguranca/login/ClienteAcessoAdE2e.java`
- [ ] `sgc/seguranca/login/ConfiguracaoAcessoAd.java`
- [ ] `sgc/seguranca/login/PropriedadesAcessoAd.java`
- [ ] `sgc/seguranca/sanitizacao/SanitizarHtml.java`
- [ ] `sgc/Sgc.java`
- [ ] `sgc/subprocesso/dto/AnaliseHistoricoDto.java`
- [ ] `sgc/subprocesso/dto/AtividadeAjusteDto.java`
- [ ] `sgc/subprocesso/dto/AtividadeOperacaoResponse.java`
- [ ] `sgc/subprocesso/dto/AtualizarSubprocessoRequest.java`
- [ ] `sgc/subprocesso/dto/CompetenciaAjusteDto.java`
- [ ] `sgc/subprocesso/dto/CompetenciaRequest.java`
- [ ] `sgc/subprocesso/dto/ConhecimentoAjusteDto.java`
- [ ] `sgc/subprocesso/dto/ContextoEdicaoResponse.java`
- [ ] `sgc/subprocesso/dto/CriarAnaliseRequest.java`
- [ ] `sgc/subprocesso/dto/CriarSubprocessoRequest.java`
- [ ] `sgc/subprocesso/dto/DisponibilizarMapaRequest.java`
- [ ] `sgc/subprocesso/dto/ImportarAtividadesRequest.java`
- [ ] `sgc/subprocesso/dto/MensagemResponse.java`
- [ ] `sgc/subprocesso/dto/MovimentacaoDto.java`
- [ ] `sgc/subprocesso/dto/NotificacaoCommand.java`
- [ ] `sgc/subprocesso/dto/PermissoesSubprocessoDto.java`
- [ ] `sgc/subprocesso/dto/ProcessarEmBlocoRequest.java`
- [ ] `sgc/subprocesso/dto/RegistrarTransicaoCommand.java`
- [ ] `sgc/subprocesso/dto/RegistrarWorkflowCommand.java`
- [ ] `sgc/subprocesso/dto/SalvarAjustesRequest.java`
- [ ] `sgc/subprocesso/dto/SubmeterMapaAjustadoRequest.java`
- [ ] `sgc/subprocesso/dto/SubprocessoCadastroDto.java`
- [ ] `sgc/subprocesso/dto/SubprocessoCodigoDto.java`
- [ ] `sgc/subprocesso/dto/SubprocessoDetalheResponse.java`
- [ ] `sgc/subprocesso/dto/SubprocessoListagemDto.java`
- [ ] `sgc/subprocesso/dto/SubprocessoResumoDto.java`
- [ ] `sgc/subprocesso/dto/SubprocessoSituacaoDto.java`
- [ ] `sgc/subprocesso/dto/ValidacaoCadastroDto.java`
- [ ] `sgc/subprocesso/model/Analise.java`
- [ ] `sgc/subprocesso/model/AnaliseRepo.java`
- [ ] `sgc/subprocesso/model/MovimentacaoRepo.java`
- [ ] `sgc/subprocesso/model/SubprocessoRepo.java`
- [ ] `sgc/subprocesso/model/SubprocessoViews.java`
- [ ] `sgc/subprocesso/model/TipoAcaoAnalise.java`
- [ ] `sgc/subprocesso/model/TipoAnalise.java`
