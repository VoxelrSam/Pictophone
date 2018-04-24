/**
 * Used for communicating to the server
 */

// WebSocket to be used
var webSocket;

/**
 * Connect the WebSocket to the server (called by body's onLoad function)
 */
function connect(){
	var url;
	if (window.location.protocol === "https:"){
		url = "wss://";
	} else {
		url = "ws://";
	}
	url += window.location.host + "/Pictophone/socketHandler";
	
	webSocket = new WebSocket(url);

	webSocket.onopen = function(message){ onOpen(message)};
	webSocket.onmessage = function(message){ onMessage(message)};
	webSocket.onclose = function(message){ onClose(message)};
	webSocket.onerror = function(message){ onError(message);};
}

/**
 * When a connection is opened
 */
function onOpen(message){
	console.log("Connected ... \n");
	getPage();
}

/**
 * Send a message to the server
 *
 * @param message A JavaScript Object representing the JSON message to be sent
 */
function sendMessage(message){
	// Populate identifier properties
	message.id = sessionStorage["id"];
	message.name = sessionStorage["name"];
	message.gameKey = sessionStorage["gameKey"];
	
	webSocket.send(JSON.stringify(message));
	console.log("Message sent to server:");
	console.log(message);
}

/**
 * Close the connection (shouldn't actually be used by the client)
 */
function closeConnection(){
	webSocket.close();
	console.log("We closed it");
}

/**
 * Handle incoming messages from the server
 */
function onMessage(message){
	// Parse message into JSON object
	var json = JSON.parse(message.data);
	
	// Store User data in sessionStorage
	sessionStorage["id"] = json.id;
	sessionStorage["name"] = json.name;
	sessionStorage["nameColor"] = json.nameColor;
	sessionStorage["gameKey"] = json.gameKey;
	
	console.log("Message received from server :");
	console.log(json);
	
	// Handle message type
	switch (json.type){
	case "newPage":
		newCard(json);
		break;
	case "warning":
		notify("warning", json.message);
		break;
	case "usersUpdate":
		updateUsers(json);
		break;
	case "gamesListUpdate":
		updateGameList(JSON.parse(json.games));
		break;
	default:
		console.log("Invalid message type specified: " + json.type);
	}
}

/**
 * When the connection is closed
 */
function onClose(message){
	console.log(message);
	notify("danger", "Connection lost. The Server may have went down...");
}

/**
 * When an error occurs with the socket
 */
function onError(message){
	console.log("Error ... \n");
}

/**
 * Retrieve required page from the server on initial connection or refresh
 */
function getPage(){
	var message = {};
	
	message.type = "getPage";
	
	sendMessage(message);
}