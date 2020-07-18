package polarity.client.screens;

import com.jme3.input.event.KeyInputEvent;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import input.Bind;
import input.InputHandler;
import main.GameApplication;
import screens.Screen;
import tools.Sys;
import tools.Util;
import ui.Button;
import ui.UIElement;

/**
 *
 * @author SinisteRing
 */
public class MenuScreen extends Screen {
    private Button multiButton;
    private Button optionsButton;
    private Button exitButton;
    
    public MenuScreen(GameApplication app, Node rootNode, Node guiNode){
        super(app, rootNode, guiNode);
        Util.log("[MenuScreen] Initializing New MenuScreen...", 3);
        name = "Menu Screen";
    }
    
    public void initialize(final InputHandler inputHandler){
        this.inputHandler = inputHandler;
        // Initialize camera facing and location
        app.getCamera().setLocation(new Vector3f(0, 0, 50));
        app.getCamera().lookAt(Vector3f.ZERO, Vector3f.UNIT_Y);
        float width = Sys.width;
        float height = Sys.height;
        
        // Multiplayer button
        multiButton = new Button(gui, new Vector2f(width*0.5f, height*0.7f), width*0.4f, height*0.05f, 0){
            @Override
            public void onAction(Vector2f cursorLoc, String bind, boolean down, float tpf){
                if(bind.equals(Bind.LClick.toString()) && down){
                    inputHandler.switchScreens(new MultiplayerScreen(app, root.getParent(), gui.getParent()));
                }
            }
        };
        multiButton.setColor(new ColorRGBA(0.7f, 0, 0.5f, 1));
        multiButton.setText("Multiplayer");
        ui.add(multiButton);
        
        // Options button
        optionsButton = new Button(gui, new Vector2f(width*0.5f, height*0.6f), width*0.4f, height*0.05f, 0){
            @Override
            public void onAction(Vector2f cursorLoc, String bind, boolean down, float tpf){
                if(bind.equals(Bind.LClick.toString()) && down){
                    //inputHandler.switchScreens(new MultiplayerScreen(app, root.getParent(), gui.getParent()));
                }
            }
        };
        optionsButton.setColor(new ColorRGBA(0, 0.7f, 0.5f, 1));
        optionsButton.setText("Options");
        ui.add(optionsButton);
        
        // Exit Button
        exitButton = new Button(gui, new Vector2f(width*0.5f, height*0.5f), width*0.4f, height*0.05f, 0){
            @Override
            public void onAction(Vector2f cursorLoc, String bind, boolean down, float tpf){
                if(bind.equals(Bind.LClick.toString()) && down){
                    app.stop();
                }
            }
        };
        exitButton.setColor(new ColorRGBA(1, 0.4f, 0, 1));
        exitButton.setText("Exit");
        ui.add(exitButton);
    }
    
    public void changeInit(){}
    public void onCursorMove(Vector2f cursorLoc){}
    
    // Called when a key is pressed or released
    @Override
    public void onAction(Vector2f cursorLoc, String bind, boolean down, float tpf){
        UIElement e = checkUI(cursorLoc);
        if(e != null){
            e.onAction(cursorLoc, bind, down, tpf);
        }
        
        // Exit Application
        if(bind.equals(Bind.Escape.toString())){
            app.stop();
        }
    }
    
    @Override
    public void onKeyEvent(KeyInputEvent evt){
        // implement
    }
}
