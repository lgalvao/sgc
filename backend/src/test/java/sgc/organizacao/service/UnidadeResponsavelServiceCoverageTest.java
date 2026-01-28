package sgc.organizacao.service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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
import sgc.comum.repo.RepositorioComum;
import sgc.organizacao.dto.CriarAtribuicaoTemporariaRequest;
import sgc.organizacao.dto.ResponsavelDto;
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
    private RepositorioComum repo;

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
        Map<Long, ResponsavelDto> result = service.buscarResponsaveisUnidades(Collections.emptyList());
        assertThat(result).isEmpty();
        verifyNoInteractions(usuarioRepo);
    }

    @Test
    @DisplayName("Deve retornar mapa vazio quando não encontra chefes")
    void deveRetornarMapaVazioSemChefes() {
        when(usuarioRepo.findChefesByUnidadesCodigos(any())).thenReturn(Collections.emptyList());

        Map<Long, ResponsavelDto> result = service.buscarResponsaveisUnidades(List.of(1L));
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Deve carregar atribuições em lote corretamente")
    void deveCarregarAtribuicoesEmLote() {
        // Este teste verifica indiretamente carregarAtribuicoesEmLote através de buscarResponsaveisUnidades
        Usuario chefeSimples = new Usuario();
        chefeSimples.setTituloEleitoral("123");

        Usuario chefeCompleto = new Usuario();
        chefeCompleto.setTituloEleitoral("123");
        chefeCompleto.setNome("Chefe");

        UsuarioPerfil perfil = new UsuarioPerfil();
        perfil.setUsuarioTitulo("123");
        perfil.setUnidadeCodigo(1L);
        perfil.setPerfil(Perfil.CHEFE); // Importante para o filtro em buscarResponsaveisUnidades

        when(usuarioRepo.findChefesByUnidadesCodigos(List.of(1L))).thenReturn(List.of(chefeSimples));
        when(usuarioRepo.findByIdInWithAtribuicoes(List.of("123"))).thenReturn(List.of(chefeCompleto));
        when(usuarioPerfilRepo.findByUsuarioTituloIn(List.of("123"))).thenReturn(List.of(perfil));

        Map<Long, ResponsavelDto> result = service.buscarResponsaveisUnidades(List.of(1L));

        assertThat(result).hasSize(1);
        assertThat(result.get(1L).titularTitulo()).isEqualTo("123");

        // Verifica se setAtribuicoes foi chamado no chefeCompleto (embora difícil de verificar diretamente sem spy, o fluxo depende disso)
        assertThat(chefeCompleto.getAtribuicoes()).contains(perfil);
    }

    @Test
    @DisplayName("Deve lidar com lista de usuários vazia em carregarAtribuicoesEmLote")
    void deveLidarComListaUsuariosVaziaEmCarregarAtribuicoes() {
        Usuario chefeSimples = new Usuario();
        chefeSimples.setTituloEleitoral("123");

        when(usuarioRepo.findChefesByUnidadesCodigos(List.of(1L))).thenReturn(List.of(chefeSimples));
        when(usuarioRepo.findByIdInWithAtribuicoes(List.of("123"))).thenReturn(Collections.emptyList());

        // Se retornar lista vazia de usuarios completos, o stream final será vazio
        Map<Long, ResponsavelDto> result = service.buscarResponsaveisUnidades(List.of(1L));
        assertThat(result).isEmpty();

        // Verifica que não tentou buscar perfis se não tem usuários
        verify(usuarioPerfilRepo, never()).findByUsuarioTituloIn(any());
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

        ResponsavelDto result = service.buscarResponsavelUnidade(1L);

        assertThat(result.titularTitulo()).isEqualTo("TITULAR");
        assertThat(result.titularNome()).isEqualTo("Nome Titular");
        assertThat(result.substitutoTitulo()).isEqualTo("SUBSTITUTO");
        assertThat(result.substitutoNome()).isEqualTo("Nome Substituto");
    }

    @Test
    @DisplayName("Deve buscar responsáveis filtrando perfil não CHEFE")
    void deveBuscarResponsaveisFiltrandoPerfilNaoChefe() {
        Usuario chefe = new Usuario();
        chefe.setTituloEleitoral("123");

        UsuarioPerfil perfilGestor = new UsuarioPerfil();
        perfilGestor.setUsuarioTitulo("123");
        perfilGestor.setUnidadeCodigo(1L);
        perfilGestor.setPerfil(Perfil.GESTOR); // Não é CHEFE

        when(usuarioRepo.findChefesByUnidadesCodigos(List.of(1L))).thenReturn(List.of(chefe));
        when(usuarioRepo.findByIdInWithAtribuicoes(List.of("123"))).thenReturn(List.of(chefe));
        when(usuarioPerfilRepo.findByUsuarioTituloIn(List.of("123"))).thenReturn(List.of(perfilGestor));

        Map<Long, ResponsavelDto> result = service.buscarResponsaveisUnidades(List.of(1L));

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Deve carregar atribuições em lote para usuário sem perfil")
    void deveCarregarAtribuicoesEmLoteUsuarioSemPerfil() {
        Usuario chefe = new Usuario();
        chefe.setTituloEleitoral("123");

        when(usuarioRepo.findChefesByUnidadesCodigos(List.of(1L))).thenReturn(List.of(chefe));
        when(usuarioRepo.findByIdInWithAtribuicoes(List.of("123"))).thenReturn(List.of(chefe));
        when(usuarioPerfilRepo.findByUsuarioTituloIn(List.of("123"))).thenReturn(Collections.emptyList());

        Map<Long, ResponsavelDto> result = service.buscarResponsaveisUnidades(List.of(1L));

        assertThat(result).isEmpty();
        assertThat(chefe.getAtribuicoes()).isEmpty();
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

        when(usuarioPerfilRepo.findByUsuarioTituloIn(anyList())).thenReturn(List.of(perfilTitular, perfilSubstituto));

        Map<Long, ResponsavelDto> result = service.buscarResponsaveisUnidades(List.of(1L));

        assertThat(result).hasSize(1);
        ResponsavelDto resp = result.get(1L);
        assertThat(resp.titularTitulo()).isEqualTo("TITULAR");
        assertThat(resp.substitutoTitulo()).isEqualTo("SUBSTITUTO");
    }
}
