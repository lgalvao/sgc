package sgc.subprocesso.service.crud;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.mapa.model.Mapa;
import sgc.processo.model.Processo;
import sgc.subprocesso.dto.AtualizarSubprocessoRequest;
import sgc.subprocesso.dto.CriarSubprocessoRequest;
import sgc.subprocesso.dto.SubprocessoDto;
import sgc.subprocesso.mapper.SubprocessoMapper;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoRepo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubprocessoCrudServiceCoverageTest {
    @InjectMocks
    private SubprocessoCrudService crudService;
    @Mock
    private SubprocessoRepo repositorio;
    @Mock
    private SubprocessoMapper mapper;
    @Mock
    private sgc.comum.repo.RepositorioComum repo;

    @Test
    @DisplayName("criar - Sucesso")
    void criar_Sucesso() {
        CriarSubprocessoRequest req = CriarSubprocessoRequest.builder()
                .codProcesso(10L)
                .codUnidade(20L)
                .build();
        when(repositorio.save(any())).thenAnswer(i -> i.getArgument(0));
        when(mapper.toDto(any())).thenReturn(new SubprocessoDto());

        SubprocessoDto dto = crudService.criar(req);
        assertNotNull(dto);
    }

    @Test
    @DisplayName("atualizar - Sucesso")
    void atualizar_Sucesso() {
        Long codigo = 1L;
        Subprocesso sp = new Subprocesso();
        Processo proc = new Processo();
        proc.setCodigo(1L);
        sp.setProcesso(proc);
        sgc.organizacao.model.Unidade uni = new sgc.organizacao.model.Unidade();
        uni.setCodigo(1L);
        sp.setUnidade(uni);
        sp.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);

        AtualizarSubprocessoRequest req = AtualizarSubprocessoRequest.builder().build();
        when(repo.buscar(Subprocesso.class, codigo)).thenReturn(sp);
        when(mapper.toDto(any())).thenReturn(new SubprocessoDto());

        crudService.atualizar(codigo, req);

        verify(repositorio).save(sp);
    }

    @Test
    @DisplayName("verificarAcessoUnidadeAoProcesso - True")
    void verificarAcessoUnidadeAoProcesso_True() {
        Long codProcesso = 1L;
        List<Long> unidades = List.of(10L, 20L);

        when(repositorio.existsByProcessoCodigoAndUnidadeCodigoIn(codProcesso, unidades)).thenReturn(true);

        boolean result = crudService.verificarAcessoUnidadeAoProcesso(codProcesso, unidades);
        assertTrue(result);
    }

    @Test
    @DisplayName("obterEntidadePorCodigoMapa - Erro")
    void obterEntidadePorCodigoMapa_Erro() {
        when(repositorio.findByMapaCodigo(1L)).thenReturn(Optional.empty());
        var exception = assertThrows(ErroEntidadeNaoEncontrada.class, () -> crudService.obterEntidadePorCodigoMapa(1L));
        assertNotNull(exception);
    }

    @Test
    @DisplayName("atualizar - Com Alteracoes")
    void atualizar_ComAlteracoes() {
        Long codigo = 1L;
        AtualizarSubprocessoRequest req = AtualizarSubprocessoRequest.builder()
                .codMapa(100L)
                .dataLimiteEtapa1(LocalDateTime.now())
                .dataFimEtapa1(LocalDateTime.now())
                .dataFimEtapa2(LocalDateTime.now())
                .build();

        Subprocesso sp = new Subprocesso();
        sp.setProcesso(new Processo());
        sp.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
        sp.setMapa(new Mapa());
        sp.getMapa().setCodigo(99L); // Different map

        when(repo.buscar(Subprocesso.class, codigo)).thenReturn(sp);
        when(mapper.toDto(any())).thenReturn(new SubprocessoDto());

        crudService.atualizar(codigo, req);
        verify(repositorio).save(sp);
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

    @Test
    @DisplayName("criar - Sem Processo e Unidade")
    void criar_SemProcessoUnidade() {
        CriarSubprocessoRequest req = CriarSubprocessoRequest.builder()
                .codProcesso(null)
                .codUnidade(null)
                .build();
        when(repositorio.save(any())).thenAnswer(i -> i.getArgument(0));
        when(mapper.toDto(any())).thenReturn(new SubprocessoDto());

        SubprocessoDto dto = crudService.criar(req);

        assertNotNull(dto);
        // Verifica que nao setou processo/unidade na entidade
        // (Isso eh dificil verificar sem captor, mas o teste cobre os branches null)
    }

    @Test
    @DisplayName("atualizar - Sem CodMapa (null safe)")
    void atualizar_SemCodMapa() {
        Long codigo = 1L;
        AtualizarSubprocessoRequest req = AtualizarSubprocessoRequest.builder()
                .codMapa(null) // null
                .build();

        Subprocesso sp = new Subprocesso();
        sp.setProcesso(new Processo());
        sp.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);

        when(repo.buscar(Subprocesso.class, codigo)).thenReturn(sp);
        when(mapper.toDto(any())).thenReturn(new SubprocessoDto());

        crudService.atualizar(codigo, req);

        verify(repositorio).save(sp);
    }

    @Test
    @DisplayName("obterStatus - Situação Nula")
    void obterStatus_SituacaoNula() {
        Long codigo = 1L;
        Subprocesso sp = new Subprocesso();
        sp.setCodigo(codigo);
        sp.setSituacao(null); // Situação nula

        when(repo.buscar(Subprocesso.class, codigo)).thenReturn(sp);

        var status = crudService.obterStatus(codigo);

        assertNotNull(status);
        assertNull(status.situacaoLabel());
        assertNull(status.situacao());
    }
}
