package sgc.parametros;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import sgc.comum.erros.*;
import sgc.parametros.model.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConfiguracaoServiceTest {
    @Mock
    private ParametroRepo parametroRepo;

    @InjectMocks
    private ConfiguracaoService configuracaoService;

    @Test
    @DisplayName("Deve buscar todos os parâmetros")
    void deveBuscarTodos() {
        when(parametroRepo.findAll()).thenReturn(Collections.singletonList(new Parametro()));

        List<Parametro> resultado = configuracaoService.buscarTodos();

        assertThat(resultado).isNotEmpty();
        verify(parametroRepo).findAll();
    }

    @Test
    @DisplayName("Deve buscar parâmetro por chave existente")
    void deveBuscarPorChaveExistente() {
        String chave = "TESTE";
        Parametro parametro = new Parametro();
        parametro.setChave(chave);
        when(parametroRepo.findByChave(chave)).thenReturn(Optional.of(parametro));

        Parametro resultado = configuracaoService.buscarPorChave(chave);

        assertThat(resultado.getChave()).isEqualTo(chave);
    }

    @Test
    @DisplayName("Deve lançar erro ao buscar parâmetro inexistente")
    void deveLancarErroAoBuscarInexistente() {
        String chave = "INEXISTENTE";
        when(parametroRepo.findByChave(chave)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> configuracaoService.buscarPorChave(chave))
                .isInstanceOf(ErroConfiguracao.class);
    }

    @Test
    @DisplayName("Deve salvar lista de parâmetros")
    void deveSalvarLista() {
        List<Parametro> lista = Collections.singletonList(new Parametro());
        when(parametroRepo.saveAll(lista)).thenReturn(lista);

        List<Parametro> resultado = configuracaoService.salvar(lista);

        assertThat(resultado).isNotNull();
        verify(parametroRepo).saveAll(lista);
    }

    @Test
    @DisplayName("Deve atualizar parâmetro existente")
    void deveAtualizarParametro() {
        String chave = "TESTE";
        String novoValor = "NOVO_VALOR";
        Parametro parametro = new Parametro();
        parametro.setChave(chave);
        parametro.setValor("ANTIGO");

        when(parametroRepo.findByChave(chave)).thenReturn(Optional.of(parametro));
        when(parametroRepo.save(parametro)).thenReturn(parametro);

        Parametro resultado = configuracaoService.atualizar(chave, novoValor);

        assertThat(resultado.getValor()).isEqualTo(novoValor);
        verify(parametroRepo).save(parametro);
    }

    @Test
    @DisplayName("Deve buscar parâmetro por ID existente")
    void devebuscarPorCodigoExistente() {
        Long id = 1L;
        Parametro p = new Parametro();
        when(parametroRepo.findById(id)).thenReturn(Optional.of(p));

        assertThat(configuracaoService.buscarPorCodigo(id)).isEqualTo(p);
    }

    @Test
    @DisplayName("Deve lançar erro ao buscar por ID inexistente")
    void deveLancarErroAobuscarPorCodigoInexistente() {
        Long id = 1L;
        when(parametroRepo.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> configuracaoService.buscarPorCodigo(id))
                .isInstanceOf(ErroConfiguracao.class);
    }
}
