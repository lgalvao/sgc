package sgc.subprocesso.service.decomposed;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.analise.AnaliseService;
import sgc.comum.erros.ErroAccessoNegado;
import sgc.mapa.dto.ConhecimentoDto;
import sgc.mapa.mapper.ConhecimentoMapper;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.Conhecimento;
import sgc.mapa.model.Mapa;
import sgc.mapa.service.AtividadeService;
import sgc.mapa.service.CompetenciaService;
import sgc.mapa.service.ConhecimentoService;
import sgc.organizacao.UsuarioService;
import sgc.organizacao.model.Perfil;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.organizacao.model.UsuarioPerfil;
import sgc.subprocesso.dto.*;
import sgc.subprocesso.mapper.MapaAjusteMapper;
import sgc.subprocesso.mapper.SubprocessoDetalheMapper;
import sgc.subprocesso.model.MovimentacaoRepo;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.SubprocessoPermissoesService;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes para SubprocessoDetalheService")
class SubprocessoDetalheServiceTest {

    @Mock
    private SubprocessoCrudService crudService;
    @Mock
    private AtividadeService atividadeService;
    @Mock
    private UsuarioService usuarioService;
    @Mock
    private MovimentacaoRepo repositorioMovimentacao;
    @Mock
    private SubprocessoPermissoesService subprocessoPermissoesService;
    @Mock
    private SubprocessoDetalheMapper subprocessoDetalheMapper;
    @Mock
    private ConhecimentoMapper conhecimentoMapper;
    @Mock
    private AnaliseService analiseService;
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
    @DisplayName("Deve listar atividades tratando conhecimentos nulos")
    void deveListarAtividadesTratandoConhecimentosNulos() {
        Subprocesso sp = new Subprocesso();
        Mapa mapa = new Mapa();
        mapa.setCodigo(10L);
        sp.setMapa(mapa);

        when(crudService.buscarSubprocesso(1L)).thenReturn(sp);
        Atividade ativ = new Atividade();
        ativ.setDescricao("Test");
        ativ.setConhecimentos(null);
        when(atividadeService.buscarPorMapaCodigoComConhecimentos(10L)).thenReturn(List.of(ativ));

        List<AtividadeVisualizacaoDto> result = service.listarAtividadesSubprocesso(1L);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getConhecimentos()).isEmpty();
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

        SubprocessoDetalheDto result = service.obterDetalhes(1L, Perfil.ADMIN, admin);
        assertThat(result).isNotNull();
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

        SubprocessoDetalheDto result = service.obterDetalhes(1L, Perfil.ADMIN, admin);
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("obterDetalhes falha se perfil null")
    void obterDetalhesFalhaPerfilNull() {
        Usuario usuario = new Usuario();
        assertThatThrownBy(() -> service.obterDetalhes(1L, null, usuario))
            .isInstanceOf(ErroAccessoNegado.class);
    }

    @Test
    @DisplayName("obterDetalhes falha acesso negado")
    void obterDetalhesFalhaAcesso() {
        Usuario user = new Usuario();
        Unidade u1 = new Unidade();
        u1.setCodigo(10L);

        UsuarioPerfil up = new UsuarioPerfil();
        up.setPerfil(Perfil.CHEFE);
        up.setUnidade(u1);
        user.setAtribuicoes(Set.of(up));

        Subprocesso sp = new Subprocesso();
        sp.setCodigo(1L);
        Unidade u2 = new Unidade();
        u2.setCodigo(20L);
        sp.setUnidade(u2);

        when(crudService.buscarSubprocesso(1L)).thenReturn(sp);

        assertThatThrownBy(() -> service.obterDetalhes(1L, Perfil.CHEFE, user))
                .isInstanceOf(ErroAccessoNegado.class);
    }

    @Test
    @DisplayName("obterDetalhes sucesso GESTOR unidade subordinada")
    void obterDetalhesSucessoGestorSubordinada() {
        Usuario gestor = new Usuario();
        Unidade uSuperior = new Unidade();
        uSuperior.setCodigo(100L);

        UsuarioPerfil up = new UsuarioPerfil();
        up.setPerfil(Perfil.GESTOR);
        up.setUnidade(uSuperior);
        gestor.setAtribuicoes(Set.of(up));

        Subprocesso sp = new Subprocesso();
        sp.setCodigo(1L);
        Unidade uSubordinada = new Unidade();
        uSubordinada.setCodigo(101L);
        uSubordinada.setUnidadeSuperior(uSuperior);
        sp.setUnidade(uSubordinada);

        when(crudService.buscarSubprocesso(1L)).thenReturn(sp);
        when(subprocessoDetalheMapper.toDto(any(), any(), any(), any(), any()))
                .thenReturn(SubprocessoDetalheDto.builder().build());

        SubprocessoDetalheDto result = service.obterDetalhes(1L, Perfil.GESTOR, gestor);
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("obterDetalhes falha GESTOR unidade nao subordinada")
    void obterDetalhesFalhaGestorNaoSubordinada() {
        Usuario gestor = new Usuario();
        Unidade uGestor = new Unidade();
        uGestor.setCodigo(100L);

        UsuarioPerfil up = new UsuarioPerfil();
        up.setPerfil(Perfil.GESTOR);
        up.setUnidade(uGestor);
        gestor.setAtribuicoes(Set.of(up));

        Subprocesso sp = new Subprocesso();
        sp.setCodigo(1L);
        Unidade uOutra = new Unidade();
        uOutra.setCodigo(200L);
        sp.setUnidade(uOutra);

        when(crudService.buscarSubprocesso(1L)).thenReturn(sp);

        assertThatThrownBy(() -> service.obterDetalhes(1L, Perfil.GESTOR, gestor))
                .isInstanceOf(ErroAccessoNegado.class);
    }

    @Test
    @DisplayName("obterDetalhes sucesso SERVIDOR mesma unidade")
    void obterDetalhesSucessoServidorMesmaUnidade() {
        Usuario servidor = new Usuario();
        Unidade u1 = new Unidade();
        u1.setCodigo(10L);

        UsuarioPerfil up = new UsuarioPerfil();
        up.setPerfil(Perfil.SERVIDOR);
        up.setUnidade(u1);
        servidor.setAtribuicoes(Set.of(up));

        Subprocesso sp = new Subprocesso();
        sp.setCodigo(1L);
        sp.setUnidade(u1);

        when(crudService.buscarSubprocesso(1L)).thenReturn(sp);
        when(subprocessoDetalheMapper.toDto(any(), any(), any(), any(), any()))
                .thenReturn(SubprocessoDetalheDto.builder().build());

        SubprocessoDetalheDto result = service.obterDetalhes(1L, Perfil.SERVIDOR, servidor);
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("obterPermissoes")
    void obterPermissoes() {
        Usuario user = new Usuario();
        Subprocesso sp = new Subprocesso();
        when(crudService.buscarSubprocesso(1L)).thenReturn(sp);
        when(subprocessoPermissoesService.calcularPermissoes(sp, user))
                .thenReturn(SubprocessoPermissoesDto.builder().build());

        SubprocessoPermissoesDto result = service.obterPermissoes(1L, user);
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("Obter cadastro com atividades e conhecimentos")
    void obterCadastro() {
        Subprocesso sp = new Subprocesso();
        sp.setMapa(new Mapa());
        sp.getMapa().setCodigo(10L);
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
    @DisplayName("Obter cadastro com mapa code null")
    void obterCadastroMapaCodeNull() {
        Subprocesso sp = new Subprocesso();
        sp.setMapa(new Mapa());
        sp.getMapa().setCodigo(null);
        sp.setUnidade(new Unidade());
        sp.getUnidade().setSigla("U1");
        when(crudService.buscarSubprocesso(1L)).thenReturn(sp);

        SubprocessoCadastroDto dto = service.obterCadastro(1L);
        assertThat(dto.getAtividades()).isEmpty();
        assertThat(dto.getUnidadeSigla()).isEqualTo("U1");
    }

    @Test
    @DisplayName("Obter cadastro com atividades null")
    void obterCadastroAtividadesNull() {
        Subprocesso sp = new Subprocesso();
        sp.setMapa(new Mapa());
        sp.getMapa().setCodigo(10L);
        sp.setUnidade(new Unidade());
        when(crudService.buscarSubprocesso(1L)).thenReturn(sp);
        when(atividadeService.buscarPorMapaCodigoComConhecimentos(10L)).thenReturn(null);

        SubprocessoCadastroDto dto = service.obterCadastro(1L);
        assertThat(dto.getAtividades()).isEmpty();
    }

    @Test
    @DisplayName("Obter cadastro com atividades sem conhecimentos (null)")
    void obterCadastroConhecimentosNull() {
        Subprocesso sp = new Subprocesso();
        sp.setMapa(new Mapa());
        sp.getMapa().setCodigo(10L);
        sp.setUnidade(new Unidade());
        when(crudService.buscarSubprocesso(1L)).thenReturn(sp);

        Atividade a = new Atividade();
        a.setCodigo(1L);
        a.setDescricao("Test");
        a.setConhecimentos(null);
        when(atividadeService.buscarPorMapaCodigoComConhecimentos(10L)).thenReturn(List.of(a));

        SubprocessoCadastroDto dto = service.obterCadastro(1L);
        assertThat(dto.getAtividades()).hasSize(1);
        assertThat(dto.getAtividades().get(0).getConhecimentos()).isEmpty();
    }

    @Test
    @DisplayName("Obter cadastro com unidade null")
    void obterCadastroUnidadeNull() {
        Subprocesso sp = new Subprocesso();
        sp.setMapa(new Mapa());
        sp.getMapa().setCodigo(10L);
        sp.setUnidade(null);
        when(crudService.buscarSubprocesso(1L)).thenReturn(sp);
        when(atividadeService.buscarPorMapaCodigoComConhecimentos(10L)).thenReturn(List.of());

        SubprocessoCadastroDto dto = service.obterCadastro(1L);
        assertThat(dto.getUnidadeSigla()).isNull();
    }

    @Test
    @DisplayName("Obter detalhes com unidade sem tituloTitular")
    void obterDetalhesUnidadeSemTitular() {
        Usuario admin = new Usuario();
        UsuarioPerfil up = new UsuarioPerfil();
        up.setPerfil(Perfil.ADMIN);
        admin.setAtribuicoes(Set.of(up));

        Subprocesso sp = new Subprocesso();
        sp.setCodigo(1L);
        sp.setUnidade(new Unidade());
        sp.getUnidade().setSigla("U1");
        sp.getUnidade().setTituloTitular(null);

        when(crudService.buscarSubprocesso(1L)).thenReturn(sp);
        when(subprocessoDetalheMapper.toDto(any(), any(), any(), any(), any()))
                .thenReturn(SubprocessoDetalheDto.builder().build());

        SubprocessoDetalheDto result = service.obterDetalhes(1L, Perfil.ADMIN, admin);
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("Obter detalhes com unidade null")
    void obterDetalhesUnidadeNull() {
        Usuario admin = new Usuario();
        UsuarioPerfil up = new UsuarioPerfil();
        up.setPerfil(Perfil.ADMIN);
        admin.setAtribuicoes(Set.of(up));

        Subprocesso sp = new Subprocesso();
        sp.setCodigo(1L);
        sp.setUnidade(null);

        when(crudService.buscarSubprocesso(1L)).thenReturn(sp);

        assertThatThrownBy(() -> service.obterDetalhes(1L, Perfil.CHEFE, admin))
                .isInstanceOf(ErroAccessoNegado.class);
    }

    @Test
    @DisplayName("Obter detalhes com CHEFE mesma unidade")
    void obterDetalhesChefeMesmaUnidade() {
        Usuario chefe = new Usuario();
        Unidade u1 = new Unidade();
        u1.setCodigo(10L);
        u1.setSigla("U1");
        u1.setTituloTitular("titular");

        UsuarioPerfil up = new UsuarioPerfil();
        up.setPerfil(Perfil.CHEFE);
        up.setUnidade(u1);
        chefe.setAtribuicoes(Set.of(up));

        Subprocesso sp = new Subprocesso();
        sp.setCodigo(1L);
        sp.setUnidade(u1);

        when(crudService.buscarSubprocesso(1L)).thenReturn(sp);
        when(subprocessoDetalheMapper.toDto(any(), any(), any(), any(), any()))
                .thenReturn(SubprocessoDetalheDto.builder().build());
        when(usuarioService.buscarPorLogin("titular")).thenReturn(new Usuario());

        SubprocessoDetalheDto result = service.obterDetalhes(1L, Perfil.CHEFE, chefe);
        assertThat(result).isNotNull();
    }
}
