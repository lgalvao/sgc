package sgc.organizacao.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("unit")
@DisplayName("Testes de Cobertura para Usuario")
class UsuarioCoverageTest {

    @Test
    @DisplayName("Deve filtrar atribuições temporárias por data")
    void deveFiltrarAtribuicoesTemporarias() {
        Usuario u = new Usuario();
        u.setTituloEleitoral("123");

        UsuarioPerfil permanente = new UsuarioPerfil();
        permanente.setUnidadeCodigo(1L);
        permanente.setPerfil(Perfil.GESTOR);

        LocalDateTime agora = LocalDateTime.now();

        // Atribuição temporária futura
        AtribuicaoTemporaria tempFutura = new AtribuicaoTemporaria();
        tempFutura.setDataInicio(agora.plusDays(1));
        tempFutura.setDataTermino(agora.plusDays(2));

        // Atribuição temporária passada
        AtribuicaoTemporaria tempPassada = new AtribuicaoTemporaria();
        tempPassada.setDataInicio(agora.minusDays(5));
        tempPassada.setDataTermino(agora.minusDays(1));

        // Atribuição temporária ativa
        AtribuicaoTemporaria tempAtiva = new AtribuicaoTemporaria();
        tempAtiva.setDataInicio(agora.minusDays(1));
        tempAtiva.setDataTermino(agora.plusDays(1));
        Unidade un2 = new Unidade();
        un2.setCodigo(2L);
        tempAtiva.setUnidade(un2);
        tempAtiva.setPerfil(Perfil.CHEFE);

        // Atribuição temporária ativa 2
        AtribuicaoTemporaria tempAtiva2 = new AtribuicaoTemporaria();
        tempAtiva2.setDataInicio(agora.minusDays(1));
        tempAtiva2.setDataTermino(agora.plusDays(1));
        Unidade un3 = new Unidade();
        un3.setCodigo(3L);
        tempAtiva2.setUnidade(un3);
        tempAtiva2.setPerfil(Perfil.SERVIDOR);

        u.setAtribuicoesTemporarias(new HashSet<>(Set.of(tempFutura, tempPassada, tempAtiva, tempAtiva2)));

        Set<UsuarioPerfil> todas = u.getTodasAtribuicoes(new HashSet<>(Set.of(permanente)));

        // Deve conter: permanente, tempAtiva e tempAtiva2
        assertThat(todas).hasSize(3);
    }

    @Test
    @DisplayName("Deve comparar usuários corretamente")
    void deveCompararUsuariosCorretamente() {
        Usuario u1 = new Usuario();
        u1.setTituloEleitoral("123");
        Usuario u2 = new Usuario();
        u2.setTituloEleitoral("123");
        Usuario u3 = new Usuario();
        u3.setTituloEleitoral("456");

        assertThat(u1).isEqualTo(u1);
        assertThat(u1).isEqualTo(u2);
        assertThat(u1).isNotEqualTo(u3);
        assertThat(u1).isNotNull();
        assertThat(u1).isNotEqualTo(new Object());

        assertThat(u1.hashCode()).isEqualTo(u2.hashCode());
        assertThat(u1.hashCode()).isNotEqualTo(u3.hashCode());
    }

    @Test
    @DisplayName("Deve lidar com atribuições temporárias nulas")
    void deveLidarComAtribuicoesTemporariasNulas() {
        Usuario u = new Usuario();
        u.setTituloEleitoral("123");
        u.setAtribuicoesTemporarias(null); // Explicitamente nulo

        // Deve retornar apenas as permanentes (vazias neste caso) sem lançar NPE
        assertThat(u.getTodasAtribuicoes(new HashSet<>())).isEmpty();
    }
}
