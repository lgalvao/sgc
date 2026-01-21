package sgc.organizacao;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.comum.erros.ErroValidacao;
import sgc.comum.repo.RepositorioComum;
import sgc.mapa.model.Mapa;
import sgc.organizacao.dto.CriarAtribuicaoTemporariaRequest;
import sgc.organizacao.dto.ResponsavelDto;
import sgc.organizacao.dto.UnidadeDto;
import sgc.organizacao.mapper.UsuarioMapper;
import sgc.organizacao.model.*;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("Testes de Cobertura para UnidadeFacade")
class UnidadeFacadeCoverageTest {

    @InjectMocks
    private UnidadeFacade unidadeFacade;

    @Mock private UnidadeRepo unidadeRepo;
    @Mock private UsuarioMapper usuarioMapper;
    @Mock private RepositorioComum repo;
    @Mock private UnidadeMapaRepo unidadeMapaRepo;
    @Mock private UsuarioRepo usuarioRepo;
    @Mock private AtribuicaoTemporariaRepo atribuicaoTemporariaRepo;
    @Mock private UsuarioPerfilRepo usuarioPerfilRepo;

    @Test
    @DisplayName("Deve buscar unidade na hierarquia recursivamente")
    void deveBuscarArvoreRecursiva() {
        Unidade raiz = new Unidade(); raiz.setCodigo(1L); raiz.setSigla("RAIZ");
        Unidade filha = new Unidade(); filha.setCodigo(2L); filha.setSigla("FILHA"); filha.setUnidadeSuperior(raiz);
        Unidade neta = new Unidade(); neta.setCodigo(3L); neta.setSigla("NETA"); neta.setUnidadeSuperior(filha);

        when(unidadeRepo.findAllWithHierarquia()).thenReturn(List.of(raiz, filha, neta));

        UnidadeDto dtoRaiz = new UnidadeDto(); dtoRaiz.setCodigo(1L); dtoRaiz.setSubunidades(new ArrayList<>());
        UnidadeDto dtoFilha = new UnidadeDto(); dtoFilha.setCodigo(2L); dtoFilha.setSubunidades(new ArrayList<>());
        UnidadeDto dtoNeta = new UnidadeDto(); dtoNeta.setCodigo(3L); dtoNeta.setSubunidades(new ArrayList<>());

        when(usuarioMapper.toUnidadeDto(any(Unidade.class), anyBoolean())).thenAnswer(inv -> {
           Unidade u = inv.getArgument(0);
           if (u.getCodigo() == 1L) return dtoRaiz;
           if (u.getCodigo() == 2L) return dtoFilha;
           if (u.getCodigo() == 3L) return dtoNeta;
           return null;
        });

        UnidadeDto result = unidadeFacade.buscarArvore(3L);
        assertThat(result.getCodigo()).isEqualTo(3L);
    }

    @Test
    @DisplayName("Deve lançar erro ao criar atribuição temporária com datas inválidas")
    void deveLancarErroAtribuicaoDatasInvalidas() {
        CriarAtribuicaoTemporariaRequest req = new CriarAtribuicaoTemporariaRequest(
                "T", LocalDate.now(), LocalDate.now().minusDays(1), "Justificativa");

        when(repo.buscar(Unidade.class, 1L)).thenReturn(new Unidade());
        when(repo.buscar(Usuario.class, "T")).thenReturn(new Usuario());

        assertThrows(ErroValidacao.class, () -> unidadeFacade.criarAtribuicaoTemporaria(1L, req));
    }

    @Test
    @DisplayName("Deve retornar mapa vazio ao buscar responsáveis de lista vazia")
    void deveRetornarMapaVazioResponsaveis() {
        assertThat(unidadeFacade.buscarResponsaveisUnidades(Collections.emptyList())).isEmpty();

        when(usuarioRepo.findChefesByUnidadesCodigos(anyList())).thenReturn(Collections.emptyList());
        assertThat(unidadeFacade.buscarResponsaveisUnidades(List.of(1L))).isEmpty();
    }

    @Test
    @DisplayName("Deve buscar sigla superior")
    void deveBuscarSiglaSuperior() {
        Unidade pai = new Unidade(); pai.setSigla("PAI");
        Unidade filho = new Unidade(); filho.setSigla("FILHO"); filho.setUnidadeSuperior(pai);

        when(unidadeRepo.findBySigla("FILHO")).thenReturn(Optional.of(filho));

        assertThat(unidadeFacade.buscarSiglaSuperior("FILHO")).isPresent().contains("PAI");
    }

    @Test
    @DisplayName("Deve lançar erro ao buscar sigla superior de unidade inexistente")
    void deveLancarErroBuscarSiglaSuperiorInexistente() {
        when(unidadeRepo.findBySigla("X")).thenReturn(Optional.empty());
        assertThrows(ErroEntidadeNaoEncontrada.class, () -> unidadeFacade.buscarSiglaSuperior("X"));
    }

    @Test
    @DisplayName("Deve buscar siglas subordinadas")
    void deveBuscarSiglasSubordinadas() {
        Unidade raiz = new Unidade(); raiz.setCodigo(1L); raiz.setSigla("RAIZ");
        when(unidadeRepo.findAllWithHierarquia()).thenReturn(List.of(raiz));

        UnidadeDto dtoRaiz = new UnidadeDto(); dtoRaiz.setCodigo(1L); dtoRaiz.setSigla("RAIZ"); dtoRaiz.setSubunidades(new ArrayList<>());
        when(usuarioMapper.toUnidadeDto(any(Unidade.class), anyBoolean())).thenReturn(dtoRaiz);

        List<String> siglas = unidadeFacade.buscarSiglasSubordinadas("RAIZ");
        assertThat(siglas).contains("RAIZ");
    }

    @Test
    @DisplayName("Deve definir mapa vigente")
    void deveDefinirMapaVigente() {
        when(unidadeMapaRepo.findById(1L)).thenReturn(Optional.empty());
        unidadeFacade.definirMapaVigente(1L, new Mapa());
        verify(unidadeMapaRepo).save(any(UnidadeMapa.class));
    }

    @Test
    @DisplayName("Deve buscar ids descendentes")
    void deveBuscarIdsDescendentes() {
        Unidade raiz = new Unidade(); raiz.setCodigo(1L);
        Unidade filho = new Unidade(); filho.setCodigo(2L); filho.setUnidadeSuperior(raiz);

        when(unidadeRepo.findAllWithHierarquia()).thenReturn(List.of(raiz, filho));

        List<Long> ids = unidadeFacade.buscarIdsDescendentes(1L);
        assertThat(ids).contains(2L);
    }

    @Test
    @DisplayName("Deve verificar mapa vigente")
    void deveVerificarMapaVigente() {
        when(unidadeMapaRepo.existsById(1L)).thenReturn(true);
        assertThat(unidadeFacade.verificarMapaVigente(1L)).isTrue();
    }

    @Test
    @DisplayName("Deve buscar arvore com elegibilidade")
    void deveBuscarArvoreComElegibilidade() {
        Unidade raiz = new Unidade(); raiz.setCodigo(1L); raiz.setTipo(TipoUnidade.OPERACIONAL);
        when(unidadeRepo.findAllWithHierarquia()).thenReturn(List.of(raiz));

        when(unidadeMapaRepo.findAllUnidadeCodigos()).thenReturn(List.of(1L));

        UnidadeDto dtoRaiz = new UnidadeDto(); dtoRaiz.setCodigo(1L); dtoRaiz.setSubunidades(new ArrayList<>());
        when(usuarioMapper.toUnidadeDto(eq(raiz), anyBoolean())).thenReturn(dtoRaiz);

        List<UnidadeDto> arvore = unidadeFacade.buscarArvoreComElegibilidade(true, Collections.emptySet());
        assertThat(arvore).hasSize(1);
    }

    @Test
    @DisplayName("Deve buscar responsável atual")
    void deveBuscarResponsavelAtual() {
        Unidade un = new Unidade();
        un.setCodigo(1L);
        when(unidadeRepo.findBySigla("SIGLA")).thenReturn(Optional.of(un));

        Usuario chefeSimples = new Usuario();
        chefeSimples.setTituloEleitoral("C");
        when(usuarioRepo.chefePorCodUnidade(1L)).thenReturn(Optional.of(chefeSimples));

        Usuario chefeCompleto = new Usuario();
        chefeCompleto.setTituloEleitoral("C");
        when(usuarioRepo.findByIdWithAtribuicoes("C")).thenReturn(Optional.of(chefeCompleto));

        Usuario res = unidadeFacade.buscarResponsavelAtual("SIGLA");
        assertThat(res).isNotNull();
    }

    @Test
    @DisplayName("Deve lançar erro ao buscar responsável atual se unidade não encontrada")
    void deveLancarErroResponsavelAtualUnidadeNaoEncontrada() {
        when(unidadeRepo.findBySigla("SIGLA")).thenReturn(Optional.empty());
        assertThrows(ErroEntidadeNaoEncontrada.class, () -> unidadeFacade.buscarResponsavelAtual("SIGLA"));
    }

    @Test
    @DisplayName("Deve buscar responsáveis de múltiplas unidades")
    void deveBuscarResponsaveisMultiplasUnidades() {
        Usuario u = new Usuario();
        u.setTituloEleitoral("T");
        u.setNome("Nome");

        UsuarioPerfil up = new UsuarioPerfil();
        up.setUsuario(u);
        up.setUsuarioTitulo("T");
        up.setUnidadeCodigo(1L);
        up.setPerfil(Perfil.CHEFE);
        u.setAtribuicoes(Set.of(up));

        when(usuarioRepo.findChefesByUnidadesCodigos(List.of(1L))).thenReturn(List.of(u));
        when(usuarioRepo.findByIdInWithAtribuicoes(List.of("T"))).thenReturn(List.of(u));
        when(usuarioPerfilRepo.findByUsuarioTituloIn(List.of("T"))).thenReturn(List.of(up));

        Map<Long, ResponsavelDto> map = unidadeFacade.buscarResponsaveisUnidades(List.of(1L));
        assertThat(map).containsKey(1L);
    }
}
