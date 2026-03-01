package sgc.processo.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.organizacao.OrganizacaoFacade;
import sgc.organizacao.model.Unidade;
import sgc.processo.dto.AtualizarProcessoRequest;
import sgc.processo.dto.CriarProcessoRequest;
import sgc.processo.erros.ErroProcesso;
import sgc.processo.model.Processo;
import sgc.processo.model.ProcessoRepo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProcessoManutencaoService - Cobertura Extra")
class ProcessoManutencaoServiceCoverageExtraTest {

    @Mock private ProcessoRepo processoRepo;
    @Mock private OrganizacaoFacade organizacaoFacade;
    @Mock private ProcessoValidador processoValidador;
    @Mock private ProcessoConsultaService processoConsultaService;

    @InjectMocks
    private ProcessoManutencaoService manutencaoService;

    @Test
    @DisplayName("criar - erro unidades sem mapa para REVISAO")
    void criar_erroUnidadesSemMapa() {
        CriarProcessoRequest req = new CriarProcessoRequest("desc", TipoProcesso.REVISAO, LocalDateTime.now(), List.of(1L));
        Unidade u = new Unidade();
        u.setCodigo(1L);
        when(organizacaoFacade.unidadePorCodigo(1L)).thenReturn(u);
        when(processoValidador.validarTiposUnidades(anyList())).thenReturn(Optional.empty());
        when(processoValidador.getMensagemErroUnidadesSemMapa(anyList())).thenReturn(Optional.of("Erro"));

        assertThrows(ErroProcesso.class, () -> manutencaoService.criar(req));
    }

    @Test
    @DisplayName("atualizar - erro unidades sem mapa para DIAGNOSTICO")
    void atualizar_erroUnidadesSemMapa() {
        AtualizarProcessoRequest req = new AtualizarProcessoRequest(1L, "desc", TipoProcesso.DIAGNOSTICO, LocalDateTime.now(), List.of(1L));
        Processo p = new Processo();
        p.setSituacao(SituacaoProcesso.CRIADO);
        when(processoConsultaService.buscarProcessoCodigo(1L)).thenReturn(p);
        when(processoValidador.getMensagemErroUnidadesSemMapa(anyList())).thenReturn(Optional.of("Erro"));

        assertThrows(ErroProcesso.class, () -> manutencaoService.atualizar(1L, req));
    }
}
