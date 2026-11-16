// noinspection JSUnresolvedReference

/**
 * Script para gerenciar as interações da página de processos
 * Depende de main.js para funcionalidades compartilhadas
 */
document.addEventListener('DOMContentLoaded', function() {
    // Inicializa componentes compartilhados
    if (window.SGC && typeof window.SGC.initUIComponents === 'function') {
        window.SGC.initUIComponents();
    }
    
    // Confirmação antes de excluir
    const deleteButtons = document.querySelectorAll('.btn-delete');
    deleteButtons.forEach(button => {
        button.addEventListener('click', function(e) {
            if (!confirm('Tem certeza que deseja excluir este processo? Esta ação não pode ser desfeita.')) {
                e.preventDefault();
            }
        });
    });
    
    // Validação de datas
    const dateInputs = document.querySelectorAll('input[type="datetime-local"]');
    dateInputs.forEach(input => {
        input.addEventListener('change', function() {
            const selectedDate = new Date(this.value);
            const now = new Date();
            
            if (selectedDate < now) {
                if (window.SGC && window.SGC.showAlert) {
                    window.SGC.showAlert('warning', 'A data não pode ser anterior à data e hora atuais.');
                } else {
                    alert('A data não pode ser anterior à data e hora atuais.');
                }
                this.value = '';
            }
        });
    });
    
    // Inicialização de selects com busca (TomSelect)
    const initTomSelect = () => {
        const selectElements = document.querySelectorAll('select[data-search]');
        if (selectElements.length > 0 && typeof TomSelect !== 'undefined') {
            selectElements.forEach(select => {
                new TomSelect(select, {
                    create: false,
                    sortField: {
                        field: "text",
                        direction: "asc"
                    },
                    placeholder: select.getAttribute('placeholder') || 'Selecione...',
                    dropdownParent: 'body'
                });
            });
        }
    };
    
    // Verifica se o TomSelect está disponível
    if (typeof TomSelect !== 'undefined') {
        initTomSelect();
    } else {
        // Tenta carregar o TomSelect se não estiver disponível
        const script = document.createElement('script');
        script.src = 'https://cdn.jsdelivr.net/npm/tom-select/dist/js/tom-select.complete.min.js';
        script.onload = initTomSelect;
        document.head.appendChild(script);
    }
});