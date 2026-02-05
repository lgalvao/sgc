package sgc.organizacao.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Usuario Domain Logic Tests")
class UsuarioCoverageTest {

    @Test
    @DisplayName("Deve retornar todas as atribuições (permanentes + temporárias ativas)")
    void deveRetornarTodasAtribuicoes() {
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
