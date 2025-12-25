package sgc.subprocesso.internal.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import sgc.mapa.api.model.Mapa;
import sgc.mapa.api.model.MapaRepo;
import sgc.mapa.internal.service.CopiaMapaService;
import sgc.processo.internal.erros.ErroProcesso;
import sgc.processo.api.model.Processo;
import sgc.subprocesso.internal.model.Movimentacao;
import sgc.subprocesso.internal.model.Subprocesso;
import sgc.subprocesso.internal.model.SubprocessoMovimentacaoRepo;
import sgc.subprocesso.internal.model.SubprocessoRepo;
import sgc.unidade.api.model.TipoUnidade;
import sgc.unidade.api.model.Unidade;
import sgc.unidade.api.model.UnidadeMapa;
import sgc.unidade.api.model.UnidadeMapaRepo;

import static sgc.subprocesso.internal.model.SituacaoSubprocesso.*;

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
    private final UnidadeMapaRepo unidadeMapaRepo;

    /**
     * Cria subprocesso para processo de mapeamento.
     * Aplicável apenas a unidades OPERACIONAL ou INTEROPERACIONAL.
     */
    public void criarParaMapeamento(Processo processo, Unidade unidade) {
        if (TipoUnidade.OPERACIONAL != unidade.getTipo() && TipoUnidade.INTEROPERACIONAL != unidade.getTipo()) {
            return; // Ignora unidades não elegíveis
        }
        
        // 1. Criar subprocesso SEM mapa primeiro
        Subprocesso subprocesso = Subprocesso.builder()
                .processo(processo)
                .unidade(unidade)
                .mapa(null)
                .situacao(NAO_INICIADO)
                .dataLimiteEtapa1(processo.getDataLimite())
                .build();
        Subprocesso subprocessoSalvo = subprocessoRepo.save(subprocesso);
        
        // 2. Criar mapa COM referência ao subprocesso
        Mapa mapa = new Mapa();
        mapa.setSubprocessoCodigo(subprocessoSalvo.getCodigo());
        mapaRepo.save(mapa);
        
        // 4. Criar movimentação
        movimentacaoRepo.save(
                new Movimentacao(subprocessoSalvo, null, unidade, "Processo iniciado", null));
        
        log.debug("Subprocesso para mapeamento criado para unidade {}", unidade.getSigla());
    }

    /**
     * Cria subprocesso para processo de revisão.
     * Copia o mapa vigente da unidade.
     */
    public void criarParaRevisao(Processo processo, Unidade unidade) {
        log.debug("Criando subprocesso de revisão para unidade: {}", unidade.getCodigo());
        
        // Buscar mapa vigente da unidade
        UnidadeMapa unidadeMapa = unidadeMapaRepo.findById(unidade.getCodigo())
                .orElseThrow(() -> {
                    log.error("ERRO CRITICO: Unidade {} nao possui mapa vigente, mas passou pela validacao.", unidade.getCodigo());
                    return new ErroProcesso(
                        "Unidade %s não possui mapa vigente.".formatted(unidade.getSigla()));
                });

        Long codMapaVigente = unidadeMapa.getMapaVigente().getCodigo();
        log.debug("Mapa vigente da unidade {}: codigo={}", unidade.getSigla(), codMapaVigente);
        
        // 1. Criar subprocesso SEM mapa primeiro
        Subprocesso subprocesso = Subprocesso.builder()
                .processo(processo)
                .unidade(unidade)
                .mapa(null)
                .situacao(NAO_INICIADO)
                .dataLimiteEtapa1(processo.getDataLimite())
                .build();
        subprocessoRepo.save(subprocesso);
        log.debug("Subprocesso criado");
        
        // 2. Copiar mapa COM referência ao subprocesso
        log.debug("Iniciando copia do mapa vigente {} para unidade {}", codMapaVigente, unidade.getSigla());
        Mapa mapaCopiado = servicoDeCopiaDeMapa.copiarMapaParaUnidade(codMapaVigente, unidade.getCodigo());
        
        if (mapaCopiado == null) {
            log.error("ERRO CRITICO: Copia do mapa retornou null para unidade {}", unidade.getSigla());
            throw new ErroProcesso("Falha ao copiar mapa para unidade " + unidade.getSigla());
        }
        
        log.debug("Mapa copiado: codigo={}", mapaCopiado.getCodigo());
        mapaCopiado.setSubprocessoCodigo(subprocesso.getCodigo());
        mapaRepo.save(mapaCopiado);
        log.debug("Mapa salvo com associacao ao subprocesso");
        
        log.debug("Subprocesso associado ao mapa (local): mapaId={} unidade={}",
                mapaCopiado != null ? mapaCopiado.getCodigo() : "null", unidade.getSigla());

        // 4. Criar movimentação
        movimentacaoRepo.save(new Movimentacao(subprocesso, null, unidade, "Processo de revisão iniciado", null));
        log.info("Subprocesso para revisão criado para unidade {}", unidade.getSigla());
    }

    /**
     * Cria subprocesso para processo de diagnóstico.
     * Copia o mapa vigente e inicia com autoavaliação em andamento.
     */
    public void criarParaDiagnostico(Processo processo, Unidade unidade) {
        UnidadeMapa unidadeMapa = unidadeMapaRepo.findById(unidade.getCodigo())
                .orElseThrow(() -> new ErroProcesso(
                        "Unidade %s não possui mapa vigente para iniciar diagnóstico.".formatted(unidade.getSigla())));

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
        Mapa mapaCopiado = servicoDeCopiaDeMapa.copiarMapaParaUnidade(codMapaVigente, unidade.getCodigo());
        mapaCopiado.setSubprocessoCodigo(subprocessoSalvo.getCodigo());
        mapaRepo.save(mapaCopiado);
        
        // 4. Criar movimentação
        movimentacaoRepo.save(
                new Movimentacao(subprocessoSalvo, null, unidade, "Processo de diagnóstico iniciado", null));
        log.info("Subprocesso {} para diagnóstico criado para unidade {}", subprocessoSalvo.getCodigo(), unidade.getSigla());
    }
}
