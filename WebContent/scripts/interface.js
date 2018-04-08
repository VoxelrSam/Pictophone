// Extend JQuery for Animate.css use
$.fn.extend({
  animateCss: function(animationName, callback) {
    var animationEnd = (function(el) {
      var animations = {
        animation: 'animationend',
        OAnimation: 'oAnimationEnd',
        MozAnimation: 'mozAnimationEnd',
        WebkitAnimation: 'webkitAnimationEnd',
      };

      for (var t in animations) {
        if (el.style[t] !== undefined) {
          return animations[t];
        }
      }
    })(document.createElement('div'));

    this.addClass('animated ' + animationName).one(animationEnd, function() {
      $(this).removeClass('animated ' + animationName);

      if (typeof callback === 'function') callback();
    });

    return this;
  },
});

var notificationCounter = 0;

function notify(type, message){
	var id = "notification-" + notificationCounter++;
	
	document.getElementById("alertFrame").innerHTML +=
		"<div class=\"alert alert-" + type + "\" role=alert id=\"" + id + "\">" +
			message + 
			"<button type='button' class='close' data-dismiss='alert' aria-label='Close'>" +
				"<span aria-hidden='true'>&times;</span>" +
			"</button>" +
		"</div>";
		
		$("#" + id).animateCss('zoomInUp');
		
	window.setTimeout(function(){
		$("#" + id).animateCss('zoomOut', function(){
			$("#" + id).remove();
		});
	}, 5000);
}

function newCard(json){
	document.getElementById("frame").innerHTML += json.body;
	
	populatePage(json);
	
	var cards = document.getElementsByClassName("card");
	if (cards.length == 2)
		transitionCards(cards);
}

function populatePage(json){
	if (json.key != null && document.getElementById("key") != null)
		document.getElementById("key").innerHTML = json.key;
	
	if (json.roomName != null && document.getElementById("roomName") != null)
		document.getElementById("roomName").innerHTML = json.roomName;
}

function transitionCards(cards){
	var animation = Math.floor(Math.random() * 8);
	
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
		notify("warning", "Please specify a roomname");
		return;
	}
	
	var roomsize = document.getElementById("roomsize").value;
	if (isNaN(roomsize) || roomsize > 10 || roomsize < 3){
		notify("warning", "Please select a valid roomsize");
		return;
	}
		
	message.roomsize = roomsize;
	
	sendMessage(message);
}

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