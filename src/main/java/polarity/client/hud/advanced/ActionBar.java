package polarity.client.hud.advanced;

import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import polarity.shared.character.Player;
import polarity.shared.hud.HUDElement;
import polarity.shared.tools.GeoFactory;
import polarity.shared.tools.SinText;
import polarity.shared.ui.Button;

/**
 *
 * @author SinisteRing
 */
public class ActionBar extends HUDElement {
    public static final int NUM_SLOTS = 4;
    public static final float GEO_SIZE = 25f;
    
    protected Player player;
    protected Geometry background;
    protected Button[] actions = new Button[NUM_SLOTS];
    protected SinText[] power = new SinText[NUM_SLOTS];
    
    public ActionBar(Node parent, Vector2f loc, Player player){
        super(parent, loc);
        this.player = player;
        background = GeoFactory.createBox(node, new Vector3f(GEO_SIZE*6, GEO_SIZE*1.5f, 0), Vector3f.ZERO, ColorRGBA.Gray);
        int i = 0;
        float offset = -(GEO_SIZE*3.75f);
        while(i < NUM_SLOTS){
            // Button
            actions[i] = new Button(node, new Vector2f(offset, 0), GEO_SIZE*2, GEO_SIZE*2, 1);
            actions[i].setColor(ColorRGBA.White);
            actions[i].setText(""+(i+1));
            actions[i].setTextColor(ColorRGBA.Green);
            offset += GEO_SIZE*2.5f;
            
            // Text
            power[i] = GeoFactory.createSinText(actions[i].getNode(), GEO_SIZE*0.4f, new Vector3f(0, -GEO_SIZE*0.7f, 0.01f), "ES32", ColorRGBA.Blue, SinText.Alignment.Center);
            i++;
        }
    }
    
    @Override
    public void update(float tpf){
        int i = 0;
        while(i < power.length){
            power[i].setText(player.getMatrix(i).getStoredPower()+"/"+player.getMatrix(i).getCost());
            i++;
        }
    }
}
