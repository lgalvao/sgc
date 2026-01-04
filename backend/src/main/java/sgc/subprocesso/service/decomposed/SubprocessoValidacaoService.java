package sgc.subprocesso.service.decomposed;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.mapa.model.Atividade;
import sgc.mapa.service.AtividadeService;
import sgc.mapa.model.Competencia;
import sgc.mapa.service.CompetenciaService;
import sgc.mapa.model.Mapa;
import sgc.subprocesso.dto.ErroValidacaoDto;
import sgc.subprocesso.dto.ValidacaoCadastroDto;
import sgc.subprocesso.model.Subprocesso;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.comum.erros.ErroAccessoNegado;
import sgc.comum.erros.ErroValidacao;
import sgc.organizacao.UsuarioService;
import sgc.organizacao.model.Usuario;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.stream.Collectors;
import static java.util.Collections.emptyList;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class SubprocessoValidacaoService {
    private final AtividadeService atividadeService;
    private final CompetenciaService competenciaService;
    private final UsuarioService usuarioService;
    private final SubprocessoCrudService crudService; // Reuse lookups

    public void validarPermissaoEdicaoMapa(Long mapaCodigo, String tituloUsuario) {
        Subprocesso subprocesso = crudService.obterEntidadePorCodigoMapa(mapaCodigo);

        if (subprocesso.getUnidade() == null) {
            throw new ErroEntidadeNaoEncontrada("Unidade não associada ao Subprocesso %d".formatted(subprocesso.getCodigo()));
        }

        Usuario usuario = usuarioService.buscarUsuarioPorLogin(tituloUsuario);
        String titularTitulo = subprocesso.getUnidade().getTituloTitular();

        if (titularTitulo == null || !titularTitulo.equals(String.valueOf(usuario.getTituloEleitoral()))) {
             throw new ErroAccessoNegado("Usuário não autorizado a editar este mapa.");
        }
    }

    public List<Atividade> obterAtividadesSemConhecimento(Long codSubprocesso) {
        Subprocesso sp = crudService.buscarSubprocesso(codSubprocesso);
        return obterAtividadesSemConhecimento(sp.getMapa());
    }

    public List<Atividade> obterAtividadesSemConhecimento(Mapa mapa) {
        if (mapa == null || mapa.getCodigo() == null) {
            return emptyList();
        }
        List<Atividade> atividades = atividadeService.buscarPorMapaCodigoComConhecimentos(mapa.getCodigo());
        if (atividades == null || atividades.isEmpty()) {
            return emptyList();
        }
        return atividades.stream()
                .filter(a -> a.getConhecimentos() == null || a.getConhecimentos().isEmpty())
                .collect(Collectors.toList());
    }

    public void validarExistenciaAtividades(Long codSubprocesso) {
        Subprocesso subprocesso = crudService.buscarSubprocesso(codSubprocesso);
        Mapa mapa = subprocesso.getMapa();
        if (mapa == null) {
            throw new ErroValidacao("Mapa não encontrado para o subprocesso.");
        }

        List<Atividade> atividades = atividadeService.buscarPorMapaCodigoComConhecimentos(mapa.getCodigo());
        if (atividades == null || atividades.isEmpty()) {
            throw new ErroValidacao("O mapa de competências deve ter ao menos uma atividade cadastrada.");
        }

        List<Atividade> atividadesSemConhecimento = atividades.stream()
            .filter(a -> a.getConhecimentos() == null || a.getConhecimentos().isEmpty())
            .toList();

        if (!atividadesSemConhecimento.isEmpty()) {
            throw new ErroValidacao("Todas as atividades devem possuir conhecimentos vinculados. Verifique as atividades pendentes.");
        }
    }

    public void validarAssociacoesMapa(Long mapaId) {
        List<Competencia> competencias = competenciaService.buscarPorMapa(mapaId);
        List<String> competenciasSemAssociacao = competencias.stream()
            .filter(c -> c.getAtividades().isEmpty())
            .map(Competencia::getDescricao)
            .toList();

        if (!competenciasSemAssociacao.isEmpty()) {
            throw new ErroValidacao(
                    "Existem competências que não foram associadas a nenhuma atividade.",
                    Map.of("competenciasNaoAssociadas", competenciasSemAssociacao));
        }

        List<Atividade> atividades = atividadeService.buscarPorMapaCodigo(mapaId);
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

        if (sp.getMapa() == null) {
            return ValidacaoCadastroDto.builder()
                    .valido(false)
                    .erros(List.of(ErroValidacaoDto.builder()
                            .tipo("MAPA_INEXISTENTE")
                            .mensagem("O subprocesso não possui um mapa associado.")
                            .build()))
                    .build();
        }

        List<Atividade> atividades = atividadeService.buscarPorMapaCodigoComConhecimentos(sp.getMapa().getCodigo());
        if (atividades == null || atividades.isEmpty()) {
            erros.add(ErroValidacaoDto.builder()
                    .tipo("SEM_ATIVIDADES")
                    .mensagem("O mapa não possui atividades cadastradas.")
                    .build());
        } else {
            for (Atividade atividade : atividades) {
                if (atividade.getConhecimentos() == null || atividade.getConhecimentos().isEmpty()) {
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
}
