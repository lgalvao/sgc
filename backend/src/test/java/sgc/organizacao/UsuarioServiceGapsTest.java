package sgc.organizacao;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.organizacao.model.*;
import sgc.organizacao.dto.ResponsavelDto;

import sgc.seguranca.login.ClienteAcessoAd;
import sgc.seguranca.login.LoginService;

import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UsuarioService - Gaps de Cobertura")
class UsuarioServiceGapsTest {

    @Mock private UsuarioRepo usuarioRepo;
    @Mock private UsuarioPerfilRepo usuarioPerfilRepo;
    @Mock private AdministradorRepo administradorRepo;
    @Mock private UnidadeService unidadeService;
    @Mock private ClienteAcessoAd clienteAcessoAd;
    @Mock private LoginService loginService;

    @InjectMocks
    private UsuarioService usuarioService;

    @Test
    @DisplayName("Linha 139: Deve retornar imediatamente se lista de usuários for vazia")
    void deveRetornarSeListaVazia() {
        // O método carregarAtribuicoesEmLote é privado, mas é chamado por buscarResponsaveisUnidades
        Map<Long, ResponsavelDto> resultado = usuarioService.buscarResponsaveisUnidades(Collections.emptyList());
        assertTrue(resultado.isEmpty());
        verifyNoInteractions(usuarioPerfilRepo);
    }

    @Test
    @DisplayName("Linhas 226, 237, 324, 330-331: Deve lidar com chefes, substitutos e unidades sem chefes")
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

        // Act
        Map<Long, ResponsavelDto> resultado = usuarioService.buscarResponsaveisUnidades(unidades);

        // Assert
        assertFalse(resultado.isEmpty());
        ResponsavelDto resp = resultado.get(unidadeCod);
        assertEquals("1", resp.getTitularTitulo());
        assertEquals("2", resp.getSubstitutoTitulo()); // Cobre linhas 324, 330-331
    }

    @Test
    @DisplayName("Linhas 296, 506-507: Deve lidar com usuário sem unidade de lotação")
    void deveLidarComUsuarioSemLotacao() {
        Usuario u = new Usuario();
        u.setTituloEleitoral("123");
        u.setUnidadeLotacao(null); // Lotacao null

        // Act & Assert para toUsuarioDto (via buscarUsuariosPorTitulos que usa findAllById)
        when(usuarioRepo.findAllById(any())).thenReturn(List.of(u));
        var map = usuarioService.buscarUsuariosPorTitulos(List.of("123"));
        assertNull(map.get("123").getUnidadeCodigo());

        // Act & Assert para toAdministradorDto (via listarAdministradores)
        Administrador admin = new Administrador("123");
        when(administradorRepo.findAll()).thenReturn(List.of(admin));
        when(usuarioRepo.findById("123")).thenReturn(Optional.of(u));
        var admins = usuarioService.listarAdministradores();
        assertNull(admins.get(0).getUnidadeCodigo());
    }

    @Test
    @DisplayName("Linha 94: Deve falhar ao obter usuário autenticado sem contexto")
    void deveFalharSemContexto() {
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
        assertThrows(sgc.comum.erros.ErroAccessoNegado.class, () -> usuarioService.obterUsuarioAutenticado());
    }

    @Test
    @DisplayName("Linha 386: deve retornar null se usuário for null em toAdministradorDto")
    void deveRetornarNullParaAdminNull() {
        var result = (sgc.organizacao.dto.AdministradorDto) ReflectionTestUtils.invokeMethod(usuarioService, "toAdministradorDto", (Object)null);
        assertNull(result);
    }

    @Test
    @DisplayName("Linhas 401, 404: Deve extrair título de diferentes tipos de principal")
    void deveExtrairTitulo() {
        assertEquals("123", usuarioService.extractTituloUsuario("123")); // Linha 401
        
        Usuario u = new Usuario();
        u.setTituloEleitoral("456");
        assertEquals("456", usuarioService.extractTituloUsuario(u)); // Linha 403 (implicitamente cobre 404 se não for nem String nem Usuario)
        
        assertEquals("789", usuarioService.extractTituloUsuario(new Object() {
            @Override
            public String toString() { return "789"; }
        })); // Linha 404
        
        assertNull(usuarioService.extractTituloUsuario(null)); // Linha 404 default
    }
}
