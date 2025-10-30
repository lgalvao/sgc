package sgc.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import sgc.processo.dto.ProcessoDetalheDto;
import sgc.processo.dto.ProcessoDetalheMapperCustom;
import sgc.processo.dto.ProcessoResumoDto;
import sgc.processo.modelo.*;
import sgc.subprocesso.modelo.SituacaoSubprocesso;
import sgc.subprocesso.modelo.Subprocesso;
import sgc.subprocesso.modelo.SubprocessoRepo;
import sgc.unidade.modelo.Unidade;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ProcessoServiceDetalhesTest {
    private static final String DIRETORIA_X = "Diretoria X";

    @Mock
    private ProcessoRepo processoRepo;

    @Mock
    private UnidadeProcessoRepo unidadeProcessoRepo;

    @Mock
    private SubprocessoRepo subprocessoRepo;

    @Mock
    private ProcessoDetalheMapperCustom processoDetalheMapperCustom;

    @Test
    public void obterDetalhes_deveRetornarDtoCompleto_quandoFluxoNormal() {
        Processo p = mock(Processo.class);
        LocalDateTime dataCriacao = LocalDateTime.now();
        LocalDateTime dataLimite = LocalDateTime.now().plusDays(7);

        when(p.getCodigo()).thenReturn(1L);
        when(p.getDescricao()).thenReturn("Proc Teste");
        when(p.getTipo()).thenReturn(TipoProcesso.MAPEAMENTO);
        when(p.getSituacao()).thenReturn(SituacaoProcesso.EM_ANDAMENTO);
        when(p.getDataCriacao()).thenReturn(dataCriacao);
        when(p.getDataLimite()).thenReturn(dataLimite);
        when(p.getDataFinalizacao()).thenReturn(null);

        UnidadeProcesso up = new UnidadeProcesso();
        up.setCodigo(10L);
        up.setNome(DIRETORIA_X);
        up.setSigla("DX");
        up.setSituacao("PENDENTE");
        up.setCodUnidadeSuperior(null);

        Unidade unidade = new Unidade();
        unidade.setCodigo(10L);
        unidade.setSigla("DX");
        unidade.setNome(DIRETORIA_X);

        Subprocesso sp = new Subprocesso();
        sp.setCodigo(100L);
        sp.setUnidade(unidade);
        sp.setSituacao(SituacaoSubprocesso.NAO_INICIADO);
        sp.setDataLimiteEtapa1(LocalDateTime.now().plusDays(5));

        ProcessoDetalheDto.UnidadeParticipanteDto unidadeParticipanteDTO = ProcessoDetalheDto.UnidadeParticipanteDto.builder()
                .codUnidade(10L)
                .nome(DIRETORIA_X)
                .sigla("DX")
                .situacaoSubprocesso(SituacaoSubprocesso.NAO_INICIADO)
                .dataLimite(LocalDateTime.now().plusDays(7))
                .build();

        ProcessoResumoDto processoResumoDTO = ProcessoResumoDto.builder()
                .codigo(100L)
                .situacao(SituacaoProcesso.CRIADO)
                .dataLimite(LocalDateTime.now().plusDays(5))
                .unidadeCodigo(10L)
                .unidadeNome(DIRETORIA_X)
                .build();

        ProcessoDetalheDto processoDetalheDTO = ProcessoDetalheDto.builder()
                .codigo(1L)
                .descricao("Proc Teste")
                .tipo("MAPEAMENTO")
                .situacao(SituacaoProcesso.EM_ANDAMENTO)
                .dataLimite(dataLimite)
                .dataCriacao(dataCriacao)
                .unidades(List.of(unidadeParticipanteDTO))
                .resumoSubprocessos(List.of(processoResumoDTO))
                .build();

        when(processoRepo.findById(1L)).thenReturn(Optional.of(p));
        when(unidadeProcessoRepo.findByCodProcesso(1L)).thenReturn(List.of(up));
        when(subprocessoRepo.findByProcessoCodigoWithUnidade(1L)).thenReturn(List.of(sp));
        when(subprocessoRepo.existsByProcessoCodigoAndUnidadeCodigo(1L, 10L)).thenReturn(true);
        Mockito.lenient().when(processoDetalheMapperCustom.toDetailDTO(eq(p), anyList(), anyList()))
                .thenReturn(processoDetalheDTO);

    }
}
