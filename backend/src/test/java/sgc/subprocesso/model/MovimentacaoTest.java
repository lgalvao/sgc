package sgc.subprocesso.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sgc.organizacao.model.Usuario;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Testes da Entidade Movimentacao")
class MovimentacaoTest {

    @Test
    @DisplayName("Deve testar builder completo")
    void deveTestarBuilderCompleto() {
        Subprocesso sp = new Subprocesso();
        Usuario user = new Usuario();
        LocalDateTime agora = LocalDateTime.now();

        Movimentacao mov = Movimentacao.builder()
                .subprocesso(sp)
                .usuario(user)
                .descricao("Desc")
                .dataHora(agora)
                .build();
        mov.setCodigo(1L);

        assertThat(mov.getCodigo()).isEqualTo(1L);
        assertThat(mov.getSubprocesso()).isEqualTo(sp);
        assertThat(mov.getUsuario()).isEqualTo(user);
        assertThat(mov.getDescricao()).isEqualTo("Desc");
        assertThat(mov.getDataHora()).isEqualTo(agora);
    }


}
