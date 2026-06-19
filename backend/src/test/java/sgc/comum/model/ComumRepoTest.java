package sgc.comum.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.organizacao.model.SituacaoUnidade;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@Tag("integration")
@DisplayName("ComumRepo")
class ComumRepoTest {

    @Autowired
    private ComumRepo comumRepo;

    @Test
    @DisplayName("deve buscar entidades por codigo e filtros")
    void deveBuscarEntidadesPorCodigoEFiltros() {
        Unidade unidade = comumRepo.buscar(Unidade.class, 8L);
        Usuario usuario = comumRepo.buscar(Usuario.class, "tituloEleitoral", "1");
        Unidade unidadePorSigla = comumRepo.buscarPorSigla(Unidade.class, "sedesenv");

        assertThat(unidade.getSigla()).isEqualTo("SEDESENV");
        assertThat(usuario.getNome()).isEqualTo("Ana Paula Souza");
        assertThat(unidadePorSigla.getCodigo()).isEqualTo(8L);
    }

    @Test
    @DisplayName("deve buscar entidade por filtros compostos e falhar quando inexistente")
    void deveBuscarEntidadePorFiltrosCompostosEFalharQuandoInexistente() {
        Unidade unidade = comumRepo.buscar(Unidade.class, Map.of("sigla", "SEDESENV", "situacao", SituacaoUnidade.ATIVA));

        assertThat(unidade.getCodigo()).isEqualTo(8L);
        assertThatThrownBy(() -> comumRepo.buscar(Unidade.class, 999999L))
                .isInstanceOf(ErroEntidadeNaoEncontrada.class);
    }
}
