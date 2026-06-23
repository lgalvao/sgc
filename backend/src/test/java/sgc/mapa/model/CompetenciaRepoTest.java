package sgc.mapa.model;

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
@DisplayName("CompetenciaRepo - Testes de Repositório")
class CompetenciaRepoTest {

    @Autowired
    private CompetenciaRepo competenciaRepo;

    @Test
    @DisplayName("deve buscar competencias de um mapa com atividades carregadas")
    void deveBuscarCompetenciasDeUmMapaComAtividadesCarregadas() {
        List<Competencia> competencias = competenciaRepo.findByMapa_Codigo(1004L);

        assertThat(competencias).hasSize(1);
        Competencia competencia = competencias.getFirst();

        assertThat(competencia.getCodigo()).isEqualTo(10007L);
        assertThat(Hibernate.isInitialized(competencia.getAtividades())).isTrue();
        assertThat(competencia.getAtividades())
                .extracting(Atividade::getCodigo)
                .containsExactly(30000L);
    }

    @Test
    @DisplayName("deve projetar competencia e codigo de atividade por mapa")
    void deveProjetarCompetenciaECodigoDeAtividadePorMapa() {
        List<Object[]> linhas = competenciaRepo.listarCodigosCompetenciaEAtividadePorMapa(1004L);

        assertThat(linhas).hasSize(1);
        assertThat(linhas.getFirst())
                .containsExactly(10007L, "Gestão Administrativa", 30000L);
    }

    @Test
    @DisplayName("deve manter atividade nula na projecao quando competencia nao possui associacao")
    void deveManterAtividadeNulaNaProjecaoQuandoCompetenciaNaoPossuiAssociacao() {
        List<Object[]> linhas = competenciaRepo.listarCodigosCompetenciaEAtividadePorMapa(1003L);

        assertThat(linhas).hasSize(2);
        assertThat(linhas)
                .extracting(linha -> linha[2])
                .containsOnlyNulls();
    }

    @Test
    @DisplayName("deve buscar competencias sem carregar atividades")
    void deveBuscarCompetenciasSemCarregarAtividades() {
        List<Competencia> competencias = competenciaRepo.listarPorMapaSemRelacionamentos(1004L);

        assertThat(competencias).hasSize(1);
        Competencia competencia = competencias.getFirst();

        assertThat(competencia.getCodigo()).isEqualTo(10007L);
        assertThat(Hibernate.isInitialized(competencia.getAtividades())).isFalse();
    }

    @Test
    @DisplayName("deve remover competencias por mapa")
    void deveRemoverCompetenciasPorMapa() {
        assertThat(competenciaRepo.listarPorMapaSemRelacionamentos(1003L)).hasSize(2);

        competenciaRepo.deleteByMapa_Codigo(1003L);
        competenciaRepo.flush();

        assertThat(competenciaRepo.listarPorMapaSemRelacionamentos(1003L)).isEmpty();
    }
}
