package sgc.subprocesso.service.crud;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.comum.ComumRepo;
import sgc.mapa.model.Mapa;
import sgc.organizacao.UsuarioFacade;
import sgc.organizacao.model.Unidade;
import sgc.processo.model.Processo;
import sgc.subprocesso.dto.AtualizarSubprocessoRequest;
import sgc.subprocesso.dto.CriarSubprocessoRequest;
import sgc.subprocesso.dto.SubprocessoSituacaoDto;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoRepo;
import sgc.subprocesso.service.factory.SubprocessoFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
@DisplayName("Testes para SubprocessoCrudService")
class SubprocessoCrudServiceTest {
    @Mock
    private SubprocessoRepo subprocessoRepo;
    @Mock
    private ComumRepo repositorioComum;
    @Mock
    private SubprocessoFactory subprocessoFactory;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private UsuarioFacade usuarioService;

    @InjectMocks
    private SubprocessoCrudService service;

    private Subprocesso criarSubprocessoCompleto() {
        Subprocesso sp = new Subprocesso();
        Processo proc = new Processo();
        proc.setCodigo(1L);
        sp.setProcesso(proc);
        Unidade uni = new Unidade();
        uni.setCodigo(1L);
        sp.setUnidade(uni);
        Mapa mapa = new Mapa();
        mapa.setCodigo(1L);
        sp.setMapa(mapa);
        return sp;
    }

    @Test
    @DisplayName("Deve criar subprocesso com sucesso")
    void deveCriar() {
        CriarSubprocessoRequest request = CriarSubprocessoRequest.builder().codProcesso(1L).codUnidade(1L).build();
        Subprocesso entity = criarSubprocessoCompleto();

        when(subprocessoFactory.criar(request)).thenReturn(entity);

        assertThat(service.criarEntidade(request)).isNotNull();
    }

    @Test
    @DisplayName("Deve buscar subprocesso por código")
    void deveBuscarPorCodigo() {
        Subprocesso sp = new Subprocesso();
        when(subprocessoRepo.findByIdWithMapaAndAtividades(1L)).thenReturn(Optional.of(sp));
        assertThat(service.buscarSubprocesso(1L)).isNotNull();
    }

    @Test
    @DisplayName("Deve lançar exceção se não encontrar")
    void deveLancarExcecaoSeNaoEncontrar() {
        when(subprocessoRepo.findByIdWithMapaAndAtividades(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.buscarSubprocesso(1L))
                .isInstanceOf(ErroEntidadeNaoEncontrada.class);
    }

    @Test
    @DisplayName("Deve buscar subprocesso com mapa com sucesso")
    void deveBuscarSubprocessoComMapa() {
        Subprocesso sp = new Subprocesso();
        sp.setMapa(new Mapa());
        when(subprocessoRepo.findByIdWithMapaAndAtividades(1L)).thenReturn(Optional.of(sp));

        Subprocesso resultado = service.buscarSubprocessoComMapa(1L);
        assertThat(resultado).isNotNull();
        assertThat(resultado.getMapa()).isNotNull();
    }

    @Test
    @DisplayName("Deve listar entidades por processo")
    void deveListarEntidadesPorProcesso() {
        when(subprocessoRepo.findByProcessoCodigoWithUnidade(1L))
                .thenReturn(List.of(new Subprocesso()));

        List<Subprocesso> lista = service.listarEntidadesPorProcesso(1L);
        assertThat(lista).isNotEmpty();
    }

    @Test
    @DisplayName("Deve obter status do subprocesso")
    void deveObterStatus() {
        Subprocesso sp = new Subprocesso();
        sp.setCodigo(1L);
        sp.setSituacaoForcada(SituacaoSubprocesso.NAO_INICIADO);
        when(subprocessoRepo.findByIdWithMapaAndAtividades(1L)).thenReturn(Optional.of(sp));

        SubprocessoSituacaoDto status = service.obterStatus(1L);
        assertThat(status.codigo()).isEqualTo(1L);
        assertThat(status.situacao()).isEqualTo(SituacaoSubprocesso.NAO_INICIADO);

    }

    @Test
    @DisplayName("Deve obter entidade por código do mapa")
    void deveObterEntidadePorCodigoMapa() {
        Subprocesso sp = new Subprocesso();
        when(repositorioComum.buscar(Subprocesso.class, "mapa.codigo", 10L)).thenReturn(sp);

        assertThat(service.obterEntidadePorCodigoMapa(10L)).isEqualTo(sp);
    }

    @Test
    @DisplayName("Deve atualizar subprocesso definindo mapa")
    void deveAtualizarDefinindoMapa() {
        Subprocesso sp = criarSubprocessoCompleto();
        AtualizarSubprocessoRequest request = AtualizarSubprocessoRequest.builder().codMapa(5L).build();

        when(subprocessoRepo.findByIdWithMapaAndAtividades(1L)).thenReturn(Optional.of(sp));
        when(subprocessoRepo.save(sp)).thenReturn(sp);

        Subprocesso resultado = service.atualizarEntidade(1L, request);
        assertThat(resultado).isNotNull();
        assertThat(sp.getMapa().getCodigo()).isEqualTo(5L);
    }

    @Test
    @DisplayName("Deve lançar exceção ao atualizar subprocesso inexistente")
    void deveLancarExcecaoAoAtualizarInexistente() {
        when(subprocessoRepo.findByIdWithMapaAndAtividades(1L)).thenReturn(Optional.empty());
        AtualizarSubprocessoRequest request = AtualizarSubprocessoRequest.builder().build();

        assertThatThrownBy(() -> service.atualizarEntidade(1L, request))
                .isInstanceOf(ErroEntidadeNaoEncontrada.class);
    }

    @Test
    @DisplayName("Deve excluir subprocesso existente")
    void deveExcluirSubprocesso() {
        Subprocesso sp = criarSubprocessoCompleto();
        sp.setCodigo(1L);
        when(subprocessoRepo.findByIdWithMapaAndAtividades(1L)).thenReturn(Optional.of(sp));
        service.excluir(1L);
        verify(subprocessoRepo).deleteById(1L);
    }

    @Test
    @DisplayName("Deve lançar exceção ao excluir subprocesso inexistente")
    void deveLancarExcecaoAoExcluirInexistente() {
        when(subprocessoRepo.findByIdWithMapaAndAtividades(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.excluir(1L))
                .isInstanceOf(ErroEntidadeNaoEncontrada.class);
    }

    @Test
    @DisplayName("Deve listar todos os subprocessos")
    void deveListarTodos() {
        when(subprocessoRepo.findAllComFetch()).thenReturn(List.of(new Subprocesso()));

        assertThat(service.listarEntidades()).hasSize(1);
    }

    @Test
    @DisplayName("Deve obter subprocesso por processo e unidade")
    void deveObterPorProcessoEUnidade() {
        Subprocesso sp = new Subprocesso();
        when(subprocessoRepo.findByProcessoCodigoAndUnidadeCodigoWithFetch(1L, 2L)).thenReturn(Optional.of(sp));

        assertThat(service.obterEntidadePorProcessoEUnidade(1L, 2L)).isNotNull();
    }

    @Test
    @DisplayName("Deve verificar acesso da unidade ao processo")
    void deveVerificarAcesso() {
        when(subprocessoRepo.existsByProcessoCodigoAndUnidadeCodigoIn(1L, List.of(2L)))
                .thenReturn(true);

        assertThat(service.verificarAcessoUnidadeAoProcesso(1L, List.of(2L))).isTrue();
    }

    @Test
    @DisplayName("Deve atualizar subprocesso detectando alterações em campos diversos")
    void deveAtualizarDetectandoAlteracoes() {
        Subprocesso sp = criarSubprocessoCompleto();
        sp.setSituacaoForcada(SituacaoSubprocesso.NAO_INICIADO);
        AtualizarSubprocessoRequest request = AtualizarSubprocessoRequest.builder()
                .dataLimiteEtapa1(LocalDateTime.now())
                .dataFimEtapa1(LocalDateTime.now())
                .dataFimEtapa2(LocalDateTime.now())
                .build();

        when(subprocessoRepo.findByIdWithMapaAndAtividades(1L)).thenReturn(Optional.of(sp));
        when(subprocessoRepo.save(sp)).thenReturn(sp);

        Subprocesso resultado = service.atualizarEntidade(1L, request);
        assertThat(resultado).isNotNull();
        verify(subprocessoRepo).save(sp);
    }

    @Test
    @DisplayName("Deve atualizar quando nada mudar")
    void deveAtualizarSemMudancas() {
        Subprocesso sp = criarSubprocessoCompleto();
        sp.setSituacaoForcada(SituacaoSubprocesso.NAO_INICIADO);
        AtualizarSubprocessoRequest request = AtualizarSubprocessoRequest.builder().build();

        when(subprocessoRepo.findByIdWithMapaAndAtividades(1L)).thenReturn(Optional.of(sp));
        when(subprocessoRepo.save(sp)).thenReturn(sp);

        Subprocesso resultado = service.atualizarEntidade(1L, request);
        assertThat(resultado).isNotNull();
        verify(subprocessoRepo).save(sp);
    }

    @Test
    @DisplayName("Deve atualizar mantendo mapa quando existente e request sem mapa")
    void deveAtualizarMantendoMapaQuandoRequestSemMapa() {
        Subprocesso sp = criarSubprocessoCompleto();
        sp.setCodigo(1L);
        sp.setMapa(new Mapa()); // Tem mapa

        AtualizarSubprocessoRequest request = AtualizarSubprocessoRequest.builder().build(); // codMapa null

        when(subprocessoRepo.findByIdWithMapaAndAtividades(1L)).thenReturn(Optional.of(sp));
        when(subprocessoRepo.save(sp)).thenReturn(sp);

        service.atualizarEntidade(1L, request);

        assertThat(sp.getMapa()).isNotNull();
    }

    @Test
    @DisplayName("Deve atualizar mantendo mesmo mapa")
    void deveAtualizarMantendoMesmoMapa() {
        Subprocesso sp = criarSubprocessoCompleto();
        sp.setCodigo(1L);
        Mapa mapa = new Mapa();
        mapa.setCodigo(50L);
        sp.setMapa(mapa);

        // Request com mesmo ID de mapa
        AtualizarSubprocessoRequest request = AtualizarSubprocessoRequest.builder().codMapa(50L).build();

        when(subprocessoRepo.findByIdWithMapaAndAtividades(1L)).thenReturn(Optional.of(sp));
        when(subprocessoRepo.save(sp)).thenReturn(sp);

        Subprocesso resultado = service.atualizarEntidade(1L, request);
        assertThat(resultado).isNotNull();
        verify(subprocessoRepo).save(sp);
    }

    @Test
    @DisplayName("Deve atualizar definindo mapa quando subprocesso não tem mapa")
    void deveAtualizarDefinindoMapaQuandoSemMapa() {
        Subprocesso sp = criarSubprocessoCompleto();
        sp.setCodigo(1L);
        sp.setMapa(null); // Sem mapa

        AtualizarSubprocessoRequest request = AtualizarSubprocessoRequest.builder().codMapa(5L).build();

        when(subprocessoRepo.findByIdWithMapaAndAtividades(1L)).thenReturn(Optional.of(sp));
        when(subprocessoRepo.save(sp)).thenReturn(sp);

        service.atualizarEntidade(1L, request);

        assertThat(sp.getMapa()).isNotNull();
        assertThat(sp.getMapa().getCodigo()).isEqualTo(5L);
    }

    @Test
    @DisplayName("Deve listar subprocessos por processo e unidades")
    void deveListarPorProcessoEUnidades() {
        Subprocesso sp = new Subprocesso();
        when(subprocessoRepo.findByProcessoCodigoAndUnidadeCodigoInWithUnidade(1L, List.of(2L)))
                .thenReturn(List.of(sp));

        List<Subprocesso> lista = service.listarEntidadesPorProcessoEUnidades(1L, List.of(2L));
        assertThat(lista).hasSize(1);
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando unidades nulo ou vazio")
    void deveRetornarVazioQuandoUnidadesNuloOuVazio() {
        assertThat(service.listarEntidadesPorProcessoEUnidades(1L, null)).isEmpty();
        assertThat(service.listarEntidadesPorProcessoEUnidades(1L, List.of())).isEmpty();

        verify(subprocessoRepo, never()).findByProcessoCodigoAndUnidadeCodigoInWithUnidade(anyLong(), anyList());
    }
}
