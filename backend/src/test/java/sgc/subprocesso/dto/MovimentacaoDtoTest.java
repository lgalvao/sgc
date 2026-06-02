package sgc.subprocesso.dto;

import org.junit.jupiter.api.*;
import sgc.organizacao.*;
import sgc.organizacao.model.*;
import sgc.subprocesso.*;
import sgc.subprocesso.model.*;

import java.time.*;

import static org.assertj.core.api.Assertions.*;

@DisplayName("MovimentacaoDto")
class MovimentacaoDtoTest {
    private final SubprocessoDtoMapper mapper = new SubprocessoDtoMapper(new OrganizacaoDtoMapper());

    @Test
    @DisplayName("deve mapear movimentacao com origem e destino obrigatorios")
    void deveMapearMovimentacaoComOrigemEDestinoObrigatorios() {
        Movimentacao movimentacao = criarMovimentacao();

        MovimentacaoDto dto = mapper.paraMovimentacao(movimentacao);

        assertThat(dto.codigo()).isEqualTo(7L);
        assertThat(dto.unidadeOrigemSigla()).isEqualTo("ORG");
        assertThat(dto.unidadeDestinoSigla()).isEqualTo("DST");
        assertThat(dto.usuarioNome()).isEqualTo("Servidor");
    }

    private Movimentacao criarMovimentacao() {
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
        return movimentacao;
    }
}
