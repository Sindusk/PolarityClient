package polarity.client.screens;

import com.jme3.input.event.KeyInputEvent;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import polarity.client.main.GameClient;
import polarity.client.network.ClientNetwork;
import polarity.shared.character.Player;
import polarity.shared.equipment.AccessoryData;
import polarity.shared.equipment.ArmorData;
import polarity.shared.equipment.EquipmentData;
import polarity.shared.equipment.WeaponData;
import polarity.shared.hud.Tooltip;
import polarity.shared.hud.advanced.FPSCounter;
import polarity.shared.input.Bind;
import polarity.shared.input.InputHandler;
import polarity.shared.items.Inventory;
import polarity.shared.items.creation.ItemFactory;
import polarity.shared.items.data.SpellNodeItemData;
import polarity.shared.netdata.updates.MatrixUpdate;
import polarity.shared.screens.Screen;
import polarity.shared.spellforge.SpellMatrix;
import polarity.shared.spellforge.nodes.SpellNode;
import polarity.shared.spellforge.nodes.SpellNodeData;
import polarity.shared.spellforge.nodes.conduits.EffectConduitData;
import polarity.shared.spellforge.nodes.conduits.ModifierConduitData;
import polarity.shared.spellforge.nodes.conduits.PowerConduitData;
import polarity.shared.tools.Sys;
import polarity.shared.tools.Util;
import polarity.shared.tools.Vector2i;
import polarity.shared.ui.Button;
import polarity.shared.ui.Menu;
import polarity.shared.ui.UIElement;
import polarity.shared.ui.interfaces.Draggable;
import polarity.shared.ui.interfaces.TooltipInfo;
import polarity.shared.ui.items.EquipmentButton;
import polarity.shared.ui.items.EquipmentPanel;
import polarity.shared.ui.items.InventoryPanel;
import polarity.shared.ui.items.ItemButton;

import java.util.concurrent.Callable;

/**
 *
 * @author SinisteRing
 */
public class SpellForgeScreen extends Screen {
    protected GameScreen gameScreen;
    
    protected Draggable dragging;
    protected InventoryPanel invPanel;
    protected EquipmentPanel equipPanel;
    protected Menu menu;
    protected Tooltip tooltip;
    protected Tooltip itemTooltip;
    
    // Data
    protected Player player;
    protected SpellMatrix matrix;
    protected int matrixIndex = 0;
    
    public SpellForgeScreen(GameClient app, GameScreen gameScreen, Node root, Node gui){
        super(app, root, gui);
        this.gameScreen = gameScreen;
        this.player = gameScreen.getPlayer();
        this.name = "Spell Forge Screen";
    }
    
    public SpellMatrix getMatrix(){
        return matrix;
    }
    
    private void loadMatrix(int index){
        if(matrix == player.getMatrix(index)){
            return;
        }
        matrix.setVisible(false);
        matrix = player.getMatrix(index);
        matrix.setVisible(true);
        matrixIndex = index;
    }
    
    @Override
    public void initialize(final InputHandler inputHandler) {
        this.inputHandler = inputHandler;
        hud.add(new FPSCounter(gui, new Vector2f(10, Sys.height-15), 15));   // Creates the FPS Counter
        
        final Player p = gameScreen.getPlayer();
        player.initializeMatrixArray(gui);
        matrix = player.getMatrix(0);
        matrix.setVisible(true);
        
        invPanel = new InventoryPanel(gui, new Vector2f(Sys.width*0.15f, Sys.height*0.5f), Sys.width*0.25f, Sys.height*0.9f, 0);
        invPanel.setItemsPerRow(5);
        invPanel.setColor(new ColorRGBA(0.1f, 0.1f, 0.1f, 1));
        invPanel.setInventory(gameScreen.getPlayer().getData().getInventory());
        invPanel.display();
        ui.add(invPanel);
        
        equipPanel = new EquipmentPanel(gui, new Vector2f(Sys.width*0.85f, Sys.height*0.35f), Sys.width*0.25f, Sys.height*0.6f, 0);
        equipPanel.setColor(new ColorRGBA(0.1f, 0.1f, 0.1f, 1));
        equipPanel.setEquipment(player.getData().getEquipment());
        equipPanel.display(new Vector2i(0, 0));
        ui.add(equipPanel);
        
        tooltip = new Tooltip(gui, new Vector2f(Sys.width*0.75f, Sys.height-50));
        itemTooltip = new Tooltip(gui, Vector2f.ZERO);
        itemTooltip.toggleVisible();
        
        Button b = new Button(gui, new Vector2f(Sys.width*0.0875f, Sys.height*0.1f), Sys.width*0.1f, Sys.height*0.03f, 1){
            @Override
            public void onAction(Vector2f cursorLoc, String bind, boolean down, float tpf){
                app.enqueue(new Callable<Void>(){
                    public Void call() throws Exception{
                        Inventory inv = p.getData().getInventory();
                        //inv.add(new SpellNodeItem(icon[FastMath.nextRandomInt(0, icon.length-1)], type[FastMath.nextRandomInt(0, type.length-1)]));
                        inv.add(ItemFactory.randomItem(inv, (int) Util.scaledRandFloat(1, 200)));
                        invPanel.display();
                        return null;
                    }
                });
            }
        };
        b.setText("Add Item");
        b.setColor(ColorRGBA.Orange);
        ui.add(b);
        b = new Button(gui, new Vector2f(Sys.width*0.2125f, Sys.height*0.1f), Sys.width*0.1f, Sys.height*0.03f, 1){
            @Override
            public void onAction(Vector2f cursorLoc, String bind, boolean down, float tpf){
                app.enqueue(new Callable<Void>(){
                    public Void call() throws Exception{
                        Inventory inv = p.getData().getInventory();
                        inv.removeRandom();
                        invPanel.display();
                        return null;
                    }
                });
            }
        };
        b.setText("Delete Item");
        b.setColor(ColorRGBA.Orange);
        ui.add(b);
    }
    
    @Override
    public void update(float tpf) {
        Vector2f cursorLoc = app.getInputManager().getCursorPosition();
        
        // Update any new data from the server
        // !!! TBI !!!
        
        // Update dragging target location
        if(dragging != null){
            dragging.moveWithOffset(cursorLoc);
        }
        
        // Update spell matricies
        player.updateResources(tpf);
        for(SpellMatrix spellMatrix : player.getMatrixArray()){
            if(spellMatrix != null){
                spellMatrix.update(tpf);
            }
        }
        
        // Reset the buttons on the menu to white
        // !! ROOM FOR IMPROVEMENT !!
        if(menu != null){
            for(Button b : menu.getOptions()){
                b.setTextColor(ColorRGBA.White);
            }
        }
        
        // Gets the top UI element that the cursor is currently over
        UIElement e = checkUI(cursorLoc);
        if(e != null){
            if(e instanceof InventoryPanel){
                // Sift through the controls of the InventoryPanel to find the item the cursor is over
                InventoryPanel panel = (InventoryPanel) e;
                e = panel.checkControls(cursorLoc);
                if(e instanceof TooltipInfo){
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
            }else if(e instanceof Button && menu != null && menu.getOptions().contains((Button)e)){
                Button b = (Button) e;
                b.setTextColor(ColorRGBA.Cyan);
            }
        }else{
            // If no UI element is found, make sure the item tooltip is hidden
            if(itemTooltip.isVisible()){
                itemTooltip.toggleVisible();
            }
        }
        
        // Find the spellNode the cursor is hovering over
        SpellNode spellNode = matrix.findNode(cursorLoc);
        if(spellNode != null){
            // If one is found, update the tooltip for it
            if(!tooltip.isVisible()){
                tooltip.toggleVisible();
            }
            tooltip.setText(spellNode.getTooltip(), spellNode.getColorMap());
        }else if(tooltip.isVisible()){
            // Else, ensure the tooltip is hidden
            tooltip.toggleVisible();
        }
        
        // Super call at end so it has latest info
        super.update(tpf);
    }
    
    @Override
    public void setVisible(boolean show){
        super.setVisible(show);
        if(show){
            Sys.getCamera().setLocation(new Vector3f(0, 0, 50));
            Sys.getCamera().lookAt(Vector3f.ZERO, Vector3f.UNIT_Y);
        }
    }
    
    private void createMenu(SpellNode spellNode, Vector2f cursorLoc){
        if(menu != null){
            menu.destroy(ui);
        }
        menu = new Menu(gui, cursorLoc.add(new Vector2f(100, -30)), 1);
        if(spellNode.isEmpty()){
            menu.addOption(ui, addPowerConduitOption(menu, spellNode));
            menu.addOption(ui, addModifierConduitOption(menu, spellNode));
            menu.addOption(ui, addEffectConduitOption(menu, spellNode));
            menu.addOption(ui, addRemoveOption(menu, spellNode));
        }
    }

    @Override
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
                EquipmentPanel panel = (EquipmentPanel) e;
                e = panel.checkControls(cursorLoc);
                if(e instanceof EquipmentButton && down && bind.equals(Bind.LClick.toString())){
                    EquipmentButton button = (EquipmentButton) e;
                    EquipmentData data = button.getGear();
                    if(data instanceof WeaponData){
                        int index = panel.getWeaponIndex(button);
                        loadMatrix(index);
                        equipPanel.display(new Vector2i(0, index));
                    }else if(data instanceof AccessoryData){
                        Util.log("Accessory");
                    }else if(data instanceof ArmorData){
                        Util.log("Armor");
                    }
                }
            }else if(down){
                e.onAction(cursorLoc, bind, down, tpf);
                if(menu != null){
                    menu.destroy(ui);
                }
                return;
            }
        }
        SpellNode spellNode = matrix.findNode(cursorLoc);
        if(spellNode != null){
            if(dragging != null && !down && bind.equals(Bind.LClick.toString()) && dragging instanceof ItemButton){
                // If the mouse is released when dragging an item over a spell node, replace the spell node
                ItemButton button = (ItemButton) dragging;
                if(button.getItem().getData() instanceof SpellNodeItemData){
                    SpellNodeItemData item = (SpellNodeItemData) button.getItem().getData();
                    SpellNodeData data = item.getData().cleanData(spellNode.getData());
                    ClientNetwork.Instance.send(new MatrixUpdate(gameScreen.getPlayer().getID(), matrixIndex, data));
                    button.getItem().getInventory().remove(item);
                    invPanel.display();
                }
            }else if(down && bind.equals(Bind.RClick.toString())){
                createMenu(spellNode, cursorLoc);
            }else if(menu != null && menu.isActive() && down){
                menu.destroy(ui);
            }
        }else if(menu != null && menu.isActive() && down){
            menu.destroy(ui);
        }
        if(dragging != null && !down && bind.equals(Bind.LClick.toString())){
            dragging.resetDragging();
            dragging = null;
        }
        if(bind.equals(Bind.Escape.toString()) && down){
            inputHandler.changeScreens(gameScreen);
        }
    }
    
    public Button addPowerConduitOption(Menu menu, final SpellNode spellNode){
        Button b = new Button(menu.getNode(), new Vector2f(0, -menu.size()*20), 200, 20, 1){
            @Override
            public void onAction(Vector2f cursorLoc, String bind, boolean down, float tpf){
                SpellNodeData data = spellNode.getData();
                if(data.toItem() != null){
                    invPanel.getInventory().add(data.toItem());
                }
                PowerConduitData conduit = new PowerConduitData(data);
                ClientNetwork.Instance.send(new MatrixUpdate(gameScreen.getPlayer().getID(), matrixIndex, conduit));
                invPanel.display();
            }
        };
        b.setColor(new ColorRGBA(0.58f, 0.11f, 0.73f, 1));
        b.setText("Power Conduit");
        b.setTextColor(ColorRGBA.White);
        return b;
    }
    public Button addModifierConduitOption(Menu menu, final SpellNode spellNode){
        Button b = new Button(menu.getNode(), new Vector2f(0, -menu.size()*20), 200, 20, 1){
            @Override
            public void onAction(Vector2f cursorLoc, String bind, boolean down, float tpf){
                SpellNodeData data = spellNode.getData();
                if(data.toItem() != null){
                    invPanel.getInventory().add(data.toItem());
                }
                ModifierConduitData conduit = new ModifierConduitData(data);
                ClientNetwork.Instance.send(new MatrixUpdate(gameScreen.getPlayer().getID(), matrixIndex, conduit));
                spellNode.changeData(conduit);
                invPanel.display();
            }
        };
        b.setColor(new ColorRGBA(0.73f, 0.73f, 0, 1));
        b.setText("Modifier Conduit");
        b.setTextColor(ColorRGBA.White);
        return b;
    }
    public Button addEffectConduitOption(Menu menu, final SpellNode spellNode){
        Button b = new Button(menu.getNode(), new Vector2f(0, -menu.size()*20), 200, 20, 1){
            @Override
            public void onAction(Vector2f cursorLoc, String bind, boolean down, float tpf){
                SpellNodeData data = spellNode.getData();
                if(data.toItem() != null){
                    invPanel.getInventory().add(data.toItem());
                }
                EffectConduitData conduit = new EffectConduitData(data);
                ClientNetwork.Instance.send(new MatrixUpdate(gameScreen.getPlayer().getID(), matrixIndex, conduit));
                spellNode.changeData(conduit);
                invPanel.display();
            }
        };
        b.setColor(new ColorRGBA(0.16f, 0.60f, 0, 1));
        b.setText("Effect Conduit");
        b.setTextColor(ColorRGBA.White);
        return b;
    }
    public Button addRemoveOption(Menu menu, final SpellNode spellNode){
        Button b = new Button(menu.getNode(), new Vector2f(0, -menu.size()*20), 200, 20, 1){
            @Override
            public void onAction(Vector2f cursorLoc, String bind, boolean down, float tpf){
                SpellNodeData data = spellNode.getData();
                if(data.toItem() != null){
                    invPanel.getInventory().add(data.toItem());
                }
                SpellNodeData removal = new SpellNodeData(data.getX(), data.getY(), data.getLocation());
                ClientNetwork.Instance.send(new MatrixUpdate(gameScreen.getPlayer().getID(), matrixIndex, removal));
                spellNode.changeData(removal);
                invPanel.display();
            }
        };
        b.setColor(new ColorRGBA(0.5f, 0.5f, 0.5f, 1));
        b.setText("Remove");
        b.setTextColor(ColorRGBA.White);
        return b;
    }
    
    public void changeInit(){}
    public void onCursorMove(Vector2f cursorLoc) {}
    public void onKeyEvent(KeyInputEvent evt) {}
}
