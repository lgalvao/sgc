package sgc.subprocesso.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import sgc.subprocesso.model.*;
import sgc.organizacao.model.*;
import sgc.processo.model.Processo;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SubprocessoTransicaoService - Cobertura adicional")
class SubprocessoTransicaoServiceCoverageTest {

    @Mock
    private sgc.comum.model.ComumRepo repo;

    @Mock
    private MovimentacaoRepo movimentacaoRepo;

    @Mock
    private SubprocessoValidacaoService validacaoService;

    @Mock
    private sgc.processo.service.ProcessoService processoService;

    @Mock
    private sgc.organizacao.service.UnidadeService unidadeService;

    @Mock
    private SubprocessoRepo subprocessoRepo;

    @Mock
    private AnaliseRepo analiseRepo;

    @InjectMocks
    private SubprocessoTransicaoService subprocessoTransicaoService;

    @Test
    @DisplayName("aceitarValidacao deve testar proximaUnidade nula")
    void deveAceitarValidacaoQuandoProximaUnidadeNula() {
        Subprocesso sp = new Subprocesso();
        sp.setCodigo(100L);
        sp.setProcesso(sgc.processo.model.Processo.builder().tipo(sgc.processo.model.TipoProcesso.MAPEAMENTO).build());
        sp.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_MAPA_COM_SUGESTOES);

        Unidade uAtual = new Unidade(); uAtual.setCodigo(1L); uAtual.setSigla("U1");
        sp.setUnidade(uAtual);

        when(subprocessoRepo.buscarPorCodigoComMapaEAtividades(100L)).thenReturn(Optional.of(sp));
        when(movimentacaoRepo.findBySubprocessoCodigoOrderByDataHoraDesc(100L)).thenReturn(Collections.emptyList());
        when(unidadeService.buscarPorSigla("U1")).thenReturn(uAtual);

        Usuario user = new Usuario(); user.setTituloEleitoral("123");

        subprocessoTransicaoService.aceitarValidacao(100L, "obs", user);

        assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_MAPA_HOMOLOGADO);
        verify(subprocessoRepo).save(sp);
    }
}
