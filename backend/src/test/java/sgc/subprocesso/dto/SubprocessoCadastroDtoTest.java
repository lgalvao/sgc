package sgc.subprocesso.dto;

import org.junit.jupiter.api.*;
import sgc.mapa.dto.*;
import sgc.organizacao.model.*;
import sgc.subprocesso.model.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

@DisplayName("SubprocessoCadastroDto")
class SubprocessoCadastroDtoTest {

    @Test
    @DisplayName("deve mapear subprocesso com unidade resumida")
    void deveMapearSubprocessoComUnidadeResumida() {
        Unidade unidade = new Unidade();
        unidade.setCodigo(10L);
        unidade.setNome("Unidade");
        unidade.setSigla("UND");
        unidade.setTipo(TipoUnidade.OPERACIONAL);
        unidade.setTituloTitular("999");

        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(20L);
        subprocesso.setUnidade(unidade);

        List<AtividadeDto> atividades = List.of(AtividadeDto.builder().codigo(1L).descricao("A").conhecimentos(List.of()).build());

        SubprocessoCadastroDto dto = SubprocessoCadastroDto.fromEntity(subprocesso, atividades);

        assertThat(dto.codigo()).isEqualTo(20L);
        assertThat(dto.unidade()).isNotNull();
        assertThat(dto.unidade().sigla()).isEqualTo("UND");
        assertThat(dto.atividades()).hasSize(1);
    }

    @Test
    @DisplayName("deve falhar quando subprocesso nao possuir unidade")
    void deveFalharQuandoSubprocessoNaoPossuirUnidade() {
        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(20L);

        List<AtividadeDto> atividades = List.of();

        assertThatThrownBy(() -> SubprocessoCadastroDto.fromEntity(subprocesso, atividades))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Subprocesso deve possuir unidade associada");
    }
}
