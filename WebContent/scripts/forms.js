/**
 * Validate and send form for creating a room
 */
function createRoom(){
	var message = {};
	message.type = "createRoom";
	message.username = document.getElementById("username").value;
	message.roomname = document.getElementById("roomname").value;
	message.roomsize = document.getElementById("roomsize").value;
	message.roomtype = document.getElementById("roomtype").value;
	
	if (message.username.length == 0){
		notify("warning", "Please specify a username");
		return;
	}
	
	if (message.roomname.length == 0){
		notify("warning", "Please specify a room name");
		return;
	}
	
	if (isNaN(message.roomsize) || message.roomsize > 10 || message.roomsize < 3){
		notify("warning", "Please select a valid room size");
		return;
	}
	
	if (message.roomtype === "Room Type"){
		notify("warning", "Please select a valid room type");
		return;
	}
	
	sendMessage(message);
}

/**
 * Validate and send form for joining a public room
 */
function joinPublicRoom(){
	var message = {};
	message.type = "joinRoom";
	message.username = document.getElementById("username").value;
	
	if (selectedGame == null){
		notify("warning", "Please pick a game first");
		return;
	}
	
	message.roomkey = selectedGame;
	
	if (message.username.length == 0){
		notify("warning", "Please specify a username");
		return;
	}
	
	sendMessage(message);
}

/**
 * Validate and send form for joining a public room
 */
function joinPrivateRoom(){
	var message = {};
	message.type = "joinRoom";
	message.username = document.getElementById("username").value;
	message.roomkey = document.getElementById("roomkey").value;
	
	if (message.username.length == 0){
		notify("warning", "Please specify a username");
		return;
	}
	
	sendMessage(message);
}

/**
 * Send a typed prompt
 */
function submitPrompt(){
	var message = {};
	message.type = "submitPrompt";
	message.prompt = document.getElementById("promptInput").value;
	
	window.clearInterval(timer);
	
	sendMessage(message);
}

/**
 * Send newly created drawing
 */
function submitDrawing(){
	var message = {};
	message.type = "submitDrawing";
	message.image = canvas.toDataURL('image/png');
	
	window.clearInterval(timer);
	
	sendMessage(message);
}

function login(){
	var message = {};
	message.type = "login";
	message.username = document.getElementById("loginUsername").value;
	message.password = document.getElementById("loginPassword").value;
	
	if (message.username.length == 0){
		notify("warning", "Please specify a username");
		return;
	}
	
	if (message.password.length == 0){
		notify("warning", "Please specify a password");
		return;
	}
	
	sendMessage(message);
}

function signup(){
	var message = {};
	message.type = "signup";
	message.username = document.getElementById("signupUsername").value;
	message.password = document.getElementById("signupPassword").value;
	
	if (message.username.length == 0){
		notify("warning", "Please specify a username");
		return;
	}
	
	if (message.password.length == 0){
		notify("warning", "Please specify a password");
		return;
	}
	
	if (message.password != document.getElementById("confirmPassword").value){
		notify("warning", "Passwords do not match");
		return;
	}
	
	sendMessage(message);
}