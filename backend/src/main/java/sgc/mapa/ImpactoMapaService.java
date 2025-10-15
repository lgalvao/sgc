package sgc.mapa;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.atividade.modelo.Atividade;
import sgc.atividade.modelo.AtividadeRepo;
import sgc.comum.erros.ErroDominioAccessoNegado;
import sgc.comum.erros.ErroDominioNaoEncontrado;
import sgc.mapa.dto.AtividadeImpactadaDto;
import sgc.mapa.dto.CompetenciaImpactadaDto;
import sgc.mapa.dto.ImpactoMapaDto;
import sgc.mapa.modelo.Mapa;
import sgc.mapa.modelo.MapaRepo;
import sgc.sgrh.Usuario;
import sgc.subprocesso.SituacaoSubprocesso;
import sgc.subprocesso.modelo.Subprocesso;
import sgc.subprocesso.modelo.SubprocessoRepo;

import java.util.List;
import java.util.Optional;

import static sgc.subprocesso.SituacaoSubprocesso.*;

/**
 * Interface do serviço responsável por detectar impactos no mapa de competências
 * causados por alterações no cadastro de atividades durante processos de revisão.
 * <p>
 * CDU-12 - Verificar impactos no mapa de competências
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ImpactoMapaService {
    private final SubprocessoRepo repositorioSubprocesso;
    private final MapaRepo mapaRepo;
    private final AtividadeRepo atividadeRepo;
    private final ImpactoAtividadeService impactoAtividadeService;
    private final ImpactoCompetenciaService impactoCompetenciaService;

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
     * @param idSubprocesso Código do subprocesso a verificar
     * @param usuario       O usuário autenticado que está realizando a operação.
     * @return ImpactoMapaDto com análise completa dos impactos
     */
    @Transactional(readOnly = true)
    public ImpactoMapaDto verificarImpactos(Long idSubprocesso, Usuario usuario) {
        log.info("Verificando impactos no mapa: subprocesso={}", idSubprocesso);

        Subprocesso subprocesso = repositorioSubprocesso.findById(idSubprocesso)
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Subprocesso", idSubprocesso));

        verificarAcesso(usuario, subprocesso);

        Optional<Mapa> mapaVigenteOpt = mapaRepo
                .findMapaVigenteByUnidade(subprocesso.getUnidade().getCodigo());

        if (mapaVigenteOpt.isEmpty()) {
            log.info("Unidade sem mapa vigente, não há impactos a analisar");
            // TODO muito ruim essa instancianção cheio de valores 'vazios'
            return new ImpactoMapaDto(
                    false, 0, 0, 0, 0,
                    List.of(), List.of(), List.of(), List.of());
        }

        Mapa mapaVigente = mapaVigenteOpt.get();
        Mapa mapaSubprocesso = mapaRepo
                .findBySubprocessoCodigo(idSubprocesso)
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Mapa não encontrado para subprocesso", idSubprocesso));

        List<Atividade> atividadesAtuais = atividadeRepo.findByMapaCodigo(mapaSubprocesso.getCodigo());
        List<Atividade> atividadesVigentes = impactoAtividadeService.obterAtividadesDoMapa(mapaVigente);
        List<AtividadeImpactadaDto> inseridas = impactoAtividadeService.detectarAtividadesInseridas(atividadesAtuais, atividadesVigentes);
        List<AtividadeImpactadaDto> removidas = impactoAtividadeService.detectarAtividadesRemovidas(atividadesAtuais, atividadesVigentes, mapaVigente);
        List<AtividadeImpactadaDto> alteradas = impactoAtividadeService.detectarAtividadesAlteradas(atividadesAtuais, atividadesVigentes, mapaVigente);
        List<CompetenciaImpactadaDto> competenciasImpactadas = impactoCompetenciaService.identificarCompetenciasImpactadas(mapaVigente, removidas, alteradas);

        boolean temImpactos = !inseridas.isEmpty() || !removidas.isEmpty() || !alteradas.isEmpty();

        log.info("Análise de impactos concluída: tem={}, inseridas={}, removidas={}, alteradas={}",
                temImpactos, inseridas.size(), removidas.size(), alteradas.size());

        // TODO rever essa instanciação com muitos parâmetros. Está frágil!
        return new ImpactoMapaDto(
                temImpactos,
                inseridas.size(),
                removidas.size(),
                alteradas.size(),
                competenciasImpactadas.size(),
                inseridas,
                removidas,
                alteradas,
                competenciasImpactadas);
    }

    //TODO rever esse método para evitar tantos strings fixos e linhas longas demais
    private void verificarAcesso(Usuario usuario, Subprocesso subprocesso) {
        SituacaoSubprocesso sitSubprocesso = subprocesso.getSituacao();

        if (hasRole(usuario, "CHEFE")) {
            if (sitSubprocesso != REVISAO_CADASTRO_EM_ANDAMENTO) {
                throw new ErroDominioAccessoNegado("O chefe da unidade só pode verificar os impactos com o subprocesso na situação 'Revisão do cadastro em andamento'.");
            }
        } else if (hasRole(usuario, "GESTOR")) {
            if (sitSubprocesso != REVISAO_CADASTRO_DISPONIBILIZADA) {
                throw new ErroDominioAccessoNegado("O gestor só pode verificar os impactos com o subprocesso na situação 'Revisão do cadastro disponibilizada'.");
            }
        } else if (hasRole(usuario, "ADMIN")) {
            if (sitSubprocesso != REVISAO_CADASTRO_HOMOLOGADA && sitSubprocesso != SituacaoSubprocesso.MAPA_AJUSTADO) {
                throw new ErroDominioAccessoNegado("O administrador só pode verificar os impactos com o subprocesso na situação 'Revisão do cadastro homologada' ou 'Mapa Ajustado'.");
            }
        }
    }

    private boolean hasRole(Usuario usuario, String role) {
        return usuario.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_%s".formatted(role)));
    }

    private record CompetenciaImpactoAcumulador(Long codigo, String descricao) {
    }
}