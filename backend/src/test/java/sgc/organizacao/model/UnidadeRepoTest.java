package sgc.organizacao.model;

import org.hibernate.Hibernate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@Tag("integration")
@DisplayName("UnidadeRepo - Testes de Repositório")
class UnidadeRepoTest {

    @Autowired
    private UnidadeRepo unidadeRepo;

    @Test
    @DisplayName("deve buscar unidade ativa por sigla e por titular")
    void deveBuscarUnidadeAtivaPorSiglaEPorTitular() {
        assertThat(unidadeRepo.findBySigla("SEDESENV")).get().extracting(Unidade::getCodigo).isEqualTo(8L);
        assertThat(unidadeRepo.findByTituloTitular("3"))
                .extracting(Unidade::getCodigo)
                .containsExactly(8L);
    }

    @Test
    @DisplayName("deve buscar siglas e hierarquia com responsavel carregado")
    void deveBuscarSiglasEHierarquiaComResponsavelCarregado() {
        assertThat(unidadeRepo.buscarSiglasPorCodigos(List.of(8L, 9L, 10L)))
                .containsExactlyInAnyOrder("SEDESENV", "SEDIA", "SESEL");

        Unidade unidade = unidadeRepo.buscarPorCodigoComResponsavel(8L).orElseThrow();
        Responsabilidade responsabilidade = unidade.getResponsabilidade();

        assertThat(Hibernate.isInitialized(responsabilidade)).isTrue();
        assertThat(responsabilidade).isNotNull();
        assertThat(responsabilidade.getUsuarioTitulo()).isEqualTo("3");
        assertThat(unidadeRepo.findByUnidadeSuperiorCodigo(6L))
                .extracting(Unidade::getCodigo)
                .contains(8L, 9L, 10L);
    }
}
