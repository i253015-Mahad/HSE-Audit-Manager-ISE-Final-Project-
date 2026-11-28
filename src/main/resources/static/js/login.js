(function () {
    const input = document.getElementById('password');
    const toggle = document.getElementById('password-toggle');
    if (!input || !toggle) return;

    function setRevealed(revealed) {
        input.type = revealed ? 'text' : 'password';
        toggle.classList.toggle('is-revealed', revealed);
        toggle.setAttribute('aria-pressed', String(revealed));
        const label = revealed ? 'Hide password' : 'Show password';
        toggle.setAttribute('aria-label', label);
        toggle.setAttribute('title', label);
    }

    toggle.addEventListener('click', () => {
        setRevealed(input.type === 'password');
        input.focus();
        if (input.type === 'text') {
            input.setSelectionRange(input.value.length, input.value.length);
        }
    });
})();
