package sgc.integracao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import sgc.fixture.UnidadeFixture;
import sgc.mapa.model.Mapa;
import sgc.mapa.model.MapaRepo;
import sgc.organizacao.model.Unidade;
import sgc.processo.model.Processo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;
import sgc.subprocesso.dto.AtualizarSubprocessoRequest;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.SubprocessoService;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("integration")
@Transactional
@DisplayName("Integração: SubprocessoService - Cobertura de ProcessarAlteracoes e Extras")
class SubprocessoServiceProcessarAlteracoesIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private SubprocessoService subprocessoService;

    @Autowired
    private MapaRepo mapaRepo;

    private Unidade unidade;
    private Processo processo;
    private Subprocesso subprocesso;

    @BeforeEach
    void setUp() {
        unidade = UnidadeFixture.unidadePadrao();
        unidade.setCodigo(null);
        unidade.setSigla("TEST_PROC");
        unidade = unidadeRepo.save(unidade);

        processo = Processo.builder()
                .descricao("Processo Teste")
                .tipo(TipoProcesso.MAPEAMENTO)
                .situacao(SituacaoProcesso.EM_ANDAMENTO)
                .dataLimite(LocalDateTime.now().plusDays(30))
                .build();
        processoRepo.save(processo);

        subprocesso = Subprocesso.builder()
                .unidade(unidade)
                .situacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO)
                .processo(processo)
                .build();
        subprocessoRepo.save(subprocesso);
    }

    @Test
    @DisplayName("atualizarEntidade: altera mapa de null para mapa existente")
    void atualizarEntidade_SemMapaAntes() {
        Mapa novoMapa = new Mapa();
        mapaRepo.save(novoMapa);

        AtualizarSubprocessoRequest request = new AtualizarSubprocessoRequest(
            unidade.getCodigo(), novoMapa.getCodigo(), null, null, null, null
        );

        Subprocesso atualizado = subprocessoService.atualizarEntidade(subprocesso.getCodigo(), request);
        assertThat(atualizado.getMapa().getCodigo()).isEqualTo(novoMapa.getCodigo());
    }

    @Test
    @DisplayName("atualizarEntidade: nao altera mapa se o codigo for o mesmo")
    void atualizarEntidade_MesmoMapa() {
        Mapa mapa = new Mapa();
        mapa.setSubprocesso(subprocesso);
        mapaRepo.save(mapa);
        subprocesso.setMapa(mapa);
        subprocessoRepo.save(subprocesso);

        AtualizarSubprocessoRequest request = new AtualizarSubprocessoRequest(
            unidade.getCodigo(), mapa.getCodigo(), null, null, null, null
        );

        Subprocesso atualizado = subprocessoService.atualizarEntidade(subprocesso.getCodigo(), request);
        assertThat(atualizado.getMapa().getCodigo()).isEqualTo(mapa.getCodigo());
    }
}
