/**
 * Used for validating and submitting forms
 * 
 * @author Samuel Ingram
 */
 
// Used for keeping track of the new name color chosen in the user edit page
 let newNameColor;

/**
 * Validate and send form for creating a room
 */
function createRoom(){
	let message = {};
	message.type = "createRoom";
	
	if (document.getElementById("username") != null)
		message.username = document.getElementById("username").value;
	else
		message.username = sessionStorage["name"];
		
	message.roomname = document.getElementById("roomname").value;
	message.roomsize = document.getElementById("roomsize").value;
	message.roomtype = document.getElementById("roomtype").value;
	
	if (message.username.length == 0){
		notify("warning", "Please specify a username");
		return;
	}
	
	if (message.username.length > 16){
		notify("warning", "Please keep usernames under 16 characters in length");
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
	let message = {};
	message.type = "joinRoom";
	
	if (document.getElementById("username") != null)
		message.username = document.getElementById("username").value;
	else
		message.username = sessionStorage["name"];
		
	if (message.username.length > 16){
		notify("warning", "Please keep usernames under 16 characters in length");
		return;
	}
	
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
	let message = {};
	message.type = "joinRoom";
	message.roomkey = document.getElementById("roomkey").value;
	
	if (document.getElementById("username") != null)
		message.username = document.getElementById("username").value;
	else
		message.username = sessionStorage["name"];
		
	if (message.username.length > 16){
		notify("warning", "Please keep usernames under 16 characters in length");
		return;
	}
	
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
	let message = {};
	message.type = "submitPrompt";
	message.prompt = document.getElementById("promptInput").value;
	
	window.clearInterval(timer);
	
	sendMessage(message);
}

/**
 * Send newly created drawing
 */
function submitDrawing(){
	let message = {};
	message.type = "submitDrawing";
	message.image = canvas.toDataURL('image/png');
	
	context.clearRect(0, 0, context.canvas.width, context.canvas.height);
	clickX = new Array();
	clickY = new Array();
	clickDrag = new Array();
	clickColor = new Array();
	
	window.clearInterval(timer);
	
	sendMessage(message);
}

/**
 * Send login information
 */
function login(){
	let message = {};
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

/**
 * Send signup information
 */
function signup(){
	let message = {};
	message.type = "signup";
	message.username = document.getElementById("signupUsername").value;
	message.password = document.getElementById("signupPassword").value;
	
	if (message.username.length == 0){
		notify("warning", "Please specify a username");
		return;
	}
	
	if (message.username.length > 16){
		notify("warning", "Please keep usernames under 16 characters in length");
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

/**
 * Send changed information from the edit user page
 */
function saveUser(){
	let message = {};
	message.type = "saveUser";
	
	if (document.getElementById("name").value != sessionStorage["name"]){
		message.username = document.getElementById("name").value;
		
		if (message.username.length == 0){
			notify("warning", "Please specify a username");
			return;
		}
		
		if (message.username.length > 16){
			notify("warning", "Please keep usernames under 16 characters in length");
			return;
		}
	}
	
	if (document.getElementById("password").value.length != 0){
		message.password = document.getElementById("password").value;
		
		if (message.password != document.getElementById("confirmPassword").value){
			notify("warning", "Passwords do not match");
			return;
		}
	}
	
	if (newNameColor.length != 0)
		message.nameColor = newNameColor;
	
	if (color.length != 0)
		message.defaultColor = color;
	
	sendMessage(message);
}