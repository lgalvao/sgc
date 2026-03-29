package sgc.alerta.model;

import org.hibernate.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.context.*;
import org.springframework.data.domain.*;
import org.springframework.transaction.annotation.*;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
@Tag("integration")
@DisplayName("AlertaRepo - Testes de Repositório")
class AlertaRepoTest {

    @Autowired
    private AlertaRepo alertaRepo;

    @Test
    @DisplayName("deve buscar alertas por processo")
    void deveBuscarAlertasPorProcesso() {
        assertThat(alertaRepo.findByProcessoCodigo(50000L))
                .extracting(Alerta::getCodigo)
                .contains(70000L, 70002L, 70003L);
    }

    @Test
    @DisplayName("deve buscar alertas exclusivos do usuario com relacionamentos carregados")
    void deveBuscarAlertasExclusivosDoUsuarioComRelacionamentosCarregados() {
        Alerta alerta = alertaRepo.buscarAlertasExclusivosDoUsuario("8").getFirst();

        assertThat(alerta.getCodigo()).isEqualTo(70002L);
        assertThat(Hibernate.isInitialized(alerta.getProcesso())).isTrue();
        assertThat(Hibernate.isInitialized(alerta.getUnidadeOrigem())).isTrue();
        assertThat(Hibernate.isInitialized(alerta.getUnidadeDestino())).isTrue();
    }

    @Test
    @DisplayName("deve paginar alertas da unidade e individuais")
    void devePaginarAlertasDaUnidadeEIndividuais() {
        Page<Alerta> pagina = alertaRepo.buscarAlertasDaUnidadeEIndividuais(6L, "8", PageRequest.of(0, 10));

        assertThat(pagina.getContent())
                .extracting(Alerta::getCodigo)
                .contains(70002L, 70003L);
    }
}
