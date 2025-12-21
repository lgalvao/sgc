package sgc.subprocesso.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.HashSet;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import sgc.analise.AnaliseService;
import sgc.analise.model.TipoAnalise;
import sgc.atividade.dto.AtividadeMapper;
import sgc.atividade.dto.ConhecimentoDto;
import sgc.atividade.dto.ConhecimentoMapper;
import sgc.atividade.model.Atividade;
import sgc.atividade.model.AtividadeRepo;
import sgc.atividade.model.Conhecimento;
import sgc.atividade.model.ConhecimentoRepo;
import sgc.comum.erros.ErroAccessoNegado;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.mapa.model.CompetenciaRepo;
import sgc.mapa.model.Mapa;
import sgc.sgrh.model.Perfil;
import sgc.sgrh.model.Usuario;
import sgc.sgrh.model.UsuarioPerfil;
import sgc.sgrh.SgrhService;
import sgc.subprocesso.dto.MapaAjusteDto;
import sgc.subprocesso.dto.SubprocessoDetalheDto;
import sgc.subprocesso.dto.SubprocessoDto;
import sgc.subprocesso.dto.SubprocessoPermissoesDto;
import sgc.subprocesso.mapper.MapaAjusteMapper;
import sgc.subprocesso.mapper.MovimentacaoMapper;
import sgc.subprocesso.mapper.SubprocessoDetalheMapper;
import sgc.subprocesso.mapper.SubprocessoMapper;
import sgc.subprocesso.model.MovimentacaoRepo;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoRepo;
import sgc.unidade.model.Unidade;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários para SubprocessoDtoService")
class SubprocessoDtoServiceTest {

    @Mock
    private SubprocessoRepo repositorioSubprocesso;
    @Mock
    private MovimentacaoRepo repositorioMovimentacao;
    @Mock
    private AtividadeRepo atividadeRepo;
    @Mock
    private ConhecimentoRepo repositorioConhecimento;
    @Mock
    private CompetenciaRepo competenciaRepo;
    @Mock
    private AnaliseService analiseService;
    @Mock
    private AtividadeMapper atividadeMapper;
    @Mock
    private ConhecimentoMapper conhecimentoMapper;
    @Mock
    private MovimentacaoMapper movimentacaoMapper;
    @Mock
    private SubprocessoMapper subprocessoMapper;
    @Mock
    private SubprocessoPermissoesService subprocessoPermissoesService;
    @Mock
    private SgrhService sgrhService;
    @Mock
    private SubprocessoDetalheMapper subprocessoDetalheMapper;
    @Mock
    private MapaAjusteMapper mapaAjusteMapper;

    @InjectMocks
    private SubprocessoDtoService service;

    private MockedStatic<SecurityContextHolder> securityMock;

    @BeforeEach
    void setUp() {
        securityMock = mockStatic(SecurityContextHolder.class);
    }

    @AfterEach
    void tearDown() {
        securityMock.close();
    }

    @Nested
    @DisplayName("Cenários de Obter Detalhes")
    class ObterDetalhesTests {

        @Test
        @DisplayName("Deve obter detalhes com sucesso para ADMIN")
        void deveObterDetalhesComSucessoParaAdmin() {
            Long id = 1L;
            Subprocesso sp = new Subprocesso();
            sp.setCodigo(id);
            sp.setUnidade(new Unidade());
            sp.getUnidade().setSigla("U1");
            sp.setSituacao(SituacaoSubprocesso.NAO_INICIADO);

            configurarMockSecurity("admin");

            Usuario admin = criarUsuario("admin", Perfil.ADMIN, new Unidade());

            when(repositorioSubprocesso.findById(id)).thenReturn(Optional.of(sp));
            when(sgrhService.buscarUsuarioPorLogin("admin")).thenReturn(admin);
            when(subprocessoPermissoesService.calcularPermissoes(any(), any()))
                    .thenReturn(SubprocessoPermissoesDto.builder().build());
            when(subprocessoDetalheMapper.toDto(any(), any(), any(), any(), any()))
                    .thenReturn(SubprocessoDetalheDto.builder().build());

            SubprocessoDetalheDto res = service.obterDetalhes(id, Perfil.ADMIN, null);

            assertThat(res).isNotNull();
        }

        @Test
        @DisplayName("Deve lançar exceção se perfil for nulo")
        void deveLancarExcecaoSePerfilNull() {
            assertThatThrownBy(() -> service.obterDetalhes(1L, null, null))
                    .isInstanceOf(ErroAccessoNegado.class);
        }

        @Test
        @DisplayName("Deve obter detalhes com sucesso para SERVIDOR na mesma unidade")
        void deveObterDetalhesComSucessoParaServidorMesmaUnidade() {
            Long id = 1L;
            Unidade u = new Unidade();
            u.setCodigo(10L);
            u.setSigla("U1");

            Subprocesso sp = new Subprocesso();
            sp.setCodigo(id);
            sp.setUnidade(u);
            sp.setSituacao(SituacaoSubprocesso.NAO_INICIADO);

            configurarMockSecurity("servidor");

            Usuario servidor = criarUsuario("servidor", Perfil.SERVIDOR, u);
            servidor.setUnidadeLotacao(u);

            when(repositorioSubprocesso.findById(id)).thenReturn(Optional.of(sp));
            when(sgrhService.buscarUsuarioPorLogin("servidor")).thenReturn(servidor);
            when(subprocessoPermissoesService.calcularPermissoes(any(), any()))
                    .thenReturn(SubprocessoPermissoesDto.builder().build());
            when(subprocessoDetalheMapper.toDto(any(), any(), any(), any(), any()))
                    .thenReturn(SubprocessoDetalheDto.builder().build());

            SubprocessoDetalheDto res = service.obterDetalhes(id, Perfil.SERVIDOR, 10L);

            assertThat(res).isNotNull();
        }

        @Test
        @DisplayName("Deve lançar exceção para SERVIDOR em unidade diferente")
        void deveLancarExcecaoParaServidorUnidadeDiferente() {
            Long id = 1L;
            Unidade u1 = new Unidade();
            u1.setCodigo(10L);
            Unidade u2 = new Unidade();
            u2.setCodigo(20L);

            Subprocesso sp = new Subprocesso();
            sp.setCodigo(id);
            sp.setUnidade(u1);

            configurarMockSecurity("servidor");

            Usuario servidor = criarUsuario("servidor", Perfil.SERVIDOR, u2);
            servidor.setUnidadeLotacao(u2);

            when(repositorioSubprocesso.findById(id)).thenReturn(Optional.of(sp));
            when(sgrhService.buscarUsuarioPorLogin("servidor")).thenReturn(servidor);

            assertThatThrownBy(() -> service.obterDetalhes(id, Perfil.SERVIDOR, null))
                    .isInstanceOf(ErroAccessoNegado.class);
        }

        @Test
        @DisplayName("Deve obter detalhes com sucesso para GESTOR na unidade correta")
        void deveObterDetalhesComSucessoParaGestorUnidadeCorreta() {
            Long id = 1L;
            Unidade u = new Unidade();
            u.setCodigo(10L);
            u.setSigla("U1");

            Subprocesso sp = new Subprocesso();
            sp.setCodigo(id);
            sp.setUnidade(u);
            sp.setSituacao(SituacaoSubprocesso.NAO_INICIADO);

            configurarMockSecurity("gestor");

            Usuario gestor = criarUsuario("gestor", Perfil.GESTOR, u);
            gestor.setUnidadeLotacao(u);

            when(repositorioSubprocesso.findById(id)).thenReturn(Optional.of(sp));
            when(sgrhService.buscarUsuarioPorLogin("gestor")).thenReturn(gestor);
            when(subprocessoPermissoesService.calcularPermissoes(any(), any()))
                    .thenReturn(SubprocessoPermissoesDto.builder().build());
            when(subprocessoDetalheMapper.toDto(any(), any(), any(), any(), any()))
                    .thenReturn(SubprocessoDetalheDto.builder().build());

            SubprocessoDetalheDto res = service.obterDetalhes(id, Perfil.GESTOR, 10L);

            assertThat(res).isNotNull();
        }

        @Test
        @DisplayName("Deve lançar exceção para GESTOR em unidade errada")
        void deveLancarExcecaoParaGestorUnidadeErrada() {
            Long id = 1L;
            Unidade u1 = new Unidade();
            u1.setCodigo(10L);
            Unidade u2 = new Unidade();
            u2.setCodigo(20L);

            Subprocesso sp = new Subprocesso();
            sp.setCodigo(id);
            sp.setUnidade(u1);

            configurarMockSecurity("gestor");

            Usuario gestor = criarUsuario("gestor", Perfil.GESTOR, u2);
            gestor.setUnidadeLotacao(u2);

            when(repositorioSubprocesso.findById(id)).thenReturn(Optional.of(sp));
            when(sgrhService.buscarUsuarioPorLogin("gestor")).thenReturn(gestor);

            assertThatThrownBy(() -> service.obterDetalhes(id, Perfil.GESTOR, 20L))
                    .isInstanceOf(ErroAccessoNegado.class);
        }

        @Test
        @DisplayName("Deve obter detalhes mesmo se mapa for nulo")
        void deveObterDetalhesSeMapaForNulo() {
            Long id = 1L;
            Subprocesso sp = new Subprocesso();
            sp.setCodigo(id);
            sp.setUnidade(new Unidade());
            sp.setMapa(null);
            sp.setSituacao(SituacaoSubprocesso.NAO_INICIADO);

            configurarMockSecurity("admin");

            Usuario admin = criarUsuario("admin", Perfil.ADMIN, new Unidade());

            when(repositorioSubprocesso.findById(id)).thenReturn(Optional.of(sp));
            when(sgrhService.buscarUsuarioPorLogin("admin")).thenReturn(admin);
            when(subprocessoPermissoesService.calcularPermissoes(any(), any()))
                    .thenReturn(SubprocessoPermissoesDto.builder().build());
            when(subprocessoDetalheMapper.toDto(any(), any(), any(), any(), any()))
                    .thenReturn(SubprocessoDetalheDto.builder().build());

            SubprocessoDetalheDto res = service.obterDetalhes(id, Perfil.ADMIN, null);

            assertThat(res).isNotNull();
        }
    }

    @Nested
    @DisplayName("Cenários de Obter Cadastro")
    class ObterCadastroTests {
        @Test
        @DisplayName("Deve obter cadastro com sucesso")
        void deveObterCadastroComSucesso() {
            Long id = 1L;
            Subprocesso sp = new Subprocesso();
            sp.setCodigo(id);
            sp.setMapa(new Mapa());
            sp.getMapa().setCodigo(100L);

            Atividade ativ = new Atividade();
            ativ.setCodigo(10L);
            ativ.setConhecimentos(List.of(new Conhecimento()));

            when(repositorioSubprocesso.findById(id)).thenReturn(Optional.of(sp));
            when(atividadeRepo.findByMapaCodigoWithConhecimentos(100L)).thenReturn(List.of(ativ));
            when(conhecimentoMapper.toDto(any())).thenReturn(new ConhecimentoDto());

            var res = service.obterCadastro(id);

            assertThat(res.getAtividades()).hasSize(1);
        }

        @Test
        @DisplayName("Deve retornar vazio se mapa for nulo")
        void deveRetornarVazioSeMapaNulo() {
            Long id = 1L;
            Subprocesso sp = new Subprocesso();
            sp.setCodigo(id);
            sp.setMapa(null);

            when(repositorioSubprocesso.findById(id)).thenReturn(Optional.of(sp));

            var res = service.obterCadastro(id);

            assertThat(res.getAtividades()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Cenários de Sugestões")
    class SugestoesTests {
        @Test
        @DisplayName("Deve obter sugestões com sucesso")
        void deveObterSugestoesComSucesso() {
            Long id = 1L;
            Subprocesso sp = new Subprocesso();
            sp.setMapa(new Mapa());
            sp.getMapa().setSugestoes("sug");

            when(repositorioSubprocesso.findById(id)).thenReturn(Optional.of(sp));

            var res = service.obterSugestoes(id);

            assertThat(res.getSugestoes()).isEqualTo("sug");
        }

        @Test
        @DisplayName("Deve lançar exceção se subprocesso não encontrado para sugestões")
        void deveLancarExcecaoSeSubprocessoNaoEncontradoParaSugestoes() {
            when(repositorioSubprocesso.findById(1L)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> service.obterSugestoes(1L))
                    .isInstanceOf(ErroEntidadeNaoEncontrada.class);
        }
    }

    @Nested
    @DisplayName("Cenários de Mapa Ajuste")
    class MapaAjusteTests {
        @Test
        @DisplayName("Deve obter mapa para ajuste com sucesso")
        void deveObterMapaParaAjusteComSucesso() {
            Long id = 1L;
            Subprocesso sp = new Subprocesso();
            sp.setMapa(new Mapa());
            sp.getMapa().setCodigo(100L);

            when(repositorioSubprocesso.findById(id)).thenReturn(Optional.of(sp));
            when(analiseService.listarPorSubprocesso(id, TipoAnalise.VALIDACAO))
                    .thenReturn(Collections.emptyList());
            when(mapaAjusteMapper.toDto(any(), any(), any(), any(), any()))
                    .thenReturn(MapaAjusteDto.builder().build());

            var res = service.obterMapaParaAjuste(id);

            assertThat(res).isNotNull();
        }

        @Test
        @DisplayName("Deve lançar exceção se mapa for nulo ao obter para ajuste")
        void deveLancarExcecaoSeMapaNuloAoObterParaAjuste() {
            Long id = 1L;
            Subprocesso sp = new Subprocesso();
            sp.setMapa(null);

            when(repositorioSubprocesso.findById(id)).thenReturn(Optional.of(sp));

            assertThatThrownBy(() -> service.obterMapaParaAjuste(id))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("Cenários de Listagem e Busca")
    class ListagemBuscaTests {
        @Test
        @DisplayName("Deve listar todos os subprocessos")
        void deveListarTodosOsSubprocessos() {
            when(repositorioSubprocesso.findAll()).thenReturn(List.of(new Subprocesso()));
            when(subprocessoMapper.toDTO(any())).thenReturn(SubprocessoDto.builder().build());

            assertThat(service.listar()).hasSize(1);
        }

        @Test
        @DisplayName("Deve obter subprocesso por processo e unidade")
        void deveObterSubprocessoPorProcessoEUnidade() {
            when(repositorioSubprocesso.findByProcessoCodigoAndUnidadeCodigo(1L, 1L))
                    .thenReturn(Optional.of(new Subprocesso()));
            when(subprocessoMapper.toDTO(any())).thenReturn(SubprocessoDto.builder().build());

            assertThat(service.obterPorProcessoEUnidade(1L, 1L)).isNotNull();
        }

        @Test
        @DisplayName("Deve lançar exceção se não encontrar subprocesso por processo e unidade")
        void deveLancarExcecaoSeNaoEncontrarSubprocessoPorProcessoEUnidade() {
            when(repositorioSubprocesso.findByProcessoCodigoAndUnidadeCodigo(1L, 1L))
                    .thenReturn(Optional.empty());
            assertThatThrownBy(() -> service.obterPorProcessoEUnidade(1L, 1L))
                    .isInstanceOf(ErroEntidadeNaoEncontrada.class);
        }
    }

    // Helpers
    private void configurarMockSecurity(String username) {
        Authentication auth = mock(Authentication.class);
        SecurityContext ctx = mock(SecurityContext.class);
        when(ctx.getAuthentication()).thenReturn(auth);
        securityMock.when(SecurityContextHolder::getContext).thenReturn(ctx);
        when(auth.getName()).thenReturn(username);
    }

    private Usuario criarUsuario(String username, Perfil perfil, Unidade unidade) {
        Usuario usuario = new Usuario();
        Set<UsuarioPerfil> perfis = new HashSet<>();
        perfis.add(UsuarioPerfil.builder()
                .usuario(usuario)
                .unidade(unidade)
                .perfil(perfil)
                .build());
        usuario.setAtribuicoes(perfis);
        return usuario;
    }
}
