package polarity.client.hud.advanced;

import com.jme3.input.KeyInput;
import com.jme3.input.event.KeyInputEvent;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import polarity.shared.hud.HUDElement;
import polarity.shared.tools.GeoFactory;
import polarity.shared.tools.SinText;

import java.util.ArrayList;

/**
 *
 * @author SinisteRing
 */
public class ChatBox extends HUDElement {
    protected ArrayList<String> messages = new ArrayList<>();
    protected ArrayList<SinText> lines = new ArrayList<>();
    protected Vector2f size;
    protected Node lineNode = new Node("lineNode");
    protected Geometry background;
    protected Geometry typingBackground;
    protected SinText typingArea;
    protected boolean active = false;
    
    public ChatBox(Node parent, Vector2f loc, Vector2f size){
        super(parent, loc);
        this.size = size;
        background = GeoFactory.createBox(node, new Vector3f(size.x, size.y, 0), Vector3f.ZERO, new ColorRGBA(0.3f, 0.3f, 0.3f, 0.3f));
        node.attachChild(lineNode);
        typingArea = GeoFactory.createSinText(node, 20, new Vector3f(-size.x, -size.y-10, 1), "ES32", new ColorRGBA(1, 0.7f, 0, 1), SinText.Alignment.Left);
        typingBackground = GeoFactory.createBox(node, new Vector3f(size.x, 10, 0), new Vector3f(0, -size.y-10, 0), new ColorRGBA(0.1f, 0.1f, 0.1f, 0.5f));
    }
    
    public boolean isActive(){
        return active;
    }
    private void addCharToMessage(String currentMessage, char c){
        String newMessage = currentMessage.substring(0, currentMessage.length()-1);
        typingArea.setText(newMessage+c+"|");
    }
    public void addToMessage(KeyInputEvent evt){
        String currentMessage = typingArea.getText();
        if(!Character.isISOControl(evt.getKeyChar())){
            addCharToMessage(currentMessage, evt.getKeyChar());
        }else if(KeyInput.KEY_SPACE == evt.getKeyCode() && evt.isPressed()){
            addCharToMessage(currentMessage, ' ');
        }else if(KeyInput.KEY_BACK == evt.getKeyCode() && evt.isPressed() && currentMessage.length() >= 2){
            String newMessage = currentMessage.substring(0, currentMessage.length()-2);
            typingArea.setText(newMessage+"|");
        }
    }
    
    public String getNewMessage(){
        return typingArea.getText().substring(0, typingArea.getText().length()-1).trim();
    }
    public void startNewMessage(){
        typingArea.setText("|");
        active = true;
    }
    public void endMessage(){
        typingArea.setText(" ");
        active = false;
    }
    
    public void display(int rows){
        int start = (messages.size()-rows);
        if(start < 0){
            start = 0;
        }
        lineNode.detachAllChildren();
        float rowSize = (size.y/rows)*2;   // Amount of space between each row
        int i = messages.size()-1;
        int counter = 0;
        while(i >= start){
            float yLoc = -size.y+(counter*rowSize)+rowSize/2f;
            SinText text = GeoFactory.createSinText(lineNode, size.y/rows*2, new Vector3f(-size.x, yLoc, 0), "ES32", messages.get(i),
                    ColorRGBA.Orange, SinText.Alignment.Left);
            lines.add(text);
            i--;
            counter++;
        }
    }
    
    public void addMessage(String message){
        messages.add(message);
        display(13);
    }
}
