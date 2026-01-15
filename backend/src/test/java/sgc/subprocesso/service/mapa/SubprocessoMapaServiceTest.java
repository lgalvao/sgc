package sgc.subprocesso.service.mapa;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.mapa.dto.AtividadeDto;
import sgc.mapa.mapper.AtividadeMapper;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.Competencia;
import sgc.mapa.model.Mapa;
import sgc.mapa.service.AtividadeService;
import sgc.mapa.service.CompetenciaService;
import sgc.mapa.service.CopiaMapaService;
import sgc.processo.model.Processo;
import sgc.subprocesso.dto.AtividadeAjusteDto;
import sgc.subprocesso.dto.CompetenciaAjusteDto;
import sgc.subprocesso.erros.ErroAtividadesEmSituacaoInvalida;
import sgc.subprocesso.erros.ErroMapaEmSituacaoInvalida;
import sgc.subprocesso.erros.ErroMapaNaoAssociado;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoMovimentacaoRepo;
import sgc.subprocesso.model.SubprocessoRepo;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("SubprocessoMapaService")
class SubprocessoMapaServiceTest {
    @Mock
    private SubprocessoRepo subprocessoRepo;

    @Mock
    private SubprocessoMovimentacaoRepo movimentacaoRepo;

    @Mock
    private AtividadeService atividadeService;

    @Mock
    private CompetenciaService competenciaService;

    @Mock
    private AtividadeMapper atividadeMapper;

    @Mock
    private CopiaMapaService copiaMapaService;

    @InjectMocks
    private SubprocessoMapaService subprocessoMapaService;

    @Test
    @DisplayName("salvarAjustesMapa deve lançar exceção se subprocesso não encontrado")
    void salvarAjustesMapa_NaoEncontrado() {
        when(subprocessoRepo.findById(1L)).thenReturn(Optional.empty());
        List<CompetenciaAjusteDto> emptyList = Collections.emptyList();
        
        assertThatThrownBy(() ->
                subprocessoMapaService.salvarAjustesMapa(1L, emptyList, "123"))
                .isInstanceOf(ErroEntidadeNaoEncontrada.class);
    }

    @Test
    @DisplayName("salvarAjustesMapa deve lançar exceção se situação inválida")
    void salvarAjustesMapa_SituacaoInvalida() {
        Subprocesso sp = new Subprocesso();
        sp.setSituacao(SituacaoSubprocesso.NAO_INICIADO);
        when(subprocessoRepo.findById(1L)).thenReturn(Optional.of(sp));
        List<CompetenciaAjusteDto> emptyList = Collections.emptyList();

        assertThatThrownBy(() ->
                subprocessoMapaService.salvarAjustesMapa(1L, emptyList, "123"))
                .isInstanceOf(ErroMapaEmSituacaoInvalida.class);
    }

    @Test
    @DisplayName("salvarAjustesMapa deve salvar com sucesso")
    void salvarAjustesMapa_Sucesso() {
        Subprocesso sp = new Subprocesso();
        sp.setSituacao(SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA);
        when(subprocessoRepo.findById(1L)).thenReturn(Optional.of(sp));

        AtividadeAjusteDto ativDto = AtividadeAjusteDto.builder()
                .codAtividade(20L)
                .nome("Nova Ativ")
                .build();

        CompetenciaAjusteDto compDto = CompetenciaAjusteDto.builder()
                .codCompetencia(10L)
                .nome("Nova Comp")
                .atividades(List.of(ativDto))
                .build();

        Competencia comp = new Competencia();
        comp.setCodigo(10L);
        when(competenciaService.buscarPorCodigo(10L)).thenReturn(comp);

        Atividade ativ = new Atividade();
        ativ.setCodigo(20L);
        when(atividadeService.obterPorCodigo(20L)).thenReturn(ativ);
        when(atividadeMapper.toDto(ativ)).thenReturn(new AtividadeDto());

        subprocessoMapaService.salvarAjustesMapa(1L, List.of(compDto), "123");

        verify(competenciaService).salvar(comp);
        verify(subprocessoRepo).save(sp);
    }

    @Test
    @DisplayName("importarAtividades deve lançar exceção se destino não encontrado")
    void importarAtividades_DestinoNaoEncontrado() {
        when(subprocessoRepo.findById(2L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> subprocessoMapaService.importarAtividades(2L, 1L))
                .isInstanceOf(ErroEntidadeNaoEncontrada.class);
    }

    @Test
    @DisplayName("importarAtividades deve lançar exceção se destino situação inválida")
    void importarAtividades_DestinoSituacaoInvalida() {
        Subprocesso dest = new Subprocesso();
        dest.setSituacao(SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA); // Inválida pra importar
        when(subprocessoRepo.findById(2L)).thenReturn(Optional.of(dest));

        assertThatThrownBy(() -> subprocessoMapaService.importarAtividades(2L, 1L))
                .isInstanceOf(ErroAtividadesEmSituacaoInvalida.class);
    }

    @Test
    @DisplayName("importarAtividades deve lançar exceção se origem não encontrada")
    void importarAtividades_OrigemNaoEncontrada() {
        Subprocesso dest = new Subprocesso();
        dest.setSituacao(SituacaoSubprocesso.NAO_INICIADO);
        when(subprocessoRepo.findById(2L)).thenReturn(Optional.of(dest));
        when(subprocessoRepo.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> subprocessoMapaService.importarAtividades(2L, 1L))
                .isInstanceOf(ErroEntidadeNaoEncontrada.class);
    }

    @Test
    @DisplayName("importarAtividades deve lançar exceção se mapa nulo")
    void importarAtividades_MapaNulo() {
        Subprocesso dest = new Subprocesso(); dest.setSituacao(SituacaoSubprocesso.NAO_INICIADO);
        Subprocesso orig = new Subprocesso(); // Sem mapa
        when(subprocessoRepo.findById(2L)).thenReturn(Optional.of(dest));
        when(subprocessoRepo.findById(1L)).thenReturn(Optional.of(orig));

        assertThatThrownBy(() -> subprocessoMapaService.importarAtividades(2L, 1L))
                .isInstanceOf(ErroMapaNaoAssociado.class);
    }

    @Test
    @DisplayName("importarAtividades deve ignorar alteração de status se tipo de processo for nulo")
    void importarAtividades_TipoProcessoNulo() {
        // Cobre o caso default/null no switch
        Subprocesso dest = new Subprocesso();
        dest.setSituacao(SituacaoSubprocesso.NAO_INICIADO);
        Mapa mapaDest = new Mapa();
        mapaDest.setCodigo(22L);
        dest.setMapa(mapaDest);

        Processo proc = new Processo();
        proc.setTipo(null); // Tipo nulo cai no default
        dest.setProcesso(proc);

        Subprocesso orig = new Subprocesso();
        Mapa mapaOrig = new Mapa();
        mapaOrig.setCodigo(11L);
        orig.setMapa(mapaOrig);

        when(subprocessoRepo.findById(2L)).thenReturn(Optional.of(dest));
        when(subprocessoRepo.findById(1L)).thenReturn(Optional.of(orig));

        // Simula lista de atividades origem para não sair no early return
        Atividade ativ = new Atividade();
        ativ.setDescricao("Ativ 1");

        subprocessoMapaService.importarAtividades(2L, 1L);

        // Verifica que status NÃO mudou (continua NAO_INICIADO)
        assertThat(dest.getSituacao()).isEqualTo(SituacaoSubprocesso.NAO_INICIADO);

        verify(copiaMapaService).importarAtividadesDeOutroMapa(11L, 22L);
    }
}
