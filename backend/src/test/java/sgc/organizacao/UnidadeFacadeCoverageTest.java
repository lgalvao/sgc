package sgc.organizacao;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.comum.repo.RepositorioComum;
import sgc.organizacao.dto.UnidadeDto;
import sgc.organizacao.mapper.UsuarioMapper;
import sgc.organizacao.model.TipoUnidade;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.UnidadeMapa;
import sgc.organizacao.model.UnidadeMapaRepo;
import sgc.organizacao.model.UnidadeRepo;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UnidadeFacadeCoverageTest")
class UnidadeFacadeCoverageTest {

    @Mock private UnidadeRepo unidadeRepo;
    @Mock private UnidadeMapaRepo unidadeMapaRepo;
    @Mock private sgc.organizacao.model.UsuarioRepo usuarioRepo;
    @Mock private sgc.organizacao.model.AtribuicaoTemporariaRepo atribuicaoTemporariaRepo;
    @Mock private UsuarioMapper usuarioMapper;
    @Mock private RepositorioComum repo;

    @InjectMocks
    private UnidadeFacade facade;

    @Test
    @DisplayName("buscarNaHierarquia - Encontra em Subunidades (Recursão)")
    void buscarNaHierarquia_Recursion() {
        // Setup hierarchy: Root -> Child -> Grandchild (Target)
        Long targetId = 3L;

        UnidadeDto grandchild = UnidadeDto.builder().codigo(targetId).subunidades(Collections.emptyList()).build();
        UnidadeDto child = UnidadeDto.builder().codigo(2L).subunidades(List.of(grandchild)).build();
        UnidadeDto root = UnidadeDto.builder().codigo(1L).subunidades(List.of(child)).build();

        // We need to mock buscarTodasUnidades which calls unidadeRepo.findAllWithHierarquia
        Unidade rootEntity = new Unidade();
        rootEntity.setCodigo(1L);

        Unidade childEntity = new Unidade();
        childEntity.setCodigo(2L);
        childEntity.setUnidadeSuperior(rootEntity);

        Unidade grandchildEntity = new Unidade();
        grandchildEntity.setCodigo(targetId);
        grandchildEntity.setUnidadeSuperior(childEntity);

        when(unidadeRepo.findAllWithHierarquia()).thenReturn(List.of(rootEntity, childEntity, grandchildEntity));

        // Mock mapper to return our DTO structure
        when(usuarioMapper.toUnidadeDto(eq(rootEntity), anyBoolean())).thenReturn(root);
        when(usuarioMapper.toUnidadeDto(eq(childEntity), anyBoolean())).thenReturn(child);
        when(usuarioMapper.toUnidadeDto(eq(grandchildEntity), anyBoolean())).thenReturn(grandchild);

        UnidadeDto result = facade.buscarArvore(targetId);

        assertEquals(targetId, result.getCodigo());
    }

    @Test
    @DisplayName("buscarIdsDescendentes - Caching and Recursion")
    void buscarIdsDescendentes_Coverage() {
        Long rootId = 1L;
        Long childId = 2L;
        Long grandchildId = 3L;

        Unidade root = new Unidade();
        root.setCodigo(rootId);

        Unidade child = new Unidade();
        child.setCodigo(childId);
        child.setUnidadeSuperior(root);

        Unidade grandchild = new Unidade();
        grandchild.setCodigo(grandchildId);
        grandchild.setUnidadeSuperior(child);

        when(unidadeRepo.findAllWithHierarquia()).thenReturn(List.of(root, child, grandchild));

        List<Long> result = facade.buscarIdsDescendentes(rootId);

        assertTrue(result.contains(childId));
        assertTrue(result.contains(grandchildId));
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("definirMapaVigente - UnidadeMapa Existente vs Novo")
    void definirMapaVigente_Coverage() {
        Long unidadeCodigo = 1L;
        sgc.mapa.model.Mapa mapa = new sgc.mapa.model.Mapa();

        // Case 1: Exists
        UnidadeMapa existing = new UnidadeMapa();
        when(unidadeMapaRepo.findById(unidadeCodigo)).thenReturn(Optional.of(existing));
        facade.definirMapaVigente(unidadeCodigo, mapa);
        verify(unidadeMapaRepo).save(existing);

        // Case 2: Not exists
        when(unidadeMapaRepo.findById(unidadeCodigo)).thenReturn(Optional.empty());
        facade.definirMapaVigente(unidadeCodigo, mapa);
        verify(unidadeMapaRepo, times(2)).save(any(UnidadeMapa.class));
    }

    @Test
    @DisplayName("montarHierarquiaComElegibilidade - Check Filter Logic")
    void montarHierarquiaComElegibilidade_FilterCoverage() {
        // Need to test the lambda passed to montarHierarquia
        // Condition: u.getTipo() != INTERMEDIARIA && (!requerMapaVigente || mapExists) && !blocked

        Unidade u1 = new Unidade();
        u1.setCodigo(1L);
        u1.setTipo(TipoUnidade.OPERACIONAL);

        Unidade u2 = new Unidade();
        u2.setCodigo(2L);
        u2.setTipo(TipoUnidade.INTERMEDIARIA); // Should be false in mapper

        Unidade u3 = new Unidade();
        u3.setCodigo(3L);
        u3.setTipo(TipoUnidade.OPERACIONAL); // Blocked

        when(unidadeRepo.findAllWithHierarquia()).thenReturn(List.of(u1, u2, u3));

        // blocked set
        java.util.Set<Long> blocked = java.util.Set.of(3L);

        // Mock mapper to capture the boolean flag
        when(usuarioMapper.toUnidadeDto(eq(u1), eq(true))).thenReturn(UnidadeDto.builder().codigo(1L).isElegivel(true).subunidades(Collections.emptyList()).build());
        when(usuarioMapper.toUnidadeDto(eq(u2), eq(false))).thenReturn(UnidadeDto.builder().codigo(2L).isElegivel(false).subunidades(Collections.emptyList()).build());
        when(usuarioMapper.toUnidadeDto(eq(u3), eq(false))).thenReturn(UnidadeDto.builder().codigo(3L).isElegivel(false).subunidades(Collections.emptyList()).build());

        facade.buscarArvoreComElegibilidade(false, blocked);

        // Verify calls with expected boolean values
        verify(usuarioMapper).toUnidadeDto(eq(u1), eq(true));
        verify(usuarioMapper).toUnidadeDto(eq(u2), eq(false));
        verify(usuarioMapper).toUnidadeDto(eq(u3), eq(false));
    }

    @Test
    @DisplayName("buscarSiglasSubordinadas - Recursion")
    void buscarSiglasSubordinadas_Recursion() {
        String siglaRoot = "ROOT";
        String siglaChild = "CHILD";

        UnidadeDto root = UnidadeDto.builder().codigo(1L).sigla(siglaRoot).build();
        UnidadeDto child = UnidadeDto.builder().codigo(2L).sigla(siglaChild).build();
        root.setSubunidades(List.of(child));
        child.setSubunidades(Collections.emptyList());

        Unidade uRoot = new Unidade(); uRoot.setCodigo(1L); uRoot.setSigla(siglaRoot);
        Unidade uChild = new Unidade(); uChild.setCodigo(2L); uChild.setSigla(siglaChild); uChild.setUnidadeSuperior(uRoot);

        when(unidadeRepo.findAllWithHierarquia()).thenReturn(List.of(uRoot, uChild));
        when(usuarioMapper.toUnidadeDto(eq(uRoot), anyBoolean())).thenReturn(root);
        when(usuarioMapper.toUnidadeDto(eq(uChild), anyBoolean())).thenReturn(child);

        List<String> result = facade.buscarSiglasSubordinadas(siglaRoot);

        assertTrue(result.contains(siglaRoot));
        assertTrue(result.contains(siglaChild));
    }
}
