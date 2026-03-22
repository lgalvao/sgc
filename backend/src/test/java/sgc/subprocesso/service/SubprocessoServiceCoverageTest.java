package sgc.subprocesso.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import sgc.subprocesso.model.*;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SubprocessoService - Cobertura adicional")
class SubprocessoServiceCoverageTest {

    @Mock
    private SubprocessoRepo subprocessoRepo;

    @Mock
    private MovimentacaoRepo movimentacaoRepo;

    @InjectMocks
    private SubprocessoService subprocessoService;

    @BeforeEach
    void setup() {
        subprocessoService.setSubprocessoRepo(subprocessoRepo);
        subprocessoService.setMovimentacaoRepo(movimentacaoRepo);
    }

    @Test
    @DisplayName("listarPorProcessoEUnidadeCodigosESituacoes deve filtrar por situacao")
    void deveFiltrarPorSituacao() {
        Subprocesso sp1 = new Subprocesso();
        sp1.setSituacao(SituacaoSubprocesso.NAO_INICIADO);
        Subprocesso sp2 = new Subprocesso();
        sp2.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO);

        when(subprocessoRepo.findByProcessoCodigoAndUnidadeCodigoInWithUnidade(1L, List.of(10L)))
            .thenReturn(List.of(sp1, sp2));

        List<Subprocesso> res = subprocessoService.listarPorProcessoEUnidadeCodigosESituacoes(1L, List.of(10L), List.of(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO));

        assertThat(res).hasSize(1);
        assertThat(res.getFirst()).isEqualTo(sp2);
    }

    @Test
    @DisplayName("obterUnidadeLocalizacao deve retornar unidade do SP se codigo nulo ou sem movimentacao")
    void deveRetornarUnidadeSP() {
        Subprocesso sp = new Subprocesso();
        sgc.organizacao.model.Unidade u = new sgc.organizacao.model.Unidade(); u.setCodigo(1L);
        sp.setUnidade(u);

        // localizacaoAtual nula e codigo nulo
        sgc.organizacao.model.Unidade res = subprocessoService.obterUnidadeLocalizacao(sp);
        assertThat(res).isEqualTo(u);

        // com codigo mas sem movs
        sp.setCodigo(100L);
        when(movimentacaoRepo.findBySubprocessoCodigoOrderByDataHoraDesc(100L)).thenReturn(Collections.emptyList());
        sgc.organizacao.model.Unidade res2 = subprocessoService.obterUnidadeLocalizacao(sp);
        assertThat(res2).isEqualTo(u);
    }
}
