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
import sgc.processo.model.Processo;
import sgc.processo.model.ProcessoRepo;
import sgc.processo.model.SituacaoProcesso;
import sgc.sgrh.dto.UnidadeDto;
import sgc.sgrh.model.Usuario;
import sgc.sgrh.model.UsuarioRepo;
import sgc.processo.model.TipoProcesso;
import sgc.unidade.dto.CriarAtribuicaoTemporariaRequest;
import sgc.unidade.model.*;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UnidadeServiceTest {

    @InjectMocks
    private UnidadeService unidadeService;

    @Mock
    private UnidadeRepo unidadeRepo;

    @Mock
    private MapaRepo mapaRepo;

    @Mock
    private UsuarioRepo usuarioRepo;

    @Mock
    private AtribuicaoTemporariaRepo atribuicaoTemporariaRepo;

    @Mock
    private ProcessoRepo processoRepo;

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
        when(unidadeRepo.findAllComMapas()).thenReturn(List.of(new Unidade("U1", "SIGLA1")));
        when(processoRepo.findBySituacao(SituacaoProcesso.EM_ANDAMENTO)).thenReturn(List.of());
        when(processoRepo.findBySituacao(SituacaoProcesso.CRIADO)).thenReturn(List.of());

        unidadeService.buscarArvoreComElegibilidade(TipoProcesso.REVISAO, 1L);

        verify(unidadeRepo).findAllComMapas();
    }

    @Test
    @DisplayName("buscarArvoreComElegibilidade deve usar findAll quando nao requerMapaVigente")
    void buscarArvoreComElegibilidadeMapeamento() {
        when(unidadeRepo.findAll()).thenReturn(List.of(new Unidade("U1", "SIGLA1")));
        when(processoRepo.findBySituacao(SituacaoProcesso.EM_ANDAMENTO)).thenReturn(List.of());
        when(processoRepo.findBySituacao(SituacaoProcesso.CRIADO)).thenReturn(List.of());

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

        Processo processoAtivo = new Processo();
        processoAtivo.setCodigo(99L);
        processoAtivo.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        processoAtivo.setParticipantes(Set.of(unidade));

        when(processoRepo.findBySituacao(SituacaoProcesso.EM_ANDAMENTO)).thenReturn(List.of(processoAtivo));
        when(processoRepo.findBySituacao(SituacaoProcesso.CRIADO)).thenReturn(List.of());

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

        Processo processoAtual = new Processo();
        processoAtual.setCodigo(100L);
        processoAtual.setSituacao(SituacaoProcesso.CRIADO);
        processoAtual.setParticipantes(Set.of(unidade));

        when(processoRepo.findBySituacao(SituacaoProcesso.EM_ANDAMENTO)).thenReturn(List.of());
        when(processoRepo.findBySituacao(SituacaoProcesso.CRIADO)).thenReturn(List.of(processoAtual));

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

        when(processoRepo.findBySituacao(SituacaoProcesso.EM_ANDAMENTO)).thenReturn(List.of());
        when(processoRepo.findBySituacao(SituacaoProcesso.CRIADO)).thenReturn(List.of());

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
        when(processoRepo.findBySituacao(SituacaoProcesso.EM_ANDAMENTO)).thenReturn(List.of());
        when(processoRepo.findBySituacao(SituacaoProcesso.CRIADO)).thenReturn(List.of());

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
}
