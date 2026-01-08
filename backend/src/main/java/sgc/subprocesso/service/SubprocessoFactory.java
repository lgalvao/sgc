package sgc.subprocesso.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import sgc.mapa.model.Mapa;
import sgc.mapa.model.MapaRepo;
import sgc.mapa.service.CopiaMapaService;
import sgc.organizacao.model.TipoUnidade;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.UnidadeMapa;
import sgc.processo.erros.ErroProcesso;
import sgc.processo.model.Processo;
import sgc.subprocesso.model.Movimentacao;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoMovimentacaoRepo;
import sgc.subprocesso.model.SubprocessoRepo;

import java.util.List;

import static sgc.subprocesso.model.SituacaoSubprocesso.DIAGNOSTICO_AUTOAVALIACAO_EM_ANDAMENTO;
import static sgc.subprocesso.model.SituacaoSubprocesso.NAO_INICIADO;

/**
 * Factory responsável pela criação de subprocessos para diferentes tipos de processo.
 * Extraído do ProcessoService para reduzir complexidade e melhorar coesão.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SubprocessoFactory {
    
    private final SubprocessoRepo subprocessoRepo;
    private final MapaRepo mapaRepo;
    private final SubprocessoMovimentacaoRepo movimentacaoRepo;
    private final CopiaMapaService servicoDeCopiaDeMapa;

    /**
     * Cria subprocessos para processo de mapeamento em lote.
     * Aplicável apenas a unidades OPERACIONAL ou INTEROPERACIONAL.
     */
    public void criarParaMapeamento(Processo processo, java.util.Collection<Unidade> unidades) {
        List<Unidade> unidadesElegiveis = unidades.stream()
                .filter(u -> TipoUnidade.OPERACIONAL == u.getTipo() || TipoUnidade.INTEROPERACIONAL == u.getTipo())
                .toList();
        
        if (unidadesElegiveis.isEmpty()) {
            return;
        }

        // 1. Criar todos os subprocessos SEM mapa
        List<Subprocesso> subprocessos = unidadesElegiveis.stream()
                .map(unidade -> Subprocesso.builder()
                        .processo(processo)
                        .unidade(unidade)
                        .mapa(null)
                        .situacao(NAO_INICIADO)
                        .dataLimiteEtapa1(processo.getDataLimite())
                        .build())
                .toList();
        List<Subprocesso> subprocessosSalvos = subprocessoRepo.saveAll(subprocessos);
        
        // 2. Criar todos os mapas COM referência aos subprocessos
        List<Mapa> mapas = subprocessosSalvos.stream()
                .map(sp -> {
                    Mapa mapa = new Mapa();
                    mapa.setSubprocesso(sp);
                    return mapa;
                })
                .toList();
        List<Mapa> mapasSalvos = mapaRepo.saveAll(mapas);
        
        // 3. Atualizar subprocessos com os mapas (em memória)
        for (int i = 0; i < subprocessosSalvos.size(); i++) {
            subprocessosSalvos.get(i).setMapa(mapasSalvos.get(i));
        }
        
        // 4. Criar todas as movimentações em lote
        List<Movimentacao> movimentacoes = new java.util.ArrayList<>();
        for (Subprocesso sp : subprocessosSalvos) {
            movimentacoes.add(new Movimentacao(sp, null, sp.getUnidade(), "Processo iniciado", null));
        }
        movimentacaoRepo.saveAll(movimentacoes);
    }

    /**
     * Cria subprocesso para processo de revisão.
     * Copia o mapa vigente da unidade.
     */
    public void criarParaRevisao(Processo processo, Unidade unidade, UnidadeMapa unidadeMapa) {
        if (unidadeMapa == null) {
            log.error("ERRO CRITICO: Unidade {} nao possui mapa vigente.", unidade.getCodigo());
            throw new ErroProcesso("Unidade %s não possui mapa vigente.".formatted(unidade.getSigla()));
        }

        Long codMapaVigente = unidadeMapa.getMapaVigente().getCodigo();
        
        // 1. Criar subprocesso SEM mapa primeiro
        Subprocesso subprocesso = Subprocesso.builder()
                .processo(processo)
                .unidade(unidade)
                .mapa(null)
                .situacao(NAO_INICIADO)
                .dataLimiteEtapa1(processo.getDataLimite())
                .build();
        Subprocesso subprocessoSalvo = subprocessoRepo.save(subprocesso);
        
        // 2. Copiar mapa COM referência ao subprocesso
        Mapa mapaCopiado = servicoDeCopiaDeMapa.copiarMapaParaUnidade(codMapaVigente);
        
        mapaCopiado.setSubprocesso(subprocessoSalvo);
        Mapa mapaSalvo = mapaRepo.save(mapaCopiado);
        
        // 3. Atualizar subprocesso local com o mapa
        subprocessoSalvo.setMapa(mapaSalvo);

        // 4. Criar movimentação
        movimentacaoRepo.save(new Movimentacao(subprocessoSalvo, null, unidade, "Processo de revisão iniciado", null));
        log.info("Subprocesso para revisão criado para unidade {}", unidade.getSigla());
    }

    /**
     * Cria subprocesso para processo de diagnóstico.
     * Copia o mapa vigente e inicia com autoavaliação em andamento.
     */
    public void criarParaDiagnostico(Processo processo, Unidade unidade, UnidadeMapa unidadeMapa) {
        if (unidadeMapa == null) {
            throw new ErroProcesso(
                    "Unidade %s não possui mapa vigente para iniciar diagnóstico.".formatted(unidade.getSigla()));
        }

        Long codMapaVigente = unidadeMapa.getMapaVigente().getCodigo();
        
        // 1. Criar subprocesso SEM mapa primeiro
        Subprocesso subprocesso = Subprocesso.builder()
                .processo(processo)
                .unidade(unidade)
                .mapa(null)
                .situacao(DIAGNOSTICO_AUTOAVALIACAO_EM_ANDAMENTO)
                .dataLimiteEtapa1(processo.getDataLimite())
                .build();
        Subprocesso subprocessoSalvo = subprocessoRepo.save(subprocesso);
        
        // 2. Copiar mapa COM referência ao subprocesso
        Mapa mapaCopiado = servicoDeCopiaDeMapa.copiarMapaParaUnidade(codMapaVigente);
        mapaCopiado.setSubprocesso(subprocessoSalvo);
        Mapa mapaSalvo = mapaRepo.save(mapaCopiado);
        
        // 3. Atualizar subprocesso com o mapa
        subprocessoSalvo.setMapa(mapaSalvo);

        // 4. Criar movimentação
        movimentacaoRepo.save(
                new Movimentacao(subprocessoSalvo, null, unidade, "Processo de diagnóstico iniciado", null));
        log.info("Subprocesso {} para diagnóstico criado para unidade {}", subprocessoSalvo.getCodigo(), unidade.getSigla());
    }
}
