package sgc.configuracoes;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import sgc.comum.erros.*;
import sgc.configuracoes.model.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConfiguracaoServiceTest {
    @Mock
    private ConfiguracaoRepo configuracaoRepo;

    @InjectMocks
    private ConfiguracaoService configuracaoService;

    @Test
    @DisplayName("Deve buscar todos os Configuraçãos")
    void deveBuscarTodos() {
        when(configuracaoRepo.findAll()).thenReturn(Collections.singletonList(new Configuracao()));

        List<Configuracao> resultado = configuracaoService.buscarTodos();

        assertThat(resultado).isNotEmpty();
        verify(configuracaoRepo).findAll();
    }

    @Test
    @DisplayName("Deve buscar Configuração por chave existente")
    void deveBuscarPorChaveExistente() {
        String chave = "TESTE";
        Configuracao configuracao = new Configuracao();
        configuracao.setChave(chave);
        when(configuracaoRepo.findByChave(chave)).thenReturn(Optional.of(configuracao));

        Configuracao resultado = configuracaoService.buscarPorChave(chave);

        assertThat(resultado.getChave()).isEqualTo(chave);
    }

    @Test
    @DisplayName("Deve lançar erro ao buscar Configuração inexistente")
    void deveLancarErroAoBuscarInexistente() {
        String chave = "INEXISTENTE";
        when(configuracaoRepo.findByChave(chave)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> configuracaoService.buscarPorChave(chave))
                .isInstanceOf(ErroConfiguracao.class);
    }

    @Test
    @DisplayName("Deve salvar lista de Configuraçãos")
    void deveSalvarLista() {
        List<Configuracao> lista = Collections.singletonList(new Configuracao());
        when(configuracaoRepo.saveAll(lista)).thenReturn(lista);

        List<Configuracao> resultado = configuracaoService.salvar(lista);

        assertThat(resultado).isNotNull();
        verify(configuracaoRepo).saveAll(lista);
    }

    @Test
    @DisplayName("Deve atualizar Configuração existente")
    void deveAtualizarParametro() {
        String chave = "TESTE";
        String novoValor = "NOVO_VALOR";
        Configuracao configuracao = new Configuracao();
        configuracao.setChave(chave);
        configuracao.setValor("ANTIGO");

        when(configuracaoRepo.findByChave(chave)).thenReturn(Optional.of(configuracao));
        when(configuracaoRepo.save(configuracao)).thenReturn(configuracao);

        Configuracao resultado = configuracaoService.atualizar(chave, novoValor);

        assertThat(resultado.getValor()).isEqualTo(novoValor);
        verify(configuracaoRepo).save(configuracao);
    }

    @Test
    @DisplayName("Deve buscar Configuração por ID existente")
    void devebuscarPorCodigoExistente() {
        Long id = 1L;
        Configuracao p = new Configuracao();
        when(configuracaoRepo.findById(id)).thenReturn(Optional.of(p));

        assertThat(configuracaoService.buscarPorCodigo(id)).isEqualTo(p);
    }

    @Test
    @DisplayName("Deve lançar erro ao buscar por ID inexistente")
    void deveLancarErroAobuscarPorCodigoInexistente() {
        Long id = 1L;
        when(configuracaoRepo.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> configuracaoService.buscarPorCodigo(id))
                .isInstanceOf(ErroConfiguracao.class);
    }
}
