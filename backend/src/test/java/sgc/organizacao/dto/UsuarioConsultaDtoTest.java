package sgc.organizacao.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sgc.organizacao.OrganizacaoDtoMapper;
import sgc.organizacao.model.TipoUnidade;
import sgc.organizacao.model.UsuarioConsultaLeitura;

import static org.assertj.core.api.Assertions.assertThat;

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
