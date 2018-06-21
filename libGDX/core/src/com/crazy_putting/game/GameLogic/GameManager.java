
package com.crazy_putting.game.GameLogic;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector3;
import com.crazy_putting.game.Bot.Bot;
import com.crazy_putting.game.Bot.GeneticAlgorithm;
import com.crazy_putting.game.Components.Colliders.CollisionManager;
import com.crazy_putting.game.Components.Colliders.SphereCollider;
import com.crazy_putting.game.Components.Graphics.Graphics2DComponent;
import com.crazy_putting.game.Components.Graphics.SphereGraphics3DComponent;
import com.crazy_putting.game.GameObjects.Ball;
import com.crazy_putting.game.GameObjects.GameObject;
import com.crazy_putting.game.GameObjects.Hole;
import com.crazy_putting.game.Others.InputData;
import com.crazy_putting.game.Others.MultiplayerSettings;
import com.crazy_putting.game.Others.Velocity;
import com.crazy_putting.game.Parser.ReadAndAnalyse;
import com.crazy_putting.game.Physics.Physics;
import com.crazy_putting.game.Screens.GolfGame;
import com.crazy_putting.game.Screens.MenuScreen;

public class GameManager {

    private Ball _ball;
    private Hole _hole;
    private GolfGame _game;
    private int _turns;
    private int _mode;
    private Bot bot;
    private boolean printMessage = true;

    private int nPlayers;
    private int allowedDistance;
    private Ball[] allBalls;
    private Hole[] allHoles;
    private float[][] allInput;
    private double[][] distancesMatrix;
    private Ball[] cacheBalls;

    public GameManager(GolfGame pGame, int pMode){
        _mode = pMode;
        _game = pGame;
        if (_mode == 4) {
            nPlayers = MultiplayerSettings.PlayerAmount;
            allowedDistance = MultiplayerSettings.AllowedDistance;
        }
        else {
            nPlayers = 1;
        }
        if (_mode == 2)
            ReadAndAnalyse.calculate("myFile.txt");
        initGameObjects();
        _turns = 0;
        Physics.physics.updateCoefficients();
    }

    private void initGameObjects(){
        allBalls = new Ball[nPlayers];
        allHoles = new Hole[nPlayers];
        allInput = new float[nPlayers][2];
        distancesMatrix = new double[nPlayers][nPlayers];
        cacheBalls = new Ball[nPlayers];
        CourseManager.initObstacles();
        do {
            System.out.println("Setup");
            for (int i = 0; i < nPlayers; i++) {
               if(allBalls[i]!= null) allBalls[i].destroy();
                allBalls[i] = new Ball(createPosition(CourseManager.getStartPosition()));
                allHoles[i] = new Hole((int) CourseManager.getActiveCourse().getGoalRadius(), createPosition(CourseManager.getGoalStartPosition()));

                System.out.println("Balls "+allBalls[i].getPosition().x+" "+allBalls[i].getPosition().y);
                System.out.println("Hole "+allHoles[i].getPosition().x+" "+allHoles[i].getPosition().y);
            }
        } while (!checkLegitimacy() && _mode==4);

        if(MenuScreen.Mode3D ) {//3D Logic
            // if we are in multiplayer mode

                for (int i = 0; i < nPlayers; i++) {
                    allBalls[i].addGraphicComponent(new SphereGraphics3DComponent(40, Color.WHITE));
                    SphereCollider sphere = new SphereCollider(CourseManager.getStartPosition(),20);
                    allBalls[i].addColliderComponent(sphere);
                    allHoles[i].addGraphicComponent(new SphereGraphics3DComponent(40, Color.BLACK));
                }


        }
        else{//2D Logic
            for (int i = 0; i < nPlayers; i++) {
                allBalls[i].addGraphicComponent(new Graphics2DComponent(new Texture("golfBall.png")));
                allHoles[i].addGraphicComponent(new Graphics2DComponent(new Texture("hole.png"), allHoles[i].getRadius() * 2, allHoles[i].getRadius() * 2));
            }
        }
        _ball = allBalls[0];
        _hole = allHoles[0];
    }

    public void update(float pDelta){
        if(pDelta > 0.03){
            pDelta = 0.00166f;
        }
        handleInput(_game.input);
        Physics.physics.update(pDelta);
        CollisionManager.update();
        if(printMessage){
            updateGameLogic(pDelta);
        }
        if (_mode == 4)
            multiPlayerUpdate(pDelta);
    }

    //TODO blazej or Simon, is here where we stop the ball? otherwise we can erase this
    public void updateGameLogic(float pDelta){
        int i=0;
        while (i<nPlayers && printMessage) {
            if (isBallInTheHole(allBalls[i], allHoles[i]) && allBalls[i].isSlow()) {
                printMessage = false;
                //allBalls[i].fix(true);
                allBalls[i].setVelocityComponents(0, 0);
                System.out.println("Ball in goal");
                //allBalls[i].fix(true);
                for (int n=0; n<nPlayers; n++){
                    allBalls[n].fix(true);
                }
            }
            i++;
        }
    }

    //TODO move to input class?
    //TODO fix GA in AI mode
    public void handleInput(InputData input){
        // later on it should be if speed of the ball is zero (ball is not moving, then input data)
        if(_mode == 1) {

            if (Gdx.input.isKeyJustPressed(Input.Keys.G) && !_ball.isMoving()){
                System.out.println(_ball.getPosition().x + "  " + _ball.getPosition().y);

                GeneticAlgorithm GA = new GeneticAlgorithm(_hole, CourseManager.getActiveCourse());

                Ball b = GA.getBestBall();
                float speed = b.getVelocityGA().speed;
                float angle = b.getVelocityGA().angle;
                _ball.setVelocity(speed,angle);
                _ball.fix(false);

            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.I) && !_ball.isMoving()) {
                //CourseManager.reWriteCourse();//TODO: CHECK WHY THIS IS HERE
                Gdx.input.getTextInput(input, "Input data", "", "Input speed and direction separated with space");
            }
            if (input.getText() != null) {
                try {
                    String[] data = input.getText().split(" ");
                    float speed = Float.parseFloat(data[0]);
                    float angle = Float.parseFloat(data[1]);
                    allInput[0][0] = speed;
                    allInput[0][1] = angle;
                    input.clearText();//important to clear text or it will overwrite every frame
                    checkConstrainsAndSetVelocity(allInput);
                    //  input.clearText();//important to clear text or it will overwrite every frame

                }
                catch (NumberFormatException e) {
                    // later on this will be added on the game screen so that it wasn't printed multiple times
                    // after doing this change, delete printing stack trace
                    Gdx.app.error("Exception: ", "You must input numbers");
                    e.getStackTrace();
                }
            }
        }
        else if(_mode == 2){
            if (Gdx.input.isKeyJustPressed(Input.Keys.I)){
                System.out.println("MODE "+_mode+" with N: " + ReadAndAnalyse.getN());
                if(!_ball.isMoving() && _turns<ReadAndAnalyse.getN()) {
                    _ball.setVelocity(ReadAndAnalyse.getResult()[_turns][0], ReadAndAnalyse.getResult()[_turns][1]);
                    _ball.fix(false);
                    increaseTurnCount();
                }
                else if(_turns>=ReadAndAnalyse.getN()){
                    System.out.println("No more moves...");
                }
            }
        }
        else if (_mode == 3){
            if (Gdx.input.isKeyJustPressed(Input.Keys.I) && !_ball.isMoving()){
                bot = new Bot(_ball,_hole, CourseManager.getActiveCourse());
                bot.computeOptimalVelocity();
                Velocity computedVelocity = bot.getBestBall().getVelocity();
                Gdx.app.log("Ball","Position x "+ _ball.getPosition().x+" position y "+_ball.getPosition().y);
                allInput[0][0] = computedVelocity.speed;
                allInput[0][1] = computedVelocity.angle;
                checkConstrainsAndSetVelocity(allInput);
                Gdx.app.log("Manager","speed "+computedVelocity.speed+" angle "+computedVelocity.angle);
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.G) && !_ball.isMoving()){

                GeneticAlgorithm GA = new GeneticAlgorithm(_hole,CourseManager.getActiveCourse());
                Ball b = GA.getBestBall();
                float speed = b.getVelocityGA().speed;
                float angle = b.getVelocityGA().angle;
                _ball.setVelocity(speed,angle);
                _ball.fix(false);
            }
        }
        else if(_mode == 4) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.I) && !anyBallIsMoving()) {
                Gdx.input.getTextInput(input, "Input data", "", "For all Players: input speed and direction separated with space");
            }
            if (input.getText() != null) {
                try {
                    String[] data = input.getText().split(" ");
                    for (int i=0; i<nPlayers; i++) {
                        float speed = Float.parseFloat(data[i*2]);
                        float angle = Float.parseFloat(data[i*2+1]);
                        allInput[i][0] = speed;
                        allInput[i][1] = angle;
                        input.clearText();//important to clear text or it will overwrite every frame
                    }
                    copyPreviousPosition();
                    checkConstrainsAndSetVelocity(allInput);
                }
                catch (NumberFormatException e) {
                    // later on this will be added on the game screen so that it wasn't printed multiple times
                    // after doing this change, delete printing stack trace
                    Gdx.app.error("Exception: ", "You must input numbers");
                    e.getStackTrace();
                }
            }
        }
    }

    public static boolean isBallInTheHole(Ball ball, Hole hole){
        if(Math.sqrt(Math.pow(ball.getPosition().x -hole.getPosition().x,2) +Math.pow((ball.getPosition().y - hole.getPosition().y),2)+Math.pow((ball.getPosition().z - hole.getPosition().z),2))< hole.getRadius()){
            return true;
        }
        return false;
    }

    public void checkConstrainsAndSetVelocity(float[][] input) {
        for (int i=0; i<nPlayers; i++) {
            float speed = checkMaxSpeedConstrain(input[i][0]);
            float angle = input[i][1];
            if (speed == 0) {
                speed = 0.000001f;
            }
            allBalls[i].setVelocity(speed, angle);
            allBalls[i].fix(false);
        }
        increaseTurnCount();
    }

    public float checkMaxSpeedConstrain(float speed){
        if(speed > CourseManager.getMaxSpeed()){
            speed = CourseManager.getMaxSpeed();
        }
        return speed;
    }

    public void increaseTurnCount(){
        _turns++;
    }

    public Ball getBall() {
        return _ball;
    }

    public int getTurns(){
        return _turns;
    }

    public Hole getHole() {
        return _hole;
    }

    /////////////////////////////////////////////////////////////////////
    //////////Methods for spline Edit Mode//////////////////////////////
    ////////////////////////////////////////////////////////////////////

    /**
     * Overwrite the position of ball and hole when saving the new coordinates of the edited course by spplines
     */
    public void saveBallAndHolePos(){
        for (int i=0; i<nPlayers; i++) {
            CourseManager.getActiveCourse().setBallStartPos(allBalls[i].getPosition());
            CourseManager.getActiveCourse().setGoalPosition(allHoles[i].getPosition());
        }
    }

    /**
     *  Updates the height position of the ball and hole after the course changed using spline editor
     */
    public void updateObjectPos(){
        for (int i=0; i<nPlayers; i++) {
            Ball _ball = allBalls[i];
            Hole _hole = allHoles[i];
            _ball.getPosition().z = CourseManager.calculateHeight(_ball.getPosition().x, _ball.getPosition().y);
            _hole.getPosition().z = CourseManager.calculateHeight(_hole.getPosition().x, _hole.getPosition().y);
        }
    }

    /**
     * Change the position of the ball when using the change ball position editor mode
     * @param pos
     */
    public void updateBallPos(Vector3 pos){

        Vector3 cache = _ball.getPosition();
        _ball.setPosition(pos);
        if(checkDistances(allBalls)==false)
            _ball.setPosition(cache);
    }

    /**
     * Change the position of the hole when using the change hole position editor mode
     * @param pos
     */
    public void updateHolePos(Vector3 pos){
        Vector3 cache = _hole.getPosition();
        _hole.setPosition(pos);
        if(checkDistances(allHoles)==false)
            _hole.setPosition(cache);
    }

    /////////////////////////////////////////////////////////////////////
    //////////Methods for multiple players Mode//////////////////////////////
    ////////////////////////////////////////////////////////////////////

    public boolean checkDistances(GameObject[] balls){
        if (nPlayers==1)
            return true;
        for (int i=0; i<nPlayers; i++){
            for (int j=0; j<nPlayers; j++) {
                double d = euclideanDistance(balls[i].getPosition(), balls[j].getPosition());
                distancesMatrix[i][j] = d;
                if (distancesMatrix[i][j] > allowedDistance)
                    return false;
            }
        }
        return true;
    }

    public double euclideanDistance(Vector3 start, Vector3 goal){
        double d = (float) Math.sqrt(Math.pow(start.x-goal.x,2)+Math.pow(start.y-goal.y,2)+Math.pow(start.z-goal.z,2));
        return d;
    }

    public boolean anyBallIsMoving(){
        for (int i=0; i<nPlayers; i++) {
            if (allBalls[i].isMoving()) {
                return true;
            }
        }
        return false;
    }

    public void changeActiveBallandHole(int n){
        if (n >= allBalls.length) return;
        _ball = allBalls[n];
        _hole = allHoles[n];
    }

    public void multiPlayerUpdate(double pDelta){
        /*
        if (!anyBallIsMoving() && MultiplayerSettings.CollisionHappened) {
            System.out.println("The ball fell to the water or out of the world. Please try again.");
            MultiplayerSettings.CollisionHappened = false;
            returnToPreviousPosition();
            // TODO: display UI massage
        }*/
        if (!anyBallIsMoving() && !checkDistances(allBalls)){
            System.out.println("Exceeding the allowed distance from each other. Please try again.");
            returnToPreviousPosition();
            // TODO: display UI massage
        }
    }

    public void copyPreviousPosition(){
        for (int i=0; i<nPlayers; i++){
            cacheBalls[i] = allBalls[i].clone();
        }
    }

    public void returnToPreviousPosition(){
        for (int i=0; i<nPlayers; i++){
            allBalls[i] = cacheBalls[i].clone();
            //allBalls[i].fix(true);
        }
    }

    public Vector3 createPosition(Vector3 p) {
        float size = CourseManager.getCourseDimensions().x / 2;
        float x;
        float y;
        do {
            x = (float) (p.x + Math.random() * allowedDistance/2);
            y = (float) (p.y + Math.random() * allowedDistance/2);
        } while (x<-1*size || x>size || y<-1*size || y>size);
        float z = CourseManager.calculateHeight(x, y);
        return new Vector3(x, y, z);
    }

    public boolean checkLegitimacy(){
        if (checkDistances(allBalls)==false || checkDistances(allHoles)==false)
            return false;
        int a = 0;
        for (Hole element: allHoles){
            for (Hole element2: allHoles){
                a++;
                double d = euclideanDistance(element.getPosition(),element2.getPosition());
                if ( d>0 && d < element.getRadius()*2)
                    return false;
            }
        }
        return true;
    }

}
