package polarity.client.network;

import com.jme3.audio.AudioNode;
import com.jme3.math.FastMath;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.network.*;
import com.jme3.network.serializing.Serializer;
import com.jme3.scene.Node;
import polarity.client.main.GameClient;
import polarity.client.screens.GameScreen;
import polarity.client.screens.MenuScreen;
import polarity.client.screens.MultiplayerScreen;
import polarity.shared.character.CharacterManager;
import polarity.shared.character.LivingCharacter;
import polarity.shared.character.data.MonsterData;
import polarity.shared.character.data.PlayerData;
import polarity.shared.entity.MonsterEntity;
import polarity.shared.equipment.Equipment;
import polarity.shared.events.ProjectileEvent;
import polarity.shared.input.InputHandler;
import polarity.shared.netdata.*;
import polarity.shared.netdata.destroyers.*;
import polarity.shared.netdata.testing.DevLogData;
import polarity.shared.netdata.updates.*;
import polarity.shared.network.*;
import polarity.shared.screens.Screen;
import polarity.shared.spellforge.SpellMatrix;
import polarity.shared.spellforge.nodes.GeneratorData;
import polarity.shared.tools.Sys;
import polarity.shared.tools.Util;

import java.io.IOException;
import java.util.concurrent.Callable;

/**
 * ClientNetwork - Used for the connection and maintainence of client-side networking.
 * @author SinisteRing
 */
public class ClientNetwork extends GameNetwork {
    public static ClientNetwork Instance = null;
    // Important variables:
    private ClientListener listener = new ClientListener();
    protected final GameClient app;
    protected Client client = null;  // For SpiderMonkey connectivity.
    
    // Game variables:
    protected InputHandler inputHandler;
    protected CharacterManager charManager;
    protected ServerStatus serverStatus;
    
    // Constants:
    public static final float PING_INTERVAL = 1;
    
    // ClientNetwork Variables:
    private boolean CLIENT_CONNECTED = false;
    private int     CLIENT_ID = -1;
    
    // Index Holders:
    private final int PING = 0;
    private final int MOVE = 1;
    
    // Instance Variables:
    private boolean pinging = false;
    private float[] timers = new float[2];
    private float time;
    
    public ClientNetwork(GameClient app, Node root, Node gui){
        Util.log("[ClientNetwork] <Initialize> Initializing ClientNetwork...", 2);
        this.app = app;
        this.charManager = app.getCharManager();
        Instance = this; // Set the ClientNetwork instance.
    }
    
    // Attempted connection, will fail if the server is not available.
    public boolean connect(String ip){
        try {
            client = Network.connectToServer(ip, 6143);
            registerSerials();
            client.addClientStateListener(listener);
            client.start();
            if(System.getProperty("user.name").equals("SinisteRing")){
                client.send(new ConnectData(Sys.getVersion(), "Sindusk"));
            }else{
                client.send(new ConnectData(Sys.getVersion(), System.getProperty("user.name")));
            }
            timers[PING] = 1;
            timers[MOVE] = 0;
            return true;
        } catch (IOException ex) {
            Util.log(ex);
            return false;
        }
    }
    
    // Primary update loop
    public void update(float tpf){
        if(!client.isConnected()){
            return;
        } 
        int i = 0;
        while(i < timers.length){
            timers[i] += tpf;
            i++;
        }
        
        // Ping:
        if(timers[PING] >= PING_INTERVAL && !pinging){
            time = app.getTimer().getTimeInSeconds();
            client.send(new PingData());
            timers[PING] = 0;
        }
        
        // Send updated movement data:
        if(timers[MOVE] >= MOVE_INTERVAL){
            Util.log("[ClientNetwork] <update> Sending my ("+CLIENT_ID+") movement...", 4);
            client.send(new MoveData(CLIENT_ID, charManager.getPlayer(CLIENT_ID).getLocation(), inputHandler.getCursorLocWorld()));
            timers[MOVE] = 0;
        }
    }
    
    public boolean isConnected(){
        return CLIENT_CONNECTED;
    }
    public int getID(){
        Util.log("[ClientNetwork] <getID> CLIENT_ID = "+CLIENT_ID, 4);
        return CLIENT_ID;
    }
    public void setInputHandler(InputHandler inputHandler){
        this.inputHandler = inputHandler;
    }
    
    private void registerClass(Class c){
        Serializer.registerClass(c);
        client.addMessageListener(listener, c);
    }
    private void registerSerials(){
        for(NetData d : NetData.values()){
            registerClass(d.c);
        }
    }
    
    public void disconnect(){
        if(!CLIENT_CONNECTED){
            return;
        }
        CLIENT_ID = -1;
        CLIENT_CONNECTED = false;
        client.close();
    }
    
    public void send(Message message){
        if(isConnected()){
            Util.log("[ClientNetwork] <send> Sending message...", 2);
            client.send(message);
        }
    }
    public void stop(){
        client.close();
    }
    
    private class ClientListener implements MessageListener<Client>, ClientStateListener{
        private Client client;
        
        public void clientConnected(Client c){
            Util.log("Connection to server successful.");
        }
        public void clientDisconnected(Client c, DisconnectInfo info){
            if(info != null){
                Util.log(info.reason);
            }
            inputHandler.switchScreens(new MenuScreen(app, Screen.getTopRoot(), Screen.getTopGUI()));
        }
        
        // HANDSHAKE PROCESS BEGINS
        
        // SSPD on  - Recieves player data + id, let the server know you're ready for world data
        private void PlayerConnectionMessage(final PlayerConnectionData d){
            Util.log("[ClientNetwork] <PlayerConnectionMessage> Receiving PlayerConnectionMessage...", 4);
            CLIENT_ID = d.getID();
            client.send(d.getPlayerData());
            app.enqueue(new Callable<Void>(){
                public Void call() throws Exception{
                    charManager.addPlayer(app.getWorld(), d.getPlayerData());
                    inputHandler.switchScreens(new GameScreen(app, Screen.getTopRoot(), Screen.getTopGUI()));
                    CLIENT_CONNECTED = true;
                    return null;
                }
            });
        }
        
        // SSPD off - Recieves player ID, must send data back to server with player data
        /** When the server has determined the players ID, this message sends it to the player.
         * <p>
         * This is also only recieved if the player connects successfully, so it is best used
         * to actually begin entering the game and starting play.
         * @param d The data from the message.
         */
        private void PlayerIDMessage(final PlayerIDData d){
            Util.log("[ClientNetwork] <PlayerIDMessage> Recieving PlayerIDMessage...", 4);
            CLIENT_ID = d.getID();
            final PlayerData pd = new PlayerData(CLIENT_ID, "Player "+CLIENT_ID, new Vector2f(0, 0), new Equipment());
            client.send(pd);
            app.enqueue(new Callable<Void>(){
                public Void call() throws Exception{
                    charManager.addPlayer(app.getWorld(), pd);
                    inputHandler.switchScreens(new GameScreen(app, Screen.getTopRoot(), Screen.getTopGUI()));
                    CLIENT_CONNECTED = true;
                    return null;
                }
            });
        }
        
        // HANDSHAKE PROCESS ENDS
        
        // SPELL MATRIX
        
        private void GeneratorPowerMessage(final GeneratorPowerUpdate d){
            app.enqueue(new Callable<Void>(){
                public Void call() throws Exception{
                    SpellMatrix matrix = charManager.getPlayer(CLIENT_ID).getMatrix(d.getSlot());
                    GeneratorData data = (GeneratorData) matrix.getSpellNode(d.getIndex().x, d.getIndex().y).getData();
                    data.setStoredPower(d.getPower());
                    return null;
                }
            });
        }
        
        public void MatrixUpdateMessage(final MatrixUpdate d){
            app.enqueue(new Callable<Void>(){
                public Void call() throws Exception{
                    charManager.updateMatrix(d);
                    return null;
                }
            });
        }
        
        // END SPELL MATRIX
        // MOB/ENTITIES
        
        private void MonsterMessage(final MonsterData d){
            app.enqueue(new Callable<Void>(){
                public Void call() throws Exception{
                    charManager.addMonster(app.getWorld(), d);
                    return null;
                }
            });
        }
        private void MonsterStateMessage(final MonsterStateUpdate d){
            app.enqueue(new Callable<Void>(){
                public Void call() throws Exception{
                    MonsterEntity entity = (MonsterEntity) charManager.getMonster(d.getID()).getEntity();
                    entity.getNameplate().updateName(d.getStateName());
                    return null;
                }
            });
        }
        
        // END MOB/ENTITES
        // WORLD
        
        private void ChunkMessage(final ChunkData d){
            app.enqueue(new Callable<Void>(){
                public Void call() throws Exception{
                    app.getWorld().updateChunk(d);
                    return null;
                }
            });
        }
        
        // END WORLD
        // CHAT
        
        private void ChatMessage(final ChatMessage d){
            app.enqueue(new Callable<Void>(){
                public Void call() throws Exception{
                    if(inputHandler.getScreen() instanceof GameScreen){
                        GameScreen screen = (GameScreen) inputHandler.getScreen();
                        screen.chatMessage(d);
                    }
                    return null;
                }
            });
        }
        
        //END CHAT
        
        private void CommandMessage(final CommandData d){
            app.enqueue(new Callable<Void>(){
                public Void call() throws Exception{
                    Util.handleCommand(d.getCommand());
                    return null;
                }
            });
        }
        
        private void DamageMessage(final DamageData d){
            app.enqueue(new Callable<Void>(){
                public Void call() throws Exception{
                    charManager.damage(d);
                    return null;
                }
            });
        }
        
        /** This is the message sent when the player is disconnected.
         * <p>
         * It holds certain data for the disconnect message, and potentially other misc data.
         * @param d The data from the message.
         */
        private void DisconnectMessage(final DisconnectData d){
            Util.log("[ClientNetwork] <DisconnectMessage> Recieving DisconnectMessage...", 1);
            app.enqueue(new Callable<Void>(){
                public Void call() throws Exception{
                    charManager.removePlayer(d.getID());
                    return null;
                }
            });
        }
        
        private void HealMessage(final HealData d){
            app.enqueue(new Callable<Void>(){
                public Void call() throws Exception{
                    charManager.heal(d);
                    return null;
                }
            });
        }
        
        /** When a player moves to a new position, this message is recieved to update all clients of the player's new position.
         * <p>
         * The client will interpet the interpolation over time to
         * the new location, but this will give the final location
         * of where they're at on the server.
         * @param d The data from the message.
         */
        private void MoveMessage(final MoveData d){
            app.enqueue(new Callable<Void>(){
                public Void call() throws Exception{
                    charManager.updatePlayerLocation(d);
                    return null;
                }
            });
        }
        private void PingMessage(){
            final float pingTime = app.getTimer().getTimeInSeconds() - time;
            app.enqueue(new Callable<Void>(){
                public Void call() throws Exception{
                    Util.log("[ClientNetwork] <PingMessage> Current Ping: "+(int) FastMath.ceil(pingTime*1000)+" ms", 4);
                    return null;
                }
            });
        }
        
        /**
         * Message responsible for updating all clients of a new player's information.
         * <p>
         * Contains an instance of Player to give to all clients, so they have client-side
         * knowledge of what to do when that player makes an action.
         * @param d The data from the message.
         */
        private void PlayerMessage(final PlayerData d){
            Util.log("[ClientNetwork] <PlayerMessage> Recieving new PlayerMessage...", 2);
            if(d.getID() == CLIENT_ID){
                return;
            }
            app.enqueue(new Callable<Void>(){
                public Void call() throws Exception{
                    charManager.addPlayer(app.getWorld(), d);
                    return null;
                }
            });
        }
        
        /**
         * Message broadcast when a projectile is spawned.
         * <p>
         * Holds all information necessary to spawn the projectile.
         * @param d The data from the message.
         */
        private void ProjectileMessage(final ProjectileData d){
            Util.log("[ClientNetwork] <ProjectileMessage> Recieving new ProjectileMessage...", 2);
            app.enqueue(new Callable(){
                public Void call() throws Exception{
                    app.getWorld().addProjectile(new ProjectileEvent(charManager, d));
                    return null;
                }
            });
        }
        
        /** Server information & status message.
         * <p>
         * The server sends this data to inform the client of crucial
         * information before allowing entry to the server.
         * @param d The data from the message.
         */
        private void ServerStatusMessage(final ServerStatusData d){
            Util.log("[ClientNetwork] <ServerStatusMessage> Recieving new ServerStatusMessage...", 2);
            app.enqueue(new Callable(){
                public Void call() throws Exception{
                    if(!(inputHandler.getScreen() instanceof MultiplayerScreen)){
                        Util.log("Error 5: Server sent status when multiplayer screen was not open.", 0);
                    }
                    serverStatus = d.getStatus();
                    return null;
                }
            });
        }
        
        // Recieved when a sound is played in the server world, and the client needs to know.
        private void SoundMessage(final SoundData d){
            if(d.getID() == CLIENT_ID) {
                return;
            }
            final Vector2f loc = charManager.getPlayer(d.getID()).getLocation();
            app.enqueue(new Callable<Void>(){
                public Void call() throws Exception{
                    AudioNode node = new AudioNode(app.getAssetManager(), d.getSound());
                    node.setPositional(true);
                    node.setLocalTranslation(new Vector3f(loc.x, loc.y, 0));
                    node.playInstance();
                    return null;
                }
            });
        }
        
        private void StatusMessage(final StatusData d){
            app.enqueue(new Callable<Void>(){
                public Void call() throws Exception{
                    ((LivingCharacter)charManager.getOwner(d.getOwner())).applyStatus(d.getStatus());
                    return null;
                }
            });
        }
        
        private void DestroyProjectile(final DestroyProjectileData d){
            app.enqueue(new Callable<Void>(){
                public Void call() throws Exception{
                    app.getWorld().destroyProjectile(d.getHashCode());
                    return null;
                }
            });
        }
        private void DestroyStatus(final DestroyStatusData d){
            app.enqueue(new Callable<Void>(){
                public Void call() throws Exception{
                    ((LivingCharacter)charManager.getOwner(d.getOwner())).removeStatus(d.getStatus());
                    return null;
                }
            });
        }
        
        // Handler for when any message (generic) is recieved.
        // It then separates it by type and calls the proper sub-method to handle each instance.
        public void messageReceived(Client source, Message m) {
            client = source;
            
            // Handshaking
            if(m instanceof PlayerConnectionData){
                PlayerConnectionMessage((PlayerConnectionData) m);
            }else if(m instanceof PlayerIDData){
                PlayerIDMessage((PlayerIDData) m);
            // Spell Matrix Updates
            }else if(m instanceof GeneratorPowerUpdate){
                GeneratorPowerMessage((GeneratorPowerUpdate) m);
            }else if(m instanceof MatrixUpdate){
                MatrixUpdateMessage((MatrixUpdate) m);
            // Entities/Mobs
            }else if(m instanceof MonsterData){
                MonsterMessage((MonsterData) m);
            }else if(m instanceof MonsterStateUpdate){
                MonsterStateMessage((MonsterStateUpdate) m);
            // World Messages
            }else if(m instanceof ChunkData){
                ChunkMessage((ChunkData) m);
            }else if(m instanceof ChatMessage){
                ChatMessage((ChatMessage) m);
            // Uncategorized
            }else if(m instanceof CommandData){
                CommandMessage((CommandData) m);
            }else if(m instanceof DamageData){
                DamageMessage((DamageData) m);
            }else if(m instanceof DisconnectData){
                DisconnectMessage((DisconnectData) m);
            }else if(m instanceof HealData){
                HealMessage((HealData) m);
            }else if(m instanceof MoveData){
                MoveMessage((MoveData) m);
            }else if(m instanceof PingData){
                PingMessage();
            }else if(m instanceof PlayerData){
                PlayerMessage((PlayerData) m);
            }else if(m instanceof ProjectileData){
                ProjectileMessage((ProjectileData) m);
            }else if(m instanceof ServerStatusData){
                ServerStatusMessage((ServerStatusData) m);
            }else if(m instanceof SoundData){
                SoundMessage((SoundData) m);
            }else if(m instanceof StatusData){
                StatusMessage((StatusData) m);
            }
            // Data cleanup/destruction messages
            else if(m instanceof DestroyProjectileData){
                DestroyProjectile((DestroyProjectileData) m);
            }else if(m instanceof DestroyStatusData){
                DestroyStatus((DestroyStatusData) m);
            }
            // Development testing
            else if(m instanceof DevLogData){
                DevLogData d = (DevLogData) m;
                Util.log(d.getMessage());
            }
        }
    }
}
