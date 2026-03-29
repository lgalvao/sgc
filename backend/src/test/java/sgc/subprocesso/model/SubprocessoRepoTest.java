package sgc.subprocesso.model;

import org.hibernate.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.context.*;
import org.springframework.transaction.annotation.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static sgc.subprocesso.model.SituacaoSubprocesso.*;

@SpringBootTest
@Transactional
@Tag("integration")
@DisplayName("SubprocessoRepo - Testes de Repositório")
class SubprocessoRepoTest {

    @Autowired
    private SubprocessoRepo subprocessoRepo;

    @Test
    @DisplayName("deve listar subprocessos por processo com unidade carregada")
    void deveListarSubprocessosPorProcessoComUnidadeCarregada() {
        List<Subprocesso> resultado = subprocessoRepo.findByProcessoCodigoComUnidade(50002L);

        assertThat(resultado)
                .extracting(Subprocesso::getCodigo)
                .containsExactlyInAnyOrder(60003L, 60004L, 60201L);
        assertThat(resultado)
                .allSatisfy(sp -> assertThat(Hibernate.isInitialized(sp.getUnidade())).isTrue());
    }

    @Test
    @DisplayName("deve buscar subprocesso com mapa e atividades carregados")
    void deveBuscarSubprocessoComMapaEAtividadesCarregados() {
        Subprocesso subprocesso = subprocessoRepo.buscarPorCodigoComMapaEAtividades(60004L).orElseThrow();

        assertThat(subprocesso.getUnidade().getCodigo()).isEqualTo(102L);
        assertThat(subprocesso.getMapa()).isNotNull();
        assertThat(Hibernate.isInitialized(subprocesso.getMapa())).isTrue();
        assertThat(Hibernate.isInitialized(subprocesso.getMapa().getAtividades())).isTrue();
        assertThat(subprocesso.getMapa().getAtividades()).hasSize(1);
    }

    @Test
    @DisplayName("deve contar e verificar existencia por processo e unidades")
    void deveContarEVerificarExistenciaPorProcessoEUnidades() {
        assertThat(subprocessoRepo.existsByProcessoCodigoAndUnidadeCodigoIn(50002L, List.of(10L, 11L))).isTrue();
        assertThat(subprocessoRepo.existsByProcessoCodigoAndUnidadeCodigoIn(50002L, List.of(11L, 12L))).isFalse();
        assertThat(subprocessoRepo.countByProcessoCodigo(50002L)).isEqualTo(3L);
        assertThat(subprocessoRepo.countByProcessoCodigoAndSituacaoIn(50002L, List.of(MAPEAMENTO_MAPA_HOMOLOGADO)))
                .isEqualTo(3L);
    }
}
