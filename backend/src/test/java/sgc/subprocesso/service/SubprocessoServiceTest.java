package sgc.subprocesso.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.atividade.model.Atividade;
import sgc.atividade.model.AtividadeRepo;
import sgc.atividade.model.Conhecimento;
import sgc.atividade.model.ConhecimentoRepo;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.comum.erros.ErroValidacao;
import sgc.mapa.model.Competencia;
import sgc.mapa.model.CompetenciaRepo;
import sgc.mapa.model.Mapa;
import sgc.subprocesso.dto.SubprocessoDto;
import sgc.subprocesso.dto.SubprocessoMapper;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoRepo;
import sgc.processo.model.Processo;
import sgc.unidade.model.Unidade;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubprocessoServiceTest {

    @Mock private SubprocessoRepo repositorioSubprocesso;
    @Mock private AtividadeRepo atividadeRepo;
    @Mock private ConhecimentoRepo repositorioConhecimento;
    @Mock private CompetenciaRepo competenciaRepo;
    @Mock private SubprocessoMapper subprocessoMapper;

    @InjectMocks private SubprocessoService service;

    @Test
    @DisplayName("obterAtividadesSemConhecimento deve retornar lista vazia se todas tiverem")
    void obterAtividadesSemConhecimentoVazia() {
        Long id = 1L;
        Subprocesso sp = new Subprocesso();
        sp.setMapa(new Mapa());
        sp.getMapa().setCodigo(10L);

        Atividade ativ = new Atividade();
        ativ.setCodigo(100L);

        when(repositorioSubprocesso.findById(id)).thenReturn(Optional.of(sp));
        when(atividadeRepo.findByMapaCodigo(10L)).thenReturn(List.of(ativ));
        when(repositorioConhecimento.findByAtividadeCodigo(100L)).thenReturn(List.of(new Conhecimento()));

        var res = service.obterAtividadesSemConhecimento(id);

        assertThat(res).isEmpty();
    }

    @Test
    @DisplayName("obterAtividadesSemConhecimento deve retornar atividades sem conhecimento")
    void obterAtividadesSemConhecimentoComItens() {
        Long id = 1L;
        Subprocesso sp = new Subprocesso();
        sp.setMapa(new Mapa());
        sp.getMapa().setCodigo(10L);

        Atividade ativ = new Atividade();
        ativ.setCodigo(100L);

        when(repositorioSubprocesso.findById(id)).thenReturn(Optional.of(sp));
        when(atividadeRepo.findByMapaCodigo(10L)).thenReturn(List.of(ativ));
        when(repositorioConhecimento.findByAtividadeCodigo(100L)).thenReturn(Collections.emptyList());

        var res = service.obterAtividadesSemConhecimento(id);

        assertThat(res).hasSize(1);
    }

    @Test
    @DisplayName("validarAssociacoesMapa sucesso")
    void validarAssociacoesMapa() {
        Long id = 1L;
        Competencia comp = new Competencia();
        comp.setAtividades(Set.of(new Atividade()));
        Atividade ativ = new Atividade();
        ativ.setCompetencias(Set.of(comp));

        when(competenciaRepo.findByMapaCodigo(id)).thenReturn(List.of(comp));
        when(atividadeRepo.findByMapaCodigo(id)).thenReturn(List.of(ativ));

        service.validarAssociacoesMapa(id);
    }

    @Test
    @DisplayName("validarAssociacoesMapa falha competencia isolada")
    void validarAssociacoesMapaCompIsolada() {
        Long id = 1L;
        Competencia comp = new Competencia();
        comp.setAtividades(Collections.emptySet());

        when(competenciaRepo.findByMapaCodigo(id)).thenReturn(List.of(comp));

        assertThatThrownBy(() -> service.validarAssociacoesMapa(id))
            .isInstanceOf(ErroValidacao.class);
    }

    @Test
    @DisplayName("validarAssociacoesMapa falha atividade isolada")
    void validarAssociacoesMapaAtivIsolada() {
        Long id = 1L;
        Competencia comp = new Competencia();
        comp.setAtividades(Set.of(new Atividade()));
        Atividade ativ = new Atividade();
        ativ.setCompetencias(Collections.emptySet());

        when(competenciaRepo.findByMapaCodigo(id)).thenReturn(List.of(comp));
        when(atividadeRepo.findByMapaCodigo(id)).thenReturn(List.of(ativ));

        assertThatThrownBy(() -> service.validarAssociacoesMapa(id))
            .isInstanceOf(ErroValidacao.class);
    }

    @Test
    @DisplayName("criar salva entidade")
    void criar() {
        SubprocessoDto dto = SubprocessoDto.builder().build();
        when(subprocessoMapper.toEntity(dto)).thenReturn(new Subprocesso());
        when(repositorioSubprocesso.save(any())).thenReturn(new Subprocesso());
        when(subprocessoMapper.toDTO(any())).thenReturn(dto);

        service.criar(dto);

        verify(repositorioSubprocesso).save(any());
    }

    @Test
    @DisplayName("atualizar modifica e salva")
    void atualizar() {
        Long id = 1L;
        SubprocessoDto dto = SubprocessoDto.builder()
            .codProcesso(10L)
            .codUnidade(20L)
            .codMapa(30L)
            .build();

        Subprocesso sp = new Subprocesso();

        when(repositorioSubprocesso.findById(id)).thenReturn(Optional.of(sp));
        when(repositorioSubprocesso.save(any())).thenReturn(sp);
        when(subprocessoMapper.toDTO(any())).thenReturn(dto);

        var res = service.atualizar(id, dto);

        assertThat(res.getCodProcesso()).isEqualTo(10L);
        assertThat(sp.getProcesso().getCodigo()).isEqualTo(10L);
    }

    @Test
    @DisplayName("excluir deleta se existir")
    void excluir() {
        Long id = 1L;
        when(repositorioSubprocesso.existsById(id)).thenReturn(true);

        service.excluir(id);

        verify(repositorioSubprocesso).deleteById(id);
    }
}
