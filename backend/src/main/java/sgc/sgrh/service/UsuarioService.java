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
import sgc.unidade.model.TipoUnidade;
import sgc.unidade.model.Unidade;
import sgc.unidade.model.UnidadeRepo;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UsuarioService {
    private final SgrhService sgrhService;
    private final UnidadeRepo unidadeRepo;
    private final UsuarioRepo usuarioRepo;

    public boolean autenticar(String tituloEleitoral, String senha) {
        log.debug("Simulando autenticação para usuário: {}", tituloEleitoral);
        return true;
    }

    public List<PerfilUnidade> autorizar(String tituloEleitoral) {
        log.debug("Buscando autorizações (perfis e unidades) para o usuário: {}", tituloEleitoral);
        Usuario usuario = usuarioRepo.findById(tituloEleitoral)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Usuário", tituloEleitoral));

        List<PerfilUnidade> perfis = new ArrayList<>();

        // 1. Atribuições explícitas e temporárias (via getTodasAtribuicoes)
        perfis.addAll(usuario.getTodasAtribuicoes().stream()
                .map(atribuicao -> new PerfilUnidade(atribuicao.getPerfil(), toUnidadeDto(atribuicao.getUnidade())))
                .toList());

        // 2. Perfil de titular de unidade
        List<Unidade> unidadesChefiadas = unidadeRepo.findByTitularTituloEleitoral(tituloEleitoral);
        for (Unidade unidade : unidadesChefiadas) {
            Perfil perfil = determinarPerfilPorUnidade(unidade);
            PerfilUnidade pu = new PerfilUnidade(perfil, toUnidadeDto(unidade));
            if (!contemPerfilUnidade(perfis, pu)) {
                perfis.add(pu);
            }
        }

        // 3. Perfil SERVIDOR na unidade de lotação (se não houver outro perfil para
        // ela)
        Unidade lotacao = usuario.getUnidadeLotacao();
        if (lotacao != null) {
            boolean jaTemPerfilNaLotacao = perfis.stream()
                    .anyMatch(p -> p.getUnidade().getCodigo().equals(lotacao.getCodigo())
                            && !p.getPerfil().equals(Perfil.SERVIDOR));

            if (!jaTemPerfilNaLotacao) {
                boolean isOperacional = lotacao.getTipo() == TipoUnidade.OPERACIONAL
                        || lotacao.getTipo() == TipoUnidade.INTEROPERACIONAL;
                // Lógica do frontend: só adiciona SERVIDOR se for OPERACIONAL (ou
                // INTEROPERACIONAL)
                // "if (isOperacional && !hasNonServidorProfileForPrincipalUnit)"
                if (isOperacional) {
                    PerfilUnidade puServidor = new PerfilUnidade(Perfil.SERVIDOR, toUnidadeDto(lotacao));
                    if (!contemPerfilUnidade(perfis, puServidor)) {
                        perfis.add(puServidor);
                    }
                }
            }
        }

        log.info("Usuário {} tem {} perfis autorizados.", tituloEleitoral, perfis.size());
        return perfis;
    }

    private boolean contemPerfilUnidade(List<PerfilUnidade> lista, PerfilUnidade novo) {
        return lista.stream().anyMatch(p -> p.getPerfil().equals(novo.getPerfil()) &&
                p.getUnidade().getCodigo().equals(novo.getUnidade().getCodigo()));
    }

    private Perfil determinarPerfilPorUnidade(Unidade unidade) {
        if ("SEDOC".equals(unidade.getSigla()))
            return Perfil.ADMIN;
        if (unidade.getTipo() == TipoUnidade.INTERMEDIARIA)
            return Perfil.GESTOR;
        if (unidade.getTipo() == TipoUnidade.OPERACIONAL || unidade.getTipo() == TipoUnidade.INTEROPERACIONAL)
            return Perfil.CHEFE;
        return Perfil.SERVIDOR;
    }

    private UnidadeDto toUnidadeDto(Unidade unidade) {
        return UnidadeDto.builder()
                .codigo(unidade.getCodigo())
                .nome(unidade.getNome())
                .sigla(unidade.getSigla())
                .codigoPai(unidade.getUnidadeSuperior() != null ? unidade.getUnidadeSuperior().getCodigo() : null)
                .tipo(unidade.getTipo().name())
                .isElegivel(false)
                .build();
    }

    public void entrar(String tituloEleitoral, PerfilUnidade pu) {
        log.info("Usuário {} entrou com sucesso. Perfil: {}, Unidade: {}", tituloEleitoral, pu.getPerfil(),
                pu.getSiglaUnidade());
    }

    public void entrar(EntrarReq request) {
        Unidade unidade = unidadeRepo.findById(request.getUnidadeCodigo())
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada(
                        "Unidade não encontrada com código: " + request.getUnidadeCodigo()));
        Perfil perfil = Perfil.valueOf(request.getPerfil());
        PerfilUnidade pu = new PerfilUnidade(perfil, toUnidadeDto(unidade));
        this.entrar(request.getTituloEleitoral(), pu);
    }
}
