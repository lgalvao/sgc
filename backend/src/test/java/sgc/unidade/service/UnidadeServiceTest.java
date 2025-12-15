package sgc.unidade.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.mapa.model.Mapa;
import sgc.mapa.model.MapaRepo;
import sgc.processo.model.ProcessoRepo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;
import sgc.sgrh.dto.ServidorDto;
import sgc.sgrh.dto.UnidadeDto;
import sgc.sgrh.mapper.SgrhMapper;
import sgc.sgrh.model.Usuario;
import sgc.sgrh.model.UsuarioRepo;
import sgc.unidade.dto.CriarAtribuicaoTemporariaRequest;
import sgc.unidade.model.*;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UnidadeServiceTest {

    @InjectMocks
    private UnidadeService unidadeService;

    @Mock
    private UnidadeRepo unidadeRepo;

    @Mock
    private sgc.unidade.model.UnidadeMapaRepo unidadeMapaRepo;

    @Mock
    private MapaRepo mapaRepo;

    @Mock
    private UsuarioRepo usuarioRepo;

    @Mock
    private AtribuicaoTemporariaRepo atribuicaoTemporariaRepo;

    @Mock
    private ProcessoRepo processoRepo;

    @Mock
    private SgrhMapper sgrhMapper;

    @Test
    @DisplayName("buscarTodasUnidades deve retornar hierarquia correta")
    void buscarTodasUnidades() {
        Unidade raiz = new Unidade("Raiz", "RAIZ");
        raiz.setCodigo(1L);
        raiz.setTipo(TipoUnidade.OPERACIONAL);

        Unidade filha = new Unidade("Filha", "FILHA");
        filha.setCodigo(2L);
        filha.setTipo(TipoUnidade.OPERACIONAL);
        filha.setUnidadeSuperior(raiz);

        when(unidadeRepo.findAll()).thenReturn(Arrays.asList(raiz, filha));

        when(sgrhMapper.toUnidadeDto(raiz)).thenReturn(UnidadeDto.builder()
                .codigo(1L).nome("Raiz").sigla("RAIZ").subunidades(new ArrayList<>()).build());
        when(sgrhMapper.toUnidadeDto(filha)).thenReturn(UnidadeDto.builder()
                .codigo(2L).nome("Filha").sigla("FILHA").codigoPai(1L).subunidades(new ArrayList<>()).build());

        List<UnidadeDto> resultado = unidadeService.buscarTodasUnidades();

        assertThat(resultado).hasSize(1);
        UnidadeDto dtoRaiz = resultado.get(0);
        assertThat(dtoRaiz.getSigla()).isEqualTo("RAIZ");
        assertThat(dtoRaiz.getSubunidades()).hasSize(1);
        assertThat(dtoRaiz.getSubunidades().get(0).getSigla()).isEqualTo("FILHA");
    }

    @Test
    @DisplayName("criarAtribuicaoTemporaria deve salvar atribuicao com sucesso")
    void criarAtribuicaoTemporaria() {
        Long unidadeId = 1L;
        String usuarioId = "123456789012";
        CriarAtribuicaoTemporariaRequest req =
                new CriarAtribuicaoTemporariaRequest(
                        usuarioId, LocalDate.now().plusDays(5), "Justificativa");

        Unidade unidade = new Unidade();
        unidade.setCodigo(unidadeId);

        Usuario usuario = new Usuario();
        usuario.setTituloEleitoral(usuarioId);

        when(unidadeRepo.findById(unidadeId)).thenReturn(Optional.of(unidade));
        when(usuarioRepo.findById(usuarioId)).thenReturn(Optional.of(usuario));

        unidadeService.criarAtribuicaoTemporaria(unidadeId, req);

        verify(atribuicaoTemporariaRepo).save(any(AtribuicaoTemporaria.class));
    }

    @Test
    @DisplayName("verificarMapaVigente deve retornar verdadeiro se mapa existe")
    void verificarMapaVigente() {
        Long unidadeId = 1L;
        when(mapaRepo.findMapaVigenteByUnidade(unidadeId)).thenReturn(Optional.of(new Mapa()));

        boolean existe = unidadeService.verificarMapaVigente(unidadeId);

        assertThat(existe).isTrue();
    }

    @Test
    @DisplayName("buscarServidoresPorUnidade deve retornar lista de servidores")
    void buscarServidoresPorUnidade() {
        Long unidadeId = 1L;
        Unidade unidade = new Unidade();
        unidade.setCodigo(unidadeId);

        Usuario usuario = new Usuario();
        usuario.setTituloEleitoral("123");
        usuario.setNome("Teste");
        usuario.setEmail("teste@email.com");
        usuario.setUnidadeLotacao(unidade);

        when(usuarioRepo.findByUnidadeLotacaoCodigo(unidadeId)).thenReturn(List.of(usuario));
        when(sgrhMapper.toServidorDto(usuario)).thenReturn(ServidorDto.builder().nome("Teste").build());

        var resultado = unidadeService.buscarServidoresPorUnidade(unidadeId);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getNome()).isEqualTo("Teste");
    }

    @Test
    @DisplayName("buscarPorSigla deve retornar unidade se existir")
    void buscarPorSigla() {
        String sigla = "TESTE";
        Unidade unidade = new Unidade("Nome", sigla);
        unidade.setCodigo(1L);
        unidade.setTipo(TipoUnidade.OPERACIONAL);

        when(unidadeRepo.findBySigla(sigla)).thenReturn(Optional.of(unidade));
        when(sgrhMapper.toUnidadeDto(unidade, false)).thenReturn(UnidadeDto.builder().sigla(sigla).build());

        UnidadeDto dto = unidadeService.buscarPorSigla(sigla);

        assertThat(dto.getSigla()).isEqualTo(sigla);
    }

    @Test
    @DisplayName("buscarPorCodigo deve retornar unidade se existir")
    void buscarPorCodigo() {
        Long id = 1L;
        Unidade unidade = new Unidade("Nome", "SIGLA");
        unidade.setCodigo(id);
        unidade.setTipo(TipoUnidade.OPERACIONAL);

        when(unidadeRepo.findById(id)).thenReturn(Optional.of(unidade));
        when(sgrhMapper.toUnidadeDto(unidade, false)).thenReturn(UnidadeDto.builder().codigo(id).build());

        UnidadeDto dto = unidadeService.buscarPorCodigo(id);

        assertThat(dto.getCodigo()).isEqualTo(id);
    }

    @Test
    @DisplayName("buscarPorCodigo deve lançar exceção se não encontrar")
    void buscarPorCodigoException() {
        Long id = 99L;
        when(unidadeRepo.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> unidadeService.buscarPorCodigo(id))
                .isInstanceOf(ErroEntidadeNaoEncontrada.class);
    }

    @Test
    @DisplayName("buscarArvoreComElegibilidade deve usar findAllComMapas quando requerMapaVigente")
    void buscarArvoreComElegibilidadeRevisao() {
        Unidade u1 = new Unidade("U1", "SIGLA1");
        u1.setCodigo(1L);

        when(unidadeRepo.findAllComMapas()).thenReturn(List.of(u1));
        when(processoRepo.findUnidadeCodigosBySituacaoInAndProcessoCodigoNot(
                Arrays.asList(SituacaoProcesso.EM_ANDAMENTO, SituacaoProcesso.CRIADO),
                1L))
                .thenReturn(Collections.emptyList());
        when(unidadeMapaRepo.findAllUnidadeCodigos()).thenReturn(List.of());
        when(sgrhMapper.toUnidadeDto(any(), any(Boolean.class))).thenReturn(UnidadeDto.builder().codigo(1L).build());

        unidadeService.buscarArvoreComElegibilidade(TipoProcesso.REVISAO, 1L);

        verify(unidadeRepo).findAllComMapas();
        verify(unidadeMapaRepo).findAllUnidadeCodigos();
    }

    @Test
    @DisplayName("buscarArvoreComElegibilidade deve usar findAll quando nao requerMapaVigente")
    void buscarArvoreComElegibilidadeMapeamento() {
        Unidade u1 = new Unidade("U1", "SIGLA1");
        u1.setCodigo(1L);

        when(unidadeRepo.findAll()).thenReturn(List.of(u1));
        when(processoRepo.findUnidadeCodigosBySituacaoInAndProcessoCodigoNot(
                Arrays.asList(SituacaoProcesso.EM_ANDAMENTO, SituacaoProcesso.CRIADO),
                1L))
                .thenReturn(Collections.emptyList());
        when(sgrhMapper.toUnidadeDto(any(), any(Boolean.class))).thenReturn(UnidadeDto.builder().codigo(1L).build());

        unidadeService.buscarArvoreComElegibilidade(TipoProcesso.MAPEAMENTO, 1L);

        verify(unidadeRepo).findAll();
    }

    @Test
    @DisplayName("buscarArvoreComElegibilidade deve marcar unidade como nao elegivel se estiver em processo ativo")
    void buscarArvoreComElegibilidadeComProcessoAtivo() {
        Unidade unidade = new Unidade("Unidade Teste", "TESTE");
        unidade.setCodigo(10L);
        unidade.setTipo(TipoUnidade.OPERACIONAL);

        when(unidadeRepo.findAll()).thenReturn(List.of(unidade));

        when(processoRepo.findUnidadeCodigosBySituacaoInAndProcessoCodigoNot(
                Arrays.asList(SituacaoProcesso.EM_ANDAMENTO, SituacaoProcesso.CRIADO),
                null))
                .thenReturn(List.of(10L));

        when(sgrhMapper.toUnidadeDto(unidade, false)).thenReturn(UnidadeDto.builder().codigo(10L).isElegivel(false).build());

        List<UnidadeDto> resultado = unidadeService.buscarArvoreComElegibilidade(TipoProcesso.MAPEAMENTO, null);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).isElegivel()).isFalse();
    }

    @Test
    @DisplayName("buscarArvoreComElegibilidade deve marcar unidade como elegivel se estiver no mesmo processo (edição)")
    void buscarArvoreComElegibilidadeEdicaoMesmoProcesso() {
        Unidade unidade = new Unidade("Unidade Teste", "TESTE");
        unidade.setCodigo(10L);
        unidade.setTipo(TipoUnidade.OPERACIONAL);

        when(unidadeRepo.findAll()).thenReturn(List.of(unidade));

        // O novo método do repo deve retornar uma lista vazia, pois o único processo ativo
        // é o próprio processo que estamos editando (que deve ser ignorado)
        when(processoRepo.findUnidadeCodigosBySituacaoInAndProcessoCodigoNot(
                Arrays.asList(SituacaoProcesso.EM_ANDAMENTO, SituacaoProcesso.CRIADO),
                100L))
                .thenReturn(Collections.emptyList());

        when(sgrhMapper.toUnidadeDto(unidade, true)).thenReturn(UnidadeDto.builder().codigo(10L).isElegivel(true).build());

        // Passamos o codigo do processo atual para ignorar
        List<UnidadeDto> resultado = unidadeService.buscarArvoreComElegibilidade(TipoProcesso.MAPEAMENTO, 100L);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).isElegivel()).isTrue();
    }

    @Test
    @DisplayName("buscarArvoreComElegibilidade deve marcar unidade como elegivel se nao estiver em processo ativo")
    void buscarArvoreComElegibilidadeSemProcessoAtivo() {
        Unidade unidade = new Unidade("Unidade Teste", "TESTE");
        unidade.setCodigo(10L);
        unidade.setTipo(TipoUnidade.OPERACIONAL);

        when(unidadeRepo.findAll()).thenReturn(List.of(unidade));

        when(processoRepo.findUnidadeCodigosBySituacaoInAndProcessoCodigoNot(
                Arrays.asList(SituacaoProcesso.EM_ANDAMENTO, SituacaoProcesso.CRIADO),
                null))
                .thenReturn(Collections.emptyList());

        when(sgrhMapper.toUnidadeDto(unidade, true)).thenReturn(UnidadeDto.builder().codigo(10L).isElegivel(true).build());

        List<UnidadeDto> resultado = unidadeService.buscarArvoreComElegibilidade(TipoProcesso.MAPEAMENTO, null);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).isElegivel()).isTrue();
    }

    @Test
    @DisplayName("buscarArvoreComElegibilidade deve marcar ASSESSORIA_11 como elegivel em hierarquia completa")
    void buscarArvoreComElegibilidadeAssessoria11() {
        // Setup: criar hierarquia SEDOC -> SECRETARIA_1 -> ASSESSORIA_11
        Unidade sedoc = new Unidade("SEDOC", "SEDOC");
        sedoc.setCodigo(1L);
        sedoc.setTipo(TipoUnidade.INTEROPERACIONAL);

        Unidade secretaria = new Unidade("Secretaria 1", "SECRETARIA_1");
        secretaria.setCodigo(2L);
        secretaria.setTipo(TipoUnidade.INTEROPERACIONAL);
        secretaria.setUnidadeSuperior(sedoc);

        Unidade assessoria = new Unidade("Assessoria 11", "ASSESSORIA_11");
        assessoria.setCodigo(3L);
        assessoria.setTipo(TipoUnidade.OPERACIONAL);
        assessoria.setUnidadeSuperior(secretaria);

        when(unidadeRepo.findAll()).thenReturn(List.of(sedoc, secretaria, assessoria));
        when(processoRepo.findUnidadeCodigosBySituacaoInAndProcessoCodigoNot(
                Arrays.asList(SituacaoProcesso.EM_ANDAMENTO, SituacaoProcesso.CRIADO),
                null))
                .thenReturn(Collections.emptyList());

        // Mock sgrhMapper
        when(sgrhMapper.toUnidadeDto(eq(sedoc), any(Boolean.class)))
                .thenReturn(UnidadeDto.builder().codigo(1L).sigla("SEDOC").subunidades(new ArrayList<>()).build());
        when(sgrhMapper.toUnidadeDto(eq(secretaria), any(Boolean.class)))
                .thenReturn(UnidadeDto.builder().codigo(2L).codigoPai(1L).sigla("SECRETARIA_1").subunidades(new ArrayList<>()).build());
        when(sgrhMapper.toUnidadeDto(eq(assessoria), any(Boolean.class)))
                .thenReturn(UnidadeDto.builder().codigo(3L).codigoPai(2L).sigla("ASSESSORIA_11").isElegivel(true).subunidades(new ArrayList<>()).build());

        // Act
        List<UnidadeDto> resultado = unidadeService.buscarArvoreComElegibilidade(
                TipoProcesso.MAPEAMENTO, null);

        // Assert: encontrar ASSESSORIA_11 na hierarquia e verificar que é elegível
        assertThat(resultado).hasSize(1);
        UnidadeDto raiz = resultado.get(0); // SEDOC
        assertThat(raiz.getSigla()).isEqualTo("SEDOC");
        assertThat(raiz.getSubunidades()).hasSize(1);

        UnidadeDto secretaria1 = raiz.getSubunidades().get(0); // SECRETARIA_1
        assertThat(secretaria1.getSigla()).isEqualTo("SECRETARIA_1");
        assertThat(secretaria1.getSubunidades()).hasSize(1);

        UnidadeDto assessoria11 = secretaria1.getSubunidades().get(0);
        assertThat(assessoria11.getSigla()).isEqualTo("ASSESSORIA_11");
        assertThat(assessoria11.isElegivel()).isTrue();
    }

    @Test
    @DisplayName("UnidadeDto deve serializar isElegivel corretamente para JSON")
    void testeSerializacaoJson() throws Exception {
        UnidadeDto dto = new UnidadeDto();
        dto.setElegivel(true);
        dto.setCodigo(1L);
        dto.setNome("Teste");

        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        String json = mapper.writeValueAsString(dto);

        assertThat(json).contains("\"isElegivel\":true");
    }

    @Test
    @DisplayName("buscarArvoreComElegibilidade deve retornar ASSESSORIA_11 habilitada com dados do seed")
    void buscarArvoreComElegibilidade_DeveRetornarAssessoria11Habilitada() {
        // Setup: Simular dados do seed.sql
        // 1. SEDOC (INTEROPERACIONAL)
        Unidade sedoc = new Unidade("Seção de Desenvolvimento e Capacitação", "SEDOC");
        sedoc.setCodigo(1L);
        sedoc.setTipo(TipoUnidade.INTEROPERACIONAL);
        sedoc.setSituacao(sgc.unidade.model.SituacaoUnidade.ATIVA);

        // 2. SECRETARIA_1 (INTEROPERACIONAL, Pai: SEDOC)
        Unidade secretaria1 = new Unidade("Secretaria 1", "SECRETARIA_1");
        secretaria1.setCodigo(2L);
        secretaria1.setTipo(TipoUnidade.INTEROPERACIONAL);
        secretaria1.setSituacao(sgc.unidade.model.SituacaoUnidade.ATIVA);
        secretaria1.setUnidadeSuperior(sedoc);

        // 3. ASSESSORIA_11 (OPERACIONAL, Pai: SECRETARIA_1)
        Unidade assessoria11 = new Unidade("Assessoria 11", "ASSESSORIA_11");
        assessoria11.setCodigo(3L);
        assessoria11.setTipo(TipoUnidade.OPERACIONAL);
        assessoria11.setSituacao(sgc.unidade.model.SituacaoUnidade.ATIVA);
        assessoria11.setUnidadeSuperior(secretaria1);

        List<Unidade> todasUnidades = Arrays.asList(sedoc, secretaria1, assessoria11);

        when(unidadeRepo.findAll()).thenReturn(todasUnidades);
        when(processoRepo.findUnidadeCodigosBySituacaoInAndProcessoCodigoNot(
                Arrays.asList(SituacaoProcesso.EM_ANDAMENTO, SituacaoProcesso.CRIADO),
                null))
                .thenReturn(Collections.emptyList());

        // Mock mappers
        when(sgrhMapper.toUnidadeDto(eq(sedoc), any(Boolean.class)))
                .thenReturn(UnidadeDto.builder().codigo(1L).sigla("SEDOC").subunidades(new ArrayList<>()).build());
        when(sgrhMapper.toUnidadeDto(eq(secretaria1), any(Boolean.class)))
                .thenReturn(UnidadeDto.builder().codigo(2L).codigoPai(1L).sigla("SECRETARIA_1").subunidades(new ArrayList<>()).build());
        when(sgrhMapper.toUnidadeDto(eq(assessoria11), any(Boolean.class)))
                .thenReturn(UnidadeDto.builder().codigo(3L).codigoPai(2L).sigla("ASSESSORIA_11").isElegivel(true).subunidades(new ArrayList<>()).build());

        // Act
        // TipoProcesso.MAPEAMENTO não requer mapa vigente
        List<UnidadeDto> resultado = unidadeService.buscarArvoreComElegibilidade(TipoProcesso.MAPEAMENTO, null);

        // Assert
        // Deve encontrar a árvore e ASSESSORIA_11 deve ser elegível
        UnidadeDto dtoSedoc = resultado.stream()
                .filter(u -> u.getSigla().equals("SEDOC"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("SEDOC não encontrada"));

        UnidadeDto dtoSecretaria1 = dtoSedoc.getSubunidades().stream()
                .filter(u -> u.getSigla().equals("SECRETARIA_1"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("SECRETARIA_1 não encontrada"));

        UnidadeDto dtoAssessoria11 = dtoSecretaria1.getSubunidades().stream()
                .filter(u -> u.getSigla().equals("ASSESSORIA_11"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("ASSESSORIA_11 não encontrada"));

        assertThat(dtoAssessoria11.isElegivel())
                .as("ASSESSORIA_11 deve ser elegível para Mapeamento")
                .isTrue();
    }
}
