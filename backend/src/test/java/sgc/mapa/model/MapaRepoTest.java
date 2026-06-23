package sgc.mapa.model;

import org.hibernate.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.context.*;
import org.springframework.transaction.annotation.*;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
@Tag("integration")
@DisplayName("MapaRepo - Testes de Repositório")
class MapaRepoTest {

    @Autowired
    private MapaRepo mapaRepo;

    @Test
    @DisplayName("deve buscar mapa vigente por unidade")
    void deveBuscarMapaVigentePorUnidade() {
        Mapa mapa = mapaRepo.buscarMapaVigentePorUnidade(8L).orElseThrow();

        assertThat(mapa.getCodigo()).isEqualTo(1001L);
        assertThat(mapa.getSubprocesso().getCodigo()).isEqualTo(60000L);
    }

    @Test
    @DisplayName("deve buscar mapa por subprocesso")
    void deveBuscarMapaPorSubprocesso() {
        Mapa mapa = mapaRepo.buscarPorSubprocesso(60004L).orElseThrow();

        assertThat(mapa.getCodigo()).isEqualTo(1004L);
        assertThat(mapa.getSubprocesso().getCodigo()).isEqualTo(60004L);
    }

    @Test
    @DisplayName("deve buscar mapa completo por subprocesso com relacionamentos carregados")
    void deveBuscarMapaCompletoPorSubprocessoComRelacionamentosCarregados() {
        Mapa mapa = mapaRepo.buscarCompletoPorSubprocesso(60004L).orElseThrow();

        assertThat(mapa.getCodigo()).isEqualTo(1004L);
        assertThat(Hibernate.isInitialized(mapa.getAtividades())).isTrue();
        assertThat(Hibernate.isInitialized(mapa.getCompetencias())).isTrue();

        Atividade atividade = mapa.getAtividades().iterator().next();
        Competencia competencia = mapa.getCompetencias().iterator().next();

        assertThat(atividade.getCodigo()).isEqualTo(30000L);
        assertThat(Hibernate.isInitialized(atividade.getConhecimentos())).isTrue();
        assertThat(Hibernate.isInitialized(atividade.getCompetencias())).isTrue();
        assertThat(atividade.getConhecimentos())
                .extracting(Conhecimento::getCodigo)
                .containsExactly(40000L);
        assertThat(atividade.getCompetencias())
                .extracting(Competencia::getCodigo)
                .containsExactly(10007L);

        assertThat(competencia.getCodigo()).isEqualTo(10007L);
        assertThat(Hibernate.isInitialized(competencia.getAtividades())).isTrue();
        assertThat(competencia.getAtividades())
                .extracting(Atividade::getCodigo)
                .containsExactly(30000L);
    }

    @Test
    @DisplayName("deve buscar mapa por subprocesso com competencias e atividades carregadas")
    void deveBuscarMapaPorSubprocessoComCompetenciasEAtividadesCarregadas() {
        Mapa mapa = mapaRepo.buscarComCompetenciasEAtividadesPorSubprocesso(60004L).orElseThrow();

        assertThat(mapa.getCodigo()).isEqualTo(1004L);
        assertThat(Hibernate.isInitialized(mapa.getCompetencias())).isTrue();

        Competencia competencia = mapa.getCompetencias().iterator().next();
        assertThat(Hibernate.isInitialized(competencia.getAtividades())).isTrue();
        assertThat(competencia.getAtividades())
                .extracting(Atividade::getCodigo)
                .containsExactly(30000L);
    }
}
