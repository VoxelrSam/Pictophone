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
public class Handler {
	
	@OnOpen
	public void onOpen(Session session) {
		System.out.println("Connection Found!");
	}
	 
	@OnClose
	public void onClose(CloseReason c, Session session) {
		System.out.println("Connection Ended: " + c.getReasonPhrase());
		
		// TODO: Add delete timer for users
		
	}
	 
	@OnMessage
	public void onMessage(String message, Session session) {
		RequestHandler.handleRequest(message, session);
	}
	 
	@OnError
	public void onError(Throwable e) {
		e.printStackTrace();
	}
}
