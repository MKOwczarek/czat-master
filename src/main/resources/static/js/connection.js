let stompClient = null;
let connectingElement = document.querySelector(".connecting");

let socket = new SockJS('/ws');
stompClient = Stomp.over(socket);
stompClient.connect({}, onConnected, onError);

function onConnected() {
    connectingElement.classList.add('hidden');
    stompClient.subscribe(`/topic/table`, onChangeTable);
    stompClient.subscribe('/user/queue/canvas', onCanvas);
    stompClient.subscribe('/user/queue/sendModal', onModalWindow);
}

function onError() {
    connectingElement.textContent = 'Could not connect to WebSocket server. Please refresh this page to try again!';
    connectingElement.style.color = 'red';
}