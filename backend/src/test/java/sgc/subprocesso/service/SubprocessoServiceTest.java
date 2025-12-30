package sgc.subprocesso.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import sgc.comum.erros.ErroAccessoNegado;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.Conhecimento;
import sgc.mapa.service.AtividadeService;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.comum.erros.ErroValidacao;
import sgc.fixture.MapaFixture;
import sgc.fixture.SubprocessoFixture;
import sgc.mapa.model.Competencia;
import sgc.mapa.service.CompetenciaService;
import sgc.mapa.model.Mapa;
import sgc.mapa.service.MapaService;
import sgc.subprocesso.dto.*;
import sgc.subprocesso.mapper.SubprocessoDetalheMapper;
import sgc.subprocesso.mapper.SubprocessoMapper;
import sgc.subprocesso.model.MovimentacaoRepo;
import sgc.processo.model.Processo;
import sgc.processo.model.TipoProcesso;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoRepo;
import sgc.unidade.model.Unidade;
import sgc.usuario.UsuarioService;
import sgc.usuario.model.Perfil;
import sgc.usuario.model.Usuario;
import sgc.usuario.model.UsuarioPerfil;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários para SubprocessoService")
class SubprocessoServiceTest {

    @Mock
    private SubprocessoRepo repositorioSubprocesso;
    @Mock
    private AtividadeService atividadeService;
    @Mock
    private CompetenciaService competenciaService;
    @Mock
    private SubprocessoMapper subprocessoMapper;
    @Mock
    private MapaService mapaService;
    @Mock
    private MovimentacaoRepo repositorioMovimentacao;
    @Mock
    private UsuarioService usuarioService;
    @Mock
    private SubprocessoPermissoesService subprocessoPermissoesService;
    @Mock
    private SubprocessoDetalheMapper subprocessoDetalheMapper;
    @Mock
    private sgc.subprocesso.mapper.MapaAjusteMapper mapaAjusteMapper;
    @Mock
    private sgc.analise.AnaliseService analiseService;

    @InjectMocks
    private SubprocessoService service;

    private void mockSecurityContext(String username) {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(username);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Nested
    @DisplayName("Cenários de Leitura")
    class LeituraTests {

        @Test
        @DisplayName("Deve verificar acesso da unidade ao processo")
        void deveVerificarAcessoUnidadeAoProcesso() {
            when(repositorioSubprocesso.existsByProcessoCodigoAndUnidadeCodigoIn(1L, List.of(10L, 20L)))
                    .thenReturn(true);
            assertThat(service.verificarAcessoUnidadeAoProcesso(1L, List.of(10L, 20L))).isTrue();

            when(repositorioSubprocesso.existsByProcessoCodigoAndUnidadeCodigoIn(2L, List.of(10L)))
                    .thenReturn(false);
            assertThat(service.verificarAcessoUnidadeAoProcesso(2L, List.of(10L))).isFalse();
        }

        @Test
        @DisplayName("Deve listar entidades por processo")
        void deveListarEntidadesPorProcesso() {
            when(repositorioSubprocesso.findByProcessoCodigoWithUnidade(1L))
                    .thenReturn(List.of(new Subprocesso()));
            assertThat(service.listarEntidadesPorProcesso(1L)).hasSize(1);
        }

        @Test
        @DisplayName("Deve listar atividades do subprocesso convertidas para DTO")
        void deveListarAtividadesSubprocesso() {
            Subprocesso sp = new Subprocesso();
            Mapa mapa = new Mapa();
            mapa.setCodigo(100L);
            sp.setMapa(mapa);
            sp.setCodigo(1L);

            Atividade ativ = new Atividade();
            ativ.setCodigo(10L);
            ativ.setDescricao("Atividade Teste");

            Conhecimento con = new Conhecimento();
            con.setCodigo(50L);
            con.setDescricao("Conhecimento Teste");

            when(repositorioSubprocesso.findById(1L)).thenReturn(Optional.of(sp));
            when(atividadeService.buscarPorMapaCodigo(100L)).thenReturn(List.of(ativ));
            when(atividadeService.listarConhecimentosPorAtividade(10L)).thenReturn(List.of(con));

            List<AtividadeVisualizacaoDto> result = service.listarAtividadesSubprocesso(1L);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getDescricao()).isEqualTo("Atividade Teste");
            assertThat(result.get(0).getConhecimentos()).hasSize(1);
            assertThat(result.get(0).getConhecimentos().get(0).getDescricao()).isEqualTo("Conhecimento Teste");
        }

        @Test
        @DisplayName("Deve retornar lista vazia se subprocesso não tiver mapa")
        void deveRetornarListaVaziaSeSemMapa() {
            Subprocesso sp = new Subprocesso();
            sp.setCodigo(1L);
            sp.setMapa(null);

            when(repositorioSubprocesso.findById(1L)).thenReturn(Optional.of(sp));

            assertThat(service.listarAtividadesSubprocesso(1L)).isEmpty();
        }

        @Test
        @DisplayName("Deve retornar situação quando subprocesso existe")
        void deveRetornarSituacaoQuandoSubprocessoExiste() {
            Subprocesso sp = SubprocessoFixture.subprocessoPadrao(null, null);
            sp.setCodigo(1L);
            when(repositorioSubprocesso.findById(1L)).thenReturn(Optional.of(sp));

            assertThat(service.obterStatus(1L)).isNotNull();
        }

        @Test
        @DisplayName("Deve lançar exceção quando subprocesso não existe ao buscar situação")
        void deveLancarExcecaoQuandoSubprocessoNaoExiste() {
            when(repositorioSubprocesso.findById(1L)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> service.obterStatus(1L))
                    .isInstanceOf(ErroEntidadeNaoEncontrada.class)
                    .hasMessageContaining("Subprocesso")
                    .hasNoCause();
        }

        @Test
        @DisplayName("Deve retornar entidade por código do mapa")
        void deveRetornarEntidadePorCodigoMapa() {
            when(repositorioSubprocesso.findByMapaCodigo(100L)).thenReturn(Optional.of(SubprocessoFixture.subprocessoPadrao(null, null)));
            assertThat(service.obterEntidadePorCodigoMapa(100L)).isNotNull();
        }

        @Test
        @DisplayName("Deve retornar lista vazia se não houver atividades sem conhecimento")
        void deveRetornarListaVaziaSeNaoHouverAtividadesSemConhecimento() {
            Subprocesso subprocesso = new Subprocesso();
            Mapa mapa = new Mapa();
            mapa.setCodigo(1L);
            subprocesso.setMapa(mapa);

            when(repositorioSubprocesso.findById(1L)).thenReturn(Optional.of(subprocesso));
            when(atividadeService.buscarPorMapaCodigoComConhecimentos(1L)).thenReturn(Collections.emptyList());

            List<Atividade> result = service.obterAtividadesSemConhecimento(1L);
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Cenários de Escrita (CRUD)")
    class CrudTests {
        @Test
        @DisplayName("Deve criar subprocesso com sucesso")
        void deveCriarSubprocessoComSucesso() {
            SubprocessoDto dto = SubprocessoDto.builder().build();
            Subprocesso entity = SubprocessoFixture.subprocessoPadrao(null, null);

            when(subprocessoMapper.toEntity(dto)).thenReturn(entity);
            when(repositorioSubprocesso.save(any())).thenReturn(entity);
            when(mapaService.salvar(any())).thenReturn(MapaFixture.mapaPadrao(null));
            when(subprocessoMapper.toDTO(any())).thenReturn(dto);

            assertThat(service.criar(dto)).isNotNull();
            verify(repositorioSubprocesso, times(2)).save(any());
        }

        @Test
        @DisplayName("Deve atualizar subprocesso com sucesso")
        void deveAtualizarSubprocessoComSucesso() {
            SubprocessoDto dto = SubprocessoDto.builder().codMapa(100L).build();
            Subprocesso entity = SubprocessoFixture.subprocessoPadrao(null, null);

            when(repositorioSubprocesso.findById(1L)).thenReturn(Optional.of(entity));
            when(repositorioSubprocesso.save(any())).thenReturn(entity);
            when(subprocessoMapper.toDTO(any())).thenReturn(dto);

            assertThat(service.atualizar(1L, dto)).isNotNull();
        }

        @Test
        @DisplayName("Deve excluir subprocesso com sucesso")
        void deveExcluirSubprocessoComSucesso() {
            when(repositorioSubprocesso.existsById(1L)).thenReturn(true);
            service.excluir(1L);
            verify(repositorioSubprocesso).deleteById(1L);
        }

        @Test
        @DisplayName("Deve lançar exceção ao excluir subprocesso inexistente")
        void deveLancarExcecaoAoExcluirSubprocessoInexistente() {
            when(repositorioSubprocesso.existsById(1L)).thenReturn(false);
            assertThatThrownBy(() -> service.excluir(1L))
                    .isInstanceOf(ErroEntidadeNaoEncontrada.class)
                    .hasMessageContaining("Subprocesso")
                    .hasNoCause();
        }
    }

    @Nested
    @DisplayName("Cenários de Validação")
    class ValidacaoTests {
        @Test
        @DisplayName("Deve validar permissão de edição do mapa - Sucesso")
        void deveValidarPermissaoEdicaoMapaSucesso() {
            Subprocesso sp = new Subprocesso();
            Unidade u = new Unidade();
            u.setTituloTitular("123456789012");
            sp.setUnidade(u);

            Usuario user = new Usuario();
            user.setTituloEleitoral("123456789012");

            when(repositorioSubprocesso.findByMapaCodigo(100L)).thenReturn(Optional.of(sp));
            when(usuarioService.buscarUsuarioPorLogin("user")).thenReturn(user);

            service.validarPermissaoEdicaoMapa(100L, "user");
        }

        @Test
        @DisplayName("Deve validar permissão de edição do mapa - Falha não titular")
        void deveValidarPermissaoEdicaoMapaFalha() {
            Subprocesso sp = new Subprocesso();
            Unidade u = new Unidade();
            u.setTituloTitular("999999999999"); // Outro titular
            sp.setUnidade(u);

            Usuario user = new Usuario();
            user.setTituloEleitoral("123456789012");

            when(repositorioSubprocesso.findByMapaCodigo(100L)).thenReturn(Optional.of(sp));
            when(usuarioService.buscarUsuarioPorLogin("user")).thenReturn(user);

            assertThatThrownBy(() -> service.validarPermissaoEdicaoMapa(100L, "user"))
                    .isInstanceOf(ErroAccessoNegado.class);
        }

        @Test
        @DisplayName("Deve validar permissão de edição do mapa - Falha sem unidade")
        void deveValidarPermissaoEdicaoMapaFalhaSemUnidade() {
            Subprocesso sp = new Subprocesso();
            sp.setUnidade(null);

            when(repositorioSubprocesso.findByMapaCodigo(100L)).thenReturn(Optional.of(sp));

            assertThatThrownBy(() -> service.validarPermissaoEdicaoMapa(100L, "user"))
                    .isInstanceOf(ErroEntidadeNaoEncontrada.class)
                    .hasMessageContaining("Unidade não associada");
        }

        @Test
        @DisplayName("Deve validar existência de atividades - Sucesso")
        void deveValidarExistenciaAtividadesSucesso() {
            Subprocesso sp = new Subprocesso();
            Mapa mapa = new Mapa();
            mapa.setCodigo(10L);
            sp.setMapa(mapa);

            Atividade atividade = new Atividade();
            atividade.setConhecimentos(List.of(new Conhecimento()));
            when(repositorioSubprocesso.findById(1L)).thenReturn(Optional.of(sp));
            when(atividadeService.buscarPorMapaCodigoComConhecimentos(10L)).thenReturn(List.of(atividade));

            service.validarExistenciaAtividades(1L);
        }

        @Test
        @DisplayName("Deve validar existência de atividades - Falha sem mapa")
        void deveValidarExistenciaAtividadesFalhaSemMapa() {
            Subprocesso sp = new Subprocesso();
            sp.setMapa(null);
            when(repositorioSubprocesso.findById(1L)).thenReturn(Optional.of(sp));

            assertThatThrownBy(() -> service.validarExistenciaAtividades(1L))
                    .isInstanceOf(ErroValidacao.class)
                    .hasMessageContaining("Mapa não encontrado");
        }

        @Test
        @DisplayName("Deve validar existência de atividades - Falha lista vazia")
        void deveValidarExistenciaAtividadesFalhaVazia() {
            Subprocesso sp = new Subprocesso();
            Mapa mapa = new Mapa();
            mapa.setCodigo(10L);
            sp.setMapa(mapa);

            when(repositorioSubprocesso.findById(1L)).thenReturn(Optional.of(sp));
            when(atividadeService.buscarPorMapaCodigoComConhecimentos(10L)).thenReturn(Collections.emptyList());

            assertThatThrownBy(() -> service.validarExistenciaAtividades(1L))
                    .isInstanceOf(ErroValidacao.class)
                    .hasMessageContaining("ao menos uma atividade");
        }

        @Test
        @DisplayName("Deve lançar exceção se competência não estiver associada")
        void deveLancarExcecaoSeCompetenciaNaoEstiverAssociada() {
            Competencia competencia = new Competencia();
            competencia.setDescricao("Competencia de Teste");
            when(competenciaService.buscarPorMapa(1L))
                    .thenReturn(Collections.singletonList(competencia));

            assertThatThrownBy(() -> service.validarAssociacoesMapa(1L))
                    .isInstanceOf(ErroValidacao.class)
                    .hasMessageContaining("competência")
                    .hasNoCause();
        }

        @Test
        @DisplayName("Deve lançar exceção se atividade não estiver associada")
        void deveLancarExcecaoSeAtividadeNaoEstiverAssociada() {
            when(competenciaService.buscarPorMapa(1L)).thenReturn(Collections.emptyList());

            Atividade atividade = new Atividade();
            atividade.setDescricao("Atividade Solta");
            // Sem competencias
            when(atividadeService.buscarPorMapaCodigo(1L)).thenReturn(List.of(atividade));

            assertThatThrownBy(() -> service.validarAssociacoesMapa(1L))
                    .isInstanceOf(ErroValidacao.class)
                    .hasMessageContaining("atividades");
        }

        @Test
        @DisplayName("validarCadastro sucesso")
        void validarCadastroSucesso() {
            Subprocesso sp = new Subprocesso();
            Mapa mapa = new Mapa();
            mapa.setCodigo(10L);
            sp.setMapa(mapa);

            Atividade ativ = new Atividade();
            ativ.setConhecimentos(List.of(new sgc.mapa.model.Conhecimento()));

            when(repositorioSubprocesso.findById(1L)).thenReturn(Optional.of(sp));
            when(atividadeService.buscarPorMapaCodigoComConhecimentos(10L)).thenReturn(List.of(ativ));

            ValidacaoCadastroDto result = service.validarCadastro(1L);
            assertThat(result.getValido()).isTrue();
            assertThat(result.getErros()).isEmpty();
        }

        @Test
        @DisplayName("validarCadastro falha sem mapa")
        void validarCadastroSemMapa() {
            Subprocesso sp = new Subprocesso();
            sp.setMapa(null);

            when(repositorioSubprocesso.findById(1L)).thenReturn(Optional.of(sp));

            ValidacaoCadastroDto result = service.validarCadastro(1L);
            assertThat(result.getValido()).isFalse();
            assertThat(result.getErros().get(0).getTipo()).isEqualTo("MAPA_INEXISTENTE");
        }

        @Test
        @DisplayName("validarCadastro falha sem atividades")
        void validarCadastroSemAtividades() {
            Subprocesso sp = new Subprocesso();
            Mapa mapa = new Mapa();
            mapa.setCodigo(10L);
            sp.setMapa(mapa);

            when(repositorioSubprocesso.findById(1L)).thenReturn(Optional.of(sp));
            when(atividadeService.buscarPorMapaCodigoComConhecimentos(10L)).thenReturn(Collections.emptyList());

            ValidacaoCadastroDto result = service.validarCadastro(1L);
            assertThat(result.getValido()).isFalse();
            assertThat(result.getErros().get(0).getTipo()).isEqualTo("SEM_ATIVIDADES");
        }
    }

    @Nested
    @DisplayName("Cenários de Transição de Estado")
    class TransicaoEstadoTests {
        @Test
        @DisplayName("Deve atualizar situação para EM ANDAMENTO (Mapeamento)")
        void deveAtualizarParaEmAndamentoMapeamento() {
            Subprocesso sp = new Subprocesso();
            sp.setCodigo(1L);
            sp.setSituacao(SituacaoSubprocesso.NAO_INICIADO);
            Processo p = new Processo();
            p.setTipo(TipoProcesso.MAPEAMENTO);
            sp.setProcesso(p);

            when(repositorioSubprocesso.findByMapaCodigo(100L)).thenReturn(Optional.of(sp));

            service.atualizarSituacaoParaEmAndamento(100L);

            assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
            verify(repositorioSubprocesso).save(sp);
        }

        @Test
        @DisplayName("Deve atualizar situação para EM ANDAMENTO (Revisão)")
        void deveAtualizarParaEmAndamentoRevisao() {
            Subprocesso sp = new Subprocesso();
            sp.setCodigo(1L);
            sp.setSituacao(SituacaoSubprocesso.NAO_INICIADO);
            Processo p = new Processo();
            p.setTipo(TipoProcesso.REVISAO);
            sp.setProcesso(p);

            when(repositorioSubprocesso.findByMapaCodigo(100L)).thenReturn(Optional.of(sp));

            service.atualizarSituacaoParaEmAndamento(100L);

            assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO);
            verify(repositorioSubprocesso).save(sp);
        }

        @Test
        @DisplayName("Não deve atualizar se situação não for NAO_INICIADO")
        void naoDeveAtualizarSeJaIniciado() {
            Subprocesso sp = new Subprocesso();
            sp.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);

            when(repositorioSubprocesso.findByMapaCodigo(100L)).thenReturn(Optional.of(sp));

            service.atualizarSituacaoParaEmAndamento(100L);

            verify(repositorioSubprocesso, never()).save(sp);
        }

        @Test
        @DisplayName("Deve lançar erro se processo não associado")
        void deveLancarErroSeProcessoNaoAssociado() {
            Subprocesso sp = new Subprocesso();
            sp.setSituacao(SituacaoSubprocesso.NAO_INICIADO);
            sp.setProcesso(null);

            when(repositorioSubprocesso.findByMapaCodigo(100L)).thenReturn(Optional.of(sp));

            assertThatThrownBy(() -> service.atualizarSituacaoParaEmAndamento(100L))
                    .isInstanceOf(ErroEntidadeNaoEncontrada.class);
        }
    }

    @Nested
    @DisplayName("Cenários de Permissões e Detalhes")
    class PermissaoDetalheTests {

        @Test
        @DisplayName("obterDetalhes sucesso ADMIN")
        void obterDetalhesSucessoAdmin() {
            mockSecurityContext("admin");
            Usuario admin = new Usuario();
            // Using UsuarioPerfil directly as Atribuicao is not available/used this way
            UsuarioPerfil up = new UsuarioPerfil();
            up.setPerfil(Perfil.ADMIN);
            admin.setAtribuicoes(Set.of(up));

            when(usuarioService.buscarUsuarioPorLogin("admin")).thenReturn(admin);

            Subprocesso sp = new Subprocesso();
            sp.setCodigo(1L);
            sp.setUnidade(new Unidade());
            sp.getUnidade().setSigla("U1");

            when(repositorioSubprocesso.findById(1L)).thenReturn(Optional.of(sp));
            when(subprocessoDetalheMapper.toDto(any(), any(), any(), any(), any()))
                    .thenReturn(SubprocessoDetalheDto.builder().build());

            SubprocessoDetalheDto result = service.obterDetalhes(1L, Perfil.ADMIN, null);
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("obterDetalhes falha acesso negado")
        void obterDetalhesFalhaAcesso() {
            mockSecurityContext("user");
            Usuario user = new Usuario();
            Unidade u1 = new Unidade();
            u1.setCodigo(10L);

            UsuarioPerfil up = new UsuarioPerfil();
            up.setPerfil(Perfil.CHEFE);
            up.setUnidade(u1);
            user.setAtribuicoes(Set.of(up));

            when(usuarioService.buscarUsuarioPorLogin("user")).thenReturn(user);

            Subprocesso sp = new Subprocesso();
            sp.setCodigo(1L);
            Unidade u2 = new Unidade();
            u2.setCodigo(20L);
            sp.setUnidade(u2);

            when(repositorioSubprocesso.findById(1L)).thenReturn(Optional.of(sp));

            assertThatThrownBy(() -> service.obterDetalhes(1L, Perfil.CHEFE, 10L))
                    .isInstanceOf(ErroAccessoNegado.class);
        }

        @Test
        @DisplayName("obterDetalhes sucesso GESTOR unidade subordinada")
        void obterDetalhesSucessoGestorSubordinada() {
            mockSecurityContext("gestor");
            Usuario gestor = new Usuario();
            Unidade uSuperior = new Unidade();
            uSuperior.setCodigo(100L);

            UsuarioPerfil up = new UsuarioPerfil();
            up.setPerfil(Perfil.GESTOR);
            up.setUnidade(uSuperior);
            gestor.setAtribuicoes(Set.of(up));

            when(usuarioService.buscarUsuarioPorLogin("gestor")).thenReturn(gestor);

            Subprocesso sp = new Subprocesso();
            sp.setCodigo(1L);
            Unidade uSubordinada = new Unidade();
            uSubordinada.setCodigo(101L);
            uSubordinada.setUnidadeSuperior(uSuperior);
            sp.setUnidade(uSubordinada);

            when(repositorioSubprocesso.findById(1L)).thenReturn(Optional.of(sp));
            when(subprocessoDetalheMapper.toDto(any(), any(), any(), any(), any()))
                    .thenReturn(SubprocessoDetalheDto.builder().build());

            SubprocessoDetalheDto result = service.obterDetalhes(1L, Perfil.GESTOR, null);
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("obterDetalhes falha GESTOR unidade nao subordinada")
        void obterDetalhesFalhaGestorNaoSubordinada() {
            mockSecurityContext("gestor");
            Usuario gestor = new Usuario();
            Unidade uGestor = new Unidade();
            uGestor.setCodigo(100L);

            UsuarioPerfil up = new UsuarioPerfil();
            up.setPerfil(Perfil.GESTOR);
            up.setUnidade(uGestor);
            gestor.setAtribuicoes(Set.of(up));

            when(usuarioService.buscarUsuarioPorLogin("gestor")).thenReturn(gestor);

            Subprocesso sp = new Subprocesso();
            sp.setCodigo(1L);
            Unidade uOutra = new Unidade();
            uOutra.setCodigo(200L);
            // uOutra nao tem uGestor como superior
            sp.setUnidade(uOutra);

            when(repositorioSubprocesso.findById(1L)).thenReturn(Optional.of(sp));

            assertThatThrownBy(() -> service.obterDetalhes(1L, Perfil.GESTOR, null))
                    .isInstanceOf(ErroAccessoNegado.class);
        }

        @Test
        @DisplayName("obterDetalhes sucesso SERVIDOR mesma unidade")
        void obterDetalhesSucessoServidorMesmaUnidade() {
            mockSecurityContext("servidor");
            Usuario servidor = new Usuario();
            Unidade u1 = new Unidade();
            u1.setCodigo(10L);

            UsuarioPerfil up = new UsuarioPerfil();
            up.setPerfil(Perfil.SERVIDOR);
            up.setUnidade(u1);
            servidor.setAtribuicoes(Set.of(up));

            when(usuarioService.buscarUsuarioPorLogin("servidor")).thenReturn(servidor);

            Subprocesso sp = new Subprocesso();
            sp.setCodigo(1L);
            sp.setUnidade(u1);

            when(repositorioSubprocesso.findById(1L)).thenReturn(Optional.of(sp));
            when(subprocessoDetalheMapper.toDto(any(), any(), any(), any(), any()))
                    .thenReturn(SubprocessoDetalheDto.builder().build());

            SubprocessoDetalheDto result = service.obterDetalhes(1L, Perfil.SERVIDOR, null);
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("obterPermissoes")
        void obterPermissoes() {
            mockSecurityContext("user");
            Usuario user = new Usuario();
            when(usuarioService.buscarUsuarioPorLogin("user")).thenReturn(user);

            Subprocesso sp = new Subprocesso();
            when(repositorioSubprocesso.findById(1L)).thenReturn(Optional.of(sp));
            when(subprocessoPermissoesService.calcularPermissoes(sp, user))
                    .thenReturn(SubprocessoPermissoesDto.builder().build());

            SubprocessoPermissoesDto result = service.obterPermissoes(1L);
            assertThat(result).isNotNull();
        }
    }

    @Nested
    @DisplayName("Cenários de DTO e Mapeamento")
    class DtoMappingTests {

        @Test
        @DisplayName("obterCadastro")
        void obterCadastro() {
            Subprocesso sp = new Subprocesso();
            sp.setCodigo(1L);
            Mapa mapa = new Mapa();
            mapa.setCodigo(10L);
            sp.setMapa(mapa);

            when(repositorioSubprocesso.findById(1L)).thenReturn(Optional.of(sp));
            when(atividadeService.buscarPorMapaCodigoComConhecimentos(10L)).thenReturn(Collections.emptyList());

            SubprocessoCadastroDto result = service.obterCadastro(1L);
            assertThat(result).isNotNull();
            assertThat(result.getSubprocessoCodigo()).isEqualTo(1L);
        }

        @Test
        @DisplayName("obterSugestoes")
        void obterSugestoes() {
            Subprocesso sp = new Subprocesso();
            sp.setCodigo(1L);
            Mapa mapa = new Mapa();
            mapa.setSugestoes("Sugestão Teste");
            sp.setMapa(mapa);

            when(repositorioSubprocesso.findById(1L)).thenReturn(Optional.of(sp));

            SugestoesDto result = service.obterSugestoes(1L);
            assertThat(result.getSugestoes()).isEqualTo("Sugestão Teste");
        }

        @Test
        @DisplayName("obterMapaParaAjuste")
        void obterMapaParaAjuste() {
            Subprocesso sp = new Subprocesso();
            sp.setCodigo(1L);
            Mapa mapa = new Mapa();
            mapa.setCodigo(10L);
            sp.setMapa(mapa);

            when(repositorioSubprocesso.findById(1L)).thenReturn(Optional.of(sp));
            when(analiseService.listarPorSubprocesso(1L, sgc.analise.model.TipoAnalise.VALIDACAO))
                    .thenReturn(java.util.Collections.emptyList());
            when(mapaAjusteMapper.toDto(any(), any(), any(), any(), any()))
                    .thenReturn(MapaAjusteDto.builder().build());

            MapaAjusteDto result = service.obterMapaParaAjuste(1L);
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("listar")
        void listar() {
            when(repositorioSubprocesso.findAll()).thenReturn(List.of(new Subprocesso()));
            when(subprocessoMapper.toDTO(any())).thenReturn(new SubprocessoDto());

            List<SubprocessoDto> result = service.listar();
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("obterPorProcessoEUnidade")
        void obterPorProcessoEUnidade() {
            Subprocesso sp = new Subprocesso();
            sp.setCodigo(1L);

            when(repositorioSubprocesso.findByProcessoCodigoAndUnidadeCodigo(1L, 10L))
                    .thenReturn(Optional.of(sp));
            when(subprocessoMapper.toDTO(sp)).thenReturn(SubprocessoDto.builder().codigo(1L).build());

            SubprocessoDto result = service.obterPorProcessoEUnidade(1L, 10L);
            assertThat(result).isNotNull();
            assertThat(result.getCodigo()).isEqualTo(1L);
        }
    }
}
