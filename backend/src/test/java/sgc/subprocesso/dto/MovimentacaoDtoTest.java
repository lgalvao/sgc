package sgc.subprocesso.dto;

import org.junit.jupiter.api.*;
import sgc.organizacao.model.*;
import sgc.subprocesso.model.*;

import java.time.*;

import static org.assertj.core.api.Assertions.*;

@DisplayName("MovimentacaoDto")
class MovimentacaoDtoTest {

    @Test
    @DisplayName("deve mapear movimentacao com destino opcional")
    void deveMapearMovimentacaoComDestinoOpcional() {
        Unidade origem = new Unidade();
        origem.setCodigo(1L);
        origem.setSigla("ORG");
        origem.setNome("Origem");

        Unidade destino = new Unidade();
        destino.setCodigo(2L);
        destino.setSigla("DST");
        destino.setNome("Destino");

        Usuario usuario = new Usuario();
        usuario.setTituloEleitoral("123");
        usuario.setNome("Servidor");

        Movimentacao movimentacao = new Movimentacao();
        movimentacao.setCodigo(7L);
        movimentacao.setDataHora(LocalDateTime.of(2025, 1, 1, 10, 0));
        movimentacao.setUnidadeOrigem(origem);
        movimentacao.setUnidadeDestino(destino);
        movimentacao.setUsuario(usuario);
        movimentacao.setDescricao("Movimentação");

        MovimentacaoDto dto = MovimentacaoDto.from(movimentacao);

        assertThat(dto.codigo()).isEqualTo(7L);
        assertThat(dto.unidadeOrigemSigla()).isEqualTo("ORG");
        assertThat(dto.unidadeDestinoSigla()).isEqualTo("DST");
        assertThat(dto.usuarioNome()).isEqualTo("Servidor");
    }
}
