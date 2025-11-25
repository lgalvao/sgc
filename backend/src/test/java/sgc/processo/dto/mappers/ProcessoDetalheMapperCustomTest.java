package sgc.processo.dto.mappers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import sgc.processo.dto.ProcessoDetalheDto;
import sgc.processo.dto.ProcessoResumoDto;
import sgc.processo.model.Processo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoRepo;
import sgc.unidade.model.Unidade;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProcessoDetalheMapperCustomTest {

    @Mock
    private ProcessoDetalheMapper delegate;

    @Mock
    private SubprocessoRepo subprocessoRepo;

    @InjectMocks
    private ProcessoDetalheMapperCustomImpl mapper;

    // Concrete implementation for testing
    static class ProcessoDetalheMapperCustomImpl extends ProcessoDetalheMapperCustom {
        @Override
        public ProcessoDetalheDto.UnidadeParticipanteDto unidadeToUnidadeParticipanteDTO(Unidade unidade) {
            return null;
        }

        @Override
        public ProcessoResumoDto subprocessoToProcessoResumoDto(Subprocesso subprocesso) {
            return null;
        }

        @Override
        public ProcessoDetalheDto.UnidadeParticipanteDto subprocessoToUnidadeParticipanteDTO(Subprocesso subprocesso) {
            return null;
        }
    }

    @BeforeEach
    void setup() {
        SecurityContext context = mock(SecurityContext.class);
        SecurityContextHolder.setContext(context);
    }

    @Test
    @DisplayName("toDetailDTO deve mapear campos basicos")
    void toDetailDTO() {
        Processo p = new Processo();
        p.setCodigo(1L);
        p.setDescricao("Desc");
        p.setTipo(TipoProcesso.MAPEAMENTO);
        p.setSituacao(SituacaoProcesso.CRIADO);
        p.setDataCriacao(LocalDateTime.now());
        p.setParticipantes(new HashSet<>());

        // Mock security
        Authentication auth = mock(Authentication.class);
        when(SecurityContextHolder.getContext().getAuthentication()).thenReturn(auth);
        when(auth.isAuthenticated()).thenReturn(false);

        when(subprocessoRepo.findByProcessoCodigoWithUnidade(anyLong())).thenReturn(Collections.emptyList());

        ProcessoDetalheDto dto = mapper.toDetailDTO(p);

        assertThat(dto.getCodigo()).isEqualTo(1L);
        assertThat(dto.getDescricao()).isEqualTo("Desc");
    }

    @Test
    @DisplayName("montarHierarquiaUnidades deve montar arvore")
    void montarHierarquiaUnidades() {
        ProcessoDetalheDto dto = ProcessoDetalheDto.builder().build();
        Processo p = new Processo();
        Unidade pai = new Unidade();
        pai.setCodigo(1L);
        pai.setSigla("PAI");
        Unidade filho = new Unidade();
        filho.setCodigo(2L);
        filho.setSigla("FILHO");
        filho.setUnidadeSuperior(pai);

        p.setParticipantes(new HashSet<>(List.of(pai, filho)));

        ProcessoDetalheDto.UnidadeParticipanteDto dtoPai = ProcessoDetalheDto.UnidadeParticipanteDto.builder()
                .codUnidade(1L)
                .sigla("PAI")
                .build();

        ProcessoDetalheDto.UnidadeParticipanteDto dtoFilho = ProcessoDetalheDto.UnidadeParticipanteDto.builder()
                .codUnidade(2L)
                .codUnidadeSuperior(1L)
                .sigla("FILHO")
                .build();

        when(delegate.unidadeToUnidadeParticipanteDTO(pai)).thenReturn(dtoPai);
        when(delegate.unidadeToUnidadeParticipanteDTO(filho)).thenReturn(dtoFilho);

        mapper.montarHierarquiaUnidades(dto, p, List.of());

        assertThat(dto.getUnidades()).hasSize(1);
        assertThat(dto.getUnidades().get(0).getCodUnidade()).isEqualTo(1L);
        assertThat(dto.getUnidades().get(0).getFilhos()).hasSize(1);
    }
}
