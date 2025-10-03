package sgc.service;

import sgc.model.Mapa;

/**
 * Serviço responsável por copiar um mapa vigente para outra unidade.
 * A implementação deve clonar o mapa e suas atividades/conhecimentos mantendo integridade.
 */
public interface MapCopyService {

    /**
     * Copia o mapa identificado por sourceMapaId para a unidade targetUnidadeId.
     * Retorna o Mapa recém-criado (com novo id).
     *
     * @param sourceMapaId id do mapa vigente a ser copiado
     * @param targetUnidadeId id da unidade que receberá o mapa copiado
     * @return novo Mapa salvo
     * @throws IllegalArgumentException se o mapa fonte ou unidade alvo não existirem
     */
    Mapa copyMapForUnit(Long sourceMapaId, Long targetUnidadeId);
}