package sgc.alerta.model;

import org.hibernate.Hibernate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import sgc.organizacao.model.UnidadeRepo;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@Tag("integration")
@DisplayName("AlertaRepo - Testes de Repositório")
class AlertaRepoTest {

    @Autowired
    private AlertaRepo alertaRepo;

    @Autowired
    private UnidadeRepo unidadeRepo;

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
    @DisplayName("deve paginar alertas visiveis para gestao incluindo pessoais e coletivos")
    void devePaginarAlertasVisiveisParaGestao() {
        Page<Alerta> pagina = alertaRepo.buscarAlertasDaGestao(6L, "8", PageRequest.of(0, 10));

        assertThat(pagina.getContent())
                .extracting(Alerta::getCodigo)
                .contains(70003L)
                .contains(70002L);
    }

    @Test
    @DisplayName("deve buscar alerta pessoal sem processo e sem unidade destino")
    void deveBuscarAlertaPessoalSemProcessoESemUnidadeDestino() {
        Alerta alerta = Alerta.builder()
                .unidadeOrigem(unidadeRepo.findById(1L).orElseThrow())
                .usuarioDestinoTitulo("8")
                .descricao("Alerta pessoal")
                .dataHora(java.time.LocalDateTime.now())
                .build();
        Alerta salvo = alertaRepo.save(alerta);

        assertThat(alertaRepo.buscarAlertasExclusivosDoUsuario("8"))
                .extracting(Alerta::getCodigo)
                .contains(salvo.getCodigo());
    }
}
