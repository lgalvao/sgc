// noinspection JSUnresolvedReference

/**
 * Script principal da aplicação SGC
 * Contém funções utilitárias e inicialização de componentes comuns
 */

document.addEventListener('DOMContentLoaded', function() {
    initTooltips();
    initPopovers();
    initUIComponents();
    setupFormValidation();
    setupFlashMessages();
});

/**
 * Inicializa tooltips do Bootstrap
 */
function initTooltips() {
    const tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
    tooltipTriggerList.map(function (tooltipTriggerEl) {
        return new bootstrap.Tooltip(tooltipTriggerEl);
    });
}

/**
 * Inicializa popovers do Bootstrap
 */
function initPopovers() {
    const popoverTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="popover"]'));
    popoverTriggerList.map(function (popoverTriggerEl) {
        return new bootstrap.Popover(popoverTriggerEl, {
            trigger: 'focus',
            html: true
        });
    });
}

/**
 * Inicializa componentes de interface do usuário
 */
function initUIComponents() {
    // Ativa menus dropdown
    const dropdownElementList = [].slice.call(document.querySelectorAll('.dropdown-toggle'));
    dropdownElementList.map(function (dropdownToggleEl) {
        return new bootstrap.Dropdown(dropdownToggleEl);
    });
    
    // Inicializa toasts
    const toastElList = [].slice.call(document.querySelectorAll('.toast'));
    toastElList.map(function (toastEl) {
        return new bootstrap.Toast(toastEl, {autohide: true});
    });
    
    // Fecha automaticamente os alerts após 5 segundos
    const alerts = document.querySelectorAll('.alert');
    alerts.forEach(alert => {
        setTimeout(() => {
            const bsAlert = new bootstrap.Alert(alert);
            bsAlert.close();
        }, 5000);
    });
}

/**
 * Configura validação de formulários
 */
function setupFormValidation() {
    // Busca todos os formulários que precisam de validação
    const forms = document.querySelectorAll('.needs-validation');
    
    // Loop sobre eles e previne envio
    Array.from(forms).forEach(form => {
        form.addEventListener('submit', event => {
            if (!form.checkValidity()) {
                event.preventDefault();
                event.stopPropagation();
            }

            form.classList.add('was-validated');
        }, false);
    });
}

/**
 * Configura mensagens flash
 */
function setupFlashMessages() {
    // Adiciona funcionalidade de fechar para mensagens flash
    document.querySelectorAll('.alert .btn-close').forEach(button => {
        button.addEventListener('click', function() {
            const alert = this.closest('.alert');
            const bsAlert = new bootstrap.Alert(alert);
            bsAlert.close();
        });
    });
}

/**
 * Formata uma data para o formato brasileiro
 * @param {string} dateString - Data em formato ISO
 * @returns {string} Data formatada
 */
function formatDate(dateString) {
    if (!dateString) return '';
    
    const options = { 
        day: '2-digit', 
        month: '2-digit', 
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
        hour12: false
    };
    
    return new Date(dateString).toLocaleString('pt-BR', options);
}

/**
 * Formata um CPF
 * @param {string} cpf - CPF sem formatação
 * @returns {string} CPF formatado
 */
function formatCPF(cpf) {
    if (!cpf) return '';
    cpf = cpf.replace(/\D/g, '');
    return cpf.replace(/(\d{3})(\d{3})(\d{3})(\d{2})/, '$1.$2.$3-$4');
}

/**
 * Formata um telefone
 * @param {string} phone - Telefone sem formatação
 * @returns {string} Telefone formatado
 */
function formatPhone(phone) {
    if (!phone) return '';
    phone = phone.replace(/\D/g, '');
    
    if (phone.length === 11) {
        return phone.replace(/(\d{2})(\d{5})(\d{4})/, '($1) $2-$3');
    } else if (phone.length === 10) {
        return phone.replace(/(\d{2})(\d{4})(\d{4})/, '($1) $2-$3');
    }
    
    return phone;
}

/**
 * Exibe um alerta na página
 * @param {string} type - Tipo de alerta (success, danger, warning, info)
 * @param {string} message - Mensagem a ser exibida
 * @param {number} [duration=5000] - Duração em milissegundos que o alerta ficará visível
 */
function showAlert(type, message, duration = 5000) {
    const alertHtml = `
        <div class="alert alert-${type} alert-dismissible fade show" role="alert">
            ${message}
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Fechar"></button>
        </div>
    `;
    
    // Cria um container para os alertas se não existir
    let alertContainer = document.getElementById('alert-container');
    if (!alertContainer) {
        alertContainer = document.createElement('div');
        alertContainer.id = 'alert-container';
        alertContainer.style.position = 'fixed';
        alertContainer.style.top = '20px';
        alertContainer.style.right = '20px';
        alertContainer.style.zIndex = '1100';
        alertContainer.style.width = '350px';
        document.body.appendChild(alertContainer);
    }
    
    // Cria o elemento do alerta
    const alertElement = document.createElement('div');
    alertElement.innerHTML = alertHtml;
    alertContainer.prepend(alertElement);
    
    // Remove o alerta após o tempo especificado
    setTimeout(() => {
        const bsAlert = new bootstrap.Alert(alertElement.querySelector('.alert'));
        bsAlert.close();
        
        // Remove o container se não houver mais alertas
        setTimeout(() => {
            if (alertContainer && alertContainer.children.length === 0) {
                alertContainer.remove();
            }
        }, 150);
    }, duration);
}

// Adiciona funções ao escopo global para acesso em outros arquivos
window.SGC = window.SGC || {};
window.SGC.showAlert = showAlert;
window.SGC.formatDate = formatDate;
window.SGC.formatCPF = formatCPF;
window.SGC.formatPhone = formatPhone;
window.SGC.initTooltips = initTooltips;
window.SGC.initPopovers = initPopovers;
window.SGC.initUIComponents = initUIComponents;
window.SGC.setupFormValidation = setupFormValidation;
window.SGC.setupFlashMessages = setupFlashMessages;
window.SGC.formatCPF = formatCPF;
window.SGC.formatPhone = formatPhone;
