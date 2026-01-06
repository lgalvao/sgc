package sgc.organizacao;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.organizacao.model.*;
import sgc.seguranca.GerenciadorJwt;
import sgc.seguranca.autenticacao.AcessoAdClient;
import sgc.organizacao.dto.ResponsavelDto;
import sgc.seguranca.dto.EntrarReq;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UsuarioService - Gaps de Cobertura")
class UsuarioServiceGapsTest {

    @Mock private UsuarioRepo usuarioRepo;
    @Mock private UsuarioPerfilRepo usuarioPerfilRepo;
    @Mock private AdministradorRepo administradorRepo;
    @Mock private GerenciadorJwt gerenciadorJwt;
    @Mock private AcessoAdClient acessoAdClient;
    @Mock private UnidadeService unidadeService;

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
    @DisplayName("Linhas 423-424: Deve falhar se unidade ou perfil não baterem na autorização")
    void deveFalharSeUnidadeNaoBaterNoEntrar() {
        // Simular autenticação prévia com sucesso
        when(acessoAdClient.autenticar(anyString(), anyString())).thenReturn(true);
        usuarioService.autenticar("123", "senha");
        
        Usuario u = new Usuario();
        u.setTituloEleitoral("123");
        Unidade unidade = new Unidade();
        unidade.setCodigo(1L);
        unidade.setSigla("U1");
        unidade.setTipo(TipoUnidade.OPERACIONAL);
        
        UsuarioPerfil p = UsuarioPerfil.builder()
                .usuario(u)
                .usuarioTitulo("123")
                .perfil(Perfil.CHEFE)
                .unidade(unidade)
                .unidadeCodigo(1L)
                .build();
        u.setAtribuicoes(new HashSet<>(List.of(p)));

        when(usuarioRepo.findByIdWithAtribuicoes("123")).thenReturn(Optional.of(u));
        when(usuarioPerfilRepo.findByUsuarioTitulo("123")).thenReturn(List.of(p));

        // Tentando entrar como GESTOR na unidade 1 (ele é CHEFE)
        EntrarReq req = new EntrarReq("123", "GESTOR", 1L);
        assertThrows(sgc.comum.erros.ErroAccessoNegado.class, () -> usuarioService.entrar(req));

        // RE-AUTENTICA pois o entrar() remove do cache mesmo se falhar na autorização (segurança)
        usuarioService.autenticar("123", "senha");
        
        // Tentando entrar como CHEFE na unidade 2 (ele é da 1)
        EntrarReq req2 = new EntrarReq("123", "CHEFE", 2L);
        assertThrows(sgc.comum.erros.ErroAccessoNegado.class, () -> usuarioService.entrar(req2));
    }

    @Test
    @DisplayName("Linha 448: Deve logar e filtrar administrador não encontrado na base de usuários")
    void deveFiltrarAdminNaoEncontrado() {
        Administrador admin = new Administrador("999");
        when(administradorRepo.findAll()).thenReturn(List.of(admin));
        when(usuarioRepo.findById("999")).thenReturn(Optional.empty());

        var admins = usuarioService.listarAdministradores();
        assertTrue(admins.isEmpty());
    }
}
