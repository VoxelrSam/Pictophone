/**
 * Used for all the general functions that make the page dynamic and interactive
 */

/** 
 * Extend JQuery for Animate.css use
 * Taken from the Animate.css GitHub
 * https://github.com/daneden/animate.css/blob/master/README.md
 */
$.fn.extend({
  animateCss: function(animationName, callback) {
    var animationEnd = (function(el) {
      var animations = {
        animation: 'animationend',
        OAnimation: 'oAnimationEnd',
        MozAnimation: 'mozAnimationEnd',
        WebkitAnimation: 'webkitAnimationEnd'
      };

      for (var t in animations) {
        if (el.style[t] !== undefined) {
          return animations[t];
        }
      }
    })(document.createElement('div'));
	
    $(this).addClass('animated ' + animationName).one(animationEnd, function() {
      $(this).removeClass('animated ' + animationName);

      if (typeof callback === 'function') callback();
	  else console.log("no");
    });

    return this;
  },
});

// Used to keep track of individual notifications
var notificationCounter = 0;

/**
 * Displays a little notification in the corner
 *
 * @param type The type of Bootstrap alert to be displayed
 * @param mesage The message to be displayed
 */
function notify(type, message){
	var id = "notification-" + notificationCounter++;
	
	// Insert the notification
	document.getElementById("alertFrame").innerHTML +=
		"<div class=\"alert alert-" + type + "\" role=alert id=\"" + id + "\">" +
			message + 
			"<button type='button' class='close' data-dismiss='alert' aria-label='Close'>" +
				"<span aria-hidden='true'>&times;</span>" +
			"</button>" +
		"</div>";
		
		$("#" + id).animateCss('zoomInUp');
		
	// Delete the notification after a while
	window.setTimeout(function(){
		$("#" + id).animateCss('zoomOut', function(){
			$("#" + id).remove();
		});
	}, 5000);
}

/**
 * Handles a new card being sent in by the server
 * Fills in the proper info, themes it, and then animates it in
 */
function newCard(json){
	// Insert Card
	document.getElementById("frame").innerHTML += json.body;
	
	// Fill in the information
	populatePage(json);
	
	// Theme and animate if we need to swap cards
	var cards = document.getElementsByClassName("card");
	if (cards.length == 2){
		theme(cards[1]);
		transitionCards(cards);
	} else {
		theme(cards[0]);
	}
}

/**
 * Fills in the info on the page given by the server
 */
function populatePage(json){
	if (json.key != null && document.getElementById("key") != null)
		document.getElementById("key").innerHTML = json.key;
	
	if (json.roomName != null && document.getElementById("roomName") != null)
		document.getElementById("roomName").innerHTML = json.roomName;
	
	if (json.prompt != null && document.getElementById("prompt") != null)
		document.getElementById("prompt").innerHTML = json.prompt;
	
	if (json.timeline != null && document.getElementById("timeline") != null)
		document.getElementById("timeline").innerHTML = json.timeline;
}

/**
 * Gives a psuedo-random theme to each card passed in
 */
function theme(card){
	var css;
	var themeNumber = Math.floor(Math.random() * 5);
	
	switch (themeNumber){
		case 0:
			css = {background:"Seashell",color:"black"};
			break;
		case 1:
			css = {background:"palegreen",color:"black"};
			break;
		case 2: 
			css = {background:"lightblue",color:"black"};
			break;
		case 3: 
			css = {background:"lemonchiffon",color:"black"};
			break;
		case 4:
			css = {background:"wheat",color:"black"};
			break;
		default:
			console.log("Theme Error " + themeNumber);
	}
	
	$(card).css(css);
}

/**
 * Transitions two cards
 * The old one is deleted at the end
 */
function transitionCards(cards){
	// Pick a random animation
	var animation = Math.floor(Math.random() * 8);
	
	// If animating the old card
	if (animation <= 3){
		$(cards[1]).css("z-index", "0");
		$(cards[0]).css("z-index", "1");
		
		var name;
		switch(animation){
			case 0:
				name = "rollOut";
				break;
			case 1:
				name = "slideOutUp";
				break;
			case 2: 
				name = "slideOutRight";
				break;
			case 3:
				name = "flipOutX";
		}
		
		// Transition out card one
		$(cards[0]).animateCss(name, function(){
			$(cards[0]).remove();
		});
	} else {
		// animating the new card
		
		$(cards[0]).css("z-index", "0");
		$(cards[1]).css("z-index", "1");
		
		var name;
		switch(animation){
			case 4:
				name = "rollIn";
				break;
			case 5:
				name = "jackInTheBox";
				break;
			case 6: 
				name = "slideInLeft";
				break;
			case 7:
				name = "bounceInDown";
		}
		
		// Transition out card one
		$(cards[1]).animateCss(name, function(){
			$(cards[0]).remove();
		});
	}
}

/**
 * Validate and send form for creating a room
 */
function createRoom(){
	var message = {};
	message.type = "createRoom";
	message.username = document.getElementById("username").value;
	message.roomname = document.getElementById("roomname").value;
	
	if (message.username.length == 0){
		notify("warning", "Please specify a username");
		return;
	}
	
	if (message.roomname.length == 0){
		notify("warning", "Please specify a room name");
		return;
	}
	
	var roomsize = document.getElementById("roomsize").value;
	if (isNaN(roomsize) || roomsize > 10 || roomsize < 3){
		notify("warning", "Please select a valid room size");
		return;
	}
		
	message.roomsize = roomsize;
	
	sendMessage(message);
}

/**
 * Validate and send form for joining a room
 */
function joinRoom(){
	var message = {};
	message.type = "joinRoom";
	message.username = document.getElementById("username").value;
	message.roomkey = document.getElementById("roomkey").value;
	
	if (message.username.length == 0){
		notify("warning", "Please specify a username");
		return;
	}
	
	if (isNaN(message.roomkey) || message.roomkey.toString().length != 5){
		notify("warning", "Please specify a valid room key. Room keys consist of 5 numbers.");
		return;
	}
	
	sendMessage(message);
}

/**
 * Validate and send form for submiting a prompt
 */
function submitPrompt(){
	var message = {};
	message.type = "submitPrompt";
	message.prompt = document.getElementById("promptInput").value;
	
	sendMessage(message);
}

/**
 * Send newly created drawing
 */
function submitDrawing(){
	var message = {};
	message.type = "submitDrawing";
	
	sendMessage(message);
}