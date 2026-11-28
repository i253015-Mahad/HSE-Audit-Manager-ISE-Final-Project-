/* ============================================================
   Live filtering for the Admin project list (GET /projects?q=&state=).

   The form stays a plain GET form: without JavaScript, Enter in the search
   box submits it and the server renders the same filtered view. With
   JavaScript, every change is applied immediately — no Apply button — by
   fetching the SAME route with &partial=true and swapping only the
   #project-results fragment, so the search box keeps its focus and caret.

   Access control is untouched: ProjectController.requireAdmin() runs for the
   partial response too (ASR-01). This file is only loaded by projects.html.
   ============================================================ */
(function () {
    'use strict';

    var form = document.getElementById('filter-form');
    var input = document.getElementById('filter-q');
    var stateSelect = document.getElementById('filter-state');
    var results = document.getElementById('project-results');
    if (!form || !input || !stateSelect || !results) {
        return;
    }

    var DEBOUNCE_MS = 250;
    var debounceTimer = null;
    var inFlight = null;

    /** Current filters as query params (without the partial flag). */
    function filterParams() {
        var params = new URLSearchParams();
        var text = input.value.trim();
        var state = stateSelect.value;
        if (text) {
            params.set('q', text);
        }
        if (state) {
            params.set('state', state);
        }
        return params;
    }

    /** Put the active filters in the address bar so the view stays shareable. */
    function syncUrl() {
        var qs = filterParams().toString();
        history.replaceState(null, '', qs ? '/projects?' + qs : '/projects');
    }

    function clearBusy(element) {
        element.removeAttribute('aria-busy');
        element.classList.remove('is-updating');
    }

    /** Apply the current filters live. Any pending request is aborted first. */
    function apply() {
        // Re-query: the container is replaced on every swap.
        results = document.getElementById('project-results') || results;
        if (!results) {
            form.submit();
            return;
        }
        if (debounceTimer !== null) {
            clearTimeout(debounceTimer);
            debounceTimer = null;
        }
        if (inFlight) {
            inFlight.abort();
        }
        var controller = new AbortController();
        inFlight = controller;

        var params = filterParams();
        var url = '/projects?' + params.toString();

        results.setAttribute('aria-busy', 'true');
        results.classList.add('is-updating');

        fetch(url + '&partial=true', { signal: controller.signal })
            .then(function (response) {
                if (!response.ok) {
                    throw new Error('HTTP ' + response.status);
                }
                return response.text();
            })
            .then(function (html) {
                // The response is the whole results fragment (same id), so the
                // container itself is replaced and re-queried.
                results.outerHTML = html;
                results = document.getElementById('project-results');
                if (results) {
                    clearBusy(results);
                }
                inFlight = null;
                syncUrl();
            })
            .catch(function (error) {
                if (error && error.name === 'AbortError') {
                    return; // superseded by a newer keystroke/selection
                }
                inFlight = null;
                if (results) {
                    clearBusy(results);
                }
                // No live response (network/500): fall back to the plain GET.
                form.submit();
            });
    }

    function applySoon() {
        if (debounceTimer !== null) {
            clearTimeout(debounceTimer);
        }
        debounceTimer = setTimeout(apply, DEBOUNCE_MS);
    }

    input.addEventListener('input', applySoon);
    stateSelect.addEventListener('change', apply);

    // Enter (or any implicit submit) must not reload the whole page.
    form.addEventListener('submit', function (event) {
        event.preventDefault();
        apply();
    });

    // "Clear filters" in the empty state: reset both controls, stay live.
    document.addEventListener('click', function (event) {
        var link = event.target && event.target.closest
            ? event.target.closest('[data-clear-filters]')
            : null;
        if (!link) {
            return;
        }
        event.preventDefault();
        input.value = '';
        stateSelect.value = '';
        apply();
        input.focus();
    });
})();
