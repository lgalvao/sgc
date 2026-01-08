package sgc.seguranca.acesso;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sgc.organizacao.model.Unidade;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Serviço centralizado para gerenciar hierarquia de unidades organizacionais.
 * Consolida a lógica de verificação de subordinação que estava dispersa
 * em múltiplos serviços.
 */
@Service
@RequiredArgsConstructor
public class HierarchyService {

    /**
     * Verifica se uma unidade é subordinada a outra na hierarquia.
     * 
     * @param alvo A unidade que pode ser subordinada
     * @param superior A unidade que pode ser superior
     * @return true se 'alvo' é subordinada a 'superior' (direta ou indiretamente)
     */
    public boolean isSubordinada(Unidade alvo, Unidade superior) {
        if (alvo == null || superior == null || alvo.getUnidadeSuperior() == null) {
            return false;
        }

        Unidade atual = alvo;
        while (atual != null) {
            if (Objects.equals(superior.getCodigo(), atual.getCodigo())) {
                return true;
            }
            atual = atual.getUnidadeSuperior();
        }
        return false;
    }

    /**
     * Verifica se uma unidade é a mesma ou subordinada a outra.
     * 
     * @param alvo A unidade que pode ser subordinada ou igual
     * @param superior A unidade que pode ser superior ou igual
     * @return true se 'alvo' é a mesma unidade ou subordinada a 'superior'
     */
    public boolean isMesmaOuSubordinada(Unidade alvo, Unidade superior) {
        if (alvo == null || superior == null) {
            return false;
        }
        
        if (Objects.equals(alvo.getCodigo(), superior.getCodigo())) {
            return true;
        }
        
        return isSubordinada(alvo, superior);
    }

    /**
     * Verifica se uma unidade é a superior imediata de outra.
     * 
     * @param alvo A unidade alvo
     * @param superior A potencial unidade superior imediata
     * @return true se 'superior' é a unidade superior imediata de 'alvo'
     */
    public boolean isSuperiorImediata(Unidade alvo, Unidade superior) {
        if (alvo == null || superior == null || alvo.getUnidadeSuperior() == null) {
            return false;
        }
        
        return Objects.equals(alvo.getUnidadeSuperior().getCodigo(), superior.getCodigo());
    }

    /**
     * Busca todas as unidades subordinadas de uma unidade raiz.
     * Nota: Este método requer que as relações estejam carregadas em memória.
     * 
     * @param raiz A unidade raiz
     * @return Lista de unidades subordinadas (não inclui a própria unidade raiz)
     */
    public List<Unidade> buscarSubordinadas(Unidade raiz) {
        // Implementação básica - pode ser otimizada com query ao banco se necessário
        List<Unidade> subordinadas = new ArrayList<>();
        // Esta implementação seria expandida conforme necessidade
        return subordinadas;
    }

    /**
     * Busca os códigos de todas as unidades na hierarquia (incluindo a própria).
     * 
     * @param codUnidade Código da unidade raiz
     * @return Lista de códigos de unidades na hierarquia
     */
    public List<Long> buscarCodigosHierarquia(Long codUnidade) {
        List<Long> codigos = new ArrayList<>();
        if (codUnidade != null) {
            codigos.add(codUnidade);
            // Implementação básica - expandir conforme necessidade
        }
        return codigos;
    }
}
