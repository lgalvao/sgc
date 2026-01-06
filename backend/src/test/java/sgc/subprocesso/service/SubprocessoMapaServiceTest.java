package sgc.subprocesso.service;

import org.junit.jupiter.api.DisplayName;
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
import sgc.mapa.service.AtividadeService;
import sgc.mapa.service.CompetenciaService;
import sgc.subprocesso.dto.AtividadeAjusteDto;
import sgc.subprocesso.dto.CompetenciaAjusteDto;
import sgc.subprocesso.erros.ErroAtividadesEmSituacaoInvalida;
import sgc.subprocesso.erros.ErroMapaEmSituacaoInvalida;
import sgc.subprocesso.erros.ErroMapaNaoAssociado;
import sgc.subprocesso.model.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários: SubprocessoMapaService")
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
        when(competenciaService.buscarPorId(10L)).thenReturn(comp);

        Atividade ativ = new Atividade();
        ativ.setCodigo(20L);
        when(atividadeService.obterEntidadePorCodigo(20L)).thenReturn(ativ);
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
}
