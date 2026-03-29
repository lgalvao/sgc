package sgc.organizacao.model;

import org.hibernate.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.context.*;
import org.springframework.transaction.annotation.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

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
        assertThat(unidadeRepo.findSiglasByCodigos(List.of(8L, 9L, 10L)))
                .containsExactlyInAnyOrder("SEDESENV", "SEDIA", "SESEL");

        Unidade unidade = unidadeRepo.findByCodigoComResponsavel(8L).orElseThrow();

        assertThat(Hibernate.isInitialized(unidade.getResponsabilidade())).isTrue();
        assertThat(unidade.getResponsabilidade().getUsuarioTitulo()).isEqualTo("3");
        assertThat(unidadeRepo.findByUnidadeSuperiorCodigo(6L))
                .extracting(Unidade::getCodigo)
                .contains(8L, 9L, 10L);
    }
}
