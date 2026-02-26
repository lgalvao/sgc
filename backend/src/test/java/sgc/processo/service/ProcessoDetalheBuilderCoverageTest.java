package sgc.processo.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import sgc.mapa.model.*;
import sgc.organizacao.model.*;
import sgc.processo.dto.*;
import sgc.processo.model.*;
import sgc.seguranca.*;
import sgc.subprocesso.model.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProcessoDetalheBuilder - Cobertura Adicional")
class ProcessoDetalheBuilderCoverageTest {

    @Mock
    private SubprocessoRepo subprocessoRepo;
    @Mock
    private SgcPermissionEvaluator permissionEvaluator;

    @InjectMocks
    private ProcessoDetalheBuilder builder;


    @Test
    @DisplayName("build deve mapear subprocesso e mapa quando existem")
    void deveMapearSubprocessoEMapa() {
        // Arrange
        Long codProcesso = 1L;
        Processo processo = new Processo();
        processo.setCodigo(codProcesso);
        processo.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        processo.setTipo(TipoProcesso.MAPEAMENTO);
        
        UnidadeProcesso participante = new UnidadeProcesso();
        participante.setUnidadeCodigo(100L);
        processo.setParticipantes(List.of(participante));

        Unidade unidade = new Unidade();
        unidade.setCodigo(100L);

        Subprocesso sp = new Subprocesso();
        sp.setCodigo(500L);
        sp.setUnidade(unidade);
        sp.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
        sp.setMapa(Mapa.builder().codigo(900L).build());

        when(subprocessoRepo.findByProcessoCodigoWithUnidade(codProcesso)).thenReturn(List.of(sp));
        
        when(subprocessoRepo.findByProcessoCodigoWithUnidade(codProcesso)).thenReturn(List.of(sp));

        // Act
        ProcessoDetalheDto dto = builder.build(processo, new Usuario());

        // Assert
        assertThat(dto.getUnidades()).hasSize(1);
        ProcessoDetalheDto.UnidadeParticipanteDto result = dto.getUnidades().getFirst();
        assertThat(result.getCodSubprocesso()).isEqualTo(500L);
    }


    @Test
    @DisplayName("build deve manter unidadeDto quando sp é nulo")
    void deveManterUnidadeDtoQuandoSpNulo() {
        // Arrange
        Long codProcesso = 1L;
        Processo processo = new Processo();
        processo.setCodigo(codProcesso);
        processo.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        processo.setTipo(TipoProcesso.MAPEAMENTO);
        
        UnidadeProcesso participante = new UnidadeProcesso();
        participante.setUnidadeCodigo(100L);
        participante.setSigla("TESTE");
        processo.setParticipantes(List.of(participante));

        when(subprocessoRepo.findByProcessoCodigoWithUnidade(codProcesso)).thenReturn(new ArrayList<>());
        
        when(subprocessoRepo.findByProcessoCodigoWithUnidade(codProcesso)).thenReturn(new ArrayList<>());

        // Act
        ProcessoDetalheDto dto = builder.build(processo, new Usuario());

        // Assert
        assertThat(dto.getUnidades()).hasSize(1);
        assertThat(dto.getUnidades().getFirst().getSigla()).isEqualTo("TESTE");
        assertThat(dto.getUnidades().getFirst().getCodSubprocesso()).isNull();
    }
}
