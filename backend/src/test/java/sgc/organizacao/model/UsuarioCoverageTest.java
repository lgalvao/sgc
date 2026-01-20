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
        u.setAtribuicoes(Set.of(permanente));
        
        LocalDateTime agora = LocalDateTime.now();

        // Atribuição temporária futura
        AtribuicaoTemporaria tempFutura = new AtribuicaoTemporaria();
        tempFutura.setDataInicio(agora.plusDays(1));
        
        // Atribuição temporária passada
        AtribuicaoTemporaria tempPassada = new AtribuicaoTemporaria();
        tempPassada.setDataTermino(agora.minusDays(1));
        
        // Atribuição temporária ativa (sem data fim)
        AtribuicaoTemporaria tempAtiva = new AtribuicaoTemporaria();
        tempAtiva.setDataInicio(agora.minusDays(1));
        Unidade un2 = new Unidade(); un2.setCodigo(2L);
        tempAtiva.setUnidade(un2);
        tempAtiva.setPerfil(Perfil.CHEFE);
        
        // Atribuição temporária com datas null
        AtribuicaoTemporaria tempNull = new AtribuicaoTemporaria();
        Unidade un3 = new Unidade(); un3.setCodigo(3L);
        tempNull.setUnidade(un3);
        tempNull.setPerfil(Perfil.SERVIDOR);
        
        u.setAtribuicoesTemporarias(new HashSet<>(Set.of(tempFutura, tempPassada, tempAtiva, tempNull)));
        
        Set<UsuarioPerfil> todas = u.getTodasAtribuicoes();
        
        // Deve conter: permanente, tempAtiva e tempNull
        assertThat(todas).hasSize(3);
    }
}
