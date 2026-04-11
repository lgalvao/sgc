package sgc.processo.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import sgc.alerta.*;
import sgc.comum.*;
import sgc.comum.erros.*;
import sgc.comum.model.*;
import sgc.organizacao.*;
import sgc.organizacao.model.*;
import sgc.organizacao.service.*;
import sgc.processo.dto.*;
import sgc.processo.model.*;
import sgc.seguranca.*;
import sgc.subprocesso.model.*;
import sgc.subprocesso.service.*;

import java.time.*;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static sgc.processo.model.AcaoProcesso.*;
import static sgc.processo.model.SituacaoProcesso.*;
import static sgc.processo.model.TipoProcesso.*;
import static sgc.seguranca.AcaoPermissao.*;
import static sgc.subprocesso.model.SituacaoSubprocesso.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProcessoService - Cobertura de Testes")
@SuppressWarnings("NullAway.Init")
class ProcessoServiceCoverageTest {
    @Mock private ProcessoRepo processoRepo;
    @Mock private ComumRepo repo;
    @Mock private UnidadeHierarquiaService unidadeHierarquiaService;
    @Mock private UnidadeService unidadeService;
    @Mock private ResponsavelUnidadeService responsavelUnidadeService;
    @Mock private UsuarioFacade usuarioService;
    @Mock private SubprocessoService subprocessoService; @Mock private SubprocessoConsultaService consultaService;
    @Mock private LocalizacaoSubprocessoService localizacaoSubprocessoService;
    @Mock private SubprocessoValidacaoService validacaoService;
    @Mock private AlertaFacade servicoAlertas;
    @Mock private SgcPermissionEvaluator permissionEvaluator;

    @Mock private EmailService emailService;
    @Mock private EmailModelosService emailModelosService;
    @Mock private SubprocessoTransicaoService transicaoService;

    @InjectMocks
    private ProcessoService target;

    private void mockarResponsaveisEfetivos() {
        when(responsavelUnidadeService.todasPossuemResponsavelEfetivo(anyList())).thenReturn(true);
    }

    @Test
    @DisplayName("executarAcaoEmBloco deve lançar ErroAcessoNegado quando não houver permissão")
    void deveLancarErroAcessoNegado() {
        Long codProcesso = 100L;
        DisponibilizarMapaEmBlocoCommand req = new DisponibilizarMapaEmBlocoCommand(List.of(1L), LocalDate.now().plusDays(1));

        Subprocesso sp = mock(Subprocesso.class);
        Usuario usuario = new Usuario();
        when(consultaService.listarEntidadesPorProcessoEUnidades(eq(codProcesso), anyList()))
                .thenReturn(List.of(sp));

        when(usuarioService.usuarioAutenticado()).thenReturn(usuario);

        when(permissionEvaluator.verificarPermissao(any(Usuario.class), any(List.class), any(AcaoPermissao.class))).thenReturn(false);

        assertThatThrownBy(() -> target.executarAcaoEmBloco(codProcesso, req))
                .isInstanceOf(ErroAcessoNegado.class);
    }

    @Test
    @DisplayName("AcaoEmBlocoRequest deve exigir data limite ao converter disponibilizacao")
    void deveExigirDataLimiteAoConverterDisponibilizacao() {
        AcaoEmBlocoRequest req = new AcaoEmBlocoRequest(List.of(1L), DISPONIBILIZAR, null);

        assertThatThrownBy(req::paraCommand)
                .isInstanceOf(ErroValidacao.class)
                .hasMessage(Mensagens.DATA_LIMITE_OBRIGATORIA);
    }

    @Test
    @DisplayName("obterDetalhesCompleto deve reutilizar subprocessos ja carregados ao listar elegiveis")
    void deveReutilizarSubprocessosAoListarElegiveisNoContextoCompleto() {
        Long cod = 1L;
        Processo processo = new Processo();
        processo.setCodigo(cod);
        processo.setTipo(MAPEAMENTO);
        processo.setSituacao(EM_ANDAMENTO);

        Unidade unidade = new Unidade();
        unidade.setCodigo(10L);
        unidade.setSigla("U10");
        unidade.setNome("Unidade 10");
        unidade.setTipo(TipoUnidade.OPERACIONAL);
        unidade.setSituacao(SituacaoUnidade.ATIVA);
        processo.adicionarParticipantes(Set.of(unidade));

        Usuario usuario = new Usuario();
        usuario.setPerfilAtivo(Perfil.ADMIN);
        usuario.setUnidadeAtivaCodigo(10L);

        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(100L);
        subprocesso.setUnidade(unidade);
        subprocesso.setSituacao(MAPEAMENTO_CADASTRO_DISPONIBILIZADO);
        subprocesso.setDataLimiteEtapa1(LocalDateTime.now().plusDays(1));

        when(repo.buscar(Processo.class, cod)).thenReturn(processo);
        when(consultaService.listarEntidadesPorProcesso(cod)).thenReturn(List.of(subprocesso));
        when(localizacaoSubprocessoService.obterLocalizacoesAtuais(anyCollection())).thenReturn(Map.of(subprocesso.getCodigo(), unidade));
        when(permissionEvaluator.verificarPermissao(usuario, processo, FINALIZAR_PROCESSO)).thenReturn(false);
        when(permissionEvaluator.verificarPermissaoSilenciosa(eq(usuario), any(Subprocesso.class), any(AcaoPermissao.class))).thenReturn(true);

        when(usuarioService.usuarioAutenticado()).thenReturn(usuario);
        ProcessoDetalheDto resultado = target.obterDetalhesCompleto(cod, true);

        assertThat(resultado.getElegiveis()).hasSize(1);
        verify(consultaService, times(1)).listarEntidadesPorProcesso(cod);
        verify(localizacaoSubprocessoService, times(1)).obterLocalizacoesAtuais(anyCollection());
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

        UnidadeProcesso up = new UnidadeProcesso();
        up.setUnidadeCodigo(10L);
        when(p.getParticipantes()).thenReturn(List.of(up));

        Unidade uni = new Unidade();
        uni.setCodigo(10L);
        when(unidadeService.buscarPorCodigos(anyList())).thenReturn(List.of(uni));

        when(repo.buscar(Processo.class, codigo)).thenReturn(p);

        SubprocessoValidacaoService.ValidationResult v = SubprocessoValidacaoService.ValidationResult.ofValido();
        when(validacaoService.validarSubprocessosParaFinalizacao(codigo)).thenReturn(v);

        target.finalizar(codigo);

        verify(p).setSituacao(FINALIZADO);
        verify(servicoAlertas).criarAlertaAdmin(p, uni, "Processo finalizado: Desc");
        verify(processoRepo).save(p);
    }

    @Test
    @DisplayName("validarSelecaoBloco deve lançar ErroValidacao quando tamanhos diferirem")
    void deveLancarErroValidacaoEmBloco() {
        Long codProcesso = 100L;
        DisponibilizarMapaEmBlocoCommand req = new DisponibilizarMapaEmBlocoCommand(List.of(1L, 2L), LocalDate.now().plusDays(1));

        Subprocesso sp = mock(Subprocesso.class);
        Unidade u = new Unidade();
        Usuario usuario = new Usuario();
        u.setCodigo(1L);
        when(sp.getUnidade()).thenReturn(u);

        // Retorna apenas 1 subprocesso para 2 códigos solicitados
        when(consultaService.listarEntidadesPorProcessoEUnidades(eq(codProcesso), anyList()))
                .thenReturn(List.of(sp));

        when(usuarioService.usuarioAutenticado()).thenReturn(usuario);

        assertThatThrownBy(() -> target.executarAcaoEmBloco(codProcesso, req))
                .isInstanceOf(ErroValidacao.class);
    }

    @Test
    @DisplayName("enviarLembrete deve lançar IllegalStateException quando processo não tem data limite")
    void enviarLembreteSemDataLimite() {
        Long codProcesso = 1L;
        Long unidadeCodigo = 10L;
        Processo p = new Processo();
        UnidadeProcesso up = new UnidadeProcesso();
        up.setUnidadeCodigo(unidadeCodigo);
        p.setParticipantes(new ArrayList<>(List.of(up)));
        
        when(processoRepo.buscarPorCodigoComParticipantes(codProcesso)).thenReturn(Optional.of(p));
        when(unidadeService.buscarPorCodigo(unidadeCodigo)).thenReturn(new Unidade());

        assertThatThrownBy(() -> target.enviarLembrete(codProcesso, unidadeCodigo))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sem data limite");
    }

    @Test
    @DisplayName("iniciar deve lançar IllegalStateException quando unidade não tem mapa no modo Revisão")
    void iniciarSemMapaRevisao() {
        Long cod = 1L;
        Processo p = new Processo();
        p.setCodigo(cod);
        p.setTipo(REVISAO);
        p.setSituacao(CRIADO);
        
        when(repo.buscar(Processo.class, cod)).thenReturn(p);
        
        Unidade uni = new Unidade();
        uni.setCodigo(10L);
        uni.setSigla("U10");
        uni.setNome("Unidade 10");
        uni.setTipo(sgc.organizacao.model.TipoUnidade.OPERACIONAL);
        uni.setSituacao(sgc.organizacao.model.SituacaoUnidade.ATIVA);
        
        when(unidadeService.buscarPorCodigos(anyList())).thenReturn(List.of(uni));
        when(unidadeService.buscarMapasPorUnidades(anyList())).thenReturn(new ArrayList<>());
        mockarResponsaveisEfetivos();
        
        Unidade admin = new Unidade();
        admin.setCodigo(99L);
        admin.setSigla("ADMIN");
        admin.setNome("Administrador");
        admin.setTipo(sgc.organizacao.model.TipoUnidade.RAIZ);
        admin.setSituacao(sgc.organizacao.model.SituacaoUnidade.ATIVA);
        when(unidadeService.buscarAdmin()).thenReturn(admin);

        List<Long> unidadeCods = List.of(10L);
        // when(usuarioService.usuarioAutenticado()).thenReturn(usuario); - desnecessário nesta versão
        assertThatThrownBy(() -> target.iniciar(cod, unidadeCods))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sem mapa vigente");
    }

    @Nested
    @DisplayName("Gaps de Data Limite no DTO")
    class DataLimiteGaps {
        @Test
        @DisplayName("obterDetalhesCompleto deve lançar IllegalStateException quando etapa 2 existe sem etapa 1")
        void etapa2SemEtapa1() {
            Long cod = 1L;
            Processo p = new Processo();
            p.setCodigo(cod);
            p.setTipo(MAPEAMENTO);
            Usuario u = new Usuario();
            u.setPerfilAtivo(Perfil.ADMIN);
            
            Subprocesso sp = new Subprocesso();
            sp.setCodigo(100L);
            sp.setSituacao(MAPEAMENTO_CADASTRO_DISPONIBILIZADO);
            Unidade uni = new Unidade();
            uni.setCodigo(10L);
            sp.setUnidade(uni);
            sp.setDataLimiteEtapa2(java.time.LocalDateTime.now());
            
            when(repo.buscar(Processo.class, cod)).thenReturn(p);
            when(consultaService.listarEntidadesPorProcesso(cod)).thenReturn(List.of(sp));
            when(localizacaoSubprocessoService.obterLocalizacoesAtuais(anyCollection())).thenReturn(Map.of(sp.getCodigo(), uni));
            when(permissionEvaluator.verificarPermissao(u, p, sgc.seguranca.AcaoPermissao.FINALIZAR_PROCESSO)).thenReturn(true);
            when(validacaoService.validarSubprocessosParaFinalizacao(cod)).thenReturn(sgc.subprocesso.service.SubprocessoValidacaoService.ValidationResult.ofValido());
            when(permissionEvaluator.verificarPermissaoSilenciosa(eq(u), any(Subprocesso.class), any())).thenReturn(true);

            when(usuarioService.usuarioAutenticado()).thenReturn(u);
            assertThatThrownBy(() -> target.obterDetalhesCompleto(cod, true))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("sem data limite da etapa 1");
        }

        @Test
        @DisplayName("obterDetalhesCompleto deve priorizar dataLimite2 quando etapa 1 é posterior à etapa 2")
        void etapa1PosteriorEtapa2() {
            Long cod = 1L;
            Processo p = new Processo();
            p.setCodigo(cod);
            p.setTipo(MAPEAMENTO);
            Usuario u = new Usuario();
            u.setPerfilAtivo(Perfil.ADMIN);
            
            Subprocesso sp = new Subprocesso();
            sp.setCodigo(100L);
            sp.setSituacao(MAPEAMENTO_CADASTRO_DISPONIBILIZADO);
            Unidade uni = new Unidade();
            uni.setCodigo(10L);
            sp.setUnidade(uni);
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            sp.setDataLimiteEtapa1(now.plusDays(2));
            sp.setDataLimiteEtapa2(now.plusDays(1));
            
            when(repo.buscar(Processo.class, cod)).thenReturn(p);
            when(consultaService.listarEntidadesPorProcesso(cod)).thenReturn(List.of(sp));
            when(localizacaoSubprocessoService.obterLocalizacoesAtuais(anyCollection())).thenReturn(Map.of(sp.getCodigo(), uni));
            when(permissionEvaluator.verificarPermissao(u, p, sgc.seguranca.AcaoPermissao.FINALIZAR_PROCESSO)).thenReturn(true);
            when(validacaoService.validarSubprocessosParaFinalizacao(cod)).thenReturn(sgc.subprocesso.service.SubprocessoValidacaoService.ValidationResult.ofValido());
            when(permissionEvaluator.verificarPermissaoSilenciosa(eq(u), any(Subprocesso.class), any())).thenReturn(true);

            when(usuarioService.usuarioAutenticado()).thenReturn(u);
            assertThatCode(() -> target.obterDetalhesCompleto(cod, true))
                    .doesNotThrowAnyException();
        }
        
        @Test
        @DisplayName("obterDetalhesCompleto deve retornar dataLimite2 quando válida via elegíveis")
        void etapa2Valida() {
            Long cod = 1L;
            Processo p = new Processo();
            p.setCodigo(cod);
            p.setTipo(MAPEAMENTO);
            Usuario u = new Usuario();
            u.setPerfilAtivo(Perfil.ADMIN);
            
            Subprocesso sp = new Subprocesso();
            sp.setCodigo(100L);
            sp.setSituacao(MAPEAMENTO_CADASTRO_DISPONIBILIZADO);
            Unidade uni = new Unidade();
            uni.setCodigo(10L);
            sp.setUnidade(uni);
            java.time.LocalDateTime d1 = java.time.LocalDateTime.now().plusDays(1);
            java.time.LocalDateTime d2 = java.time.LocalDateTime.now().plusDays(2);
            sp.setDataLimiteEtapa1(d1);
            sp.setDataLimiteEtapa2(d2);
            
            when(repo.buscar(Processo.class, cod)).thenReturn(p);
            when(consultaService.listarEntidadesPorProcesso(cod)).thenReturn(List.of(sp));
            when(localizacaoSubprocessoService.obterLocalizacoesAtuais(anyCollection())).thenReturn(Map.of(sp.getCodigo(), uni));
            when(permissionEvaluator.verificarPermissao(u, p, sgc.seguranca.AcaoPermissao.FINALIZAR_PROCESSO)).thenReturn(true);
            when(validacaoService.validarSubprocessosParaFinalizacao(cod)).thenReturn(sgc.subprocesso.service.SubprocessoValidacaoService.ValidationResult.ofValido());
            when(permissionEvaluator.verificarPermissaoSilenciosa(eq(u), any(Subprocesso.class), any())).thenReturn(true);

            when(usuarioService.usuarioAutenticado()).thenReturn(u);
            ProcessoDetalheDto res = target.obterDetalhesCompleto(cod, true);
            assertThat(res.getElegiveis()).isNotEmpty();
            assertThat(res.getElegiveis().getFirst().getUltimaDataLimite()).isEqualTo(d2);
        }

        @Test
        @DisplayName("obterDetalhesCompleto deve tratar subprocesso sem mapa")
        void subprocessoSemMapa() {
            Long cod = 1L;
            Processo p = new Processo(); p.setCodigo(cod); p.setTipo(MAPEAMENTO);
            Usuario u = new Usuario(); u.setPerfilAtivo(Perfil.ADMIN); u.setUnidadeAtivaCodigo(10L);
            
            Subprocesso sp = new Subprocesso(); sp.setCodigo(100L); sp.setSituacao(MAPEAMENTO_CADASTRO_DISPONIBILIZADO);
            Unidade uni = new Unidade(); uni.setCodigo(10L); 
            uni.setSigla("U10"); uni.setNome("Unidade 10"); uni.setTipo(TipoUnidade.OPERACIONAL); uni.setSituacao(SituacaoUnidade.ATIVA);
            sp.setUnidade(uni);
            sp.setDataLimiteEtapa1(java.time.LocalDateTime.now());
            sp.setMapa(null); // branch 441
            
            p.adicionarParticipantes(Set.of(uni));

            when(repo.buscar(Processo.class, cod)).thenReturn(p);
            when(consultaService.listarEntidadesPorProcesso(cod)).thenReturn(List.of(sp));
            when(localizacaoSubprocessoService.obterLocalizacoesAtuais(anyCollection())).thenReturn(Map.of(sp.getCodigo(), uni));
            when(permissionEvaluator.verificarPermissao(any(Usuario.class), any(Processo.class), any(AcaoPermissao.class))).thenReturn(true);
            when(validacaoService.validarSubprocessosParaFinalizacao(cod)).thenReturn(sgc.subprocesso.service.SubprocessoValidacaoService.ValidationResult.ofValido());

            when(usuarioService.usuarioAutenticado()).thenReturn(u);
            ProcessoDetalheDto res = target.obterDetalhesCompleto(cod, false);
            assertThat(res.getUnidades()).hasSize(1);
            assertThat(res.getUnidades().getFirst().getMapaCodigo()).isNull();
        }
    }

    @Test
    @DisplayName("iniciar deve suportar tipo DIAGNOSTICO")
    void iniciarDiagnostico() {
        Long cod = 1L;
        Processo p = new Processo(); p.setCodigo(cod); p.setTipo(DIAGNOSTICO); p.setSituacao(CRIADO);
        Unidade u = new Unidade(); u.setCodigo(10L); u.setTipo(sgc.organizacao.model.TipoUnidade.OPERACIONAL);
        u.setSigla("U10"); u.setSituacao(SituacaoUnidade.ATIVA);
        p.adicionarParticipantes(Set.of(u));

        Unidade admin = new Unidade(); admin.setCodigo(99L); admin.setSigla("ADMIN"); admin.setSituacao(SituacaoUnidade.ATIVA);

        when(repo.buscar(Processo.class, cod)).thenReturn(p);
        when(unidadeService.buscarPorCodigos(any())).thenReturn(List.of(u));
        UnidadeMapa um = new UnidadeMapa(); um.setUnidadeCodigo(10L); 
        when(unidadeService.buscarMapasPorUnidades(any())).thenReturn(List.of(um));
        when(unidadeService.buscarAdmin()).thenReturn(admin);
        mockarResponsaveisEfetivos();

        // when(usuarioService.usuarioAutenticado()).thenReturn(new Usuario()); - desnecessário
        target.iniciar(cod, List.of(10L)); // branch 419 (DIAGNOSTICO)
        verify(subprocessoService).criarParaDiagnostico(any());
    }

    @Test
    @DisplayName("iniciar deve lançar IllegalStateException quando unidade obrigatória está ausente")
    void iniciarUnidadeObrigatoriaAusente() {
        Long cod = 1L;
        Processo p = new Processo(); p.setCodigo(cod); p.setTipo(REVISAO); p.setSituacao(CRIADO);
        
        when(repo.buscar(Processo.class, cod)).thenReturn(p);
        
        Unidade u10 = new Unidade(); u10.setCodigo(10L); u10.setSigla("U10"); u10.setTipo(sgc.organizacao.model.TipoUnidade.OPERACIONAL); u10.setSituacao(SituacaoUnidade.ATIVA);
        
        // Retorna apenas U10, mas vamos pedir 10 e 11
        when(unidadeService.buscarPorCodigos(anyList())).thenReturn(List.of(u10));
        
        UnidadeMapa um10 = new UnidadeMapa(); um10.setUnidadeCodigo(10L);
        UnidadeMapa um11 = new UnidadeMapa(); um11.setUnidadeCodigo(11L);
        when(unidadeService.buscarMapasPorUnidades(anyList())).thenReturn(List.of(um10, um11));
        
        mockarResponsaveisEfetivos();
        when(unidadeService.buscarAdmin()).thenReturn(new Unidade());

        List<Long> codigos = List.of(10L, 11L);
        // when(usuarioService.usuarioAutenticado()).thenReturn(usuario); - desnecessário
        assertThatThrownBy(() -> target.iniciar(cod, codigos))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Unidade 11 ausente para iniciar subprocesso");
    }

    @Test
    @DisplayName("executarAcaoEmBloco - acao HOMOLOGAR")
    void executarAcaoEmBloco_Homologar() {
        Long codProc = 1L;
        ProcessarAnaliseEmBlocoCommand req = new ProcessarAnaliseEmBlocoCommand(List.of(10L), AcaoProcesso.HOMOLOGAR);
        Subprocesso sp = new Subprocesso(); sp.setCodigo(100L); sp.setSituacao(MAPEAMENTO_CADASTRO_DISPONIBILIZADO);
        Unidade u = new Unidade(); u.setCodigo(10L); sp.setUnidade(u);

        when(consultaService.listarEntidadesPorProcessoEUnidades(eq(codProc), anyList())).thenReturn(List.of(sp));
        when(usuarioService.usuarioAutenticado()).thenReturn(new Usuario());

        target.executarAcaoEmBloco(codProc, req); // branch 570 (HOMOLOGAR)
        verify(transicaoService).homologarCadastroEmBloco(anyList());
    }

    @Nested
    @DisplayName("Elegibilidade e Hierarquia")
    class ElegibilidadeHierarquia {
        @Test
        @DisplayName("buscarDescendentes com ciclo ou repetição")
        void buscarDescendentesRepeticao() {
            Unidade u1 = new Unidade(); u1.setCodigo(1L);
            Unidade u2 = new Unidade(); u2.setCodigo(2L);
            u2.setUnidadeSuperior(u1);
            Unidade u3 = new Unidade(); u3.setCodigo(2L); // mesma unidade 2
            u3.setUnidadeSuperior(u1);

            when(unidadeHierarquiaService.buscarIdsDescendentes(1L)).thenReturn(List.of(2L));
            
            List<Long> res = org.springframework.test.util.ReflectionTestUtils.invokeMethod(target, "buscarDescendentes", 1L);
            assertThat(res).containsExactlyInAnyOrder(1L, 2L); // branch 357 (result.add(codFilho) == false)
        }

        @Test
        @DisplayName("isElegivelParaAcaoEmBloco - nao elegivel por situacao")
        void isElegivel_NaoElegivelSituacao() {
            Usuario user = new Usuario();
            Subprocesso sp = new Subprocesso();
            sp.setSituacao(NAO_INICIADO);
            
            Boolean res = org.springframework.test.util.ReflectionTestUtils.invokeMethod(target, "isElegivelParaAcaoEmBloco", sp, user);
            assertThat(res).isNotNull().isFalse();
        }

        @Test
        @DisplayName("isElegivelParaAcaoEmBloco - homologar cadastro autorizado")
        void isElegivel_HomologarCadastro() {
            Usuario user = new Usuario();
            Subprocesso sp = new Subprocesso();
            sp.setSituacao(MAPEAMENTO_CADASTRO_DISPONIBILIZADO);
            
            when(permissionEvaluator.verificarPermissaoSilenciosa(any(Usuario.class), any(Subprocesso.class), eq(ACEITAR_CADASTRO))).thenReturn(false);
            when(permissionEvaluator.verificarPermissaoSilenciosa(any(Usuario.class), any(Subprocesso.class), eq(HOMOLOGAR_CADASTRO))).thenReturn(true);
            
            Boolean res = org.springframework.test.util.ReflectionTestUtils.invokeMethod(target, "isElegivelParaAcaoEmBloco", sp, user);
            assertThat(res).isNotNull().isTrue();
        }

        @Test
        @DisplayName("isElegivelParaAcaoEmBloco - homologar cadastro autorizado em revisão")
        void isElegivel_HomologarCadastroRevisao() {
            Usuario user = new Usuario();
            Subprocesso sp = new Subprocesso();
            sp.setSituacao(REVISAO_CADASTRO_DISPONIBILIZADA);

            when(permissionEvaluator.verificarPermissaoSilenciosa(any(Usuario.class), any(Subprocesso.class), eq(ACEITAR_CADASTRO))).thenReturn(false);
            when(permissionEvaluator.verificarPermissaoSilenciosa(any(Usuario.class), any(Subprocesso.class), eq(HOMOLOGAR_CADASTRO))).thenReturn(true);

            Boolean res = org.springframework.test.util.ReflectionTestUtils.invokeMethod(target, "isElegivelParaAcaoEmBloco", sp, user);
            assertThat(res).isNotNull().isTrue();
        }

        @Test
        @DisplayName("isElegivelParaAcaoEmBloco - homologar mapa autorizado")
        void isElegivel_HomologarMapa() {
            Usuario user = new Usuario();
            Subprocesso sp = new Subprocesso();
            sp.setSituacao(MAPEAMENTO_MAPA_VALIDADO);
            
            when(permissionEvaluator.verificarPermissaoSilenciosa(any(Usuario.class), any(Subprocesso.class), eq(ACEITAR_MAPA))).thenReturn(false);
            when(permissionEvaluator.verificarPermissaoSilenciosa(any(Usuario.class), any(Subprocesso.class), eq(HOMOLOGAR_MAPA))).thenReturn(true);
            
            Boolean res = org.springframework.test.util.ReflectionTestUtils.invokeMethod(target, "isElegivelParaAcaoEmBloco", sp, user);
            assertThat(res).isNotNull().isTrue();
        }

        @Test
        @DisplayName("isElegivelParaAcaoEmBloco - homologar mapa autorizado em revisão")
        void isElegivel_HomologarMapaRevisao() {
            Usuario user = new Usuario();
            Subprocesso sp = new Subprocesso();
            sp.setSituacao(REVISAO_MAPA_VALIDADO);

            when(permissionEvaluator.verificarPermissaoSilenciosa(any(Usuario.class), any(Subprocesso.class), eq(ACEITAR_MAPA))).thenReturn(false);
            when(permissionEvaluator.verificarPermissaoSilenciosa(any(Usuario.class), any(Subprocesso.class), eq(HOMOLOGAR_MAPA))).thenReturn(true);

            Boolean res = org.springframework.test.util.ReflectionTestUtils.invokeMethod(target, "isElegivelParaAcaoEmBloco", sp, user);
            assertThat(res).isNotNull().isTrue();
        }

        @Test
        @DisplayName("isElegivelParaAcaoEmBloco - nao elegivel em revisão cadastro homologado (sem mapa criado)")
        void isElegivel_NaoElegivelRevisaoCadastroHomologado() {
            Usuario user = new Usuario();
            Subprocesso sp = new Subprocesso();
            sp.setSituacao(REVISAO_CADASTRO_HOMOLOGADA);

            Boolean res = org.springframework.test.util.ReflectionTestUtils.invokeMethod(target, "isElegivelParaAcaoEmBloco", sp, user);
            assertThat(res).isNotNull().isFalse();
        }

        @Test
        @DisplayName("isElegivelParaAcaoEmBloco - disponibilizar revisão ajustada")
        void isElegivel_DisponibilizarRevisaoAjustada() {
            Usuario user = new Usuario();
            Subprocesso sp = new Subprocesso();
            sp.setSituacao(REVISAO_MAPA_AJUSTADO);

            when(permissionEvaluator.verificarPermissaoSilenciosa(any(Usuario.class), any(Subprocesso.class), eq(DISPONIBILIZAR_MAPA))).thenReturn(true);

            Boolean res = org.springframework.test.util.ReflectionTestUtils.invokeMethod(target, "isElegivelParaAcaoEmBloco", sp, user);
            assertThat(res).isNotNull().isTrue();
        }

        @Test
        @DisplayName("isSituacaoCadastro - branches 577")
        void isSituacaoCadastro_Branches() {
            assertThat((Boolean)org.springframework.test.util.ReflectionTestUtils.invokeMethod(target, "isSituacaoCadastro", MAPEAMENTO_CADASTRO_DISPONIBILIZADO)).isTrue();
            assertThat((Boolean)org.springframework.test.util.ReflectionTestUtils.invokeMethod(target, "isSituacaoCadastro", REVISAO_CADASTRO_DISPONIBILIZADA)).isTrue();
            assertThat((Boolean)org.springframework.test.util.ReflectionTestUtils.invokeMethod(target, "isSituacaoCadastro", REVISAO_CADASTRO_HOMOLOGADA)).isTrue();
            assertThat((Boolean)org.springframework.test.util.ReflectionTestUtils.invokeMethod(target, "isSituacaoCadastro", MAPEAMENTO_MAPA_DISPONIBILIZADO)).isFalse();
        }
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
        u.setTituloTitular("titular");
        u.setSituacao(sgc.organizacao.model.SituacaoUnidade.ATIVA);
        u.setTipo(sgc.organizacao.model.TipoUnidade.OPERACIONAL);
        
        p.adicionarParticipantes(Set.of(u));
        
        Usuario titular = new Usuario();
        titular.setEmail("teste@teste.com");

        when(processoRepo.buscarPorCodigoComParticipantes(1L)).thenReturn(Optional.of(p));
        when(unidadeService.buscarPorCodigo(10L)).thenReturn(u);
        when(usuarioService.buscarPorLogin("titular")).thenReturn(titular);
        when(emailModelosService.criarEmailLembretePrazo(any(), any(), any())).thenReturn("<html></html>");

        target.enviarLembrete(1L, 10L);

        verify(emailService).enviarEmailHtml(eq("teste@teste.com"), anyString(), anyString());
    }

    @Test
    @DisplayName("obterUltimaDataLimite - apenas etapa 1 deve retornar data 1")
    void obterUltimaDataLimiteApenasEtapa1() {
        Subprocesso sp = new Subprocesso();
        sp.setCodigo(100L);
        LocalDateTime d1 = LocalDateTime.now();
        sp.setDataLimiteEtapa1(d1);
        sp.setDataLimiteEtapa2(null);

        LocalDateTime res = org.springframework.test.util.ReflectionTestUtils.invokeMethod(target, "obterUltimaDataLimite", sp);
        assertThat(res).isEqualTo(d1);
    }
}
