package sgc.configuracoes;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.comum.erros.ErroConfiguracao;
import sgc.configuracoes.model.Configuracao;
import sgc.configuracoes.model.ConfiguracaoRepo;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

    @Test
    @DisplayName("buscarValorInteiro deve retornar o valor padrão se a configuração não existir")
    void buscarValorInteiroInexistenteDeveRetornarPadrao() {
        String chave = "CHAVE_TESTE";
        when(configuracaoRepo.findByChave(chave)).thenReturn(Optional.empty());

        int resultado = configuracaoService.buscarValorInteiro(chave, 42);

        assertThat(resultado).isEqualTo(42);
    }

    @Test
    @DisplayName("buscarValorInteiro deve retornar o valor convertido se for válido")
    void buscarValorInteiroValidoDeveRetornarConvertido() {
        String chave = "CHAVE_TESTE";
        Configuracao configuracao = new Configuracao();
        configuracao.setChave(chave);
        configuracao.setValor("15");
        when(configuracaoRepo.findByChave(chave)).thenReturn(Optional.of(configuracao));

        int resultado = configuracaoService.buscarValorInteiro(chave, 42);

        assertThat(resultado).isEqualTo(15);
    }

    @Test
    @DisplayName("buscarValorInteiro deve lançar erro se o valor for menor que 1")
    void buscarValorInteiroMenorQueUmDeveLancarErro() {
        String chave = "CHAVE_TESTE";
        Configuracao configuracao = new Configuracao();
        configuracao.setChave(chave);
        configuracao.setValor("0");
        when(configuracaoRepo.findByChave(chave)).thenReturn(Optional.of(configuracao));

        assertThatThrownBy(() -> configuracaoService.buscarValorInteiro(chave, 42))
                .isInstanceOf(ErroConfiguracao.class)
                .hasMessageContaining("deve ser maior ou igual a 1");
    }

    @Test
    @DisplayName("buscarValorInteiro deve lançar erro se o valor possuir formato inválido")
    void buscarValorInteiroInvalidoDeveLancarErro() {
        String chave = "CHAVE_TESTE";
        Configuracao configuracao = new Configuracao();
        configuracao.setChave(chave);
        configuracao.setValor("invalido");
        when(configuracaoRepo.findByChave(chave)).thenReturn(Optional.of(configuracao));

        assertThatThrownBy(() -> configuracaoService.buscarValorInteiro(chave, 42))
                .isInstanceOf(ErroConfiguracao.class)
                .hasMessageContaining("possui valor inválido");
    }

    @Test
    @DisplayName("buscarDiasInativacaoProcesso deve retornar valor da configuração")
    void buscarDiasInativacaoProcessoDeveRetornarValor() {
        Configuracao configuracao = new Configuracao();
        configuracao.setChave(ConfiguracaoService.CHAVE_DIAS_INATIVACAO_PROCESSO);
        configuracao.setValor("5");
        when(configuracaoRepo.findByChave(ConfiguracaoService.CHAVE_DIAS_INATIVACAO_PROCESSO))
                .thenReturn(Optional.of(configuracao));

        int resultado = configuracaoService.buscarDiasInativacaoProcesso();

        assertThat(resultado).isEqualTo(5);
    }

    @Test
    @DisplayName("buscarDiasAlertaNovo deve retornar valor da configuração")
    void buscarDiasAlertaNovoDeveRetornarValor() {
        Configuracao configuracao = new Configuracao();
        configuracao.setChave(ConfiguracaoService.CHAVE_DIAS_ALERTA_NOVO);
        configuracao.setValor("2");
        when(configuracaoRepo.findByChave(ConfiguracaoService.CHAVE_DIAS_ALERTA_NOVO))
                .thenReturn(Optional.of(configuracao));

        int resultado = configuracaoService.buscarDiasAlertaNovo();

        assertThat(resultado).isEqualTo(2);
    }
}
