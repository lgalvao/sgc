package sgc.subprocesso.service.crud;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.comum.erros.ErroValidacao;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.Competencia;
import sgc.mapa.model.Mapa;
import sgc.mapa.service.MapaManutencaoService;
import sgc.subprocesso.dto.ErroValidacaoDto;
import sgc.subprocesso.dto.ValidacaoCadastroDto;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.emptyList;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class SubprocessoValidacaoService {
    private final MapaManutencaoService mapaManutencaoService;
    private final SubprocessoCrudService crudService; // Reuse lookups

    public List<Atividade> obterAtividadesSemConhecimento(Long codSubprocesso) {
        Subprocesso sp = crudService.buscarSubprocesso(codSubprocesso);
        return obterAtividadesSemConhecimento(sp.getMapa());
    }

    public List<Atividade> obterAtividadesSemConhecimento(@Nullable Mapa mapa) {
        if (mapa == null || mapa.getCodigo() == null) {
            return emptyList();
        }
        List<Atividade> atividades = mapaManutencaoService.buscarAtividadesPorMapaCodigoComConhecimentos(mapa.getCodigo());
        if (atividades.isEmpty()) {
            return emptyList();
        }
        return atividades.stream()
                .filter(a -> a.getConhecimentos().isEmpty())
                .toList();
    }

    public void validarExistenciaAtividades(Long codSubprocesso) {
        Subprocesso subprocesso = crudService.buscarSubprocesso(codSubprocesso);
        Mapa mapa = subprocesso.getMapa();

        List<Atividade> atividades = mapaManutencaoService.buscarAtividadesPorMapaCodigoComConhecimentos(mapa.getCodigo());
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

        if (!competenciasSemAssociacao.isEmpty()) {
            throw new ErroValidacao(
                    "Existem competências que não foram associadas a nenhuma atividade.",
                    Map.of("competenciasNaoAssociadas", competenciasSemAssociacao));
        }

        List<Atividade> atividades = mapaManutencaoService.buscarAtividadesPorMapaCodigo(mapaId);
        List<String> atividadesSemAssociacao = atividades.stream()
                .filter(a -> a.getCompetencias().isEmpty())
                .map(Atividade::getDescricao)
                .toList();

        if (!atividadesSemAssociacao.isEmpty()) {
            throw new ErroValidacao(
                    "Existem atividades que não foram associadas a nenhuma competência.",
                    Map.of("atividadesNaoAssociadas", atividadesSemAssociacao));
        }
    }

    public ValidacaoCadastroDto validarCadastro(Long codSubprocesso) {
        Subprocesso sp = crudService.buscarSubprocesso(codSubprocesso);
        List<ErroValidacaoDto> erros = new ArrayList<>();

        List<Atividade> atividades = mapaManutencaoService.buscarAtividadesPorMapaCodigoComConhecimentos(sp.getMapa().getCodigo());
        if (atividades.isEmpty()) {
            erros.add(ErroValidacaoDto.builder()
                    .tipo("SEM_ATIVIDADES")
                    .mensagem("O mapa não possui atividades cadastradas.")
                    .build());
        } else {
            for (Atividade atividade : atividades) {
                if (atividade.getConhecimentos().isEmpty()) {
                    erros.add(ErroValidacaoDto.builder()
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

    /**
     * Valida se a situação atual do subprocesso está entre as situações permitidas.
     *
     * @param subprocesso Subprocesso a validar (não pode ser null)
     * @param permitidas Conjunto de situações permitidas (não pode ser vazio)
     * @throws ErroValidacao se a situação atual não está entre as permitidas
     * @throws IllegalArgumentException se situação do subprocesso for null
     */
    public void validarSituacaoPermitida(@NonNull Subprocesso subprocesso, @NonNull Set<SituacaoSubprocesso> permitidas) {
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

    /**
     * Valida se a situação atual do subprocesso está entre as situações permitidas (variadic).
     *
     * @param subprocesso Subprocesso a validar (não pode ser null)
     * @param permitidas Situações permitidas (varargs, pelo menos uma é obrigatória)
     * @throws ErroValidacao se a situação atual não está entre as permitidas
     * @throws IllegalArgumentException se subprocesso for null, situação for null, ou nenhuma situação permitida for fornecida
     */
    public void validarSituacaoPermitida(@NonNull Subprocesso subprocesso, @NonNull SituacaoSubprocesso... permitidas) {
        if (permitidas.length == 0) {
            throw new IllegalArgumentException("Pelo menos uma situação permitida deve ser fornecida");
        }
        validarSituacaoPermitida(subprocesso, Set.of(permitidas));
    }

    /**
     * Valida se a situação atual do subprocesso está entre as situações permitidas com mensagem customizada.
     *
     * @param subprocesso Subprocesso a validar (não pode ser null)
     * @param mensagem Mensagem customizada de erro
     * @param permitidas Situações permitidas (varargs, pelo menos uma é obrigatória)
     * @throws ErroValidacao se a situação atual não está entre as permitidas
     * @throws IllegalArgumentException se subprocesso for null, situação for null, ou nenhuma situação permitida for fornecida
     */
    public void validarSituacaoPermitida(@NonNull Subprocesso subprocesso, @NonNull String mensagem, @NonNull SituacaoSubprocesso... permitidas) {
        if (subprocesso == null || subprocesso.getSituacao() == null) {
            throw new IllegalArgumentException("Subprocesso e sua situação não podem ser nulos");
        }
        
        if (permitidas.length == 0) {
            throw new IllegalArgumentException("Pelo menos uma situação permitida deve ser fornecida");
        }
        
        if (!Set.of(permitidas).contains(subprocesso.getSituacao())) {
            throw new ErroValidacao(mensagem);
        }
    }

    /**
     * Valida se a situação atual do subprocesso é maior ou igual à situação mínima.
     *
     * @param subprocesso Subprocesso a validar (não pode ser null)
     * @param minima Situação mínima exigida (não pode ser null)
     * @throws ErroValidacao se a situação atual é inferior à mínima
     * @throws IllegalArgumentException se subprocesso for null ou situação for null
     */
    public void validarSituacaoMinima(@NonNull Subprocesso subprocesso, @NonNull SituacaoSubprocesso minima) {
        if (subprocesso == null || subprocesso.getSituacao() == null) {
            throw new IllegalArgumentException("Subprocesso e sua situação não podem ser nulos");
        }
        
        if (minima == null) {
            throw new IllegalArgumentException("Situação mínima não pode ser nula");
        }
        
        if (subprocesso.getSituacao().ordinal() < minima.ordinal()) {
            throw new ErroValidacao(
                "Subprocesso não atingiu a situação mínima necessária. Situação atual: %s. Mínima exigida: %s"
                    .formatted(subprocesso.getSituacao(), minima));
        }
    }

    /**
     * Valida se a situação atual do subprocesso é maior ou igual à situação mínima com mensagem customizada.
     *
     * @param subprocesso Subprocesso a validar (não pode ser null)
     * @param minima Situação mínima exigida (não pode ser null)
     * @param mensagem Mensagem customizada de erro
     * @throws ErroValidacao se a situação atual é inferior à mínima
     * @throws IllegalArgumentException se subprocesso for null ou situação for null
     */
    public void validarSituacaoMinima(@NonNull Subprocesso subprocesso, @NonNull SituacaoSubprocesso minima, @NonNull String mensagem) {
        if (subprocesso == null || subprocesso.getSituacao() == null) {
            throw new IllegalArgumentException("Subprocesso e sua situação não podem ser nulos");
        }
        
        if (minima == null) {
            throw new IllegalArgumentException("Situação mínima não pode ser nula");
        }
        
        if (subprocesso.getSituacao().ordinal() < minima.ordinal()) {
            throw new ErroValidacao(mensagem);
        }
    }
}