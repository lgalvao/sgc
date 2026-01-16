package sgc.organizacao;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.UnidadeRepo;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("UnidadeFacade - Testes de Hierarquia")
class UnidadeFacadeHierarchyTest {

    @Mock
    private UnidadeRepo unidadeRepo;

    @InjectMocks
    private UnidadeFacade unidadeService;

    @Test
    @DisplayName("buscarIdsDescendentes: deve retornar lista vazia se não houver descendentes")
    void buscarIdsDescendentes_SemFilhos() {
        Long raizId = 1L;
        Unidade raiz = new Unidade(); raiz.setCodigo(raizId);

        // Simulando que só existe a raiz no banco (cache)
        when(unidadeRepo.findAllWithHierarquia()).thenReturn(List.of(raiz));

        List<Long> ids = unidadeService.buscarIdsDescendentes(raizId);

        assertThat(ids).isEmpty();
    }

    @Test
    @DisplayName("buscarIdsDescendentes: deve retornar todos os descendentes diretos e indiretos")
    void buscarIdsDescendentes_ComHierarquiaComplexa() {
        // Hierarquia:
        // Raiz (1)
        //   -> Filho 1 (2)
        //      -> Neto 1 (3)
        //   -> Filho 2 (4)

        Unidade raiz = new Unidade(); raiz.setCodigo(1L);
        Unidade filho1 = new Unidade(); filho1.setCodigo(2L); filho1.setUnidadeSuperior(raiz);
        Unidade neto1 = new Unidade(); neto1.setCodigo(3L); neto1.setUnidadeSuperior(filho1);
        Unidade filho2 = new Unidade(); filho2.setCodigo(4L); filho2.setUnidadeSuperior(raiz);

        List<Unidade> todas = List.of(raiz, filho1, neto1, filho2);

        when(unidadeRepo.findAllWithHierarquia()).thenReturn(todas);

        // Teste: Descendentes da Raiz (1) devem ser 2, 3 e 4
        List<Long> idsRaiz = unidadeService.buscarIdsDescendentes(1L);
        assertThat(idsRaiz).containsExactlyInAnyOrder(2L, 3L, 4L);

        // Teste: Descendentes do Filho 1 (2) deve ser apenas 3
        List<Long> idsFilho1 = unidadeService.buscarIdsDescendentes(2L);
        assertThat(idsFilho1).containsExactlyInAnyOrder(3L);

        // Teste: Descendentes do Neto 1 (3) deve ser vazio
        List<Long> idsNeto1 = unidadeService.buscarIdsDescendentes(3L);
        assertThat(idsNeto1).isEmpty();
    }

    @Test
    @DisplayName("buscarIdsDescendentes: deve ignorar unidades de outras árvores")
    void buscarIdsDescendentes_OutrasArvores() {
        // Arvore A: 1 -> 2
        // Arvore B: 10 -> 11

        Unidade a1 = new Unidade(); a1.setCodigo(1L);
        Unidade a2 = new Unidade(); a2.setCodigo(2L); a2.setUnidadeSuperior(a1);

        Unidade b1 = new Unidade(); b1.setCodigo(10L);
        Unidade b2 = new Unidade(); b2.setCodigo(11L); b2.setUnidadeSuperior(b1);

        when(unidadeRepo.findAllWithHierarquia()).thenReturn(List.of(a1, a2, b1, b2));

        List<Long> idsA1 = unidadeService.buscarIdsDescendentes(1L);
        assertThat(idsA1).containsExactly(2L);
        assertThat(idsA1).doesNotContain(10L, 11L);
    }
}
