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
@DisplayName("AtividadeRepo - Testes de Repositório")
class AtividadeRepoTest {

    @Autowired
    private AtividadeRepo atividadeRepo;

    @Autowired
    private CompetenciaRepo competenciaRepo;

    @Test
    @DisplayName("deve listar atividades com mapa carregado no findAll")
    void deveListarAtividadesComMapaCarregadoNoFindAll() {
        List<Atividade> atividades = atividadeRepo.findAll();

        assertThat(atividades)
                .extracting(Atividade::getCodigo)
                .contains(17001L, 30000L, 30001L);
        assertThat(atividades)
                .allSatisfy(atividade -> assertThat(Hibernate.isInitialized(atividade.getMapa())).isTrue());
    }

    @Test
    @DisplayName("deve buscar atividades de um mapa com competencias carregadas")
    void deveBuscarAtividadesDeUmMapaComCompetenciasCarregadas() {
        List<Atividade> atividades = atividadeRepo.findByMapa_Codigo(1004L);

        assertThat(atividades).hasSize(1);
        Atividade atividade = atividades.getFirst();

        assertThat(atividade.getCodigo()).isEqualTo(30000L);
        assertThat(Hibernate.isInitialized(atividade.getCompetencias())).isTrue();
        assertThat(atividade.getCompetencias())
                .extracting(Competencia::getCodigo)
                .containsExactly(10007L);
    }

    @Test
    @DisplayName("deve buscar atividades de um mapa sem carregar relacionamentos")
    void deveBuscarAtividadesDeUmMapaSemCarregarRelacionamentos() {
        List<Atividade> atividades = atividadeRepo.listarPorMapaSemRelacionamentos(1004L);

        assertThat(atividades).hasSize(1);
        Atividade atividade = atividades.getFirst();

        assertThat(atividade.getCodigo()).isEqualTo(30000L);
        assertThat(Hibernate.isInitialized(atividade.getCompetencias())).isFalse();
    }

    @Test
    @DisplayName("deve buscar atividades com conhecimentos carregados por mapa")
    void deveBuscarAtividadesComConhecimentosCarregadosPorMapa() {
        List<Atividade> atividades = atividadeRepo.listarPorMapaComConhecimentos(1004L);

        assertThat(atividades).hasSize(1);
        Atividade atividade = atividades.getFirst();

        assertThat(Hibernate.isInitialized(atividade.getConhecimentos())).isTrue();
        assertThat(atividade.getConhecimentos())
                .extracting(Conhecimento::getDescricao)
                .containsExactly("Atendimento ao público");
    }

    @Test
    @DisplayName("deve buscar atividades por subprocesso")
    void deveBuscarAtividadesPorSubprocesso() {
        List<Atividade> atividades = atividadeRepo.listarPorCodigoSubprocesso(60004L);

        assertThat(atividades)
                .extracting(Atividade::getCodigo)
                .containsExactly(30000L);
    }

    @Test
    @DisplayName("deve listar atividades por competencia")
    void deveListarAtividadesPorCompetencia() {
        Competencia competencia = competenciaRepo.findById(10007L).orElseThrow();

        List<Atividade> atividades = atividadeRepo.listarPorCompetencia(competencia);

        assertThat(atividades)
                .extracting(Atividade::getCodigo)
                .containsExactly(30000L);
        assertThat(atividades)
                .allSatisfy(atividade -> assertThat(Hibernate.isInitialized(atividade.getCompetencias())).isTrue());
    }
}
