package sgc.alerta.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@Tag("integration")
@DisplayName("AlertaUsuarioRepo - Testes de Repositório")
class AlertaUsuarioRepoTest {

    @Autowired
    private AlertaUsuarioRepo alertaUsuarioRepo;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("deve buscar associacoes por usuario e alertas")
    void deveBuscarAssociacoesPorUsuarioEAlertas() {
        jdbcTemplate.update(
                "INSERT INTO SGC.ALERTA_USUARIO (alerta_codigo, usuario_titulo, data_hora_leitura) VALUES (?, ?, ?)",
                70002L,
                "8",
                Timestamp.valueOf(LocalDateTime.of(2025, 1, 2, 10, 0))
        );

        List<AlertaUsuario> encontrados = alertaUsuarioRepo.listarPorUsuarioEAlertas("8", List.of(70002L, 70003L));

        assertThat(encontrados).singleElement().satisfies(alertaUsuario -> {
            assertThat(alertaUsuario.getCodigo().getAlertaCodigo()).isEqualTo(70002L);
            assertThat(alertaUsuario.getCodigo().getUsuarioTitulo()).isEqualTo("8");
            assertThat(alertaUsuario.getDataHoraLeitura()).isEqualTo(LocalDateTime.of(2025, 1, 2, 10, 0));
        });
    }
}
