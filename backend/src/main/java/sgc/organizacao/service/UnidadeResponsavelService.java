package sgc.organizacao.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.comum.erros.ErroValidacao;
import sgc.comum.repo.ComumRepo;
import sgc.organizacao.dto.AtribuicaoTemporariaDto;
import sgc.organizacao.dto.CriarAtribuicaoTemporariaRequest;
import sgc.organizacao.dto.UnidadeResponsavelDto;
import sgc.organizacao.mapper.UsuarioMapper;
import sgc.organizacao.model.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
 *
 * <p>Este serviço foi extraído de UnidadeFacade para respeitar o
 * Single Responsibility Principle (SRP).
 *
 */
@Service
@RequiredArgsConstructor
public class UnidadeResponsavelService {
    private final UnidadeRepo unidadeRepo;
    private final UsuarioRepo usuarioRepo;
    private final UsuarioPerfilRepo usuarioPerfilRepo;
    private final AtribuicaoTemporariaRepo atribuicaoTemporariaRepo;
    private final UsuarioMapper usuarioMapper;
    private final ComumRepo repo;

    /**
     * Busca todas as atribuições temporárias cadastradas.
     *
     * @return lista de DTOs de atribuições temporárias
     */
    public List<AtribuicaoTemporariaDto> buscarTodasAtribuicoes() {
        return atribuicaoTemporariaRepo.findAll().stream()
                .map(usuarioMapper::toAtribuicaoTemporariaDto)
                .toList();
    }

    /**
     * Cria uma atribuição temporária de responsável para uma unidade.
     *
     * @param codUnidade código da unidade
     * @param request    dados da atribuição (usuário, datas, justificativa)
     * @throws ErroValidacao se a data de término for anterior à data de início
     */
    public void criarAtribuicaoTemporaria(Long codUnidade, CriarAtribuicaoTemporariaRequest request) {
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
     * @param siglaUnidade sigla da unidade
     * @return usuário responsável com atribuições carregadas
     * @throws ErroEntidadeNaoEncontrada se a unidade ou responsável não for encontrado
     */
    @Transactional(readOnly = true)
    public Usuario buscarResponsavelAtual(String siglaUnidade) {
        Unidade unidade = unidadeRepo.findBySigla(siglaUnidade)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Unidade", siglaUnidade));

        Usuario usuarioSimples = usuarioRepo
                .chefePorCodUnidade(unidade.getCodigo())
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Responsável da unidade", siglaUnidade));

        Usuario usuarioCompleto = usuarioRepo.findByIdWithAtribuicoes(usuarioSimples.getTituloEleitoral())
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Usuário", usuarioSimples.getTituloEleitoral()));

        carregarAtribuicoesUsuario(usuarioCompleto);
        return usuarioCompleto;
    }

    /**
     * Busca o responsável (titular e substituto) de uma unidade.
     *
     * @param unidadeCodigo código da unidade
     * @return DTO do responsável com dados do titular e substituto
     * @throws ErroEntidadeNaoEncontrada se não houver responsável
     */
    public UnidadeResponsavelDto buscarResponsavelUnidade(Long unidadeCodigo) {
        List<Usuario> chefes = usuarioRepo.findChefesByUnidadesCodigos(List.of(unidadeCodigo));
        if (chefes.isEmpty()) {
            throw new ErroEntidadeNaoEncontrada("Responsável da unidade", unidadeCodigo);
        }
        return montarResponsavelDto(unidadeCodigo, chefes);
    }

    /**
     * Busca responsáveis de múltiplas unidades em lote.
     *
     * @param unidadesCodigos lista de códigos de unidades
     * @return mapa de código de unidade para DTO de responsável
     */
    @Transactional(readOnly = true)
    public Map<Long, UnidadeResponsavelDto> buscarResponsaveisUnidades(List<Long> unidadesCodigos) {
        if (unidadesCodigos.isEmpty()) return Collections.emptyMap();

        List<Usuario> todosChefes = usuarioRepo.findChefesByUnidadesCodigos(unidadesCodigos);
        if (todosChefes.isEmpty()) return Collections.emptyMap();

        List<String> titulos = todosChefes.stream().map(Usuario::getTituloEleitoral).toList();
        List<Usuario> chefesCompletos = usuarioRepo.findByIdInWithAtribuicoes(titulos);
        carregarAtribuicoesEmLote(chefesCompletos);

        Map<Long, List<Usuario>> chefesPorUnidade = chefesCompletos.stream()
                .flatMap(u -> {
                    Set<UsuarioPerfil> atribuicoes = new HashSet<>(
                            usuarioPerfilRepo.findByUsuarioTitulo(u.getTituloEleitoral())
                    );
                    return u.getTodasAtribuicoes(atribuicoes).stream()
                            .filter(a -> a.getPerfil() == Perfil.CHEFE && unidadesCodigos.contains(a.getUnidadeCodigo()))
                            .map(a -> new AbstractMap.SimpleEntry<>(a.getUnidadeCodigo(), u));
                })
                .collect(groupingBy(Map.Entry::getKey, mapping(Map.Entry::getValue, toList())));

        return chefesPorUnidade.entrySet().stream()
                .collect(toMap(
                        Map.Entry::getKey,
                        e -> montarResponsavelDto(e.getKey(), e.getValue())
                ));
    }

    private UnidadeResponsavelDto montarResponsavelDto(Long unidadeCodigo, List<Usuario> chefes) {
        Usuario titular = chefes.getFirst();
        Usuario substituto = chefes.size() > 1 ? chefes.get(1) : null;

        return UnidadeResponsavelDto.builder()
                .unidadeCodigo(unidadeCodigo)
                .titularTitulo(titular.getTituloEleitoral())
                .titularNome(titular.getNome())
                .substitutoTitulo(substituto != null ? substituto.getTituloEleitoral() : null)
                .substitutoNome(substituto != null ? substituto.getNome() : null)
                .build();
    }
}
