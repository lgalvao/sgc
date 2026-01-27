package sgc.organizacao;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.organizacao.service.UnidadeHierarquiaService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("UnidadeFacade - Testes de Hierarquia")
class UnidadeFacadeHierarchyTest {

    @Mock
    private UnidadeHierarquiaService unidadeHierarquiaService;

    @InjectMocks
    private UnidadeFacade unidadeService;

    @Test
    @DisplayName("buscarIdsDescendentes: deve retornar lista vazia se não houver descendentes")
    void buscarIdsDescendentes_SemFilhos() {
        Long raizId = 1L;

        when(unidadeHierarquiaService.buscarIdsDescendentes(raizId)).thenReturn(List.of());

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

        when(unidadeHierarquiaService.buscarIdsDescendentes(1L))
                .thenReturn(List.of(2L, 3L, 4L));
        when(unidadeHierarquiaService.buscarIdsDescendentes(2L))
                .thenReturn(List.of(3L));
        when(unidadeHierarquiaService.buscarIdsDescendentes(3L))
                .thenReturn(List.of());

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

        when(unidadeHierarquiaService.buscarIdsDescendentes(1L))
                .thenReturn(List.of(2L));

        List<Long> idsA1 = unidadeService.buscarIdsDescendentes(1L);
        assertThat(idsA1)
                .containsExactly(2L)
                .doesNotContain(10L, 11L);
    }
}
