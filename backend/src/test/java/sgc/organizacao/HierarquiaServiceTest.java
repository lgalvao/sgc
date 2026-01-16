package sgc.organizacao;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.organizacao.model.Unidade;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("Testes do ServicoHierarquia")
class ServicoHierarquiaTest {

    @InjectMocks
    private ServicoHierarquia servicoHierarquia;

    @Test
    @DisplayName("Deve retornar false quando unidades são nulas")
    void deveRetornarFalseQuandoUnidadesSaoNulas() {
        assertThat(servicoHierarquia.isSubordinada(null, null)).isFalse();
        assertThat(servicoHierarquia.isSubordinada(criarUnidade(1L, null), null)).isFalse();
        assertThat(servicoHierarquia.isSubordinada(null, criarUnidade(1L, null))).isFalse();
    }

    @Test
    @DisplayName("Deve retornar false quando unidade alvo não tem superior")
    void deveRetornarFalseQuandoUnidadeAlvoNaoTemSuperior() {
        Unidade alvo = criarUnidade(1L, null);
        Unidade superior = criarUnidade(2L, null);

        assertThat(servicoHierarquia.isSubordinada(alvo, superior)).isFalse();
    }

    @Test
    @DisplayName("Deve retornar true quando unidade é diretamente subordinada")
    void deveRetornarTrueQuandoUnidadeDiretamenteSubordinada() {
        Unidade superior = criarUnidade(1L, null);
        Unidade alvo = criarUnidade(2L, superior);

        assertThat(servicoHierarquia.isSubordinada(alvo, superior)).isTrue();
    }

    @Test
    @DisplayName("Deve retornar true quando unidade é indiretamente subordinada")
    void deveRetornarTrueQuandoUnidadeIndiretamenteSubordinada() {
        Unidade raiz = criarUnidade(1L, null);
        Unidade intermediaria = criarUnidade(2L, raiz);
        Unidade alvo = criarUnidade(3L, intermediaria);

        assertThat(servicoHierarquia.isSubordinada(alvo, raiz)).isTrue();
    }

    @Test
    @DisplayName("Deve retornar false quando unidade não é subordinada")
    void deveRetornarFalseQuandoUnidadeNaoSubordinada() {
        Unidade raiz1 = criarUnidade(1L, null);
        Unidade raiz2 = criarUnidade(2L, null);
        Unidade alvo = criarUnidade(3L, raiz1);

        assertThat(servicoHierarquia.isSubordinada(alvo, raiz2)).isFalse();
    }

    @Test
    @DisplayName("Deve retornar true para mesma unidade em isMesmaOuSubordinada")
    void deveRetornarTrueParaMesmaUnidade() {
        Unidade unidade = criarUnidade(1L, null);

        assertThat(servicoHierarquia.isMesmaOuSubordinada(unidade, unidade)).isTrue();
    }

    @Test
    @DisplayName("Deve retornar true para unidade subordinada em isMesmaOuSubordinada")
    void deveRetornarTrueParaUnidadeSubordinadaEmIsMesmaOuSubordinada() {
        Unidade superior = criarUnidade(1L, null);
        Unidade alvo = criarUnidade(2L, superior);

        assertThat(servicoHierarquia.isMesmaOuSubordinada(alvo, superior)).isTrue();
    }

    @Test
    @DisplayName("Deve retornar false para unidades nulas em isMesmaOuSubordinada")
    void deveRetornarFalseParaUnidadesNulasEmIsMesmaOuSubordinada() {
        assertThat(servicoHierarquia.isMesmaOuSubordinada(null, null)).isFalse();
        assertThat(servicoHierarquia.isMesmaOuSubordinada(criarUnidade(1L, null), null)).isFalse();
    }

    @Test
    @DisplayName("Deve retornar true quando é superior imediata")
    void deveRetornarTrueQuandoSuperiorImediata() {
        Unidade superior = criarUnidade(1L, null);
        Unidade alvo = criarUnidade(2L, superior);

        assertThat(servicoHierarquia.isSuperiorImediata(alvo, superior)).isTrue();
    }

    @Test
    @DisplayName("Deve retornar false quando não é superior imediata")
    void deveRetornarFalseQuandoNaoSuperiorImediata() {
        Unidade raiz = criarUnidade(1L, null);
        Unidade intermediaria = criarUnidade(2L, raiz);
        Unidade alvo = criarUnidade(3L, intermediaria);

        assertThat(servicoHierarquia.isSuperiorImediata(alvo, raiz)).isFalse();
    }

    private Unidade criarUnidade(Long codigo, Unidade superior) {
        Unidade unidade = new Unidade();
        unidade.setCodigo(codigo);
        unidade.setUnidadeSuperior(superior);
        return unidade;
    }
}
