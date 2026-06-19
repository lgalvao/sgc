package sgc.mapa.model;

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
@DisplayName("ConhecimentoRepo - Testes de Repositório")
class ConhecimentoRepoTest {

    @Autowired
    private ConhecimentoRepo conhecimentoRepo;

    @Test
    @DisplayName("deve buscar conhecimentos por atividade")
    void deveBuscarConhecimentosPorAtividade() {
        List<Conhecimento> conhecimentos = conhecimentoRepo.findByAtividade_Codigo(30000L);

        assertThat(conhecimentos)
                .extracting(Conhecimento::getCodigo)
                .containsExactly(40000L);
        assertThat(conhecimentos)
                .extracting(Conhecimento::getDescricao)
                .containsExactly("Atendimento ao público");
    }

    @Test
    @DisplayName("deve buscar conhecimentos por mapa")
    void deveBuscarConhecimentosPorMapa() {
        List<Conhecimento> conhecimentos = conhecimentoRepo.listarPorMapa(201L);

        assertThat(conhecimentos)
                .extracting(Conhecimento::getCodigo)
                .containsExactly(40001L);
        assertThat(conhecimentos)
                .extracting(Conhecimento::getDescricao)
                .containsExactly("Atendimento ao público");
    }

    @Test
    @DisplayName("deve retornar vazio quando o mapa nao possui conhecimentos")
    void deveRetornarVazioQuandoOMapaNaoPossuiConhecimentos() {
        List<Conhecimento> conhecimentos = conhecimentoRepo.listarPorMapa(1003L);

        assertThat(conhecimentos).isEmpty();
    }
}
