package sgc.subprocesso.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Testes da Entidade Movimentacao")
@Tag("unit")
class MovimentacaoTest {

    @Test
    @DisplayName("Deve testar construtor completo")
    void deveTestarConstrutorCompleto() {
        Subprocesso sp = new Subprocesso();
        Usuario user = new Usuario();
        LocalDateTime agora = LocalDateTime.now();
        
        Movimentacao mov = new Movimentacao(1L, sp, user, "Desc", agora);
        
        assertThat(mov.getCodigo()).isEqualTo(1L);
        assertThat(mov.getSubprocesso()).isEqualTo(sp);
        assertThat(mov.getUsuario()).isEqualTo(user);
        assertThat(mov.getDescricao()).isEqualTo("Desc");
        assertThat(mov.getDataHora()).isEqualTo(agora);
    }

    @Test
    @DisplayName("Deve testar construtor de conveniência")
    void deveTestarConstrutorConveniencia() {
        Subprocesso sp = new Subprocesso();
        Unidade orig = new Unidade();
        Unidade dest = new Unidade();
        Usuario user = new Usuario();
        
        Movimentacao mov = new Movimentacao(sp, orig, dest, "Desc", user);
        
        assertThat(mov.getSubprocesso()).isEqualTo(sp);
        assertThat(mov.getUnidadeOrigem()).isEqualTo(orig);
        assertThat(mov.getUnidadeDestino()).isEqualTo(dest);
        assertThat(mov.getDescricao()).isEqualTo("Desc");
        assertThat(mov.getUsuario()).isEqualTo(user);
        assertThat(mov.getDataHora()).isBeforeOrEqualTo(LocalDateTime.now());
    }
}
