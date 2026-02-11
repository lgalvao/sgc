package sgc.subprocesso.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import sgc.organizacao.model.Unidade;
import sgc.subprocesso.dto.MovimentacaoDto;
import sgc.subprocesso.model.Movimentacao;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("unit")
@DisplayName("MovimentacaoMapper")
class MovimentacaoMapperTest {

    private MovimentacaoMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(MovimentacaoMapper.class);
    }

    @Test
    @DisplayName("Deve mapear Movimentacao para MovimentacaoDto")
    void deveMapearParaDto() {
        Unidade origem = new Unidade();
        origem.setCodigo(1L);
        origem.setSigla("ORG");
        origem.setNome("Origem");

        Unidade destino = new Unidade();
        destino.setCodigo(2L);
        destino.setSigla("DES");
        destino.setNome("Destino");

        Movimentacao mov = new Movimentacao();
        mov.setCodigo(10L);
        mov.setDescricao("Movimentacao Teste");
        mov.setDataHora(LocalDateTime.now());
        mov.setUnidadeOrigem(origem);
        mov.setUnidadeDestino(destino);

        MovimentacaoDto dto = mapper.toDto(mov);

        assertThat(dto).isNotNull();
        assertThat(dto.codigo()).isEqualTo(10L);
        assertThat(dto.descricao()).isEqualTo("Movimentacao Teste");
        assertThat(dto.unidadeOrigemCodigo()).isEqualTo(1L);
        assertThat(dto.unidadeOrigemSigla()).isEqualTo("SEDOC");
        assertThat(dto.unidadeOrigemNome()).isEqualTo("Origem");
        assertThat(dto.unidadeDestinoCodigo()).isEqualTo(2L);
        assertThat(dto.unidadeDestinoSigla()).isEqualTo("DES");
        assertThat(dto.unidadeDestinoNome()).isEqualTo("Destino");
    }

    @Test
    @DisplayName("Deve lidar com unidades null")
    void deveLidarComUnidadesNull() {
        Movimentacao mov = new Movimentacao();
        mov.setCodigo(10L);
        mov.setUnidadeOrigem(null);
        mov.setUnidadeDestino(null);

        MovimentacaoDto dto = mapper.toDto(mov);

        assertThat(dto).isNotNull();
        assertThat(dto.unidadeOrigemCodigo()).isNull();
        assertThat(dto.unidadeDestinoCodigo()).isNull();
    }
}
