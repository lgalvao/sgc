package sgc.organizacao.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("unit")
@DisplayName("Entidade: Usuario")
class UsuarioTest {

    @Test
    @DisplayName("Deve retornar atribuições permanentes")
    void deveRetornarAtribuicoesPermanentes() {
        Usuario usuario = new Usuario();
        Set<UsuarioPerfil> permanentes = new HashSet<>();
        UsuarioPerfil perfil = new UsuarioPerfil();
        perfil.setPerfil(Perfil.ADMIN);
        permanentes.add(perfil);

        assertThat(usuario.getTodasAtribuicoes(permanentes)).containsExactly(perfil);
    }

    @Test
    @DisplayName("Deve incluir atribuições temporárias vigentes")
    void deveIncluirAtribuicoesTemporariasVigentes() {
        Usuario usuario = new Usuario();
        usuario.setTituloEleitoral("12345");
        usuario.setAtribuicoesTemporarias(new HashSet<>());

        Unidade unidade = new Unidade();
        unidade.setCodigo(1L);

        AtribuicaoTemporaria temp = new AtribuicaoTemporaria();
        temp.setPerfil(Perfil.GESTOR);
        temp.setUnidade(unidade);
        temp.setDataInicio(LocalDateTime.now().minusDays(1));
        temp.setDataTermino(LocalDateTime.now().plusDays(1));

        usuario.getAtribuicoesTemporarias().add(temp);

        Set<UsuarioPerfil> todas = usuario.getTodasAtribuicoes(new HashSet<>());
        assertThat(todas).hasSize(1);
        assertThat(todas.iterator().next().getPerfil()).isEqualTo(Perfil.GESTOR);
    }

    @Test
    @DisplayName("Deve ignorar atribuições temporárias expiradas")
    void deveIgnorarAtribuicoesTemporariasExpiradas() {
        Usuario usuario = new Usuario();
        usuario.setTituloEleitoral("12345");
        usuario.setAtribuicoesTemporarias(new HashSet<>());

        AtribuicaoTemporaria temp = new AtribuicaoTemporaria();
        temp.setPerfil(Perfil.GESTOR);
        Unidade unidade = new Unidade();
        unidade.setCodigo(1L);
        temp.setUnidade(unidade);
        temp.setDataInicio(LocalDateTime.now().minusDays(10));
        temp.setDataTermino(LocalDateTime.now().minusDays(1));

        usuario.getAtribuicoesTemporarias().add(temp);

        assertThat(usuario.getTodasAtribuicoes(new HashSet<>())).isEmpty();
    }

    @Test
    @DisplayName("Deve ignorar atribuições temporárias futuras")
    void deveIgnorarAtribuicoesTemporariasFuturas() {
        Usuario usuario = new Usuario();
        usuario.setTituloEleitoral("12345");
        usuario.setAtribuicoesTemporarias(new HashSet<>());

        AtribuicaoTemporaria temp = new AtribuicaoTemporaria();
        temp.setPerfil(Perfil.GESTOR);
        Unidade unidade = new Unidade();
        unidade.setCodigo(1L);
        temp.setUnidade(unidade);
        temp.setDataInicio(LocalDateTime.now().plusDays(1));
        temp.setDataTermino(LocalDateTime.now().plusDays(10));

        usuario.getAtribuicoesTemporarias().add(temp);

        assertThat(usuario.getTodasAtribuicoes(new HashSet<>())).isEmpty();
    }

    @Test
    @DisplayName("Deve funcionar quando não há atribuições")
    void deveFuncionarSemAtribuicoes() {
        Usuario usuario = new Usuario();
        usuario.setTituloEleitoral("12345");
        usuario.setAtribuicoesTemporarias(new HashSet<>());
        assertThat(usuario.getTodasAtribuicoes(new HashSet<>())).isEmpty();
    }

    @Test
    @DisplayName("Deve implementar equals e hashCode baseado no título eleitoral")
    void deveImplementarEqualsHashCode() {
        Usuario u1 = Usuario.builder().tituloEleitoral("123").build();
        Usuario u2 = Usuario.builder().tituloEleitoral("123").build();
        Usuario u3 = Usuario.builder().tituloEleitoral("456").build();

        assertThat(u1)
                .isEqualTo(u2)
                .isNotEqualTo(u3)
                .hasSameHashCodeAs(u2);
        assertThat(u1.hashCode()).isNotEqualTo(u3.hashCode());
    }

    @Test
    @DisplayName("Deve mapear perfil para authority")
    void deveMapearPerfilParaAuthority() {
        Usuario usuario = new Usuario();
        usuario.setTituloEleitoral("123");
        usuario.setAuthorities(Set.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

        var authorities = usuario.getAuthorities();
        assertThat(authorities).extracting("authority").containsExactly("ROLE_ADMIN");
    }
    @Test
    @DisplayName("Deve retornar todas as atribuições (permanentes + temporárias ativas)")
    void deveRetornarTodasAtribuicoesComLogicaComplexa() {
        Usuario usuario = new Usuario();
        usuario.setTituloEleitoral("123");

        Unidade u1 = Unidade.builder().codigo(1L).build();
        Unidade u2 = Unidade.builder().codigo(2L).build();
        Unidade u3 = Unidade.builder().codigo(3L).build();

        // Atribuição Permanente
        UsuarioPerfil up1 = new UsuarioPerfil()
                .setUnidade(u1)
                .setPerfil(Perfil.ADMIN);
        Set<UsuarioPerfil> permanentes = Set.of(up1);

        // Atribuição Temporária ATIVA (hoje entre inicio e termino)
        AtribuicaoTemporaria tempAtiva = AtribuicaoTemporaria.builder()
                .unidade(u2)
                .perfil(Perfil.GESTOR)
                .dataInicio(LocalDateTime.now().minusDays(1))
                .dataTermino(LocalDateTime.now().plusDays(1))
                .build();

        // Atribuição Temporária INATIVA (já expirou)
        AtribuicaoTemporaria tempExpirada = AtribuicaoTemporaria.builder()
                .unidade(u3)
                .perfil(Perfil.CHEFE)
                .dataInicio(LocalDateTime.now().minusDays(10))
                .dataTermino(LocalDateTime.now().minusDays(1))
                .build();

        usuario.setAtribuicoesTemporarias(new HashSet<>(Set.of(tempAtiva, tempExpirada)));

        Set<UsuarioPerfil> todas = usuario.getTodasAtribuicoes(permanentes);

        assertThat(todas).hasSize(2);
        assertThat(todas.stream().anyMatch(p -> p.getPerfil() == Perfil.ADMIN)).isTrue();
        assertThat(todas.stream().anyMatch(p -> p.getPerfil() == Perfil.GESTOR)).isTrue();
    }
}