package sgc.mapa.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.atividade.modelo.Atividade;
import sgc.atividade.modelo.AtividadeRepo;
import sgc.comum.erros.ErroAccessoNegado;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.mapa.dto.AtividadeImpactadaDto;
import sgc.mapa.dto.CompetenciaImpactadaDto;
import sgc.mapa.dto.ImpactoMapaDto;
import sgc.mapa.modelo.Mapa;
import sgc.mapa.modelo.MapaRepo;
import sgc.sgrh.modelo.Usuario;
import sgc.subprocesso.modelo.SituacaoSubprocesso;
import sgc.subprocesso.modelo.Subprocesso;
import sgc.subprocesso.modelo.SubprocessoRepo;

import java.util.List;
import java.util.Optional;

import static sgc.subprocesso.modelo.SituacaoSubprocesso.*;

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
     * Realiza a verificação de impactos no mapa de competências, comparando o mapa
     * em revisão de um subprocesso com o mapa vigente da unidade.
     * <p>
     * Este método implementa a lógica do CDU-12. Ele analisa as diferenças entre
     * os dois mapas, identificando atividades inseridas, removidas ou alteradas,
     * e as competências que são afetadas por essas mudanças.
     * <p>
     * O acesso a esta funcionalidade é restrito por perfil e pela situação atual
     * do subprocesso para garantir que a análise de impacto seja feita no momento
     * correto do fluxo de trabalho.
     *
     * @param codSubprocesso O código do subprocesso cujo mapa será analisado.
     * @param usuario       O usuário autenticado que realiza a operação.
     * @return Um {@link ImpactoMapaDto} que encapsula todos os impactos encontrados.
     *         Retorna um DTO sem impactos se a unidade não possuir um mapa vigente.
     * @throws ErroEntidadeNaoEncontrada se o subprocesso ou seu mapa não forem encontrados.
     * @throws ErroAccessoNegado se o usuário não tiver permissão para executar
     *                                  a operação na situação atual do subprocesso.
     */
    @Transactional(readOnly = true)
    public ImpactoMapaDto verificarImpactos(Long codSubprocesso, Usuario usuario) {
        log.info("Verificando impactos no mapa: subprocesso={}", codSubprocesso);

        Subprocesso subprocesso = repositorioSubprocesso.findById(codSubprocesso)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Subprocesso", codSubprocesso));

        verificarAcesso(usuario, subprocesso);

        Optional<Mapa> mapaVigenteOpt = mapaRepo
                .findMapaVigenteByUnidade(subprocesso.getUnidade().getCodigo());

        if (mapaVigenteOpt.isEmpty()) {
            log.info("Unidade sem mapa vigente, não há impactos a analisar");
            return ImpactoMapaDto.semImpacto();
        }

        Mapa mapaVigente = mapaVigenteOpt.get();
        Mapa mapaSubprocesso = mapaRepo
                .findBySubprocessoCodigo(codSubprocesso)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Mapa não encontrado para subprocesso", codSubprocesso));

        log.info("ImpactoMapaService - Mapa Vigente Código: {}", mapaVigente.getCodigo());
        log.info("ImpactoMapaService - Mapa Subprocesso Código: {}", mapaSubprocesso.getCodigo());

        List<Atividade> atividadesAtuais = impactoAtividadeService.obterAtividadesDoMapa(mapaSubprocesso);
        List<Atividade> atividadesVigentes = impactoAtividadeService.obterAtividadesDoMapa(mapaVigente);

        atividadesAtuais.forEach(a -> log.info("Atividade Atual: {} - {}", a.getCodigo(), a.getDescricao()));
        atividadesVigentes.forEach(a -> log.info("Atividade Vigente: {} - {}", a.getCodigo(), a.getDescricao()));

        log.info("ImpactoMapaService - Atividades Atuais (mapaSubprocesso) tamanho: {}", atividadesAtuais.size());
        log.info("ImpactoMapaService - Atividades Vigentes (mapaVigente) tamanho: {}", atividadesVigentes.size());

        List<AtividadeImpactadaDto> inseridas = impactoAtividadeService.detectarAtividadesInseridas(atividadesAtuais, atividadesVigentes);
        List<AtividadeImpactadaDto> removidas = impactoAtividadeService.detectarAtividadesRemovidas(atividadesAtuais, atividadesVigentes, mapaVigente);
        List<AtividadeImpactadaDto> alteradas = impactoAtividadeService.detectarAtividadesAlteradas(atividadesAtuais, atividadesVigentes, mapaVigente);
        List<CompetenciaImpactadaDto> competenciasImpactadas = impactoCompetenciaService.identificarCompetenciasImpactadas(mapaVigente, removidas, alteradas);

        ImpactoMapaDto impactos = ImpactoMapaDto.comImpactos(inseridas, removidas, alteradas, competenciasImpactadas);

        log.info("ImpactoMapaService - Análise de impactos concluída: tem={}, inseridas={}, removidas={}, alteradas={}",
                impactos.temImpactos(), impactos.totalAtividadesInseridas(), impactos.totalAtividadesRemovidas(), impactos.totalAtividadesAlteradas());
        return impactos;
    }

    private static final String MSG_ERRO_CHEFE = "O chefe da unidade só pode verificar os impactos com o subprocesso na situação 'Revisão do cadastro em andamento'.";
    private static final String MSG_ERRO_GESTOR = "O gestor só pode verificar os impactos com o subprocesso na situação 'Revisão do cadastro disponibilizada'.";
    private static final String MSG_ERRO_ADMIN = "O administrador só pode verificar os impactos com o subprocesso na situação 'Revisão do cadastro disponibilizada', 'Revisão do cadastro homologada' ou 'Mapa Ajustado'.";

    private void verificarAcesso(Usuario usuario, Subprocesso subprocesso) {
        final SituacaoSubprocesso situacao = subprocesso.getSituacao();

        if (hasRole(usuario, "CHEFE")) {
            validarSituacao(situacao, List.of(REVISAO_CADASTRO_EM_ANDAMENTO), MSG_ERRO_CHEFE);
        } else if (hasRole(usuario, "GESTOR")) {
            validarSituacao(situacao, List.of(REVISAO_CADASTRO_DISPONIBILIZADA), MSG_ERRO_GESTOR);
        } else if (hasRole(usuario, "ADMIN")) {
            validarSituacao(situacao, List.of(REVISAO_CADASTRO_DISPONIBILIZADA, REVISAO_CADASTRO_HOMOLOGADA, MAPA_AJUSTADO), MSG_ERRO_ADMIN);
        }
    }

    private void validarSituacao(SituacaoSubprocesso atual, List<SituacaoSubprocesso> esperadas, String mensagemErro) {
        if (!esperadas.contains(atual)) {
            throw new ErroAccessoNegado(mensagemErro);
        }
    }

    private boolean hasRole(Usuario usuario, String role) {
        return usuario.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_%s".formatted(role)));
    }

    private record CompetenciaImpactoAcumulador(Long codigo, String descricao) {
    }
}