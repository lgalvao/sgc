package sgc.subprocesso.service;

import lombok.*;
import org.jspecify.annotations.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;
import sgc.comum.*;
import sgc.comum.erros.*;
import sgc.mapa.model.*;
import sgc.mapa.service.*;
import sgc.subprocesso.dto.*;
import sgc.subprocesso.model.*;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubprocessoValidacaoService {

    private final SubprocessoRepo subprocessoRepo;
    private final MapaManutencaoService mapaManutencaoService;

    public void validarExistenciaAtividades(Subprocesso subprocesso) {
        if (subprocesso.getMapa() == null || subprocesso.getMapa().getCodigo() == null) {
            throw new ErroValidacao(SgcMensagens.SUBPROCESSO_SEM_MAPA);
        }

        List<Atividade> atividades = mapaManutencaoService.atividadesMapaCodigoComConhecimentos(subprocesso.getMapa().getCodigo());
        if (atividades.isEmpty()) {
            throw new ErroValidacao(SgcMensagens.MAPA_SEM_ATIVIDADES);
        }

        List<Atividade> atividadesSemConhecimento = atividades.stream()
                .filter(a -> a.getConhecimentos().isEmpty())
                .toList();

        if (!atividadesSemConhecimento.isEmpty()) {
            throw new ErroValidacao(SgcMensagens.ATIVIDADES_SEM_CONHECIMENTOS);
        }
    }

    public void validarAssociacoesMapa(Long codMapa) {
        List<Competencia> competencias = mapaManutencaoService.competenciasCodMapa(codMapa);
        List<String> competenciasSemAssociacao = competencias.stream()
                .filter(c -> c.getAtividades().isEmpty())
                .map(Competencia::getDescricao)
                .toList();

        if (!competenciasSemAssociacao.isEmpty()) throw new ErroValidacao(
                SgcMensagens.COMPETENCIAS_SEM_ATIVIDADE,
                Map.of("competenciasNaoAssociadas", competenciasSemAssociacao));

        List<Atividade> atividades = mapaManutencaoService.atividadesMapaCodigo(codMapa);
        List<String> atividadesSemAssociacao = atividades.stream()
                .filter(a -> a.getCompetencias().isEmpty())
                .map(Atividade::getDescricao)
                .toList();

        if (!atividadesSemAssociacao.isEmpty()) throw new ErroValidacao(
                SgcMensagens.ATIVIDADES_SEM_COMPETENCIA,
                Map.of("atividadesNaoAssociadas", atividadesSemAssociacao));
    }

    public void validarMapaParaDisponibilizacao(Subprocesso subprocesso) {
        Long codMapa = subprocesso.getMapa().getCodigo();
        var competencias = mapaManutencaoService.competenciasCodMapa(codMapa);

        if (competencias.stream().anyMatch(c -> c.getAtividades().isEmpty())) {
            throw new ErroValidacao(SgcMensagens.TODAS_COMPETENCIAS_DEVEM_TER_ATIVIDADE);
        }

        var atividadesDoSubprocesso = mapaManutencaoService.atividadesMapaCodigo(codMapa);
        var atividadesAssociadas = competencias.stream()
                .flatMap(c -> c.getAtividades().stream())
                .map(Atividade::getCodigo)
                .collect(java.util.stream.Collectors.toSet());

        var atividadesNaoAssociadas = atividadesDoSubprocesso.stream()
                .filter(a -> !atividadesAssociadas.contains(a.getCodigo()))
                .toList();

        if (!atividadesNaoAssociadas.isEmpty()) {
            String nomesAtividades = atividadesNaoAssociadas.stream()
                    .map(Atividade::getDescricao)
                    .collect(java.util.stream.Collectors.joining(", "));

            throw new ErroValidacao(
                    SgcMensagens.ATIVIDADES_PENDENTES_PREFIXO
                            .formatted(nomesAtividades));
        }
    }

    public ValidacaoCadastroDto validarCadastro(Subprocesso sp) {
        List<ValidacaoCadastroDto.Erro> erros = new ArrayList<>();

        if (sp.getMapa() == null || sp.getMapa().getCodigo() == null) {
            erros.add(ValidacaoCadastroDto.Erro.builder()
                    .tipo("SEM_MAPA")
                    .mensagem("O subprocesso não possui mapa associado.")
                    .build());
            return ValidacaoCadastroDto.builder()
                    .valido(false)
                    .erros(erros)
                    .build();
        }

        List<Atividade> atividades = mapaManutencaoService.atividadesMapaCodigoComConhecimentos(sp.getMapa().getCodigo());
        if (atividades.isEmpty()) {
            erros.add(ValidacaoCadastroDto.Erro.builder()
                    .tipo("SEM_ATIVIDADES")
                    .mensagem("O mapa não possui atividades cadastradas.")
                    .build());
        } else {
            for (Atividade atividade : atividades) {
                if (atividade.getConhecimentos().isEmpty()) {
                    erros.add(ValidacaoCadastroDto.Erro.builder()
                            .tipo("ATIVIDADE_SEM_CONHECIMENTO")
                            .atividadeCodigo(atividade.getCodigo())
                            .descricaoAtividade(atividade.getDescricao())
                            .mensagem("Esta atividade não possui conhecimentos associados.")
                            .build());
                }
            }
        }

        return ValidacaoCadastroDto.builder()
                .valido(erros.isEmpty())
                .erros(erros)
                .build();
    }

    public void validarSituacaoPermitida(Subprocesso subprocesso, Set<SituacaoSubprocesso> permitidas) {
        if (subprocesso.getSituacao() == null) {
            throw new IllegalArgumentException("Situação do subprocesso não pode ser nula");
        }
        if (permitidas.isEmpty()) {
            throw new IllegalArgumentException("Conjunto de situações permitidas não pode ser vazio");
        }
        if (!permitidas.contains(subprocesso.getSituacao())) {
            String permitidasStr = String.join(", ",
                    permitidas.stream().map(SituacaoSubprocesso::name).toList());
            throw new ErroValidacao(
                    SgcMensagens.SITUACAO_NAO_PERMITE
                            .formatted(subprocesso.getSituacao(), permitidasStr));
        }
    }

    public void validarSituacaoPermitida(Subprocesso subprocesso, SituacaoSubprocesso... permitidas) {
        if (permitidas.length == 0) {
            throw new IllegalArgumentException("Pelo menos uma situação permitida deve ser fornecida");
        }
        validarSituacaoPermitida(subprocesso, Set.of(permitidas));
    }

    public void validarSituacaoPermitida(Subprocesso subprocesso, String mensagem, SituacaoSubprocesso... permitidas) {
        if (subprocesso.getSituacao() == null) {
            throw new IllegalArgumentException("Situação do subprocesso não pode ser nula");
        }
        if (permitidas.length == 0) {
            throw new IllegalArgumentException("Pelo menos uma situação permitida deve ser fornecida");
        }
        if (!Set.of(permitidas).contains(subprocesso.getSituacao())) {
            throw new ErroValidacao(mensagem);
        }
    }

    public void validarSituacaoMinima(Subprocesso subprocesso, SituacaoSubprocesso minima, String mensagem) {
        if (subprocesso.getSituacao() == null) {
            throw new IllegalArgumentException("Situação do subprocesso não pode ser nula");
        }
        if (subprocesso.getSituacao().ordinal() < minima.ordinal()) {
            throw new ErroValidacao(mensagem);
        }
    }

    public void validarRequisitosNegocioParaDisponibilizacao(Subprocesso sp, List<Atividade> atividadesSemConhecimento) {
        validarExistenciaAtividades(sp);

        if (!atividadesSemConhecimento.isEmpty()) {
            var atividadesInfo = atividadesSemConhecimento.stream()
                    .map(atividade -> Map.of("codigo", atividade.getCodigo(), "descricao", atividade.getDescricao()))
                    .toList();
            throw new ErroValidacao(
                    SgcMensagens.ATIVIDADES_SEM_CONHECIMENTO_ASSOCIADO,
                    Map.of("atividadesSemConhecimento", atividadesInfo));
        }
    }

    public boolean verificarAcessoUnidadeAoProcesso(Long codProcesso, List<Long> unidadeCodigos) {
        if (unidadeCodigos.isEmpty()) {
            return false;
        }
        return subprocessoRepo.existsByProcessoCodigoAndUnidadeCodigoIn(codProcesso, unidadeCodigos);
    }

    public ValidationResult validarSubprocessosParaFinalizacao(Long codProcesso) {
        long total = subprocessoRepo.countByProcessoCodigo(codProcesso);

        if (total == 0) return ValidationResult.ofInvalido("O processo não possui subprocessos para finalizar");

        long homologados = subprocessoRepo.countByProcessoCodigoAndSituacaoIn(codProcesso,
                List.of(
                        SituacaoSubprocesso.MAPEAMENTO_MAPA_HOMOLOGADO,
                        SituacaoSubprocesso.REVISAO_MAPA_HOMOLOGADO,
                        SituacaoSubprocesso.DIAGNOSTICO_CONCLUIDO
                )
        );

        if (total != homologados) {
            return ValidationResult.ofInvalido(
                    ("Apenas %d de %d subprocessos foram homologados. " +
                            "Todos os subprocessos devem estar homologados para finalizar o processo.")
                            .formatted(homologados, total)
            );
        }

        return ValidationResult.ofValido();
    }

    public record ValidationResult(boolean valido, @Nullable String mensagem) {
        public static ValidationResult ofValido() {
            return new ValidationResult(true, null);
        }

        public static ValidationResult ofInvalido(String mensagem) {
            return new ValidationResult(false, mensagem);
        }
    }
}
