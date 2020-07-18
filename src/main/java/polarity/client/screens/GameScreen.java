package polarity.client.screens;

import ai.pathfinding.AStarPathFinder;
import ai.pathfinding.Path;
import character.CharacterManager;
import character.Player;
import com.jme3.input.event.KeyInputEvent;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import entity.Entity;
import files.ChunkFileManager;
import hud.advanced.FPSCounter;
import polarity.client.hud.advanced.*;
import input.Bind;
import input.InputHandler;
import main.GameApplication;
import netdata.ChatMessage;
import netdata.requests.SpellMatrixRequest;
import netdata.testing.MonsterCreateData;
import polarity.client.network.ClientNetwork;
import screens.Screen;
import spellforge.SpellMatrix;
import tools.GeoFactory;
import tools.Sys;
import tools.Util;
import ui.Button;
import ui.UIElement;
import ui.advanced.GameMenu;
import world.Sector;

import java.util.concurrent.Callable;

/**
 * Screen encompassing Gameplay.
 * @author Sindusk
 */
public class GameScreen extends Screen {
    protected SpellForgeScreen spellForgeScreen;
    protected GameMenu gameMenu;
    protected CharacterManager charManager;
    
    // HUD Elements
    protected FPSCounter fpsCounter;
    protected Locator locator;
    protected CameraInfo cameraInfo;
    protected VitalDisplay vitalDisplay;
    protected ActionBar actionBar;
    protected ChatBox chatBox;
    
    // Camera testing
    protected boolean unlockedCamera = true;
    protected float cameraHeight = 40;
    // Movement testing
    protected int playerID;
    
    public GameScreen(GameApplication app, Node rootNode, Node guiNode){
        super(app, rootNode, guiNode);
        gameMenu = new GameMenu(gui, new Vector2f(Sys.width*0.5f, Sys.height*0.5f), 2);
        charManager = app.getCharManager();
        playerID = ClientNetwork.Instance.getID();
        charManager.setMyID(playerID);
        name="Game Screen";
    }
    public int getCameraSetting(){
        return (int)cameraHeight/4;
    }
    public Player getPlayer(){
        return charManager.getPlayer(playerID);
    }
    
    public void chatMessage(ChatMessage d){
        chatBox.addMessage(charManager.getOwner(d.getOwner()).getName()+": "+d.getMessage());
    }
    private void updateCamera(){
        Vector2f tempVect = getPlayer().getLocation();
        if(unlockedCamera){
            Vector2f modCursorLoc = app.getInputManager().getCursorPosition().subtract(new Vector2f(Sys.width*0.5f, Sys.height*0.5f)).mult(1/cameraHeight);
            app.getCamera().getFrustumRight();
            app.getCamera().setLocation(new Vector3f(tempVect.x+modCursorLoc.x, tempVect.y+modCursorLoc.y, 50));
        }else{
            app.getCamera().setLocation(new Vector3f(tempVect.x, tempVect.y, 50));
        }
    }
    
    public void initialize(final InputHandler inputHandler) {
        this.inputHandler = inputHandler;
        
        // Add HUD elements
        fpsCounter = new FPSCounter(gui, new Vector2f(10, Sys.height-15), 15);
        hud.add(fpsCounter);
        locator = new Locator(gui, new Vector2f(10, Sys.height-35), 15, app.getWorld(), getPlayer());
        hud.add(locator);
        cameraInfo = new CameraInfo(gui, new Vector2f(10, Sys.height-95), 15, this);
        hud.add(cameraInfo);
        vitalDisplay = new VitalDisplay(gui, new Vector2f(150, 50), getPlayer());
        hud.add(vitalDisplay);
        actionBar = new ActionBar(gui, new Vector2f(Sys.width*0.5f, ActionBar.GEO_SIZE*1.5f), getPlayer());
        hud.add(actionBar);
        chatBox = new ChatBox(gui, new Vector2f(Sys.width*0.2f, Sys.height*0.4f), new Vector2f(Sys.width*0.175f, Sys.height*0.15f));
        hud.add(chatBox);
        
        // Buttons for Game Menu
        // Return to Game button:
        Button returnButton = new Button(gameMenu.getNode(), new Vector2f(0, Sys.height*0.1f), Sys.width*0.4f, Sys.height*0.05f, 0){
            @Override
            public void onAction(Vector2f cursorLoc, String bind, boolean down, float tpf){
                if(bind.equals(Bind.LClick.toString()) && down){
                    gameMenu.setVisible(ui, false);
                }
            }
        };
        returnButton.setText("Return to Game");
        returnButton.setColor(ColorRGBA.Red);
        gameMenu.addOption(ui, returnButton);
        ui.remove(returnButton);
        
        // Spell Matrix button:
        Button spellMatrixButton = new Button(gameMenu.getNode(), new Vector2f(0, -Sys.height*0f), Sys.width*0.4f, Sys.height*0.05f, 0){
            @Override
            public void onAction(Vector2f cursorLoc, String bind, boolean down, float tpf){
                if(bind.equals(Bind.LClick.toString()) && down){
                    ClientNetwork.Instance.send(new SpellMatrixRequest(playerID));
                    inputHandler.changeScreens(spellForgeScreen);
                }
            }
        };
        spellMatrixButton.setText("Spell Matrix");
        spellMatrixButton.setColor(ColorRGBA.Red);
        gameMenu.addOption(ui, spellMatrixButton);
        ui.remove(spellMatrixButton);
        
        // Exit Button
        Button exitButton = new Button(gameMenu.getNode(), new Vector2f(0, -Sys.height*0.1f), Sys.width*0.4f, Sys.height*0.05f, 0){
            @Override
            public void onAction(Vector2f cursorLoc, String bind, boolean down, float tpf){
                if(bind.equals(Bind.LClick.toString()) && down){
                    app.stop();
                }
            }
        };
        exitButton.setText("Exit");
        exitButton.setColor(ColorRGBA.Red);
        gameMenu.addOption(ui, exitButton);
        ui.remove(exitButton);
        
        spellForgeScreen = new SpellForgeScreen(app, this, Screen.getTopRoot(), Screen.getTopGUI());
        spellForgeScreen.initialize(inputHandler);
        spellForgeScreen.setVisible(false);
        
        root.attachChild(app.getWorld().getNode());
    }
    
    @Override
    public void changeInit(){
        getPlayer().setAttacking(0, false);
    }
    
    @Override
    public void update(final float tpf){
        // For multithreading of heavy CPU usage loops, create an enqueue:
        app.enqueue(new Callable<Void>(){
            public Void call() throws Exception{
                // Update the local player
                getPlayer().updateLocal(tpf, inputHandler.getCursorLocWorld(), app.getInputManager().getCursorPosition());
                return null;
            }
        });
        app.enqueue(new Callable<Void>(){
            public Void call() throws Exception{
                app.getWorld().update(tpf);
                return null;
            }
        });
        app.enqueue(new Callable<Void>(){
            public Void call() throws Exception{
                app.getWorld().checkChunks(getPlayer(), ClientNetwork.Instance);
                return null;
            }
        });
        
        getPlayer().updateResources(tpf);
        for(SpellMatrix matrix : getPlayer().getMatrixArray()){
            if(matrix != null){
                matrix.update(tpf);
            }
        }
        
        // Update Camera
        if(!gameMenu.isActive()){
            updateCamera();
        }
        
        // Update client network
        ClientNetwork.Instance.update(tpf);
        
        // Super call at end so it has latest info
        super.update(tpf);
    }
    
    public void onCursorMove(Vector2f cursorLoc) {}
    
    @Override
    public void onAction(Vector2f cursorLoc, String bind, boolean down, float tpf) {
        if(chatBox.isActive() && !bind.equals(Bind.Enter.toString())){
            return;
        }
        UIElement e = checkUI(cursorLoc);
        if(e != null){
            e.onAction(cursorLoc, bind, down, tpf);
        }
        if(gameMenu.isActive() || e != null){
            return;
        }
        // Actions
        if(bind.equals(Bind.LClick.toString())){
            getPlayer().setAttacking(0, down);
        }else if(bind.equals(Bind.One.toString())){
            getPlayer().setAttacking(0, down);
        }else if(bind.equals(Bind.Two.toString())){
            getPlayer().setAttacking(1, down);
        }else if(bind.equals(Bind.Three.toString())){
            getPlayer().setAttacking(2, down);
        }else if(bind.equals(Bind.Four.toString())){
            getPlayer().setAttacking(3, down);
        // Right-Click Testing
        }else if(bind.equals(Bind.RClick.toString()) && down){
            Entity entity = getPlayer().getEntity();
            Vector3f cursorWorldLoc = Util.getWorldLoc(cursorLoc, app.getCamera()).add(new Vector3f(0.5f, 0.5f, 0));
            Vector2f eLoc = entity.getLocation().add(new Vector2f(0.5f, 0.5f));
            
            Sector sector = new Sector(app.getWorld(), eLoc, new Vector2f(cursorWorldLoc.x, cursorWorldLoc.y));
            AStarPathFinder finder = new AStarPathFinder(sector, 50, true);
            Path path = finder.findPath(entity, (int)eLoc.x, (int)eLoc.y, (int)cursorWorldLoc.x, (int)cursorWorldLoc.y);
            if(path != null){
                app.getWorld().showPath(path);
            }
        // Movement
        }else if(bind.equals(Bind.W.toString())){
            getPlayer().setMovement(0, down);
        }else if(bind.equals(Bind.D.toString())){
            getPlayer().setMovement(1, down);
        }else if(bind.equals(Bind.S.toString())){
            getPlayer().setMovement(2, down);
        }else if(bind.equals(Bind.A.toString())){
            getPlayer().setMovement(3, down);
        }else if(bind.equals(Bind.Escape.toString()) && down && !gameMenu.isActive()){
            gameMenu.setVisible(ui, true);
        }else if(bind.equals(Bind.Enter.toString()) && down){
            if(chatBox.isActive()){
                String message = chatBox.getNewMessage();
                chatBox.endMessage();
                chatBox.addMessage(getPlayer().getName()+": "+message);
                ClientNetwork.Instance.send(new ChatMessage(getPlayer().asOwner(), message));
            }else{
                chatBox.startNewMessage();
            }
        // Extra (Generally just poorly-coded testing keys)
        }else if(bind.equals(Bind.ScrollUp.toString()) && down){
            if(cameraHeight < 80){
                cameraHeight += 4f;
            }
            float width= Sys.width/cameraHeight;
            float height= Sys.height/cameraHeight;
            app.getCamera().setFrustum(1.0f, 100f, -width, width, height, -height);
            app.getCamera().update();
        }else if(bind.equals(Bind.ScrollDown.toString()) && down){
            if(cameraHeight > 4){
                cameraHeight -= 4;
            }
            float width= Sys.width/cameraHeight;
            float height= Sys.height/cameraHeight;
            app.getCamera().setFrustum(1.0f, 100f, -width, width, height, -height);
            app.getCamera().update();
        }else if(bind.equals(Bind.M.toString()) && down){
            GeoFactory.toggleWireframe();
        }else if(bind.equals(Bind.N.toString()) && down){
            Vector3f worldLoc = Util.getWorldLoc(cursorLoc, app.getCamera());
            ClientNetwork.Instance.send(new MonsterCreateData(new Vector2f(worldLoc.x, worldLoc.y)));
        }else if(bind.equals(Bind.B.toString()) && down){
            getPlayer().updateLocation(inputHandler.getCursorLocWorld());
        }else if(bind.equals(Bind.Y.toString()) && down){
            unlockedCamera = !unlockedCamera;
        }else if(bind.equals(Bind.K.toString()) && down){
            ChunkFileManager cfm = new ChunkFileManager("data/chunktest.data", app.getWorld().getChunk(getPlayer().getLocation()));
            cfm.save();
        }else if(bind.equals(Bind.J.toString()) && down){
            app.enqueue(new Callable<Void>(){
                public Void call() throws Exception{
                    ChunkFileManager cfm = new ChunkFileManager("data/chunktest.data", app.getWorld().getChunk(getPlayer().getLocation()));
                    cfm.load();
                    return null;
                }
            });
        }
    }
    
    @Override
    public void onKeyEvent(KeyInputEvent evt){
        if(chatBox.isActive()){
            chatBox.addToMessage(evt);
        }
    }
}
