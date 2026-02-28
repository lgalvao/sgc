package sgc.organizacao.service;

import lombok.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;
import sgc.comum.erros.*;
import sgc.comum.model.*;
import sgc.organizacao.dto.*;
import sgc.organizacao.model.*;

import java.time.*;
import java.util.*;

import static java.util.stream.Collectors.*;

/**
 * Serviço especializado para gerenciar responsáveis e atribuições de unidades.
 *
 * <p>Responsabilidades:
 * <ul>
 *   <li>Gestão de responsáveis (titular e substituto)</li>
 *   <li>Atribuições temporárias de responsáveis</li>
 *   <li>Carregamento de perfis e atribuições de usuários</li>
 *   <li>Consultas em lote de responsáveis</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class ResponsavelUnidadeService {
    private final UsuarioRepo usuarioRepo;
    private final AtribuicaoTemporariaRepo atribuicaoTemporariaRepo;
    private final ResponsabilidadeRepo responsabilidadeRepo;
    private final ComumRepo repo;

    /**
     * Busca todas as atribuições temporárias cadastradas.
     */
    public List<AtribuicaoDto> buscarTodasAtribuicoes() {
        return atribuicaoTemporariaRepo.findAll().stream()
                .map(this::toAtribuicaoTemporariaDto)
                .toList();
    }

    private AtribuicaoDto toAtribuicaoTemporariaDto(AtribuicaoTemporaria a) {
        Usuario usuario = repo.buscar(Usuario.class, a.getUsuarioTitulo());
        return AtribuicaoDto.builder()
                .codigo(a.getCodigo())
                .unidadeCodigo(a.getUnidade().getCodigo())
                .unidadeSigla(a.getUnidade().getSigla())
                .usuario(usuario)
                .dataInicio(a.getDataInicio())
                .dataTermino(a.getDataTermino())
                .justificativa(a.getJustificativa())
                .build();
    }

    /**
     * Cria uma atribuição temporária de responsável para uma unidade.
     *
     * @throws ErroValidacao se a data de término for anterior à data de início
     */
    public void criarAtribuicaoTemporaria(Long codUnidade, CriarAtribuicaoRequest request) {
        Unidade unidade = repo.buscar(Unidade.class, codUnidade);

        String titulo = request.tituloEleitoralUsuario();
        Usuario usuario = repo.buscar(Usuario.class, titulo);

        LocalDate inicio = request.dataInicio() != null ? request.dataInicio() : LocalDate.now();

        if (request.dataTermino().isBefore(inicio)) {
            throw new ErroValidacao("A data de término deve ser posterior à data de início.");
        }

        AtribuicaoTemporaria atribuicao = new AtribuicaoTemporaria()
                .setUnidade(unidade)
                .setUsuarioTitulo(usuario.getTituloEleitoral())
                .setUsuarioMatricula(usuario.getMatricula())
                .setDataInicio(request.dataInicio() != null ? request.dataInicio().atStartOfDay() : LocalDateTime.now())
                .setDataTermino(request.dataTermino().atTime(23, 59, 59))
                .setJustificativa(request.justificativa());

        atribuicaoTemporariaRepo.save(atribuicao);
    }

    /**
     * Busca o responsável atual de uma unidade (com atribuições carregadas).
     *
     * @throws ErroEntidadeNaoEncontrada se a unidade ou responsável não for encontrado
     */
    @Transactional(readOnly = true)
    public Usuario buscarResponsavelAtual(String siglaUnidade) {
        Unidade unidade = repo.buscarPorSigla(Unidade.class, siglaUnidade);

        Responsabilidade resp = repo.buscar(Responsabilidade.class, unidade.getCodigo());

        return repo.buscar(Usuario.class, resp.getUsuarioTitulo());
    }

    /**
     * Busca o responsável (titular e substituto) de uma unidade.
     *
     * @throws ErroEntidadeNaoEncontrada se não houver responsável
     */
    public UnidadeResponsavelDto buscarResponsavelUnidade(Long unidadeCodigo) {
        Responsabilidade responsabilidade = repo.buscar(Responsabilidade.class, unidadeCodigo);
        Usuario responsavel = repo.buscar(Usuario.class, responsabilidade.getUsuarioTitulo());

        Usuario titularOficial = repo.buscar(Usuario.class, responsabilidade.getUnidade().getTituloTitular());

        return montarResponsavelDto(unidadeCodigo, responsavel, titularOficial);
    }

    /**
     * Busca responsáveis de múltiplas unidades em lote.
     */
    @Transactional(readOnly = true)
    public Map<Long, UnidadeResponsavelDto> buscarResponsaveisUnidades(List<Long> unidadesCodigos) {
        if (unidadesCodigos.isEmpty()) return Collections.emptyMap();

        List<Responsabilidade> responsabilidades = responsabilidadeRepo.findByUnidadeCodigoIn(unidadesCodigos);
        if (responsabilidades.isEmpty()) return Collections.emptyMap();

        // Coletar todos os títulos (responsáveis atuais e titulares oficiais) para busca em lote
        Set<String> todosTitulos = new HashSet<>();
        responsabilidades.forEach(r -> {
            todosTitulos.add(r.getUsuarioTitulo());
            todosTitulos.add(r.getUnidade().getTituloTitular());
        });

        Map<String, Usuario> usuariosPorTitulo = usuarioRepo.findByIdInWithAtribuicoes(new ArrayList<>(todosTitulos)).stream()
                .collect(toMap(Usuario::getTituloEleitoral, u -> u));

        return responsabilidades.stream()
                .filter(r -> usuariosPorTitulo.containsKey(r.getUsuarioTitulo()))
                .collect(toMap(
                        Responsabilidade::getUnidadeCodigo,
                        r -> {
                            Usuario responsavel = usuariosPorTitulo.get(r.getUsuarioTitulo());
                            Usuario titularOficial = usuariosPorTitulo.get(r.getUnidade().getTituloTitular());
                            return montarResponsavelDto(r.getUnidadeCodigo(), responsavel, titularOficial);
                        }
                ));
    }

    private UnidadeResponsavelDto montarResponsavelDto(Long unidadeCodigo, Usuario responsavel, Usuario titularOficial) {
        // Se o responsável é o próprio titular
        if (responsavel.getTituloEleitoral().equals(titularOficial.getTituloEleitoral())) {
            return UnidadeResponsavelDto.builder()
                    .unidadeCodigo(unidadeCodigo)
                    .titularTitulo(responsavel.getTituloEleitoral())
                    .titularNome(responsavel.getNome())
                    .substitutoTitulo(null)
                    .substitutoNome(null)
                    .build();
        }

        // Caso o responsável seja um substituto ou atribuição temporária
        return UnidadeResponsavelDto.builder()
                .unidadeCodigo(unidadeCodigo)
                .titularTitulo(titularOficial.getTituloEleitoral())
                .titularNome(titularOficial.getNome())
                .substitutoTitulo(responsavel.getTituloEleitoral())
                .substitutoNome(responsavel.getNome())
                .build();
    }


    /**
     * Busca os códigos das unidades onde o usuário é o responsável atual.
     */
    @Transactional(readOnly = true)
    public List<Long> buscarUnidadesOndeEhResponsavel(String titulo) {
        return responsabilidadeRepo.findByUsuarioTitulo(titulo).stream()
                .map(Responsabilidade::getUnidadeCodigo)
                .toList();
    }
}
