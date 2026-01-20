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
import sgc.organizacao.model.UnidadeRepo;

import java.util.ArrayList;
import java.util.List;

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
    @Mock private UsuarioMapper usuarioMapper;
    @Mock private RepositorioComum repo;
    
    // Mocks adicionais podem ser necessarios dependendo da construcao da classe, 
    // mas se nao usados pelos metodos testados, ok.
    @Mock private sgc.organizacao.model.UnidadeMapaRepo unidadeMapaRepo;
    @Mock private sgc.organizacao.model.UsuarioRepo usuarioRepo;
    @Mock private sgc.organizacao.model.AtribuicaoTemporariaRepo atribuicaoTemporariaRepo;

    @Test
    @DisplayName("Deve buscar unidade na hierarquia recursivamente")
    void deveBuscarArvoreRecursiva() {
        // Arrange
        Unidade raiz = new Unidade(); raiz.setCodigo(1L); raiz.setSigla("RAIZ");
        Unidade filha = new Unidade(); filha.setCodigo(2L); filha.setSigla("FILHA"); filha.setUnidadeSuperior(raiz);
        Unidade neta = new Unidade(); neta.setCodigo(3L); neta.setSigla("NETA"); neta.setUnidadeSuperior(filha);

        // O facade chama findAllWithHierarquia e entao monta a arvore via mapper
        when(unidadeRepo.findAllWithHierarquia()).thenReturn(List.of(raiz, filha, neta));

        // Mock dos DTOs
        UnidadeDto dtoRaiz = new UnidadeDto(); dtoRaiz.setCodigo(1L); dtoRaiz.setSubunidades(new ArrayList<>());
        UnidadeDto dtoFilha = new UnidadeDto(); dtoFilha.setCodigo(2L); dtoFilha.setSubunidades(new ArrayList<>());
        UnidadeDto dtoNeta = new UnidadeDto(); dtoNeta.setCodigo(3L); dtoNeta.setSubunidades(new ArrayList<>());

        // Simula o mapper
        when(usuarioMapper.toUnidadeDto(any(Unidade.class), anyBoolean())).thenAnswer(inv -> {
           Unidade u = inv.getArgument(0);
           if (u.getCodigo() == 1L) return dtoRaiz;
           if (u.getCodigo() == 2L) return dtoFilha;
           if (u.getCodigo() == 3L) return dtoNeta;
           return null;
        });

        // Act
        // Busca pela neta (codigo 3)
        // O metodo buscarArvore busca todas e depois procura o ID 3 na hierarquia
        // A montagem da hierarquia vai colocar Neta dentro de Filha dentro de Raiz
        // porem a logica de montarHierarquia usa subunidades
        // Precisamos garantir que a logica de montagem funcione ou simular os DTOs ja aninhados?
        // O metodo usuarioMapper.toUnidadeDto retorna DTO sem filhas (subunidades vazias).
        // A logica do facade preenche subunidades.

        // Testar buscarArvore(3L).
        // Isso vai chamar buscarTodasUnidades -> montarHierarquia -> ...
        // E depois buscarNaHierarquia(todas, 3L).
        
        UnidadeDto result = unidadeFacade.buscarArvore(3L);
        assertThat(result.getCodigo()).isEqualTo(3L);
    }
}
