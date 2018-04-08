var webSocket = new WebSocket("ws://localhost:8080/Pictophone/socketHandler");

webSocket.onopen = function(message){ onOpen(message)};
webSocket.onmessage = function(message){ onMessage(message)};
webSocket.onclose = function(message){ onClose(message)};
webSocket.onerror = function(message){ onError(message);};

function onOpen(message){
	console.log("Connected ... \n");
}

function sendMessage(message){
	message.id = getCookie("id");
	message.name = getCookie("name");
	
	webSocket.send(JSON.stringify(message));
	console.log("Message sent to server : " + JSON.stringify(message) + "\n");
}

function closeConnection(){
	webSocket.close();
}

function onMessage(message){
	console.log("Message received: " + message.data);
	
	var json = JSON.parse(message.data);
	
	if (json.id != null){
		setCookie("id", json.id);
	}
	
	if (json.name != null){
		setCookie("name", json.name);
	}
	
	console.log("Message received from server :");
	console.log(json);
	
	if (json.type == "newPage"){
		document.getElementById("frame").innerHTML = json.body;
	}
}

function onClose(message){
	console.log("Disconnected ... \n");
}

function onError(message){
	console.log("Error ... \n");
}

function getPage(){
	var message = {};
	
	message.type = "getPage";
	
	sendMessage(message);
}

function buildMessage(){
	
}

function setCookie(name, value){
	document.cookie = name + "=" + value;
}

function getCookie(name){
	var cookies = decodeURIComponent(document.cookie).split(";");
	for (var i = 0; i < cookies.length; i++){
		var cookie = cookies[i];
		while (cookie.charAt(0) == ' '){
			cookie = cookie.substring(1);
		}
		
		if (cookie.indexOf(name + "=") == 0){
			return cookie.substring(name.length + 1, cookie.length);
		}
	}
	
	return "null";
}