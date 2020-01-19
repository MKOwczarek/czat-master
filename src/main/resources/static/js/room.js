let roomId;
let path = null;

function join2room(event) {
    roomId = event.toString();

    tableForm.classList.add('hidden');
    canvasForm.classList.remove('hidden');
    chatPage.classList.remove('hidden');
    userList.classList.remove('hidden');

    roomIdDisplay.textContent = roomId;
    path = `/app/chat/${roomId}`;

    scoreSubscription = stompClient.subscribe(`/topic/${roomId}/score`, onScore);
    changeGuessSubscription = stompClient.subscribe(`/topic/${roomId}/changeGuess`, messageSend);
    messageReceivedSubscription = stompClient.subscribe(`/topic/${roomId}/public`, onMessageReceived);
    endSubscription = stompClient.subscribe(`/topic/${roomId}/end`, onEnd);
    timerSubscription = stompClient.subscribe(`/topic/${roomId}/timer`, onTimer);
    resetCanvasSubscription = stompClient.subscribe(`/topic/${roomId}/resetCanvas`, onClearCanvas);

    stompClient.send(`${path}/addUser`, {}, JSON.stringify({sender: username, type: 'JOIN'}));
}

function messageSend(payload) {
    guessOpened.innerHTML = '';
}

function onMessageReceived(payload) {
    let message = JSON.parse(payload.body);

    let messageElement = document.createElement('li');

    if (message.type === 'JOIN') {
        messageElement.classList.add('event-message');
        message.content = message.sender + ' has joined the chat!';
    } else if (message.type === 'LEAVE') {
        messageElement.classList.add('event-message');
        message.content = message.sender + ' has left the chat!';
    } 
     else {
        messageElement.classList.add('chat-message');

        let avatarElement = document.createElement('i');
        let avatarText = document.createTextNode(message.sender[0]);
        avatarElement.appendChild(avatarText);
        avatarElement.style['background-color'] = getAvatarColor(message.sender);

        messageElement.appendChild(avatarElement);

        let usernameElement = document.createElement('span');
        let usernameText = document.createTextNode(message.sender);
        usernameElement.appendChild(usernameText);
        messageElement.appendChild(usernameElement);
    }

    let textElement = document.createElement('p');
    let messageText = document.createTextNode(message.content);
    textElement.appendChild(messageText);

    messageElement.appendChild(textElement);

    messageArea.appendChild(messageElement);
    messageArea.scrollTop = messageArea.scrollHeight;
}

function onEnd(payload) {
    let message = JSON.parse(payload.body);

    //TODO remove first and last char
    let score = message.content.split(",");
    for (let i = 0; i < score.length; i++) {
        modalContent.appendChild(document.createElement('h1').appendChild(document.createTextNode(score[i])));
    }

    $('#endModal').modal({backdrop: 'static', keyboard: false});

    endModal.style.display = "block";

    //unsub from all
    drawSubscription.unsubscribe();
    messageReceivedSubscription.unsubscribe();
    changeGuessSubscription.unsubscribe();
    endSubscription.unsubscribe();
    scoreSubscription.unsubscribe();
    resetCanvasSubscription.unsubscribe();
}
    
function onScore(payload) {
    let message = JSON.parse(payload.body);

    //clear previous user list
    userList.innerHTML = "";

    let users = message.usersScore;

    for (let i = 0; i < users.length; i++) {

        let messageElement = document.createElement('li');
        let usernameElement = document.createElement('span');
        let usernameText = document.createTextNode(" " + users[i]);
        let fontAwesome = document.createElement("i");

        usernameElement.appendChild(usernameText);
        fontAwesome.appendChild(usernameElement);
        messageElement.appendChild(fontAwesome);
        userList.appendChild(messageElement);
    }
}

function onTimer() {
    clearInterval(gameInterval);
}

function onClearCanvas() {
    context.clearRect(0, 0, canvas.width, canvas.height);
}