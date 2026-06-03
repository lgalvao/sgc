package sgc.organizacao.dto;

import org.junit.jupiter.api.*;
import sgc.organizacao.*;
import sgc.organizacao.model.*;

import static org.assertj.core.api.Assertions.*;

@DisplayName("UsuarioConsultaDto")
class UsuarioConsultaDtoTest {
    private final OrganizacaoDtoMapper mapper = new OrganizacaoDtoMapper();


    @Test
    @DisplayName("deve mapear leitura com unidade obrigatoria")
    void deveMapearLeituraComUnidadeObrigatoria() {
        UsuarioConsultaLeitura usuario = new UsuarioConsultaLeitura(
                "123",
                "0001",
                "João",
                "joao@tre.jus.br",
                "1234",
                10L,
                "Unidade",
                "UND",
                TipoUnidade.OPERACIONAL,
                "999",
                10L);

        UsuarioConsultaDto dto = mapper.paraUsuarioConsultaDto(usuario);

        assertThat(dto.unidade()).isNotNull();
        assertThat(dto.unidade().codigo()).isEqualTo(10L);
        assertThat(dto.unidade().sigla()).isEqualTo("UND");
    }
}
