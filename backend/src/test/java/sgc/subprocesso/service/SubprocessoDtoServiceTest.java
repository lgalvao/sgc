package sgc.subprocesso.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoRepo;
import sgc.unidade.model.Unidade;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
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

    @Test
    @DisplayName("obterDetalhes sucesso admin")
    void obterDetalhesAdmin() {
        Long id = 1L;
        Subprocesso sp = new Subprocesso();
        sp.setCodigo(id);
        sp.setUnidade(new Unidade());
        sp.getUnidade().setSigla("U1");
        sp.setSituacao(sgc.subprocesso.model.SituacaoSubprocesso.NAO_INICIADO);

        Authentication auth = mock(Authentication.class);
        SecurityContext ctx = mock(SecurityContext.class);
        when(ctx.getAuthentication()).thenReturn(auth);
        securityMock.when(SecurityContextHolder::getContext).thenReturn(ctx);
        when(auth.getName()).thenReturn("admin");

        Usuario admin = new Usuario();
        java.util.Set<sgc.sgrh.model.UsuarioPerfil> attrs = new java.util.HashSet<>();
        attrs.add(
                        sgc.sgrh.model.UsuarioPerfil.builder()
                                .usuario(admin)
                                .unidade(new Unidade())
                                .perfil(Perfil.ADMIN)
                                .build());
        admin.setAtribuicoes(attrs);

        when(repositorioSubprocesso.findById(id)).thenReturn(Optional.of(sp));
        when(sgrhService.buscarUsuarioPorLogin("admin")).thenReturn(admin);
        when(subprocessoPermissoesService.calcularPermissoes(any(), any()))
                .thenReturn(SubprocessoPermissoesDto.builder().build());
        when(subprocessoDetalheMapper.toDto(any(), any(), any(), any(), any()))
                .thenReturn(SubprocessoDetalheDto.builder().build());

        var res = service.obterDetalhes(id, Perfil.ADMIN, null);

        assertThat(res).isNotNull();
    }

    @Test
    @DisplayName("obterDetalhes falha se perfil null")
    void obterDetalhesPerfilNull() {
        assertThatThrownBy(() -> service.obterDetalhes(1L, null, null))
                .isInstanceOf(ErroAccessoNegado.class);
    }

    @Test
    @DisplayName("obterDetalhes sucesso servidor mesma unidade")
    void obterDetalhesServidorMesmaUnidade() {
        Long id = 1L;
        Unidade u = new Unidade();
        u.setCodigo(10L);
        u.setSigla("U1");

        Subprocesso sp = new Subprocesso();
        sp.setCodigo(id);
        sp.setUnidade(u);
        sp.setSituacao(sgc.subprocesso.model.SituacaoSubprocesso.NAO_INICIADO);

        Authentication auth = mock(Authentication.class);
        SecurityContext ctx = mock(SecurityContext.class);
        when(ctx.getAuthentication()).thenReturn(auth);
        securityMock.when(SecurityContextHolder::getContext).thenReturn(ctx);
        when(auth.getName()).thenReturn("servidor");

        Usuario servidor = new Usuario();
        servidor.setUnidadeLotacao(u);
        java.util.Set<sgc.sgrh.model.UsuarioPerfil> attrs = new java.util.HashSet<>();
        attrs.add(
                        sgc.sgrh.model.UsuarioPerfil.builder()
                                .usuario(servidor)
                                .unidade(u)
                                .perfil(Perfil.SERVIDOR)
                                .build());
        servidor.setAtribuicoes(attrs);

        when(repositorioSubprocesso.findById(id)).thenReturn(Optional.of(sp));
        when(sgrhService.buscarUsuarioPorLogin("servidor")).thenReturn(servidor);
        when(subprocessoPermissoesService.calcularPermissoes(any(), any()))
                .thenReturn(SubprocessoPermissoesDto.builder().build());
        when(subprocessoDetalheMapper.toDto(any(), any(), any(), any(), any()))
                .thenReturn(SubprocessoDetalheDto.builder().build());

        var res = service.obterDetalhes(id, Perfil.SERVIDOR, 10L);

        assertThat(res).isNotNull();
    }

    @Test
    @DisplayName("obterDetalhes falha servidor unidade diferente")
    void obterDetalhesServidorUnidadeDiferente() {
        Long id = 1L;
        Unidade u1 = new Unidade();
        u1.setCodigo(10L);
        Unidade u2 = new Unidade();
        u2.setCodigo(20L);

        Subprocesso sp = new Subprocesso();
        sp.setCodigo(id);
        sp.setUnidade(u1);

        Authentication auth = mock(Authentication.class);
        SecurityContext ctx = mock(SecurityContext.class);
        when(ctx.getAuthentication()).thenReturn(auth);
        securityMock.when(SecurityContextHolder::getContext).thenReturn(ctx);
        when(auth.getName()).thenReturn("servidor");

        Usuario servidor = new Usuario();
        servidor.setUnidadeLotacao(u2);
        java.util.Set<sgc.sgrh.model.UsuarioPerfil> attrs = new java.util.HashSet<>();
        attrs.add(
                        sgc.sgrh.model.UsuarioPerfil.builder()
                                .usuario(servidor)
                                .unidade(u2)
                                .perfil(Perfil.SERVIDOR)
                                .build());
        servidor.setAtribuicoes(attrs);

        when(repositorioSubprocesso.findById(id)).thenReturn(Optional.of(sp));
        when(sgrhService.buscarUsuarioPorLogin("servidor")).thenReturn(servidor);

        assertThatThrownBy(() -> service.obterDetalhes(id, Perfil.SERVIDOR, null))
                .isInstanceOf(ErroAccessoNegado.class);
    }

    @Test
    @DisplayName("obterDetalhes sucesso gestor unidade correta")
    void obterDetalhesGestor() {
        Long id = 1L;
        Unidade u = new Unidade();
        u.setCodigo(10L);
        u.setSigla("U1");

        Subprocesso sp = new Subprocesso();
        sp.setCodigo(id);
        sp.setUnidade(u);
        sp.setSituacao(sgc.subprocesso.model.SituacaoSubprocesso.NAO_INICIADO);

        Authentication auth = mock(Authentication.class);
        SecurityContext ctx = mock(SecurityContext.class);
        when(ctx.getAuthentication()).thenReturn(auth);
        securityMock.when(SecurityContextHolder::getContext).thenReturn(ctx);
        when(auth.getName()).thenReturn("gestor");

        Usuario gestor = new Usuario();
        gestor.setUnidadeLotacao(u);
        java.util.Set<sgc.sgrh.model.UsuarioPerfil> attrs = new java.util.HashSet<>();
        attrs.add(
                        sgc.sgrh.model.UsuarioPerfil.builder()
                                .usuario(gestor)
                                .unidade(u)
                                .perfil(Perfil.GESTOR)
                                .build());
        gestor.setAtribuicoes(attrs);

        when(repositorioSubprocesso.findById(id)).thenReturn(Optional.of(sp));
        when(sgrhService.buscarUsuarioPorLogin("gestor")).thenReturn(gestor);
        when(subprocessoPermissoesService.calcularPermissoes(any(), any()))
                .thenReturn(SubprocessoPermissoesDto.builder().build());
        when(subprocessoDetalheMapper.toDto(any(), any(), any(), any(), any()))
                .thenReturn(SubprocessoDetalheDto.builder().build());

        var res = service.obterDetalhes(id, Perfil.GESTOR, 10L);

        assertThat(res).isNotNull();
    }

    @Test
    @DisplayName("obterDetalhes falha gestor unidade errada")
    void obterDetalhesGestorErrado() {
        Long id = 1L;
        Unidade u1 = new Unidade();
        u1.setCodigo(10L);
        Unidade u2 = new Unidade();
        u2.setCodigo(20L);

        Subprocesso sp = new Subprocesso();
        sp.setCodigo(id);
        sp.setUnidade(u1);

        Authentication auth = mock(Authentication.class);
        SecurityContext ctx = mock(SecurityContext.class);
        when(ctx.getAuthentication()).thenReturn(auth);
        securityMock.when(SecurityContextHolder::getContext).thenReturn(ctx);
        when(auth.getName()).thenReturn("gestor");

        Usuario gestor = new Usuario();
        gestor.setUnidadeLotacao(u2);
        java.util.Set<sgc.sgrh.model.UsuarioPerfil> attrs = new java.util.HashSet<>();
        attrs.add(
                        sgc.sgrh.model.UsuarioPerfil.builder()
                                .usuario(gestor)
                                .unidade(u2)
                                .perfil(Perfil.GESTOR)
                                .build());
        gestor.setAtribuicoes(attrs);

        when(repositorioSubprocesso.findById(id)).thenReturn(Optional.of(sp));
        when(sgrhService.buscarUsuarioPorLogin("gestor")).thenReturn(gestor);

        assertThatThrownBy(() -> service.obterDetalhes(id, Perfil.GESTOR, 20L))
                .isInstanceOf(ErroAccessoNegado.class);
    }

    @Test
    @DisplayName("obterDetalhes com mapa null e conhecimentos")
    void obterDetalhesMapaNull() {
        Long id = 1L;
        Subprocesso sp = new Subprocesso();
        sp.setCodigo(id);
        sp.setUnidade(new Unidade());
        sp.setMapa(null);
        sp.setSituacao(sgc.subprocesso.model.SituacaoSubprocesso.NAO_INICIADO);

        Authentication auth = mock(Authentication.class);
        SecurityContext ctx = mock(SecurityContext.class);
        when(ctx.getAuthentication()).thenReturn(auth);
        securityMock.when(SecurityContextHolder::getContext).thenReturn(ctx);
        when(auth.getName()).thenReturn("admin");

        Usuario admin = new Usuario();
        java.util.Set<sgc.sgrh.model.UsuarioPerfil> attrs = new java.util.HashSet<>();
        attrs.add(
                        sgc.sgrh.model.UsuarioPerfil.builder()
                                .usuario(admin)
                                .unidade(new Unidade())
                                .perfil(Perfil.ADMIN)
                                .build());
        admin.setAtribuicoes(attrs);

        when(repositorioSubprocesso.findById(id)).thenReturn(Optional.of(sp));
        when(sgrhService.buscarUsuarioPorLogin("admin")).thenReturn(admin);
        when(subprocessoPermissoesService.calcularPermissoes(any(), any()))
                .thenReturn(SubprocessoPermissoesDto.builder().build());
        when(subprocessoDetalheMapper.toDto(any(), any(), any(), any(), any()))
                .thenReturn(SubprocessoDetalheDto.builder().build());

        var res = service.obterDetalhes(id, Perfil.ADMIN, null);

        assertThat(res).isNotNull();
    }

    @Test
    @DisplayName("obterCadastro sucesso")
    void obterCadastro() {
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
    @DisplayName("obterCadastro com mapa null")
    void obterCadastroMapaNull() {
        Long id = 1L;
        Subprocesso sp = new Subprocesso();
        sp.setCodigo(id);
        sp.setMapa(null);

        when(repositorioSubprocesso.findById(id)).thenReturn(Optional.of(sp));

        var res = service.obterCadastro(id);

        assertThat(res.getAtividades()).isEmpty();
    }

    @Test
    @DisplayName("obterSugestoes sucesso")
    void obterSugestoes() {
        Long id = 1L;
        Subprocesso sp = new Subprocesso();
        sp.setMapa(new Mapa());
        sp.getMapa().setSugestoes("sug");

        when(repositorioSubprocesso.findById(id)).thenReturn(Optional.of(sp));

        var res = service.obterSugestoes(id);

        assertThat(res.getSugestoes()).isEqualTo("sug");
    }

    @Test
    @DisplayName("obterSugestoes nao encontrado")
    void obterSugestoesNaoEncontrado() {
        when(repositorioSubprocesso.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.obterSugestoes(1L))
                .isInstanceOf(ErroEntidadeNaoEncontrada.class);
    }

    @Test
    @DisplayName("obterMapaParaAjuste sucesso")
    void obterMapaParaAjuste() {
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
    @DisplayName("obterMapaParaAjuste falha sem mapa")
    void obterMapaParaAjusteSemMapa() {
        Long id = 1L;
        Subprocesso sp = new Subprocesso();
        sp.setMapa(null);

        when(repositorioSubprocesso.findById(id)).thenReturn(Optional.of(sp));

        assertThatThrownBy(() -> service.obterMapaParaAjuste(id))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("listar e obterPorProcessoEUnidade")
    void outrosMetodos() {
        when(repositorioSubprocesso.findAll()).thenReturn(List.of(new Subprocesso()));
        when(subprocessoMapper.toDTO(any())).thenReturn(SubprocessoDto.builder().build());

        assertThat(service.listar()).hasSize(1);

        when(repositorioSubprocesso.findByProcessoCodigoAndUnidadeCodigo(1L, 1L))
                .thenReturn(Optional.of(new Subprocesso()));
        assertThat(service.obterPorProcessoEUnidade(1L, 1L)).isNotNull();
    }

    @Test
    @DisplayName("obterPorProcessoEUnidade falha se nao encontrado")
    void obterPorProcessoEUnidadeNaoEncontrado() {
        when(repositorioSubprocesso.findByProcessoCodigoAndUnidadeCodigo(1L, 1L))
                .thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.obterPorProcessoEUnidade(1L, 1L))
                .isInstanceOf(ErroEntidadeNaoEncontrada.class);
    }
}
