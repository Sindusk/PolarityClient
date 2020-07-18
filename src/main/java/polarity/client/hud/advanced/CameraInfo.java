package polarity.client.hud.advanced;

import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import com.jme3.scene.Node;
import polarity.client.screens.GameScreen;
import polarity.shared.hud.HUDElement;
import polarity.shared.tools.SinText;
import polarity.shared.ui.Label;

/**
 *
 * @author SinisteRing
 */
public class CameraInfo extends HUDElement {
    protected GameScreen gameScreen;
    protected Label label;
    
    public CameraInfo(Node parent, Vector2f location, float height, GameScreen gameScreen){
        super(parent, location);
        this.gameScreen = gameScreen;
        label = new Label(node, Vector2f.ZERO, height, 0);
        label.setTextAlign(SinText.Alignment.Left);
        label.setColor(ColorRGBA.White);
    }
    
    @Override
    public void update(float tpf){
        label.setText("Camera Setting: "+gameScreen.getCameraSetting());
    }
}
