(function () {
    const container = document.getElementById('violations');
    const addButton = document.getElementById('add-violation');
    const template = document.getElementById('violation-template');
    if (!container || !addButton || !template) return;

    function toggleOtherField(row, select) {
        const kind = select.dataset.otherField;
        const wrapper = row.querySelector('.other-field[data-other-field="' + kind + '"]');
        if (!wrapper) return;

        const show = select.value === select.dataset.otherValue;
        wrapper.classList.toggle('is-visible', show);
        const input = wrapper.querySelector('input[type="text"]');
        if (input) {
            input.disabled = !show;
            if (!show) input.value = '';
        }
    }

    function syncOtherFields(row) {
        row.querySelectorAll('.choice-with-other').forEach(select => toggleOtherField(row, select));
    }

    function reindex() {
        container.querySelectorAll('.violation-row').forEach((row, index) => {
            row.querySelectorAll('[name]').forEach(element => {
                element.name = element.name.replace(/violations\[\d+\]/, 'violations[' + index + ']');
            });
            const label = row.querySelector('.violation-number');
            if (label) label.textContent = String(index + 1);
            const remove = row.querySelector('.remove-violation');
            if (remove) remove.hidden = index === 0;
        });
        syncObservationMirrors();
    }

    function syncObservationMirrors() {
        const source = container.querySelector('.violation-row:first-child');
        if (!source) return;

        const values = {};
        source.querySelectorAll('[data-observation-field]').forEach(field => {
            values[field.dataset.observationField] = field.value;
        });

        container.querySelectorAll('.violation-row:not(:first-child)').forEach(row => {
            row.querySelectorAll('.observation-mirror').forEach(field => {
                const value = values[field.dataset.observationField] || '';
                field.value = value;
                if (field.tagName === 'SELECT') {
                    field.selectedIndex = value ? Array.from(field.options)
                        .findIndex(option => option.value === value) : 0;
                }
            });
        });
    }

    function clearRow(row) {
        row.querySelectorAll('input, textarea, select').forEach(element => {
            if (element.classList.contains('observation-mirror') || element.classList.contains('readonly')) return;
            if (element.readOnly) return;
            if (element.type === 'file') {
                element.value = '';
            } else if (element.tagName === 'SELECT') {
                element.selectedIndex = 0;
            } else if (element.type === 'checkbox' || element.type === 'radio') {
                element.checked = false;
            } else {
                element.value = '';
            }
        });
        row.querySelectorAll('.other-field').forEach(field => field.classList.remove('is-visible'));
        syncOtherFields(row);
    }

    addButton.addEventListener('click', () => {
        const clone = template.content.firstElementChild.cloneNode(true);
        container.appendChild(clone);
        clearRow(clone);
        reindex();
        syncOtherFields(clone);
        syncObservationMirrors();
        clone.querySelector('input, select, textarea')?.focus();
    });

    container.querySelectorAll('.violation-row').forEach(syncOtherFields);
    reindex();

    container.addEventListener('click', event => {
        const button = event.target.closest('.remove-violation');
        if (!button) return;
        if (container.querySelectorAll('.violation-row').length <= 1) {
            return;
        }
        button.closest('.violation-row').remove();
        reindex();
    });

    container.addEventListener('change', event => {
        if (event.target.matches('.choice-with-other')) {
            toggleOtherField(event.target.closest('.violation-row'), event.target);
            return;
        }
        if (event.target.type !== 'file') return;
        if (event.target.files.length > 3) {
            alert('An observation may have at most 3 photographs.');
            event.target.value = '';
        }
    });

    container.addEventListener('input', event => {
        if (event.target.matches('[data-observation-field]:not(.observation-mirror)')) {
            syncObservationMirrors();
        }
    });

    const project = document.getElementById('project');
    const manager = document.getElementById('manager');
    if (project && manager) {
        const updateManager = () => {
            const selected = project.options[project.selectedIndex];
            manager.value = selected ? (selected.dataset.manager || '') : '';
        };
        project.addEventListener('change', updateManager);
        updateManager();
    }
})();
