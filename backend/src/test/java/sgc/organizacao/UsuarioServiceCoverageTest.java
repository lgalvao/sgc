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
import sgc.organizacao.model.*;
import sgc.seguranca.AcessoAdClient;
import sgc.seguranca.GerenciadorJwt;
import sgc.seguranca.dto.EntrarReq;
import java.util.*;

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
    @DisplayName("autorizar: erro se não autenticado recentemente")
    void autorizar_ErroNaoAutenticado() {
        assertThatThrownBy(() -> service.autorizar("user"))
                .isInstanceOf(ErroAutenticacao.class);
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
}
