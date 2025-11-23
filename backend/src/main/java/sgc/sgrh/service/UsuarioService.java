package sgc.sgrh.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.sgrh.dto.EntrarReq;
import sgc.sgrh.dto.PerfilUnidade;
import sgc.sgrh.dto.UnidadeDto;
import sgc.sgrh.model.Perfil;
import sgc.sgrh.model.Usuario;
import sgc.sgrh.model.UsuarioRepo;
import sgc.unidade.model.Unidade;
import sgc.unidade.model.UnidadeRepo;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UsuarioService {
    private final SgrhService sgrhService;
    private final UnidadeRepo unidadeRepo;
    private final UsuarioRepo usuarioRepo;

    /**
     * Simula a autenticação de um usuário.
     * <p>
     * Em um ambiente de produção, este método conteria a lógica para validar as
     * credenciais do usuário contra um provedor de identidade, como o Active Directory.
     *
     * @param tituloEleitoral O título de eleitor do usuário.
     * @param senha           A senha do usuário (atualmente ignorada na simulação).
     * @return {@code true} para simular uma autenticação bem-sucedida.
     */
    public boolean autenticar(String tituloEleitoral, String senha) {
        log.debug("Simulando autenticação para usuário: {}", tituloEleitoral);
        // Em um cenário real, aqui haveria a chamada para o AcessoAD.
        // Para esta simulação, consideramos sempre autenticado.
        return true;
    }

    /**
     * Busca os perfis e unidades aos quais um usuário está associado.
     * <p>
     * Este método consulta o serviço SGRH para obter os perfis do usuário e, em
     * seguida, enriquece essa informação com os dados da unidade local, montando
     * uma lista de opções de acesso para o usuário.
     *
     * @param tituloEleitoral O título de eleitor do usuário.
     * @return Uma {@link List} de {@link PerfilUnidade}, onde cada item representa
     *         um perfil que o usuário pode assumir em uma determinada unidade.
     * @throws ErroEntidadeNaoEncontrada se uma unidade associada a um perfil não for
     *                                  encontrada no banco de dados local.
     */
    public List<PerfilUnidade> autorizar(String tituloEleitoral) {
        log.debug("Buscando autorizações (perfis e unidades) para o usuário: {}", tituloEleitoral);
        Usuario usuario = usuarioRepo.findById(tituloEleitoral)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Usuário", tituloEleitoral));

        Unidade unidade = usuario.getUnidade();
        UnidadeDto unidadeDto = UnidadeDto.builder()
                .codigo(unidade.getCodigo())
                .nome(unidade.getNome())
                .sigla(unidade.getSigla())
                .codigoPai(null)
                .tipo(unidade.getTipo().name())
                .isElegivel(false)
                .build();

        return usuario.getPerfis().stream()
                .map(perfil -> new PerfilUnidade(perfil, unidadeDto))
                .collect(Collectors.toList());
    }

    /**
     * Simula a conclusão do processo de login do usuário.
     * <p>
     * Em um cenário real, este método seria responsável por estabelecer o contexto
     * de segurança do usuário para a sessão, registrando o perfil e a unidade
     * escolhidos para as verificações de permissão subsequentes.
     *
     * @param tituloEleitoral O título de eleitor do usuário.
     * @param pu              O {@link PerfilUnidade} que representa o contexto de acesso
     *                        escolhido pelo usuário para a sessão.
     */
    public void entrar(String tituloEleitoral, PerfilUnidade pu) {
        // Em um cenário real, aqui seriam definidos o perfil e a unidade do usuário na sessão.
        // Para esta simulação, apenas registramos a escolha.
        log.info("Usuário {} entrou com sucesso. Perfil: {}, Unidade: {}", tituloEleitoral, pu.getPerfil(), pu.getSiglaUnidade());
    }

    /**
     * Processa a requisição de entrada do usuário no sistema.
     * <p>
     * Este método de conveniência extrai os dados da requisição, constrói o
     * objeto {@link PerfilUnidade} e delega para o método principal de entrada.
     *
     * @param request O DTO {@link EntrarReq} contendo os dados da escolha do usuário.
     * @throws ErroEntidadeNaoEncontrada se a unidade especificada na requisição não for encontrada.
     * @throws IllegalArgumentException se o perfil especificado na requisição for inválido.
     */
    public void entrar(EntrarReq request) {
        Unidade unidade = unidadeRepo.findById(request.getUnidadeCodigo())
            .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Unidade não encontrada com código: " + request.getUnidadeCodigo()));
        Perfil perfil = Perfil.valueOf(request.getPerfil());
        UnidadeDto unidadeDto = UnidadeDto.builder()
                .codigo(unidade.getCodigo())
                .nome(unidade.getNome())
                .sigla(unidade.getSigla())
                .codigoPai(null)
                .tipo(unidade.getTipo().name())
                .isElegivel(false)
                .build();
        PerfilUnidade pu = new PerfilUnidade(perfil, unidadeDto);
        this.entrar(request.getTituloEleitoral(), pu);
    }
}