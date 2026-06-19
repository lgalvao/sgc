package sgc.organizacao.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@Tag("integration")
@DisplayName("AtribuicaoTemporariaRepo - Testes de Repositório")
class AtribuicaoTemporariaRepoTest {

    @Autowired
    private AtribuicaoTemporariaRepo atribuicaoTemporariaRepo;

    @Autowired
    private UnidadeRepo unidadeRepo;

    @Test
    @DisplayName("deve salvar atribuicao temporaria")
    void deveSalvarAtribuicaoTemporaria() {
        AtribuicaoTemporaria atribuicao = AtribuicaoTemporaria.builder()
                .unidade(unidadeRepo.findById(8L).orElseThrow())
                .usuarioTitulo("1")
                .usuarioMatricula("00000001")
                .dataInicio(LocalDateTime.of(2025, 1, 10, 8, 0))
                .dataTermino(LocalDateTime.of(2025, 1, 20, 18, 0))
                .justificativa("Cobertura temporaria")
                .build();

        AtribuicaoTemporaria salva = atribuicaoTemporariaRepo.save(atribuicao);

        assertThat(atribuicaoTemporariaRepo.findById(salva.getCodigo())).get().satisfies(item -> {
            assertThat(item.getUsuarioTitulo()).isEqualTo("1");
            assertThat(item.getUnidade().getCodigo()).isEqualTo(8L);
        });
    }
}
