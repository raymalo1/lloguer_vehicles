/* 
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Other/javascript.js to edit this template
 */


// Front del xat de Prisita. Les funcions són globals perquè el fragment les crida amb onclick.
(function () {
    var ENDPOINT = '/api/chatbot/preguntar';
    var enCurs = false;

    function cos() {
        return document.getElementById('chat-body');
    }

    function afegirBombolla(text, tipus) {
        var div = document.createElement('div');
        div.className = 'bubble ' + tipus;
        div.style.whiteSpace = 'pre-wrap';
        // textContent (mai innerHTML): la resposta ve d'una IA i no s'ha d'interpretar com a HTML
        div.textContent = text;
        cos().appendChild(div);
        cos().scrollTop = cos().scrollHeight;
        return div;
    }

    async function enviar(text) {
        text = (text || '').trim();
        if (!text || enCurs) {
            return;
        }
        enCurs = true;

        afegirBombolla(text, 'user');
        var espera = afegirBombolla('Escrivint...', 'bot');

        try {
            var resposta = await fetch(ENDPOINT, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                credentials: 'same-origin',
                body: JSON.stringify({ missatge: text })
            });
            var dades = await resposta.json().catch(function () { return null; });
            espera.textContent = (dades && dades.resposta)
                ? dades.resposta
                : 'No he pogut respondre. Contacta amb l\'oficina de Terrassa.';
        } catch (e) {
            espera.textContent = 'No puc connectar ara mateix. Contacta amb l\'oficina de Terrassa.';
        } finally {
            enCurs = false;
            cos().scrollTop = cos().scrollHeight;
        }
    }

    window.enviarMensaje = function (text) {
        enviar(text);
    };

    window.enviarTextoLibre = function () {
        var input = document.getElementById('chat-input');
        var text = input.value;
        input.value = '';
        enviar(text);
    };

    window.handleEnter = function (event) {
        if (event.key === 'Enter') {
            event.preventDefault();
            window.enviarTextoLibre();
        }
    };
})();