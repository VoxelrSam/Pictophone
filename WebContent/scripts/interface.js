/**
 * Used for all the general functions that make the page dynamic and interactive
 * 
 * @author Samuel Ingram
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
    });

    return this;
  },
});

// Used to keep track of individual notifications
var notificationCounter = 0;

// Keep track of the last theme used as to not repeat them back to back
var lastTheme = 10;

// Timer variable used for stopping the timer
var timer;

// Keeps track of the game selected in the Public Games list
var selectedGame;

// Keeps track of the complement color for the current scheme
var complement;

// Keeps track of if the web page is in focus
var isInFocus = true;

window.onblur = function(){
	isInFocus = false;
}

window.onfocus = function(){
	isInFocus = true;
}

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
		"<div class=\"alert alert-" + type + "\" role='alert' id=\"" + id + "\">" +
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
 * 
 * @param json The json message that contains the new card
 */
function newCard(json){
	// Insert Card
	document.getElementById("frame").innerHTML += json.body;
	
	// Init drawer if the canvas is present
	if (document.getElementById("canvas") != null)
		initDrawer();
		
	if (document.getElementsByClassName("jscolor") != null)
		initJSColor();
	
	// Theme and animate if we need to swap cards
	var cards = document.getElementsByClassName("card");
	if (cards.length == 2){
		populatePage(json, cards[1]);
		theme(cards[1]);
		
		if ($(cards[1]).find("#timer")[0] != null)
			startTimer(60, null);
		
		transitionCards(cards);
	} else {
		populatePage(json, cards[0]);
		
		if ($(cards[0]).find("#timer")[0] != null)
			continueTimer();
		
		theme(cards[0]);
	}
}

/**
 * Fills in the info on the page given by the server
 * 
 * @param json The json message that was sent that probably contains info to populate the page with
 * @param card The card to populate
 */
function populatePage(json, card){
	if (json.key != null && $(card).find("#key")[0] != null)
		$(card).find("#key")[0].innerHTML = "\"" + json.key + "\"";
	
	if (json.roomName != null && $(card).find("#roomName")[0] != null)
		$(card).find("#roomName")[0].innerHTML = json.roomName;
	
	if (json.prompt != null && $(card).find("#prompt")[0] != null)
		$(card).find("#prompt")[0].innerHTML = "\"" + json.prompt + "\"";
	
	if (json.timeline != null && $(card).find("#timeline")[0] != null)
		buildTimeline(JSON.parse(json.timeline), JSON.parse(json.users));
		
	if (json.image != null && document.getElementsByClassName("drawing") != null)
		document.getElementsByClassName("drawing")[0].src = json.image;
		
	if (sessionStorage["name"] !== "null" && document.getElementsByClassName("identifier") != null){
		var ids = document.getElementsByClassName("identifier");
		
		if ((json.stage == "init" || json.stage == "createRoomForm" || json.stage == "joinRoom") && json.isLoggedIn){
			ids[ids.length - 1].innerHTML = "<div class=\"dropdown\">" + 
												"<a class=\"btn btn-secondary dropdown-toggle\" href=\"#\" id=\"idDropdown\" data-toggle=\"dropdown\" aria-haspopup=\"true\" aria-expanded=\"false\">" +
													"<span>" + sessionStorage["name"] + "</span>" +
												"</a>" + 
												"<div class=\"dropdown-menu\" aria-labelledby=\"idDropdown\">" +
													"<a class=\"dropdown-item\" href=\"#\" onClick=\"sendMessage({'type': 'editUser'})\">" + "Edit User" + "</a>" +
													"<a class=\"dropdown-item\" href=\"#\" onClick=\"sendMessage({'type': 'logout'})\">" + "Logout" + "</a>" +
												"</div>" +
											"</div>";
		} else {
			if (!(json.stage == "init" || json.stage == "createRoomForm" || json.stage == "joinRoom"))
				ids[ids.length - 1].innerHTML = "<span>" + sessionStorage["name"] + "</span>";
		}
		$(ids[ids.length - 1]).find("span").css({"color": json.nameColor});
	}
	
	if (json.users != null)
		updateUsers(json);
		
	if (json.games != null)
		updateGameList(JSON.parse(json.games));
		
	if (json.isLoggedIn && document.getElementById("username") != null){
		$("#username").remove();
	}
	
	if (!json.isLoggedIn && document.getElementById("username") != null && sessionStorage["name"] != "null"){
		document.getElementById("username").value = sessionStorage["name"];
	}
	
	if (json.stage == "editUser"){
		document.getElementById("name").value = json.name;
		$("#nameColor").css({"background-color": json.nameColor});
		$("#brushColor").css({"background-color": JSON.parse(json.info).defaultColor});
		document.getElementById("gameCount").innerHTML = JSON.parse(json.info).gamesPlayed;
		
		newNameColor = "";
		color = "";
	}
}

/**
 * Build out the representation for the timeline (Used at game end)
 * 
 * @param timeline The array of strings representing the timeline
 * @param users The names of the users in the order that they responded
 */
function buildTimeline(timeline, users){
	var div = document.getElementById("timeline");

	for (var i = 0; i < timeline.length; i++){
		var userDetails = users[i].split(";");
		var name = "<span style=\"color:" + userDetails[1] + ";\">" + userDetails[0] + "</span>";
		
		if (i % 2 == 0){
			div.innerHTML +=
				"<h2>" + name + " said</h2>" +
				"<h3>\"" + timeline[i] + "\"</h3>" 
				"<br/>";
		} else {
			div.innerHTML +=
				"<h2>" + name + " drew</h2>" +
				"<img class=\"drawing\" src=\"" + timeline[i] + "\"/>" +
				"<br/>";
		}
		
		if (i != timeline.length - 1)
			div.innerHTML += "<div class=\"line\"></div>"
	}
}

/**
 * Gives a psuedo-random theme to each card passed in
 * 
 * @param card The card to theme
 */
function theme(card){
	var css;
	var themeNumber = Math.floor(Math.random() * 5);
	
	if (themeNumber == lastTheme)
		themeNumber = (themeNumber + 1) % 5;
		
	lastTheme = themeNumber;
	
	switch (themeNumber){
		case 0:
			css = {background:"#ffddd3",color:"black"};
			complement = "#d3fffb";
			break;
		case 1:
			css = {background:"palegreen",color:"black"};
			complement = "#fa98fa";
			break;
		case 2: 
			css = {background:"lightblue",color:"black"};
			complement = "#e6baac";
			break;
		case 3: 
			css = {background:"lemonchiffon",color:"black"};
			complement = "#ccd1ff";
			break;
		case 4:
			css = {background:"wheat",color:"black"};
			complement = "#b3caf5";
			break;
		default:
			console.log("Theme Error " + themeNumber);
	}
	
	$(card).css(css);
	$(card).find("h1").css({"color": complement});
	
	var inverse = getInverse($(card).css("background-color"));
	
	$(card).find(".btn").css({
		"color": inverse,
		"border-color": inverse,
		"background-color": "transparent"
	});
	
	var background;
	var color;
	$(card).find(".btn").hover(function(){
		if (this.style.background-color != inverse){
			background = this.style.backgroundColor;
			color = this.style.color;
		}
	
		$(this).css({
			"color": "white",
			"background-color": inverse
		});
	}, function(){
		$(this).css({
			"color": color,
			"background-color": background
		});
	});
}

/**
 * Get the opposite color of the one passed in
 * 
 * @param color The color to get the inverse of
 * @return The inverse hex color
 */
function getInverse(color){
	var r = (255 - color.slice(4, 7)).toString(16);
	var g = (255 - color.slice(9, 12)).toString(16);
	var b = (255 - color.slice(14, 17)).toString(16);
	
	if (r.length == 1)
		r = '0' + r;
	if (g.length == 1)
		g = '0' + g;
	if (b.length == 1)
		b = '0' + b;
	
	return '#' + r + g + b;
}

/**
 * Transitions two cards
 * The old one is deleted at the end
 * 
 * @param cards The two cards to transition
 */
function transitionCards(cards){
	if (!isInFocus){
		$(cards[0]).remove();
		return;
	}

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
 * Start the timer on the interface
 * 
 * @param seconds The duration with which to set the timer
 */
function startTimer(seconds){
	document.getElementById("timer").innerHTML = seconds;
	sessionStorage["timer"] = seconds;

	timer = setInterval(tick, 1000);
	setTimeout(function(){$("#timer").animateCss("pulse")}, 800);
}

/**
 * Continue the timer where it left off if the user refreshes the page
 */
function continueTimer(){
	timer = setInterval(tick, 1000);
	setTimeout(function(){$("#timer").animateCss("pulse")}, 800);
}

/**
 * Called once per second via the timer.
 * Counts down and executes a submission at the end.
 */
function tick(){
	var time = sessionStorage["timer"];
	time--;
	
	sessionStorage["timer"] = time;
	
	if (time < 1){
		document.getElementById("timer").innerHTML = 0;
		window.clearInterval(timer);
		
		// End Timer Function
		if (document.getElementById("canvas") != null)
			submitDrawing();
		else
			submitPrompt();
	} else {
		document.getElementById("timer").innerHTML = time;
		setTimeout(function(){$("#timer").animateCss("pulse")}, 800);
	}
}

/**
 * Update the user list/count
 * 
 * @param json An array full of users
 */
function updateUsers(json){
	var users = JSON.parse(json.users);

	if (document.getElementById("userCount") != null){
		document.getElementById("userCount").innerHTML = users.length + "/" + json.roomsize;
	}
	
	if (document.getElementsByClassName("identifier") != null){
		var ids = document.getElementsByClassName("identifier");
		
		var names = "<span style=\"color:" + sessionStorage["nameColor"] + ";\">" + sessionStorage["name"] + "</span>";
		
		for (var i = 0; i < users.length; i++){
			var userDetails = users[i].split(";");
			if (userDetails[0] === sessionStorage["name"])
				continue;
		
			names += "<br/>" + "<span style=\"color:" + userDetails[1] + ";\">" + userDetails[0] + "</span>";
		}
		
		if (names.length != 0)
			ids[ids.length - 1].innerHTML = names;
	}
}

/**
 * Update the game list/count
 * 
 * @param list The game list to use
 */
function updateGameList(list){
	if (document.getElementById("gamesList") == null)
		return;
		
	var body = document.getElementById("gamesListBody");
	body.innerHTML = "";
	
	if (list.length == 0){
		body.innerHTML += 
			"<tr value=\"\">" + 
				"<td>No Games Available</td>" +
				"<td>:(</td>" +
			"</tr>";
	}
	
	for (var i = 0; i < list.length; i++){
		var elements = list[i].split(";");
		
		body.innerHTML += 
				"<tr value=\"" + elements[0] + "\">" + 
					"<td>" + elements[1] + "</td>" +
					"<td>" + elements[2] + "</td>" +
				"</tr>";
	}
	
	$("#gamesListBody tr").click(function(){
		$("#gamesListBody tr").each(function(){
			$(this).css({"background-color": "inherit"});
		});
		
		selectedGame = $(this).attr("value");
		$(this).css({"background-color": complement});
	});
}

/**
 * Sets the name color for user editing
 * 
 * @param color The color to set to
 */
function setNameColor(color){
	$(".identifier span").css({"color": "#" + color});
	
	newNameColor = "#" + color;
}