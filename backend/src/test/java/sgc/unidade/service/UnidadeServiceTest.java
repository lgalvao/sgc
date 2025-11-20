package sgc.unidade.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.mapa.model.MapaRepo;
import sgc.mapa.model.Mapa;
import sgc.sgrh.dto.UnidadeDto;
import sgc.sgrh.model.Usuario;
import sgc.sgrh.model.UsuarioRepo;
import sgc.unidade.dto.CriarAtribuicaoTemporariaRequest;
import sgc.unidade.model.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UnidadeServiceTest {

    @InjectMocks
    private UnidadeService unidadeService;

    @Mock
    private UnidadeRepo unidadeRepo;

    @Mock
    private MapaRepo mapaRepo;

    @Mock
    private UsuarioRepo usuarioRepo;

    @Mock
    private AtribuicaoTemporariaRepo atribuicaoTemporariaRepo;

    @Test
    @DisplayName("buscarTodasUnidades deve retornar hierarquia correta")
    void buscarTodasUnidades() {
        Unidade raiz = new Unidade("Raiz", "RAIZ");
        raiz.setCodigo(1L);
        raiz.setTipo(TipoUnidade.OPERACIONAL);

        Unidade filha = new Unidade("Filha", "FILHA");
        filha.setCodigo(2L);
        filha.setTipo(TipoUnidade.OPERACIONAL);
        filha.setUnidadeSuperior(raiz);

        when(unidadeRepo.findAll()).thenReturn(Arrays.asList(raiz, filha));

        List<UnidadeDto> resultado = unidadeService.buscarTodasUnidades();

        assertThat(resultado).hasSize(1);
        UnidadeDto dtoRaiz = resultado.get(0);
        assertThat(dtoRaiz.getSigla()).isEqualTo("RAIZ");
        assertThat(dtoRaiz.getSubunidades()).hasSize(1);
        assertThat(dtoRaiz.getSubunidades().get(0).getSigla()).isEqualTo("FILHA");
    }

    @Test
    @DisplayName("criarAtribuicaoTemporaria deve salvar atribuicao com sucesso")
    void criarAtribuicaoTemporaria() {
        Long unidadeId = 1L;
        String usuarioId = "123456789012";
        CriarAtribuicaoTemporariaRequest req = new CriarAtribuicaoTemporariaRequest(
            usuarioId, LocalDate.now().plusDays(5), "Justificativa"
        );

        Unidade unidade = new Unidade();
        unidade.setCodigo(unidadeId);

        Usuario usuario = new Usuario();
        usuario.setTituloEleitoral(usuarioId);

        when(unidadeRepo.findById(unidadeId)).thenReturn(Optional.of(unidade));
        when(usuarioRepo.findById(usuarioId)).thenReturn(Optional.of(usuario));

        unidadeService.criarAtribuicaoTemporaria(unidadeId, req);

        verify(atribuicaoTemporariaRepo).save(any(AtribuicaoTemporaria.class));
    }

    @Test
    @DisplayName("verificarMapaVigente deve retornar verdadeiro se mapa existe")
    void verificarMapaVigente() {
        Long unidadeId = 1L;
        when(mapaRepo.findMapaVigenteByUnidade(unidadeId)).thenReturn(Optional.of(new Mapa()));

        boolean existe = unidadeService.verificarMapaVigente(unidadeId);

        assertThat(existe).isTrue();
    }

    @Test
    @DisplayName("buscarServidoresPorUnidade deve retornar lista de servidores")
    void buscarServidoresPorUnidade() {
        Long unidadeId = 1L;
        Unidade unidade = new Unidade();
        unidade.setCodigo(unidadeId);

        Usuario usuario = new Usuario();
        usuario.setTituloEleitoral("123");
        usuario.setNome("Teste");
        usuario.setEmail("teste@email.com");
        usuario.setUnidade(unidade);

        when(usuarioRepo.findByUnidadeCodigo(unidadeId)).thenReturn(List.of(usuario));

        var resultado = unidadeService.buscarServidoresPorUnidade(unidadeId);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getNome()).isEqualTo("Teste");
    }

    @Test
    @DisplayName("buscarPorSigla deve retornar unidade se existir")
    void buscarPorSigla() {
        String sigla = "TESTE";
        Unidade unidade = new Unidade("Nome", sigla);
        unidade.setCodigo(1L);
        unidade.setTipo(TipoUnidade.OPERACIONAL);

        when(unidadeRepo.findBySigla(sigla)).thenReturn(Optional.of(unidade));

        UnidadeDto dto = unidadeService.buscarPorSigla(sigla);

        assertThat(dto.getSigla()).isEqualTo(sigla);
    }

    @Test
    @DisplayName("buscarPorId deve retornar unidade se existir")
    void buscarPorId() {
        Long id = 1L;
        Unidade unidade = new Unidade("Nome", "SIGLA");
        unidade.setCodigo(id);
        unidade.setTipo(TipoUnidade.OPERACIONAL);

        when(unidadeRepo.findById(id)).thenReturn(Optional.of(unidade));

        UnidadeDto dto = unidadeService.buscarPorId(id);

        assertThat(dto.getCodigo()).isEqualTo(id);
    }

    @Test
    @DisplayName("buscarPorId deve lançar exceção se não encontrar")
    void buscarPorIdException() {
        Long id = 99L;
        when(unidadeRepo.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> unidadeService.buscarPorId(id))
            .isInstanceOf(ErroEntidadeNaoEncontrada.class);
    }
}
