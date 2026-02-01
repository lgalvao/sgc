package sgc.organizacao;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.organizacao.dto.PerfilDto;
import sgc.organizacao.dto.UnidadeResponsavelDto;
import sgc.organizacao.model.*;
import sgc.organizacao.service.AdministradorService;
import sgc.organizacao.service.UnidadeConsultaService;
import sgc.organizacao.service.UsuarioConsultaService;
import sgc.organizacao.service.UsuarioPerfilService;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UsuarioFacade - Testes de Cobertura")
class UsuarioFacadeCoverageTest {

    @Mock
    private UsuarioConsultaService usuarioConsultaService;

    @Mock
    private UsuarioPerfilService usuarioPerfilService;

    @Mock
    private AdministradorService administradorService;

    @Mock
    private UnidadeConsultaService unidadeConsultaService;

    @InjectMocks
    private UsuarioFacade facade;

    @Test
    @DisplayName("buscarResponsaveisUnidades: deve filtrar unidades inativas")
    void buscarResponsaveisUnidades_DeveFiltrarUnidadesInativas() {
        // Arrange
        Long codigoUnidade = 1L;
        String titulo = "123456";
        Usuario chefe = criarUsuario(titulo);
        Unidade unidadeInativa = criarUnidade(codigoUnidade, "UNID1");
        unidadeInativa.setSituacao(SituacaoUnidade.INATIVA); // Unidade Inativa

        UsuarioPerfil atribuicao = criarAtribuicao(chefe, unidadeInativa, Perfil.CHEFE);
        Set<UsuarioPerfil> atribuicoes = Set.of(atribuicao);

        when(usuarioConsultaService.buscarChefesPorUnidades(List.of(codigoUnidade)))
                .thenReturn(List.of(chefe));
        when(usuarioConsultaService.buscarPorIdsComAtribuicoes(List.of(titulo)))
                .thenReturn(List.of(chefe));
        when(usuarioPerfilService.buscarAtribuicoesParaCache(titulo))
                .thenReturn(atribuicoes);

        // Act
        Map<Long, UnidadeResponsavelDto> resultado = facade.buscarResponsaveisUnidades(List.of(codigoUnidade));

        // Assert
        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("buscarResponsaveisUnidades: deve filtrar perfil diferente de CHEFE")
    void buscarResponsaveisUnidades_DeveFiltrarNaoChefe() {
        // Arrange
        Long codigoUnidade = 1L;
        String titulo = "123456";
        Usuario servidor = criarUsuario(titulo);
        Unidade unidade = criarUnidade(codigoUnidade, "UNID1");

        // Perfil SERVIDOR em vez de CHEFE
        UsuarioPerfil atribuicao = criarAtribuicao(servidor, unidade, Perfil.SERVIDOR);
        Set<UsuarioPerfil> atribuicoes = Set.of(atribuicao);

        when(usuarioConsultaService.buscarChefesPorUnidades(List.of(codigoUnidade)))
                .thenReturn(List.of(servidor));
        when(usuarioConsultaService.buscarPorIdsComAtribuicoes(List.of(titulo)))
                .thenReturn(List.of(servidor));
        when(usuarioPerfilService.buscarAtribuicoesParaCache(titulo))
                .thenReturn(atribuicoes);

        // Act
        Map<Long, UnidadeResponsavelDto> resultado = facade.buscarResponsaveisUnidades(List.of(codigoUnidade));

        // Assert
        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("buscarResponsaveisUnidades: deve filtrar unidade diferente da solicitada")
    void buscarResponsaveisUnidades_DeveFiltrarUnidadeDiferente() {
        // Arrange
        Long codigoUnidadeSolicitada = 1L;
        Long codigoOutraUnidade = 2L;

        String titulo = "123456";
        Usuario chefe = criarUsuario(titulo);
        Unidade outraUnidade = criarUnidade(codigoOutraUnidade, "UNID2");

        UsuarioPerfil atribuicao = criarAtribuicao(chefe, outraUnidade, Perfil.CHEFE);
        Set<UsuarioPerfil> atribuicoes = Set.of(atribuicao);

        when(usuarioConsultaService.buscarChefesPorUnidades(List.of(codigoUnidadeSolicitada)))
                .thenReturn(List.of(chefe));
        when(usuarioConsultaService.buscarPorIdsComAtribuicoes(List.of(titulo)))
                .thenReturn(List.of(chefe));
        when(usuarioPerfilService.buscarAtribuicoesParaCache(titulo))
                .thenReturn(atribuicoes);

        // Act
        Map<Long, UnidadeResponsavelDto> resultado = facade.buscarResponsaveisUnidades(List.of(codigoUnidadeSolicitada));

        // Assert
        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("buscarResponsaveisUnidades: deve mapear substituto corretamente")
    void buscarResponsaveisUnidades_DeveMapearSubstituto() {
        // Arrange
        Long codigoUnidade = 1L;
        String tituloTitular = "111";
        String tituloSubstituto = "222";

        Usuario titular = criarUsuario(tituloTitular);
        Usuario substituto = criarUsuario(tituloSubstituto);

        Unidade unidade = criarUnidade(codigoUnidade, "UNID1");

        UsuarioPerfil atribuicaoTitular = criarAtribuicao(titular, unidade, Perfil.CHEFE);
        UsuarioPerfil atribuicaoSubstituto = criarAtribuicao(substituto, unidade, Perfil.CHEFE);

        when(usuarioConsultaService.buscarChefesPorUnidades(List.of(codigoUnidade)))
                .thenReturn(List.of(titular, substituto));

        when(usuarioConsultaService.buscarPorIdsComAtribuicoes(anyList()))
                .thenReturn(List.of(titular, substituto));

        when(usuarioPerfilService.buscarAtribuicoesParaCache(tituloTitular))
                .thenReturn(Set.of(atribuicaoTitular));
        when(usuarioPerfilService.buscarAtribuicoesParaCache(tituloSubstituto))
                .thenReturn(Set.of(atribuicaoSubstituto));

        // Act
        Map<Long, UnidadeResponsavelDto> resultado = facade.buscarResponsaveisUnidades(List.of(codigoUnidade));

        // Assert
        assertThat(resultado).containsKey(codigoUnidade);
        UnidadeResponsavelDto dto = resultado.get(codigoUnidade);

        assertThat(dto.titularTitulo()).isEqualTo(tituloTitular);
        assertThat(dto.substitutoTitulo()).isEqualTo(tituloSubstituto);
    }

    @Test
    @DisplayName("buscarPerfisUsuario: deve filtrar unidades inativas")
    void buscarPerfisUsuario_DeveFiltrarUnidadesInativas() {
        // Arrange
        String titulo = "123456";
        Usuario usuario = criarUsuario(titulo);
        Unidade unidadeInativa = criarUnidade(1L, "UNID1");
        unidadeInativa.setSituacao(SituacaoUnidade.INATIVA);

        UsuarioPerfil atribuicao = criarAtribuicao(usuario, unidadeInativa, Perfil.CHEFE);

        when(usuarioConsultaService.buscarPorIdComAtribuicoesOpcional(titulo))
                .thenReturn(Optional.of(usuario));
        when(usuarioPerfilService.buscarPorUsuario(titulo))
                .thenReturn(List.of(atribuicao));

        // Act
        List<PerfilDto> resultado = facade.buscarPerfisUsuario(titulo);

        // Assert
        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("buscarUnidadesOndeEhResponsavel: deve filtrar unidades inativas e não chefes")
    void buscarUnidadesOndeEhResponsavel_Filtros() {
        // Arrange
        String titulo = "123456";
        Usuario usuario = criarUsuario(titulo);

        Unidade unidadeInativa = criarUnidade(1L, "UNID1");
        unidadeInativa.setSituacao(SituacaoUnidade.INATIVA);

        Unidade unidadeAtivaNaoChefe = criarUnidade(2L, "UNID2");
        unidadeAtivaNaoChefe.setSituacao(SituacaoUnidade.ATIVA);

        UsuarioPerfil atribuicao1 = criarAtribuicao(usuario, unidadeInativa, Perfil.CHEFE);
        UsuarioPerfil atribuicao2 = criarAtribuicao(usuario, unidadeAtivaNaoChefe, Perfil.SERVIDOR);

        when(usuarioConsultaService.buscarPorIdComAtribuicoesOpcional(titulo))
                .thenReturn(Optional.of(usuario));
        when(usuarioPerfilService.buscarPorUsuario(titulo))
                .thenReturn(List.of(atribuicao1, atribuicao2));

        // Act
        List<Long> resultado = facade.buscarUnidadesOndeEhResponsavel(titulo);

        // Assert
        assertThat(resultado).isEmpty();
    }

    // Helpers
    private Usuario criarUsuario(String titulo) {
        Usuario usuario = new Usuario();
        usuario.setTituloEleitoral(titulo);
        usuario.setNome("Usuário Teste");
        usuario.setEmail("usuario@test.com");
        usuario.setMatricula("12345");
        usuario.setUnidadeLotacao(criarUnidade(1L, "UNID1"));
        return usuario;
    }

    private Unidade criarUnidade(Long codigo, String sigla) {
        Unidade unidade = new Unidade();
        unidade.setCodigo(codigo);
        unidade.setSigla(sigla);
        unidade.setNome("Unidade Teste");
        unidade.setSituacao(SituacaoUnidade.ATIVA);
        return unidade;
    }

    private UsuarioPerfil criarAtribuicao(Usuario usuario, Unidade unidade, Perfil perfil) {
        UsuarioPerfil atribuicao = new UsuarioPerfil();
        atribuicao.setUsuario(usuario);
        atribuicao.setUsuarioTitulo(usuario.getTituloEleitoral());
        atribuicao.setUnidade(unidade);
        atribuicao.setUnidadeCodigo(unidade.getCodigo());
        atribuicao.setPerfil(perfil);
        return atribuicao;
    }
}
