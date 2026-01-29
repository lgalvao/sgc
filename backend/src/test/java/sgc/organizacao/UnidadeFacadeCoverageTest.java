package sgc.organizacao;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.comum.erros.ErroValidacao;

import sgc.mapa.model.Mapa;
import sgc.organizacao.dto.CriarAtribuicaoTemporariaRequest;
import sgc.organizacao.dto.ResponsavelDto;
import sgc.organizacao.dto.UnidadeDto;
import sgc.organizacao.model.SituacaoUnidade;
import sgc.organizacao.model.TipoUnidade;
import sgc.organizacao.model.Unidade;

import sgc.organizacao.model.Usuario;
import sgc.organizacao.service.*;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("Testes de Cobertura para UnidadeFacade")
class UnidadeFacadeCoverageTest {

    @InjectMocks
    private UnidadeFacade unidadeFacade;

    @Mock
    private UnidadeHierarquiaService unidadeHierarquiaService;
    @Mock
    private UnidadeMapaService unidadeMapaService;
    @Mock
    private UnidadeResponsavelService unidadeResponsavelService;
    @Mock
    private UnidadeRepositoryService unidadeRepositoryService;
    @Mock
    private UnidadeMapaRepositoryService unidadeMapaRepositoryService;
    @Mock
    private UsuarioRepositoryService usuarioRepositoryService;

    @Captor
    private ArgumentCaptor<Function<Unidade, Boolean>> functionCaptor;

    @Test
    @DisplayName("Deve buscar unidade na hierarquia recursivamente")
    void deveBuscarArvoreRecursiva() {
        UnidadeDto dtoNeta = new UnidadeDto();
        dtoNeta.setCodigo(3L);
        dtoNeta.setSubunidades(new ArrayList<>());

        when(unidadeHierarquiaService.buscarArvore(3L)).thenReturn(dtoNeta);

        UnidadeDto result = unidadeFacade.buscarArvore(3L);
        assertThat(result.getCodigo()).isEqualTo(3L);
        verify(unidadeHierarquiaService).buscarArvore(3L);
    }

    @Test
    @DisplayName("Deve lançar erro ao criar atribuição temporária com datas inválidas")
    void deveLancarErroAtribuicaoDatasInvalidas() {
        CriarAtribuicaoTemporariaRequest req = new CriarAtribuicaoTemporariaRequest(
                "T", LocalDate.now(), LocalDate.now().minusDays(1), "Justificativa");

        doThrow(new ErroValidacao("A data de término deve ser posterior à data de início."))
                .when(unidadeResponsavelService).criarAtribuicaoTemporaria(1L, req);

        assertNotNull(assertThrows(ErroValidacao.class, () -> unidadeFacade.criarAtribuicaoTemporaria(1L, req)));
        verify(unidadeResponsavelService).criarAtribuicaoTemporaria(1L, req);
    }

    @Test
    @DisplayName("Deve retornar mapa vazio ao buscar responsáveis de lista vazia")
    void deveRetornarMapaVazioResponsaveis() {
        when(unidadeResponsavelService.buscarResponsaveisUnidades(Collections.emptyList()))
                .thenReturn(Collections.emptyMap());
        assertThat(unidadeFacade.buscarResponsaveisUnidades(Collections.emptyList())).isEmpty();

        when(unidadeResponsavelService.buscarResponsaveisUnidades(List.of(1L)))
                .thenReturn(Collections.emptyMap());
        assertThat(unidadeFacade.buscarResponsaveisUnidades(List.of(1L))).isEmpty();
    }

    @Test
    @DisplayName("Deve buscar sigla superior")
    void deveBuscarSiglaSuperior() {
        when(unidadeHierarquiaService.buscarSiglaSuperior("FILHO"))
                .thenReturn(Optional.of("PAI"));

        assertThat(unidadeFacade.buscarSiglaSuperior("FILHO")).isPresent().contains("PAI");
        verify(unidadeHierarquiaService).buscarSiglaSuperior("FILHO");
    }

    @Test
    @DisplayName("Deve lançar erro ao buscar sigla superior de unidade inexistente")
    void deveLancarErroBuscarSiglaSuperiorInexistente() {
        when(unidadeHierarquiaService.buscarSiglaSuperior("X"))
                .thenThrow(new ErroEntidadeNaoEncontrada("Unidade não encontrada"));
        assertNotNull(assertThrows(ErroEntidadeNaoEncontrada.class, () -> unidadeFacade.buscarSiglaSuperior("X")));
    }

    @Test
    @DisplayName("Deve buscar siglas subordinadas")
    void deveBuscarSiglasSubordinadas() {
        when(unidadeHierarquiaService.buscarSiglasSubordinadas("RAIZ"))
                .thenReturn(List.of("RAIZ"));

        List<String> siglas = unidadeFacade.buscarSiglasSubordinadas("RAIZ");
        assertThat(siglas).contains("RAIZ");
        verify(unidadeHierarquiaService).buscarSiglasSubordinadas("RAIZ");
    }

    @Test
    @DisplayName("Deve definir mapa vigente")
    void deveDefinirMapaVigente() {
        Mapa mapa = new Mapa();
        unidadeFacade.definirMapaVigente(1L, mapa);
        verify(unidadeMapaService).definirMapaVigente(1L, mapa);
    }

    @Test
    @DisplayName("Deve buscar ids descendentes")
    void deveBuscarIdsDescendentes() {
        when(unidadeHierarquiaService.buscarIdsDescendentes(1L))
                .thenReturn(List.of(2L));

        List<Long> ids = unidadeFacade.buscarIdsDescendentes(1L);
        assertThat(ids).contains(2L);
        verify(unidadeHierarquiaService).buscarIdsDescendentes(1L);
    }

    @Test
    @DisplayName("Deve verificar mapa vigente")
    void deveVerificarMapaVigente() {
        when(unidadeMapaService.verificarMapaVigente(1L)).thenReturn(true);
        assertThat(unidadeFacade.verificarMapaVigente(1L)).isTrue();
        verify(unidadeMapaService).verificarMapaVigente(1L);
    }

    @Test
    @DisplayName("Deve buscar arvore com elegibilidade")
    void deveBuscarArvoreComElegibilidade() {
        UnidadeDto dtoRaiz = new UnidadeDto();
        dtoRaiz.setCodigo(1L);
        dtoRaiz.setSubunidades(new ArrayList<>());

        when(unidadeMapaRepositoryService.findAllUnidadeCodigos()).thenReturn(List.of(1L));
        when(unidadeHierarquiaService.buscarArvoreComElegibilidade(any()))
                .thenReturn(List.of(dtoRaiz));

        List<UnidadeDto> arvore = unidadeFacade.buscarArvoreComElegibilidade(true, Collections.emptySet());
        assertThat(arvore).hasSize(1);
        verify(unidadeHierarquiaService).buscarArvoreComElegibilidade(any());
    }

    @Test
    @DisplayName("Deve buscar responsável atual")
    void deveBuscarResponsavelAtual() {
        Usuario chefeCompleto = new Usuario();
        chefeCompleto.setTituloEleitoral("C");
        when(unidadeResponsavelService.buscarResponsavelAtual("SIGLA"))
                .thenReturn(chefeCompleto);

        Usuario res = unidadeFacade.buscarResponsavelAtual("SIGLA");
        assertThat(res).isNotNull();
        verify(unidadeResponsavelService).buscarResponsavelAtual("SIGLA");
    }

    @Test
    @DisplayName("Deve lançar erro ao buscar responsável atual se unidade não encontrada")
    void deveLancarErroResponsavelAtualUnidadeNaoEncontrada() {
        when(unidadeResponsavelService.buscarResponsavelAtual("SIGLA"))
                .thenThrow(new ErroEntidadeNaoEncontrada("Unidade não encontrada"));
        assertNotNull(assertThrows(ErroEntidadeNaoEncontrada.class, () -> unidadeFacade.buscarResponsavelAtual("SIGLA")));
    }

    @Test
    @DisplayName("Deve buscar responsáveis de múltiplas unidades")
    void deveBuscarResponsaveisMultiplasUnidades() {
        ResponsavelDto dto = new ResponsavelDto(1L, "T", "Nome", null, null);
        when(unidadeResponsavelService.buscarResponsaveisUnidades(List.of(1L)))
                .thenReturn(Map.of(1L, dto));

        Map<Long, ResponsavelDto> map = unidadeFacade.buscarResponsaveisUnidades(List.of(1L));
        assertThat(map).containsKey(1L);
        verify(unidadeResponsavelService).buscarResponsaveisUnidades(List.of(1L));
    }

    @Test
    @DisplayName("Deve montar DTO com titular e substituto")
    void deveMontarDtoComTitularESubstituto() {
        ResponsavelDto dto = new ResponsavelDto(1L, "T", "Titular", "S", "Substituto");
        when(unidadeResponsavelService.buscarResponsavelUnidade(1L))
                .thenReturn(dto);

        ResponsavelDto result = unidadeFacade.buscarResponsavelUnidade(1L);

        assertThat(result.titularTitulo()).isEqualTo("T");
        assertThat(result.substitutoTitulo()).isEqualTo("S");
        assertThat(result.substitutoNome()).isEqualTo("Substituto");
        verify(unidadeResponsavelService).buscarResponsavelUnidade(1L);
    }

    @Test
    @DisplayName("Deve lançar erro ao buscar responsável unidade não encontrada")
    void deveLancarErroBuscarResponsavelUnidadeNaoEncontrada() {
        when(unidadeResponsavelService.buscarResponsavelUnidade(1L))
                .thenThrow(new ErroEntidadeNaoEncontrada("Unidade não encontrada"));
        assertNotNull(assertThrows(ErroEntidadeNaoEncontrada.class, () -> unidadeFacade.buscarResponsavelUnidade(1L)));
    }

    @Test
    @DisplayName("Deve buscar siglas subordinadas recursivamente")
    void deveBuscarSiglasSubordinadasRecursivo() {
        when(unidadeHierarquiaService.buscarSiglasSubordinadas("FILHO"))
                .thenReturn(List.of("FILHO"));

        List<String> siglas = unidadeFacade.buscarSiglasSubordinadas("FILHO");
        assertThat(siglas).contains("FILHO");
        verify(unidadeHierarquiaService).buscarSiglasSubordinadas("FILHO");
    }

    @Test
    @DisplayName("Deve aplicar regras de elegibilidade corretamente (negativo)")
    void deveAplicarRegrasElegibilidadeNegativo() {
        UnidadeDto dto1 = new UnidadeDto();
        dto1.setCodigo(1L);
        dto1.setSubunidades(new ArrayList<>());
        UnidadeDto dto2 = new UnidadeDto();
        dto2.setCodigo(2L);
        dto2.setSubunidades(new ArrayList<>());
        UnidadeDto dto3 = new UnidadeDto();
        dto3.setCodigo(3L);
        dto3.setSubunidades(new ArrayList<>());

        when(unidadeMapaRepositoryService.findAllUnidadeCodigos()).thenReturn(List.of(3L));
        when(unidadeHierarquiaService.buscarArvoreComElegibilidade(any()))
                .thenReturn(List.of(dto1, dto2, dto3));

        // Testar com requerMapaVigente=true e Bloqueios
        List<UnidadeDto> result = unidadeFacade.buscarArvoreComElegibilidade(true, Set.of(3L));

        assertThat(result).hasSize(3);
        verify(unidadeHierarquiaService).buscarArvoreComElegibilidade(any());
    }

    @Test
    @DisplayName("Deve lançar erro ao buscar entidade por id se unidade inativa")
    void deveLancarErroBuscarEntidadePorIdInativa() {
        Unidade u = new Unidade();
        u.setCodigo(1L);
        u.setSituacao(SituacaoUnidade.INATIVA);

        when(unidadeRepositoryService.buscarPorId(1L)).thenReturn(u);

        assertNotNull(assertThrows(ErroEntidadeNaoEncontrada.class, () -> unidadeFacade.buscarEntidadePorId(1L)));
    }

    @Test
    @DisplayName("Deve filtrar unidades na árvore com elegibilidade corretamente")
    void deveFiltrarUnidadesNaArvoreComElegibilidade() {
        // Setup
        when(unidadeMapaRepositoryService.findAllUnidadeCodigos()).thenReturn(List.of(10L, 20L));

        // Execute method to trigger lambda creation
        unidadeFacade.buscarArvoreComElegibilidade(true, Set.of(30L));

        // Capture function
        verify(unidadeHierarquiaService).buscarArvoreComElegibilidade(functionCaptor.capture());
        Function<Unidade, Boolean> function = functionCaptor.getValue();

        // Test assertions
        Unidade uOk = new Unidade();
        uOk.setCodigo(10L);
        uOk.setTipo(TipoUnidade.OPERACIONAL);
        assertThat(function.apply(uOk)).isTrue(); // In map, not blocked, not intermediary

        Unidade uNotInMap = new Unidade();
        uNotInMap.setCodigo(99L);
        uNotInMap.setTipo(TipoUnidade.OPERACIONAL);
        assertThat(function.apply(uNotInMap)).isFalse(); // Not in map

        Unidade uBlocked = new Unidade();
        uBlocked.setCodigo(30L);
        uBlocked.setTipo(TipoUnidade.OPERACIONAL);
        assertThat(function.apply(uBlocked)).isFalse(); // Blocked

        Unidade uIntermediary = new Unidade();
        uIntermediary.setCodigo(10L);
        uIntermediary.setTipo(TipoUnidade.INTERMEDIARIA);
        assertThat(function.apply(uIntermediary)).isFalse(); // Intermediary (even if in map and not blocked)
    }

    @Test
    @DisplayName("Deve filtrar unidades na árvore com elegibilidade (sem requerer mapa)")
    void deveFiltrarUnidadesNaArvoreComElegibilidadeSemMapa() {
        // Execute method
        unidadeFacade.buscarArvoreComElegibilidade(false, Set.of(30L));

        // Capture function
        verify(unidadeHierarquiaService).buscarArvoreComElegibilidade(functionCaptor.capture());
        Function<Unidade, Boolean> function = functionCaptor.getValue();

        // Test assertions
        Unidade uOk = new Unidade();
        uOk.setCodigo(99L);
        uOk.setTipo(TipoUnidade.OPERACIONAL);
        assertThat(function.apply(uOk)).isTrue(); // Not in map but map not required, not blocked

        Unidade uBlocked = new Unidade();
        uBlocked.setCodigo(30L);
        uBlocked.setTipo(TipoUnidade.OPERACIONAL);
        assertThat(function.apply(uBlocked)).isFalse(); // Blocked

        Unidade uIntermediary = new Unidade();
        uIntermediary.setCodigo(99L);
        uIntermediary.setTipo(TipoUnidade.INTERMEDIARIA);
        assertThat(function.apply(uIntermediary)).isFalse(); // Intermediary
    }

    @Test
    @DisplayName("Deve buscar árvore com elegibilidade sem requerer mapa")
    void deveBuscarArvoreComElegibilidadeSemRequererMapa() {
        unidadeFacade.buscarArvoreComElegibilidade(false, Collections.emptySet());

        // Verify that findAllUnidadeCodigos was NOT called
        verify(unidadeMapaRepositoryService, never()).findAllUnidadeCodigos();
        verify(unidadeHierarquiaService).buscarArvoreComElegibilidade(any());
    }


}
