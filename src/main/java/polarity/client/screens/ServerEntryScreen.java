package polarity.client.screens;

import com.jme3.input.event.KeyInputEvent;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import com.jme3.scene.Node;
import polarity.client.main.GameClient;
import polarity.shared.input.Bind;
import polarity.shared.input.InputHandler;
import polarity.shared.screens.Screen;
import polarity.shared.tools.Sys;
import polarity.shared.tools.Util;
import polarity.shared.ui.Button;
import polarity.shared.ui.UIElement;

/**
 *
 * @author Sindusk
 */
public class ServerEntryScreen extends Screen {
    protected Button addButton;
    protected Button backButton;
    
    public ServerEntryScreen(GameClient app, Node rootNode, Node guiNode){
        super(app, rootNode, guiNode);
        name = "ServerEntryScreen";
    }

    @Override
    public void initialize(final InputHandler inputHandler) {
        Util.log("[ServerEntryScreen] Initializing...", 1);
        this.inputHandler = inputHandler;
        float width = Sys.width;
        float height = Sys.height;
        
        //Makes addButton
        addButton = new Button(gui, new Vector2f(width*0.4f, height*0.3f), width*0.3f, height*0.05f, 0){
            @Override
            public void onAction(Vector2f cursorLoc, String bind, boolean down, float tpf){
                if(bind.equals(Bind.LClick.toString()) && down){
                    //Does nothing yet should add server
                }
            }
        };
        addButton.setColor(new ColorRGBA(0.5f, 0.5f, 0.5f, 1));
        addButton.setText("Add");
        
        //Makes back button
        backButton = new Button(gui, new Vector2f(width*0.4f, height*0.2f), width*0.3f, height*0.05f, 0){
            @Override
            public void onAction(Vector2f cursorLoc, String bind, boolean down, float tpf){
                //Click mouse goes back
                if(bind.equals(Bind.LClick.toString()) && down){
                    inputHandler.switchScreens(new MultiplayerScreen((GameClient)app, root.getParent(), gui.getParent()));
                }
            }
        };
        //Colors back button
        backButton.setColor(new ColorRGBA(0.8f, 0f, 0f, 1));
        backButton.setText("Back");
        ui.add(backButton);
    }
    
    @Override
    public void changeInit(){
        //
    }
    
    @Override
    public void update(float tpf) {
        //
    }

    @Override
    public void onCursorMove(Vector2f cursorLoc) {
        //
    }

    @Override
    public void onAction(Vector2f cursorLoc, String bind, boolean down, float tpf) {
        UIElement e = checkUI(cursorLoc);
        if(e != null){
            e.onAction(cursorLoc, bind, down, tpf);
        }
        
        // Default bind back to menu screen
        if(bind.equals(Bind.Escape.toString())){
            inputHandler.switchScreens(new MenuScreen((GameClient)app, root.getParent(), gui.getParent()));
        }
    }

    @Override
    public void onKeyEvent(KeyInputEvent evt) {
        //
    }
    
}
