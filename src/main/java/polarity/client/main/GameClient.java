package polarity.client.main;

import com.jme3.system.AppSettings;
import polarity.client.input.ClientInputHandler;
import polarity.client.monsters.MonsterHandler;
import polarity.client.network.ClientNetwork;
import polarity.client.players.PlayerHandler;
import polarity.client.screens.MenuScreen;
import polarity.client.world.ClientWorld;
import polarity.shared.main.GameApplication;
import polarity.shared.screens.Screen;
import polarity.shared.tools.Sys;
import polarity.shared.tools.Util;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
Copyright (c) 2003-2012 jMonkeyEngine
All rights reserved.
 
Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are
met:
 
Redistributions of source code must retain the above copyright
notice, this list of conditions and the following disclaimer.
 
Redistributions in binary form must reproduce the above copyright
notice, this list of conditions and the following disclaimer in the
documentation and/or other materials provided with the distribution.
 
Neither the name of 'jMonkeyEngine' nor the names of its contributors 
may be used to endorse or promote products derived from this software 
without specific prior written permission.
 
THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
public class GameClient extends GameApplication {
    protected static GameClient Instance = null;
    protected PlayerHandler playerHandler = new PlayerHandler();
    protected MonsterHandler monsterHandler = new MonsterHandler();
    protected ClientInputHandler inputHandler;
    protected ClientNetwork clientNetwork;
    
    public static void main(String[] args) throws InterruptedException{
        Instance = new GameClient();
        Instance.start();
    }
    
    public ClientNetwork getNetwork(){
        return clientNetwork;
    }
    
    @Override
    public void start() {
        Logger.getLogger("com.jme3").setLevel(Level.WARNING);
        settings = new AppSettings(true);
        settings.setSamples(0);
        settings.setVSync(false);
        //settings.setRenderer(AppSettings.LWJGL_OPENGL1);
        //settings.setResolution(640, 360);
        //settings.setResolution(1280, 720);
        settings.setResolution(1408, 792);
        //settings.setResolution(1485, 972);
        //settings.setFullscreen(true);
        settings.setTitle("Polarity");
        this.setSettings(settings);
        Util.log("GameClient start()");
        super.start();
    }
    
    @Override
    public void initialize() {
        super.initialize();

        // World initialization
        world = new ClientWorld(50);
        Sys.setWorld(world);
        
        // Initialize camera
        Util.log("[GameClient] <initialize> Initializing Camera...", 1);
        cam.setParallelProjection(true);
        float width=25f* Sys.width/1200f;
        float height=25f* Sys.height/1200f;
        cam.setFrustum(1.0f, 100f, -width, width, height, -height);
        cam.update();
        
        // Initialize input handler
        inputHandler = new ClientInputHandler(this);
        inputHandler.setupInputs();
        
        // Initialize networking
        clientNetwork = new ClientNetwork(this, playerHandler, monsterHandler);
        clientNetwork.setInputHandler(inputHandler);
        Sys.setNetwork(clientNetwork);
        
        // Initialize Screen static vars
        Screen.setNodes(root, gui);
        
        inputHandler.switchScreens(new MenuScreen(this, root, gui));
    }

    @Override
    public void update() {
        super.update();
        if(speed == 0 || paused){   // If the client is paused, do not update.
            return;
        }
        float tpf = timer.getTimePerFrame() * speed;    // Calculated time from last frame for keeping time consistency through FPS fluctuations.
        
        // Custom updates
        inputHandler.update(tpf);
        
        // Update node states
        updateNodeStates(tpf);
        
        // Update renderer
        renderDisplay(tpf);
    }
    
    @Override
    public void destroy(){
        if(clientNetwork != null && clientNetwork.isConnected()){
            clientNetwork.disconnect();
        }
        super.destroy();
    }
}
