package sgc.processo.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.organizacao.OrganizacaoFacade;
import sgc.organizacao.model.TipoUnidade;
import sgc.organizacao.model.Unidade;
import sgc.processo.dto.CriarProcessoRequest;
import sgc.processo.erros.ErroProcesso;
import sgc.processo.model.ProcessoRepo;
import sgc.processo.model.TipoProcesso;
import sgc.testutils.UnidadeTestBuilder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProcessoManutencaoServiceValidationTest")
class ProcessoManutencaoServiceValidationTest {

    @Mock
    private ProcessoRepo processoRepo;
    @Mock
    private OrganizacaoFacade unidadeService;
    @Mock
    private ProcessoValidador processoValidador;
    @Mock
    private ProcessoConsultaService processoConsultaService;

    @InjectMocks
    private ProcessoManutencaoService service;

    @Test
    @DisplayName("Deve impedir criação de processo com unidade INTERMEDIARIA")
    void deveImpedirCriacaoComUnidadeIntermediaria() {
        // Given
        CriarProcessoRequest request = CriarProcessoRequest.builder()
                .descricao("Processo Teste")
                .tipo(TipoProcesso.MAPEAMENTO)
                .dataLimiteEtapa1(LocalDateTime.now().plusDays(10))
                .unidades(List.of(1L))
                .build();

        Unidade unidadeIntermediaria = UnidadeTestBuilder.umaDe().comCodigo("1")
                .comTipo(TipoUnidade.INTERMEDIARIA)
                .build();

        when(unidadeService.unidadePorCodigo(1L)).thenReturn(unidadeIntermediaria);

        when(processoValidador.validarTiposUnidades(anyList()))
                .thenReturn(Optional.of("Unidades do tipo INTERMEDIARIA não podem participar de processos: COORD_11"));

        // When/Then
        assertThatThrownBy(() -> service.criar(request))
                .isInstanceOf(ErroProcesso.class)
                .hasMessageContaining("Unidades do tipo INTERMEDIARIA não podem participar de processos");
    }
}
