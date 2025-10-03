package sgc.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.model.Atividade;
import sgc.model.Conhecimento;
import sgc.model.Mapa;
import sgc.model.Unidade;
import sgc.repository.AtividadeRepository;
import sgc.repository.ConhecimentoRepository;
import sgc.repository.MapaRepository;
import sgc.repository.UnidadeRepository;
import sgc.service.MapCopyService;

import java.util.*;

/**
 * Implementação simples do MapCopyService.
 * - Clona o Mapa (criando novo registro) e todas as Atividades e Conhecimentos associados.
 * - Mantém integridade mapeando ids originais para novos ids.
 * <p>
 * Observações:
 * - Para mapas grandes esta implementação pode ser melhorada com chunking/queries específicas.
 * - Não copia histórico (análises/homologações antigas) intencionalmente.
 */
@Service
@RequiredArgsConstructor
public class MapCopyServiceImpl implements MapCopyService {

    private final MapaRepository mapaRepository;
    private final AtividadeRepository atividadeRepository;
    private final ConhecimentoRepository conhecimentoRepository;
    private final UnidadeRepository unidadeRepository;

    @Override
    @Transactional
    public Mapa copyMapForUnit(Long sourceMapaId, Long targetUnidadeId) {
        Mapa source = mapaRepository.findById(sourceMapaId)
                .orElseThrow(() -> new IllegalArgumentException("Mapa fonte não encontrado: " + sourceMapaId));

        Unidade targetUnidade = unidadeRepository.findById(targetUnidadeId)
                .orElseThrow(() -> new IllegalArgumentException("Unidade alvo não encontrada: " + targetUnidadeId));

        // Criar novo mapa (snapshot). Não copiamos histórico de homologações/analises.
        Mapa novo = new Mapa();
        novo.setDataHoraDisponibilizado(source.getDataHoraDisponibilizado());
        novo.setObservacoesDisponibilizacao(source.getObservacoesDisponibilizacao());
        novo.setSugestoesApresentadas(source.getSugestoesApresentadas());
        // Não copiar dataHoraHomologado (tratamos como novo rascunho)
        novo.setDataHoraHomologado(null);

        Mapa salvoMapa = mapaRepository.save(novo);

        // Map original atividadeId -> nova Atividade
        Map<Long, Atividade> atividadeMap = new HashMap<>();

        // Carrega todas atividades e filtra pelas do mapa fonte.
        List<Atividade> atividadesFonte = atividadeRepository.findAll()
                .stream()
                .filter(a -> a.getMapa() != null && source.getCodigo().equals(a.getMapa().getCodigo()))
                .toList();

        for (Atividade aFonte : atividadesFonte) {
            Atividade aNovo = new Atividade();
            aNovo.setDescricao(aFonte.getDescricao());
            aNovo.setMapa(salvoMapa);
            Atividade aSalvo = atividadeRepository.save(aNovo);
            atividadeMap.put(aFonte.getCodigo(), aSalvo);
        }

        // Copiar conhecimentos associados às atividades originais (se existirem)
        List<Conhecimento> conhecimentosFonte = conhecimentoRepository.findAll()
                .stream()
                .filter(c -> c.getAtividade() != null && atividadeMap.containsKey(c.getAtividade().getCodigo()))
                .toList();

        for (Conhecimento cFonte : conhecimentosFonte) {
            Conhecimento cNovo = new Conhecimento();
            // vincula ao objeto Atividade recém-criado
            Atividade atividadeVinculada = atividadeMap.get(cFonte.getAtividade().getCodigo());
            cNovo.setAtividade(atividadeVinculada);
            cNovo.setDescricao(cFonte.getDescricao());
            conhecimentoRepository.save(cNovo);
        }

        // Observação: caso precise relacionar o mapa à unidade (por exemplo preencher UNIDADE_MAPA),
        // isso deve ser feito externamente (neste projeto a entidade UnidadeMapa é gerenciada em outro fluxo).
        return salvoMapa;
    }
}