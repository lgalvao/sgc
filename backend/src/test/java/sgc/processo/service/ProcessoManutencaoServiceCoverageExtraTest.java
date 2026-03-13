package sgc.processo.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import sgc.comum.erros.*;
import sgc.organizacao.model.*;
import sgc.organizacao.service.*;
import sgc.processo.dto.*;
import sgc.processo.model.*;

import java.time.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProcessoManutencaoService - Cobertura extra")
class ProcessoManutencaoServiceCoverageExtraTest {

    @Mock private ProcessoRepo processoRepo;
    @Mock private UnidadeService unidadeService;
    @Mock private ProcessoValidacaoService processoValidador;
    @Mock private ProcessoConsultaService processoConsultaService;

    @InjectMocks
    private ProcessoManutencaoService manutencaoService;

    @Test
    @DisplayName("criar - erro unidades sem mapa para REVISAO")
    void criar_erroUnidadesSemMapa() {
        CriarProcessoRequest req = new CriarProcessoRequest("desc", TipoProcesso.REVISAO, LocalDateTime.now(), List.of(1L));
        Unidade u = new Unidade();
        u.setCodigo(1L);
        when(unidadeService.buscarPorId(1L)).thenReturn(u);
        when(processoValidador.validarTiposUnidades(anyList())).thenReturn(Optional.empty());
        when(processoValidador.getMensagemErroUnidadesSemMapa(anyList())).thenReturn(Optional.of("Erro"));

        assertThrows(ErroValidacao.class, () -> manutencaoService.criar(req));
    }

    @Test
    @DisplayName("atualizar - erro unidades sem mapa para DIAGNOSTICO")
    void atualizar_erroUnidadesSemMapa() {
        AtualizarProcessoRequest req = new AtualizarProcessoRequest(1L, "desc", TipoProcesso.DIAGNOSTICO, LocalDateTime.now(), List.of(1L));
        Processo p = new Processo();
        p.setSituacao(SituacaoProcesso.CRIADO);
        when(processoConsultaService.buscarProcessoCodigo(1L)).thenReturn(p);
        when(processoValidador.getMensagemErroUnidadesSemMapa(anyList())).thenReturn(Optional.of("Erro"));

        assertThrows(ErroValidacao.class, () -> manutencaoService.atualizar(1L, req));
    }
}
