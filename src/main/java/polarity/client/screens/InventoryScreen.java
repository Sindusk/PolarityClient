package polarity.client.screens;

import character.Player;
import com.jme3.input.event.KeyInputEvent;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import com.jme3.scene.Node;
import hud.Tooltip;
import hud.advanced.FPSCounter;
import input.Bind;
import input.InputHandler;
import main.GameApplication;
import screens.Screen;
import tools.Sys;
import tools.Vector2i;
import ui.UIElement;
import ui.interfaces.Draggable;
import ui.interfaces.TooltipInfo;
import ui.items.EquipmentPanel;
import ui.items.InventoryPanel;
import ui.items.ItemButton;

/**
 *
 * @author SinisteRing
 */
public class InventoryScreen extends Screen {
    protected GameScreen gameScreen;
    protected Player player;
    
    protected InventoryPanel invPanel;
    protected EquipmentPanel equipPanel;
    protected Draggable dragging;
    protected Tooltip itemTooltip;
    
    public InventoryScreen(GameApplication app, GameScreen gameScreen, Node rootNode, Node guiNode){
        super(app, rootNode, guiNode);
        this.gameScreen = gameScreen;
        this.player = gameScreen.getPlayer();
        this.name = "Inventory Screen";
    }
    
    public void initialize(final InputHandler inputHandler){
        this.inputHandler = inputHandler;
        
        // Initialize HUD
        hud.add(new FPSCounter(gui, new Vector2f(10, Sys.height-15), 15));   // Creates the FPS Counter
        
        // Initialize Tooltip
        itemTooltip = new Tooltip(gui, Vector2f.ZERO);
        itemTooltip.toggleVisible();
        
        // Add the Inventory Panel
        invPanel = new InventoryPanel(gui, new Vector2f(Sys.width*0.15f, Sys.height*0.5f), Sys.width*0.25f, Sys.height*0.9f, 0);
        invPanel.setItemsPerRow(6);
        invPanel.setColor(new ColorRGBA(0.1f, 0.1f, 0.1f, 1));
        invPanel.setInventory(player.getData().getInventory());
        invPanel.display();
        ui.add(invPanel);
        
        // Add the Equipment Panel
        equipPanel = new EquipmentPanel(gui, new Vector2f(Sys.width*0.85f, Sys.height*0.35f), Sys.width*0.25f, Sys.height*0.6f, 0);
        equipPanel.setColor(new ColorRGBA(0.1f, 0.1f, 0.1f, 1));
        equipPanel.setEquipment(player.getData().getEquipment());
        equipPanel.display(new Vector2i(0, 0));
        ui.add(equipPanel);
    }
    
    @Override
    public void update(float tpf) {
        Vector2f cursorLoc = app.getInputManager().getCursorPosition();
        
        // Update dragging target location
        if(dragging != null){
            dragging.moveWithOffset(cursorLoc);
        }
        
        // Gets the top UI element that the cursor is currently over
        UIElement e = checkUI(cursorLoc);
        if(e != null){
            if(e instanceof InventoryPanel){
                // Sift through the controls of the InventoryPanel to find the item the cursor is over
                InventoryPanel panel = (InventoryPanel) e;
                e = panel.checkControls(cursorLoc);
                if(e != null && e instanceof TooltipInfo){
                    // If there's an item, display new tooltip
                    TooltipInfo ttinfo = (TooltipInfo) e;
                    if(!itemTooltip.isVisible()){
                        itemTooltip.toggleVisible();
                    }
                    itemTooltip.setText(ttinfo.getTooltip(), ttinfo.getColorMap());
                    itemTooltip.setLocation(cursorLoc.add(new Vector2f(50, 5)));
                }else{
                    // Else, ensure the tooltip is hidden.
                    if(itemTooltip.isVisible()){
                        itemTooltip.toggleVisible();
                    }
                }
            }
        }else{
            // If no UI element is found, make sure the item tooltip is hidden
            if(itemTooltip.isVisible()){
                itemTooltip.toggleVisible();
            }
        }
        
        // Super call at end so it has latest info
        super.update(tpf);
    }
    
    public void onAction(Vector2f cursorLoc, String bind, boolean down, float tpf) {
        UIElement e = checkUI(cursorLoc);
        if(e != null){
            if(e instanceof InventoryPanel){
                InventoryPanel panel = (InventoryPanel) e;
                e = panel.checkControls(cursorLoc);
                if(e != null && e instanceof Draggable && down && bind.equals(Bind.LClick.toString())){
                    dragging = (Draggable) e;
                }
            }else if(e instanceof EquipmentPanel){
                if(dragging == null){
                    EquipmentPanel panel = (EquipmentPanel) e;
                    e = panel.checkControls(cursorLoc);
                    if(e != null && e instanceof Draggable && down && bind.equals(Bind.LClick.toString())){
                        dragging = (Draggable) e;
                    }
                }else if(!down && bind.equals(Bind.LClick.toString()) && dragging instanceof ItemButton){
                    // If the mouse is released when dragging an item over a spell node, replace the spell node
                    ItemButton button = (ItemButton) dragging;
                    //if(button.getItem().getData() instanceof EquipmentData){
                        //EquipmentData data = (EquipmentData) button.getItem().getData();
                        //clientNetwork.send(new EquipmentUpdate(gameScreen.getPlayer().getID(), matrixIndex, data));
                        //button.getItem().getInventory().remove(item);
                        //invPanel.display();
                    //}
                }
            }else if(down){
                e.onAction(cursorLoc, bind, down, tpf);
                return;
            }
        }
        if(dragging != null && !down && bind.equals(Bind.LClick.toString())){
            dragging.resetDragging();
            dragging = null;
        }
        if(bind.equals(Bind.Escape.toString()) && down){
            inputHandler.changeScreens(gameScreen);
        }
    }
    
    public void changeInit() {}
    public void onCursorMove(Vector2f cursorLoc) {}
    public void onKeyEvent(KeyInputEvent evt) {}
}
