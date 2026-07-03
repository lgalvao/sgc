package sgc.processo.service;

import org.junit.jupiter.api.*;
import sgc.alerta.model.*;
import sgc.comum.erros.*;
import sgc.mapa.model.*;
import sgc.organizacao.model.*;
import sgc.processo.model.*;
import sgc.subprocesso.model.*;
import sgc.subprocesso.service.SubprocessoValidacaoService.*;

import java.time.*;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static sgc.processo.model.SituacaoProcesso.*;
import static sgc.processo.model.TipoProcesso.*;

@DisplayName("ProcessoService Notificação Test suite")
class ProcessoServiceNotificacaoTest extends ProcessoServiceTestBase {

    @Test
    @DisplayName("Deve vincular subprocesso nas notificacoes de inicio para exibir no painel de notificacoes")
    void deveVincularSubprocessoNasNotificacoesDeInicio() {
        Long id = 103L;
        Processo processo = new Processo();
        processo.setCodigo(id);
        processo.setSituacao(SituacaoProcesso.CRIADO);
        processo.setTipo(TipoProcesso.REVISAO);
        processo.setDescricao("Processo notificacoes");
        processo.setDataLimite(LocalDateTime.now().plusDays(30));

        Unidade unidade = criarUnidadeValida(10L);
        processo.adicionarParticipantes(Set.of(unidade));

        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(501L);
        subprocesso.setProcesso(processo);
        subprocesso.setUnidade(unidade);

        when(repo.buscar(Processo.class, id)).thenReturn(processo);
        when(unidadeService.buscarAdmin()).thenReturn(criarUnidadeValida(999L));
        when(unidadeService.buscarPorCodigos(List.of(10L))).thenReturn(List.of(unidade));
        when(unidadeService.buscarTodosCodigosUnidadesComMapa()).thenReturn(List.of(10L));
        when(unidadeService.buscarMapasPorUnidades(List.of(10L))).thenReturn(List.of(
                sgc.organizacao.model.UnidadeMapa.builder().unidadeCodigo(10L).build()
        ));
        when(consultaService.listarEntidadesPorProcesso(id)).thenReturn(List.of(subprocesso));
        when(emailModelosService.criarEmailInicioProcessoConsolidado(anyString(), anyString(), any(), anyString(), anyBoolean(), anyList()))
                .thenReturn("<html>inicio</html>");
        mockarResponsaveisEfetivos();

        processoService.iniciar(id, List.of(10L));

        verify(notificacaoService).enfileirar(argThat(cmd ->
                cmd.tipoNotificacao() == TipoNotificacao.PROCESSO_INICIADO
                        && cmd.subprocesso() != null
                        && Objects.equals(cmd.subprocesso().getCodigo(), 501L)
        ));
    }

    @Test
    @DisplayName("Deve disparar notificações de início cobrindo todos os tipos de unidade organizacional")
    void deveDispararNotificacoesInicioParaTodosTiposDeUnidade() {
        Long id = 100L;
        Processo processo = new Processo();
        processo.setCodigo(id);
        processo.setSituacao(SituacaoProcesso.CRIADO);
        processo.setTipo(TipoProcesso.MAPEAMENTO);
        processo.setDescricao("Teste Cobertura Tipos Unidade");
        processo.setDataLimite(LocalDateTime.now().plusDays(30));

        // Lacuna E: RAIZ, INTEROPERACIONAL, OPERACIONAL, INTERMEDIARIA
        Unidade uRaiz = criarUnidadeValida(1L);
        uRaiz.setTipo(TipoUnidade.RAIZ);
        Unidade uInterop = criarUnidadeValida(2L);
        uInterop.setTipo(TipoUnidade.INTEROPERACIONAL);
        Unidade uOper = criarUnidadeValida(3L);
        Unidade uIntermed = criarUnidadeValida(4L);
        uIntermed.setTipo(TipoUnidade.INTERMEDIARIA);

        when(repo.buscar(Processo.class, id)).thenReturn(processo);
        when(unidadeService.buscarPorCodigos(anyList())).thenReturn(List.of(uRaiz, uInterop, uOper, uIntermed));
        when(emailModelosService.criarEmailInicioProcessoConsolidado(anyString(), anyString(), any(), anyString(), anyBoolean(), anyList()))
                .thenReturn("<html>inicio</html>");
        mockarResponsaveisEfetivos();

        processoService.iniciar(id, List.of(1L, 2L, 3L, 4L));

        // Verifica se notificou as unidades (operacionais/raiz e intermediárias/interoperacionais)
        verify(notificacaoService, atLeast(4)).enfileirar(any());
    }

    @Test
    @DisplayName("Deve finalizar processo criando notificacao por email")
    void deveFinalizarProcessoQuandoTudoHomologado() {
        Long id = 100L;
        Processo p = new Processo();
        p.setCodigo(id);
        p.setDescricao("Mapeamento 2026");
        p.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        p.setTipo(TipoProcesso.DIAGNOSTICO);
        Unidade unidade = criarUnidadeValida(10L);
        unidade.setSigla("SECAO");
        p.adicionarParticipantes(Set.of(unidade));

        when(repo.buscar(Processo.class, id)).thenReturn(p);
        when(unidadeService.buscarPorCodigos(anyList())).thenReturn(List.of(unidade));
        when(unidadeHierarquiaService.buscarCodigosSuperiores(10L)).thenReturn(List.of());
        when(emailModelosService.criarEmailProcessoFinalizadoPorUnidade("SECAO", "Mapeamento 2026", TipoProcesso.DIAGNOSTICO))
                .thenReturn("<html>finalizado</html>");
        when(emailModelosService.criarAssuntoProcessoFinalizado(TipoProcesso.DIAGNOSTICO))
                .thenReturn("SGC: Finalização de processo de diagnóstico");
        when(validacaoService.validarSubprocessosParaFinalizacao(id, TipoProcesso.DIAGNOSTICO))
                .thenReturn(ResultadoValidacao.ofValido());

        processoService.finalizar(id);

        verify(processoRepo).save(p);
        verify(notificacaoService).enfileirar(argThat(command ->
                command.tipoNotificacao() == TipoNotificacao.PROCESSO_FINALIZADO
                        && command.destinatario().equals("secao@tre-pe.jus.br")
                        && command.assunto().equals("SGC: Finalização de processo de diagnóstico")
                        && command.corpoHtml().equals("<html>finalizado</html>")
                        && command.chaveIdempotencia().equals("processo:100:finalizacao:unidade:10:direto")
        ));
        assertThat(p.getSituacao()).isEqualTo(SituacaoProcesso.FINALIZADO);
    }


    @Test
    @DisplayName("Deve enfileirar email e criar alerta ao enviar lembrete com sucesso")
    void deveEnfileirarEmailECriarAlertaAoEnviarLembrete() {
        Long codProcesso = 1L;
        Long codUnidade = 10L;

        Processo p = new Processo();
        p.setCodigo(codProcesso);
        p.setDescricao("Processo teste");
        Unidade u = criarUnidadeValida(codUnidade);
        u.setTituloTitular("TITULAR");
        p.adicionarParticipantes(Set.of(u));
        p.setDataLimite(LocalDateTime.of(2026, 6, 30, 0, 0));

        sgc.organizacao.model.Usuario titular = new sgc.organizacao.model.Usuario();
        titular.setTituloEleitoral("TITULAR");
        titular.setEmail("titular@tre-pe.jus.br");

        sgc.alerta.model.Alerta alerta = new sgc.alerta.model.Alerta();
        alerta.setCodigo(55L);

        when(processoRepo.buscarPorCodigoComParticipantes(codProcesso)).thenReturn(Optional.of(p));
        when(unidadeService.buscarPorCodigo(codUnidade)).thenReturn(u);
        when(emailModelosService.criarEmailLembretePrazo(anyString(), anyString(), any())).thenReturn("<html>lembrete</html>");
        when(emailModelosService.criarAssuntoLembretePrazo("Processo teste")).thenReturn("SGC: Lembrete de prazo - Processo teste");
        when(servicoAlertas.criarAlertaAdmin(eq(p), eq(u), anyString())).thenReturn(alerta);

        processoService.enviarLembrete(codProcesso, codUnidade);

        verify(servicoAlertas).criarAlertaAdmin(eq(p), eq(u), contains("Lembrete"));
        verify(notificacaoService).enfileirar(argThat(cmd ->
                "u10@tre-pe.jus.br".equals(cmd.destinatario())
                        && "U10".equals(cmd.unidadeDestinoSigla())
                        && cmd.usuarioDestinoTitulo() == null
                        && cmd.assunto().contains("Processo teste")
                        && cmd.tipoNotificacao() == TipoNotificacao.LEMBRETE_PRAZO
                        && cmd.subprocesso() == null
                        && cmd.chaveIdempotencia().contains("processo:1:lembrete:unidade:10")
        ));
    }


    @Test
    @DisplayName("enviarLembrete - sucesso")
    void enviarLembreteSucesso() {
        Processo p = new Processo();
        p.setCodigo(1L);
        p.setDescricao("Processo Teste");
        p.setDataLimite(java.time.LocalDateTime.now().plusDays(1));

        Unidade u = new Unidade();
        u.setCodigo(10L);
        u.setSigla("U1");
        u.setSituacao(sgc.organizacao.model.SituacaoUnidade.ATIVA);
        u.setTipo(sgc.organizacao.model.TipoUnidade.OPERACIONAL);

        p.adicionarParticipantes(Set.of(u));

        when(processoRepo.buscarPorCodigoComParticipantes(1L)).thenReturn(Optional.of(p));
        when(unidadeService.buscarPorCodigo(10L)).thenReturn(u);
        when(emailModelosService.criarEmailLembretePrazo(any(), any(), any())).thenReturn("<html></html>");

        processoService.enviarLembrete(1L, 10L);

        verify(servicoAlertas).criarAlertaAdmin(eq(p), eq(u), anyString());
        verify(notificacaoService).enfileirar(argThat(cmd ->
                "u1@tre-pe.jus.br".equals(cmd.destinatario())
                        && cmd.tipoNotificacao() == TipoNotificacao.LEMBRETE_PRAZO
                        && "U1".equals(cmd.unidadeDestinoSigla())
                        && cmd.chaveIdempotencia().startsWith("processo:1:lembrete:unidade:10:dia:")
        ));
    }

    @Test
    @DisplayName("enviarLembrete para unidade ADMIN deve enviar para SEDOC quando ADMIN estiver lotado na SEDOC")
    void enviarLembreteAdminLotadoNaSedoc() {
        Processo processo = new Processo();
        processo.setCodigo(1L);
        processo.setDescricao("Processo Teste");
        processo.setDataLimite(LocalDateTime.of(2026, 6, 30, 0, 0));

        Unidade admin = new Unidade();
        admin.setCodigo(1L);
        admin.setSigla("ADMIN");
        admin.setNome("Administrador");
        sgc.processo.model.UnidadeProcesso participanteAdmin = new sgc.processo.model.UnidadeProcesso();
        participanteAdmin.setUnidadeCodigo(1L);
        participanteAdmin.setSigla("ADMIN");
        processo.setParticipantes(new ArrayList<>(List.of(participanteAdmin)));

        Unidade sedoc = new Unidade();
        sedoc.setCodigo(2L);
        sedoc.setSigla("SEDOC");

        sgc.organizacao.model.Usuario usuario = new sgc.organizacao.model.Usuario();
        usuario.setTituloEleitoral("TITULO_SEDOC");
        usuario.setPerfilAtivo(Perfil.ADMIN);
        usuario.setEmail("admin.sedoc@tre-pe.jus.br");
        usuario.setUnidadeLotacao(sedoc);

        Alerta alerta = new Alerta();
        alerta.setCodigo(10L);

        when(processoRepo.buscarPorCodigoComParticipantes(1L)).thenReturn(Optional.of(processo));
        when(unidadeService.buscarPorCodigo(1L)).thenReturn(admin);
        when(emailModelosService.criarEmailLembretePrazo(anyString(), anyString(), any())).thenReturn("<html>lembrete</html>");
        when(servicoAlertas.criarAlertaAdmin(eq(processo), eq(admin), anyString())).thenReturn(alerta);
        when(usuarioService.usuarioAutenticado()).thenReturn(usuario);
        when(usuarioService.buscarUsuarioComUnidadeLotacao("TITULO_SEDOC")).thenReturn(usuario);

        processoService.enviarLembrete(1L, 1L);

        verify(notificacaoService, times(1)).enfileirar(argThat(cmd ->
                "sedoc@tre-pe.jus.br".equals(cmd.destinatario())
                        && "ADMIN".equals(cmd.unidadeDestinoSigla())
                        && cmd.chaveIdempotencia().equals("processo:1:lembrete:unidade:1:dia:" + LocalDate.now())
        ));
    }

    @Test
    @DisplayName("enviarLembrete para unidade ADMIN deve enviar cópia ao e-mail pessoal quando ADMIN estiver lotado em outra unidade")
    void enviarLembreteAdminLotadoForaDaSedoc() {
        Processo processo = new Processo();
        processo.setCodigo(1L);
        processo.setDescricao("Processo Teste");
        processo.setDataLimite(LocalDateTime.of(2026, 6, 30, 0, 0));

        Unidade admin = new Unidade();
        admin.setCodigo(1L);
        admin.setSigla("ADMIN");
        admin.setNome("Administrador");
        sgc.processo.model.UnidadeProcesso participanteAdmin = new sgc.processo.model.UnidadeProcesso();
        participanteAdmin.setUnidadeCodigo(1L);
        participanteAdmin.setSigla("ADMIN");
        processo.setParticipantes(new ArrayList<>(List.of(participanteAdmin)));

        Unidade outra = new Unidade();
        outra.setCodigo(3L);
        outra.setSigla("COAUD");

        sgc.organizacao.model.Usuario usuario = new sgc.organizacao.model.Usuario();
        usuario.setTituloEleitoral("TITULO_COAUD");
        usuario.setPerfilAtivo(Perfil.ADMIN);
        usuario.setEmail("admin.teste@tre-pe.jus.br");
        usuario.setUnidadeLotacao(outra);

        Alerta alerta = new Alerta();
        alerta.setCodigo(10L);

        when(processoRepo.buscarPorCodigoComParticipantes(1L)).thenReturn(Optional.of(processo));
        when(unidadeService.buscarPorCodigo(1L)).thenReturn(admin);
        when(emailModelosService.criarEmailLembretePrazo(anyString(), anyString(), any())).thenReturn("<html>lembrete</html>");
        when(servicoAlertas.criarAlertaAdmin(eq(processo), eq(admin), anyString())).thenReturn(alerta);
        when(usuarioService.usuarioAutenticado()).thenReturn(usuario);
        when(usuarioService.buscarUsuarioComUnidadeLotacao("TITULO_COAUD")).thenReturn(usuario);

        processoService.enviarLembrete(1L, 1L);

        verify(notificacaoService, times(2)).enfileirar(any());
        verify(notificacaoService).enfileirar(argThat(cmd ->
                "sedoc@tre-pe.jus.br".equals(cmd.destinatario())
                        && cmd.chaveIdempotencia().equals("processo:1:lembrete:unidade:1:dia:" + LocalDate.now())
        ));
        verify(notificacaoService).enfileirar(argThat(cmd ->
                "admin.teste@tre-pe.jus.br".equals(cmd.destinatario())
                        && cmd.chaveIdempotencia().equals("processo:1:lembrete:unidade:1:dia:" + LocalDate.now() + ":copia-admin")
        ));
    }

    @Test
    @DisplayName("enviarLembrete deve lançar ErroValidacao quando unidade nao participa do processo")
    void enviarLembreteDeveLancarErroQuandoUnidadeNaoParticipaDoProcesso() {
        Long codProcesso = 1L;
        Long codUnidade = 99L;

        Processo p = new Processo();
        p.setCodigo(codProcesso);
        Unidade outraUnidade = criarUnidadeValida(10L);
        p.adicionarParticipantes(Set.of(outraUnidade));

        when(processoRepo.buscarPorCodigoComParticipantes(codProcesso)).thenReturn(Optional.of(p));
        when(unidadeService.buscarPorCodigo(codUnidade)).thenReturn(criarUnidadeValida(codUnidade));

        assertThatThrownBy(() -> processoService.enviarLembrete(codProcesso, codUnidade))
                .isInstanceOf(ErroValidacao.class)
                .hasMessageContaining("Unidade não participa");
    }

    @Test
    @DisplayName("finalizar deve criar notificacao consolidada para unidade INTEROPERACIONAL")
    void finalizarDeveCriarNotificacaoConsolidadaParaUnidadeInteroperacional() {
        Long codProcesso = 200L;
        Processo p = new Processo();
        p.setCodigo(codProcesso);
        p.setDescricao("Processo INTEROPERACIONAL");
        p.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        p.setTipo(TipoProcesso.MAPEAMENTO);

        Unidade unidadeInterop = criarUnidadeValida(20L);
        unidadeInterop.setTipo(TipoUnidade.INTEROPERACIONAL);
        unidadeInterop.setSigla("INTEROP");
        p.adicionarParticipantes(Set.of(unidadeInterop));

        when(repo.buscar(Processo.class, codProcesso)).thenReturn(p);
        when(unidadeService.buscarPorCodigos(anyList())).thenReturn(List.of(unidadeInterop));
        when(unidadeHierarquiaService.buscarCodigosSuperiores(20L)).thenReturn(List.of());
        when(emailModelosService.criarEmailProcessoFinalizadoPorUnidade(anyString(), anyString(), any()))
                .thenReturn("<html>finalizado interop</html>");
        when(validacaoService.validarSubprocessosParaFinalizacao(codProcesso, TipoProcesso.MAPEAMENTO))
                .thenReturn(ResultadoValidacao.ofValido());
        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(2000L);
        subprocesso.setUnidade(unidadeInterop);
        Mapa mapa = new Mapa();
        mapa.setSubprocesso(subprocesso);
        when(consultaService.listarEntidadesPorProcesso(codProcesso)).thenReturn(List.of(subprocesso));
        when(mapaManutencaoService.buscarMapasPorSubprocessos(List.of(2000L))).thenReturn(List.of(mapa));

        processoService.finalizar(codProcesso);

        verify(notificacaoService, atLeastOnce()).enfileirar(any());
        verify(unidadeService).definirMapasVigentesEmBloco(Map.of(20L, mapa));
    }

    @Test
    @DisplayName("finalizar deve criar notificacao para unidade INTERMEDIARIA com subordinadas")
    void finalizarDeveCriarNotificacaoParaUnidadeIntermediariaComSubordinadas() {
        Long codProcesso = 201L;
        Processo p = new Processo();
        p.setCodigo(codProcesso);
        p.setDescricao("Processo INTERMEDIARIA");
        p.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        p.setTipo(TipoProcesso.MAPEAMENTO);

        Unidade unidadeInter = criarUnidadeValida(30L);
        unidadeInter.setTipo(TipoUnidade.INTERMEDIARIA);
        unidadeInter.setSigla("INTER");
        Unidade unidadeOper = criarUnidadeValida(31L);
        unidadeOper.setTipo(TipoUnidade.OPERACIONAL);
        unidadeOper.setSigla("OPER");
        p.adicionarParticipantes(Set.of(unidadeInter, unidadeOper));

        when(repo.buscar(Processo.class, codProcesso)).thenReturn(p);
        when(unidadeService.buscarPorCodigos(anyList())).thenReturn(List.of(unidadeInter, unidadeOper));
        when(unidadeHierarquiaService.buscarCodigosSuperiores(30L)).thenReturn(List.of());
        when(unidadeHierarquiaService.buscarCodigosSuperiores(31L)).thenReturn(List.of(30L));
        when(emailModelosService.criarEmailProcessoFinalizadoPorUnidade(anyString(), anyString(), any()))
                .thenReturn("<html>finalizado inter</html>");
        when(emailModelosService.criarEmailProcessoFinalizadoUnidadesSubordinadas(anyString(), anyString(), anyList(), any()))
                .thenReturn("<html>consolidado</html>");
        when(validacaoService.validarSubprocessosParaFinalizacao(codProcesso, TipoProcesso.MAPEAMENTO))
                .thenReturn(ResultadoValidacao.ofValido());
        Subprocesso subprocessoInter = new Subprocesso();
        subprocessoInter.setCodigo(3000L);
        subprocessoInter.setUnidade(unidadeInter);
        Subprocesso subprocessoOper = new Subprocesso();
        subprocessoOper.setCodigo(3001L);
        subprocessoOper.setUnidade(unidadeOper);
        Mapa mapaInter = new Mapa();
        mapaInter.setSubprocesso(subprocessoInter);
        Mapa mapaOper = new Mapa();
        mapaOper.setSubprocesso(subprocessoOper);
        when(consultaService.listarEntidadesPorProcesso(codProcesso)).thenReturn(List.of(subprocessoInter, subprocessoOper));
        when(mapaManutencaoService.buscarMapasPorSubprocessos(List.of(3000L, 3001L))).thenReturn(List.of(mapaInter, mapaOper));

        processoService.finalizar(codProcesso);

        verify(notificacaoService, atLeastOnce()).enfileirar(any());
        verify(unidadeService).definirMapasVigentesEmBloco(Map.of(30L, mapaInter, 31L, mapaOper));
    }

    @Test
    @DisplayName("finalizar deve notificar participantes")
    void deveNotificarParticipantesAoFinalizar() {
        Long codigo = 1L;
        Processo p = mock(Processo.class);
        when(p.getCodigo()).thenReturn(codigo);
        when(p.getDescricao()).thenReturn("Desc");
        when(p.getTipo()).thenReturn(DIAGNOSTICO);
        when(p.getSituacao()).thenReturn(EM_ANDAMENTO);

        sgc.processo.model.UnidadeProcesso up = new sgc.processo.model.UnidadeProcesso();
        up.setUnidadeCodigo(10L);
        when(p.getParticipantes()).thenReturn(List.of(up));

        Unidade uni = new Unidade();
        uni.setCodigo(10L);
        uni.setSigla("SECAO");
        uni.setTipo(TipoUnidade.OPERACIONAL);
        when(unidadeService.buscarPorCodigos(anyList())).thenReturn(List.of(uni));

        when(repo.buscar(Processo.class, codigo)).thenReturn(p);

        ResultadoValidacao v = ResultadoValidacao.ofValido();
        when(validacaoService.validarSubprocessosParaFinalizacao(codigo, TipoProcesso.DIAGNOSTICO)).thenReturn(v);

        processoService.finalizar(codigo);

        verify(p).setSituacao(FINALIZADO);
        verify(servicoAlertas).criarAlertaAdmin(p, uni, "Processo finalizado");
        verify(processoRepo).save(p);
    }

    @Test
    @DisplayName("finalizar não deve criar alerta nem notificação para a unidade virtual ADMIN")
    void finalizarNaoDeveCriarAlertaNemNotificacaoParaUnidadeAdmin() {
        Long codigo = 206L;
        Processo processo = new Processo();
        processo.setCodigo(codigo);
        processo.setDescricao("Processo admin");
        processo.setSituacao(EM_ANDAMENTO);
        processo.setTipo(DIAGNOSTICO);

        Unidade admin = criarUnidadeValida(1L);
        admin.setSigla("ADMIN");
        admin.setTipo(TipoUnidade.RAIZ);
        processo.adicionarParticipantes(Set.of(admin));

        when(repo.buscar(Processo.class, codigo)).thenReturn(processo);
        when(unidadeService.buscarPorCodigos(List.of(1L))).thenReturn(List.of(admin));
        when(validacaoService.validarSubprocessosParaFinalizacao(codigo, TipoProcesso.DIAGNOSTICO))
                .thenReturn(ResultadoValidacao.ofValido());

        processoService.finalizar(codigo);

        verify(servicoAlertas, never()).criarAlertaAdmin(eq(processo), eq(admin), anyString());
        verify(notificacaoService, never()).enfileirar(argThat(cmd ->
                "ADMIN".equals(cmd.unidadeDestinoSigla())
        ));
        verify(processoRepo).save(processo);
    }

    @Test
    @DisplayName("finalizar deve carregar unidades consolidadas ausentes antes de notificar")
    void finalizarDeveCarregarUnidadesConsolidadasAusentesAntesDeNotificar() {
        Long codigoProcesso = 205L;
        Processo processo = new Processo();
        processo.setCodigo(codigoProcesso);
        processo.setDescricao("Processo consolidado");
        processo.setSituacao(EM_ANDAMENTO);
        processo.setTipo(MAPEAMENTO);

        Unidade unidadeSuperior = criarUnidadeValida(30L);
        unidadeSuperior.setTipo(TipoUnidade.INTERMEDIARIA);
        unidadeSuperior.setSigla("SUP");
        Unidade unidadeOperacional = criarUnidadeValida(31L);
        unidadeOperacional.setSigla("OPER");
        processo.adicionarParticipantes(Set.of(unidadeOperacional));

        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(3100L);
        subprocesso.setUnidade(unidadeOperacional);
        Mapa mapa = new Mapa();
        mapa.setSubprocesso(subprocesso);

        when(repo.buscar(Processo.class, codigoProcesso)).thenReturn(processo);
        when(validacaoService.validarSubprocessosParaFinalizacao(codigoProcesso, TipoProcesso.MAPEAMENTO)).thenReturn(ResultadoValidacao.ofValido());
        when(consultaService.listarEntidadesPorProcesso(codigoProcesso)).thenReturn(List.of(subprocesso));
        when(mapaManutencaoService.buscarMapasPorSubprocessos(List.of(3100L))).thenReturn(List.of(mapa));
        when(unidadeService.buscarPorCodigos(anyList())).thenAnswer(invocation -> {
            List<Long> codigos = invocation.getArgument(0);
            if (codigos.size() == 1 && codigos.contains(31L)) {
                return List.of(unidadeOperacional);
            }
            if (codigos.size() == 1 && codigos.contains(30L)) {
                return List.of(unidadeSuperior);
            }
            return List.of();
        });
        when(unidadeHierarquiaService.buscarCodigosSuperiores(31L)).thenReturn(List.of(30L));
        when(emailModelosService.criarEmailProcessoFinalizadoPorUnidade(anyString(), anyString(), any())).thenReturn("<html>direto</html>");
        when(emailModelosService.criarEmailProcessoFinalizadoUnidadesSubordinadas(eq("SUP"), eq("Processo consolidado"), eq(List.of("OPER")), eq(TipoProcesso.MAPEAMENTO)))
                .thenReturn("<html>consolidado</html>");

        processoService.finalizar(codigoProcesso);

        verify(unidadeService, atLeastOnce()).buscarPorCodigos(argThat(codigos -> codigos.size() == 1 && codigos.contains(30L)));
        verify(notificacaoService).enfileirar(argThat(cmd ->
                "SUP".equals(cmd.unidadeDestinoSigla()) && "<html>consolidado</html>".equals(cmd.corpoHtml())
        ));
    }

    @Test
    @DisplayName("Não deve criar notificacao consolidada de inicio para a unidade virtual ADMIN")
    void naoDeveCriarNotificacaoConsolidadaDeInicioParaUnidadeAdmin() {
        Long id = 300L;
        Processo processo = new Processo();
        processo.setCodigo(id);
        processo.setSituacao(CRIADO);
        processo.setTipo(MAPEAMENTO);
        processo.setDescricao("Processo sem agregacao admin");
        processo.setDataLimite(LocalDateTime.now().plusDays(30));

        Unidade admin = criarUnidadeValida(1L);
        admin.setSigla("ADMIN");
        admin.setTipo(TipoUnidade.RAIZ);
        Unidade operacional = criarUnidadeValida(10L);
        operacional.setSigla("OPER");
        operacional.setTipo(TipoUnidade.OPERACIONAL);
        processo.adicionarParticipantes(Set.of(operacional));

        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(501L);
        subprocesso.setProcesso(processo);
        subprocesso.setUnidade(operacional);

        when(repo.buscar(Processo.class, id)).thenReturn(processo);
        when(unidadeService.buscarAdmin()).thenReturn(admin);
        when(unidadeService.buscarPorCodigos(List.of(10L))).thenReturn(List.of(operacional));
        when(consultaService.listarEntidadesPorProcesso(id)).thenReturn(List.of(subprocesso));
        when(unidadeHierarquiaService.buscarCodigosSuperiores(10L)).thenReturn(List.of(1L));
        when(unidadeService.buscarPorCodigos(List.of(1L))).thenReturn(List.of(admin));
        when(unidadeService.buscarPorCodigo(1L)).thenReturn(admin);
        when(emailModelosService.criarEmailInicioProcessoConsolidado(anyString(), anyString(), any(), anyString(), anyBoolean(), anyList()))
                .thenReturn("<html>inicio</html>");
        mockarResponsaveisEfetivos();

        processoService.iniciar(id, List.of(10L));

        verify(notificacaoService, times(1)).enfileirar(any());
        verify(notificacaoService).enfileirar(argThat(cmd ->
                cmd.tipoNotificacao() == TipoNotificacao.PROCESSO_INICIADO
                        && "OPER".equals(cmd.unidadeDestinoSigla())
        ));
        verify(notificacaoService, never()).enfileirar(argThat(cmd ->
                "ADMIN".equals(cmd.unidadeDestinoSigla())
                        && cmd.tipoNotificacao() == TipoNotificacao.PROCESSO_INICIADO
        ));
    }

    @Test
    @DisplayName("Não deve criar notificacao direta de inicio para a unidade virtual ADMIN")
    void naoDeveCriarNotificacaoDiretaDeInicioParaUnidadeAdmin() {
        Long id = 301L;
        Processo processo = new Processo();
        processo.setCodigo(id);
        processo.setSituacao(CRIADO);
        processo.setTipo(MAPEAMENTO);
        processo.setDescricao("Processo sem notificacao admin");
        processo.setDataLimite(LocalDateTime.now().plusDays(30));

        Unidade admin = criarUnidadeValida(1L);
        admin.setSigla("ADMIN");
        admin.setTipo(TipoUnidade.RAIZ);
        processo.adicionarParticipantes(Set.of(admin));

        when(repo.buscar(Processo.class, id)).thenReturn(processo);
        when(unidadeService.buscarAdmin()).thenReturn(admin);
        when(unidadeService.buscarPorCodigos(List.of(1L))).thenReturn(List.of(admin));
        when(consultaService.listarEntidadesPorProcesso(id)).thenReturn(List.of());
        when(emailModelosService.criarEmailInicioProcessoConsolidado(anyString(), anyString(), any(), anyString(), anyBoolean(), anyList()))
                .thenReturn("<html>inicio</html>");
        mockarResponsaveisEfetivos();

        processoService.iniciar(id, List.of(1L));

        verify(notificacaoService, never()).enfileirar(argThat(cmd ->
                cmd.tipoNotificacao() == TipoNotificacao.PROCESSO_INICIADO
                        && "ADMIN".equals(cmd.unidadeDestinoSigla())
        ));
    }
}


