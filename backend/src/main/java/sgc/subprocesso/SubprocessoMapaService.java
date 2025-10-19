package sgc.subprocesso;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.atividade.modelo.Atividade;
import sgc.atividade.modelo.AtividadeRepo;
import sgc.competencia.modelo.CompetenciaAtividade;
import sgc.competencia.modelo.CompetenciaAtividadeRepo;
import sgc.competencia.modelo.CompetenciaRepo;
import sgc.comum.erros.ErroDominioNaoEncontrado;
import sgc.conhecimento.modelo.Conhecimento;
import sgc.conhecimento.modelo.ConhecimentoRepo;
import sgc.subprocesso.dto.AtividadeAjusteDto;
import sgc.subprocesso.dto.CompetenciaAjusteDto;
import sgc.subprocesso.dto.ConhecimentoAjusteDto;
import sgc.subprocesso.modelo.Movimentacao;
import sgc.subprocesso.modelo.MovimentacaoRepo;
import sgc.subprocesso.modelo.Subprocesso;
import sgc.subprocesso.modelo.SubprocessoRepo;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubprocessoMapaService {

    private final SubprocessoRepo repositorioSubprocesso;
    private final MovimentacaoRepo repositorioMovimentacao;
    private final AtividadeRepo atividadeRepo;
    private final ConhecimentoRepo repositorioConhecimento;
    private final CompetenciaAtividadeRepo competenciaAtividadeRepo;
    private final CompetenciaRepo competenciaRepo;

    @Transactional
    public void salvarAjustesMapa(Long idSubprocesso, List<CompetenciaAjusteDto> competencias, Long usuarioTituloEleitoral) {
        Subprocesso sp = repositorioSubprocesso.findById(idSubprocesso)
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Subprocesso não encontrado: " + idSubprocesso));

        if (sp.getSituacao() != SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA &&
                sp.getSituacao() != SituacaoSubprocesso.MAPA_AJUSTADO) {
            throw new IllegalStateException("Ajustes no mapa só podem ser feitos em estados específicos. Situação atual: " + sp.getSituacao());
        }

        log.info("Salvando ajustes para o mapa do subprocesso {}...", idSubprocesso);

        // Atualiza as descrições
        for (CompetenciaAjusteDto compDto : competencias) {
            competenciaRepo.findById(compDto.getCompetenciaId()).ifPresent(c -> c.setDescricao(compDto.getNome()));
            for (AtividadeAjusteDto ativDto : compDto.getAtividades()) {
                atividadeRepo.findById(ativDto.getAtividadeId()).ifPresent(a -> a.setDescricao(ativDto.getNome()));
            }
        }

        // Recria os Vínculos
        competenciaAtividadeRepo.deleteByCompetenciaMapaCodigo(sp.getMapa().getCodigo());

        for (CompetenciaAjusteDto compDto : competencias) {
            var competencia = competenciaRepo.findById(compDto.getCompetenciaId())
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Competência não encontrada: " + compDto.getCompetenciaId()));

            for (AtividadeAjusteDto ativDto : compDto.getAtividades()) {
                var atividade = atividadeRepo.findById(ativDto.getAtividadeId())
                    .orElseThrow(() -> new ErroDominioNaoEncontrado("Atividade não encontrada: " + ativDto.getAtividadeId()));

                var id = new CompetenciaAtividade.Id(ativDto.getAtividadeId(), compDto.getCompetenciaId());
                CompetenciaAtividade novoLink = new CompetenciaAtividade(id, competencia, atividade);
                competenciaAtividadeRepo.save(novoLink);
            }
        }

        sp.setSituacao(SituacaoSubprocesso.MAPA_AJUSTADO);
        repositorioSubprocesso.save(sp);
    }

    @Transactional
    public void importarAtividades(Long idSubprocessoDestino, Long idSubprocessoOrigem) {
        Subprocesso spDestino = repositorioSubprocesso.findById(idSubprocessoDestino)
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Subprocesso de destino não encontrado: " + idSubprocessoDestino));

        if (spDestino.getSituacao() != SituacaoSubprocesso.CADASTRO_EM_ANDAMENTO) {
            throw new IllegalStateException("Atividades só podem ser importadas para um subprocesso com cadastro em elaboração.");
        }

        Subprocesso spOrigem = repositorioSubprocesso.findById(idSubprocessoOrigem)
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Subprocesso de origem não encontrado: " + idSubprocessoOrigem));

        if (spOrigem.getMapa() == null || spDestino.getMapa() == null) {
            throw new IllegalStateException("Subprocesso de origem ou destino não possui mapa associado.");
        }

        List<Atividade> atividadesOrigem = atividadeRepo.findByMapaCodigo(spOrigem.getMapa().getCodigo());
        if (atividadesOrigem == null || atividadesOrigem.isEmpty()) {
            return; // Nada a importar
        }

        List<String> descricoesExistentes = atividadeRepo.findByMapaCodigo(spDestino.getMapa().getCodigo())
                .stream()
                .map(Atividade::getDescricao)
                .toList();

        for (Atividade atividadeOrigem : atividadesOrigem) {
            if (descricoesExistentes.contains(atividadeOrigem.getDescricao())) {
                continue; // Pula a importação se a atividade já existe
            }
            Atividade novaAtividade = new Atividade();
            novaAtividade.setDescricao(atividadeOrigem.getDescricao());
            novaAtividade.setMapa(spDestino.getMapa());
            Atividade atividadeSalva = atividadeRepo.save(novaAtividade);

            List<Conhecimento> conhecimentosOrigem = repositorioConhecimento.findByAtividadeCodigo(atividadeOrigem.getCodigo());
            if (conhecimentosOrigem != null) {
                for (Conhecimento conhecimentoOrigem : conhecimentosOrigem) {
                    Conhecimento novoConhecimento = new Conhecimento();
                    novoConhecimento.setDescricao(conhecimentoOrigem.getDescricao());
                    novoConhecimento.setAtividade(atividadeSalva);
                    repositorioConhecimento.save(novoConhecimento);
                }
            }
        }

        String descricaoMovimentacao = String.format("Importação de atividades do subprocesso #%d (Unidade: %s)",
                spOrigem.getCodigo(),
                spOrigem.getUnidade() != null ? spOrigem.getUnidade().getSigla() : "N/A");

        repositorioMovimentacao.save(new Movimentacao(spDestino, spDestino.getUnidade(), spDestino.getUnidade(), descricaoMovimentacao));

        log.info("Atividades importadas com sucesso do subprocesso {} para {}", idSubprocessoOrigem, idSubprocessoDestino);
    }
}