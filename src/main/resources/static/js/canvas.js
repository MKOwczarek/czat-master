let canvasForm = id("canvas-form");
let context = canvas.getContext("2d");
let width   = canvas.getAttribute("width");
let height  = canvas.getAttribute("height");

document.addEventListener("DOMContentLoaded", function() {
    function mainLoop() {
        if (mouse[0] && mouse[1] && mouse[3]) {
            let drawMessage = JSON.stringify({
                sender : username,
                content : mouse[2].toString() + "#" + mouse[3].toString() + context.strokeStyle,
                type : 'DRAW'
            });

            stompClient.send(`${path}/draw`, {}, drawMessage);
            mouse[1] = false;
        }
    }
    mainLoop();
});
