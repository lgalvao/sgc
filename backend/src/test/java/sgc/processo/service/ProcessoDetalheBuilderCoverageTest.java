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
        processo.setSituacao(sgc.processo.model.SituacaoProcesso.EM_ANDAMENTO);
        processo.setTipo(sgc.processo.model.TipoProcesso.MAPEAMENTO);
        
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

    @Test
    @DisplayName("build deve mapear subprocesso e mapa quando existem")
    void deveMapearSubprocessoEMapa() {
        // Arrange
        Long codProcesso = 1L;
        Processo processo = new Processo();
        processo.setCodigo(codProcesso);
        processo.setSituacao(sgc.processo.model.SituacaoProcesso.EM_ANDAMENTO);
        processo.setTipo(sgc.processo.model.TipoProcesso.MAPEAMENTO);
        
        UnidadeProcesso participante = new UnidadeProcesso();
        participante.setUnidadeCodigo(100L);
        processo.setParticipantes(List.of(participante));

        sgc.organizacao.model.Unidade unidade = new sgc.organizacao.model.Unidade();
        unidade.setCodigo(100L);

        sgc.subprocesso.model.Subprocesso sp = new sgc.subprocesso.model.Subprocesso();
        sp.setCodigo(500L);
        sp.setUnidade(unidade);
        sp.setSituacao(sgc.subprocesso.model.SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
        sp.setMapa(sgc.mapa.model.Mapa.builder().codigo(900L).build());

        when(subprocessoRepo.findByProcessoCodigoWithUnidade(codProcesso)).thenReturn(List.of(sp));
        
        ProcessoDetalheDto.UnidadeParticipanteDto unidadeDto = new ProcessoDetalheDto.UnidadeParticipanteDto();
        unidadeDto.setCodUnidade(100L);
        when(processoDetalheMapper.fromSnapshot(participante)).thenReturn(unidadeDto);

        // Act
        ProcessoDetalheDto dto = builder.build(processo, new Usuario());

        // Assert
        assertThat(dto.getUnidades()).hasSize(1);
        ProcessoDetalheDto.UnidadeParticipanteDto result = dto.getUnidades().get(0);
        assertThat(result.getCodSubprocesso()).isEqualTo(500L);
    }

    @Test
    @DisplayName("build deve ignorar quando sp existe mas unidadeDto é nulo")
    void deveIgnorarQuandoSpExisteMasUnidadeDtoNulo() {
        // Arrange
        Long codProcesso = 1L;
        Processo processo = new Processo();
        processo.setCodigo(codProcesso);
        processo.setSituacao(sgc.processo.model.SituacaoProcesso.EM_ANDAMENTO);
        processo.setTipo(sgc.processo.model.TipoProcesso.MAPEAMENTO);
        
        UnidadeProcesso participante = new UnidadeProcesso();
        participante.setUnidadeCodigo(100L);
        processo.setParticipantes(List.of(participante));

        sgc.organizacao.model.Unidade unidade = new sgc.organizacao.model.Unidade();
        unidade.setCodigo(100L);

        sgc.subprocesso.model.Subprocesso sp = new sgc.subprocesso.model.Subprocesso();
        sp.setUnidade(unidade);

        when(subprocessoRepo.findByProcessoCodigoWithUnidade(codProcesso)).thenReturn(List.of(sp));
        when(processoDetalheMapper.fromSnapshot(participante)).thenReturn(null);

        // Act
        ProcessoDetalheDto dto = builder.build(processo, new Usuario());

        // Assert
        assertThat(dto.getUnidades()).isEmpty();
    }

    @Test
    @DisplayName("build deve manter unidadeDto quando sp é nulo")
    void deveManterUnidadeDtoQuandoSpNulo() {
        // Arrange
        Long codProcesso = 1L;
        Processo processo = new Processo();
        processo.setCodigo(codProcesso);
        processo.setSituacao(sgc.processo.model.SituacaoProcesso.EM_ANDAMENTO);
        processo.setTipo(sgc.processo.model.TipoProcesso.MAPEAMENTO);
        
        UnidadeProcesso participante = new UnidadeProcesso();
        participante.setUnidadeCodigo(100L);
        processo.setParticipantes(List.of(participante));

        when(subprocessoRepo.findByProcessoCodigoWithUnidade(codProcesso)).thenReturn(new ArrayList<>());
        
        ProcessoDetalheDto.UnidadeParticipanteDto unidadeDto = new ProcessoDetalheDto.UnidadeParticipanteDto();
        unidadeDto.setCodUnidade(100L);
        unidadeDto.setSigla("TESTE");
        when(processoDetalheMapper.fromSnapshot(participante)).thenReturn(unidadeDto);

        // Act
        ProcessoDetalheDto dto = builder.build(processo, new Usuario());

        // Assert
        assertThat(dto.getUnidades()).hasSize(1);
        assertThat(dto.getUnidades().get(0).getSigla()).isEqualTo("TESTE");
        assertThat(dto.getUnidades().get(0).getCodSubprocesso()).isNull();
    }
}
