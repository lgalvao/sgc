package sgc.organizacao.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import sgc.comum.erros.*;
import sgc.organizacao.dto.*;
import sgc.organizacao.model.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ResponsavelUnidadeService - Cobertura de Testes")
class ResponsavelUnidadeServiceCoverageTest {

    @InjectMocks
    private ResponsavelUnidadeService target;

    @Mock
    private UnidadeRepo unidadeRepo;

    @Mock
    private UsuarioRepo usuarioRepo;

    @Mock
    private AtribuicaoTemporariaRepo atribuicaoTemporariaRepo;

    @Mock
    private ResponsabilidadeRepo responsabilidadeRepo;

    @Test
    @DisplayName("buscarTodasAtribuicoes - deve retornar lista vazia quando não houver títulos")
    void buscarTodasAtribuicoes_SemTitulos() {
        when(atribuicaoTemporariaRepo.listarTodasComUnidade()).thenReturn(List.of());

        List<AtribuicaoDto> result = target.buscarTodasAtribuicoes();

        assertThat(result).isEmpty();
        verify(usuarioRepo, never()).listarPorTitulosComUnidadeLotacao(anyList());
    }

    @Test
    @DisplayName("buscarTodasAtribuicoes - deve lançar IllegalStateException quando usuário estiver ausente")
    void buscarTodasAtribuicoes_UsuarioAusente() {
        AtribuicaoTemporaria at = new AtribuicaoTemporaria();
        at.setCodigo(1L);
        at.setUsuarioTitulo("123456789012");
        when(atribuicaoTemporariaRepo.listarTodasComUnidade()).thenReturn(List.of(at));
        when(usuarioRepo.listarPorTitulosComUnidadeLotacao(anyList())).thenReturn(List.of());

        assertThatThrownBy(() -> target.buscarTodasAtribuicoes())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Usuário ausente");
    }

    @Test
    @DisplayName("buscarResponsabilidadeDetalhadaAtual - deve buscar por sigla")
    void buscarResponsabilidadeDetalhadaAtual_Sigla() {
        String sigla = "U10";
        Long codigo = 10L;
        when(unidadeRepo.buscarCodigoAtivoPorSigla(sigla)).thenReturn(Optional.of(codigo));
        
        ResponsabilidadeUnidadeLeitura leitura = mock(ResponsabilidadeUnidadeLeitura.class);
        when(leitura.usuarioTitulo()).thenReturn("123");
        when(responsabilidadeRepo.buscarLeituraDetalhadaPorCodigoUnidade(codigo)).thenReturn(Optional.of(leitura));
        when(usuarioRepo.findById("123")).thenReturn(Optional.of(new Usuario()));

        target.buscarResponsabilidadeDetalhadaAtual(sigla);

        verify(unidadeRepo).buscarCodigoAtivoPorSigla(sigla);
    }

    @Test
    @DisplayName("buscarResponsavelUnidade - deve lançar erro quando não houver responsável")
    void buscarResponsavelUnidade_NaoEncontrado() {
        Long codigo = 10L;
        when(responsabilidadeRepo.listarResumosPorCodigosUnidade(List.of(codigo))).thenReturn(List.of());

        assertThatThrownBy(() -> target.buscarResponsavelUnidade(codigo))
                .isInstanceOf(ErroEntidadeNaoEncontrada.class);
    }
}
