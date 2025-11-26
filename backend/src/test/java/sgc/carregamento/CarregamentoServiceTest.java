package sgc.carregamento;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import sgc.alerta.model.AlertaRepo;
import sgc.alerta.model.AlertaUsuarioRepo;
import sgc.analise.model.AnaliseRepo;
import sgc.atividade.model.AtividadeRepo;
import sgc.atividade.model.ConhecimentoRepo;
import sgc.mapa.model.CompetenciaRepo;
import sgc.mapa.model.MapaRepo;
import sgc.notificacao.model.NotificacaoRepo;
import sgc.processo.model.ProcessoRepo;
import sgc.sgrh.model.UsuarioRepo;
import sgc.subprocesso.model.MovimentacaoRepo;
import sgc.subprocesso.model.SubprocessoRepo;
import sgc.unidade.model.AtribuicaoTemporariaRepo;
import sgc.unidade.model.UnidadeRepo;
import sgc.unidade.model.VinculacaoUnidadeRepo;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("e2e")
@DisplayName("Testes do CarregamentoService")
class CarregamentoServiceTest {
    @Autowired
    private CarregamentoService carregamentoService;
    @Autowired
    private AlertaRepo alertaRepo;
    @Autowired
    private AlertaUsuarioRepo alertaUsuarioRepo;
    @Autowired
    private AnaliseRepo analiseRepo;
    @Autowired
    private AtividadeRepo atividadeRepo;
    @Autowired
    private AtribuicaoTemporariaRepo atribuicaoTemporariaRepo;
    @Autowired
    private CompetenciaRepo competenciaRepo;
    @Autowired
    private ConhecimentoRepo conhecimentoRepo;
    @Autowired
    private MapaRepo mapaRepo;
    @Autowired
    private MovimentacaoRepo movimentacaoRepo;
    @Autowired
    private NotificacaoRepo notificacaoRepo;
    @Autowired
    private ProcessoRepo processoRepo;
    @Autowired
    private SubprocessoRepo subprocessoRepo;
    @Autowired
    private UnidadeRepo unidadeRepo;
    @Autowired
    private UsuarioRepo usuarioRepo;
    @Autowired
    private VinculacaoUnidadeRepo vinculacaoUnidadeRepo;

    @Test
    @DisplayName("Deve carregar e remover os dados")
    void deveCarregarERemoverOsDados() {
        carregamentoService.removerDados();
        carregamentoService.carregarDados();

        assertThat(mapaRepo.count()).isEqualTo(5);
        assertThat(unidadeRepo.count()).isEqualTo(26);
        assertThat(usuarioRepo.count()).isEqualTo(46);
        assertThat(competenciaRepo.count()).isEqualTo(8);
        assertThat(atividadeRepo.count()).isEqualTo(2);
        assertThat(conhecimentoRepo.count()).isEqualTo(2);
        assertThat(processoRepo.count()).isEqualTo(2);
        assertThat(subprocessoRepo.count()).isEqualTo(1);
        assertThat(alertaRepo.count()).isEqualTo(2);
        assertThat(movimentacaoRepo.count()).isEqualTo(1);
        assertThat(alertaUsuarioRepo.count()).isZero();
        assertThat(analiseRepo.count()).isZero();
        assertThat(atribuicaoTemporariaRepo.count()).isZero();
        assertThat(notificacaoRepo.count()).isZero();
        assertThat(vinculacaoUnidadeRepo.count()).isZero();

        carregamentoService.removerDados();

        assertThat(mapaRepo.count()).isZero();
        assertThat(unidadeRepo.count()).isZero();
        assertThat(usuarioRepo.count()).isZero();
        assertThat(competenciaRepo.count()).isZero();
        assertThat(atividadeRepo.count()).isZero();
        assertThat(conhecimentoRepo.count()).isZero();
        assertThat(processoRepo.count()).isZero();
        assertThat(subprocessoRepo.count()).isZero();
        assertThat(alertaRepo.count()).isZero();
        assertThat(movimentacaoRepo.count()).isZero();
    }
}
