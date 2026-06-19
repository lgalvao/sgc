package sgc.organizacao.model;

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
@DisplayName("ResponsabilidadeRepo - Testes de Repositório")
class ResponsabilidadeRepoTest {

    @Autowired
    private ResponsabilidadeRepo responsabilidadeRepo;

    @Test
    @DisplayName("deve buscar responsabilidades por unidades")
    void deveBuscarResponsabilidadesPorUnidades() {
        assertThat(responsabilidadeRepo.listarLeiturasPorCodigosUnidade(List.of(8L, 10L)))
                .extracting(sgc.organizacao.model.ResponsabilidadeLeitura::unidadeCodigo)
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
