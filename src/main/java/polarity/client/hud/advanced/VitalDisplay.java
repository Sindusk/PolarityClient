package polarity.client.hud.advanced;

import character.Player;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import com.jme3.scene.Node;
import hud.DynamicBar;
import hud.HUDElement;
import stats.advanced.Resources;
import stats.advanced.Vitals;
import tools.Sys;
import ui.Label;

/**
 *
 * @author SinisteRing
 */
public class VitalDisplay extends HUDElement {
    protected Player player;
    protected DynamicBar healthBar;
    protected Label healthText;
    protected DynamicBar shieldBar;
    protected Label shieldText;
    protected DynamicBar res1Bar;
    protected Label res1Text;
    protected DynamicBar res2Bar;
    protected Label res2Text;
    
    public VitalDisplay(Node parent, Vector2f location, Player player){
        super(parent, location);
        this.player = player;
        Vector2f barSize = new Vector2f(Sys.width*0.2f, Sys.height*0.03f);
        
        // Health
        Vector2f healthLoc = new Vector2f(0, Sys.height*0.1f);
        healthText = new Label(node, healthLoc, barSize.y, 1);
        healthText.setColor(new ColorRGBA(0.1f, 0.1f, 0.1f, 1));
        healthText.setText("1000/1000");
        healthBar = new DynamicBar(node, healthLoc, barSize, new ColorRGBA(1, 0.3f, 0.3f, 1));
        healthBar.setAlign(DynamicBar.Alignment.Left);
        
        // Shields
        Vector2f shieldLoc = new Vector2f(0, Sys.height*0.05f);
        shieldText = new Label(node, shieldLoc, barSize.y, 1);
        shieldText.setColor(new ColorRGBA(0.1f, 0.1f, 0.1f, 1));
        shieldText.setText("1000/1000");
        shieldBar = new DynamicBar(node, shieldLoc, barSize, new ColorRGBA(0.3f, 0.3f, 1, 1));
        shieldBar.setAlign(DynamicBar.Alignment.Left);
        
        // Resource formatting values
        float separator = 5f;
        Vector2f resSize = new Vector2f(barSize.x/2f-separator, barSize.y);
        
        // Mana
        Vector2f res1Loc = new Vector2f(-barSize.x/4f-separator/2f, 0);
        res1Text = new Label(node, res1Loc, barSize.y, 1);
        res1Text.setColor(new ColorRGBA(0.1f, 0.1f, 0.1f, 1));
        res1Text.setText("1000/1000");
        res1Bar = new DynamicBar(node, res1Loc, resSize, new ColorRGBA(0.3f, 1, 0.3f, 1));
        res1Bar.setAlign(DynamicBar.Alignment.Left);
        
        // Energy
        Vector2f res2Loc = new Vector2f(barSize.x/4f+separator/2f, 0);
        res2Text = new Label(node, res2Loc, barSize.y, 1);
        res2Text.setColor(new ColorRGBA(0.1f, 0.1f, 0.1f, 1));
        res2Text.setText("100/100");
        res2Bar = new DynamicBar(node, res2Loc, resSize, new ColorRGBA(1f, 1f, 0.3f, 1));
        res2Bar.setAlign(DynamicBar.Alignment.Right);
    }
    
    @Override
    public void update(float tpf){
        Vitals vitals = player.getVitals();
        healthText.setText(Math.round(vitals.getHealth().value())+"/"+Math.round(vitals.getHealth().getMax()));
        healthBar.updateSize(vitals.getHealth().value() / vitals.getHealth().getMax());
        shieldText.setText(Math.round(vitals.getShield().value())+"/"+Math.round(vitals.getShield().getMax()));
        shieldBar.updateSize(vitals.getShield().value() / vitals.getShield().getMax());
        Resources resources = player.getResources();
        res1Text.setText(Math.round(resources.getMana().value())+"/"+Math.round(resources.getMana().getMax()));
        res1Bar.updateSize(resources.getMana().value() / resources.getMana().getMax());
        res2Text.setText(Math.round(resources.getEnergy().value())+"/"+Math.round(resources.getEnergy().getMax()));
        res2Bar.updateSize(resources.getEnergy().value() / resources.getEnergy().getMax());
    }
}
