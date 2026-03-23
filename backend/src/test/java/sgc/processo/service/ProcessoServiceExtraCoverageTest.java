package sgc.processo.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import org.mockito.quality.*;
import sgc.alerta.*;
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
import sgc.mapa.model.*;
import org.springframework.security.core.Authentication;

import java.time.*;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("ProcessoService Extra Coverage Test")
class ProcessoServiceExtraCoverageTest {

    @InjectMocks
    private ProcessoService processoService;

    @Mock
    private ProcessoRepo processoRepo;

    @Mock
    private ComumRepo repo;

    @Mock
    private UnidadeService unidadeService;

    @Mock
    private SubprocessoService subprocessoService;

    @Mock
    private SubprocessoValidacaoService validacaoService;

    @Mock
    private UsuarioFacade usuarioService;

    @Mock
    private AlertaFacade servicoAlertas;

    @Mock
    private EmailService emailService;

    @Mock
    private EmailModelosService emailModelosService;

    @Mock
    private SgcPermissionEvaluator permissionEvaluator;

    @Mock
    private SubprocessoTransicaoService transicaoService;

    @Nested
    @DisplayName("buscarPorCodigoComParticipantes")
    class BuscarPorCodigoComParticipantes {
        @Test
        @DisplayName("deve lancar excecao se nao encontrar")
        void deveLancarExcecao() {
            when(processoRepo.buscarPorCodigoComParticipantes(1L)).thenReturn(Optional.empty());

            assertThrows(sgc.comum.erros.ErroEntidadeNaoEncontrada.class, () -> processoService.buscarPorCodigoComParticipantes(1L));
        }

        @Test
        @DisplayName("deve retornar se encontrar")
        void deveRetornar() {
            Processo p = new Processo();
            when(processoRepo.buscarPorCodigoComParticipantes(1L)).thenReturn(Optional.of(p));

            Processo res = processoService.buscarPorCodigoComParticipantes(1L);

            assertThat(res).isEqualTo(p);
        }
    }

    @Nested
    @DisplayName("listarFinalizados")
    class ListarFinalizados {
        @Test
        @DisplayName("deve retornar listarPorSituacaoComParticipantes se admin")
        void admin() {
            Usuario u = new Usuario();
            u.setPerfilAtivo(Perfil.ADMIN);
            when(usuarioService.usuarioAutenticado()).thenReturn(u);

            Processo p = new Processo();
            when(processoRepo.listarPorSituacaoComParticipantes(SituacaoProcesso.FINALIZADO)).thenReturn(List.of(p));

            List<Processo> res = processoService.listarFinalizados();

            assertThat(res).containsExactly(p);
        }

        @Test
        @DisplayName("deve retornar listarPorSituacaoEUnidadeCodigos se nao admin")
        void naoAdmin() {
            Usuario u = new Usuario();
            u.setPerfilAtivo(Perfil.GESTOR);
            u.setUnidadeAtivaCodigo(1L);
            when(usuarioService.usuarioAutenticado()).thenReturn(u);

            Unidade uni = new Unidade();
            uni.setCodigo(1L);
            when(unidadeService.todasComHierarquia()).thenReturn(List.of(uni));

            Processo p = new Processo();
            when(processoRepo.listarPorSituacaoEUnidadeCodigos(eq(SituacaoProcesso.FINALIZADO), anyList())).thenReturn(List.of(p));

            List<Processo> res = processoService.listarFinalizados();

            assertThat(res).containsExactly(p);
        }
    }

    @Nested
    @DisplayName("atualizar")
    class Atualizar {
        @Test
        @DisplayName("deve lancar erro se nao estiver em criacao")
        void situacaoInvalida() {
            Processo p = new Processo();
            p.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
            when(repo.buscar(Processo.class, 1L)).thenReturn(p);

            AtualizarProcessoRequest req = new AtualizarProcessoRequest(1L, "desc", TipoProcesso.MAPEAMENTO, null, List.<Long>of());

            assertThrows(ErroValidacao.class, () -> processoService.atualizar(1L, req));
        }

        @Test
        @DisplayName("deve lancar erro se unidade invalida")
        void unidadeInvalida() {
            Processo p = new Processo();
            p.setSituacao(SituacaoProcesso.CRIADO);
            when(repo.buscar(Processo.class, 1L)).thenReturn(p);

            Unidade u = new Unidade();
            u.setTipo(TipoUnidade.INTERMEDIARIA);
            when(unidadeService.buscarPorCodigo(1L)).thenReturn(u);

            AtualizarProcessoRequest req = new AtualizarProcessoRequest(1L, "desc", TipoProcesso.MAPEAMENTO, null, List.of(1L));

            assertThrows(ErroValidacao.class, () -> processoService.atualizar(1L, req));
        }

        @Test
        @DisplayName("deve atualizar com sucesso")
        void sucesso() {
            Processo p = new Processo();
            p.setSituacao(SituacaoProcesso.CRIADO);
            when(repo.buscar(Processo.class, 1L)).thenReturn(p);

            Unidade u = new Unidade();
            u.setTipo(TipoUnidade.RAIZ);
            u.setSituacao(sgc.organizacao.model.SituacaoUnidade.ATIVA);
            when(unidadeService.buscarPorCodigo(1L)).thenReturn(u);

            when(processoRepo.saveAndFlush(p)).thenReturn(p);

            AtualizarProcessoRequest req = new AtualizarProcessoRequest(1L, "desc2", TipoProcesso.MAPEAMENTO, null, List.of(1L));

            Processo res = processoService.atualizar(1L, req);

            assertThat(res.getDescricao()).isEqualTo("desc2");
        }
    }

    @Nested
    @DisplayName("apagar")
    class Apagar {
        @Test
        @DisplayName("deve lancar erro se nao estiver em criacao")
        void situacaoInvalida() {
            Processo p = new Processo();
            p.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
            when(repo.buscar(Processo.class, 1L)).thenReturn(p);

            assertThrows(ErroValidacao.class, () -> processoService.apagar(1L));
        }

        @Test
        @DisplayName("deve apagar com sucesso")
        void sucesso() {
            Processo p = new Processo();
            p.setSituacao(SituacaoProcesso.CRIADO);
            when(repo.buscar(Processo.class, 1L)).thenReturn(p);

            processoService.apagar(1L);

            verify(processoRepo).deleteById(1L);
        }
    }

    @Nested
    @DisplayName("iniciar")
    class Iniciar {
        @Test
        @DisplayName("deve lancar erro se nao estiver em criacao")
        void situacaoInvalida() {
            Processo p = new Processo();
            p.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
            when(repo.buscar(Processo.class, 1L)).thenReturn(p);

            assertThrows(ErroValidacao.class, () -> processoService.iniciar(1L, List.of(), new Usuario()));
        }

        @Test
        @DisplayName("deve lancar erro se revisao sem unidades")
        void revisaoSemUnidades() {
            Processo p = new Processo();
            p.setSituacao(SituacaoProcesso.CRIADO);
            p.setTipo(TipoProcesso.REVISAO);
            when(repo.buscar(Processo.class, 1L)).thenReturn(p);

            assertThrows(ErroValidacao.class, () -> processoService.iniciar(1L, List.of(), new Usuario()));
        }

        @Test
        @DisplayName("deve lancar erro se mapeamento sem participantes")
        void mapeamentoSemParticipantes() {
            Processo p = new Processo();
            p.setSituacao(SituacaoProcesso.CRIADO);
            p.setTipo(TipoProcesso.MAPEAMENTO);
            when(repo.buscar(Processo.class, 1L)).thenReturn(p);

            assertThrows(ErroValidacao.class, () -> processoService.iniciar(1L, List.of(), new Usuario()));
        }
    }

    @Nested
    @DisplayName("finalizar")
    class Finalizar {
        @Test
        @DisplayName("deve lancar erro se nao em andamento")
        void naoAndamento() {
            Processo p = new Processo();
            p.setSituacao(SituacaoProcesso.CRIADO);
            when(repo.buscar(Processo.class, 1L)).thenReturn(p);

            assertThrows(ErroValidacao.class, () -> processoService.finalizar(1L));
        }

        @Test
        @DisplayName("deve lancar erro se nao validado")
        void naoValidado() {
            Processo p = new Processo();
            p.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
            when(repo.buscar(Processo.class, 1L)).thenReturn(p);

            when(validacaoService.validarSubprocessosParaFinalizacao(any())).thenReturn(sgc.subprocesso.service.SubprocessoValidacaoService.ValidationResult.ofInvalido("Erro de validacao"));

            assertThrows(ErroValidacao.class, () -> processoService.finalizar(1L));
        }
    }

    @Nested
    @DisplayName("enviarLembrete")
    class EnviarLembrete {
        @Test
        @DisplayName("deve lancar erro se unidade nao participa")
        void unidadeNaoParticipa() {
            Processo p = new Processo();
            p.setParticipantes(new ArrayList<>());
            when(processoRepo.buscarPorCodigoComParticipantes(1L)).thenReturn(Optional.of(p));
            when(unidadeService.buscarPorCodigo(2L)).thenReturn(new Unidade());

            assertThrows(ErroValidacao.class, () -> processoService.enviarLembrete(1L, 2L));
        }

        @Test
        @DisplayName("deve enviar lembrete com sucesso")
        void sucesso() {
            Processo p = new Processo();
            p.setDescricao("Processo Teste");
            p.setDataLimite(LocalDateTime.of(2026, 3, 22, 12, 0));
            UnidadeProcesso up = new UnidadeProcesso();
            up.setUnidadeCodigo(1L);
            p.setParticipantes(new ArrayList<>(List.of(up)));
            when(processoRepo.buscarPorCodigoComParticipantes(1L)).thenReturn(Optional.of(p));

            Unidade u = new Unidade();
            u.setCodigo(1L);
            u.setSigla("UNI1");
            u.setTituloTitular("TITULAR");
            when(unidadeService.buscarPorCodigo(1L)).thenReturn(u);

            when(emailModelosService.criarEmailLembretePrazo(anyString(), anyString(), any())).thenReturn("html");
            Usuario titular = new Usuario();
            titular.setEmail("titular@teste.com");
            when(usuarioService.buscarPorLogin("TITULAR")).thenReturn(titular);

            processoService.enviarLembrete(1L, 1L);

            verify(emailService).enviarEmailHtml(eq("titular@teste.com"), anyString(), eq("html"));
            verify(servicoAlertas).criarAlertaAdmin(eq(p), eq(u), anyString());
        }
    }

    @Nested
    @DisplayName("listarSubprocessosElegiveis")
    class ListarSubprocessosElegiveis {
        @Test
        @DisplayName("deve listar para nao-admin")
        void naoAdmin() {
            Usuario u = new Usuario();
            u.setPerfilAtivo(Perfil.GESTOR);
            u.setUnidadeAtivaCodigo(1L);
            when(usuarioService.usuarioAutenticado()).thenReturn(u);

            Unidade uni = new Unidade();
            uni.setCodigo(1L);
            when(unidadeService.todasComHierarquia()).thenReturn(List.of(uni));

            Subprocesso sp = new Subprocesso();
            sp.setCodigo(100L);
            sp.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO);
            sp.setUnidade(uni);
            when(subprocessoService.listarEntidadesPorProcessoEUnidades(eq(1L), anyList())).thenReturn(List.of(sp));
            when(permissionEvaluator.verificarPermissao(eq(u), eq(sp), eq(sgc.seguranca.AcaoPermissao.DISPONIBILIZAR_MAPA))).thenReturn(true);

            List<SubprocessoElegivelDto> res = processoService.listarSubprocessosElegiveis(1L);

            assertThat(res).hasSize(1);
            assertThat(res.get(0).getCodigo()).isEqualTo(100L);
        }
    }

    @Nested
    @DisplayName("checarAcesso")
    class ChecarAcesso {
        @Test
        @DisplayName("deve retornar falso se nao autenticado")
        void naoAutenticado() {
            assertThat(processoService.checarAcesso(null, 1L)).isFalse();
            
            Authentication auth = mock(Authentication.class);
            when(auth.isAuthenticated()).thenReturn(false);
            assertThat(processoService.checarAcesso(auth, 1L)).isFalse();
        }

        @Test
        @DisplayName("deve retornar verdadeiro se admin")
        void admin() {
            Usuario u = new Usuario();
            u.setPerfilAtivo(Perfil.ADMIN);
            Authentication auth = mock(Authentication.class);
            when(auth.isAuthenticated()).thenReturn(true);
            when(auth.getPrincipal()).thenReturn(u);

            assertThat(processoService.checarAcesso(auth, 1L)).isTrue();
        }

        @Test
        @DisplayName("deve retornar verdadeiro se participante")
        void participante() {
            Usuario u = new Usuario();
            u.setPerfilAtivo(Perfil.GESTOR);
            u.setUnidadeAtivaCodigo(1L);
            Authentication auth = mock(Authentication.class);
            when(auth.isAuthenticated()).thenReturn(true);
            when(auth.getPrincipal()).thenReturn(u);

            Unidade uni = new Unidade();
            uni.setCodigo(1L);
            when(unidadeService.todasComHierarquia()).thenReturn(List.of(uni));

            Processo p = new Processo();
            UnidadeProcesso up = new UnidadeProcesso();
            up.setUnidadeCodigo(1L);
            p.setParticipantes(new ArrayList<>(List.of(up)));
            when(processoRepo.buscarPorCodigoComParticipantes(1L)).thenReturn(Optional.of(p));

            assertThat(processoService.checarAcesso(auth, 1L)).isTrue();
        }

        @Test
        @DisplayName("deve retornar falso se nao participante")
        void naoParticipante() {
            Usuario u = new Usuario();
            u.setPerfilAtivo(Perfil.GESTOR);
            u.setUnidadeAtivaCodigo(1L);
            Authentication auth = mock(Authentication.class);
            when(auth.isAuthenticated()).thenReturn(true);
            when(auth.getPrincipal()).thenReturn(u);

            Unidade uni = new Unidade();
            uni.setCodigo(1L);
            when(unidadeService.todasComHierarquia()).thenReturn(List.of(uni));

            Processo p = new Processo();
            UnidadeProcesso up = new UnidadeProcesso();
            up.setUnidadeCodigo(2L);
            p.setParticipantes(new ArrayList<>(List.of(up)));
            when(processoRepo.buscarPorCodigoComParticipantes(1L)).thenReturn(Optional.of(p));

            assertThat(processoService.checarAcesso(auth, 1L)).isFalse();
        }
    }

    @Nested
    @DisplayName("finalizar")
    class FinalizarMaisGaps {
        @Test
        @DisplayName("deve tornar mapas vigentes se nao for diagnostico")
        void naoDiagnostico() {
            Processo p = new Processo();
            p.setCodigo(1L);
            p.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
            p.setTipo(TipoProcesso.MAPEAMENTO);
            p.setParticipantes(new ArrayList<>());
            when(repo.buscar(Processo.class, 1L)).thenReturn(p);

            when(validacaoService.validarSubprocessosParaFinalizacao(1L)).thenReturn(sgc.subprocesso.service.SubprocessoValidacaoService.ValidationResult.ofValido());

            Subprocesso sp = new Subprocesso();
            Unidade uni = new Unidade();
            uni.setCodigo(10L);
            sp.setUnidade(uni);
            Mapa mapa = new Mapa();
            sp.setMapa(mapa);
            when(subprocessoService.listarEntidadesPorProcesso(1L)).thenReturn(List.of(sp));

            processoService.finalizar(1L);

            verify(unidadeService).definirMapaVigente(10L, mapa);
        }
    }

    @Nested
    @DisplayName("iniciar")
    class IniciarMaisGaps {
        @Test
        @DisplayName("deve iniciar diagnostico")
        void diagnostico() {
            Processo p = new Processo();
            p.setCodigo(1L);
            p.setSituacao(SituacaoProcesso.CRIADO);
            p.setTipo(TipoProcesso.DIAGNOSTICO);
            
            UnidadeProcesso up = new UnidadeProcesso();
            up.setUnidadeCodigo(10L);
            p.setParticipantes(new ArrayList<>(List.of(up)));
            when(repo.buscar(Processo.class, 1L)).thenReturn(p);

            Unidade uni = new Unidade();
            uni.setCodigo(10L);
            uni.setSigla("UNI10");
            when(unidadeService.porCodigos(anyList())).thenReturn(List.of(uni));
            when(unidadeService.buscarPorCodigo(10L)).thenReturn(uni);
            when(unidadeService.verificarMapaVigente(10L)).thenReturn(true);

            Unidade admin = new Unidade();
            when(repo.buscarPorSigla(Unidade.class, "ADMIN")).thenReturn(admin);

            processoService.iniciar(1L, List.of(), new Usuario());

            verify(subprocessoService).criarParaDiagnostico(eq(p), eq(uni), any(), eq(admin), any());
        }
    }

    @Nested
    @DisplayName("isElegivelParaAcaoEmBloco")
    class IsElegivelParaAcaoEmBloco {
        @Test
        @DisplayName("deve ser elegivel para mapa")
        void elegivelMapa() {
            Usuario u = new Usuario();
            Subprocesso sp = new Subprocesso();
            sp.setSituacao(SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO);
            
            when(permissionEvaluator.verificarPermissao(u, sp, sgc.seguranca.AcaoPermissao.ACEITAR_MAPA)).thenReturn(true);

            // Chamando via listarSubprocessosElegiveis para testar o metodo privado
            u.setPerfilAtivo(Perfil.ADMIN);
            when(usuarioService.usuarioAutenticado()).thenReturn(u);
            when(subprocessoService.listarEntidadesPorProcesso(1L)).thenReturn(List.of(sp));
            Unidade uni = new Unidade();
            uni.setCodigo(1L);
            sp.setUnidade(uni);

            List<SubprocessoElegivelDto> res = processoService.listarSubprocessosElegiveis(1L);
            assertThat(res).hasSize(1);
        }

        @Test
        @DisplayName("nao deve ser elegivel se situacao nao permite")
        void naoElegivel() {
            Usuario u = new Usuario();
            Subprocesso sp = new Subprocesso();
            sp.setSituacao(SituacaoSubprocesso.DIAGNOSTICO_CONCLUIDO);
            
            u.setPerfilAtivo(Perfil.ADMIN);
            when(usuarioService.usuarioAutenticado()).thenReturn(u);
            when(subprocessoService.listarEntidadesPorProcesso(1L)).thenReturn(List.of(sp));

            List<SubprocessoElegivelDto> res = processoService.listarSubprocessosElegiveis(1L);
            assertThat(res).isEmpty();
        }
    }
}
