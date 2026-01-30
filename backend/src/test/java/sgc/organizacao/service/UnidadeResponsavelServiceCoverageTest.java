package sgc.organizacao.service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import sgc.comum.erros.ErroValidacao;
import sgc.comum.repo.ComumRepo;
import sgc.organizacao.dto.CriarAtribuicaoTemporariaRequest;
import sgc.organizacao.dto.UnidadeResponsavelDto;
import sgc.organizacao.mapper.UsuarioMapper;
import sgc.organizacao.model.AtribuicaoTemporaria;
import sgc.organizacao.model.AtribuicaoTemporariaRepo;
import sgc.organizacao.model.Perfil;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.UnidadeRepo;
import sgc.organizacao.model.Usuario;
import sgc.organizacao.model.UsuarioPerfil;
import sgc.organizacao.model.UsuarioPerfilRepo;
import sgc.organizacao.model.UsuarioRepo;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("Testes de Cobertura para UnidadeResponsavelService")
class UnidadeResponsavelServiceCoverageTest {

    @InjectMocks
    private UnidadeResponsavelService service;

    @Mock
    private UnidadeRepo unidadeRepo;
    @Mock
    private UsuarioRepo usuarioRepo;
    @Mock
    private UsuarioPerfilRepo usuarioPerfilRepo;
    @Mock
    private AtribuicaoTemporariaRepo atribuicaoTemporariaRepo;
    @Mock
    private UsuarioMapper usuarioMapper;
    @Mock
    private ComumRepo repo;

    @Test
    @DisplayName("Deve criar atribuição temporária com data de início nula (usa data atual)")
    void deveCriarAtribuicaoComDataInicioNula() {
        CriarAtribuicaoTemporariaRequest request = new CriarAtribuicaoTemporariaRequest(
                "123", null, LocalDate.now().plusDays(1), "Justificativa");

        Unidade unidade = new Unidade();
        Usuario usuario = new Usuario();
        usuario.setTituloEleitoral("123");
        usuario.setMatricula("MAT123");

        when(repo.buscar(Unidade.class, 1L)).thenReturn(unidade);
        when(repo.buscar(Usuario.class, "123")).thenReturn(usuario);

        service.criarAtribuicaoTemporaria(1L, request);

        verify(atribuicaoTemporariaRepo).save(any(AtribuicaoTemporaria.class));
    }

    @Test
    @DisplayName("Deve validar data termino anterior ao inicio quando inicio é nulo (usa hoje)")
    void deveValidarDataTerminoAnteriorHojeQuandoInicioNulo() {
        CriarAtribuicaoTemporariaRequest request = new CriarAtribuicaoTemporariaRequest(
                "123", null, LocalDate.now().minusDays(1), "Justificativa");

        Unidade unidade = new Unidade();
        Usuario usuario = new Usuario();
        usuario.setTituloEleitoral("123");

        when(repo.buscar(Unidade.class, 1L)).thenReturn(unidade);
        when(repo.buscar(Usuario.class, "123")).thenReturn(usuario);

        assertThrows(ErroValidacao.class, () -> service.criarAtribuicaoTemporaria(1L, request));
    }

    @Test
    @DisplayName("Deve retornar mapa vazio quando lista de códigos vazia")
    void deveRetornarMapaVazioListaVazia() {
        Map<Long, UnidadeResponsavelDto> result = service.buscarResponsaveisUnidades(Collections.emptyList());
        assertThat(result).isEmpty();
        verifyNoInteractions(usuarioRepo);
    }

    @Test
    @DisplayName("Deve retornar mapa vazio quando não encontra chefes")
    void deveRetornarMapaVazioSemChefes() {
        when(usuarioRepo.findChefesByUnidadesCodigos(any())).thenReturn(Collections.emptyList());

        Map<Long, UnidadeResponsavelDto> result = service.buscarResponsaveisUnidades(List.of(1L));
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Deve carregar atribuições em lote corretamente")
    void deveCarregarAtribuicoesEmLote() {
        Usuario chefeSimples = new Usuario();
        chefeSimples.setTituloEleitoral("123");

        Usuario chefeCompleto = new Usuario();
        chefeCompleto.setTituloEleitoral("123");
        chefeCompleto.setNome("Chefe");

        UsuarioPerfil perfil = new UsuarioPerfil();
        perfil.setUsuarioTitulo("123");
        perfil.setUnidadeCodigo(1L);
        perfil.setPerfil(Perfil.CHEFE);

        when(usuarioRepo.findChefesByUnidadesCodigos(List.of(1L))).thenReturn(List.of(chefeSimples));
        when(usuarioRepo.findByIdInWithAtribuicoes(List.of("123"))).thenReturn(List.of(chefeCompleto));
        when(usuarioPerfilRepo.findByUsuarioTitulo("123")).thenReturn(List.of(perfil));

        Map<Long, UnidadeResponsavelDto> result = service.buscarResponsaveisUnidades(List.of(1L));

        assertThat(result).hasSize(1);
        assertThat(result.get(1L).titularTitulo()).isEqualTo("123");
    }

    @Test
    @DisplayName("Deve buscar responsável da unidade com substituto")
    void deveBuscarResponsavelUnidadeComSubstituto() {
        Usuario titular = new Usuario();
        titular.setTituloEleitoral("TITULAR");
        titular.setNome("Nome Titular");

        Usuario substituto = new Usuario();
        substituto.setTituloEleitoral("SUBSTITUTO");
        substituto.setNome("Nome Substituto");

        when(usuarioRepo.findChefesByUnidadesCodigos(List.of(1L)))
                .thenReturn(List.of(titular, substituto));

        UnidadeResponsavelDto result = service.buscarResponsavelUnidade(1L);

        assertThat(result.titularTitulo()).isEqualTo("TITULAR");
        assertThat(result.titularNome()).isEqualTo("Nome Titular");
        assertThat(result.substitutoTitulo()).isEqualTo("SUBSTITUTO");
        assertThat(result.substitutoNome()).isEqualTo("Nome Substituto");
    }

    @Test
    @DisplayName("Deve lançar erro quando não encontra responsável da unidade")
    void deveLancarErroQuandoNaoEncontraResponsavelUnidade() {
        when(usuarioRepo.findChefesByUnidadesCodigos(List.of(1L))).thenReturn(Collections.emptyList());
        assertThrows(ErroEntidadeNaoEncontrada.class, () -> service.buscarResponsavelUnidade(1L));
    }

    @Test
    @DisplayName("Deve buscar responsáveis filtrando perfil não CHEFE")
    void deveBuscarResponsaveisFiltrandoPerfilNaoChefe() {
        Usuario chefe = new Usuario();
        chefe.setTituloEleitoral("123");

        UsuarioPerfil perfilGestor = new UsuarioPerfil();
        perfilGestor.setUsuarioTitulo("123");
        perfilGestor.setUnidadeCodigo(1L);
        perfilGestor.setPerfil(Perfil.GESTOR);

        when(usuarioRepo.findChefesByUnidadesCodigos(List.of(1L))).thenReturn(List.of(chefe));
        when(usuarioRepo.findByIdInWithAtribuicoes(List.of("123"))).thenReturn(List.of(chefe));
        when(usuarioPerfilRepo.findByUsuarioTitulo("123")).thenReturn(List.of(perfilGestor));

        Map<Long, UnidadeResponsavelDto> result = service.buscarResponsaveisUnidades(List.of(1L));

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Deve carregar atribuições em lote para usuário sem perfil")
    void deveCarregarAtribuicoesEmLoteUsuarioSemPerfil() {
        Usuario chefe = new Usuario();
        chefe.setTituloEleitoral("123");

        when(usuarioRepo.findChefesByUnidadesCodigos(List.of(1L))).thenReturn(List.of(chefe));
        when(usuarioRepo.findByIdInWithAtribuicoes(List.of("123"))).thenReturn(List.of(chefe));
        when(usuarioPerfilRepo.findByUsuarioTitulo("123")).thenReturn(Collections.emptyList());

        Map<Long, UnidadeResponsavelDto> result = service.buscarResponsaveisUnidades(List.of(1L));

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Deve buscar responsáveis em lote com substituto")
    void deveBuscarResponsaveisEmLoteComSubstituto() {
        Usuario titular = new Usuario();
        titular.setTituloEleitoral("TITULAR");
        titular.setNome("Nome Titular");

        Usuario substituto = new Usuario();
        substituto.setTituloEleitoral("SUBSTITUTO");
        substituto.setNome("Nome Substituto");

        when(usuarioRepo.findChefesByUnidadesCodigos(List.of(1L))).thenReturn(List.of(titular, substituto));
        when(usuarioRepo.findByIdInWithAtribuicoes(anyList())).thenReturn(List.of(titular, substituto));

        UsuarioPerfil perfilTitular = new UsuarioPerfil();
        perfilTitular.setUsuarioTitulo("TITULAR");
        perfilTitular.setUnidadeCodigo(1L);
        perfilTitular.setPerfil(Perfil.CHEFE);

        UsuarioPerfil perfilSubstituto = new UsuarioPerfil();
        perfilSubstituto.setUsuarioTitulo("SUBSTITUTO");
        perfilSubstituto.setUnidadeCodigo(1L);
        perfilSubstituto.setPerfil(Perfil.CHEFE);

        when(usuarioPerfilRepo.findByUsuarioTitulo("TITULAR")).thenReturn(List.of(perfilTitular));
        when(usuarioPerfilRepo.findByUsuarioTitulo("SUBSTITUTO")).thenReturn(List.of(perfilSubstituto));

        Map<Long, UnidadeResponsavelDto> result = service.buscarResponsaveisUnidades(List.of(1L));

        assertThat(result).hasSize(1);
        UnidadeResponsavelDto resp = result.get(1L);
        assertThat(resp.titularTitulo()).isEqualTo("TITULAR");
        assertThat(resp.substitutoTitulo()).isEqualTo("SUBSTITUTO");
    }

    @Test
    @DisplayName("Deve filtrar atribuições que não pertencem às unidades pesquisadas")
    void deveFiltrarAtribuicoesDeOutrasUnidades() {
        Usuario titular = new Usuario();
        titular.setTituloEleitoral("TITULAR");

        when(usuarioRepo.findChefesByUnidadesCodigos(List.of(10L))).thenReturn(List.of(titular));
        when(usuarioRepo.findByIdInWithAtribuicoes(anyList())).thenReturn(List.of(titular));

        UsuarioPerfil perfilOutraUnidade = new UsuarioPerfil();
        perfilOutraUnidade.setUsuarioTitulo("TITULAR");
        perfilOutraUnidade.setUnidadeCodigo(99L); // Diferente de 10L
        perfilOutraUnidade.setPerfil(Perfil.CHEFE);

        when(usuarioPerfilRepo.findByUsuarioTitulo("TITULAR")).thenReturn(List.of(perfilOutraUnidade));

        Map<Long, UnidadeResponsavelDto> result = service.buscarResponsaveisUnidades(List.of(10L));

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Deve buscar responsável atual e carregar atribuições")
    void deveBuscarResponsavelAtual() {
        String sigla = "SIGLA";
        Unidade unidade = new Unidade();
        unidade.setCodigo(1L);

        Usuario chefeSimples = new Usuario();
        chefeSimples.setTituloEleitoral("123");

        Usuario chefeCompleto = new Usuario();
        chefeCompleto.setTituloEleitoral("123");

        when(unidadeRepo.findBySigla(sigla)).thenReturn(Optional.of(unidade));
        when(usuarioRepo.chefePorCodUnidade(1L)).thenReturn(Optional.of(chefeSimples));
        when(usuarioRepo.findByIdWithAtribuicoes("123")).thenReturn(Optional.of(chefeCompleto));

        Usuario result = service.buscarResponsavelAtual(sigla);

        assertThat(result).isSameAs(chefeCompleto);
    }
}