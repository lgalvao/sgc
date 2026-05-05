package sgc.integracao;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.data.domain.*;
import org.springframework.test.context.jdbc.*;
import sgc.alerta.model.*;
import sgc.mapa.model.*;
import sgc.subprocesso.model.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Mapa SESEL - Seed H2")
@SqlGroup({
        @Sql(scripts = "classpath:sql/mapa-sesel-h2.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
        @Sql(scripts = "classpath:sql/mapa-sesel-h2-cleanup.sql",
                executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD,
                config = @SqlConfig(transactionMode = SqlConfig.TransactionMode.ISOLATED))
})
class MapaSeselH2SeedIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private AlertaRepo alertaRepo;

    @Autowired
    private NotificacaoEmailRepo notificacaoEmailRepo;

    @Test
    @DisplayName("deve carregar o mapa SESEL completo com histórico, alertas e notificações")
    void deveCarregarMapaSeselCompleto() {
        Mapa mapa = mapaRepo.buscarCompletoPorSubprocesso(60003L).orElseThrow();

        assertThat(mapa.getCompetencias()).hasSize(17);
        assertThat(mapa.getAtividades()).hasSize(50);
        assertThat(mapa.getAtividades())
                .allSatisfy(atividade -> assertThat(atividade.getConhecimentos()).isNotEmpty());
        assertThat(mapa.getDataHoraDisponibilizado()).isNotNull();
        assertThat(mapa.getDataHoraHomologado()).isNotNull();

        Movimentacao ultimaMovimentacao = movimentacaoRepo.buscarUltimaPorSubprocesso(60003L).orElseThrow();
        assertThat(ultimaMovimentacao.getDescricao()).isEqualTo("Mapa SESEL homologado");
        assertThat(ultimaMovimentacao.getUnidadeOrigem().getCodigo()).isEqualTo(10L);
        assertThat(ultimaMovimentacao.getUnidadeDestino().getCodigo()).isEqualTo(10L);

        assertThat(alertaRepo.buscarAlertasExclusivosDoUsuario("1"))
                .extracting(Alerta::getCodigo)
                .containsExactly(950001L);

        assertThat(alertaRepo.buscarAlertasDaUnidadeEIndividuais(10L, "1"))
                .extracting(Alerta::getCodigo)
                .containsExactly(950001L, 950002L);

        List<NotificacaoEmail> notificacoes = notificacaoEmailRepo
                .findBySubprocesso_CodigoOrderByDataHoraCriacaoDesc(60003L, PageRequest.of(0, 10));

        assertThat(notificacoes)
                .hasSize(2)
                .extracting(NotificacaoEmail::getTipoNotificacao)
                .containsExactly(TipoNotificacao.MAPA_HOMOLOGADO, TipoNotificacao.MAPA_DISPONIBILIZADO);
        assertThat(notificacoes)
                .extracting(NotificacaoEmail::getSituacao)
                .containsExactly(SituacaoNotificacao.FALHA_DEFINITIVA, SituacaoNotificacao.ENVIADO);
    }
}
