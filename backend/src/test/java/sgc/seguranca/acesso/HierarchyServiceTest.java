package sgc.seguranca.acesso;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.organizacao.model.Unidade;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do HierarchyService")
class HierarchyServiceTest {

    @InjectMocks
    private HierarchyService hierarchyService;

    @Test
    @DisplayName("Deve retornar false quando unidades são nulas")
    void deveRetornarFalseQuandoUnidadesSaoNulas() {
        assertThat(hierarchyService.isSubordinada(null, null)).isFalse();
        assertThat(hierarchyService.isSubordinada(criarUnidade(1L, null), null)).isFalse();
        assertThat(hierarchyService.isSubordinada(null, criarUnidade(1L, null))).isFalse();
    }

    @Test
    @DisplayName("Deve retornar false quando unidade alvo não tem superior")
    void deveRetornarFalseQuandoUnidadeAlvoNaoTemSuperior() {
        Unidade alvo = criarUnidade(1L, null);
        Unidade superior = criarUnidade(2L, null);
        
        assertThat(hierarchyService.isSubordinada(alvo, superior)).isFalse();
    }

    @Test
    @DisplayName("Deve retornar true quando unidade é diretamente subordinada")
    void deveRetornarTrueQuandoUnidadeDiretamenteSubordinada() {
        Unidade superior = criarUnidade(1L, null);
        Unidade alvo = criarUnidade(2L, superior);
        
        assertThat(hierarchyService.isSubordinada(alvo, superior)).isTrue();
    }

    @Test
    @DisplayName("Deve retornar true quando unidade é indiretamente subordinada")
    void deveRetornarTrueQuandoUnidadeIndiretamenteSubordinada() {
        Unidade raiz = criarUnidade(1L, null);
        Unidade intermediaria = criarUnidade(2L, raiz);
        Unidade alvo = criarUnidade(3L, intermediaria);
        
        assertThat(hierarchyService.isSubordinada(alvo, raiz)).isTrue();
    }

    @Test
    @DisplayName("Deve retornar false quando unidade não é subordinada")
    void deveRetornarFalseQuandoUnidadeNaoSubordinada() {
        Unidade raiz1 = criarUnidade(1L, null);
        Unidade raiz2 = criarUnidade(2L, null);
        Unidade alvo = criarUnidade(3L, raiz1);
        
        assertThat(hierarchyService.isSubordinada(alvo, raiz2)).isFalse();
    }

    @Test
    @DisplayName("Deve retornar true para mesma unidade em isMesmaOuSubordinada")
    void deveRetornarTrueParaMesmaUnidade() {
        Unidade unidade = criarUnidade(1L, null);
        
        assertThat(hierarchyService.isMesmaOuSubordinada(unidade, unidade)).isTrue();
    }

    @Test
    @DisplayName("Deve retornar true para unidade subordinada em isMesmaOuSubordinada")
    void deveRetornarTrueParaUnidadeSubordinadaEmIsMesmaOuSubordinada() {
        Unidade superior = criarUnidade(1L, null);
        Unidade alvo = criarUnidade(2L, superior);
        
        assertThat(hierarchyService.isMesmaOuSubordinada(alvo, superior)).isTrue();
    }

    @Test
    @DisplayName("Deve retornar false para unidades nulas em isMesmaOuSubordinada")
    void deveRetornarFalseParaUnidadesNulasEmIsMesmaOuSubordinada() {
        assertThat(hierarchyService.isMesmaOuSubordinada(null, null)).isFalse();
        assertThat(hierarchyService.isMesmaOuSubordinada(criarUnidade(1L, null), null)).isFalse();
    }

    @Test
    @DisplayName("Deve retornar true quando é superior imediata")
    void deveRetornarTrueQuandoSuperiorImediata() {
        Unidade superior = criarUnidade(1L, null);
        Unidade alvo = criarUnidade(2L, superior);
        
        assertThat(hierarchyService.isSuperiorImediata(alvo, superior)).isTrue();
    }

    @Test
    @DisplayName("Deve retornar false quando não é superior imediata")
    void deveRetornarFalseQuandoNaoSuperiorImediata() {
        Unidade raiz = criarUnidade(1L, null);
        Unidade intermediaria = criarUnidade(2L, raiz);
        Unidade alvo = criarUnidade(3L, intermediaria);
        
        assertThat(hierarchyService.isSuperiorImediata(alvo, raiz)).isFalse();
    }

    @Test
    @DisplayName("Deve retornar lista vazia para buscarSubordinadas (implementação básica)")
    void deveRetornarListaVaziaParaBuscarSubordinadas() {
        Unidade unidade = criarUnidade(1L, null);
        
        assertThat(hierarchyService.buscarSubordinadas(unidade)).isEmpty();
    }

    @Test
    @DisplayName("Deve retornar lista com código para buscarCodigosHierarquia")
    void deveRetornarListaComCodigoParaBuscarCodigosHierarquia() {
        Long codigo = 1L;
        
        assertThat(hierarchyService.buscarCodigosHierarquia(codigo))
                .hasSize(1)
                .contains(codigo);
    }

    @Test
    @DisplayName("Deve retornar lista vazia para código nulo em buscarCodigosHierarquia")
    void deveRetornarListaVaziaParaCodigoNulo() {
        assertThat(hierarchyService.buscarCodigosHierarquia(null)).isEmpty();
    }

    private Unidade criarUnidade(Long codigo, Unidade superior) {
        Unidade unidade = new Unidade();
        unidade.setCodigo(codigo);
        unidade.setUnidadeSuperior(superior);
        return unidade;
    }
}
