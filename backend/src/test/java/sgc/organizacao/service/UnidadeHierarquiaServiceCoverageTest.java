package sgc.organizacao.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import sgc.organizacao.dto.UnidadeDto;
import sgc.organizacao.model.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.util.ReflectionTestUtils.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UnidadeHierarquiaService - Cobertura de Testes")
class UnidadeHierarquiaServiceCoverageTest {

    @InjectMocks
    private UnidadeHierarquiaService target;

    @Mock
    private UnidadeRepo unidadeRepo;

    @Test
    @DisplayName("carregarTitulosResponsavel - deve retornar mapa vazio quando lista for vazia")
    void carregarTitulosResponsavel_ListaVazia() {
        Map<Long, String> result = invokeMethod(target, "carregarTitulosResponsavel", Collections.emptyList());
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("buscarNaHierarquiaPorSigla - deve encontrar recursivamente")
    void buscarNaHierarquiaPorSigla_Recursivo() {
        UnidadeDto filho = new UnidadeDto();
        filho.setCodigo(2L);
        filho.setSigla("F1");
        
        UnidadeDto pai = new UnidadeDto();
        pai.setCodigo(1L);
        pai.setSigla("P1");
        pai.setSubunidades(List.of(filho));
        
        Optional<UnidadeDto> result = invokeMethod(target, "buscarNaHierarquiaPorSigla", List.of(pai), "F1");
        assertThat(result).isPresent();
        assertThat(result.get().getCodigo()).isEqualTo(2L);
    }
}
