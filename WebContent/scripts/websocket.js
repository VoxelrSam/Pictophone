/**
 * Used for communicating to the server
 * 
 * @author Samuel Ingram
 */

// WebSocket to be used
let webSocket;

/**
 * Connect the WebSocket to the server (called by body's onLoad function)
 */
function connect(){
	let url;
	if (window.location.protocol === "https:"){
		url = "wss://";
	} else {
		url = "ws://";
	}
	
	// The url will be different on AWS
	if (window.location.hostname.indexOf("localhost") == -1)
		url += window.location.hostname + "/socketHandler";
	else 
		url += window.location.hostname + ":8080/Pictophone/socketHandler";
	
	webSocket = new WebSocket(url);

	webSocket.onopen = function(message){ onOpen(message)};
	webSocket.onmessage = function(message){ onMessage(message)};
	webSocket.onclose = function(message){ onClose(message)};
	webSocket.onerror = function(message){ onError(message);};
	
	window.setInterval(function(){sendMessage({"type": "ping"})}, 10000);
}

/**
 * When a connection is opened
 * 
 * @param message The message that is received. Can be used, but is not being used....
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
}

/**
 * Close the connection (shouldn't actually be used by the client)
 */
function closeConnection(){
	webSocket.close();
}

/**
 * Handle incoming messages from the server
 * 
 * @param message A JSON message from the server to be handled by the client
 */
function onMessage(message){
	// Parse message into JSON object
	let json = JSON.parse(message.data);
	
	if (json.type == "pong")
		return;
	
	// Store User data in sessionStorage
	sessionStorage["id"] = json.id;
	sessionStorage["name"] = json.name;
	sessionStorage["nameColor"] = json.nameColor;
	sessionStorage["gameKey"] = json.gameKey;
	
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
 * 
 * @param message The message that is received. Can be used, but is not being used....
 */
function onClose(message){
	console.log(message);
	notify("danger", "Connection lost.... Refresh the page.");
}

/**
 * When an error occurs with the socket
 * 
 * @param message The message that is received. Can be used, but is not being used....
 */
function onError(message){
	console.log("Error... \n");
}

/**
 * Retrieve required page from the server on initial connection or refresh
 */
function getPage(){
	let message = {};
	
	message.type = "getPage";
	
	sendMessage(message);
}