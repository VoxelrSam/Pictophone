import javax.websocket.server.ServerEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;

/**
 * Class used to handle WebSocket connections
 * 
 * @author Samuel Ingram
 */
@ServerEndpoint("/socketHandler")
public class handler {
	@OnOpen
	public void onOpen() {
		System.out.println("Connection Found!");
	}
	 
	@OnClose
	public void onClose(CloseReason c) {
		System.out.println("Connection Ended: " + c.getReasonPhrase());
		// TODO: Delete temp users and add delete timer for logged users
	}
	 
	@OnMessage
	public void onMessage(String message, Session session) {
		System.out.println("\n--------------------\n" + message);
		RequestHandler.handleRequest(message, session);
	}
	 
	@OnError
	public void onError(Throwable e) {
		e.printStackTrace();
	}
}
