package sgc.configuracao;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.comum.erros.ErroConfiguracao;
import sgc.configuracao.model.Parametro;
import sgc.configuracao.model.ParametroRepo;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

        assertFalse(resultado.isEmpty());
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

        assertNotNull(resultado);
        assertEquals(chave, resultado.getChave());
    }

    @Test
    @DisplayName("Deve lançar erro ao buscar parâmetro inexistente")
    void deveLancarErroAoBuscarInexistente() {
        String chave = "INEXISTENTE";
        when(parametroRepo.findByChave(chave)).thenReturn(Optional.empty());

        assertThrows(ErroConfiguracao.class, () -> configuracaoService.buscarPorChave(chave));
    }

    @Test
    @DisplayName("Deve salvar lista de parâmetros")
    void deveSalvarLista() {
        List<Parametro> lista = Collections.singletonList(new Parametro());
        when(parametroRepo.saveAll(lista)).thenReturn(lista);

        List<Parametro> resultado = configuracaoService.salvar(lista);

        assertNotNull(resultado);
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

        assertEquals(novoValor, resultado.getValor());
        verify(parametroRepo).save(parametro);
    }

    @Test
    @DisplayName("Deve buscar parâmetro por ID existente")
    void deveBuscarPorIdExistente() {
        Long id = 1L;
        Parametro p = new Parametro();
        when(parametroRepo.findById(id)).thenReturn(Optional.of(p));

        assertEquals(p, configuracaoService.buscarPorId(id));
    }

    @Test
    @DisplayName("Deve lançar erro ao buscar por ID inexistente")
    void deveLancarErroAoBuscarPorIdInexistente() {
        Long id = 1L;
        when(parametroRepo.findById(id)).thenReturn(Optional.empty());

        assertThrows(ErroConfiguracao.class, () -> configuracaoService.buscarPorId(id));
    }
}
