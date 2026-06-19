package sgc.integracao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import sgc.comum.Mensagens;
import sgc.comum.erros.ErroValidacao;
import sgc.fixture.UnidadeFixture;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.AtividadeRepo;
import sgc.mapa.model.Mapa;
import sgc.organizacao.model.Unidade;
import sgc.processo.model.Processo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.SubprocessoValidacaoService;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Tag("integration")
@Transactional
@DisplayName("Integração: SubprocessoService - CDU-11/CDU-12 Validações de Mapa")
class SubprocessoServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private SubprocessoValidacaoService validacaoService;

    @Autowired
    private AtividadeRepo atividadeRepo;

    private Subprocesso subprocesso;

    @BeforeEach
    void setUp() {
        Unidade unidade = UnidadeFixture.unidadePadrao();
        unidade.setCodigo(null);
        unidade.setSigla("TEST_COV");
        unidade.setNome("Unidade cov");
        unidade = unidadeRepo.save(unidade);

        Processo processo = Processo.builder()
                .descricao("Processo teste cov")
                .tipo(TipoProcesso.MAPEAMENTO)
                .situacao(SituacaoProcesso.EM_ANDAMENTO)
                .dataLimite(LocalDateTime.now().plusDays(30))
                .build();
        processoRepo.save(processo);

        subprocesso = Subprocesso.builder()
                .unidade(unidade)
                .situacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO)
                .processo(processo)
                .dataLimiteEtapa1(LocalDateTime.now().plusDays(20))
                .build();
        subprocessoRepo.save(subprocesso);

        Mapa mapa = new Mapa();
        mapa.setSubprocesso(subprocesso);
        mapaRepo.save(mapa);
        subprocesso.setMapa(mapa);
    }

    @Test
    @DisplayName("validarExistenciaAtividades: deve lançar erro se mapa sem atividades")
    void validarExistenciaAtividades_SemAtividades() {
        assertThatThrownBy(() -> validacaoService.validarExistenciaAtividades(subprocesso))
                .isInstanceOf(ErroValidacao.class)
                .hasMessage(Mensagens.MAPA_SEM_ATIVIDADES);
    }

    @Test
    @DisplayName("validarExistenciaAtividades: deve lançar erro se atividades sem conhecimento")
    void validarExistenciaAtividades_SemConhecimento() {
        Atividade atividade = Atividade.builder().mapa(subprocesso.getMapa()).descricao("Sem conhecimento").build();
        atividadeRepo.save(atividade);

        assertThatThrownBy(() -> validacaoService.validarExistenciaAtividades(subprocesso))
                .isInstanceOf(ErroValidacao.class)
                .hasMessage(Mensagens.ATIVIDADES_SEM_CONHECIMENTOS);
    }
}
