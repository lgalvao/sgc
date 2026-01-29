package sgc.subprocesso.service.factory;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.mapa.model.MapaRepo;
import sgc.mapa.service.CopiaMapaService;
import sgc.organizacao.model.TipoUnidade;
import sgc.organizacao.model.Unidade;
import sgc.processo.model.Processo;
import sgc.subprocesso.service.MovimentacaoRepositoryService;
import sgc.subprocesso.service.SubprocessoRepositoryService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
class SubprocessoFactoryCoverageTest {
    @Mock
    private SubprocessoRepositoryService subprocessoService;

    @Mock
    private MapaRepo mapaRepo;

    @Mock
    private MovimentacaoRepositoryService movimentacaoService;

    @Mock
    private CopiaMapaService servicoDeCopiaDeMapa;

    @InjectMocks
    private SubprocessoFactory factory;

    @Test
    @DisplayName("criarParaMapeamento sucesso para unidade INTEROPERACIONAL")
    void criarParaMapeamento_SucessoInteroperacional() {
        Processo processo = new Processo();
        processo.setDataLimite(LocalDateTime.now().plusDays(10));
        Unidade unidade = new Unidade();
        unidade.setTipo(TipoUnidade.INTEROPERACIONAL);
        unidade.setSigla("UI1");

        when(subprocessoService.saveAll(anyList())).thenAnswer(i -> i.getArgument(0));
        when(mapaRepo.saveAll(anyList())).thenAnswer(i -> i.getArgument(0));

        factory.criarParaMapeamento(processo, List.of(unidade));

        verify(subprocessoService, times(1)).saveAll(anyList());
        verify(mapaRepo).saveAll(anyList());
        verify(movimentacaoService).saveAll(anyList());
    }
}
