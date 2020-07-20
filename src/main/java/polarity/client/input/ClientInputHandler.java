package polarity.client.input;

import com.jme3.input.RawInputListener;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.event.*;
import polarity.shared.input.Bind;
import polarity.shared.input.InputHandler;
import polarity.shared.main.GameApplication;
import polarity.shared.tools.Util;

/**
 * ClientInputHandler - Handles all polarity.shared.input from users and organizes them based on conditions.
 * @author SinisteRing
 */
public class ClientInputHandler extends InputHandler implements ActionListener, AnalogListener, RawInputListener{
    // Initialization
    public ClientInputHandler(GameApplication app){
        super(app);
    }
    public void setupInputs(){
        for(Bind bind : Bind.values()){
            app.getInputManager().addMapping(bind.mapping, bind.trigger);
            app.getInputManager().addListener(this, bind.mapping);
        }
        app.getInputManager().addRawInputListener(this);
    }
    
    // Action handlers
    public void onAction(String bind, boolean down, float tpf){
        if(screen == null){
            return;
        }
        screen.onAction(app.getInputManager().getCursorPosition(), bind, down, tpf);
    }
    /**
     * Called when the cursor moves.
     * @param name Name of the movement that occured
     * @param value How much movement there is
     * @param tpf Time Per Frame
     */
    public void onAnalog(String name, float value, float tpf){
        if(screen == null){
            return;
        }
        screen.onCursorMove(app.getInputManager().getCursorPosition());
    }
    public void onKeyEvent(KeyInputEvent evt){
        if(evt.isPressed()){
            Util.log(evt.getKeyChar()+" = "+evt.getKeyCode(), 3);
        }
        if(screen == null){
            return;
        }
        screen.onKeyEvent(evt);
    }
    
    // Excess methods because of RawInputListener implementation
    public void beginInput() {}
    public void endInput() {}
    public void onJoyAxisEvent(JoyAxisEvent evt) {}
    public void onJoyButtonEvent(JoyButtonEvent evt) {}
    public void onMouseMotionEvent(MouseMotionEvent evt) {}
    public void onMouseButtonEvent(MouseButtonEvent evt) {}
    public void onTouchEvent(TouchEvent evt) {}
}
