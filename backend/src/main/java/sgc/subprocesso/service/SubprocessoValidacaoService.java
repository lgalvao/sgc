package sgc.subprocesso.service;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.comum.erros.ErroValidacao;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.Competencia;
import sgc.mapa.service.MapaManutencaoService;
import sgc.subprocesso.dto.ValidacaoCadastroDto;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoRepo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubprocessoValidacaoService {

    private final SubprocessoRepo subprocessoRepo;
    private final MapaManutencaoService mapaManutencaoService;

    public void validarExistenciaAtividades(Subprocesso subprocesso) {
        if (subprocesso.getMapa() == null || subprocesso.getMapa().getCodigo() == null) {
            throw new ErroValidacao("Subprocesso não possui mapa associado.");
        }

        List<Atividade> atividades = mapaManutencaoService.buscarAtividadesPorMapaCodigoComConhecimentos(subprocesso.getMapa().getCodigo());
        if (atividades.isEmpty()) {
            throw new ErroValidacao("O mapa de competências deve ter ao menos uma atividade cadastrada.");
        }

        List<Atividade> atividadesSemConhecimento = atividades.stream()
                .filter(a -> a.getConhecimentos().isEmpty())
                .toList();

        if (!atividadesSemConhecimento.isEmpty()) {
            throw new ErroValidacao("Todas as atividades devem possuir conhecimentos vinculados. Verifique as atividades pendentes.");
        }
    }

    public void validarAssociacoesMapa(Long mapaId) {
        List<Competencia> competencias = mapaManutencaoService.buscarCompetenciasPorCodMapa(mapaId);
        List<String> competenciasSemAssociacao = competencias.stream()
                .filter(c -> c.getAtividades().isEmpty())
                .map(Competencia::getDescricao)
                .toList();

        if (!competenciasSemAssociacao.isEmpty()) throw new ErroValidacao(
                "Existem competências que não foram associadas a nenhuma atividade.",
                Map.of("competenciasNaoAssociadas", competenciasSemAssociacao));

        List<Atividade> atividades = mapaManutencaoService.buscarAtividadesPorMapaCodigo(mapaId);
        List<String> atividadesSemAssociacao = atividades.stream()
                .filter(a -> a.getCompetencias().isEmpty())
                .map(Atividade::getDescricao)
                .toList();

        if (!atividadesSemAssociacao.isEmpty()) throw new ErroValidacao(
                "Existem atividades que não foram associadas a nenhuma competência.",
                Map.of("atividadesNaoAssociadas", atividadesSemAssociacao));
    }

    public void validarMapaParaDisponibilizacao(Subprocesso subprocesso) {
        Long codMapa = subprocesso.getMapa().getCodigo();
        var competencias = mapaManutencaoService.buscarCompetenciasPorCodMapa(codMapa);

        if (competencias.stream().anyMatch(c -> c.getAtividades().isEmpty())) {
            throw new ErroValidacao("Todas as competências devem estar associadas a pelo menos uma atividade.");
        }

        var atividadesDoSubprocesso = mapaManutencaoService.buscarAtividadesPorMapaCodigo(codMapa);
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
                    "Todas as atividades devem estar associadas a pelo menos uma competência.%nAtividades pendentes: %s"
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

        List<Atividade> atividades = mapaManutencaoService.buscarAtividadesPorMapaCodigoComConhecimentos(sp.getMapa().getCodigo());
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
                    "Situação do subprocesso não permite esta operação. Situação atual: %s. Situações permitidas: %s"
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
                    "Existem atividades sem conhecimentos associados.",
                    Map.of("atividadesSemConhecimento", atividadesInfo));
        }
    }

    public boolean verificarAcessoUnidadeAoProcesso(Long processoId, List<Long> unidadeCodigos) {
        if (unidadeCodigos.isEmpty()) {
            return false;
        }
        return subprocessoRepo.existsByProcessoCodigoAndUnidadeCodigoIn(processoId, unidadeCodigos);
    }

    public ValidationResult validarSubprocessosParaFinalizacao(Long processoId) {
        long total = subprocessoRepo.countByProcessoCodigo(processoId);

        if (total == 0) return ValidationResult.ofInvalido("O processo não possui subprocessos para finalizar");

        long homologados = subprocessoRepo.countByProcessoCodigoAndSituacaoIn(processoId,
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
