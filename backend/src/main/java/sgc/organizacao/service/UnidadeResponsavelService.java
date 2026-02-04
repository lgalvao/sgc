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
 */
@Service
@RequiredArgsConstructor
public class UnidadeResponsavelService {
    private final UnidadeRepo unidadeRepo;
    private final UsuarioRepo usuarioRepo;
    private final UsuarioPerfilRepo usuarioPerfilRepo;
    private final AtribuicaoTemporariaRepo atribuicaoTemporariaRepo;
    private final ResponsabilidadeRepo responsabilidadeRepo;
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

        Responsabilidade resp = repo.buscar(Responsabilidade.class, unidade.getCodigo());
        Usuario usuarioCompleto = repo.buscar(Usuario.class, resp.getUsuarioTitulo());

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
        Responsabilidade responsabilidade = repo.buscar(Responsabilidade.class, unidadeCodigo);
        Usuario responsavel = repo.buscar(Usuario.class, responsabilidade.getUsuarioTitulo());

        Usuario titularOficial = responsabilidade.getUnidade() != null ?
                repo.buscar(Usuario.class, responsabilidade.getUnidade().getTituloTitular()) : null;

        return montarResponsavelDto(unidadeCodigo, responsavel, titularOficial);
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

        List<Responsabilidade> responsabilidades = responsabilidadeRepo.findByUnidadeCodigoIn(unidadesCodigos);
        if (responsabilidades.isEmpty()) return Collections.emptyMap();

        // Coletar todos os títulos (responsáveis atuais e titulares oficiais) para busca em lote
        Set<String> todosTitulos = new HashSet<>();
        responsabilidades.forEach(r -> {
            todosTitulos.add(r.getUsuarioTitulo());
            if (r.getUnidade() != null) {
                todosTitulos.add(r.getUnidade().getTituloTitular());
            }
        });

        Map<String, Usuario> usuariosPorTitulo = usuarioRepo.findByIdInWithAtribuicoes(new ArrayList<>(todosTitulos)).stream()
                .collect(toMap(Usuario::getTituloEleitoral, u -> u));

        carregarAtribuicoesEmLote(new ArrayList<>(usuariosPorTitulo.values()));

        return responsabilidades.stream()
                .filter(r -> usuariosPorTitulo.containsKey(r.getUsuarioTitulo()))
                .collect(toMap(
                        Responsabilidade::getUnidadeCodigo,
                        r -> {
                            Usuario responsavel = usuariosPorTitulo.get(r.getUsuarioTitulo());
                            Usuario titularOficial = r.getUnidade() != null ? 
                                    usuariosPorTitulo.get(r.getUnidade().getTituloTitular()) : null;
                            return montarResponsavelDto(r.getUnidadeCodigo(), responsavel, titularOficial);
                        }
                ));
    }

    private UnidadeResponsavelDto montarResponsavelDto(Long unidadeCodigo, Usuario responsavel, Usuario titularOficial) {
        // Se não temos titular oficial ou o responsável é o próprio titular
        if (titularOficial == null || responsavel.getTituloEleitoral().equals(titularOficial.getTituloEleitoral())) {
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

    private UnidadeResponsavelDto montarResponsavelDto(Long unidadeCodigo, Usuario responsavel) {
        // Fallback mantendo compatibilidade, sem info do titular oficial
        return UnidadeResponsavelDto.builder()
                .unidadeCodigo(unidadeCodigo)
                .titularTitulo(responsavel.getTituloEleitoral())
                .titularNome(responsavel.getNome())
                .substitutoTitulo(null)
                .substitutoNome(null)
                .build();
    }

    /**
     * Busca os códigos das unidades onde o usuário é o responsável atual.
     *
     * @param titulo título do usuário
     * @return lista de códigos de unidades
     */
    @Transactional(readOnly = true)
    public List<Long> buscarUnidadesOndeEhResponsavel(String titulo) {
        return responsabilidadeRepo.findByUsuarioTitulo(titulo).stream()
                .map(Responsabilidade::getUnidadeCodigo)
                .toList();
    }

    private void carregarAtribuicoesUsuario(Usuario usuario) {
        Set<UsuarioPerfil> permanentes = new HashSet<>(usuarioPerfilRepo.findByUsuarioTitulo(usuario.getTituloEleitoral()));
        usuario.getTodasAtribuicoes(permanentes);
    }

    private void carregarAtribuicoesEmLote(List<Usuario> usuarios) {
        for (Usuario u : usuarios) {
            Set<UsuarioPerfil> permanentes = new HashSet<>(usuarioPerfilRepo.findByUsuarioTitulo(u.getTituloEleitoral()));
            u.getTodasAtribuicoes(permanentes);
        }
    }
}
