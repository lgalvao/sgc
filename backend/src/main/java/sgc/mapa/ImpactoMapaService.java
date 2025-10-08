package sgc.mapa;

import sgc.mapa.dto.ImpactoMapaDto;

/**
 * Interface do serviço responsável por detectar impactos no mapa de competências
 * causados por alterações no cadastro de atividades durante processos de revisão.
 * <p>
 * CDU-12 - Verificar impactos no mapa de competências
 */
public interface ImpactoMapaService {
    /**
     * Verifica impactos no mapa de competências comparando o cadastro atual
     * do subprocesso com o mapa vigente da unidade.
     * <p>
     * Detecta:
     * - Atividades inseridas (estão no cadastro atual mas não no mapa vigente)
     * - Atividades removidas (estavam no mapa vigente mas não no cadastro atual)
     * - Atividades alteradas (mesmo código mas descrição diferente)
     * - Competências impactadas pelas mudanças
     * 
     * @param subprocessoId Código do subprocesso a verificar
     * @return ImpactoMapaDto com análise completa dos impactos
     */
    ImpactoMapaDto verificarImpactos(Long subprocessoId);
}