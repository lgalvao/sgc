package sgc.mapa.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.comum.erros.ErroAccessoNegado;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.mapa.dto.AtividadeImpactadaDto;
import sgc.mapa.dto.CompetenciaImpactadaDto;
import sgc.mapa.dto.ImpactoMapaDto;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.Competencia;
import sgc.mapa.model.CompetenciaRepo;
import sgc.mapa.model.Mapa;
import sgc.mapa.model.MapaRepo;
import sgc.organizacao.model.Usuario;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.SubprocessoService;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static sgc.subprocesso.model.SituacaoSubprocesso.*;

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
    private static final String MSG_ERRO_CHEFE = """
            O chefe da unidade só pode verificar os impactos com o subprocesso na situação\
             'Revisão do cadastro em andamento'.""";
    private static final String MSG_ERRO_GESTOR = """
            O gestor só pode verificar os impactos com o subprocesso na situação 'Revisão do\
             cadastro disponibilizada'.""";
    private static final String MSG_ERRO_ADMIN = """
            O administrador só pode verificar os impactos com o subprocesso na situação 'Revisão\
             do cadastro disponibilizada', 'Revisão do cadastro homologada' ou 'Mapa\
             Ajustado'.""";

    private final SubprocessoService subprocessoService;
    private final MapaRepo mapaRepo;
    private final CompetenciaRepo competenciaRepo;
    private final AtividadeService atividadeService;

    // Services refatorados
    private final DetectorAtividadesService detectorAtividades;
    private final AnalisadorCompetenciasService analisadorCompetencias;
    private final MapaAcessoService mapaAcessoService;

    /**
     * Realiza a verificação de impactos no mapa de competências, comparando o mapa em revisão de um
     * subprocesso com o mapa vigente da unidade.
     *
     * <p>Este método implementa a lógica do CDU-12. Ele analisa as diferenças entre os dois mapas,
     * identificando atividades inseridas, removidas ou alteradas, e as competências que são
     * afetadas por essas mudanças.
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
        log.debug("Verificando impactos no mapa: subprocesso={}", codSubprocesso);

        Subprocesso subprocesso = subprocessoService.buscarSubprocesso(codSubprocesso);

        mapaAcessoService.verificarAcessoImpacto(usuario, subprocesso);

        Optional<Mapa> mapaVigenteOpt = mapaRepo.findMapaVigenteByUnidade(subprocesso.getUnidade().getCodigo());

        if (mapaVigenteOpt.isEmpty()) {
            log.info("Unidade sem mapa vigente, não há impactos a analisar");
            return ImpactoMapaDto.semImpacto();
        }

        Mapa mapaVigente = mapaVigenteOpt.get();
        Mapa mapaSubprocesso = mapaRepo.findBySubprocessoCodigo(codSubprocesso)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada(
                        "Mapa não encontrado para subprocesso", codSubprocesso)
                );

        List<Atividade> atividadesAtuais = obterAtividadesDoMapa(mapaSubprocesso);
        List<Atividade> atividadesVigentes = obterAtividadesDoMapa(mapaVigente);

        // ⚡ Otimização: Carregar todas as competências e construir o mapa em memória
        List<Competencia> competenciasDoMapa = competenciaRepo.findByMapaCodigo(mapaVigente.getCodigo());

        // Delegar análise para services especializados
        Map<Long, List<Competencia>> atividadeIdToCompetencias =
                analisadorCompetencias.construirMapaAtividadeCompetencias(competenciasDoMapa);

        List<AtividadeImpactadaDto> inseridas =
                detectorAtividades.detectarInseridas(atividadesAtuais, atividadesVigentes);

        List<AtividadeImpactadaDto> removidas =
                detectorAtividades.detectarRemovidas(atividadesAtuais, atividadesVigentes, atividadeIdToCompetencias);

        List<AtividadeImpactadaDto> alteradas =
                detectorAtividades.detectarAlteradas(atividadesAtuais, atividadesVigentes, atividadeIdToCompetencias);

        List<CompetenciaImpactadaDto> competenciasImpactadas =
                analisadorCompetencias.identificarCompetenciasImpactadas(
                        competenciasDoMapa, removidas, alteradas, atividadesVigentes);

        ImpactoMapaDto impactos =
                ImpactoMapaDto.comImpactos(inseridas, removidas, alteradas, competenciasImpactadas);

        log.info(
                "Análise de impactos concluída: tem={}, inseridas={} removidas={}, alteradas={}",
                impactos.isTemImpactos(),
                impactos.getTotalAtividadesInseridas(),
                impactos.getTotalAtividadesRemovidas(),
                impactos.getTotalAtividadesAlteradas());

        return impactos;
    }

    /**
     * Obtém todas as atividades associadas a um mapa, com seus conhecimentos.
     */
    private List<Atividade> obterAtividadesDoMapa(Mapa mapa) {
        log.debug("Buscando atividades e conhecimentos para o mapa {}", mapa.getCodigo());
        return atividadeService.buscarPorMapaCodigoComConhecimentos(mapa.getCodigo());
    }
}
