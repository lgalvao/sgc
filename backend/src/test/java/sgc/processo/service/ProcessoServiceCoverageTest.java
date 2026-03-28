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

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static sgc.processo.model.AcaoProcesso.*;
import static sgc.processo.model.SituacaoProcesso.*;
import static sgc.processo.model.TipoProcesso.*;
import static sgc.subprocesso.model.SituacaoSubprocesso.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProcessoService - Cobertura de Testes")
@SuppressWarnings("NullAway.Init")
class ProcessoServiceCoverageTest {
    @Mock private ProcessoRepo processoRepo;
    @Mock private ComumRepo repo;
    @Mock private UnidadeService unidadeService;
    @Mock private UsuarioFacade usuarioService;
    @Mock private SubprocessoService subprocessoService;
    @Mock private SubprocessoValidacaoService validacaoService;
    @Mock private AlertaFacade servicoAlertas;
    @Mock private SgcPermissionEvaluator permissionEvaluator;

    @Mock private EmailService emailService;
    @Mock private EmailModelosService emailModelosService;

    @InjectMocks
    private ProcessoService target;

    @Test
    @DisplayName("executarAcaoEmBloco deve lançar ErroAcessoNegado quando não houver permissão")
    void deveLancarErroAcessoNegado() {
        Long codProcesso = 100L;
        AcaoEmBlocoRequest req = new AcaoEmBlocoRequest(List.of(1L), DISPONIBILIZAR, null);

        Subprocesso sp = mock(Subprocesso.class);
        Usuario usuario = new Usuario();
        when(subprocessoService.listarEntidadesPorProcessoEUnidades(eq(codProcesso), anyList()))
                .thenReturn(List.of(sp));

        when(usuarioService.usuarioAutenticado()).thenReturn(usuario);

        when(permissionEvaluator.verificarPermissao(any(), anyList(), any())).thenReturn(false);

        assertThatThrownBy(() -> target.executarAcaoEmBloco(codProcesso, req))
                .isInstanceOf(ErroAcessoNegado.class);
    }

    @Test
    @DisplayName("executarAcaoEmBloco deve exigir data limite ao disponibilizar")
    void deveExigirDataLimiteAoDisponibilizar() {
        Long codProcesso = 100L;
        AcaoEmBlocoRequest req = new AcaoEmBlocoRequest(List.of(1L), DISPONIBILIZAR, null);

        Subprocesso sp = mock(Subprocesso.class);
        Usuario usuario = new Usuario();
        when(subprocessoService.listarEntidadesPorProcessoEUnidades(eq(codProcesso), anyList()))
                .thenReturn(List.of(sp));
        when(usuarioService.usuarioAutenticado()).thenReturn(usuario);
        when(permissionEvaluator.verificarPermissao(any(), anyList(), any())).thenReturn(true);

        assertThatThrownBy(() -> target.executarAcaoEmBloco(codProcesso, req))
                .isInstanceOf(ErroValidacao.class)
                .hasMessage(Mensagens.DATA_LIMITE_OBRIGATORIA);
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
        when(unidadeService.porCodigos(anyList())).thenReturn(List.of(uni));

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
        AcaoEmBlocoRequest req = new AcaoEmBlocoRequest(List.of(1L, 2L), DISPONIBILIZAR, null);

        Subprocesso sp = mock(Subprocesso.class);
        Unidade u = new Unidade();
        Usuario usuario = new Usuario();
        u.setCodigo(1L);
        when(sp.getUnidade()).thenReturn(u);

        // Retorna apenas 1 subprocesso para 2 códigos solicitados
        when(subprocessoService.listarEntidadesPorProcessoEUnidades(eq(codProcesso), anyList()))
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
        
        when(unidadeService.porCodigos(anyList())).thenReturn(List.of(uni));
        when(unidadeService.buscarMapasPorUnidades(anyList())).thenReturn(new ArrayList<>());
        
        Unidade admin = new Unidade();
        admin.setCodigo(99L);
        admin.setSigla("ADMIN");
        admin.setNome("Administrador");
        admin.setTipo(sgc.organizacao.model.TipoUnidade.RAIZ);
        admin.setSituacao(sgc.organizacao.model.SituacaoUnidade.ATIVA);
        when(repo.buscarPorSigla(any(), anyString())).thenReturn(admin);

        List<Long> unidadeCods = List.of(10L);
        Usuario usuario = new Usuario();
        assertThatThrownBy(() -> target.iniciar(cod, unidadeCods, usuario))
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
            when(usuarioService.usuarioAutenticado()).thenReturn(u);
            when(subprocessoService.listarEntidadesPorProcesso(cod)).thenReturn(List.of(sp));
            when(subprocessoService.listarEntidadesPorProcessoComUnidade(cod)).thenReturn(List.of(sp));
            when(subprocessoService.obterLocalizacaoAtual(sp)).thenReturn(uni);
            when(permissionEvaluator.verificarPermissao(u, p, sgc.seguranca.AcaoPermissao.FINALIZAR_PROCESSO)).thenReturn(true);
            when(validacaoService.validarSubprocessosParaFinalizacao(cod)).thenReturn(sgc.subprocesso.service.SubprocessoValidacaoService.ValidationResult.ofValido());
            when(permissionEvaluator.verificarPermissao(eq(u), any(Subprocesso.class), any())).thenReturn(true);

            assertThatThrownBy(() -> target.obterDetalhesCompleto(cod, u, true))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("sem data limite da etapa 1");
        }

        @Test
        @DisplayName("obterDetalhesCompleto deve lançar IllegalStateException quando etapa 1 é posterior à etapa 2")
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
            when(usuarioService.usuarioAutenticado()).thenReturn(u);
            when(subprocessoService.listarEntidadesPorProcesso(cod)).thenReturn(List.of(sp));
            when(subprocessoService.listarEntidadesPorProcessoComUnidade(cod)).thenReturn(List.of(sp));
            when(subprocessoService.obterLocalizacaoAtual(sp)).thenReturn(uni);
            when(permissionEvaluator.verificarPermissao(u, p, sgc.seguranca.AcaoPermissao.FINALIZAR_PROCESSO)).thenReturn(true);
            when(validacaoService.validarSubprocessosParaFinalizacao(cod)).thenReturn(sgc.subprocesso.service.SubprocessoValidacaoService.ValidationResult.ofValido());
            when(permissionEvaluator.verificarPermissao(eq(u), any(Subprocesso.class), any())).thenReturn(true);

            assertThatThrownBy(() -> target.obterDetalhesCompleto(cod, u, true))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("posterior à etapa 2");
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
            when(usuarioService.usuarioAutenticado()).thenReturn(u);
            when(subprocessoService.listarEntidadesPorProcesso(cod)).thenReturn(List.of(sp));
            when(subprocessoService.listarEntidadesPorProcessoComUnidade(cod)).thenReturn(List.of(sp));
            when(subprocessoService.obterLocalizacaoAtual(sp)).thenReturn(uni);
            when(permissionEvaluator.verificarPermissao(u, p, sgc.seguranca.AcaoPermissao.FINALIZAR_PROCESSO)).thenReturn(true);
            when(validacaoService.validarSubprocessosParaFinalizacao(cod)).thenReturn(sgc.subprocesso.service.SubprocessoValidacaoService.ValidationResult.ofValido());
            when(permissionEvaluator.verificarPermissao(eq(u), any(Subprocesso.class), any())).thenReturn(true);

            ProcessoDetalheDto res = target.obterDetalhesCompleto(cod, u, true);
            assertThat(res.getElegiveis()).isNotEmpty();
            assertThat(res.getElegiveis().getFirst().getUltimaDataLimite()).isEqualTo(d2);
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
}
