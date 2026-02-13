package sgc.processo.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.organizacao.model.Usuario;
import sgc.processo.dto.ProcessoDetalheDto;
import sgc.processo.mapper.ProcessoDetalheMapper;
import sgc.processo.model.Processo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;
import sgc.processo.model.UnidadeProcesso;
import sgc.seguranca.acesso.AccessControlService;
import sgc.subprocesso.model.SubprocessoRepo;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("ProcessoDetalheBuilder - Cobertura Adicional")
class ProcessoDetalheBuilderCoverageTest {

    @Mock
    private SubprocessoRepo subprocessoRepo;
    @Mock
    private ProcessoDetalheMapper processoDetalheMapper;
    @Mock
    private AccessControlService accessControlService;

    @InjectMocks
    private ProcessoDetalheBuilder builder;

    @Test
    @DisplayName("build deve lidar com unidadeDto nulo")
    void deveLidarComUnidadeDtoNulo() {
        // Arrange
        Processo processo = new Processo();
        processo.setCodigo(1L);
        processo.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        processo.setTipo(TipoProcesso.MAPEAMENTO);
        
        UnidadeProcesso participante = new UnidadeProcesso();
        participante.setUnidadeCodigo(100L);
        processo.setParticipantes(List.of(participante));

        Usuario usuario = new Usuario();

        when(subprocessoRepo.findByProcessoCodigoWithUnidade(1L)).thenReturn(new ArrayList<>());
        // Simular que o mapper retorna null para o snapshot (cobertura da linha 83)
        when(processoDetalheMapper.fromSnapshot(participante)).thenReturn(null);

        // Act
        ProcessoDetalheDto dto = builder.build(processo, usuario);

        // Assert
        assertThat(dto.getUnidades()).isEmpty();
    }
}
