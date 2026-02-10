package sgc.analise.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.analise.dto.AnaliseHistoricoDto;
import sgc.analise.dto.AnaliseValidacaoHistoricoDto;
import sgc.analise.model.Analise;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.UnidadeRepo;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("AnaliseMapper")
class AnaliseMapperTest {

    private AnaliseMapper mapper;

    @Mock
    private UnidadeRepo unidadeRepo;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(AnaliseMapper.class);
        mapper.unidadeRepo = unidadeRepo;
    }

    @Test
    @DisplayName("Deve mapear Analise para AnaliseHistoricoDto")
    void deveMapearParaAnaliseHistoricoDto() {
        Unidade unidade = new Unidade();
        unidade.setCodigo(100L);
        unidade.setSigla("TESTE");
        when(unidadeRepo.findById(100L)).thenReturn(Optional.of(unidade));

        Analise analise = new Analise();
        analise.setCodigo(1L);
        analise.setUnidadeCodigo(100L);
        analise.setUsuarioTitulo("12345");
        analise.setObservacoes("Obs");
        analise.setDataHora(LocalDateTime.now());

        AnaliseHistoricoDto dto = mapper.toAnaliseHistoricoDto(analise);

        assertThat(dto).isNotNull();
        assertThat(dto.unidadeSigla()).isEqualTo("TESTE");
        assertThat(dto.analistaUsuarioTitulo()).isEqualTo("12345");
        assertThat(dto.observacoes()).isEqualTo("Obs");
    }

    @Test
    @DisplayName("Deve mapear Analise para AnaliseValidacaoHistoricoDto")
    void deveMapearParaAnaliseValidacaoHistoricoDto() {
        Unidade unidade = new Unidade();
        unidade.setCodigo(200L);
        unidade.setSigla("SIGLA");
        when(unidadeRepo.findById(200L)).thenReturn(Optional.of(unidade));

        Analise analise = new Analise();
        analise.setCodigo(2L);
        analise.setUnidadeCodigo(200L);
        analise.setUsuarioTitulo("67890");
        analise.setObservacoes("Validacao");
        analise.setDataHora(LocalDateTime.now());

        AnaliseValidacaoHistoricoDto dto = mapper.toAnaliseValidacaoHistoricoDto(analise);

        assertThat(dto).isNotNull();
        assertThat(dto.unidadeSigla()).isEqualTo("SIGLA");
        assertThat(dto.analistaUsuarioTitulo()).isEqualTo("67890");
        assertThat(dto.observacoes()).isEqualTo("Validacao");
    }
}
