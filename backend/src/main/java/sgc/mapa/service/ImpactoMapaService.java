package sgc.mapa.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.comum.erros.ErroAccessoNegado;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.mapa.dto.AtividadeImpactadaDto;
import sgc.mapa.dto.CompetenciaImpactadaDto;
import sgc.mapa.dto.ImpactoMapaDto;
import sgc.mapa.model.*;
import sgc.organizacao.model.Usuario;
import sgc.seguranca.acesso.AccessControlService;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.SubprocessoFacade;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static sgc.seguranca.acesso.Acao.VERIFICAR_IMPACTOS;

/**
 * Serviço responsável por detectar impactos no mapa de competências causados por alterações no
 * cadastro de atividades durante processos de revisão.
 *
 * <p>CDU-12 - Verificar impactos no mapa de competências
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ImpactoMapaService {
    private final SubprocessoFacade subprocessoFacade;
    private final MapaRepo mapaRepo;
    private final CompetenciaRepo competenciaRepo;
    private final AtividadeService atividadeService;

    // Serviços de detecção de mudanças e impactos
    private final DetectorMudancasAtividadeService detectorMudancasAtividade;
    private final DetectorImpactoCompetenciaService detectorImpactoCompetencia;
    private final AccessControlService accessControlService;

    /**
     * Realiza a verificação de impactos no mapa de competências, comparando o mapa em revisão de um
     * subprocesso com o mapa vigente da unidade.
     *
     * <p>Analisa as diferenças entre os dois mapas, identificando atividades inseridas,
     * removidas ou alteradas, e as competências que são afetadas por essas mudanças.
     *
     * <p>O acesso a esta funcionalidade é restrito por perfil e pela situação atual do subprocesso
     * para garantir que a análise de impacto seja feita no momento correto do fluxo de trabalho.
     *
     * @param codSubprocesso O código do subprocesso cujo mapa será analisado.
     * @param usuario        O usuário autenticado que realiza a operação.
     * @return Um {@link ImpactoMapaDto} que encapsula todos os impactos encontrados. Retorna um DTO
     * sem impactos se a unidade não possuir um mapa vigente.
     * @throws ErroEntidadeNaoEncontrada se o subprocesso ou seu mapa não forem encontrados.
     * @throws ErroAccessoNegado         se o usuário não tiver permissão para executar a operação na
     *                                   situação atual do subprocesso.
     */
    @Transactional(readOnly = true)
    public ImpactoMapaDto verificarImpactos(Long codSubprocesso, Usuario usuario) {
        Subprocesso subprocesso = subprocessoFacade.buscarSubprocesso(codSubprocesso);
        
        // Verificação centralizada de acesso
        accessControlService.verificarPermissao(usuario, VERIFICAR_IMPACTOS, subprocesso);

        Optional<Mapa> mapaVigenteOpt = mapaRepo.findMapaVigenteByUnidade(subprocesso.getUnidade().getCodigo());
        if (mapaVigenteOpt.isEmpty()) {
            log.info("Unidade sem mapa vigente, não há impactos a analisar");
            return ImpactoMapaDto.semImpacto();
        }

        Mapa mapaVigente = mapaVigenteOpt.get();
        Mapa mapaSubprocesso = mapaRepo.findBySubprocessoCodigo(codSubprocesso).orElseThrow(
                () -> new ErroEntidadeNaoEncontrada("Mapa não encontrado para subprocesso", codSubprocesso)
        );

        List<Atividade> atividadesAtuais = obterAtividadesDoMapa(mapaSubprocesso);
        List<Atividade> atividadesVigentes = obterAtividadesDoMapa(mapaVigente);

        List<Competencia> competenciasMapa = competenciaRepo.findByMapaCodigo(mapaVigente.getCodigo());

        Map<Long, List<Competencia>> atividadeIdToCompetencias =
                detectorImpactoCompetencia.construirMapaAtividadeCompetencias(competenciasMapa);

        // Otimização: Construir mapas auxiliares uma única vez para evitar
        // re-construção dentro de cada método do detector
        Map<String, Atividade> mapaVigentes = detectorMudancasAtividade.atividadesPorDescricao(atividadesVigentes);
        Map<String, Atividade> mapaAtuais = detectorMudancasAtividade.atividadesPorDescricao(atividadesAtuais);

        List<AtividadeImpactadaDto> inseridas = detectorMudancasAtividade.detectarInseridas(
                atividadesAtuais, mapaVigentes.keySet()
        );

        List<AtividadeImpactadaDto> removidas = detectorMudancasAtividade.detectarRemovidas(
                mapaAtuais, atividadesVigentes, atividadeIdToCompetencias
        );

        List<AtividadeImpactadaDto> alteradas = detectorMudancasAtividade.detectarAlteradas(
                atividadesAtuais, mapaVigentes, atividadeIdToCompetencias
        );

        List<CompetenciaImpactadaDto> competenciasImpactadas = detectorImpactoCompetencia.competenciasImpactadas(
                competenciasMapa, removidas, alteradas, atividadesVigentes
        );

        return ImpactoMapaDto.comImpactos(inseridas, removidas, alteradas, competenciasImpactadas);
    }

    /**
     * Obtém todas as atividades associadas a um mapa, com seus conhecimentos.
     */
    private List<Atividade> obterAtividadesDoMapa(Mapa mapa) {
        return atividadeService.buscarPorMapaCodigoComConhecimentos(mapa.getCodigo());
    }
}
