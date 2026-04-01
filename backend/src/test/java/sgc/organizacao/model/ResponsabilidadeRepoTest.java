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
@DisplayName("ResponsabilidadeRepo - Testes de Repositório")
class ResponsabilidadeRepoTest {

    @Autowired
    private ResponsabilidadeRepo responsabilidadeRepo;

    @Test
    @DisplayName("deve buscar responsabilidades por unidades")
    void deveBuscarResponsabilidadesPorUnidades() {
        assertThat(responsabilidadeRepo.listarPorCodigosUnidade(List.of(8L, 10L)))
                .extracting(Responsabilidade::getUnidadeCodigo)
                .containsExactlyInAnyOrder(8L, 10L);
    }

    @Test
    @DisplayName("deve buscar responsabilidades por usuario")
    void deveBuscarResponsabilidadesPorUsuario() {
        assertThat(responsabilidadeRepo.findByUsuarioTitulo("3"))
                .singleElement()
                .satisfies(responsabilidade -> assertThat(responsabilidade.getUnidadeCodigo()).isEqualTo(8L));
    }
}
