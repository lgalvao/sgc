package sgc.organizacao.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.comum.repo.ComumRepo;
import sgc.organizacao.dto.UnidadeDto;
import sgc.organizacao.mapper.UsuarioMapper;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.UnidadeRepo;
import sgc.organizacao.model.TipoUnidade;
import sgc.testutils.UnidadeTestBuilder;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("UnidadeHierarquiaService - Cobertura Adicional")
class UnidadeHierarquiaServiceCoverageTest {

    @Mock
    private UnidadeRepo unidadeRepo;

    @Mock
    private UsuarioMapper usuarioMapper;

    @Mock
    private ComumRepo repo;

    @InjectMocks
    private UnidadeHierarquiaService service;

    @Test
    @DisplayName("buscarArvore deve retornar DTO mapeado manualmente quando não está na árvore mas existe no banco")
    void deveRetornarDtoMapeadoManualmente() {
        // Arrange
        Long codigo = 123L;
        when(unidadeRepo.findAllWithHierarquia()).thenReturn(new ArrayList<>());
        
        Unidade unidade = UnidadeTestBuilder.operacional().build();
        unidade.setCodigo(codigo);
        unidade.setNome("Unidade Solta");
        unidade.setSigla("SOLTA");
        unidade.setTipo(TipoUnidade.OPERACIONAL);
        
        when(repo.buscar(Unidade.class, codigo)).thenReturn(unidade);

        // Act
        UnidadeDto resultado = service.buscarArvore(codigo);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.getCodigo()).isEqualTo(codigo);
        assertThat(resultado.getNome()).isEqualTo("Unidade Solta");
        assertThat(resultado.getSigla()).isEqualTo("SOLTA");
        assertThat(resultado.getTipo()).isEqualTo("OPERACIONAL");
        assertThat(resultado.getSubunidades()).isEmpty();
    }

    @Test
    @DisplayName("buscarSiglasSubordinadas deve retornar lista vazia quando unidade existe no banco mas não na árvore")
    void deveRetornarListaVaziaQuandoUnidadeExisteNoBancoMasNaoNaArvore() {
        // Arrange
        String sigla = "SOLTA";
        when(unidadeRepo.findAllWithHierarquia()).thenReturn(new ArrayList<>());
        
        Unidade unidade = UnidadeTestBuilder.operacional().build();
        unidade.setSigla(sigla);
        
        when(repo.buscarPorSigla(Unidade.class, sigla)).thenReturn(unidade);

        // Act
        List<String> resultado = service.buscarSiglasSubordinadas(sigla);

        // Assert
        assertThat(resultado).isEmpty();
    }
}
