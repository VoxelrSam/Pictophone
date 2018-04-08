var webSocket = new WebSocket("ws://localhost:8080/Pictophone/socketHandler");

webSocket.onopen = function(message){ onOpen(message)};
webSocket.onmessage = function(message){ onMessage(message)};
webSocket.onclose = function(message){ onClose(message)};
webSocket.onerror = function(message){ onError(message);};

function onOpen(message){
	console.log("Connected ... \n");
	getPage();
}

function sendMessage(message){
	message.id = sessionStorage["id"];
	message.name = sessionStorage["name"];
	
	webSocket.send(JSON.stringify(message));
	console.log("Message sent to server:");
	console.log(message);
}

function closeConnection(){
	webSocket.close();
}

function onMessage(message){
	var json = JSON.parse(message.data);
	
	if (json.id != null){
		sessionStorage["id"] = json.id;
	}
	
	if (json.name != null){
		sessionStorage["name"] = json.name;
	}
	
	console.log("Message received from server :");
	console.log(json);
	
	if (json.type == "newPage"){
		newCard(json);
	} else if (json.type == "roomNotFound"){
		notify("warning", "Room not found with that key. Try another.");
	} else if (json.type == "roomNotOpen"){
		notify("warning", "This room is not accepting users at the moment. Try another.");
	}
}

function onClose(message){
	console.log("Disconnected ... \n");
	notify("Danger", "Connection lost. The Server may have went down...");
}

function onError(message){
	console.log("Error ... \n");
}

function getPage(){
	var message = {};
	
	message.type = "getPage";
	
	sendMessage(message);
}