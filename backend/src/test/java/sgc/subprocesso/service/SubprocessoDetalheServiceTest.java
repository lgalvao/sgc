package sgc.subprocesso.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.analise.AnaliseFacade;
import sgc.comum.erros.ErroAccessoNegado;
import sgc.mapa.dto.ConhecimentoDto;
import sgc.mapa.mapper.ConhecimentoMapper;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.Conhecimento;
import sgc.mapa.model.Mapa;
import sgc.mapa.service.AtividadeService;
import sgc.mapa.service.CompetenciaService;
import sgc.mapa.service.ConhecimentoService;
import sgc.organizacao.UsuarioFacade;
import sgc.organizacao.model.Perfil;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.organizacao.model.UsuarioPerfil;
import sgc.seguranca.acesso.Acao;
import sgc.seguranca.acesso.AccessControlService;
import sgc.subprocesso.dto.*;
import sgc.subprocesso.mapper.MapaAjusteMapper;
import sgc.subprocesso.mapper.SubprocessoDetalheMapper;
import sgc.subprocesso.model.MovimentacaoRepo;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.crud.SubprocessoCrudService;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("Testes para SubprocessoDetalheService")
class SubprocessoDetalheServiceTest {

    @Mock
    private SubprocessoCrudService crudService;
    @Mock
    private AtividadeService atividadeService;
    @Mock
    private UsuarioFacade usuarioService;
    @Mock
    private MovimentacaoRepo repositorioMovimentacao;
    @Mock
    private AccessControlService accessControlService;
    @Mock
    private SubprocessoDetalheMapper subprocessoDetalheMapper;
    @Mock
    private ConhecimentoMapper conhecimentoMapper;
    @Mock
    private AnaliseFacade analiseFacade;
    @Mock
    private CompetenciaService competenciaService;
    @Mock
    private MapaAjusteMapper mapaAjusteMapper;
    @Mock
    private ConhecimentoService conhecimentoService;

    @InjectMocks
    private SubprocessoDetalheService service;

    @Test
    @DisplayName("Deve listar atividades")
    void deveListarAtividades() {
        Subprocesso sp = new Subprocesso();
        Mapa mapa = new Mapa();
        mapa.setCodigo(10L);
        sp.setMapa(mapa);

        when(crudService.buscarSubprocesso(1L)).thenReturn(sp);
        Atividade ativ = new Atividade();
        ativ.setDescricao("Test");
        when(atividadeService.buscarPorMapaCodigoComConhecimentos(10L)).thenReturn(List.of(ativ));

        List<AtividadeVisualizacaoDto> result = service.listarAtividadesSubprocesso(1L);
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("Deve listar atividades com conhecimentos")
    void deveListarAtividadesComConhecimentos() {
        Subprocesso sp = new Subprocesso();
        Mapa mapa = new Mapa();
        mapa.setCodigo(10L);
        sp.setMapa(mapa);
        when(crudService.buscarSubprocesso(1L)).thenReturn(sp);

        Atividade ativ = new Atividade();
        ativ.setDescricao("Test");
        Conhecimento c = new Conhecimento();
        c.setCodigo(1L);
        c.setDescricao("K1");
        ativ.setConhecimentos(List.of(c));
        when(atividadeService.buscarPorMapaCodigoComConhecimentos(10L)).thenReturn(List.of(ativ));

        List<AtividadeVisualizacaoDto> result = service.listarAtividadesSubprocesso(1L);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getConhecimentos()).hasSize(1);
        assertThat(result.get(0).getConhecimentos().get(0).getDescricao()).isEqualTo("K1");
    }

    @Test
    @DisplayName("Deve retornar vazio ao listar atividades se mapa null")
    void deveRetornarVazioSeMapaNull() {
        Subprocesso sp = new Subprocesso();
        sp.setMapa(null);
        when(crudService.buscarSubprocesso(1L)).thenReturn(sp);
        assertThat(service.listarAtividadesSubprocesso(1L)).isEmpty();
    }

    @Test
    @DisplayName("obterDetalhes sucesso ADMIN")
    void obterDetalhesSucessoAdmin() {
        Usuario admin = new Usuario();
        UsuarioPerfil up = new UsuarioPerfil();
        up.setPerfil(Perfil.ADMIN);
        admin.setAtribuicoes(Set.of(up));

        Subprocesso sp = new Subprocesso();
        sp.setCodigo(1L);
        sp.setUnidade(new Unidade());
        sp.getUnidade().setSigla("U1");
        sp.getUnidade().setTituloTitular("titular");

        when(crudService.buscarSubprocesso(1L)).thenReturn(sp);
        when(subprocessoDetalheMapper.toDto(any(), any(), any(), any(), any()))
                .thenReturn(SubprocessoDetalheDto.builder().build());
        when(usuarioService.buscarPorLogin("titular")).thenReturn(new Usuario());

        SubprocessoDetalheDto result = service.obterDetalhes(1L, admin);
        assertThat(result).isNotNull();
        verify(accessControlService).verificarPermissao(admin, Acao.VISUALIZAR_SUBPROCESSO, sp);
    }

    @Test
    @DisplayName("obterDetalhes trata exceção ao buscar titular")
    void obterDetalhesTrataExcecaoTitular() {
        Usuario admin = new Usuario();
        UsuarioPerfil up = new UsuarioPerfil();
        up.setPerfil(Perfil.ADMIN);
        admin.setAtribuicoes(Set.of(up));

        Subprocesso sp = new Subprocesso();
        sp.setCodigo(1L);
        sp.setUnidade(new Unidade());
        sp.getUnidade().setSigla("U1");
        sp.getUnidade().setTituloTitular("titular");

        when(crudService.buscarSubprocesso(1L)).thenReturn(sp);
        when(subprocessoDetalheMapper.toDto(any(), any(), any(), any(), any()))
                .thenReturn(SubprocessoDetalheDto.builder().build());
        when(usuarioService.buscarPorLogin("titular")).thenThrow(new RuntimeException("Erro"));

        SubprocessoDetalheDto result = service.obterDetalhes(1L, admin);
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("obterDetalhes falha acesso negado via AccessControlService")
    void obterDetalhesFalhaAcesso() {
        Usuario user = new Usuario();
        Subprocesso sp = new Subprocesso();
        sp.setCodigo(1L);

        when(crudService.buscarSubprocesso(1L)).thenReturn(sp);
        doThrow(new ErroAccessoNegado("Acesso negado")).when(accessControlService)
                .verificarPermissao(user, Acao.VISUALIZAR_SUBPROCESSO, sp);

        assertThatThrownBy(() -> service.obterDetalhes(1L, user))
                .isInstanceOf(ErroAccessoNegado.class)
                .hasMessage("Acesso negado");
    }

    @Test
    @DisplayName("obterDetalhes sucesso GESTOR")
    void obterDetalhesSucessoGestor() {
        Usuario gestor = new Usuario();
        Subprocesso sp = new Subprocesso();
        sp.setCodigo(1L);
        sp.setUnidade(new Unidade());

        when(crudService.buscarSubprocesso(1L)).thenReturn(sp);
        when(subprocessoDetalheMapper.toDto(any(), any(), any(), any(), any()))
                .thenReturn(SubprocessoDetalheDto.builder().build());

        SubprocessoDetalheDto result = service.obterDetalhes(1L, gestor);
        assertThat(result).isNotNull();
        verify(accessControlService).verificarPermissao(gestor, Acao.VISUALIZAR_SUBPROCESSO, sp);
    }

    @Test
    @DisplayName("obterPermissoes")
    void obterPermissoes() {
        Usuario user = new Usuario();
        Subprocesso sp = new Subprocesso();
        when(crudService.buscarSubprocesso(1L)).thenReturn(sp);
        
        SubprocessoPermissoesDto result = service.obterPermissoes(1L, user);
        assertThat(result).isNotNull();
        org.mockito.Mockito.verify(accessControlService, org.mockito.Mockito.atLeastOnce())
                .podeExecutar(eq(user), any(Acao.class), eq(sp));
    }

    @Test
    @DisplayName("Obter cadastro com atividades e conhecimentos")
    void obterCadastro() {
        Subprocesso sp = new Subprocesso();
        sp.setMapa(new Mapa());
        sp.getMapa().setCodigo(10L);
        sp.setUnidade(new Unidade());
        when(crudService.buscarSubprocesso(1L)).thenReturn(sp);

        Atividade a = new Atividade();
        a.setConhecimentos(List.of(new Conhecimento()));
        when(atividadeService.buscarPorMapaCodigoComConhecimentos(10L)).thenReturn(List.of(a));
        when(conhecimentoMapper.toDto(any())).thenReturn(ConhecimentoDto.builder().build());

        SubprocessoCadastroDto dto = service.obterCadastro(1L);
        assertThat(dto.getAtividades()).hasSize(1);
    }

    @Test
    @DisplayName("Obter sugestoes")
    void obterSugestoes() {
        Subprocesso sp = new Subprocesso();
        when(crudService.buscarSubprocesso(1L)).thenReturn(sp);
        assertThat(service.obterSugestoes(1L)).isNotNull();
    }

    @Test
    @DisplayName("Obter mapa para ajuste")
    void obterMapaParaAjuste() {
        Subprocesso sp = new Subprocesso();
        sp.setMapa(new Mapa());
        sp.getMapa().setCodigo(10L);
        when(crudService.buscarSubprocessoComMapa(1L)).thenReturn(sp);
        when(mapaAjusteMapper.toDto(any(), any(), any(), any(), any())).thenReturn(MapaAjusteDto.builder().build());

        assertThat(service.obterMapaParaAjuste(1L)).isNotNull();
    }

    @Test
    @DisplayName("Obter cadastro com atividades vazias")
    void obterCadastroAtividadesVazias() {
        Subprocesso sp = new Subprocesso();
        sp.setMapa(new Mapa());
        sp.getMapa().setCodigo(10L);
        sp.setUnidade(new Unidade());
        when(crudService.buscarSubprocesso(1L)).thenReturn(sp);
        when(atividadeService.buscarPorMapaCodigoComConhecimentos(10L)).thenReturn(List.of());

        SubprocessoCadastroDto dto = service.obterCadastro(1L);
        assertThat(dto.getAtividades()).isEmpty();
    }
}
