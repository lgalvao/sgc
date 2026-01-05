package sgc.relatorio.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.Competencia;
import sgc.mapa.model.Conhecimento;
import sgc.mapa.model.Mapa;
import sgc.mapa.service.CompetenciaService;
import sgc.organizacao.UsuarioService;
import sgc.organizacao.dto.ResponsavelDto;
import sgc.organizacao.model.Unidade;
import sgc.processo.model.Processo;
import sgc.processo.service.ProcessoService;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.SubprocessoService;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("RelatorioService Test")
class RelatorioServiceTest {

    @Mock
    private ProcessoService processoService;
    @Mock
    private SubprocessoService subprocessoService;
    @Mock
    private UsuarioService usuarioService;
    @Mock
    private CompetenciaService competenciaService;

    @InjectMocks
    private RelatorioService relatorioService;

    @Test
    @DisplayName("Deve gerar relatório de andamento")
    void deveGerarRelatorioAndamento() {
        Processo p = new Processo();
        p.setDescricao("Proc Teste");

        Unidade u = new Unidade();
        u.setSigla("U1");
        u.setNome("Unidade 1");
        u.setCodigo(1L);

        Subprocesso sp = new Subprocesso();
        sp.setUnidade(u);
        sp.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);

        when(processoService.buscarEntidadePorId(1L)).thenReturn(p);
        when(subprocessoService.listarEntidadesPorProcesso(1L)).thenReturn(List.of(sp));
        when(usuarioService.buscarResponsavelUnidade(1L)).thenReturn(Optional.of(ResponsavelDto.builder().titularNome("Resp").build()));

        OutputStream out = new ByteArrayOutputStream();
        relatorioService.gerarRelatorioAndamento(1L, out);
        assertThat(out.toString()).isNotEmpty();
    }

    @Test
    @DisplayName("Deve gerar relatório de andamento sem responsável definido")
    void deveGerarRelatorioAndamentoSemResponsavel() {
        Processo p = new Processo();
        p.setDescricao("Proc Teste");

        Unidade u = new Unidade();
        u.setSigla("U1");
        u.setNome("Unidade 1");
        u.setCodigo(1L);

        Subprocesso sp = new Subprocesso();
        sp.setUnidade(u);
        sp.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);

        when(processoService.buscarEntidadePorId(1L)).thenReturn(p);
        when(subprocessoService.listarEntidadesPorProcesso(1L)).thenReturn(List.of(sp));
        when(usuarioService.buscarResponsavelUnidade(1L)).thenReturn(Optional.empty());

        OutputStream out = new ByteArrayOutputStream();
        relatorioService.gerarRelatorioAndamento(1L, out);
        assertThat(out.toString()).isNotEmpty();
    }

    @Test
    @DisplayName("Deve gerar relatório de mapas completo")
    void deveGerarRelatorioMapasCompleto() {
        Processo p = new Processo();
        p.setDescricao("Proc Teste");

        Unidade u = new Unidade();
        u.setSigla("U1");
        u.setNome("Unidade 1");

        Subprocesso sp = new Subprocesso();
        sp.setUnidade(u);
        sp.setMapa(new Mapa());
        sp.getMapa().setCodigo(10L);

        Competencia c = new Competencia();
        c.setDescricao("Comp 1");

        Atividade a = new Atividade();
        a.setDescricao("Ativ 1");

        Conhecimento k = new Conhecimento();
        k.setDescricao("Conh 1");
        a.setConhecimentos(List.of(k));
        c.setAtividades(java.util.Set.of(a));

        when(processoService.buscarEntidadePorId(1L)).thenReturn(p);
        when(subprocessoService.listarEntidadesPorProcesso(1L)).thenReturn(List.of(sp));
        when(competenciaService.buscarPorMapa(10L)).thenReturn(List.of(c));

        OutputStream out = new ByteArrayOutputStream();
        relatorioService.gerarRelatorioMapas(1L, null, out);
        assertThat(out.toString()).isNotEmpty();
    }

    @Test
    @DisplayName("Deve filtrar por unidade no relatório de mapas")
    void deveFiltrarPorUnidadeNoRelatorioMapas() {
        Processo p = new Processo();
        p.setDescricao("Proc Teste");

        Unidade u1 = new Unidade(); u1.setCodigo(1L); u1.setSigla("U1");
        Unidade u2 = new Unidade(); u2.setCodigo(2L); u2.setSigla("U2");

        Subprocesso sp1 = new Subprocesso(); sp1.setUnidade(u1); sp1.setMapa(new Mapa()); sp1.getMapa().setCodigo(10L);
        Subprocesso sp2 = new Subprocesso(); sp2.setUnidade(u2); sp2.setMapa(new Mapa()); sp2.getMapa().setCodigo(20L);

        when(processoService.buscarEntidadePorId(1L)).thenReturn(p);
        when(subprocessoService.listarEntidadesPorProcesso(1L)).thenReturn(List.of(sp1, sp2));
        when(competenciaService.buscarPorMapa(10L)).thenReturn(List.of());

        OutputStream out = new ByteArrayOutputStream();
        // Filtra pela unidade 1
        relatorioService.gerarRelatorioMapas(1L, 1L, out);
        assertThat(out.toString()).isNotEmpty();
    }
}
