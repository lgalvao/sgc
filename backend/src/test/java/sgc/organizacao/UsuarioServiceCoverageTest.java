package sgc.organizacao;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import sgc.comum.erros.ErroAccessoNegado;
import sgc.comum.erros.ErroAutenticacao;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.comum.erros.ErroValidacao;
import sgc.organizacao.dto.ResponsavelDto;
import sgc.organizacao.dto.UsuarioDto;
import sgc.organizacao.model.*;
import sgc.seguranca.GerenciadorJwt;
import sgc.seguranca.autenticacao.AcessoAdClient;
import sgc.seguranca.dto.EntrarReq;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceCoverageTest {

    @InjectMocks
    private UsuarioService service;

    @Mock private UsuarioRepo usuarioRepo;
    @Mock private UsuarioPerfilRepo usuarioPerfilRepo;
    @Mock private AdministradorRepo administradorRepo;
    @Mock private GerenciadorJwt gerenciadorJwt;
    @Mock private AcessoAdClient acessoAdClient;
    @Mock private UnidadeService unidadeService;

    // --- MÉTODOS DE BUSCA E MAPEAMENTO ---

    @Test
    @DisplayName("carregarUsuarioParaAutenticacao: deve retornar nulo se usuario nao encontrado")
    void carregarUsuarioParaAutenticacao_Nulo() {
        when(usuarioRepo.findByIdWithAtribuicoes("user")).thenReturn(Optional.empty());
        assertThat(service.carregarUsuarioParaAutenticacao("user")).isNull();
    }

    @Test
    @DisplayName("carregarUsuarioParaAutenticacao: deve carregar atribuicoes se encontrado")
    void carregarUsuarioParaAutenticacao_Sucesso() {
        Usuario usuario = mock(Usuario.class);
        when(usuario.getTituloEleitoral()).thenReturn("user");
        when(usuarioRepo.findByIdWithAtribuicoes("user")).thenReturn(Optional.of(usuario));
        when(usuarioPerfilRepo.findByUsuarioTitulo("user")).thenReturn(Collections.emptyList());

        Usuario result = service.carregarUsuarioParaAutenticacao("user");

        assertThat(result).isNotNull();
        verify(usuario).getAuthorities(); // Garante que authorities foram chamadas
        verify(usuario).setAtribuicoes(any());
    }

    @Test
    @DisplayName("buscarUsuarioPorTitulo: empty se nao encontrado")
    void buscarUsuarioPorTitulo_Empty() {
        when(usuarioRepo.findById("user")).thenReturn(Optional.empty());
        assertThat(service.buscarUsuarioPorTitulo("user")).isEmpty();
    }

    @Test
    @DisplayName("buscarUsuarioPorTitulo: retorna dto se encontrado")
    void buscarUsuarioPorTitulo_Sucesso() {
        Usuario u = new Usuario();
        u.setTituloEleitoral("user");
        when(usuarioRepo.findById("user")).thenReturn(Optional.of(u));

        Optional<UsuarioDto> res = service.buscarUsuarioPorTitulo("user");
        assertThat(res).isPresent();
        assertThat(res.get().getTituloEleitoral()).isEqualTo("user");
    }

    @Test
    @DisplayName("buscarUsuariosPorUnidade: lista vazia")
    void buscarUsuariosPorUnidade_Vazia() {
        when(usuarioRepo.findByUnidadeLotacaoCodigo(1L)).thenReturn(Collections.emptyList());
        assertThat(service.buscarUsuariosPorUnidade(1L)).isEmpty();
    }

    @Test
    @DisplayName("buscarEntidadePorId: lança erro se nao encontrado")
    void buscarPorId_Erro() {
        when(usuarioRepo.findById("user")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.buscarPorId("user"))
            .isInstanceOf(ErroEntidadeNaoEncontrada.class);
    }

    @Test
    @DisplayName("buscarUsuarioPorLogin: lança erro se nao encontrado")
    void buscarPorLogin_Erro() {
        when(usuarioRepo.findByIdWithAtribuicoes("user")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.buscarPorLogin("user"))
            .isInstanceOf(ErroEntidadeNaoEncontrada.class);
    }

    @Test
    @DisplayName("buscarUsuarioPorLogin: sucesso")
    void buscarPorLogin_Sucesso() {
        Usuario u = new Usuario();
        u.setTituloEleitoral("user");
        when(usuarioRepo.findByIdWithAtribuicoes("user")).thenReturn(Optional.of(u));
        when(usuarioPerfilRepo.findByUsuarioTitulo("user")).thenReturn(Collections.emptyList());

        Usuario res = service.buscarPorLogin("user");
        assertThat(res).isNotNull();
    }

    @Test
    @DisplayName("buscarResponsavelVigente: erro se chefe nao encontrado na busca simples")
    void buscarResponsavelAtual_ErroChefeSimples() {
        sgc.organizacao.dto.UnidadeDto dto = sgc.organizacao.dto.UnidadeDto.builder().codigo(1L).build();
        when(unidadeService.buscarPorSigla("SIGLA")).thenReturn(dto);
        when(usuarioRepo.chefePorCodUnidade(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarResponsavelAtual("SIGLA"))
            .isInstanceOf(ErroEntidadeNaoEncontrada.class);
    }

    @Test
    @DisplayName("buscarResponsavelVigente: erro se chefe nao encontrado na busca completa")
    void buscarResponsavelAtual_ErroChefeCompleto() {
        sgc.organizacao.dto.UnidadeDto dto = sgc.organizacao.dto.UnidadeDto.builder().codigo(1L).build();
        when(unidadeService.buscarPorSigla("SIGLA")).thenReturn(dto);

        Usuario chefeSimples = new Usuario();
        chefeSimples.setTituloEleitoral("user");
        when(usuarioRepo.chefePorCodUnidade(1L)).thenReturn(Optional.of(chefeSimples));

        when(usuarioRepo.findByIdWithAtribuicoes("user")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarResponsavelAtual("SIGLA"))
            .isInstanceOf(ErroEntidadeNaoEncontrada.class);
    }

    @Test
    @DisplayName("buscarUsuarioPorEmail: empty se nao encontrado")
    void buscarUsuarioPorEmail_Empty() {
        when(usuarioRepo.findByEmail("email")).thenReturn(Optional.empty());
        assertThat(service.buscarUsuarioPorEmail("email")).isEmpty();
    }

    @Test
    @DisplayName("buscarUsuariosAtivos: lista vazia")
    void buscarUsuariosAtivos_Vazia() {
        when(usuarioRepo.findAll()).thenReturn(Collections.emptyList());
        assertThat(service.buscarUsuariosAtivos()).isEmpty();
    }

    @Test
    @DisplayName("buscarUnidadePorCodigo: retorna unidade")
    void buscarUnidadePorCodigo_Sucesso() {
        sgc.organizacao.dto.UnidadeDto dto = sgc.organizacao.dto.UnidadeDto.builder().codigo(1L).build();
        when(unidadeService.buscarPorCodigo(1L)).thenReturn(dto);
        assertThat(service.buscarUnidadePorCodigo(1L)).isPresent();
    }

    @Test
    @DisplayName("buscarUnidadePorSigla: retorna unidade")
    void buscarUnidadePorSigla_Sucesso() {
        sgc.organizacao.dto.UnidadeDto dto = sgc.organizacao.dto.UnidadeDto.builder().codigo(1L).build();
        when(unidadeService.buscarPorSigla("S")).thenReturn(dto);
        assertThat(service.buscarUnidadePorSigla("S")).isPresent();
    }

    @Test
    @DisplayName("buscarUnidadesAtivas: chama service")
    void buscarUnidadesAtivas() {
        service.buscarUnidadesAtivas();
        verify(unidadeService).buscarTodasUnidades();
    }

    @Test
    @DisplayName("buscarSubunidades: mapeia lista")
    void buscarSubunidades() {
        Unidade u = new Unidade("Nome", "Sigla");
        ReflectionTestUtils.setField(u, "codigo", 2L);
        ReflectionTestUtils.setField(u, "tipo", TipoUnidade.OPERACIONAL);

        when(unidadeService.listarSubordinadas(1L)).thenReturn(List.of(u));
        var res = service.buscarSubunidades(1L);
        assertThat(res).hasSize(1);
    }

    @Test
    @DisplayName("construirArvoreHierarquica: chama service")
    void construirArvoreHierarquica() {
        service.construirArvoreHierarquica();
        verify(unidadeService).buscarArvoreHierarquica();
    }

    @Test
    @DisplayName("buscarResponsavelUnidade: retorna dto com titular e substituto")
    void buscarResponsavelUnidade_ComSubstituto() {
        Usuario t = new Usuario(); t.setTituloEleitoral("t"); t.setNome("T");
        Usuario s = new Usuario(); s.setTituloEleitoral("s"); s.setNome("S");

        when(usuarioRepo.findChefesByUnidadesCodigos(anyList())).thenReturn(List.of(t, s));

        Optional<ResponsavelDto> res = service.buscarResponsavelUnidade(1L);
        assertThat(res).isPresent();
        assertThat(res.get().getTitularTitulo()).isEqualTo("t");
        assertThat(res.get().getSubstitutoTitulo()).isEqualTo("s");
    }

    @Test
    @DisplayName("buscarResponsaveisUnidades: retorna map vazio se sem chefes")
    void buscarResponsaveisUnidades_Vazio() {
        when(usuarioRepo.findChefesByUnidadesCodigos(anyList())).thenReturn(Collections.emptyList());
        assertThat(service.buscarResponsaveisUnidades(List.of(1L))).isEmpty();
    }

    @Test
    @DisplayName("buscarUsuariosPorTitulos: retorna map")
    void buscarUsuariosPorTitulos() {
        Usuario u = new Usuario(); u.setTituloEleitoral("u");
        when(usuarioRepo.findAllById(anyList())).thenReturn(List.of(u));

        var map = service.buscarUsuariosPorTitulos(List.of("u"));
        assertThat(map).containsKey("u");
    }

    @Test
    @DisplayName("buscarUnidadesOndeEhResponsavel: lista vazia se usuario nao encontrado")
    void buscarUnidadesOndeEhResponsavel_Vazia() {
        when(usuarioRepo.findByIdWithAtribuicoes("user")).thenReturn(Optional.empty());
        assertThat(service.buscarUnidadesOndeEhResponsavel("user")).isEmpty();
    }

    @Test
    @DisplayName("buscarUnidadesPorPerfil: lista vazia se usuario nao encontrado")
    void buscarUnidadesPorPerfil_Vazia() {
        when(usuarioRepo.findByIdWithAtribuicoes("user")).thenReturn(Optional.empty());
        assertThat(service.buscarUnidadesPorPerfil("user", "GESTOR")).isEmpty();
    }

    @Test
    @DisplayName("usuarioTemPerfil: false se usuario nao encontrado")
    void usuarioTemPerfil_False() {
        when(usuarioRepo.findByIdWithAtribuicoes("user")).thenReturn(Optional.empty());
        assertThat(service.usuarioTemPerfil("user", "GESTOR", 1L)).isFalse();
    }

    @Test
    @DisplayName("buscarResponsavelUnidade: empty se nao houver chefe")
    void buscarResponsavelUnidade_Empty() {
        when(usuarioRepo.findChefesByUnidadesCodigos(anyList())).thenReturn(Collections.emptyList());
        assertThat(service.buscarResponsavelUnidade(1L)).isEmpty();
    }

    @Test
    @DisplayName("buscarPerfisUsuario: empty se usuario nao encontrado")
    void buscarPerfisUsuario_Empty() {
        when(usuarioRepo.findByIdWithAtribuicoes("user")).thenReturn(Optional.empty());
        assertThat(service.buscarPerfisUsuario("user")).isEmpty();
    }

    @Test
    @DisplayName("buscarUnidadePorCodigo: empty se erro entidade nao encontrada")
    void buscarUnidadePorCodigo_Empty() {
        when(unidadeService.buscarPorCodigo(1L)).thenThrow(new ErroEntidadeNaoEncontrada("Unidade", 1L));
        assertThat(service.buscarUnidadePorCodigo(1L)).isEmpty();
    }

    @Test
    @DisplayName("buscarUnidadePorSigla: empty se erro entidade nao encontrada")
    void buscarUnidadePorSigla_Empty() {
        when(unidadeService.buscarPorSigla("S")).thenThrow(new ErroEntidadeNaoEncontrada("Unidade", "S"));
        assertThat(service.buscarUnidadePorSigla("S")).isEmpty();
    }

    // --- MÉTODOS DE ADMINISTRAÇÃO ---

    @Test
    @DisplayName("listarAdministradores: deve ignorar se usuario não existe na base")
    void listarAdministradores_IgnoraInexistente() {
        Administrador admin = new Administrador("user");
        when(administradorRepo.findAll()).thenReturn(List.of(admin));
        when(usuarioRepo.findById("user")).thenReturn(Optional.empty());

        assertThat(service.listarAdministradores()).isEmpty();
    }

    @Test
    @DisplayName("adicionarAdministrador: erro se já existe")
    void adicionarAdministrador_ErroJaExiste() {
        Usuario u = new Usuario(); u.setTituloEleitoral("user");
        when(usuarioRepo.findById("user")).thenReturn(Optional.of(u));
        when(administradorRepo.existsById("user")).thenReturn(true);

        assertThatThrownBy(() -> service.adicionarAdministrador("user"))
                .isInstanceOf(ErroValidacao.class);
    }

    @Test
    @DisplayName("adicionarAdministrador: sucesso")
    void adicionarAdministrador_Sucesso() {
        Usuario u = new Usuario(); u.setTituloEleitoral("user");
        when(usuarioRepo.findById("user")).thenReturn(Optional.of(u));
        when(administradorRepo.existsById("user")).thenReturn(false);

        service.adicionarAdministrador("user");
        verify(administradorRepo).save(any());
    }

    @Test
    @DisplayName("removerAdministrador: erro se não é admin")
    void removerAdministrador_ErroNaoEhAdmin() {
        when(administradorRepo.existsById("user")).thenReturn(false);
        assertThatThrownBy(() -> service.removerAdministrador("user", "other"))
                .isInstanceOf(ErroValidacao.class);
    }

    @Test
    @DisplayName("removerAdministrador: erro se remover a si mesmo")
    void removerAdministrador_ErroSiMesmo() {
        when(administradorRepo.existsById("user")).thenReturn(true);
        assertThatThrownBy(() -> service.removerAdministrador("user", "user"))
                .isInstanceOf(ErroValidacao.class);
    }

    @Test
    @DisplayName("removerAdministrador: erro se único admin")
    void removerAdministrador_ErroUnico() {
        when(administradorRepo.existsById("user")).thenReturn(true);
        when(administradorRepo.count()).thenReturn(1L);
        assertThatThrownBy(() -> service.removerAdministrador("user", "other"))
                .isInstanceOf(ErroValidacao.class);
    }

    @Test
    @DisplayName("removerAdministrador: sucesso")
    void removerAdministrador_Sucesso() {
        when(administradorRepo.existsById("user")).thenReturn(true);
        when(administradorRepo.count()).thenReturn(2L);

        service.removerAdministrador("user", "other");
        verify(administradorRepo).deleteById("user");
    }

    @Test
    @DisplayName("isAdministrador: retorna repo")
    void isAdministrador() {
        when(administradorRepo.existsById("user")).thenReturn(true);
        assertThat(service.isAdministrador("user")).isTrue();
    }

    // --- AUTENTICAÇÃO E AUTORIZAÇÃO ---

    @Test
    @DisplayName("autenticar: erro se sem AD e não é teste")
    void autenticar_ErroSemAdProducao() {
        ReflectionTestUtils.setField(service, "acessoAdClient", null);
        ReflectionTestUtils.setField(service, "ambienteTestes", false);

        assertThat(service.autenticar("user", "pass")).isFalse();
    }

    @Test
    @DisplayName("autenticar: sucesso se sem AD, é teste, e usuário existe")
    void autenticar_SucessoSemAdTeste() {
        ReflectionTestUtils.setField(service, "acessoAdClient", null);
        ReflectionTestUtils.setField(service, "ambienteTestes", true);
        when(usuarioRepo.existsById("user")).thenReturn(true);

        assertThat(service.autenticar("user", "pass")).isTrue();
    }

    @Test
    @DisplayName("autenticar: falha se sem AD, é teste, mas usuário não existe")
    void autenticar_FalhaSemAdTesteUsuarioInexistente() {
        ReflectionTestUtils.setField(service, "acessoAdClient", null);
        ReflectionTestUtils.setField(service, "ambienteTestes", true);
        when(usuarioRepo.existsById("user")).thenReturn(false);

        assertThat(service.autenticar("user", "pass")).isFalse();
    }

    @Test
    @DisplayName("autenticar: falha no AD retorna false")
    void autenticar_FalhaAD() {
        when(acessoAdClient.autenticar("user", "pass")).thenThrow(new ErroAutenticacao("Falha"));
        assertThat(service.autenticar("user", "pass")).isFalse();
    }

    @Test
    @DisplayName("autenticar: sucesso no AD")
    void autenticar_SucessoAD() {
        when(acessoAdClient.autenticar("user", "pass")).thenReturn(true);
        assertThat(service.autenticar("user", "pass")).isTrue();
    }

    @Test
    @DisplayName("autorizar: erro se não autenticado recentemente")
    void autorizar_ErroNaoAutenticado() {
        assertThatThrownBy(() -> service.autorizar("user"))
                .isInstanceOf(ErroAutenticacao.class);
    }

    @Test
    @DisplayName("autorizar: sucesso")
    void autorizar_Sucesso() {
         @SuppressWarnings("unchecked")
        Map<String, java.time.LocalDateTime> auths = (Map<String, java.time.LocalDateTime>) ReflectionTestUtils.getField(service, "autenticacoesRecentes");
        auths.put("user", java.time.LocalDateTime.now());

        Usuario u = new Usuario(); u.setTituloEleitoral("user");
        when(usuarioRepo.findByIdWithAtribuicoes("user")).thenReturn(Optional.of(u));
        when(usuarioPerfilRepo.findByUsuarioTitulo("user")).thenReturn(Collections.emptyList());

        assertThat(service.autorizar("user")).isNotNull();
    }

    @Test
    @DisplayName("entrar: erro se sessão expirada ou inválida")
    void entrar_ErroSessaoInvalida() {
        EntrarReq req = EntrarReq.builder().tituloEleitoral("user").build();
        assertThatThrownBy(() -> service.entrar(req))
                .isInstanceOf(ErroAutenticacao.class);
    }

    @Test
    @DisplayName("entrar: erro se unidade não encontrada")
    void entrar_ErroUnidadeNaoEncontrada() {
        // Simular autenticação recente
        @SuppressWarnings("unchecked")
        Map<String, java.time.LocalDateTime> auths = (Map<String, java.time.LocalDateTime>) ReflectionTestUtils.getField(service, "autenticacoesRecentes");
        auths.put("user", java.time.LocalDateTime.now());

        EntrarReq req = EntrarReq.builder().tituloEleitoral("user").unidadeCodigo(1L).build();

        when(unidadeService.buscarEntidadePorId(1L)).thenThrow(new ErroEntidadeNaoEncontrada("Unidade", 1L));

        assertThatThrownBy(() -> service.entrar(req))
                .isInstanceOf(ErroEntidadeNaoEncontrada.class);
    }

    @Test
    @DisplayName("entrar: erro se sem permissão")
    void entrar_ErroSemPermissao() {
        // Simular autenticação recente
        @SuppressWarnings("unchecked")
        Map<String, java.time.LocalDateTime> auths = (Map<String, java.time.LocalDateTime>) ReflectionTestUtils.getField(service, "autenticacoesRecentes");
        auths.put("user", java.time.LocalDateTime.now());

        EntrarReq req = EntrarReq.builder().tituloEleitoral("user").unidadeCodigo(1L).perfil("GESTOR").build();

        when(unidadeService.buscarEntidadePorId(1L)).thenReturn(new Unidade());

        Usuario u = new Usuario();
        u.setTituloEleitoral("user");
        when(usuarioRepo.findByIdWithAtribuicoes("user")).thenReturn(Optional.of(u));
        when(usuarioPerfilRepo.findByUsuarioTitulo("user")).thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> service.entrar(req))
                .isInstanceOf(ErroAccessoNegado.class);
    }

    @Test
    @DisplayName("entrar: sucesso")
    void entrar_Sucesso() {
         // Simular autenticação recente
        @SuppressWarnings("unchecked")
        Map<String, java.time.LocalDateTime> auths = (Map<String, java.time.LocalDateTime>) ReflectionTestUtils.getField(service, "autenticacoesRecentes");
        auths.put("user", java.time.LocalDateTime.now());

        EntrarReq req = EntrarReq.builder().tituloEleitoral("user").unidadeCodigo(1L).perfil("CHEFE").build();

        Unidade unidade = new Unidade();
        ReflectionTestUtils.setField(unidade, "codigo", 1L);
        ReflectionTestUtils.setField(unidade, "nome", "U");
        ReflectionTestUtils.setField(unidade, "sigla", "S");
        ReflectionTestUtils.setField(unidade, "tipo", TipoUnidade.OPERACIONAL);

        when(unidadeService.buscarEntidadePorId(1L)).thenReturn(unidade);

        Usuario u = new Usuario();
        u.setTituloEleitoral("user");
        when(usuarioRepo.findByIdWithAtribuicoes("user")).thenReturn(Optional.of(u));

        UsuarioPerfil up = new UsuarioPerfil();
        ReflectionTestUtils.setField(up, "usuario", u);
        ReflectionTestUtils.setField(up, "unidade", unidade);
        ReflectionTestUtils.setField(up, "perfil", Perfil.CHEFE);
        when(usuarioPerfilRepo.findByUsuarioTitulo("user")).thenReturn(List.of(up));

        when(gerenciadorJwt.gerarToken(eq("user"), eq(Perfil.CHEFE), eq(1L))).thenReturn("token");

        assertThat(service.entrar(req)).isEqualTo("token");
    }

    @Test
    @DisplayName("carregarAtribuicoesEmLote: deve processar lista de usuarios")
    void carregarAtribuicoesEmLote_Sucesso() {
         Usuario u1 = new Usuario(); u1.setTituloEleitoral("u1");
         Usuario u2 = new Usuario(); u2.setTituloEleitoral("u2");

         UsuarioPerfil up = new UsuarioPerfil();
         ReflectionTestUtils.setField(up, "usuarioTitulo", "u1");

         when(usuarioPerfilRepo.findByUsuarioTituloIn(anyList())).thenReturn(List.of(up));

         ReflectionTestUtils.invokeMethod(service, "carregarAtribuicoesEmLote", List.of(u1, u2));

         assertThat(u1.getTodasAtribuicoes()).isNotEmpty();
         assertThat(u2.getTodasAtribuicoes()).isEmpty();
    }

    @Test
    @DisplayName("carregarAtribuicoesEmLote: deve ignorar lista vazia")
    void carregarAtribuicoesEmLote_Vazia() {
        ReflectionTestUtils.invokeMethod(service, "carregarAtribuicoesEmLote", Collections.emptyList());
        verify(usuarioPerfilRepo, never()).findByUsuarioTituloIn(any());
    }

    @Test
    @DisplayName("toAdministradorDto: deve retornar null se unidade lotacao for null")
    void toAdministradorDto_UnidadeNull() {
        Usuario u = new Usuario();
        u.setTituloEleitoral("user");
        u.setUnidadeLotacao(null);

        // Usar reflection para acessar método privado
        sgc.organizacao.dto.AdministradorDto dto = ReflectionTestUtils.invokeMethod(service, "toAdministradorDto", u);

        assertThat(dto).isNotNull();
        assertThat(dto.getUnidadeCodigo()).isNull();
    }
}
