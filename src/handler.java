import javax.websocket.server.ServerEndpoint;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;

@ServerEndpoint("/socketHandler")
public class handler {
	 @OnOpen
	 public void onOpen() {
		 
	 }
	 
	 @OnClose
	 public void onClose() {
		 
	 }
	 
	 @OnMessage
	 public void onMessage(String message) {
		 System.out.println(message);
		 RequestHandler.handleRequest(message);
	 }
	 
	 @OnError
	 public void onError(Throwable e) {
		 e.printStackTrace();
	 }
}
