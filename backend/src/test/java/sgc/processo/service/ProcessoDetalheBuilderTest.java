package sgc.processo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import sgc.processo.api.ProcessoDetalheDto;
import sgc.processo.internal.model.Processo;
import sgc.processo.internal.model.SituacaoProcesso;
import sgc.processo.internal.model.TipoProcesso;
import sgc.processo.internal.service.ProcessoDetalheBuilder;
import sgc.sgrh.internal.model.Perfil;
import sgc.sgrh.internal.model.Usuario;
import sgc.sgrh.internal.model.UsuarioPerfil;
import sgc.mapa.internal.model.Mapa;
import sgc.subprocesso.internal.model.SituacaoSubprocesso;
import sgc.subprocesso.internal.model.Subprocesso;
import sgc.subprocesso.internal.model.SubprocessoRepo;
import sgc.unidade.internal.model.Unidade;

@ExtendWith(MockitoExtension.class)
class ProcessoDetalheBuilderTest {

  @Mock private SubprocessoRepo subprocessoRepo;

  @InjectMocks private ProcessoDetalheBuilder builder;

  @Mock private Authentication authentication;

  @Mock private SecurityContext securityContext;

  @BeforeEach
  void setUp() {
    SecurityContextHolder.setContext(securityContext);
  }

  @Nested
  @DisplayName("Permissões")
  class PermissoesTest {

    @Test
    @DisplayName("Deve retornar true para admin")
    void deveRetornarTrueParaAdmin() {
      // Arrange
      Processo processo = mock(Processo.class);
      when(processo.getCodigo()).thenReturn(1L);
      when(processo.getDescricao()).thenReturn("Descricao");
      when(processo.getSituacao()).thenReturn(SituacaoProcesso.CRIADO);
      when(processo.getTipo()).thenReturn(TipoProcesso.MAPEAMENTO);
      when(processo.getParticipantes()).thenReturn(Set.of()); // Evita NPE

      when(securityContext.getAuthentication()).thenReturn(authentication);
      when(authentication.isAuthenticated()).thenReturn(true);
      // Simula autoridades usando wildcard para evitar problemas de tipos genéricos
      List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
      when((List) authentication.getAuthorities()).thenReturn(authorities);

      // Act
      ProcessoDetalheDto dto = builder.build(processo);

      // Assert
      assertTrue(dto.isPodeFinalizar());
    }

    @Test
    @DisplayName("Deve retornar false para não admin")
    void deveRetornarFalseParaNaoAdmin() {
      // Arrange
      Processo processo = mock(Processo.class);
      when(processo.getCodigo()).thenReturn(1L);
      when(processo.getDescricao()).thenReturn("Descricao");
      when(processo.getSituacao()).thenReturn(SituacaoProcesso.CRIADO);
      when(processo.getTipo()).thenReturn(TipoProcesso.MAPEAMENTO);
      when(processo.getParticipantes()).thenReturn(Set.of());

      when(securityContext.getAuthentication()).thenReturn(authentication);
      when(authentication.isAuthenticated()).thenReturn(true);
      List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
      when((List) authentication.getAuthorities()).thenReturn(authorities);
      when(authentication.getPrincipal()).thenReturn(new Object());

      // Act
      ProcessoDetalheDto dto = builder.build(processo);

      // Assert
      assertFalse(dto.isPodeFinalizar());
    }

    @Test
    @DisplayName("Deve retornar false quando autenticação for nula")
    void deveRetornarFalseQuandoAutenticacaoNula() {
      // Arrange
      Processo processo = mock(Processo.class);
      when(processo.getCodigo()).thenReturn(1L);
      when(processo.getDescricao()).thenReturn("Descricao");
      when(processo.getSituacao()).thenReturn(SituacaoProcesso.CRIADO);
      when(processo.getTipo()).thenReturn(TipoProcesso.MAPEAMENTO);
      when(processo.getParticipantes()).thenReturn(Set.of());

      when(securityContext.getAuthentication()).thenReturn(null);

      // Act
      ProcessoDetalheDto dto = builder.build(processo);

      // Assert
      assertFalse(dto.isPodeFinalizar());
      assertFalse(dto.isPodeHomologarCadastro());
    }

    @Test
    @DisplayName("Deve retornar false quando não autenticado")
    void deveRetornarFalseQuandoNaoAutenticado() {
      // Arrange
      Processo processo = mock(Processo.class);
      when(processo.getCodigo()).thenReturn(1L);
      when(processo.getDescricao()).thenReturn("Descricao");
      when(processo.getSituacao()).thenReturn(SituacaoProcesso.CRIADO);
      when(processo.getTipo()).thenReturn(TipoProcesso.MAPEAMENTO);
      when(processo.getParticipantes()).thenReturn(Set.of());

      when(securityContext.getAuthentication()).thenReturn(authentication);
      when(authentication.isAuthenticated()).thenReturn(false);

      // Act
      ProcessoDetalheDto dto = builder.build(processo);

      // Assert
      assertFalse(dto.isPodeFinalizar());
      assertFalse(dto.isPodeHomologarCadastro());
    }

    @Test
    @DisplayName("Deve retornar false se autenticado mas principal não é Usuario")
    void deveRetornarFalseSePrincipalInvalido() {
        // Arrange
        Processo processo = mock(Processo.class);
        when(processo.getCodigo()).thenReturn(1L);
        when(processo.getDescricao()).thenReturn("Descricao");
        when(processo.getSituacao()).thenReturn(SituacaoProcesso.CRIADO);
        when(processo.getTipo()).thenReturn(TipoProcesso.MAPEAMENTO);
        when(processo.getParticipantes()).thenReturn(Set.of());

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn("Not a user");
        when(authentication.getAuthorities()).thenReturn(Collections.emptyList());

        // Act
        ProcessoDetalheDto dto = builder.build(processo);

        // Assert
        assertFalse(dto.isPodeFinalizar());
        assertFalse(dto.isPodeHomologarCadastro());
    }

    @Test
    @DisplayName("Deve retornar true se usuário é chefe/coordenador de unidade participante")
    void deveRetornarTrueSeUsuarioParticipante() {
      // Arrange
      Unidade unidade = new Unidade("Unidade 1", "U1");
      unidade.setCodigo(1L);

      Processo processo = mock(Processo.class);
      when(processo.getCodigo()).thenReturn(1L);
      when(processo.getDescricao()).thenReturn("Descricao");
      when(processo.getSituacao()).thenReturn(SituacaoProcesso.CRIADO);
      when(processo.getTipo()).thenReturn(TipoProcesso.MAPEAMENTO);
      when(processo.getParticipantes()).thenReturn(Set.of(unidade));

      Usuario usuario = mock(Usuario.class);
      UsuarioPerfil atribuicao = new UsuarioPerfil();
      atribuicao.setUnidade(unidade);
      atribuicao.setPerfil(Perfil.CHEFE);

      when(usuario.getTodasAtribuicoes()).thenReturn(Set.of(atribuicao));

      when(securityContext.getAuthentication()).thenReturn(authentication);
      when(authentication.isAuthenticated()).thenReturn(true);
      when(authentication.getAuthorities()).thenReturn(Collections.emptyList());
      when(authentication.getPrincipal()).thenReturn(usuario);

      // Act
      ProcessoDetalheDto dto = builder.build(processo);

      // Assert
      assertTrue(dto.isPodeHomologarCadastro());
    }

    @Test
    @DisplayName("Deve retornar false se usuário não é participante")
    void deveRetornarFalseSeUsuarioNaoParticipante() {
      // Arrange
      Unidade unidade = new Unidade("Unidade 1", "U1");
      unidade.setCodigo(1L);
      Unidade outraUnidade = new Unidade("Unidade 2", "U2");
      outraUnidade.setCodigo(2L);

      Processo processo = mock(Processo.class);
      when(processo.getCodigo()).thenReturn(1L);
      when(processo.getDescricao()).thenReturn("Descricao");
      when(processo.getSituacao()).thenReturn(SituacaoProcesso.CRIADO);
      when(processo.getTipo()).thenReturn(TipoProcesso.MAPEAMENTO);
      when(processo.getParticipantes()).thenReturn(Set.of(unidade));

      Usuario usuario = mock(Usuario.class);
      UsuarioPerfil atribuicao = new UsuarioPerfil();
      atribuicao.setUnidade(outraUnidade);
      atribuicao.setPerfil(Perfil.CHEFE);

      when(usuario.getTodasAtribuicoes()).thenReturn(Set.of(atribuicao));

      when(securityContext.getAuthentication()).thenReturn(authentication);
      when(authentication.isAuthenticated()).thenReturn(true);
      when(authentication.getAuthorities()).thenReturn(Collections.emptyList());
      when(authentication.getPrincipal()).thenReturn(usuario);

      // Act
      ProcessoDetalheDto dto = builder.build(processo);

      // Assert
      assertFalse(dto.isPodeHomologarCadastro());
    }

    @Test
    @DisplayName("Deve retornar false se principal não é usuário")
    void deveRetornarFalseSePrincipalNaoUsuario() {
        // Arrange
        Processo processo = mock(Processo.class);
        when(processo.getCodigo()).thenReturn(1L);
        when(processo.getDescricao()).thenReturn("Descricao");
        when(processo.getSituacao()).thenReturn(SituacaoProcesso.CRIADO);
        when(processo.getTipo()).thenReturn(TipoProcesso.MAPEAMENTO);
        when(processo.getParticipantes()).thenReturn(Set.of());

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getAuthorities()).thenReturn(Collections.emptyList());
        when(authentication.getPrincipal()).thenReturn("principal string"); // Não é Usuario

        // Act
        ProcessoDetalheDto dto = builder.build(processo);

        // Assert
        assertFalse(dto.isPodeHomologarCadastro());
    }
  }

  @Nested
  @DisplayName("Hierarquia")
  class HierarquiaTest {

    @Test
    @DisplayName("Deve montar hierarquia de unidades corretamente")
    void deveMontarHierarquia() {
      // Arrange
      Unidade pai = new Unidade("Pai", "PAI");
      pai.setCodigo(1L);

      Unidade filho1 = new Unidade("Filho 1", "F1");
      filho1.setCodigo(2L);
      filho1.setUnidadeSuperior(pai);

      Unidade filho2 = new Unidade("Filho 2", "F2");
      filho2.setCodigo(3L);
      filho2.setUnidadeSuperior(pai);

      Unidade neto = new Unidade("Neto", "N1");
      neto.setCodigo(4L);
      neto.setUnidadeSuperior(filho1);

      // Unidade participante sem pai no processo
      Unidade orfao = new Unidade("Orfao", "ORF");
      orfao.setCodigo(5L);

      Processo processo = mock(Processo.class);
      when(processo.getCodigo()).thenReturn(1L);
      when(processo.getDescricao()).thenReturn("Descricao");
      when(processo.getSituacao()).thenReturn(SituacaoProcesso.CRIADO);
      when(processo.getTipo()).thenReturn(TipoProcesso.MAPEAMENTO);
      // Incluindo pai e filhos como participantes
      Set<Unidade> participantes = new HashSet<>(Arrays.asList(pai, filho1, filho2, neto, orfao));
      when(processo.getParticipantes()).thenReturn(participantes);

      // Setup Subprocessos
      Subprocesso spPai = new Subprocesso(processo, pai, null, SituacaoSubprocesso.NAO_INICIADO, LocalDateTime.now());
      spPai.setCodigo(10L);

      Mapa mapa = mock(Mapa.class);
      when(mapa.getCodigo()).thenReturn(100L);

      Subprocesso spFilho1 = new Subprocesso(processo, filho1, mapa, SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO, LocalDateTime.now());
      spFilho1.setCodigo(11L);

      when(subprocessoRepo.findByProcessoCodigoWithUnidade(1L)).thenReturn(List.of(spPai, spFilho1));

      // Mocks de segurança padrão
      when(securityContext.getAuthentication()).thenReturn(authentication);
      when(authentication.isAuthenticated()).thenReturn(true);
      when(authentication.getAuthorities()).thenReturn(Collections.emptyList());
      when(authentication.getPrincipal()).thenReturn(new Object());

      // Act
      ProcessoDetalheDto dto = builder.build(processo);

      // Assert
      List<ProcessoDetalheDto.UnidadeParticipanteDto> raizes = dto.getUnidades();
      // Esperamos PAI e ORFAO como raízes
      assertEquals(2, raizes.size());

      // Verifica ordenação das raízes (ORF vem antes de PAI? Não, F vem antes de P? Não. O vs P. ORF vem antes de PAI)
      // "Orfao" (ORF) vs "Pai" (PAI). Sigla: ORF, PAI. O vem antes de P.
      assertEquals("ORF", raizes.get(0).getSigla());
      assertEquals("PAI", raizes.get(1).getSigla());

      // Verifica PAI
      ProcessoDetalheDto.UnidadeParticipanteDto dtoPai = raizes.get(1);
      assertEquals(2, dtoPai.getFilhos().size()); // F1 e F2
      assertEquals(SituacaoSubprocesso.NAO_INICIADO.getDescricao(), dtoPai.getSituacaoLabel());

      // Verifica F1
      ProcessoDetalheDto.UnidadeParticipanteDto dtoF1 = dtoPai.getFilhos().get(0); // F1 vs F2. F1 vem antes de F2? F1 vs F2
      assertEquals("F1", dtoF1.getSigla());
      assertEquals(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO.getDescricao(), dtoF1.getSituacaoLabel());
      assertEquals(100L, dtoF1.getMapaCodigo());

      // Verifica Neto dentro de F1
      assertEquals(1, dtoF1.getFilhos().size());
      assertEquals("N1", dtoF1.getFilhos().get(0).getSigla());
    }

    @Test
    @DisplayName("Deve ignorar filhos cujo pai não participa do processo na montagem da árvore se tornar raiz")
    void deveTratarFilhoSemPaiParticipanteComoRaiz() {
        // Se o pai não participa, o filho vira raiz na visualização?
        // O código diz: if (codUnidadeSuperior == null || !mapaUnidades.containsKey(codUnidadeSuperior)) { dto.getUnidades().add(unidadeDto); }
        // Sim.

        // Arrange
        Unidade paiNaoParticipante = new Unidade("Pai", "PAI");
        paiNaoParticipante.setCodigo(1L);

        Unidade filho = new Unidade("Filho", "F1");
        filho.setCodigo(2L);
        filho.setUnidadeSuperior(paiNaoParticipante);

        Processo processo = mock(Processo.class);
        when(processo.getCodigo()).thenReturn(1L);
        when(processo.getDescricao()).thenReturn("Descricao");
        when(processo.getSituacao()).thenReturn(SituacaoProcesso.CRIADO);
        when(processo.getTipo()).thenReturn(TipoProcesso.MAPEAMENTO);
        when(processo.getParticipantes()).thenReturn(Set.of(filho));

        when(subprocessoRepo.findByProcessoCodigoWithUnidade(1L)).thenReturn(Collections.emptyList());

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getAuthorities()).thenReturn(Collections.emptyList());
        when(authentication.getPrincipal()).thenReturn(new Object());

        // Act
        ProcessoDetalheDto dto = builder.build(processo);

        // Assert
        assertEquals(1, dto.getUnidades().size());
        assertEquals("F1", dto.getUnidades().get(0).getSigla());
    }
  }
}
