package sgc.organizacao;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import sgc.comum.erros.ErroAccessoNegado;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.comum.erros.ErroValidacao;
import sgc.organizacao.dto.AdministradorDto;
import sgc.organizacao.dto.ResponsavelDto;
import sgc.organizacao.dto.UsuarioDto;
import sgc.organizacao.model.*;
import sgc.seguranca.login.ClienteAcessoAd;
import sgc.seguranca.login.LoginService;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

/**
 * Testes unitários consolidados do UsuarioService.
 * 
 * Este arquivo consolida:
 * - UsuarioServiceCoverageTest.java (38 testes)
 * - UsuarioServiceGapsTest.java (6 testes)
 * 
 * Organização:
 * - Métodos de Busca e Mapeamento
 * - Administração de Usuários
 * - Gaps de Cobertura
 */
@Tag("unit")
@ExtendWith(MockitoExtension.class)
@DisplayName("UsuarioService - Testes Unitários")
class UsuarioServiceUnitTest {

    @InjectMocks
    private UsuarioService service;

    @Mock
    private UsuarioRepo usuarioRepo;
    @Mock
    private UsuarioPerfilRepo usuarioPerfilRepo;
    @Mock
    private AdministradorRepo administradorRepo;
    @Mock
    private UnidadeRepo unidadeRepo;
    @Mock
    private ClienteAcessoAd clienteAcessoAd;
    @Mock
    private LoginService loginService;

    // ========== MÉTODOS DE BUSCA E MAPEAMENTO ==========

    @Nested
    @DisplayName("Autenticação e Carregamento de Usuário")
    class AutenticacaoCarregamento {

        @Test
        @DisplayName("Deve retornar nulo se usuário não encontrado ao carregar para autenticação")
        void deveRetornarNuloQuandoUsuarioNaoEncontrado() {
            when(usuarioRepo.findByIdWithAtribuicoes("user")).thenReturn(Optional.empty());
            
            assertThat(service.carregarUsuarioParaAutenticacao("user")).isNull();
        }

        @Test
        @DisplayName("Deve carregar atribuições se usuário encontrado")
        void deveCarregarAtribuicoesQuandoEncontrado() {
            Usuario usuario = mock(Usuario.class);
            when(usuario.getTituloEleitoral()).thenReturn("user");
            when(usuarioRepo.findByIdWithAtribuicoes("user")).thenReturn(Optional.of(usuario));
            when(usuarioPerfilRepo.findByUsuarioTitulo("user")).thenReturn(Collections.emptyList());

            Usuario result = service.carregarUsuarioParaAutenticacao("user");

            assertThat(result).isNotNull();
            verify(usuario).getAuthorities();
            verify(usuario).setAtribuicoes(any());
        }

        @Test
        @DisplayName("Deve carregar atribuições em lote para lista de usuários")
        void deveCarregarAtribuicoesEmLote() {
            Usuario u1 = new Usuario();
            u1.setTituloEleitoral("u1");
            Usuario u2 = new Usuario();
            u2.setTituloEleitoral("u2");

            UsuarioPerfil up = new UsuarioPerfil();
            ReflectionTestUtils.setField(up, "usuarioTitulo", "u1");

            when(usuarioPerfilRepo.findByUsuarioTituloIn(anyList())).thenReturn(List.of(up));

            ReflectionTestUtils.invokeMethod(service, "carregarAtribuicoesEmLote", List.of(u1, u2));

            assertThat(u1.getTodasAtribuicoes()).isNotEmpty();
            assertThat(u2.getTodasAtribuicoes()).isEmpty();
        }

        @Test
        @DisplayName("Deve ignorar lista vazia ao carregar atribuições em lote")
        void deveIgnorarListaVaziaAoCarregarAtribuicoes() {
            ReflectionTestUtils.invokeMethod(service, "carregarAtribuicoesEmLote", Collections.emptyList());
            
            verify(usuarioPerfilRepo, never()).findByUsuarioTituloIn(any());
        }
    }

    @Nested
    @DisplayName("Busca de Usuários")
    class BuscaUsuarios {

        @Test
        @DisplayName("Deve retornar empty se usuário não encontrado por título")
        void deveRetornarEmptyQuandoNaoEncontradoPorTitulo() {
            when(usuarioRepo.findById("user")).thenReturn(Optional.empty());
            
            assertThat(service.buscarUsuarioPorTitulo("user")).isEmpty();
        }

        @Test
        @DisplayName("Deve retornar DTO quando usuário encontrado por título")
        void deveRetornarDtoQuandoEncontradoPorTitulo() {
            Usuario u = new Usuario();
            u.setTituloEleitoral("user");
            when(usuarioRepo.findById("user")).thenReturn(Optional.of(u));

            Optional<UsuarioDto> res = service.buscarUsuarioPorTitulo("user");
            
            assertThat(res).isPresent();
            assertThat(res.get().getTituloEleitoral()).isEqualTo("user");
        }

        @Test
        @DisplayName("Deve retornar lista vazia se não há usuários na unidade")
        void deveRetornarListaVaziaSeNaoHaUsuariosNaUnidade() {
            when(usuarioRepo.findByUnidadeLotacaoCodigo(1L)).thenReturn(Collections.emptyList());
            
            assertThat(service.buscarUsuariosPorUnidade(1L)).isEmpty();
        }

        @Test
        @DisplayName("Deve lançar erro se usuário não encontrado por ID")
        void deveLancarErroSeUsuarioNaoEncontradoPorId() {
            when(usuarioRepo.findById("user")).thenReturn(Optional.empty());
            
            assertThatThrownBy(() -> service.buscarPorId("user"))
                    .isInstanceOf(ErroEntidadeNaoEncontrada.class);
        }

        @Test
        @DisplayName("Deve lançar erro se usuário não encontrado por login")
        void deveLancarErroSeUsuarioNaoEncontradoPorLogin() {
            when(usuarioRepo.findByIdWithAtribuicoes("user")).thenReturn(Optional.empty());
            
            assertThatThrownBy(() -> service.buscarPorLogin("user"))
                    .isInstanceOf(ErroEntidadeNaoEncontrada.class);
        }

        @Test
        @DisplayName("Deve buscar usuário por login com sucesso")
        void deveBuscarUsuarioPorLoginComSucesso() {
            Usuario u = new Usuario();
            u.setTituloEleitoral("user");
            when(usuarioRepo.findByIdWithAtribuicoes("user")).thenReturn(Optional.of(u));
            when(usuarioPerfilRepo.findByUsuarioTitulo("user")).thenReturn(Collections.emptyList());

            Usuario res = service.buscarPorLogin("user");
            
            assertThat(res).isNotNull();
        }

        @Test
        @DisplayName("Deve retornar empty se usuário não encontrado por email")
        void deveRetornarEmptySeUsuarioNaoEncontradoPorEmail() {
            when(usuarioRepo.findByEmail("email")).thenReturn(Optional.empty());
            
            assertThat(service.buscarUsuarioPorEmail("email")).isEmpty();
        }

        @Test
        @DisplayName("Deve retornar lista vazia se não há usuários ativos")
        void deveRetornarListaVaziaSeNaoHaUsuariosAtivos() {
            when(usuarioRepo.findAll()).thenReturn(Collections.emptyList());
            
            assertThat(service.buscarUsuariosAtivos()).isEmpty();
        }

        @Test
        @DisplayName("Deve retornar mapa de usuários por títulos")
        void deveRetornarMapaDeUsuariosPorTitulos() {
            Usuario u = new Usuario();
            u.setTituloEleitoral("u");
            when(usuarioRepo.findAllById(anyList())).thenReturn(List.of(u));

            var map = service.buscarUsuariosPorTitulos(List.of("u"));
            
            assertThat(map).containsKey("u");
        }

        @Test
        @DisplayName("Deve retornar lista vazia se usuário não encontrado ao buscar unidades onde é responsável")
        void deveRetornarListaVaziaSeUsuarioNaoEncontradoAoBuscarUnidadesOndeEhResponsavel() {
            when(usuarioRepo.findByIdWithAtribuicoes("user")).thenReturn(Optional.empty());
            
            assertThat(service.buscarUnidadesOndeEhResponsavel("user")).isEmpty();
        }

        @Test
        @DisplayName("Deve retornar lista vazia se usuário não encontrado ao buscar unidades por perfil")
        void deveRetornarListaVaziaSeUsuarioNaoEncontradoAoBuscarUnidadesPorPerfil() {
            when(usuarioRepo.findByIdWithAtribuicoes("user")).thenReturn(Optional.empty());
            
            assertThat(service.buscarUnidadesPorPerfil("user", "GESTOR")).isEmpty();
        }

        @Test
        @DisplayName("Deve retornar false se usuário não encontrado ao verificar perfil")
        void deveRetornarFalseSeUsuarioNaoEncontradoAoVerificarPerfil() {
            when(usuarioRepo.findByIdWithAtribuicoes("user")).thenReturn(Optional.empty());
            
            assertThat(service.usuarioTemPerfil("user", "GESTOR", 1L)).isFalse();
        }

        @Test
        @DisplayName("Deve retornar empty se usuário não encontrado ao buscar perfis")
        void deveRetornarEmptySeUsuarioNaoEncontradoAoBuscarPerfis() {
            when(usuarioRepo.findByIdWithAtribuicoes("user")).thenReturn(Optional.empty());
            
            assertThat(service.buscarPerfisUsuario("user")).isEmpty();
        }
    }

    @Nested
    @DisplayName("Busca de Responsáveis")
    class BuscaResponsaveis {

        @Test
        @DisplayName("Deve lançar erro se chefe não encontrado na busca simples")
        void deveLancarErroSeChefeNaoEncontradoNaBuscaSimples() {
            Unidade unidade = new Unidade("Nome", "SIGLA");
            ReflectionTestUtils.setField(unidade, "codigo", 1L);
            when(unidadeRepo.findBySigla("SIGLA")).thenReturn(Optional.of(unidade));
            when(usuarioRepo.chefePorCodUnidade(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.buscarResponsavelAtual("SIGLA"))
                    .isInstanceOf(ErroEntidadeNaoEncontrada.class);
        }

        @Test
        @DisplayName("Deve lançar erro se chefe não encontrado na busca completa")
        void deveLancarErroSeChefeNaoEncontradoNaBuscaCompleta() {
            Unidade unidade = new Unidade("Nome", "SIGLA");
            ReflectionTestUtils.setField(unidade, "codigo", 1L);
            when(unidadeRepo.findBySigla("SIGLA")).thenReturn(Optional.of(unidade));

            Usuario chefeSimples = new Usuario();
            chefeSimples.setTituloEleitoral("user");
            when(usuarioRepo.chefePorCodUnidade(1L)).thenReturn(Optional.of(chefeSimples));
            when(usuarioRepo.findByIdWithAtribuicoes("user")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.buscarResponsavelAtual("SIGLA"))
                    .isInstanceOf(ErroEntidadeNaoEncontrada.class);
        }

        @Test
        @DisplayName("Deve retornar DTO com titular e substituto")
        void deveRetornarDtoComTitularESubstituto() {
            Usuario t = new Usuario();
            t.setTituloEleitoral("t");
            t.setNome("T");
            Usuario s = new Usuario();
            s.setTituloEleitoral("s");
            s.setNome("S");

            when(usuarioRepo.findChefesByUnidadesCodigos(anyList())).thenReturn(List.of(t, s));

            Optional<ResponsavelDto> res = service.buscarResponsavelUnidade(1L);
            
            assertThat(res).isPresent();
            assertThat(res.get().getTitularTitulo()).isEqualTo("t");
            assertThat(res.get().getSubstitutoTitulo()).isEqualTo("s");
        }

        @Test
        @DisplayName("Deve retornar empty se não houver chefe")
        void deveRetornarEmptySeNaoHouverChefe() {
            when(usuarioRepo.findChefesByUnidadesCodigos(anyList())).thenReturn(Collections.emptyList());
            
            assertThat(service.buscarResponsavelUnidade(1L)).isEmpty();
        }

        @Test
        @DisplayName("Deve retornar mapa vazio se não há chefes nas unidades")
        void deveRetornarMapaVazioSeNaoHaChefesNasUnidades() {
            when(usuarioRepo.findChefesByUnidadesCodigos(anyList())).thenReturn(Collections.emptyList());
            
            assertThat(service.buscarResponsaveisUnidades(List.of(1L))).isEmpty();
        }
    }

    @Nested
    @DisplayName("Busca de Unidades")
    class BuscaUnidades {

        @Test
        @DisplayName("Deve retornar unidade por código")
        void deveRetornarUnidadePorCodigo() {
            Unidade unidade = new Unidade("Nome", "Sigla");
            ReflectionTestUtils.setField(unidade, "codigo", 1L);
            ReflectionTestUtils.setField(unidade, "tipo", TipoUnidade.OPERACIONAL);
            when(unidadeRepo.findById(1L)).thenReturn(Optional.of(unidade));
            
            assertThat(service.buscarUnidadePorCodigo(1L)).isPresent();
        }

        @Test
        @DisplayName("Deve retornar empty se unidade não encontrada por código")
        void deveRetornarEmptySeUnidadeNaoEncontradaPorCodigo() {
            when(unidadeRepo.findById(1L)).thenReturn(Optional.empty());
            
            assertThat(service.buscarUnidadePorCodigo(1L)).isEmpty();
        }

        @Test
        @DisplayName("Deve retornar unidade por sigla")
        void deveRetornarUnidadePorSigla() {
            Unidade unidade = new Unidade("Nome", "S");
            ReflectionTestUtils.setField(unidade, "codigo", 1L);
            ReflectionTestUtils.setField(unidade, "tipo", TipoUnidade.OPERACIONAL);
            when(unidadeRepo.findBySigla("S")).thenReturn(Optional.of(unidade));
            
            assertThat(service.buscarUnidadePorSigla("S")).isPresent();
        }

        @Test
        @DisplayName("Deve retornar empty se unidade não encontrada por sigla")
        void deveRetornarEmptySeUnidadeNaoEncontradaPorSigla() {
            when(unidadeRepo.findBySigla("S")).thenReturn(Optional.empty());
            
            assertThat(service.buscarUnidadePorSigla("S")).isEmpty();
        }

        @Test
        @DisplayName("Deve buscar unidades ativas chamando service")
        void deveBuscarUnidadesAtivasChamandoService() {
            service.buscarUnidadesAtivas();
            
            verify(unidadeRepo).findAllWithHierarquia();
        }

        @Test
        @DisplayName("Deve mapear lista de subunidades")
        void deveMapearListaDeSubunidades() {
            Unidade u = new Unidade("Nome", "Sigla");
            ReflectionTestUtils.setField(u, "codigo", 2L);
            ReflectionTestUtils.setField(u, "tipo", TipoUnidade.OPERACIONAL);

            when(unidadeRepo.findByUnidadeSuperiorCodigo(1L)).thenReturn(List.of(u));
            
            var res = service.buscarSubunidades(1L);
            
            assertThat(res).hasSize(1);
        }

        @Test
        @DisplayName("Deve construir árvore hierárquica chamando service")
        void deveConstruirArvoreHierarquicaChamandoService() {
            service.construirArvoreHierarquica();
            
            verify(unidadeRepo).findAllWithHierarquia();
        }
    }

    // ========== ADMINISTRAÇÃO DE USUÁRIOS ==========

    @Nested
    @DisplayName("Administração")
    class Administracao {

        @Test
        @DisplayName("Deve ignorar administradores inexistentes ao listar")
        void deveIgnorarAdministradoresInexistentesAoListar() {
            Administrador admin = new Administrador("user");
            when(administradorRepo.findAll()).thenReturn(List.of(admin));
            when(usuarioRepo.findById("user")).thenReturn(Optional.empty());

            assertThat(service.listarAdministradores()).isEmpty();
        }

        @Test
        @DisplayName("Deve lançar erro ao adicionar administrador que já existe")
        void deveLancarErroAoAdicionarAdministradorQueJaExiste() {
            Usuario u = new Usuario();
            u.setTituloEleitoral("user");
            when(usuarioRepo.findById("user")).thenReturn(Optional.of(u));
            when(administradorRepo.existsById("user")).thenReturn(true);

            assertThatThrownBy(() -> service.adicionarAdministrador("user"))
                    .isInstanceOf(ErroValidacao.class);
        }

        @Test
        @DisplayName("Deve adicionar administrador com sucesso")
        void deveAdicionarAdministradorComSucesso() {
            Usuario u = new Usuario();
            u.setTituloEleitoral("user");
            when(usuarioRepo.findById("user")).thenReturn(Optional.of(u));
            when(administradorRepo.existsById("user")).thenReturn(false);

            service.adicionarAdministrador("user");
            
            verify(administradorRepo).save(any());
        }

        @Test
        @DisplayName("Deve lançar erro ao remover não-administrador")
        void deveLancarErroAoRemoverNaoAdministrador() {
            when(administradorRepo.existsById("user")).thenReturn(false);
            
            assertThatThrownBy(() -> service.removerAdministrador("user", "other"))
                    .isInstanceOf(ErroValidacao.class);
        }

        @Test
        @DisplayName("Deve lançar erro ao remover a si mesmo")
        void deveLancarErroAoRemoverASiMesmo() {
            when(administradorRepo.existsById("user")).thenReturn(true);
            
            assertThatThrownBy(() -> service.removerAdministrador("user", "user"))
                    .isInstanceOf(ErroValidacao.class);
        }

        @Test
        @DisplayName("Deve lançar erro ao remover único administrador")
        void deveLancarErroAoRemoverUnicoAdministrador() {
            when(administradorRepo.existsById("user")).thenReturn(true);
            when(administradorRepo.count()).thenReturn(1L);
            
            assertThatThrownBy(() -> service.removerAdministrador("user", "other"))
                    .isInstanceOf(ErroValidacao.class);
        }

        @Test
        @DisplayName("Deve remover administrador com sucesso")
        void deveRemoverAdministradorComSucesso() {
            when(administradorRepo.existsById("user")).thenReturn(true);
            when(administradorRepo.count()).thenReturn(2L);

            service.removerAdministrador("user", "other");
            
            verify(administradorRepo).deleteById("user");
        }

        @Test
        @DisplayName("Deve verificar se é administrador")
        void deveVerificarSeEhAdministrador() {
            when(administradorRepo.existsById("user")).thenReturn(true);
            
            assertThat(service.isAdministrador("user")).isTrue();
        }
    }

    // ========== GAPS DE COBERTURA ==========

    @Nested
    @DisplayName("Gaps de Cobertura")
    class GapsCobertura {

        @Test
        @DisplayName("Deve retornar imediatamente se lista de usuários for vazia")
        void deveRetornarSeListaVazia() {
            Map<Long, ResponsavelDto> resultado = service.buscarResponsaveisUnidades(Collections.emptyList());
            
            assertTrue(resultado.isEmpty());
            verifyNoInteractions(usuarioPerfilRepo);
        }

        @Test
        @DisplayName("Deve lidar com chefes, substitutos e unidades sem chefes")
        void deveLidarComChefesESubstitutos() {
            Long unidadeCod = 1L;
            List<Long> unidades = List.of(unidadeCod);
            
            Usuario u1 = new Usuario();
            u1.setTituloEleitoral("1");
            u1.setNome("Titular");
            
            Usuario u2 = new Usuario();
            u2.setTituloEleitoral("2");
            u2.setNome("Substituto");

            Unidade unidade = new Unidade();
            unidade.setCodigo(unidadeCod);

            UsuarioPerfil p1 = UsuarioPerfil.builder()
                    .usuario(u1)
                    .usuarioTitulo("1")
                    .perfil(Perfil.CHEFE)
                    .unidade(unidade)
                    .unidadeCodigo(unidadeCod)
                    .build();
            UsuarioPerfil p2 = UsuarioPerfil.builder()
                    .usuario(u2)
                    .usuarioTitulo("2")
                    .perfil(Perfil.CHEFE)
                    .unidade(unidade)
                    .unidadeCodigo(unidadeCod)
                    .build();
            
            u1.setAtribuicoes(new HashSet<>(List.of(p1)));
            u2.setAtribuicoes(new HashSet<>(List.of(p2)));

            when(usuarioRepo.findChefesByUnidadesCodigos(unidades)).thenReturn(List.of(u1, u2));
            when(usuarioRepo.findByIdInWithAtribuicoes(any())).thenReturn(List.of(u1, u2));
            when(usuarioPerfilRepo.findByUsuarioTituloIn(any())).thenReturn(List.of(p1, p2));

            Map<Long, ResponsavelDto> resultado = service.buscarResponsaveisUnidades(unidades);

            assertFalse(resultado.isEmpty());
            ResponsavelDto resp = resultado.get(unidadeCod);
            assertEquals("1", resp.getTitularTitulo());
            assertEquals("2", resp.getSubstitutoTitulo());
        }

        @Test
        @DisplayName("Deve lidar com usuário sem unidade de lotação")
        void deveLidarComUsuarioSemLotacao() {
            Usuario u = new Usuario();
            u.setTituloEleitoral("123");
            u.setUnidadeLotacao(null);

            // Via buscarUsuariosPorTitulos (toUsuarioDto)
            when(usuarioRepo.findAllById(any())).thenReturn(List.of(u));
            var map = service.buscarUsuariosPorTitulos(List.of("123"));
            assertNull(map.get("123").getUnidadeCodigo());

            // Via listarAdministradores (toAdministradorDto)
            Administrador admin = new Administrador("123");
            when(administradorRepo.findAll()).thenReturn(List.of(admin));
            when(usuarioRepo.findById("123")).thenReturn(Optional.of(u));
            var admins = service.listarAdministradores();
            assertNull(admins.get(0).getUnidadeCodigo());
        }

        @Test
        @DisplayName("Deve falhar ao obter usuário autenticado sem contexto")
        void deveFalharSemContexto() {
            org.springframework.security.core.context.SecurityContextHolder.clearContext();
            
            assertThrows(ErroAccessoNegado.class, () -> service.obterUsuarioAutenticado());
        }

        @Test
        @DisplayName("Deve falhar se autenticação existir mas não estiver autenticado")
        void deveFalharSeNaoAutenticado() {
            org.springframework.security.core.Authentication auth = mock(org.springframework.security.core.Authentication.class);
            when(auth.isAuthenticated()).thenReturn(false);
            org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth);

            assertThrows(ErroAccessoNegado.class, () -> service.obterUsuarioAutenticado());
        }

        @Test
        @DisplayName("Deve falhar se autenticação for Anônima")
        void deveFalharSeAnonimo() {
            org.springframework.security.authentication.AnonymousAuthenticationToken auth =
                mock(org.springframework.security.authentication.AnonymousAuthenticationToken.class);
            when(auth.isAuthenticated()).thenReturn(true); // Mesmo se true, é anônimo
            org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth);

            assertThrows(ErroAccessoNegado.class, () -> service.obterUsuarioAutenticado());
        }

        @Test
        @DisplayName("Deve filtrar atribuições de outras unidades na busca de responsáveis")
        void deveFiltrarOutrasUnidades() {
            Long codAlvo = 1L;
            Long codOutro = 2L;
            List<Long> solicitados = List.of(codAlvo);

            Usuario chefe = new Usuario();
            chefe.setTituloEleitoral("1");
            chefe.setNome("Chefe");

            // Atribuição na unidade alvo
            UsuarioPerfil p1 = new UsuarioPerfil();
            p1.setUsuarioTitulo("1");
            p1.setPerfil(Perfil.CHEFE);
            p1.setUnidadeCodigo(codAlvo);
            p1.setUsuario(chefe);

            // Atribuição em outra unidade (não solicitada)
            UsuarioPerfil p2 = new UsuarioPerfil();
            p2.setUsuarioTitulo("1");
            p2.setPerfil(Perfil.CHEFE);
            p2.setUnidadeCodigo(codOutro);
            p2.setUsuario(chefe);

            chefe.setAtribuicoes(new HashSet<>(List.of(p1, p2)));

            when(usuarioRepo.findChefesByUnidadesCodigos(solicitados)).thenReturn(List.of(chefe));
            when(usuarioRepo.findByIdInWithAtribuicoes(any())).thenReturn(List.of(chefe));
            // carregarAtribuicoesEmLote vai ser chamado
            when(usuarioPerfilRepo.findByUsuarioTituloIn(any())).thenReturn(List.of(p1, p2));

            Map<Long, ResponsavelDto> res = service.buscarResponsaveisUnidades(solicitados);

            assertTrue(res.containsKey(codAlvo));
            assertFalse(res.containsKey(codOutro));
        }

        @Test
        @DisplayName("Deve retornar null se usuário for null em toAdministradorDto")
        void deveRetornarNullParaAdminNull() {
            var result = (AdministradorDto) ReflectionTestUtils.invokeMethod(
                    service, "toAdministradorDto", (Object) null);
            
            assertNull(result);
        }

        @Test
        @DisplayName("Deve retornar null se unidade lotação for null em toAdministradorDto")
        void deveRetornarNullSeUnidadeLotacaoForNull() {
            Usuario u = new Usuario();
            u.setTituloEleitoral("user");
            u.setUnidadeLotacao(null);

            AdministradorDto dto = ReflectionTestUtils.invokeMethod(service, "toAdministradorDto", u);

            assertThat(dto).isNotNull();
            assertThat(dto.getUnidadeCodigo()).isNull();
        }

        @Test
        @DisplayName("Deve extrair título de diferentes tipos de principal")
        void deveExtrairTitulo() {
            // String direto
            assertEquals("123", service.extractTituloUsuario("123"));
            
            // Usuario objeto
            Usuario u = new Usuario();
            u.setTituloEleitoral("456");
            assertEquals("456", service.extractTituloUsuario(u));
            
            // Objeto genérico (toString)
            assertEquals("789", service.extractTituloUsuario(new Object() {
                @Override
                public String toString() { 
                    return "789"; 
                }
            }));
            
            // Null
            assertNull(service.extractTituloUsuario(null));
        }
    }
}
