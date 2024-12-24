package feri;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import org.fxyz3d.shapes.primitives.Text3DMesh;
import org.poly2tri.geometry.polygon.PolygonPoint;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.geometry.Pos;
import javafx.scene.Camera;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.Shape3D;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;


public class Y24GUIOutput3D23 extends Application {

	static Y24GUIOutput3D23 instance = null;
	
	public static class DDDObject {
		String id;
		double x;
		double y;
		double z;
		double size;
		int type;

		public DDDObject(double x, double y, double z, double size, int type) {
			this(null, x, y, z, size, type);
		}
		public DDDObject(String id, double x, double y, double z, double size, int type) {
			this.id = id!=null ? id : UUID.randomUUID().toString();
			this.x = x;
			this.y = y;
			this.z = z;
			this.size = size;
			this.type = type;
		}
		
		@Override
		public String toString() {
			return id+"(" + x + "," + y + "," + z + "|" + size + ")";
		}
	}

	public static class DDDLineObject extends DDDObject {
		double x2;
		double y2;
		double z2;
		public DDDLineObject(double x, double y, double z, double x2, double y2, double z2, double size, int type) {
			this(null, x, y, z, x2, y2, z2, size, type);
		}
		public DDDLineObject(String id, double x, double y, double z, double x2, double y2, double z2, double size, int type) {
			super(id, x, y, z, size, type);
			this.x2 = x2;
			this.y2 = y2;
			this.z2 = z2;
		}
	}
	
	private String title;
	private boolean useCachedNodes;
	
	private List<List<DDDObject>> dddObjects;
	private List<String> dddTitles;
	private int currentStep;

	static record NodeInfo(DDDObject dddo, Node node) {}
	Map<String, NodeInfo> nodeInfos;
	
	double scale;
	double offsetX;
	double offsetY;
	double offsetZ;
	double radiusScale;

    Color white; 
    Color red; 
    Color green; 
    Color yellow; 
    Color blue; 
    Color black; 
    PhongMaterial matRed;
    PhongMaterial matGreen;
    PhongMaterial matYellow;
    PhongMaterial matBlue;
    PhongMaterial[] matColor;

	
	public Y24GUIOutput3D23(String title, boolean useCachedNodes) {
		this.title = title;
		this.dddObjects = new ArrayList<>();
		this.dddTitles = new ArrayList<>();
		this.currentStep = -1;
		this.scale = 1.0;
		this.offsetX = 0.0;
		this.offsetY = 0.0;
		this.offsetZ = 0.0;
		this.radiusScale = 2.0;
		this.nodeInfos = new HashMap<>();
		this.useCachedNodes = useCachedNodes;
		initColors();
		open();
	}

	
	public void initColors() {
        white  = new Color(1.0, 1.0, 1.0, 1.0); 
        red    = new Color(1.0, 0.1, 0.1, 1.0); 
        green  = new Color(0.1, 1.0, 0.1, 1.0); 
        blue   = new Color(0.1, 0.1, 1.0, 1.0); 
        yellow = new Color(1.0, 1.0, 0.1, 1.0); 
        black  = new Color(0.0, 0.0, 0.0, 1.0);
        
        matRed = new PhongMaterial();
        matRed.setDiffuseColor(red);
        matRed.setSpecularColor(red);
        matGreen = new PhongMaterial();
        matGreen.setDiffuseColor(green);
        matGreen.setSpecularColor(green);
        matBlue = new PhongMaterial();
        matBlue.setDiffuseColor(blue);
        matBlue.setSpecularColor(blue);
        matYellow = new PhongMaterial();
        matYellow.setDiffuseColor(yellow);
        matYellow.setSpecularColor(yellow);
        
        matColor = new PhongMaterial[] {matRed, matGreen, matBlue, matYellow};
	}
	
	public void setUseCachedNodes(boolean useCachedNodesValue) {
		useCachedNodes = useCachedNodesValue;
	}
	public void clearCache() {
		nodeInfos.clear();
	}

	private Node createSphere(float size, PhongMaterial mat) {
		Node child;
		Sphere sphere = new Sphere(0.5*size);
		sphere.setMaterial(mat);
		child = sphere;
		return child;
	}

	private Node createBox(float size, PhongMaterial mat) {
		Node child;
		Box box = new Box(size, size, size);
		box.setMaterial(mat);
		child = box;
		return child;
	}
	
    static public Cylinder createCylinder(Point3D lineFrom, Point3D lineTo) {
        // x axis vector is <1,0,0>
        // y axis vector is <0,1,0>
        // z axis vector is <0,0,1>
        // angle = arccos((P*Q)/(|P|*|Q|))
        // define a point representing the Y axis
        Point3D yAxis = new Point3D(0,1,0);
        // define a point based on the difference of our end point from the start point of our segment
        Point3D seg = lineTo.subtract(lineFrom);
        // determine the length of our line or the height of our cylinder object
        double height = seg.magnitude();
        // get the midpoint of our line segment
        Point3D midpoint = lineTo.midpoint(lineFrom);
        // set up a translate transform to move to our cylinder to the midpoint
        Translate moveToMidpoint = new Translate(midpoint.getX(), midpoint.getY(), midpoint.getZ());
        // get the axis about which we want to rotate our object
        Point3D axisOfRotation = seg.crossProduct(yAxis);
        // get the angle we want to rotate our cylinder
        double angle = Math.acos(seg.normalize().dotProduct(yAxis));
        // create our rotating transform for our cylinder object
        Rotate rotateAroundCenter = new Rotate(-Math.toDegrees(angle), axisOfRotation);
        // create our cylinder object representing our line
        Cylinder line = new Cylinder(1, height);
        // add our two transfroms to our cylinder object
        line.getTransforms().addAll(moveToMidpoint, rotateAroundCenter);
        // return our cylinder for use      
        return line;
    } // end of the createCylinder method

    static public Box createLineBox(Point3D lineFrom, Point3D lineTo, float size, PhongMaterial mat) {
        // x axis vector is <1,0,0>
        // y axis vector is <0,1,0>
        // z axis vector is <0,0,1>
        // angle = arccos((P*Q)/(|P|*|Q|))
        // define a point representing the Y axis
        Point3D yAxis = new Point3D(0,1,0);
        // define a point based on the difference of our end point from the start point of our segment
        Point3D seg = lineTo.subtract(lineFrom);
        // determine the length of our line or the height of our cylinder object
        double height = seg.magnitude();
        // get the midpoint of our line segment
        Point3D midpoint = lineTo.midpoint(lineFrom);
        // set up a translate transform to move to our cylinder to the midpoint
        Translate moveToMidpoint = new Translate(midpoint.getX(), midpoint.getY(), midpoint.getZ());
        // get the axis about which we want to rotate our object
        Point3D axisOfRotation = seg.crossProduct(yAxis);
        // get the angle we want to rotate our cylinder
        double angle = Math.acos(seg.normalize().dotProduct(yAxis));
        // create our rotating transform for our cylinder object
        Rotate rotateAroundCenter = new Rotate(-Math.toDegrees(angle), axisOfRotation);
        // create our cylinder object representing our line
        Box line = new Box(size, height, size);
        // add our two transfroms to our cylinder object
        line.getTransforms().addAll(moveToMidpoint, rotateAroundCenter);
        // set material
		line.setMaterial(mat);
        // return our line for use
        return line;
    } // end of the createCylinder method
	

	private void setColor(Node node, PhongMaterial mat) {
		if (node instanceof Shape3D) {
			((Shape3D) node).setMaterial(mat);
		}
	}

	private void setBoxSize(Node child, float size) {
		Box box = (Box) child;
		box.setDepth(size);
		box.setWidth(size);
		box.setHeight(size);
	}

	private void setSphereSize(Node child, float size) {
		Sphere sphere = (Sphere) child;
		sphere.setRadius(0.5*size);
	}

	private void setLineSizeAndPos(Node child, Point3D lineFrom, Point3D lineTo, float size) {
        Box line = (Box) child;

        // x axis vector is <1,0,0>
        // y axis vector is <0,1,0>
        // z axis vector is <0,0,1>
        // angle = arccos((P*Q)/(|P|*|Q|))
        // define a point representing the Y axis
        Point3D yAxis = new Point3D(0,1,0);
        // define a point based on the difference of our end point from the start point of our segment
        Point3D seg = lineTo.subtract(lineFrom);
        // determine the length of our line or the height of our cylinder object
        double height = seg.magnitude();
        // get the midpoint of our line segment
        Point3D midpoint = lineTo.midpoint(lineFrom);
        // set up a translate transform to move to our cylinder to the midpoint
        Translate moveToMidpoint = (Translate)line.getTransforms().get(0);
        moveToMidpoint.setX(midpoint.getX());
        moveToMidpoint.setY(midpoint.getY());
        moveToMidpoint.setZ(midpoint.getZ());
        
        // get the axis about which we want to rotate our object
        Point3D axisOfRotation = seg.crossProduct(yAxis);
        // get the angle we want to rotate our cylinder
        double angle = Math.acos(seg.normalize().dotProduct(yAxis));
        // create our rotating transform for our cylinder object
        Rotate rotateAroundCenter = (Rotate)line.getTransforms().get(1);
        rotateAroundCenter.setAngle(-Math.toDegrees(angle));
        rotateAroundCenter.setAxis(axisOfRotation);
//        Rotate rotateAroundCenter = new Rotate(-Math.toDegrees(angle), axisOfRotation);
        
        line.setWidth(size);
        line.setHeight(height);
        line.setDepth(size);
        // add our two transfroms to our cylinder object

//        line.getTransforms().addAll(moveToMidpoint, rotateAroundCenter);
	}



	private Node createNode(DDDObject dddo) {
		Node child;
		float size = (float) (radiusScale * scale * dddo.size);
		boolean doTranslate = true;
		switch (dddo.type) {
		case 0,1,2,3: {
			PhongMaterial mat = matColor[dddo.type];
			child = createBox(size, mat);
			break;
		}
		case 10,11,12,13: {
			PhongMaterial mat = matColor[dddo.type-10];
			child = createSphere(size, mat);
			break;
		}
		case 30,31,32,33: {
			PhongMaterial mat = matColor[dddo.type -30];
			DDDLineObject line = (DDDLineObject)dddo;
			Point3D from = new Point3D(scale*(line.x-offsetX), scale*(line.y-offsetY), scale*(line.z-offsetZ));
			Point3D to = new Point3D(scale*(line.x2-offsetX), scale*(line.y2-offsetY), scale*(line.z2-offsetZ));
			child = createLineBox(from, to, (float)(size), mat);
			doTranslate = false;
			break;
		}
		default:
			throw new RuntimeException("invalid type " + dddo.type);
		}
		if (doTranslate) {
			child.setTranslateX(scale*(dddo.x-offsetX));
			child.setTranslateY(scale*(dddo.y-offsetY));
			child.setTranslateZ(scale*(dddo.z-offsetZ));
		}
		return child;
	}

	private void updateNode(Node child, DDDObject dddo) {
		float size = (float) (radiusScale * scale * dddo.size);
		boolean doTranslate = true;
		switch (dddo.type) {
		case 0,1,2,3: {
			PhongMaterial mat = matColor[dddo.type];
			setColor(child, mat);
			setBoxSize(child, size);
			break;
		}
		case 10,11,12,13: {
			PhongMaterial mat = matColor[dddo.type-10];
			setColor(child, mat);
			setSphereSize(child, size);
			break;
		}
		case 30,31,32,33: {
			PhongMaterial mat = matColor[dddo.type-30];
			setColor(child, mat);
			DDDLineObject line = (DDDLineObject)dddo;
			Point3D from = new Point3D(scale*(line.x-offsetX), scale*(line.y-offsetY), scale*(line.z-offsetZ));
			Point3D to = new Point3D(scale*(line.x2-offsetX), scale*(line.y2-offsetY), scale*(line.z2-offsetZ));
			setLineSizeAndPos(child, from, to, (float)(size));
			doTranslate = false;
			break;
		}
		default:
			throw new RuntimeException("invalid type " + dddo.type);
		}
		if (doTranslate) {
			child.setTranslateX(scale*(dddo.x-offsetX));
			child.setTranslateY(scale*(dddo.y-offsetY));
			child.setTranslateZ(scale*(dddo.z-offsetZ));
		}
		child.setVisible(true);
	}

	
	public void updateNodes(Group parentGroup, List<DDDObject> dddOs) {

		Set<String> missingNodeIDs = new HashSet<>(nodeInfos.keySet());
		for (DDDObject dddo : dddOs) {
			missingNodeIDs.remove(dddo.id);
			NodeInfo nodeInfo = nodeInfos.get(dddo.id);
			if (nodeInfo == null) {
				Node child = createNode(dddo);
				nodeInfos.put(dddo.id, new NodeInfo(dddo, child));
				parentGroup.getChildren().add(child);
			}
			else {
				Node child = nodeInfo.node;
				updateNode(child, dddo);
			}
		}
		for (String nodeID:missingNodeIDs) {
			Node node = nodeInfos.get(nodeID).node;
			node.setVisible(false);
		}
	}



    private int nextPage = -1;

    private synchronized void switchPage(int page) {
    	if (nextPage == -1) {
        	nextPage = page;
        	Platform.runLater(() -> asyncSwitchPage());
        	return;
    	}
    	nextPage = page;
    }

    
    private synchronized void asyncSwitchPage() {
    	int targetPage = Math.min(dddObjects.size()-1, Math.max(0, nextPage));
    	if (currentStep != targetPage) {
    		currentStep = targetPage;
    		refreshCanvas();
        	lbTextID.setText(Integer.toString(currentStep)+" "+dddTitles.get(currentStep));
    	}
    	nextPage = -1;
	}
    
    	
	private synchronized void refreshCanvas() {
		if ((currentStep < 0) || (currentStep >= dddObjects.size())) {
			return;
		}
		List<DDDObject> dddo = dddObjects.get(currentStep);
		updateScene(dddo);
	}

	private void updateScene(List<DDDObject> dddo) {
		Platform.runLater(()->updateSceneAsync(dddo));
	}

	private void updateSceneAsync(List<DDDObject> dddo) {
		if (useCachedNodes) {
			updateNodes(currentScene, dddo);
		}
		if (!useCachedNodes) {
			nodeInfos.clear();
			SmartGroup parentGroup = new SmartGroup();
			updateNodes(parentGroup, dddo);
			rootGroup.getChildren().remove(currentScene);
			rootGroup.getChildren().add(parentGroup);
			currentScene = parentGroup;
		}

	}

	private void previous() {
		switchPage(currentStep-1);
	}

	private void next() {
		switchPage(currentStep+1);
	}

	public void setScale(double scale) {
    	this.scale = scale;
    	refreshCanvas();
	}

	public void adjustScale() {
		if ((currentStep < 0) || (currentStep >= dddObjects.size())) {
			return;
		}
		adjustScale(dddObjects.get(currentStep));
	}

	public void adjustScale(List<DDDObject> dddOs) {
		double minXValue = Double.POSITIVE_INFINITY;
		double minYValue = Double.POSITIVE_INFINITY;
		double minZValue = Double.POSITIVE_INFINITY;
		double maxXValue = Double.NEGATIVE_INFINITY;
		double maxYValue = Double.NEGATIVE_INFINITY;
		double maxZValue = Double.NEGATIVE_INFINITY;
		for (DDDObject dddo : dddOs) {
			minXValue = Math.min(minXValue, dddo.x);
			minYValue = Math.min(minYValue, dddo.y);
			minZValue = Math.min(minZValue, dddo.z);
			maxXValue = Math.max(maxXValue, dddo.x);
			maxYValue = Math.max(maxYValue, dddo.y);
			maxZValue = Math.max(maxZValue, dddo.z);
		}
		offsetX = (maxXValue + minXValue) / 2;
		offsetY = (maxYValue + minYValue) / 2;
		offsetZ = (maxZValue + minZValue) / 2;
		double maxDiff = Math.max(maxXValue - minXValue, Math.max(maxYValue - minYValue, maxZValue - minZValue));
		if (maxDiff == 0.0) {
			maxDiff = 1.0;
		}
		this.scale = 2.0 / maxDiff;
		scale = scale * 60;
		refreshCanvas();
	}

	public void scaleUp() {
		scale = 1.5*scale;
		refreshCanvas();
	}

	public void scaleDown() {
		scale = scale/1.5;
		refreshCanvas();
	}

	public void addStep(String title, List<DDDObject> dddO) {
		dddObjects.add(new ArrayList<>(dddO));
		dddTitles.add(title);
		if (currentStep != -1) {
			return;
		}
		currentStep = dddObjects.size()-1;
		refreshCanvas();
		
	}

	private void smaller() {
		radiusScale = 0.5 * radiusScale;
		refreshCanvas();
	}

	private void bigger() {
		radiusScale = 2 * radiusScale;
		refreshCanvas();
	}

	class AnimationTask extends TimerTask {
		@Override public void run() { asyncAnimation(); }
	}
	
	private synchronized void animation() {
    	if (timer!= null) {
    		timer.cancel();
    		timer= null;
    		return;
    	}
    	timer = new Timer();
    	timer.scheduleAtFixedRate(new AnimationTask(), 250, 250);
	}

	private synchronized void asyncAnimation() {
		if (timer == null) {
			return;
		}
		int page = currentStep + 1;
		if (page >= dddObjects.size()) {
			timer.cancel();
			timer = null;
			return;
		}
		switchPage(page);
	}

	private static Random random = new Random();

	public static double rand() {
		return rand(-1.0, 1.0);
	}

	public static double rand(double from, double to) {
		return from + random.nextDouble() * (to - from);
	}

	public static int irand(int from, int to) {
		return from + random.nextInt(to - from + 1);
	}

	
    private static final float WIDTH = 1400;
    private static final float HEIGHT = 1000;

    private double anchorX, anchorY;
    private double anchorAngleX = 0;
    private double anchorAngleY = 0;
    private final DoubleProperty angleX = new SimpleDoubleProperty(0);
    private final DoubleProperty angleY = new SimpleDoubleProperty(0);

    Stage primary;
    SmartGroup rootGroup;
    SmartGroup currentScene;
    SubScene rootScene;
    Label lbTextID;
    Slider slider;
    Timer timer;
    TimerTask timerTask;
    
	@Override
	public void start(Stage primaryStage) throws Exception {
		primary = primaryStage;

		primary.setTitle("3D Output");
		Button btPrevious = new Button("<");
		btPrevious.setOnAction(event -> {
			previous();
		});
		Button btNext = new Button(">");
		btNext.setOnAction(event -> {
			next();
		});
		
		Button btSmaller = new Button("v");
        btSmaller.setOnAction(ev -> {
        	smaller();
        });
        
        Button btBigger = new Button("^");
        btBigger.setOnAction(ev -> {
        	bigger();
        });
		
        Button btAdjustScale = new Button("Adjust Scale");
        btAdjustScale.setOnAction(ev -> {
        	adjustScale();
        });
        
        Button btScaleUp = new Button("+");
        btScaleUp.setOnAction(ev -> {
        	scaleUp();
        });
        
        Button btScaleDown = new Button("-");
        btScaleDown.setOnAction(ev -> {
        	scaleDown();
        });
        
        Button btAnimation = new Button("Animation");
        btAnimation.setOnAction(ev -> {
        	animation();
        });

		lbTextID = new Label("0");
		HBox buttons = new HBox(btPrevious, btNext, btSmaller, btBigger, btAdjustScale, btScaleUp, btScaleDown, btAnimation, lbTextID);
		buttons.setSpacing(5);
//		buttons.setPadding(new Insets(5));
		
        slider = new Slider(0, 10000, 0);
        slider.setOrientation(Orientation.HORIZONTAL);
        slider.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> ov,
                    Number old_val, Number new_val) {
            	double percent = new_val.doubleValue() * 0.0001;
            	int page = (int) (percent * (dddObjects.size()-1));
            	switchPage(page);
                }
            });
        slider.setPrefWidth(WIDTH-50);
		
		VBox container2D = new VBox(buttons, slider);
		container2D.setSpacing(15);
		container2D.setPadding(new Insets(25));
		container2D.setAlignment(Pos.CENTER);
		
		Group group2D = new Group(container2D);
		SubScene subScene2D = new SubScene(group2D, WIDTH, 80);

		SmartGroup group3D = new SmartGroup();
		SubScene subScene3D = new SubScene(group3D, WIDTH, HEIGHT, true, SceneAntialiasing.DISABLED);
		
		VBox vbox = new VBox(subScene2D, subScene3D);
		Group groupALL = new Group(vbox);
		Scene scene = new Scene(groupALL);
		
		currentScene = new SmartGroup();
		
		
		Box box = new Box(1, 1, 1);
		box.setTranslateX(-5);
		box.setTranslateY(-5);
		box.setTranslateZ(0);
//		currentScene.getChildren().add(box);
		
		box = new Box(1, 1, 1);
		box.setTranslateX(-5);
		box.setTranslateY(5);
		box.setTranslateZ(0);
//		currentScene.getChildren().add(box);
		
		box = new Box(1, 1, 1);
		box.setTranslateX(5);
		box.setTranslateY(5);
		box.setTranslateZ(0);
//		currentScene.getChildren().add(box);
		
		box = new Box(1, 1, 1);
		box.setTranslateX(5);
		box.setTranslateY(-5);
		box.setTranslateZ(0);
//		currentScene.getChildren().add(box);
		
	    String TEXT3D = "ABCXDEF";
	    String FONT = "Courier New"; //  "Arial";
	    int FONT_SIZE = 54; // 11; // 
	    boolean JOIN_SEGMENTS = true;
	    double TEXT_DEPTH = 10d;     // 10d;
	    double GAP = 0d;
	    int LEVEL = 1;
		
		Text3DMesh text3D = new Text3DMesh(TEXT3D, FONT, FONT_SIZE, JOIN_SEGMENTS, TEXT_DEPTH, GAP, LEVEL);
		Bounds textBounds = text3D.getLayoutBounds();
		System.out.println(textBounds);
		text3D.setScaleX(10.0d/textBounds.getHeight());
		text3D.setScaleY(10.0d/textBounds.getHeight());
		text3D.setScaleZ(10.0d/textBounds.getHeight());
		text3D.translateXProperty().set(-textBounds.getCenterX());
		text3D.translateYProperty().set(-textBounds.getCenterY());
		text3D.translateZProperty().set(-textBounds.getCenterZ());
//		currentScene.getChildren().add(text3D);
		
		
	    rootGroup = group3D;
	    rootGroup.getChildren().add(currentScene);
        Camera camera = new PerspectiveCamera();
        rootScene = subScene3D;
        rootScene.setFill(Color.SILVER);
        rootScene.setCamera(camera);

        rootGroup.translateXProperty().set(WIDTH / 2);
        rootGroup.translateYProperty().set(HEIGHT / 2);
        rootGroup.translateZProperty().set(-1500);

        initMouseControl(rootGroup, scene, primaryStage);

        primaryStage.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            switch (event.getCode()) {
                case W:
                	rootGroup.translateZProperty().set(rootGroup.getTranslateZ() + 100);
                    break;
                case S:
                	rootGroup.translateZProperty().set(rootGroup.getTranslateZ() - 100);
                    break;
                case Q:
                	rootGroup.rotateByX(10);
                    break;
                case E:
                	rootGroup.rotateByX(-10);
                    break;
                case NUMPAD6:
                	rootGroup.rotateByY(10);
                    break;
                case NUMPAD4:
                	rootGroup.rotateByY(-10);
                    break;
            }
        });
		primary.setScene(scene);
		primaryStage.show();
	}
	
    private void setNodeColors(Color col) {
        PhongMaterial mat = new PhongMaterial();
        mat.setDiffuseColor(col);
        mat.setSpecularColor(col);
    	for (NodeInfo nodeInfo:nodeInfos.values()) {
    		if (nodeInfo.node instanceof Shape3D) {
    			((Shape3D)nodeInfo.node).setMaterial(mat);
    		}
    	}
	}



	private void initMouseControl(Group group, Scene scene, Stage stage) {
        Rotate xRotate;
        Rotate yRotate;
        group.getTransforms().addAll(
                xRotate = new Rotate(0, Rotate.X_AXIS),
                yRotate = new Rotate(0, Rotate.Y_AXIS)
        );
        xRotate.angleProperty().bind(angleX);
        yRotate.angleProperty().bind(angleY);

        scene.setOnMousePressed(event -> {
        	System.out.println(event);
        	if (event.isSecondaryButtonDown()) {
        		checkObject(event.getX(), event.getY(), event.getSceneX(), event.getSceneY(), event.getScreenX(), event.getScreenY());
        	}
            anchorX = event.getSceneX();
            anchorY = event.getSceneY();
            anchorAngleX = angleX.get();
            anchorAngleY = angleY.get();
        });

        scene.setOnMouseDragged(event -> {
            angleX.set(anchorAngleX - (anchorY - event.getSceneY()));
            angleY.set(anchorAngleY + anchorX - event.getSceneX());
        });

        stage.addEventHandler(ScrollEvent.SCROLL, event -> {
            double delta = event.getDeltaY();
            group.translateZProperty().set(group.getTranslateZ() + delta);
        });
    }


    private void checkObject(double x, double y, double sceneX, double sceneY, double screenX, double screenY) {
    	System.out.println();
    	System.out.println("x="+x+",y="+y+",sceneX="+sceneX+",sceneY="+sceneY+",screenX="+screenX+",screenY="+screenY);
    	
    	Point2D pRClick = new Point2D(screenX, screenY);

    	boolean hit = false;
    	for (NodeInfo nodeInfo:nodeInfos.values()) {
    		Node node = nodeInfo.node;
    		Bounds boundsInLocal = node.getBoundsInLocal();
    		Bounds screenBounds = node.localToScreen(boundsInLocal);
    		if (screenBounds.contains(pRClick)) {
    			hit = true;
    			System.out.println("HIT: "+nodeInfo.dddo.id);
//    			nodeInfo.dddo.type = nodeInfo.dddo.type ^ 1;
    		}
    		if (hit) {
//    			refreshCanvas();
    		}
    	}
	}

	class SmartGroup extends Group {
        Rotate r;
        Transform t = new Rotate();

        void rotateByX(int ang) {
            r = new Rotate(ang, Rotate.X_AXIS);
            t = t.createConcatenation(r);
            this.getTransforms().clear();
            this.getTransforms().addAll(t);
        }

        void rotateByY(int ang) {
            r = new Rotate(ang, Rotate.Y_AXIS);
            t = t.createConcatenation(r);
            this.getTransforms().clear();
            this.getTransforms().addAll(t);
        }
    }

    
	public static void sleep(int ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			throw new RuntimeException(e.toString(), e);
		}
	}

	private synchronized void open() {
		instance = null;
		try {
			Platform.runLater(()->{
				Stage prim = new Stage();
				try {
					start(prim);
				} catch (Exception e) {
					throw new RuntimeException(e.toString(), e);
				}
				instance = this; 
			});
		}
		catch (IllegalStateException tkEx) {
			Platform.startup(()->{
				Stage prim = new Stage();
				try {
					start(prim);
				} catch (Exception e) {
					throw new RuntimeException(e.toString(), e);
				}
				instance = this; 
			});
		}
		while (instance == null) {
			System.out.println("WAIT");
			sleep(500);
		}
		System.out.println("FOUND");		
	}

	public static void main(String[] args) throws Exception {
		Y24GUIOutput3D23 output = new Y24GUIOutput3D23("GUIOutput3D Test", true);
		output.smaller();
		
		List<DDDObject> state = new ArrayList<>();

		// F
		state.add(new DDDObject("FTL", -1.0, -1.0, -1.0, 1.0, 1));
		state.add(new DDDObject(-1.0,  0.0, -1.0, 1.0, 0));
		state.add(new DDDObject(-1.0,  1.0, -1.0, 1.0, 0));
		state.add(new DDDObject(-1.0,  2.0, -1.0, 1.0, 0));
		state.add(new DDDObject(-1.0,  3.0, -1.0, 1.0, 0));

		state.add(new DDDObject( 0.0,  1.0, -1.0, 1.0, 0));
		
		state.add(new DDDObject( 0.0, -1.0, -1.0, 1.0, 0));
		state.add(new DDDObject( 1.0, -1.0, -1.0, 1.0, 0));
		output.addStep("E", state);		

		// E
		state.add(new DDDObject(-1.0, -1.0,  1.0, 1.0, 10));
		state.add(new DDDObject(-1.0,  0.0,  1.0, 1.0, 10));
		state.add(new DDDObject(-1.0,  1.0,  1.0, 1.0, 10));
		state.add(new DDDObject(-1.0,  2.0,  1.0, 1.0, 10));
		state.add(new DDDObject(-1.0,  3.0,  1.0, 1.0, 10));

		state.add(new DDDObject( 0.0, -1.0,  1.0, 1.0, 10));
		state.add(new DDDObject( 1.0, -1.0,  1.0, 1.0, 10));
		
		state.add(new DDDObject( 0.0,  1.0,  1.0, 1.0, 10));
		
		state.add(new DDDObject( 0.0,  3.0,  1.0, 1.0, 10));
		state.add(new DDDObject( 1.0,  3.0,  1.0, 1.0, 10));
		output.addStep("E", state);		

		
		
		state.add(new DDDLineObject("line1",  0.0,  -1.0,  0.0,  0.0,  3.0,  0.0, 0.1, 30));
		state.add(new DDDLineObject("line2",  0.0,   1.0, -1.0,  0.0,  1.0,  1.0, 0.1, 30));
		state.add(new DDDLineObject("line3", -1.0,   1.0,  0.0,  1.0,  1.0,  0.0, 0.1, 30));

		
		output.adjustScale(state);
		output.addStep("Lines", state);		
		
		
//		state = new ArrayList<>();
//		for (int i = 0; i < 10; i++) {
//			state.add(new DDDObject(rand(), rand(), rand(), rand(0.01, 0.1), irand(0, 2)));
//		}
//		output.adjustScale(state);
//		output.addStep("Erste Szene", state);

		for (int t = 0; t < 20; t++) {
			ArrayList<DDDObject> nextState = new ArrayList<>();
			int i=0;
			for (DDDObject dddo : state) {
				i++;
				int type = dddo.type<10 ? 0 : 10;
				if (dddo.type <30) {
					nextState.add(new DDDObject(dddo.id, dddo.x + rand() * 0.05, dddo.y + rand() * 0.05, dddo.z + rand() * 0.05,
							dddo.size + rand()*0.1 , type+((i/3)%4)));
				}
				else {
					type = 30;
					DDDLineObject dddol = (DDDLineObject) dddo;
					nextState.add(new DDDLineObject(dddol.id, 
							dddol.x + rand() * 0.05, dddol.y + rand() * 0.05, dddol.z + rand() * 0.05,
							dddol.x2 + rand() * 0.05, dddol.y2 + rand() * 0.05, dddol.z2 + rand() * 0.05,
							dddol.size + rand()*0.01, type+((i)%4)));
				}
			}
			output.addStep("Testanimation", state);
			state = nextState;
		}
		
	}
    
}

