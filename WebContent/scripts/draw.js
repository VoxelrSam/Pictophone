/**
 * Used for drawing capabilities
 * 
 * @author Samuel Ingram
 */

// Drawing script adapted from William Mallone's tutorial
// http://www.williammalone.com/articles/create-html5-canvas-javascript-drawing-app/#demo-complete

let canvas;
let context;

let paint = false;
let eraser = false;
let color = "#0000ff";
let clickX = new Array();
let clickY = new Array();
let clickDrag = new Array();
let clickColor = new Array();

/**
 * Initialize all the listeners needed for drawing
 */
function initDrawer(){
	$("#marker").click(function(){
		setEraser(false);
		$(this).css({"background": "lightgrey"});
		$("#eraser").css({"background": "white"});
	});
	
	$("#marker").css({"background": "lightgrey"});
	
	$("#eraser").click(function(){
		setEraser(true);
		$(this).css({"background": "lightgrey"});
		$("#marker").css({"background": "white"});
	});
	
	$("#clear").click(function(){
		clear();
	});

	canvas = document.getElementById("canvas");
	context = canvas.getContext("2d");
	
	window.addEventListener('resize', resizeCanvas, false);
	resizeCanvas();
	
	$("#canvas").mousedown(function(e){
		window.getSelection().removeAllRanges();
		
		let bodyRect = document.body.getBoundingClientRect(),
		canvasRect = this.getBoundingClientRect(),
		offsetT = canvasRect.top - bodyRect.top,
		offsetL = canvasRect.left - bodyRect.left;

		let mouseX = e.pageX - offsetL;
		let mouseY = e.pageY - offsetT;
		
		paint = true;
		addClick(mouseX, mouseY);
		redraw();
	});

	$("#canvas").mousemove(function(e){
		if (paint){
			let bodyRect = document.body.getBoundingClientRect(),
			canvasRect = this.getBoundingClientRect(),
			offsetT = canvasRect.top - bodyRect.top,
			offsetL = canvasRect.left - bodyRect.left;
		
			addClick(e.pageX - offsetL, e.pageY - offsetT, true);
			redraw();
		}
	});

	$("#canvas").mouseup(function(e){
		paint = false;
	});

	$("#canvas").mouseleave(function(e){
		paint = false;
	});
}

/**
 * Record a draw point
 * 
 * @param x The x coordinate of the point
 * @param y The y coordinate of the point
 * @param draggin A boolean indicating whether or not the mouse is being dragged
 */
function addClick(x, y, dragging){
	clickX.push(x);
	clickY.push(y);
	clickDrag.push(dragging);
	
	// Just record a white point if the eraser is being used
	if (!eraser)
		clickColor.push(color);
	else 
		clickColor.push("#ffffff");
}

/**
 * Redraw the canvas
 */
function redraw(){
	// Clear it first
	context.clearRect(0, 0, context.canvas.width, context.canvas.height);
	
	context.strokeStyle = color;
	context.lineJoin = "round";
	context.lineWidth = 5;
	
	for(let i = 0; i < clickX.length; i++){
		context.beginPath();
		if (clickDrag[i] && i){
			context.moveTo(clickX[i - 1], clickY[i - 1]);
		} else {
			context.moveTo(clickX[i] - 1, clickY[i]);
		}
		
		context.lineTo(clickX[i], clickY[i]);
		context.closePath();
		context.strokeStyle = clickColor[i];
		context.stroke();
	}
}

/**
 * Clear the entire drawing when the clear button is pushed.
 */
function clear(){
	if (!confirm("Are you sure you want to clear the drawing?"))
		return;

	context.clearRect(0, 0, context.canvas.width, context.canvas.height);
	clickX = new Array();
	clickY = new Array();
	clickDrag = new Array();
	clickColor = new Array();
}

/**
 * Resize the canvas to handle the various resolutions it can be
 */
function resizeCanvas(){
	let container = document.getElementById("canvasContainer");
	
	canvas.width = $(container).width();
	canvas.height = $(container).height();
	
	redraw();
}

/**
 * Set the color of the brush
 *
 * @param jscolor The color to change to as chosen with jscolor
 */
function setColor(jscolor){
	color = "#" + jscolor;
}

/**
 * Turn the eraser tool on and off
 *
 * @param value A boolean representing whether to turn it on or off
 */
function setEraser(value){
	eraser = value;
}

/**
 * Initializes the jscolor code. Useful when elements are dynamically inserted
 */
function initJSColor(){
	jscolor.installByClassName('jscolor');
}