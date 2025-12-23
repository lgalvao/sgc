package sgc.subprocesso.internal.mappers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import sgc.processo.internal.model.Processo;
import sgc.processo.internal.model.TipoProcesso;
import sgc.subprocesso.api.SubprocessoDetalheDto;
import sgc.subprocesso.api.SubprocessoPermissoesDto;
import sgc.subprocesso.internal.model.SituacaoSubprocesso;
import sgc.subprocesso.internal.model.Subprocesso;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class SubprocessoDetalheMapperTest {

    private SubprocessoDetalheMapper mapper;

    @Mock
    private MovimentacaoMapper movimentacaoMapper;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(SubprocessoDetalheMapper.class);
        ReflectionTestUtils.setField(mapper, "movimentacaoMapper", movimentacaoMapper);
    }

    @Test
    void toDto() {
        Subprocesso sp = new Subprocesso();
        sp.setCodigo(1L);
        sp.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
        Processo p = new Processo();
        p.setTipo(TipoProcesso.MAPEAMENTO);
        sp.setProcesso(p);

        SubprocessoPermissoesDto permissoes = SubprocessoPermissoesDto.builder().build();

        SubprocessoDetalheDto dto = mapper.toDto(sp, null, null, Collections.emptyList(), permissoes);

        assertThat(dto).isNotNull();
        assertThat(dto.getSituacao()).isEqualTo("MAPEAMENTO_CADASTRO_EM_ANDAMENTO");
        assertThat(dto.getTipoProcesso()).isEqualTo("MAPEAMENTO");
    }
}
