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
        List<Atividade> atividades = listarAtividadesObrigatorias(subprocesso);
        List<Atividade> atividadesSemConhecimento = listarAtividadesSemConhecimento(atividades);

        if (!atividadesSemConhecimento.isEmpty()) {
            throw new ErroValidacao(Mensagens.ATIVIDADES_SEM_CONHECIMENTOS);
        }
    }

    public void validarAssociacoesMapa(Long codMapa) {
        List<Competencia> competencias = mapaManutencaoService.competenciasCodMapa(codMapa);
        List<String> competenciasSemAssociacao = competencias.stream()
                .filter(c -> c.getAtividades().isEmpty())
                .map(Competencia::getDescricao)
                .toList();

        if (!competenciasSemAssociacao.isEmpty()) throw new ErroValidacao(
                Mensagens.COMPETENCIAS_SEM_ATIVIDADE,
                Map.of("competenciasNaoAssociadas", competenciasSemAssociacao));

        List<Atividade> atividades = mapaManutencaoService.atividadesMapaCodigo(codMapa);
        List<String> atividadesSemAssociacao = atividades.stream()
                .filter(a -> a.getCompetencias().isEmpty())
                .map(Atividade::getDescricao)
                .toList();

        if (!atividadesSemAssociacao.isEmpty()) throw new ErroValidacao(
                Mensagens.ATIVIDADES_SEM_COMPETENCIA,
                Map.of("atividadesNaoAssociadas", atividadesSemAssociacao));
    }

    public void validarMapaParaDisponibilizacao(Subprocesso subprocesso) {
        Mapa mapa = subprocesso.getMapa();
        if (mapa == null) {
            throw new ErroValidacao(Mensagens.SUBPROCESSO_SEM_MAPA);
        }
        Long codMapa = mapa.getCodigo();
        var competencias = mapaManutencaoService.competenciasCodMapa(codMapa);

        if (competencias.stream().anyMatch(c -> c.getAtividades().isEmpty())) {
            throw new ErroValidacao(Mensagens.TODAS_COMPETENCIAS_DEVEM_TER_ATIVIDADE);
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
                    Mensagens.ATIVIDADES_PENDENTES_PREFIXO
                            .formatted(nomesAtividades));
        }
    }

    public ValidacaoCadastroDto validarCadastro(Subprocesso sp) {
        List<ValidacaoCadastroDto.Erro> erros = new ArrayList<>();

        Long codMapa = obterCodigoMapaOpt(sp.getMapa()).orElse(null);
        if (codMapa == null) {
            erros.add(ValidacaoCadastroDto.Erro.builder()
                    .tipo("SEM_MAPA")
                    .mensagem("O subprocesso não possui mapa associado.")
                    .build());
            return ValidacaoCadastroDto.builder()
                    .valido(false)
                    .erros(erros)
                    .build();
        }

        List<Atividade> atividades = mapaManutencaoService.atividadesMapaCodigoComConhecimentos(codMapa);
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
                    Mensagens.SITUACAO_NAO_PERMITE
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

    public void validarRequisitosNegocioParaDisponibilizacao(Subprocesso sp) {
        List<Atividade> atividades = listarAtividadesObrigatorias(sp);
        List<Atividade> atividadesSemConhecimento = listarAtividadesSemConhecimento(atividades);
        validarAtividadesSemConhecimento(atividadesSemConhecimento);
    }

    private void validarAtividadesSemConhecimento(List<Atividade> atividadesSemConhecimento) {
        if (!atividadesSemConhecimento.isEmpty()) {
            var atividadesInfo = atividadesSemConhecimento.stream()
                    .map(atividade -> Map.of("codigo", atividade.getCodigo(), "descricao", atividade.getDescricao()))
                    .toList();
            throw new ErroValidacao(
                    Mensagens.ATIVIDADES_SEM_CONHECIMENTO_ASSOCIADO,
                    Map.of("atividadesSemConhecimento", atividadesInfo));
        }
    }

    private List<Atividade> listarAtividadesObrigatorias(Subprocesso subprocesso) {
        Long codMapa = obterCodigoMapaObrigatorio(subprocesso.getMapa());
        List<Atividade> atividades = mapaManutencaoService.atividadesMapaCodigoComConhecimentos(codMapa);
        if (atividades.isEmpty()) {
            throw new ErroValidacao(Mensagens.MAPA_SEM_ATIVIDADES);
        }
        return atividades;
    }

    private List<Atividade> listarAtividadesSemConhecimento(List<Atividade> atividades) {
        return atividades.stream()
                .filter(atividade -> atividade.getConhecimentos().isEmpty())
                .toList();
    }

    private Long obterCodigoMapaObrigatorio(@Nullable Mapa mapa) {
        return obterCodigoMapaOpt(mapa)
                .orElseThrow(() -> new ErroValidacao(Mensagens.SUBPROCESSO_SEM_MAPA));
    }

    private Optional<Long> obterCodigoMapaOpt(@Nullable Mapa mapa) {
        return Optional.ofNullable(mapa).map(Mapa::getCodigo);
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
