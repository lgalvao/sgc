package sgc.subprocesso.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import sgc.mapa.model.Mapa;
import sgc.mapa.model.MapaRepo;
import sgc.mapa.service.CopiaMapaService;
import sgc.processo.erros.ErroProcesso;
import sgc.processo.model.Processo;
import sgc.subprocesso.model.Movimentacao;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoMovimentacaoRepo;
import sgc.subprocesso.model.SubprocessoRepo;
import sgc.unidade.model.TipoUnidade;
import sgc.unidade.model.Unidade;
import sgc.unidade.model.UnidadeMapa;

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
        mapa.setSubprocesso(subprocessoSalvo);
        Mapa mapaSalvo = mapaRepo.save(mapa);
        
        // 3. Atualizar subprocesso com o mapa (apenas em memória, save é redundante se for mappedBy)
        subprocessoSalvo.setMapa(mapaSalvo);
        
        // 4. Criar movimentação
        movimentacaoRepo.save(
                new Movimentacao(subprocessoSalvo, null, unidade, "Processo iniciado", null));
        
        log.debug("Subprocesso for mapping created for unit {}", unidade.getSigla());
    }

    /**
     * Cria subprocesso para processo de revisão.
     * Copia o mapa vigente da unidade.
     */
    public void criarParaRevisao(Processo processo, Unidade unidade, UnidadeMapa unidadeMapa) {
        log.debug("Criando subprocesso de revisão para unidade: {}", unidade.getCodigo());
        
        if (unidadeMapa == null) {
            log.error("ERRO CRITICO: Unidade {} nao possui mapa vigente.", unidade.getCodigo());
            throw new ErroProcesso("Unidade %s não possui mapa vigente.".formatted(unidade.getSigla()));
        }

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
        Subprocesso subprocessoSalvo = subprocessoRepo.save(subprocesso);
        log.debug("Subprocesso criado");
        
        // 2. Copiar mapa COM referência ao subprocesso
        log.debug("Iniciando copia do mapa vigente {} para unidade {}", codMapaVigente, unidade.getSigla());
        Mapa mapaCopiado = servicoDeCopiaDeMapa.copiarMapaParaUnidade(codMapaVigente, unidade.getCodigo());
        
        if (mapaCopiado == null) {
            log.error("ERRO CRITICO: Copia do mapa retornou null para unidade {}", unidade.getSigla());
            throw new ErroProcesso("Falha ao copiar mapa para unidade " + unidade.getSigla());
        }
        
        log.debug("Mapa copiado: codigo={}", mapaCopiado.getCodigo());
        mapaCopiado.setSubprocesso(subprocessoSalvo);
        Mapa mapaSalvo = mapaRepo.save(mapaCopiado);
        log.debug("Mapa salvo com associacao ao subprocesso");
        
        // 3. Atualizar subprocesso local com o mapa
        subprocessoSalvo.setMapa(mapaSalvo);
        
        log.debug("Subprocesso associado ao mapa (local): mapaId={} unidade={}",
                mapaSalvo != null ? mapaSalvo.getCodigo() : "null", unidade.getSigla());

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
        Mapa mapaCopiado = servicoDeCopiaDeMapa.copiarMapaParaUnidade(codMapaVigente, unidade.getCodigo());
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
