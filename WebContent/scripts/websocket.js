/**
 * Used for communicating to the server
 */

// WebSocket to be used
var webSocket;

/**
 * Connect the WebSocket to the server (called by body's onLoad function)
 */
function connect(){
	webSocket = new WebSocket("ws://localhost:8080/Pictophone/socketHandler");

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
	sessionStorage["gameKey"] = json.gameKey;
	
	console.log("Message received from server :");
	console.log(json);
	
	// Handle message type
	if (json.type == "newPage"){
		newCard(json);
	} else if (json.type == "roomNotFound"){
		notify("warning", "Room not found with that key. Try another.");
	} else if (json.type == "roomNotOpen"){
		notify("warning", "This room is not accepting users at the moment. Try another.");
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