package sgc.sgrh;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import sgc.comum.erros.ErroDominioNaoEncontrado;
import sgc.sgrh.dto.EntrarRequest;
import sgc.sgrh.dto.PerfilDto;
import sgc.sgrh.dto.PerfilUnidade;
import sgc.unidade.modelo.Unidade;
import sgc.unidade.modelo.UnidadeRepo;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UsuarioService {

    private final SgrhService sgrhService;
    private final UnidadeRepo unidadeRepo;

    /**
     * Simula a autenticação do usuário no Active Directory.
     *
     * @param tituloEleitoral O título eleitoral do usuário.
     * @param senha           A senha do usuário (não utilizada na simulação).
     * @return Sempre `true` para simular sucesso.
     */
    public boolean autenticar(long tituloEleitoral, String senha) {
        log.info("Simulando autenticação para: {}/{}", tituloEleitoral, senha);
        // Em um cenário real, aqui haveria a chamada para o AcessoAD.
        // Para esta simulação, consideramos sempre autenticado com sucesso.
        return true;
    }

    /**
     * Busca os pares de perfil-unidade que um usuário pode assumir no sistema.
     *
     * @param tituloEleitoral O título eleitoral do usuário.
     * @return Uma lista de `PerfilUnidade` representando as opções de login.
     */
    public List<PerfilUnidade> autorizar(long tituloEleitoral) {
        log.info("Buscando autorizações (perfis e unidades) para o título: {}", tituloEleitoral);
        List<PerfilDto> perfisDto = sgrhService.buscarPerfisUsuario(String.valueOf(tituloEleitoral));

        return perfisDto.stream()
            .map(dto -> {
                Unidade unidade = unidadeRepo.findById(dto.unidadeCodigo())
                    .orElseThrow(() -> new ErroDominioNaoEncontrado("Unidade não encontrada com código: " + dto.unidadeCodigo()));
                Perfil perfil = Perfil.valueOf(dto.perfil());
                return new PerfilUnidade(perfil, unidade);
            })
            .collect(Collectors.toList());
    }

    /**
     * Simula a entrada final do usuário, definindo seu contexto de trabalho.
     *
     * @param tituloEleitoral O título eleitoral do usuário.
     * @param pu              O par `PerfilUnidade` escolhido pelo usuário.
     */
    public void entrar(long tituloEleitoral, PerfilUnidade pu) {
        // Em um cenário real, aqui seriam definidos o perfil e a unidade do usuário na sessão.
        // Para esta simulação, apenas registramos a escolha.
        log.info("Usuário com título {} entrou com sucesso. Perfil: {}, Unidade: {}",
            tituloEleitoral, pu.getPerfil(), pu.getUnidade().getSigla());
    }

    public void entrar(EntrarRequest request) {
        Unidade unidade = unidadeRepo.findById(request.getUnidadeCodigo())
            .orElseThrow(() -> new ErroDominioNaoEncontrado("Unidade não encontrada com código: " + request.getUnidadeCodigo()));
        Perfil perfil = Perfil.valueOf(request.getPerfil());
        PerfilUnidade pu = new PerfilUnidade(perfil, unidade);
        this.entrar(request.getTituloEleitoral(), pu);
    }
}