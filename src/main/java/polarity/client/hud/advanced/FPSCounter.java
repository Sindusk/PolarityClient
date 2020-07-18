package polarity.client.hud.advanced;

import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import com.jme3.scene.Node;
import hud.HUDElement;
import tools.SinText;
import ui.Label;

/**
 *
 * @author SinisteRing
 */
public class FPSCounter extends HUDElement {
    protected Label label;
    
    public FPSCounter(Node parent, Vector2f location, float height){
        super(parent, location);
        label = new Label(node, Vector2f.ZERO, height, 0);
        label.setTextAlign(SinText.Alignment.Left);
        label.setColor(ColorRGBA.White);
    }
    
    @Override
    public void update(float tpf){
        label.setText("FPS: "+Math.round(1.0/tpf));
    }
}
