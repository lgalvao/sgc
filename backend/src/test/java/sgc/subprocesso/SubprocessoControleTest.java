package sgc.subprocesso;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.mapa.ImpactoMapaService;
import sgc.mapa.MapaService;
import sgc.subprocesso.dto.*;
import sgc.subprocesso.modelo.SubprocessoRepo;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class SubprocessoControleTest {
    @Mock
    private SubprocessoRepo subprocessoRepo;

    @Mock
    private SubprocessoService subprocessoService;

    @Mock
    private MapaService mapaService;

    @Mock
    private ImpactoMapaService impactoMapaService;

    @Mock
    private SubprocessoMapper subprocessoMapper;

    @Captor
    private ArgumentCaptor<Long> idCaptor;

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        SubprocessoControle controle = new SubprocessoControle(
                subprocessoRepo,
                subprocessoService,
                mapaService,
                impactoMapaService,
                subprocessoMapper
        );
        mockMvc = MockMvcBuilders.standaloneSetup(controle).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
    }

    @Test
    void listar_RetornaListaDeSubprocessos() throws Exception {
        when(subprocessoRepo.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/subprocessos"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        verify(subprocessoRepo).findAll();
    }

    @Test
    void obterPorId_SubprocessoEncontrado_RetornaDetalhes() throws Exception {
        SubprocessoDetalheDto detalhes = new SubprocessoDetalheDto(null, null, "CADASTRO_EM_ELABORACAO", null, null, Collections.emptyList(), Collections.emptyList());
        when(subprocessoService.obterDetalhes(eq(1L), any(String.class), any(Long.class))).thenReturn(detalhes);

        mockMvc.perform(get("/api/subprocessos/1?perfil=GESTOR&unidadeUsuario=1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.situacao").value("CADASTRO_EM_ELABORACAO"));

        verify(subprocessoService).obterDetalhes(eq(1L), any(String.class), any(Long.class));
    }

    @Test
    void obterPorId_SubprocessoNaoEncontrado_RetornaNotFound() throws Exception {
        when(subprocessoService.obterDetalhes(eq(1L), any(String.class), any(Long.class)))
                .thenThrow(new sgc.comum.erros.ErroEntidadeNaoEncontrada("Subprocesso não encontrado"));

        mockMvc.perform(get("/api/subprocessos/1?perfil=GESTOR&unidadeUsuario=1"))
                .andExpect(status().isNotFound());

        verify(subprocessoService).obterDetalhes(eq(1L), any(String.class), any(Long.class));
    }

    @Test
    void obterPorId_AcessoNegado_RetornaForbidden() throws Exception {
        when(subprocessoService.obterDetalhes(eq(1L), any(String.class), any(Long.class)))
                .thenThrow(new sgc.comum.erros.ErroDominioAccessoNegado("Acesso negado"));

        mockMvc.perform(get("/api/subprocessos/1?perfil=GESTOR&unidadeUsuario=999"))
                .andExpect(status().isForbidden());

        verify(subprocessoService).obterDetalhes(eq(1L), any(String.class), any(Long.class));
    }

    @Test
    void disponibilizarCadastro_SubprocessoNaoEncontrado_RetornaNotFound() throws Exception {
        when(subprocessoService.obterAtividadesSemConhecimento(1L)).thenReturn(Collections.emptyList());
        doThrow(new ErroEntidadeNaoEncontrada("Subprocesso não encontrado"))
                .when(subprocessoService).disponibilizarCadastro(1L);

        mockMvc.perform(post("/api/subprocessos/1/disponibilizar-cadastro"))
                .andExpect(status().isNotFound());

        verify(subprocessoService).disponibilizarCadastro(1L);
    }

    @Test
    void disponibilizarCadastro_VerificaAtividadesSemConhecimento() throws Exception {
        sgc.atividade.modelo.Atividade atividade = new sgc.atividade.modelo.Atividade();
        atividade.setCodigo(100L);
        atividade.setDescricao("Atividade sem conhecimento");
        when(subprocessoService.obterAtividadesSemConhecimento(1L)).thenReturn(List.of(atividade));

        mockMvc.perform(post("/api/subprocessos/1/disponibilizar-cadastro"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.atividadesSemConhecimento").isArray())
                .andExpect(jsonPath("$.atividadesSemConhecimento[0].id").value(100L))
                .andExpect(jsonPath("$.atividadesSemConhecimento[0].descricao").value("Atividade sem conhecimento"));

        verify(subprocessoService).obterAtividadesSemConhecimento(1L);
    }

    @Test
    void disponibilizarCadastro_Sucesso_RetornaOk() throws Exception {
        when(subprocessoService.obterAtividadesSemConhecimento(1L)).thenReturn(Collections.emptyList());

        mockMvc.perform(post("/api/subprocessos/1/disponibilizar-cadastro"))
                .andExpect(status().isOk())
                .andExpect(content().string("Cadastro de atividades disponibilizado"));

        verify(subprocessoService).obterAtividadesSemConhecimento(1L);
        verify(subprocessoService).disponibilizarCadastro(1L);
    }

    @Test
    void disponibilizarRevisao_SubprocessoNaoEncontrado_RetornaNotFound() throws Exception {
        when(subprocessoService.obterAtividadesSemConhecimento(1L)).thenReturn(Collections.emptyList());
        doThrow(new sgc.comum.erros.ErroEntidadeNaoEncontrada("Subprocesso não encontrado"))
                .when(subprocessoService).disponibilizarRevisao(1L);

        mockMvc.perform(post("/api/subprocessos/1/disponibilizar-revisao"))
                .andExpect(status().isNotFound());

        verify(subprocessoService).disponibilizarRevisao(1L);
    }

    @Test
    void disponibilizarRevisao_VerificaAtividadesSemConhecimento() throws Exception {
        sgc.atividade.modelo.Atividade atividade = new sgc.atividade.modelo.Atividade();
        atividade.setCodigo(100L);
        atividade.setDescricao("Atividade sem conhecimento");
        when(subprocessoService.obterAtividadesSemConhecimento(1L)).thenReturn(List.of(atividade));

        mockMvc.perform(post("/api/subprocessos/1/disponibilizar-revisao"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.atividadesSemConhecimento").isArray())
                .andExpect(jsonPath("$.atividadesSemConhecimento[0].id").value(100L))
                .andExpect(jsonPath("$.atividadesSemConhecimento[0].descricao").value("Atividade sem conhecimento"));

        verify(subprocessoService).obterAtividadesSemConhecimento(1L);
    }

    @Test
    void disponibilizarRevisao_Sucesso_RetornaOk() throws Exception {
        when(subprocessoService.obterAtividadesSemConhecimento(1L)).thenReturn(Collections.emptyList());

        mockMvc.perform(post("/api/subprocessos/1/disponibilizar-revisao"))
                .andExpect(status().isOk())
                .andExpect(content().string("Revisão do cadastro de atividades disponibilizada"));

        verify(subprocessoService).obterAtividadesSemConhecimento(1L);
        verify(subprocessoService).disponibilizarRevisao(1L);
    }

    @Test
    void devolverCadastro_DadosValidos_RealizaDevolucao() throws Exception {
        DevolverCadastroReq request = new DevolverCadastroReq("Motivo", "Observações");
        SubprocessoDto resultado = new SubprocessoDto();
        resultado.setCodigo(1L);

        when(subprocessoService.devolverCadastro(eq(1L), eq("Motivo"), eq("Observações"), any(String.class)))
                .thenReturn(resultado);

        mockMvc.perform(post("/api/subprocessos/1/devolver-cadastro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        verify(subprocessoService).devolverCadastro(eq(1L), eq("Motivo"), eq("Observações"), any(String.class));
    }

    @Test
    void devolverCadastro_DadosInvalidos_RetornaBadRequest() throws Exception {
        DevolverCadastroReq request = new DevolverCadastroReq("", "Observações"); // Motivo é obrigatório

        mockMvc.perform(post("/api/subprocessos/1/devolver-cadastro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void aceitarCadastro_DadosValidos_RealizaAceite() throws Exception {
        AceitarCadastroReq request = new AceitarCadastroReq("Observações");
        SubprocessoDto resultado = new SubprocessoDto();
        resultado.setCodigo(1L);

        when(subprocessoService.aceitarCadastro(eq(1L), eq("Observações"), any(String.class)))
                .thenReturn(resultado);

        mockMvc.perform(post("/api/subprocessos/1/aceitar-cadastro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        verify(subprocessoService).aceitarCadastro(eq(1L), eq("Observações"), any(String.class));
    }

    @Test
    void homologarCadastro_DadosValidos_RealizaHomologacao() throws Exception {
        HomologarCadastroReq request = new HomologarCadastroReq("Observações");
        SubprocessoDto resultado = new SubprocessoDto();
        resultado.setCodigo(1L);

        when(subprocessoService.homologarCadastro(eq(1L), eq("Observações"), any(String.class)))
                .thenReturn(resultado);

        mockMvc.perform(post("/api/subprocessos/1/homologar-cadastro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        verify(subprocessoService).homologarCadastro(eq(1L), eq("Observações"), any(String.class));
    }

    @Test
    void disponibilizarMapa_DadosValidos_RealizaDisponibilizacao() throws Exception {
        DisponibilizarMapaReq request = new DisponibilizarMapaReq("Observações", LocalDate.now().plusDays(10));
        SubprocessoDto resultado = new SubprocessoDto();
        resultado.setCodigo(1L);

        when(subprocessoService.disponibilizarMapa(eq(1L), eq("Observações"), any(LocalDate.class), any(String.class)))
                .thenReturn(resultado);

        mockMvc.perform(post("/api/subprocessos/1/disponibilizar-mapa")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        verify(subprocessoService).disponibilizarMapa(eq(1L), eq("Observações"), any(LocalDate.class), any(String.class));
    }

    @Test
    void apresentarSugestoes_DadosValidos_RealizaAcao() throws Exception {
        ApresentarSugestoesReq request = new ApresentarSugestoesReq("Sugestões para melhoria");
        SubprocessoDto resultado = new SubprocessoDto();
        resultado.setCodigo(1L);

        when(subprocessoService.apresentarSugestoes(eq(1L), eq("Sugestões para melhoria"), any(String.class)))
                .thenReturn(resultado);

        mockMvc.perform(post("/api/subprocessos/1/apresentar-sugestoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        verify(subprocessoService).apresentarSugestoes(eq(1L), eq("Sugestões para melhoria"), any(String.class));
    }

    @Test
    void validarMapa_Sucesso_RealizaValidacao() throws Exception {
        SubprocessoDto resultado = new SubprocessoDto();
        resultado.setCodigo(1L);

        when(subprocessoService.validarMapa(eq(1L), any(String.class))).thenReturn(resultado);

        mockMvc.perform(post("/api/subprocessos/1/validar-mapa"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        verify(subprocessoService).validarMapa(eq(1L), any(String.class));
    }

    @Test
    void obterSugestoes_Sucesso_RetornaSugestoes() throws Exception {
        SugestoesDto sugestoesDto = new SugestoesDto("Sugestões de melhoria", true, "Unidade Exemplo");
        when(subprocessoService.obterSugestoes(1L)).thenReturn(sugestoesDto);

        mockMvc.perform(get("/api/subprocessos/1/sugestoes"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.sugestoes").value("Sugestões de melhoria"))
                .andExpect(jsonPath("$.sugestoesApresentadas").value(true))
                .andExpect(jsonPath("$.unidadeNome").value("Unidade Exemplo"));

        verify(subprocessoService).obterSugestoes(1L);
    }

    @Test
    void obterHistoricoValidacao_Sucesso_RetornaHistorico() throws Exception {
        AnaliseValidacaoDto historicoItem = new AnaliseValidacaoDto(1L, java.time.LocalDateTime.now(), "Observações");
        when(subprocessoService.obterHistoricoValidacao(1L)).thenReturn(List.of(historicoItem));

        mockMvc.perform(get("/api/subprocessos/1/historico-validacao"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].observacoes").value("Observações"));

        verify(subprocessoService).obterHistoricoValidacao(1L);
    }

    @Test
    void devolverValidacao_DadosValidos_RealizaDevolucao() throws Exception {
        DevolverValidacaoReq request = new DevolverValidacaoReq("Justificativa para devolução");
        SubprocessoDto resultado = new SubprocessoDto();
        resultado.setCodigo(1L);

        when(subprocessoService.devolverValidacao(eq(1L), eq("Justificativa para devolução"), any(String.class)))
                .thenReturn(resultado);

        mockMvc.perform(post("/api/subprocessos/1/devolver-validacao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        verify(subprocessoService).devolverValidacao(eq(1L), eq("Justificativa para devolução"), any(String.class));
    }

    @Test
    void aceitarValidacao_Sucesso_RealizaAceite() throws Exception {
        SubprocessoDto resultado = new SubprocessoDto();
        resultado.setCodigo(1L);

        when(subprocessoService.aceitarValidacao(eq(1L), any(String.class))).thenReturn(resultado);

        mockMvc.perform(post("/api/subprocessos/1/aceitar-validacao"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        verify(subprocessoService).aceitarValidacao(eq(1L), any(String.class));
    }

    @Test
    void homologarValidacao_Sucesso_RealizaHomologacao() throws Exception {
        SubprocessoDto resultado = new SubprocessoDto();
        resultado.setCodigo(1L);

        when(subprocessoService.homologarValidacao(eq(1L), any(String.class))).thenReturn(resultado);

        mockMvc.perform(post("/api/subprocessos/1/homologar-validacao"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        verify(subprocessoService).homologarValidacao(eq(1L), any(String.class));
    }

    @Test
    void obterMapaParaAjuste_Sucesso_RetornaMapa() throws Exception {
        MapaAjusteDto mapaAjuste = new MapaAjusteDto(1L, "Unidade Exemplo", Collections.emptyList(), "Justificativa");
        when(subprocessoService.obterMapaParaAjuste(1L)).thenReturn(mapaAjuste);

        mockMvc.perform(get("/api/subprocessos/1/mapa-ajuste"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.unidadeNome").value("Unidade Exemplo"));

        verify(subprocessoService).obterMapaParaAjuste(1L);
    }

    @Test
    void criarSubprocesso_Sucesso_RetornaCriado() throws Exception {
        SubprocessoDto dto = new SubprocessoDto();
        dto.setProcessoCodigo(1L);
        dto.setUnidadeCodigo(1L);
        dto.setMapaCodigo(1L);

        sgc.subprocesso.modelo.Subprocesso entity = new sgc.subprocesso.modelo.Subprocesso();
        entity.setCodigo(1L);

        when(subprocessoMapper.toEntity(any(SubprocessoDto.class))).thenReturn(entity);
        when(subprocessoRepo.save(any(sgc.subprocesso.modelo.Subprocesso.class))).thenReturn(entity);
        when(subprocessoMapper.toDTO(any(sgc.subprocesso.modelo.Subprocesso.class))).thenReturn(dto);

        mockMvc.perform(post("/api/subprocessos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/subprocessos/1"));

        verify(subprocessoRepo).save(any(sgc.subprocesso.modelo.Subprocesso.class));
    }

    @Test
    void atualizarSubprocesso_SubprocessoNaoEncontrado_RetornaNotFound() throws Exception {
        SubprocessoDto dto = new SubprocessoDto();
        dto.setProcessoCodigo(1L);

        when(subprocessoRepo.findById(1L)).thenReturn(java.util.Optional.empty());

        mockMvc.perform(put("/api/subprocessos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());

        verify(subprocessoRepo).findById(1L);
    }

    @Test
    void atualizarSubprocesso_SubprocessoEncontrado_RealizaAtualizacao() throws Exception {
        SubprocessoDto dto = new SubprocessoDto();
        dto.setProcessoCodigo(1L);
        dto.setUnidadeCodigo(1L);
        dto.setMapaCodigo(1L);

        sgc.subprocesso.modelo.Subprocesso entity = new sgc.subprocesso.modelo.Subprocesso();
        entity.setCodigo(1L);

        when(subprocessoRepo.findById(1L)).thenReturn(java.util.Optional.of(entity));
        when(subprocessoRepo.save(any(sgc.subprocesso.modelo.Subprocesso.class))).thenReturn(entity);
        when(subprocessoMapper.toDTO(any(sgc.subprocesso.modelo.Subprocesso.class))).thenReturn(dto);

        mockMvc.perform(put("/api/subprocessos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        verify(subprocessoRepo).save(any(sgc.subprocesso.modelo.Subprocesso.class));
    }

    @Test
    void excluirSubprocesso_SubprocessoNaoEncontrado_RetornaNotFound() throws Exception {
        when(subprocessoRepo.findById(1L)).thenReturn(java.util.Optional.empty());

        mockMvc.perform(delete("/api/subprocessos/1"))
                .andExpect(status().isNotFound());

        verify(subprocessoRepo).findById(1L);
    }

    @Test
    void excluirSubprocesso_SubprocessoEncontrado_RealizaExclusao() throws Exception {
        sgc.subprocesso.modelo.Subprocesso entity = new sgc.subprocesso.modelo.Subprocesso();
        entity.setCodigo(1L);

        when(subprocessoRepo.findById(1L)).thenReturn(java.util.Optional.of(entity));

        mockMvc.perform(delete("/api/subprocessos/1"))
                .andExpect(status().isNoContent());

        verify(subprocessoRepo).deleteById(1L);
    }
}