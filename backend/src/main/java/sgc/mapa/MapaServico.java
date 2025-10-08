package sgc.mapa;

import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.mapa.dto.MapaCompletoDto;
import sgc.mapa.dto.SalvarMapaRequest;

/**
 * Interface do serviço de negócio para gerenciar Mapas de Competências.
 * <p>
 * Provê operações de alto nível para manipular mapas completos,
 * incluindo competências e vínculos com atividades de forma agregada.
 */
public interface MapaServico {
    /**
     * Obtém um mapa completo com todas as competências e atividades vinculadas.
     * 
     * @param mapaId Código do mapa
     * @return DTO com o mapa completo aninhado
     * @throws ErroEntidadeNaoEncontrada se o mapa não existir
     */
    MapaCompletoDto obterMapaCompleto(Long mapaId);
    
    /**
     * Salva um mapa completo de forma atômica.
     * <p>
     * Operação transacional que:
     * - Atualiza observações do mapa
     * - Remove competências excluídas
     * - Cria novas competências
     * - Atualiza competências existentes
     * - Atualiza todos os vínculos com atividades
     * 
     * @param mapaId Código do mapa a ser atualizado
     * @param request Request com dados do mapa completo
     * @param usuarioTitulo Título do usuário que está salvando (para auditoria)
     * @return DTO com o mapa completo atualizado
     * @throws ErroEntidadeNaoEncontrada se o mapa não existir
     */
    MapaCompletoDto salvarMapaCompleto(Long mapaId, SalvarMapaRequest request, String usuarioTitulo);
    
    /**
     * Obtém o mapa de um subprocesso.
     * 
     * @param subprocessoId Código do subprocesso
     * @return DTO com o mapa completo do subprocesso
     * @throws ErroEntidadeNaoEncontrada se o subprocesso ou mapa não existir
     */
    MapaCompletoDto obterMapaSubprocesso(Long subprocessoId);
    
    /**
     * Salva o mapa de um subprocesso.
     * <p>
     * Se for a primeira vez que competências são criadas,
     * atualiza a situação do subprocesso para MAPA_CRIADO.
     * 
     * @param subprocessoId Código do subprocesso
     * @param request Request com dados do mapa completo
     * @param usuarioTitulo Título do usuário que está salvando (para auditoria)
     * @return DTO com o mapa completo atualizado
     * @throws ErroEntidadeNaoEncontrada se o subprocesso ou mapa não existir
     * @throws IllegalStateException se a situação do subprocesso não permitir a operação
     */
    MapaCompletoDto salvarMapaSubprocesso(Long subprocessoId, SalvarMapaRequest request, String usuarioTitulo);
    
    /**
     * Valida se o mapa está completo e pronto para ser disponibilizado.
     * <p>
     * Verifica:
     * - Todas competências têm pelo menos uma atividade vinculada
     * - Todas atividades estão vinculadas a pelo menos uma competência
     * 
     * @param mapaId Código do mapa
     * @throws IllegalStateException se o mapa não estiver válido
     */
    void validarMapaCompleto(Long mapaId);
}