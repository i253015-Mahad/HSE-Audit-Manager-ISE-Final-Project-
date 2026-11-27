(function () {
    const container = document.getElementById('violations');
    if (!container) return;

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
        });
    }

    document.getElementById('add-violation').addEventListener('click', () => {
        const rows = container.querySelectorAll('.violation-row');
        if (!rows.length) return;
        const clone = rows[rows.length - 1].cloneNode(true);
        clone.querySelectorAll('input, textarea, select').forEach(element => {
            if (element.type === 'file') {
                element.value = '';
            } else if (element.tagName === 'SELECT') {
                element.selectedIndex = 0;
            } else {
                element.value = '';
            }
        });
        container.appendChild(clone);
        syncOtherFields(clone);
        reindex();
    });

    container.querySelectorAll('.violation-row').forEach(syncOtherFields);

    container.addEventListener('click', event => {
        const button = event.target.closest('.remove-violation');
        if (!button) return;
        if (container.querySelectorAll('.violation-row').length <= 1) {
            alert('A report needs at least one violation.');
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
            alert('A violation may have at most 3 photographs.');
            event.target.value = '';
        }
    });
})();
