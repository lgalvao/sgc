package sgc.organizacao.model;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.context.*;
import org.springframework.transaction.annotation.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
@Tag("integration")
@DisplayName("UnidadeMapaRepo - Testes de Repositório")
class UnidadeMapaRepoTest {

    @Autowired
    private UnidadeMapaRepo unidadeMapaRepo;

    @Test
    @DisplayName("deve listar codigos de unidade com mapa vigente")
    void deveListarCodigosDeUnidadeComMapaVigente() {
        List<Long> codigos = unidadeMapaRepo.findAllUnidadeCodigos();

        assertThat(codigos).contains(8L, 9L, 10L, 102L);
    }

    @Test
    @DisplayName("deve buscar e verificar existencia por unidade codigo")
    void deveBuscarEVerificarExistenciaPorUnidadeCodigo() {
        UnidadeMapa unidadeMapa = unidadeMapaRepo.findByUnidadeCodigo(8L).orElseThrow();

        assertThat(unidadeMapaRepo.existsByUnidadeCodigo(8L)).isTrue();
        assertThat(unidadeMapaRepo.existsByUnidadeCodigo(999L)).isFalse();
        assertThat(unidadeMapa.getUnidadeCodigoPersistido()).isEqualTo(8L);
        assertThat(unidadeMapa.getMapaVigente()).isNotNull();
        assertThat(unidadeMapa.getMapaVigente().getCodigo()).isEqualTo(1001L);
    }

    @Test
    @DisplayName("deve buscar varios registros por lista de unidades")
    void deveBuscarVariosRegistrosPorListaDeUnidades() {
        List<UnidadeMapa> registros = unidadeMapaRepo.findAllByUnidadeCodigoIn(List.of(8L, 10L, 904L));

        assertThat(registros)
                .extracting(UnidadeMapa::getUnidadeCodigoPersistido)
                .containsExactlyInAnyOrder(8L, 10L, 904L);
    }
}
