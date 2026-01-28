package sgc.organizacao.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.organizacao.dto.UnidadeDto;
import sgc.organizacao.mapper.UsuarioMapper;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.UnidadeRepo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("Testes de Cobertura para UnidadeHierarquiaService")
class UnidadeHierarquiaServiceCoverageTest {

    @InjectMocks
    private UnidadeHierarquiaService service;

    @Mock
    private UnidadeRepo unidadeRepo;

    @Mock
    private UsuarioMapper usuarioMapper;

    @Test
    @DisplayName("Deve buscar ids descendentes recursivamente")
    void deveBuscarIdsDescendentes() {
        // Hierarquia: 1 (Raiz) -> 2 (Filho) -> 3 (Neto)
        Unidade raiz = Unidade.builder().codigo(1L).build();
        Unidade filho = Unidade.builder().codigo(2L).unidadeSuperior(raiz).build();
        Unidade neto = Unidade.builder().codigo(3L).unidadeSuperior(filho).build();

        when(unidadeRepo.findAllWithHierarquia()).thenReturn(List.of(raiz, filho, neto));

        List<Long> descendentesRaiz = service.buscarIdsDescendentes(1L);
        assertThat(descendentesRaiz).containsExactlyInAnyOrder(2L, 3L);

        List<Long> descendentesFilho = service.buscarIdsDescendentes(2L);
        assertThat(descendentesFilho).containsExactlyInAnyOrder(3L);

        List<Long> descendentesNeto = service.buscarIdsDescendentes(3L);
        assertThat(descendentesNeto).isEmpty();
    }

    @Test
    @DisplayName("Deve montar hierarquia com elegibilidade")
    void deveMontarHierarquiaComElegibilidade() {
        Unidade raiz = Unidade.builder().codigo(1L).build();
        Unidade filho = Unidade.builder().codigo(2L).unidadeSuperior(raiz).build();

        UnidadeDto raizDto = UnidadeDto.builder().codigo(1L).subunidades(new ArrayList<>()).build();
        UnidadeDto filhoDto = UnidadeDto.builder().codigo(2L).subunidades(new ArrayList<>()).build();

        when(unidadeRepo.findAllWithHierarquia()).thenReturn(List.of(raiz, filho));

        // Configura mapper para responder conforme a elegibilidade
        when(usuarioMapper.toUnidadeDto(eq(raiz), eq(true))).thenReturn(raizDto);
        when(usuarioMapper.toUnidadeDto(eq(filho), eq(false))).thenReturn(filhoDto);

        // Elegibilidade: raiz é elegível, filho não
        Function<Unidade, Boolean> elegibilidadeChecker = u -> u.getCodigo().equals(1L);

        List<UnidadeDto> resultado = service.buscarArvoreComElegibilidade(elegibilidadeChecker);

        assertThat(resultado).hasSize(1);
        UnidadeDto dtoResultado = resultado.get(0);
        assertThat(dtoResultado.getCodigo()).isEqualTo(1L);
        assertThat(dtoResultado.getSubunidades()).hasSize(1);
        assertThat(dtoResultado.getSubunidades().get(0).getCodigo()).isEqualTo(2L);
    }

    @Test
    @DisplayName("Deve buscar unidade na hierarquia recursivamente")
    void deveBuscarNaHierarquiaRecursivamente() {
        // Monta DTOs manualmente para simular retorno de buscarArvoreHierarquica
        UnidadeDto netoDto = UnidadeDto.builder().codigo(3L).sigla("NETO").subunidades(new ArrayList<>()).build();
        UnidadeDto filhoDto = UnidadeDto.builder().codigo(2L).sigla("FILHO").subunidades(List.of(netoDto)).build();
        UnidadeDto raizDto = UnidadeDto.builder().codigo(1L).sigla("RAIZ").subunidades(List.of(filhoDto)).build();

        Unidade raiz = Unidade.builder().codigo(1L).build();
        Unidade filho = Unidade.builder().codigo(2L).unidadeSuperior(raiz).build();
        Unidade neto = Unidade.builder().codigo(3L).unidadeSuperior(filho).build();

        when(unidadeRepo.findAllWithHierarquia()).thenReturn(List.of(raiz, filho, neto));
        when(usuarioMapper.toUnidadeDto(any(), eq(true))).thenAnswer(inv -> {
           Unidade u = inv.getArgument(0);
           if (u.getCodigo().equals(1L)) return raizDto;
           if (u.getCodigo().equals(2L)) return filhoDto;
           if (u.getCodigo().equals(3L)) return netoDto;
           return null;
        });

        // Busca pela raiz
        UnidadeDto resultRaiz = service.buscarArvore(1L);
        assertThat(resultRaiz.getCodigo()).isEqualTo(1L);

        // Busca pelo neto (recursão)
        UnidadeDto resultNeto = service.buscarArvore(3L);
        assertThat(resultNeto.getCodigo()).isEqualTo(3L);

        // Busca inexistente
        assertThrows(ErroEntidadeNaoEncontrada.class, () -> service.buscarArvore(99L));
    }

    @Test
    @DisplayName("Deve buscar siglas subordinadas recursivamente")
    void deveBuscarSiglasSubordinadasRecursivamente() {
        UnidadeDto netoDto = UnidadeDto.builder().codigo(3L).sigla("NETO").subunidades(new ArrayList<>()).build();
        UnidadeDto filhoDto = UnidadeDto.builder().codigo(2L).sigla("FILHO").subunidades(List.of(netoDto)).build();
        UnidadeDto raizDto = UnidadeDto.builder().codigo(1L).sigla("RAIZ").subunidades(List.of(filhoDto)).build();

        Unidade raiz = Unidade.builder().codigo(1L).build();
        Unidade filho = Unidade.builder().codigo(2L).unidadeSuperior(raiz).build();
        Unidade neto = Unidade.builder().codigo(3L).unidadeSuperior(filho).build();

        when(unidadeRepo.findAllWithHierarquia()).thenReturn(List.of(raiz, filho, neto));
        when(usuarioMapper.toUnidadeDto(any(), eq(true))).thenAnswer(inv -> {
            Unidade u = inv.getArgument(0);
            if (u.getCodigo().equals(1L)) return raizDto;
            if (u.getCodigo().equals(2L)) return filhoDto;
            if (u.getCodigo().equals(3L)) return netoDto;
            return null;
        });

        List<String> siglas = service.buscarSiglasSubordinadas("RAIZ");
        assertThat(siglas).containsExactlyInAnyOrder("RAIZ", "FILHO", "NETO");

        List<String> siglasFilho = service.buscarSiglasSubordinadas("FILHO");
        assertThat(siglasFilho).containsExactlyInAnyOrder("FILHO", "NETO");

        assertThrows(ErroEntidadeNaoEncontrada.class, () -> service.buscarSiglasSubordinadas("INEXISTENTE"));
    }

    @Test
    @DisplayName("Deve buscar sigla superior")
    void deveBuscarSiglaSuperior() {
        Unidade superior = Unidade.builder().codigo(1L).sigla("PAI").build();
        Unidade unidade = Unidade.builder().codigo(2L).sigla("FILHO").unidadeSuperior(superior).build();
        Unidade raiz = Unidade.builder().codigo(3L).sigla("RAIZ").unidadeSuperior(null).build();

        when(unidadeRepo.findBySigla("FILHO")).thenReturn(Optional.of(unidade));
        when(unidadeRepo.findBySigla("RAIZ")).thenReturn(Optional.of(raiz));
        when(unidadeRepo.findBySigla("X")).thenReturn(Optional.empty());

        assertThat(service.buscarSiglaSuperior("FILHO")).isPresent().contains("PAI");
        assertThat(service.buscarSiglaSuperior("RAIZ")).isEmpty();
        assertThrows(ErroEntidadeNaoEncontrada.class, () -> service.buscarSiglaSuperior("X"));
    }

    @Test
    @DisplayName("Deve lidar com subunidades vazias ou nulas ao montar hierarquia")
    void deveLidarComSubunidadesVazias() {
        // Testa especificamente o branch if (filhas == null || filhas.isEmpty()) em montarComSubunidades
        // Para isso, precisamos de um cenário onde uma unidade não tenha filhas no mapaFilhas

        Unidade unidadeIsolada = Unidade.builder().codigo(1L).build();
        UnidadeDto dto = UnidadeDto.builder().codigo(1L).subunidades(new ArrayList<>()).build();

        when(unidadeRepo.findAllWithHierarquia()).thenReturn(List.of(unidadeIsolada));
        when(usuarioMapper.toUnidadeDto(unidadeIsolada, true)).thenReturn(dto);

        List<UnidadeDto> arvore = service.buscarArvoreHierarquica();
        assertThat(arvore).hasSize(1);
        assertThat(arvore.get(0).getSubunidades()).isEmpty();
    }

    @Test
    @DisplayName("Deve lidar com órfão na montagem da hierarquia (pai não está na lista)")
    void deveLidarComOrfaoEmMontarHierarquia() {
        // Pai existe no objeto filho, mas não está na lista retornada pelo repo
        Unidade pai = Unidade.builder().codigo(999L).build();
        Unidade filho = Unidade.builder().codigo(2L).unidadeSuperior(pai).build();
        UnidadeDto filhoDto = UnidadeDto.builder().codigo(2L).subunidades(new ArrayList<>()).build();

        when(unidadeRepo.findAllWithHierarquia()).thenReturn(List.of(filho));
        when(usuarioMapper.toUnidadeDto(filho, true)).thenReturn(filhoDto);

        List<UnidadeDto> arvore = service.buscarArvoreHierarquica();

        assertThat(arvore).isEmpty();
    }
}
