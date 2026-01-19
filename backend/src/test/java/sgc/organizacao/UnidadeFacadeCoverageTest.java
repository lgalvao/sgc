package sgc.organizacao;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.comum.repo.RepositorioComum;
import sgc.organizacao.dto.UnidadeDto;
import sgc.organizacao.mapper.UsuarioMapper;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.UnidadeMapaRepo;
import sgc.organizacao.model.UnidadeRepo;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("Testes de Cobertura para UnidadeFacade")
class UnidadeFacadeCoverageTest {

    @InjectMocks
    private UnidadeFacade unidadeFacade;

    @Mock private UnidadeRepo unidadeRepo;
    @Mock private UnidadeMapaRepo unidadeMapaRepo;
    @Mock private UsuarioMapper usuarioMapper;
    @Mock private RepositorioComum repo;

    @Test
    @DisplayName("Deve buscar unidade na hierarquia por sigla recursivamente")
    void deveBuscarNaHierarquiaPorSiglaRecursivamente() {
        // Arrange
        Unidade raiz = new Unidade();
        raiz.setCodigo(1L);
        raiz.setSigla("RAIZ");
        
        Unidade filha = new Unidade();
        filha.setCodigo(2L);
        filha.setSigla("FILHA");
        filha.setUnidadeSuperior(raiz);

        when(unidadeRepo.findAllWithHierarquia()).thenReturn(List.of(raiz, filha));
        
        UnidadeDto dtoRaiz = new UnidadeDto();
        dtoRaiz.setCodigo(1L);
        dtoRaiz.setSigla("RAIZ");
        
        UnidadeDto dtoFilha = new UnidadeDto();
        dtoFilha.setCodigo(2L);
        dtoFilha.setSigla("FILHA");

        when(usuarioMapper.toUnidadeDto(raiz, true)).thenReturn(dtoRaiz);
        when(usuarioMapper.toUnidadeDto(filha, true)).thenReturn(dtoFilha);

        // Act
        List<String> siglas = unidadeFacade.buscarSiglasSubordinadas("FILHA");

        // Assert
        assertThat(siglas).containsExactly("FILHA");
    }

    @Test
    @DisplayName("Deve retornar vazio se unidade superior for nula")
    void deveRetornarVazioSeSuperiorNula() {
        Unidade u = new Unidade();
        u.setSigla("TESTE");
        u.setUnidadeSuperior(null);
        
        when(unidadeRepo.findBySigla("TESTE")).thenReturn(Optional.of(u));
        
        Optional<String> superior = unidadeFacade.buscarSiglaSuperior("TESTE");
        assertThat(superior).isEmpty();
    }
}
