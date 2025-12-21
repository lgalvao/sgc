package sgc.processo.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import sgc.processo.dto.ProcessoDetalheDto;
import sgc.processo.model.Processo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoRepo;
import sgc.unidade.model.Unidade;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProcessoDetalheBuilder")
class ProcessoDetalheBuilderTest {

    @Mock
    private SubprocessoRepo subprocessoRepo;

    @InjectMocks
    private ProcessoDetalheBuilder builder;

    @Test
    @DisplayName("Deve construir DTO com dados básicos e unidades quando dados válidos")
    void deveConstruirDtoQuandoDadosValidos() {
        // Arrange
        Processo processo = new Processo();
        processo.setCodigo(1L);
        processo.setDescricao("Processo Teste");
        processo.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        processo.setTipo(TipoProcesso.MAPEAMENTO);
        processo.setDataCriacao(LocalDateTime.now());
        processo.setDataLimite(LocalDateTime.now().plusDays(10));

        Unidade u1 = new Unidade();
        u1.setCodigo(10L);
        u1.setSigla("U1");
        u1.setNome("Unidade 1");

        processo.setParticipantes(Set.of(u1));

        Subprocesso sp = new Subprocesso();
        sp.setCodigo(100L);
        sp.setUnidade(u1);
        sp.setSituacao(sgc.subprocesso.model.SituacaoSubprocesso.NAO_INICIADO);
        sp.setDataLimiteEtapa1(processo.getDataLimite());

        when(subprocessoRepo.findByProcessoCodigoWithUnidade(1L)).thenReturn(List.of(sp));

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(null);
        SecurityContextHolder.setContext(securityContext);

        // Act
        ProcessoDetalheDto dto = builder.build(processo);

        // Assert
        assertThat(dto).isNotNull();
        assertThat(dto.getCodigo()).isEqualTo(1L);
        assertThat(dto.getDescricao()).isEqualTo("Processo Teste");
        assertThat(dto.getUnidades()).hasSize(1);

        ProcessoDetalheDto.UnidadeParticipanteDto uDto = dto.getUnidades().getFirst();
        assertThat(uDto.getCodUnidade()).isEqualTo(10L);
        assertThat(uDto.getSigla()).isEqualTo("U1");
        assertThat(uDto.getCodSubprocesso()).isEqualTo(100L);
    }

    @Test
    @DisplayName("Deve permitir finalizar quando usuário é admin")
    void devePermitirFinalizarQuandoUsuarioAdmin() {
        // Arrange
        Processo processo = new Processo();
        processo.setCodigo(1L);
        processo.setTipo(TipoProcesso.MAPEAMENTO);
        processo.setSituacao(SituacaoProcesso.CRIADO);
        processo.setParticipantes(Set.of());
        when(subprocessoRepo.findByProcessoCodigoWithUnidade(1L)).thenReturn(Collections.emptyList());

        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        org.mockito.Mockito.doReturn(List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))).when(auth).getAuthorities();
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);

        // Act
        ProcessoDetalheDto dto = builder.build(processo);

        // Assert
        assertThat(dto.isPodeFinalizar()).isTrue();
    }
}
