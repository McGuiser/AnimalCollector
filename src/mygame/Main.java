package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.audio.AudioData.DataType;
import com.jme3.audio.AudioNode;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.font.BitmapText;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Spatial;
import com.jme3.shadow.DirectionalLightShadowRenderer;
import com.jme3.system.AppSettings;

/**
 * @author Corey Castillo
 */

public class Main extends SimpleApplication{
    
    ObjectsAppState objects = new ObjectsAppState();

    
    // ***** LIGHTING *****
    
    public void createLight(){
        AmbientLight ambient = new AmbientLight();
        ambient.setColor(ColorRGBA.White.mult(.75f));
        rootNode.addLight(ambient);
        DirectionalLight sun = new DirectionalLight();
        sun.setDirection(new Vector3f(-1, .05f, 1));
        sun.setColor(ColorRGBA.White.mult(.25f));
        DirectionalLightShadowRenderer shadow = new DirectionalLightShadowRenderer(assetManager, 512, 2);
        shadow.setLight(sun);
        viewPort.addProcessor(shadow);
        rootNode.addLight(sun);
    }
    
    
    // ***** HUD METHODS *****
    
    public BitmapText hudText(String dispText, ColorRGBA color){
        BitmapText text = new BitmapText(guiFont, false);
        text.setSize(guiFont.getCharSet().getRenderedSize());
        text.setColor(color);
        text.setText(dispText);
        return text;
    }
    
    public void inventory(int numItems) {
        String armorString = "Inventory: " + numItems;
        BitmapText armorText = hudText(armorString, ColorRGBA.White);
        armorText.setLocalTranslation(250, 720, 0);
        guiNode.attachChild(armorText);
    }
    
    public void currentLocation(String city) {
        String airString = "Location: " + city;
        BitmapText airText = hudText(airString, ColorRGBA.Blue);
        airText.setLocalTranslation(478, 720, 0);
        guiNode.attachChild(airText);
    }
    
    public void crossHair() {
        String crossString = "+";
        BitmapText crossHairText = hudText(crossString, ColorRGBA.Red);
        crossHairText.setLocalTranslation(635, 370, 0);
        guiNode.attachChild(crossHairText);
    }
    
    // ***** USER INTERACTION METHODS *****
    
    public void boopSound(){
        AudioNode boopSound  = new AudioNode(assetManager, "Sounds/Boop.wav", DataType.Buffer);
        boopSound.setLooping(false);
        boopSound.setPositional(false);
        boopSound.setVolume(.3f);
        rootNode.attachChild(boopSound);
        boopSound.playInstance();
    }

    private void addCollision(Spatial spatial){
        CollisionResults results = new CollisionResults();
        Ray ray = new Ray(cam.getLocation(), cam.getDirection());
        spatial.collideWith(ray, results);
        if (results.size() > 1){
            CollisionResult closest = results.getCollision(1);
            if(closest.getGeometry().getParent().getName().equals("Cylinder")){
                objects.compDog = false;
                objects.compCat = false;
                objects.compFox = !objects.compFox;
            }
            if(closest.getGeometry().getParent().getName().equals("Wolf")){
                objects.compFox = false;
                objects.compCat = false;
                objects.compDog = !objects.compDog;
            }
            if(closest.getGeometry().getParent().getName().equals("Cat")){
                objects.compFox = false;
                objects.compDog = false;
                objects.compCat = !objects.compCat;
            }
            System.out.println(closest.getGeometry().getParent().getName());
        }
    }
    
    // ***** TRIGGERS AND MAPS *****
    
    private final static MouseButtonTrigger TRIGGER_MOUSE_BUTTON_LEFT = new MouseButtonTrigger(MouseInput.BUTTON_LEFT);
    private final static String MAP_SHOOT = "Shoot";
    
    private final ActionListener actionListener = new ActionListener() {
        @Override
        public void onAction(String name, boolean isPressed, float tpf) {
            if (null != name) switch (name) {
                case MAP_SHOOT:
                    if(!isPressed){
                        boopSound();
                        //addHit();
                        addCollision(objects.objectsNode);  
                    }
                    break;
                default:
                        break;
                    }
        }
    };
    
    private void initKeys() {
        inputManager.addMapping(MAP_SHOOT, TRIGGER_MOUSE_BUTTON_LEFT);
        inputManager.addListener(actionListener, MAP_SHOOT);
    }
    
    
    
    // ***** BUILD METHODS *****
    
    public void createSound() {
        AudioNode bgMusic  = new AudioNode(assetManager, "Sounds/wind.wav", DataType.Stream);
        bgMusic.setPositional(false);
        bgMusic.setVolume(.25f);
        rootNode.attachChild(bgMusic);
        bgMusic.play();
    }
    
    public void buildHUD(){
        crossHair();
    }
    
    public static void main(String[] args) {
        Main app = new Main();
        AppSettings settings = new AppSettings(true);
        settings.setResolution(1280, 720);
        settings.setFrameRate(60);
        app.setSettings(settings);
        app.setShowSettings(false);
        app.setDisplayFps(true);
        app.setDisplayStatView(false);
        app.start();
    }

    @Override
    public void simpleInitApp() {
        initKeys();
        stateManager.attach(objects);
        createLight();
        createSound();
        initKeys();
        buildHUD();
    }

    @Override
    public void simpleUpdate(float tpf) {
        //TODO: add update code
    }

    @Override
    public void simpleRender(RenderManager rm) {
        //TODO: add render code
    }
}