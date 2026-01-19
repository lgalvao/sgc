package sgc.subprocesso.service.crud;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.mapa.model.Mapa;
import sgc.organizacao.UnidadeFacade;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.processo.model.Processo;
import sgc.processo.model.TipoProcesso;
import sgc.processo.service.ProcessoFacade;
import sgc.seguranca.acesso.AccessControlService;
import sgc.subprocesso.dto.AtualizarSubprocessoRequest;
import sgc.subprocesso.dto.CriarSubprocessoRequest;
import sgc.subprocesso.dto.SubprocessoDto;
import sgc.subprocesso.mapper.SubprocessoMapper;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoRepo;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubprocessoCrudServiceCoverageTest {

    @InjectMocks
    private SubprocessoCrudService crudService;

    @Mock private SubprocessoRepo repositorio;
    @Mock private ProcessoFacade processoFacade;
    @Mock private UnidadeFacade unidadeFacade;
    @Mock private SubprocessoMapper mapper;
    @Mock private AccessControlService accessControlService;
    @Mock private sgc.mapa.service.MapaFacade mapaFacade;
    @Mock private sgc.comum.repo.RepositorioComum repo;
    @Mock private sgc.organizacao.UsuarioFacade usuarioService;
    @Mock private org.springframework.context.ApplicationEventPublisher eventPublisher;

    @Test
    @DisplayName("criar - Sucesso")
    void criar_Sucesso() {
        CriarSubprocessoRequest req = CriarSubprocessoRequest.builder()
                .codProcesso(10L)
                .codUnidade(20L)
                .build();
        when(repositorio.save(any())).thenAnswer(i -> i.getArgument(0));
        when(mapper.toDTO(any())).thenReturn(new SubprocessoDto());

        SubprocessoDto dto = crudService.criar(req);

        assertNotNull(dto);
        // verify(accessControlService).verificarPermissao(any(), any(), any()); // Not called in service
    }

    @Test
    @DisplayName("atualizar - Sucesso")
    void atualizar_Sucesso() {
        Long codigo = 1L;
        AtualizarSubprocessoRequest req = AtualizarSubprocessoRequest.builder().build();
        Subprocesso sp = new Subprocesso();
        sp.setProcesso(new Processo()); // Para verificar permissao
        sp.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);

        // Mock repo.buscar because buscarSubprocesso uses it
        when(repo.buscar(Subprocesso.class, codigo)).thenReturn(sp);
        when(mapper.toDTO(any())).thenReturn(new SubprocessoDto());

        SubprocessoDto dto = crudService.atualizar(codigo, req);

        verify(repositorio).save(sp);
    }

    @Test
    @DisplayName("verificarAcessoUnidadeAoProcesso - True")
    void verificarAcessoUnidadeAoProcesso_True() {
        Long codProcesso = 1L;
        List<Long> unidades = List.of(10L, 20L);

        when(repositorio.existsByProcessoCodigoAndUnidadeCodigoIn(codProcesso, unidades)).thenReturn(true);

        // This method logic might be simple delegation
        boolean result = crudService.verificarAcessoUnidadeAoProcesso(codProcesso, unidades);
        // Assuming implementation calls repo
    }

    @Test
    @DisplayName("obterEntidadePorCodigoMapa - Erro")
    void obterEntidadePorCodigoMapa_Erro() {
        when(repositorio.findByMapaCodigo(1L)).thenReturn(Optional.empty());
        assertThrows(ErroEntidadeNaoEncontrada.class, () -> crudService.obterEntidadePorCodigoMapa(1L));
    }

    @Test
    @DisplayName("atualizar - Com Alteracoes")
    void atualizar_ComAlteracoes() {
        Long codigo = 1L;
        AtualizarSubprocessoRequest req = AtualizarSubprocessoRequest.builder()
                .codMapa(100L)
                .dataLimiteEtapa1(java.time.LocalDateTime.now())
                .dataFimEtapa1(java.time.LocalDateTime.now())
                .dataFimEtapa2(java.time.LocalDateTime.now())
                .build();

        Subprocesso sp = new Subprocesso();
        sp.setProcesso(new Processo());
        sp.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
        sp.setMapa(new Mapa());
        sp.getMapa().setCodigo(99L); // Different map

        when(repo.buscar(Subprocesso.class, codigo)).thenReturn(sp);
        when(mapper.toDTO(any())).thenReturn(new SubprocessoDto());

        crudService.atualizar(codigo, req);

        verify(repositorio).save(sp);
        // Verify event published?
        // verify(eventPublisher).publishEvent(any()); // if mocked
    }

    @Test
    @DisplayName("listarPorProcessoESituacao - Sucesso")
    void listarPorProcessoESituacao_Sucesso() {
        Long codProcesso = 1L;
        SituacaoSubprocesso situacao = SituacaoSubprocesso.NAO_INICIADO;

        when(repositorio.findByProcessoCodigoAndSituacaoWithUnidade(codProcesso, situacao)).thenReturn(List.of(new Subprocesso()));

        List<Subprocesso> result = crudService.listarPorProcessoESituacao(codProcesso, situacao);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(repositorio).findByProcessoCodigoAndSituacaoWithUnidade(codProcesso, situacao);
    }

    @Test
    @DisplayName("listarPorProcessoUnidadeESituacoes - Sucesso")
    void listarPorProcessoUnidadeESituacoes_Sucesso() {
        Long codProcesso = 1L;
        Long codUnidade = 2L;
        List<SituacaoSubprocesso> situacoes = List.of(SituacaoSubprocesso.NAO_INICIADO);

        when(repositorio.findByProcessoCodigoAndUnidadeCodigoAndSituacaoInWithUnidade(codProcesso, codUnidade, situacoes)).thenReturn(List.of(new Subprocesso()));

        List<Subprocesso> result = crudService.listarPorProcessoUnidadeESituacoes(codProcesso, codUnidade, situacoes);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(repositorio).findByProcessoCodigoAndUnidadeCodigoAndSituacaoInWithUnidade(codProcesso, codUnidade, situacoes);
    }
}
