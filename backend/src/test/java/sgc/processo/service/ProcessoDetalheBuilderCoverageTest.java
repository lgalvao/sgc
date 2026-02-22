package sgc.processo.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.mapa.model.Mapa;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.processo.dto.ProcessoDetalheDto;
import sgc.processo.model.Processo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;
import sgc.processo.model.UnidadeProcesso;
import sgc.seguranca.AccessControlService;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
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
    private AccessControlService accessControlService;

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
        ProcessoDetalheDto.UnidadeParticipanteDto result = dto.getUnidades().get(0);
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
        assertThat(dto.getUnidades().get(0).getSigla()).isEqualTo("TESTE");
        assertThat(dto.getUnidades().get(0).getCodSubprocesso()).isNull();
    }
}
