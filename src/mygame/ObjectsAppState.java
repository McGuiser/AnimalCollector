/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.AnimEventListener;
import com.jme3.animation.LoopMode;
import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.input.ChaseCamera;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseAxisTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.renderer.Camera;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.util.SkyFactory;
import com.jme3.water.WaterFilter;

/**
 *
 * @author Corey Castillo
 */
public class ObjectsAppState extends AbstractAppState implements ActionListener, AnimEventListener{
    
    private SimpleApplication app;
    public final Node objectsNode = new Node("Objects Node");
    public Spatial human;
    public Spatial dog;
    public Spatial fox;
    public Spatial cat;
    public BulletAppState bulletAppState = new BulletAppState();
    private AnimChannel channel;
    private AnimChannel channelDog;
    private AnimChannel channelFox;
    private AnimChannel channelCat;
    private AnimControl control;
    private AnimControl controlDog;
    private AnimControl controlFox;
    private AnimControl controlCat;
    public boolean compDog = false;
    public boolean compFox = false;
    public boolean compCat = false;
    ChaseCamera mainChaseCam;
    public BetterCharacterControl playerControl;
    public BetterCharacterControl dogControl;
    public BetterCharacterControl foxControl;
    public BetterCharacterControl catControl;
    public Node playerNode = new Node();
    public Node dogNode = new Node();
    public Node foxNode = new Node();
    public Node catNode = new Node();
    private boolean left = false;
    private boolean right = false;
    private boolean up = false;
    private boolean down = false;
    private boolean jump = false;
    private boolean run = false;
    private boolean tp = false;
    private final Vector3f walkDirection = new Vector3f(0,0,0);
    private final Vector3f location = new Vector3f(0,0,0);
    private final Vector3f camDir = new Vector3f(0, 0, 0);
    private final Vector3f camLeft = new Vector3f(0, 0, 0);
    private final Vector3f gravity = new Vector3f(0, -150, 0);
    
    // ***** BETTER PLAYER CONTROL *****
    
    public void playerUpdate() {
        float walkSpeed = 4f;
        camDir.set(app.getCamera().getDirection().multLocal(walkSpeed, 0.0f, walkSpeed));
        camLeft.set(app.getCamera().getLeft().multLocal(walkSpeed, 0.0f, walkSpeed));
        walkDirection.set(0, 0, 0);
        if (left) {
            walkDirection.addLocal(camLeft);
        }
        if (right) {
            walkDirection.addLocal(camLeft.negate());
        }
        if (up) {
            walkDirection.addLocal(camDir);
            if(tp)playerControl.setViewDirection(camDir);
        }
        if (down) {
            walkDirection.addLocal(camDir.negate());
        }
        if (jump){
            playerControl.jump();
        }
        if (run){
            walkSpeed = walkSpeed * 1.5f;
        }
        if (tp){
           mainChaseCam.setEnabled(true);
        }else{
            mainChaseCam.setEnabled(false);
        }
        location.set(human.getLocalTranslation());
        app.getCamera().setLocation(location.add(0,5f,0));
        app.getCamera().setFrustumNear(1.3f);
        playerControl.setWalkDirection(walkDirection.mult(walkSpeed));
        if(!tp)playerControl.setViewDirection(camDir);
    }
    
    public void companionUpdate(Spatial animal, boolean selected, BetterCharacterControl control, AnimChannel channel) {
        float walkSpeed = 1f;
        Vector3f compPos = animal.getLocalTranslation();
        Vector3f playerPos = human.getLocalTranslation();
        Vector3f modelForwardDir = animal.getWorldRotation().mult(Vector3f.UNIT_Z);
        Vector3f view = playerPos.subtract(compPos);
        float d = playerPos.distance(compPos);
        if(d > 20 && selected){
            walkDirection.set(0, 0, 0);
            control.setViewDirection(view);
            view.addLocal(modelForwardDir.mult(walkSpeed));
            control.setWalkDirection(view.setY(0));
            try{
                if(!channel.getAnimationName().equals("Walking")){
                    channel.setAnim("Walking", 0.50f);
                    channel.setSpeed(4);
                    channel.setLoopMode(LoopMode.Loop);
                }else if(compFox){
                    channel.setAnim("Walking", 0.50f);
                    channel.setSpeed(4);
                    channel.setLoopMode(LoopMode.Loop);
                }
            }catch(NullPointerException e){
                channel.setAnim("Walking", 0.50f);
                channel.setSpeed(4);
                channel.setLoopMode(LoopMode.Loop);
            }
        }else if(d <= 10 && selected){
            control.setWalkDirection(Vector3f.ZERO);
            if(!channel.getAnimationName().equals("Idle") && !compFox){
                channel.setAnim("Idle", 0.50f);
                channel.setLoopMode(LoopMode.Loop);
            }
            if(compFox){
                channel.setLoopMode(LoopMode.DontLoop);
            }
        }else if(!selected){
            try{
                if(!channel.getAnimationName().equals("Idle") && control != foxControl){
                    channel.setAnim("Idle", 0.50f);
                    channel.setLoopMode(LoopMode.Loop);
                }else if(control == foxControl){
                    channel.setLoopMode(LoopMode.DontLoop);
                }
            }catch(NullPointerException e){
                channel.setLoopMode(LoopMode.DontLoop);
            }
            control.setWalkDirection(Vector3f.ZERO);
        }
    }

    private void initKeys() {
        app.getInputManager().addMapping("Left", new KeyTrigger(KeyInput.KEY_A));
        app.getInputManager().addMapping("Right", new KeyTrigger(KeyInput.KEY_D));
        app.getInputManager().addMapping("Walk", new KeyTrigger(KeyInput.KEY_W));
        app.getInputManager().addMapping("Down", new KeyTrigger(KeyInput.KEY_S));
        app.getInputManager().addMapping("Jump", new KeyTrigger(KeyInput.KEY_SPACE));
        app.getInputManager().addMapping("Run", new KeyTrigger(KeyInput.KEY_LSHIFT));
        app.getInputManager().addMapping("Punch", new  MouseButtonTrigger(MouseInput.BUTTON_RIGHT));
        app.getInputManager().addMapping("Third", new KeyTrigger(KeyInput.KEY_V));
        app.getInputManager().addListener(this, "Left");
        app.getInputManager().addListener(this, "Right");
        app.getInputManager().addListener(this, "Walk");
        app.getInputManager().addListener(this, "Down");
        app.getInputManager().addListener(this, "Jump");
        app.getInputManager().addListener(this, "Run");
        app.getInputManager().addListener(this, "Punch");
        app.getInputManager().addListener(this, "Third");
    }
    
    public void createPlayerControl(){
        app.getRootNode().attachChild(playerNode);
        playerControl = new BetterCharacterControl(2f, 6, 250);
        human.addControl(playerControl);
        bulletAppState.getPhysicsSpace().add(playerControl);
        playerControl.setGravity(gravity);
        playerControl.warp(new Vector3f(0, 1, 10));
    }
    
    public void createDogControl(){
        app.getRootNode().attachChild(dogNode);
        dogControl = new BetterCharacterControl(2f, 5f, 250);
        dogControl.resetForward(Vector3f.UNIT_X.negate());
        dog.addControl(dogControl);
        bulletAppState.getPhysicsSpace().add(dogControl);
        dogControl.setGravity(gravity);
        dogControl.warp(new Vector3f(10, 1, 10));
    }
    
    public void createFoxControl(){
        foxControl = new BetterCharacterControl(1f, 10f, 450);
        foxControl.resetForward(Vector3f.UNIT_Z.negate());
        foxNode.addControl(foxControl);
        bulletAppState.getPhysicsSpace().add(foxControl);
        foxControl.setGravity(gravity);
        foxControl.warp(new Vector3f(20, 1, 20));
    }
    
    public void createCatControl(){
        app.getRootNode().attachChild(catNode);
        catControl = new BetterCharacterControl(2f, 5f, 250);
        catControl.resetForward(Vector3f.UNIT_X.negate());
        cat.addControl(catControl);
        bulletAppState.getPhysicsSpace().add(catControl);
        catControl.setGravity(gravity);
        catControl.warp(new Vector3f(-20, 1, -20));
    }
    
    
    
    public Spatial createHuman(){
        human = app.getAssetManager().loadModel("Models/animatedhuman.j3o");
        Material humanMat = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        humanMat.setTexture("ColorMap", app.getAssetManager().loadTexture("Textures/ClothedLightSkin2.png"));
        human.setMaterial(humanMat);
        human.setShadowMode(ShadowMode.CastAndReceive);
        human.scale(1f);
        return human;
    }
    
    public Spatial createDog(){
        dog = app.getAssetManager().loadModel("Models/Wolf.j3o");
        dog.setShadowMode(ShadowMode.CastAndReceive);
        dog.scale(.75f);
        return dog;
    }
    
    public Node createFox(){
        foxNode = new Node("fx");
        fox = app.getAssetManager().loadModel("Models/Red Fox.j3o");
        fox.setShadowMode(ShadowMode.CastAndReceive);
        foxNode.attachChild(fox);
        fox.setLocalTranslation(0, 1.5f, 0);
        return foxNode;
    }
    
    public Spatial createCat(){
        cat = app.getAssetManager().loadModel("Models/Cat.j3o");
        cat.setShadowMode(ShadowMode.CastAndReceive);
        cat.scale(.75f);
        return cat;
    }
    
    // ***** LANDSCAPE *****
    
    public void createWater(){
        FilterPostProcessor fpp = new FilterPostProcessor(app.getAssetManager());
        WaterFilter water = new WaterFilter(app.getRootNode(), new Vector3f(-1, 0, 0));
        fpp.addFilter(water);
        app.getViewPort().addProcessor(fpp);
    }
    
    public Node createTrees(){
        Node treeNode = new Node("trees");
        Spatial tree1 = app.getAssetManager().loadModel("Models/SmallTreeWithLeave.j3o");
        Spatial tree2 = app.getAssetManager().loadModel("Models/BigTreeWithLeaves.j3o");
        Material treeMat = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        treeMat.setTexture("ColorMap", app.getAssetManager().loadTexture("Textures/TreeTexture.png"));
        tree1.setMaterial(treeMat);
        tree2.setMaterial(treeMat);
        tree1.scale(5f);
        tree2.scale(5f);
        tree1.setLocalTranslation(-6, 1, -15);
        tree2.setLocalTranslation(-40, 1, -9);
        tree1.setShadowMode(ShadowMode.CastAndReceive);
        tree2.setShadowMode(ShadowMode.CastAndReceive);
        treeNode.attachChild(tree1);
        treeNode.attachChild(tree2);
        RigidBodyControl treePhysics = new RigidBodyControl(0f);
        treeNode.addControl(treePhysics);
        bulletAppState.getPhysicsSpace().add(treePhysics);
        return treeNode;
    }
    
    public Node createCloudes(){
        Node cloudNode = new Node("clouds");
        Spatial cloud1 = app.getAssetManager().loadModel("Models/Cloud1.j3o");
        Spatial cloud2 = app.getAssetManager().loadModel("Models/Cloud2.j3o");
        cloud1.scale(5f);
        cloud2.scale(5f);
        cloud1.setLocalTranslation(25, 70, 75);
        cloud2.setLocalTranslation(-25, 70, -40);
        cloud1.setShadowMode(ShadowMode.Cast);
        cloud2.setShadowMode(ShadowMode.Cast);
        cloudNode.attachChild(cloud1);
        cloudNode.attachChild(cloud2);
        return cloudNode;
    }
    
    public void thirdPersonView(){
        Camera cam2 = app.getCamera().clone();
        ChaseCamera chaseCam = new ChaseCamera(cam2, human);
        cam2.setViewPort(0f, 0.25f, 0f, 0.25f);
        cam2.setLocation(new Vector3f(0, 10, 0));
        cam2.lookAtDirection(new Vector3f(0, -1, 10), Vector3f.UNIT_X);
        cam2.setRotation(Quaternion.DIRECTION_Z);
        ViewPort viewPort = app.getRenderManager().createPostView("Camera 2", cam2);
        viewPort.setEnabled(true);
        viewPort.setClearFlags(true, true, true);
        viewPort.attachScene(app.getRootNode());
        viewPort.setBackgroundColor(ColorRGBA.Red);
    }
    
    public Node buildFPS(){
        Spatial terrain = app.getAssetManager().loadModel("Textures/beachTerrain.j3o"); // Load terrain and sky
        Spatial sky = SkyFactory.createSky(app.getAssetManager(),"Scenes/Beach/FullskiesSunset0068.dds", false);
        RigidBodyControl terrainPhysics = new RigidBodyControl(0f);
        terrain.addControl(terrainPhysics);
        terrain.setShadowMode(ShadowMode.Receive);
        bulletAppState.getPhysicsSpace().add(terrainPhysics);
        objectsNode.attachChild(sky);
        objectsNode.attachChild(terrain);
        createWater();
        objectsNode.attachChild(createHuman());
        objectsNode.attachChild(createDog());
        objectsNode.attachChild(createFox());
        objectsNode.attachChild(createCat());
        objectsNode.attachChild(createTrees());
        objectsNode.attachChild(createCloudes());
        createPlayerControl();
        createDogControl();
        createFoxControl();
        createCatControl();
        control = human.getParent().getChild("Human_Mesh").getControl(AnimControl.class);
        controlDog = dog.getParent().getChild("Wolf").getControl(AnimControl.class);
        controlFox = fox.getParent().getChild("Cylinder").getControl(AnimControl.class);
        controlCat = cat.getParent().getChild("Cat").getControl(AnimControl.class);
        control.addListener(this);
        controlDog.addListener(this);
        controlFox.addListener(this);
        controlCat.addListener(this);
        channel = control.createChannel();
        channelDog = controlDog.createChannel();
        channelFox = controlFox.createChannel();
        channelCat = controlCat.createChannel();
        channel.setAnim("Idle");
        channelDog.setAnim("Idle");
        channelCat.setAnim("Idle");
        mainChaseCam = new ChaseCamera(app.getCamera(), human, app.getInputManager());
        mainChaseCam.setToggleRotationTrigger(new MouseAxisTrigger(0, true), new MouseAxisTrigger(1, true));
        mainChaseCam.setLookAtOffset(Vector3f.UNIT_XYZ.add(new Vector3f(2, 3, 3)));
        mainChaseCam.setMinDistance(9);
        mainChaseCam.setMaxDistance(9);
        mainChaseCam.setDownRotateOnCloseViewOnly(false);
        mainChaseCam.setEnabled(false);
        return objectsNode;
    }
    
    // *****APP STATE METHODS *****
    
    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        stateManager.attach(bulletAppState);
        super.initialize(stateManager, app);
        this.app = (SimpleApplication) app;
        this.app.getRootNode().attachChild(buildFPS());
        initKeys();
        thirdPersonView();
    }
    
    @Override
    public void cleanup() {
        super.cleanup();
        this.app.getRootNode().detachChild(objectsNode);
    }
    
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (enabled) {
            this.app.getRootNode().attachChild(objectsNode);
        } else {
            this.app.getRootNode().detachChild(objectsNode);
        }
    }
    
    @Override
    public void update(float tpf) {
        playerUpdate();
        companionUpdate(dog, compDog, dogControl, channelDog);
        companionUpdate(foxNode, compFox, foxControl, channelFox);
        companionUpdate(cat, compCat, catControl, channelCat);
    }

    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        switch (name) {
            case "Left":
                left = isPressed;
                channel.setAnim("Walk", 0.50f);
                channel.setLoopMode(LoopMode.Loop);
                break;
            case "Right":
                right = isPressed;
                channel.setAnim("Walk", 0.50f);
                channel.setLoopMode(LoopMode.Loop);
                break;
            case "Walk":
                up = isPressed;
                channel.setAnim("Walk", 0.50f);
                channel.setLoopMode(LoopMode.Loop);
                break;
            case "Down":
                down = isPressed;
                channel.setAnim("Walk", 0.50f);
                channel.setLoopMode(LoopMode.Loop);
                break;
            case "Jump":
                jump = isPressed;
                channel.setAnim("Jump", 0.50f);
                channel.setLoopMode(LoopMode.DontLoop);
                break;
            case "Run":
                run = isPressed;
                channel.setAnim("Run", 0.50f);
                channel.setLoopMode(LoopMode.Loop);
                break;
            case "Punch":
                channel.setAnim("Punch", 0.50f);
                channel.setLoopMode(LoopMode.DontLoop);
                break;
            case "Third":
                if(isPressed) tp = !tp;
                break;
            default:
                break;
        }
    }

    @Override
    public void onAnimCycleDone(AnimControl control, AnimChannel channel, String animName) {
        if (animName.equals("Walk") && up == false && down == false && left == false && right == false) {
            if(run == true){
                channel.setAnim("Run", 0.50f);
                channel.setLoopMode(LoopMode.Loop);
            }else if(jump == true){
                channel.setAnim("Jump", 0.50f);
                channel.setLoopMode(LoopMode.DontLoop);
                channel.setSpeed(1f);
            }else{
                channel.setAnim("Idle", 0.50f);
                channel.setLoopMode(LoopMode.DontLoop);
                channel.setSpeed(1f);
            }
        }
        if (animName.equals("Run") && run == false) {
            if(up == true){
                channel.setAnim("Walk", 0.50f);
                channel.setLoopMode(LoopMode.Loop);
            }else if(jump == true){
                channel.setAnim("Jump", 0.50f);
                channel.setLoopMode(LoopMode.DontLoop);
                channel.setSpeed(1f);
            }else{
                channel.setAnim("Idle", 0.50f);
                channel.setLoopMode(LoopMode.DontLoop);
                channel.setSpeed(1f);
            }
        }
        if (animName.equals("Jump") && jump == false) {
            if(run == true){
                channel.setAnim("Run", 0.50f);
                channel.setLoopMode(LoopMode.Loop);
            }else if(up == true){
                channel.setAnim("Walk", 0.50f);
                channel.setLoopMode(LoopMode.Loop);
                channel.setSpeed(1f);
            }else{
                channel.setAnim("Idle", 0.50f);
                channel.setLoopMode(LoopMode.DontLoop);
                channel.setSpeed(1f);
            }
        }
        if (animName.equals("Punch")) {
            if(run == true){
                channel.setAnim("Run", 0.50f);
                channel.setLoopMode(LoopMode.Loop);
            }else if(up == true){
                channel.setAnim("Walk", 0.50f);
                channel.setLoopMode(LoopMode.Loop);
                channel.setSpeed(1f);
            }else{
                channel.setAnim("Idle", 0.50f);
                channel.setLoopMode(LoopMode.DontLoop);
                channel.setSpeed(1f);
            }
        }
    }

    @Override
    public void onAnimChange(AnimControl control, AnimChannel channel, String animName) {
        // NOT USED
    }
}
