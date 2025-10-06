package sgc.mapa;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.atividade.Atividade;
import sgc.atividade.AtividadeRepository;
import sgc.conhecimento.Conhecimento;
import sgc.conhecimento.ConhecimentoRepository;
import sgc.unidade.Unidade;
import sgc.unidade.UnidadeRepository;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementação do CopiaMapaService com melhorias para garantir cópia completa de atividades e conhecimentos.
 * <p>
 * Melhorias realizadas:
 * - Usa consultas específicas para buscar atividades do mapa fonte em vez de carregar tudo e filtrar.
 * - Para cada atividade fonte usa o repositório de conhecimentos por atividade (findByAtividadeCodigo) para garantir
 * que todos os conhecimentos relacionados sejam copiados mesmo em bases grandes.
 * - Mantém um mapa de id original -> entidade nova para garantir associações corretas.
 * <p>
 * Observações:
 * - Ainda não copia histórico de análises/homologações intencionalmente.
 * - Se for necessário copiar tabelas associativas adicionais (competências, anexos, etc.) adicionar aqui e criar
 * mapeamentos equivalentes.
 */
@Service
@RequiredArgsConstructor
public class CopiaMapaServiceImpl implements CopiaMapaService {
    private final MapaRepository mapaRepository;
    private final AtividadeRepository atividadeRepository;
    private final ConhecimentoRepository conhecimentoRepository;
    private final UnidadeRepository unidadeRepository;

    @Override
    @Transactional
    public Mapa copyMapForUnit(Long sourceMapaId, Long targetUnidadeId) {
        Mapa source = mapaRepository.findById(sourceMapaId)
                .orElseThrow(() -> new IllegalArgumentException("Mapa fonte não encontrado: %d".formatted(sourceMapaId)));

        Unidade targetUnidade = unidadeRepository.findById(targetUnidadeId)
                .orElseThrow(() -> new IllegalArgumentException("Unidade de destino não encontrada: %d".formatted(targetUnidadeId)));

        Mapa novo = new Mapa();
        novo.setDataHoraDisponibilizado(source.getDataHoraDisponibilizado());
        novo.setObservacoesDisponibilizacao(source.getObservacoesDisponibilizacao());
        novo.setSugestoesApresentadas(source.getSugestoesApresentadas());
        novo.setDataHoraHomologado(null);
        novo.setUnidade(targetUnidade); // Associar o novo mapa à unidade de destino

        Mapa salvoMapa = mapaRepository.save(novo);
        Map<Long, Atividade> atividadeMap = new HashMap<>();

        List<Atividade> atividadesFonte = atividadeRepository.findByMapaCodigo(source.getCodigo());
        if (atividadesFonte == null) {
            atividadesFonte = Collections.emptyList();
        }

        for (Atividade aFonte : atividadesFonte) {
            Atividade aNovo = new Atividade();
            aNovo.setDescricao(aFonte.getDescricao());
            aNovo.setMapa(salvoMapa);
            Atividade aSalvo = atividadeRepository.save(aNovo);
            atividadeMap.put(aFonte.getCodigo(), aSalvo);
        }

        for (Atividade aFonte : atividadesFonte) {
            Long atividadeOrigId = aFonte.getCodigo();
            List<Conhecimento> ksFonte;
            try {
                ksFonte = conhecimentoRepository.findByAtividadeCodigo(atividadeOrigId);
            } catch (Exception e) {
                ksFonte = conhecimentoRepository.findAll()
                        .stream()
                        .filter(c -> c.getAtividade() != null && atividadeOrigId.equals(c.getAtividade().getCodigo()))
                        .toList();
            }

            if (ksFonte == null || ksFonte.isEmpty()) continue;

            Atividade atividadeNova = atividadeMap.get(atividadeOrigId);
            if (atividadeNova == null) continue; // proteção caso atividade não tenha sido copiada

            for (Conhecimento cFonte : ksFonte) {
                Conhecimento cNovo = new Conhecimento();
                cNovo.setAtividade(atividadeNova);
                cNovo.setDescricao(cFonte.getDescricao());
                conhecimentoRepository.save(cNovo);
            }
        }
        return salvoMapa;
    }
}