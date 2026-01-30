package sgc.processo.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import sgc.organizacao.model.Unidade;
import sgc.processo.dto.ProcessoDetalheDto;
import sgc.processo.mapper.ProcessoDetalheMapper;
import sgc.processo.model.Processo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoRepo;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("ProcessoDetalheBuilder")
class ProcessoDetalheBuilderTest {

    @Mock
    private SubprocessoRepo subprocessoRepo;

    @Mock
    private ProcessoDetalheMapper processoDetalheMapper;

    @Mock
    private sgc.organizacao.model.UsuarioPerfilRepo usuarioPerfilRepo;

    @InjectMocks
    private ProcessoDetalheBuilder builder;

    /**
     * Configura o mock do mapper para retornar DTOs corretamente mapeados.
     * Chamado apenas nos testes que usam participantes.
     */
    private void configurarMockDoMapper() {
        when(processoDetalheMapper.toUnidadeParticipanteDto(any(Unidade.class))).thenAnswer(invocation -> {
            Unidade u = invocation.getArgument(0);
            ProcessoDetalheDto.UnidadeParticipanteDto dto = new ProcessoDetalheDto.UnidadeParticipanteDto();
            dto.setCodUnidade(u.getCodigo());
            dto.setNome(u.getNome());
            dto.setSigla(u.getSigla());
            dto.setCodUnidadeSuperior(u.getUnidadeSuperior() != null ? u.getUnidadeSuperior().getCodigo() : null);
            return dto;
        });
    }

    @Test
    @DisplayName("Deve construir DTO com dados básicos e unidades quando dados válidos")
    void deveConstruirDtoQuandoDadosValidos() {
        // Arrange
        configurarMockDoMapper();
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
        sp.setMapa(new sgc.mapa.model.Mapa());

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

    @Test
    @DisplayName("Não deve permitir finalizar quando usuário não é admin")
    void naoDevePermitirFinalizarQuandoUsuarioNaoAdmin() {
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
        org.mockito.Mockito.doReturn(List.of(new SimpleGrantedAuthority("ROLE_USER"))).when(auth).getAuthorities();
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);

        // Act
        ProcessoDetalheDto dto = builder.build(processo);

        // Assert
        assertThat(dto.isPodeFinalizar()).isFalse();
    }

    @Test
    @DisplayName("Deve verificar ordenação das unidades")
    void deveVerificarOrdenacaoDasUnidades() {
        // Arrange
        configurarMockDoMapper();
        Processo processo = new Processo();
        processo.setCodigo(1L);
        processo.setTipo(TipoProcesso.MAPEAMENTO);
        processo.setSituacao(SituacaoProcesso.CRIADO);

        Unidade u1 = new Unidade();
        u1.setCodigo(1L);
        u1.setSigla("B");
        u1.setNome("Unidade B");
        Unidade u2 = new Unidade();
        u2.setCodigo(2L);
        u2.setSigla("A");
        u2.setNome("Unidade A");

        processo.setParticipantes(Set.of(u1, u2));
        when(subprocessoRepo.findByProcessoCodigoWithUnidade(1L)).thenReturn(Collections.emptyList());

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(null);
        SecurityContextHolder.setContext(securityContext);

        // Act
        ProcessoDetalheDto dto = builder.build(processo);

        // Assert
        assertThat(dto.getUnidades()).hasSize(2);
        assertThat(dto.getUnidades().get(0).getSigla()).isEqualTo("A");
        assertThat(dto.getUnidades().get(1).getSigla()).isEqualTo("B");
    }

    @Test
    @DisplayName("Deve construir DTO com hierarquia de participantes")
    void deveConstruirDtoComHierarquiaParticipantes() {
        // Arrange
        configurarMockDoMapper();
        Processo processo = new Processo();
        processo.setCodigo(1L);
        processo.setTipo(TipoProcesso.MAPEAMENTO);
        processo.setSituacao(SituacaoProcesso.EM_ANDAMENTO);

        Unidade pai = new Unidade();
        pai.setCodigo(1L);
        pai.setSigla("PAI");

        Unidade filho = new Unidade();
        filho.setCodigo(2L);
        filho.setSigla("FILHO");
        filho.setUnidadeSuperior(pai);

        processo.setParticipantes(Set.of(pai, filho));

        Subprocesso spPai = new Subprocesso();
        spPai.setUnidade(pai);
        spPai.setSituacao(sgc.subprocesso.model.SituacaoSubprocesso.NAO_INICIADO);
        spPai.setMapa(new sgc.mapa.model.Mapa());
        spPai.getMapa().setCodigo(100L);

        Subprocesso spFilho = new Subprocesso();
        spFilho.setUnidade(filho);
        spFilho.setSituacao(sgc.subprocesso.model.SituacaoSubprocesso.NAO_INICIADO);
        spFilho.setMapa(new sgc.mapa.model.Mapa());
        spFilho.getMapa().setCodigo(101L);

        when(subprocessoRepo.findByProcessoCodigoWithUnidade(1L)).thenReturn(List.of(spPai, spFilho));

        SecurityContextHolder.setContext(mock(SecurityContext.class));

        // Act
        ProcessoDetalheDto dto = builder.build(processo);

        // Assert
        assertThat(dto.getUnidades()).hasSize(1); // Somente o pai na raiz
        ProcessoDetalheDto.UnidadeParticipanteDto paiDto = dto.getUnidades().getFirst();
        assertThat(paiDto.getMapaCodigo()).isEqualTo(100L);
        assertThat(paiDto.getFilhos()).hasSize(1);
        assertThat(paiDto.getFilhos().getFirst().getSigla()).isEqualTo("FILHO");
    }

    @Test
    @DisplayName("Deve retornar false para chefe/coordenador se principal não for Usuario")
    void deveRetornarFalseSePrincipalNaoForUsuario() {
        Processo processo = new Processo();
        processo.setCodigo(1L);
        processo.setTipo(TipoProcesso.MAPEAMENTO);
        processo.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        processo.setParticipantes(Set.of());
        when(subprocessoRepo.findByProcessoCodigoWithUnidade(any())).thenReturn(Collections.emptyList());

        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getPrincipal()).thenReturn("principalString"); // Not Usuario
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);

        ProcessoDetalheDto dto = builder.build(processo);
        assertThat(dto.isPodeHomologarCadastro()).isFalse();
    }

    @Test
    @DisplayName("Deve retornar false para chefe/coordenador se usuário não é participante")
    void deveRetornarFalseSeUsuarioNaoParticipante() {
        Processo processo = new Processo();
        processo.setCodigo(1L);
        processo.setTipo(TipoProcesso.MAPEAMENTO);
        processo.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        configurarMockDoMapper();
        Unidade u1 = new Unidade();
        u1.setCodigo(1L);
        processo.setParticipantes(Set.of(u1));
        when(subprocessoRepo.findByProcessoCodigoWithUnidade(any())).thenReturn(Collections.emptyList());

        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);

        sgc.organizacao.model.Usuario usuario = new sgc.organizacao.model.Usuario();
        usuario.setTituloEleitoral("12345678901");
        sgc.organizacao.model.UsuarioPerfil atribuicao = new sgc.organizacao.model.UsuarioPerfil();
        Unidade u2 = new Unidade();
        u2.setCodigo(2L); // Different unit
        atribuicao.setUnidade(u2);

        when(auth.getPrincipal()).thenReturn(usuario);
        when(securityContext.getAuthentication()).thenReturn(auth);
        when(usuarioPerfilRepo.findByUsuarioTitulo("12345678901")).thenReturn(List.of(atribuicao));
        SecurityContextHolder.setContext(securityContext);

        ProcessoDetalheDto dto = builder.build(processo);
        assertThat(dto.isPodeHomologarCadastro()).isFalse();
    }

    @Test
    @DisplayName("Deve lidar com unidade filha cujo pai não está nos participantes")
    void deveLidarComPaiNaoParticipanteNaHierarquia() {
        Processo processo = new Processo();
        processo.setCodigo(1L);
        processo.setTipo(TipoProcesso.MAPEAMENTO);
        processo.setSituacao(SituacaoProcesso.EM_ANDAMENTO);

        Unidade pai = new Unidade();
        pai.setCodigo(1L);
        pai.setSigla("PAI");
        Unidade filho = new Unidade();
        filho.setCodigo(2L);
        filho.setSigla("FILHO");
        filho.setUnidadeSuperior(pai);

        // Apenas filho participa
        configurarMockDoMapper();
        processo.setParticipantes(Set.of(filho));
        when(subprocessoRepo.findByProcessoCodigoWithUnidade(1L)).thenReturn(Collections.emptyList());

        SecurityContextHolder.setContext(mock(SecurityContext.class));

        ProcessoDetalheDto dto = builder.build(processo);

        // Filho deve aparecer na raiz pois pai não participa
        assertThat(dto.getUnidades()).hasSize(1);
        assertThat(dto.getUnidades().getFirst().getSigla()).isEqualTo("FILHO");
    }

    @Test
    @DisplayName("Deve lidar com subprocesso sem mapa")
    void deveLidarComSubprocessoSemMapa() {
        // Arrange
        configurarMockDoMapper();
        Processo processo = new Processo();
        processo.setCodigo(1L);
        processo.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        processo.setTipo(TipoProcesso.MAPEAMENTO);
        processo.setDataCriacao(LocalDateTime.now());
        processo.setDataLimite(LocalDateTime.now().plusDays(10));

        Unidade u1 = new Unidade();
        u1.setCodigo(10L);
        u1.setSigla("U1");

        processo.setParticipantes(Set.of(u1));

        Subprocesso sp = new Subprocesso();
        sp.setCodigo(100L);
        sp.setUnidade(u1);
        sp.setSituacao(sgc.subprocesso.model.SituacaoSubprocesso.NAO_INICIADO);
        sp.setDataLimiteEtapa1(processo.getDataLimite());
        sp.setMapa(null); // Mapa nulo

        when(subprocessoRepo.findByProcessoCodigoWithUnidade(1L)).thenReturn(List.of(sp));

        SecurityContextHolder.setContext(mock(SecurityContext.class));

        // Act
        ProcessoDetalheDto dto = builder.build(processo);

        // Assert
        assertThat(dto.getUnidades()).hasSize(1);
        assertThat(dto.getUnidades().getFirst().getMapaCodigo()).isNull();
    }

    @Test
    @DisplayName("Deve lidar com subprocesso sem unidade correspondente nos participantes")
    void deveLidarComSubprocessoSemUnidadeCorrespondente() {
        // Arrange
        Processo processo = new Processo();
        processo.setCodigo(1L);
        processo.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        processo.setTipo(TipoProcesso.MAPEAMENTO);

        processo.setParticipantes(Set.of()); // Sem participantes

        Subprocesso sp = new Subprocesso();
        sp.setCodigo(100L);
        Unidade u1 = new Unidade();
        u1.setCodigo(10L);
        sp.setUnidade(u1);
        sp.setSituacao(sgc.subprocesso.model.SituacaoSubprocesso.NAO_INICIADO);

        // Subprocesso existe mas unidade não está em participantes
        when(subprocessoRepo.findByProcessoCodigoWithUnidade(1L)).thenReturn(List.of(sp));

        SecurityContextHolder.setContext(mock(SecurityContext.class));

        // Act
        ProcessoDetalheDto dto = builder.build(processo);

        // Assert
        assertThat(dto.getUnidades()).isEmpty();
    }

    @Test
    @DisplayName("Deve retornar false se não autenticado")
    void deveRetornarFalseSeNaoAutenticado() {
        Processo processo = new Processo();
        processo.setCodigo(1L);
        processo.setTipo(TipoProcesso.MAPEAMENTO);
        processo.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        processo.setParticipantes(Set.of());
        when(subprocessoRepo.findByProcessoCodigoWithUnidade(any())).thenReturn(Collections.emptyList());

        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(false);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);

        ProcessoDetalheDto dto = builder.build(processo);

        assertThat(dto.isPodeFinalizar()).isFalse();
        assertThat(dto.isPodeHomologarCadastro()).isFalse();
        assertThat(dto.isPodeHomologarMapa()).isFalse();
    }
}
