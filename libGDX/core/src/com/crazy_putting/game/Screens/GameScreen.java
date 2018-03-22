package com.crazy_putting.game.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.crazy_putting.game.FormulaParser.ExpressionNode;
import com.crazy_putting.game.FormulaParser.FormulaParser;
import com.crazy_putting.game.GameLogic.CourseManager;
import com.crazy_putting.game.GameLogic.GameManager;
import com.crazy_putting.game.GameLogic.GraphicsManager;
import com.crazy_putting.game.GameObjects.Ball;

import static com.crazy_putting.game.GameLogic.GraphicsManager.WORLD_HEIGHT;
import static com.crazy_putting.game.GameLogic.GraphicsManager.WORLD_WIDTH;

public class GameScreen extends InputAdapter implements Screen {
    final GolfGame game;
    private ShapeRenderer sr;
    FormulaParser parser;
    ExpressionNode expr = null;
    private Texture texture;
    private Pixmap pixmap;
    private GameManager _gameManager;
    OrthographicCamera cam;

    //New blazej
    private Sprite mapSprite;


    public GameScreen(GolfGame game, int pMode) {
        cam = new OrthographicCamera(WORLD_WIDTH*0.9f, WORLD_WIDTH*0.9f * (WORLD_HEIGHT / WORLD_WIDTH));
        cam.position.set(cam.viewportWidth / 2f, cam.viewportHeight / 2f, 0);
        cam.update();
        parser = new FormulaParser();
        this.game = game;
        _gameManager = new GameManager(game);
        //sr = new ShapeRenderer();

        drawHeightMap();
        mapSprite = new Sprite(texture);
        mapSprite.setPosition(0,0);
        mapSprite.setSize(WORLD_WIDTH, WORLD_HEIGHT);
        Gdx.input.setInputProcessor(this);
    }



    @Override
    public void show() {
        //Gdx.input.setInputProcessor(this);
        // drawHeightMap();
    }


    private void handleInput() {
        if(Gdx.input.isKeyJustPressed(Input.Keys.O)){//TODO: reference to InputScreen, blazej?
            //game.setScreen(new InputScreen(game));
            //game.getScreen();
        }
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            cam.zoom += 0.02;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.Q)) {
            cam.zoom -= 0.02;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            cam.translate(-3, 0, 0);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            cam.translate(3, 0, 0);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            cam.translate(0, -3, 0);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            cam.translate(0, 3, 0);
        }

        cam.zoom = MathUtils.clamp(cam.zoom, 0.1f, Math.min(WORLD_HEIGHT/cam.viewportHeight, WORLD_WIDTH/cam.viewportWidth));

        float effectiveViewportWidth = cam.viewportWidth * cam.zoom;
        float effectiveViewportHeight = cam.viewportHeight * cam.zoom;

        cam.position.x = MathUtils.clamp(cam.position.x, effectiveViewportWidth / 2f, WORLD_HEIGHT - effectiveViewportWidth / 2f);
        cam.position.y = MathUtils.clamp(cam.position.y, effectiveViewportHeight / 2f, WORLD_WIDTH - effectiveViewportHeight / 2f);
    }
    @Override
    public void render(float delta) {

        _gameManager.Update(delta);
        UpdateCamera();

        game.batch.begin();
        if(texture!=null){
            game.batch.draw(texture, 0, 0);
        }
        //Get all the graphics and draw them
        GraphicsManager.Render(game.batch);
        RenderGUI();
        game.batch.end();
        //sr.begin(ShapeRenderer.ShapeType.Filled);
        //sr.circle(hole.getPosition().x, hole.getPosition().y, hole.getRadius());
        //sr.end();




    }
    private void UpdateCamera()
    {
        handleInput();
        cam.update();
        game.batch.setProjectionMatrix(cam.combined);

        int red = 34;
        int green = 137;
        int blue = 34;
        Gdx.gl.glClearColor((float)(red/255.0), (float)(green/255.0), (float)(blue/255.0), 1);
        Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }
    private void RenderGUI()
    {
        Ball ball =_gameManager.getBall();
        Vector3 unprojectSpeed = cam.unproject(new Vector3(10, 10,0));
        Vector3 unprojectHeight = cam.unproject(new Vector3(10, 30,0));
        Vector3 unprojectBallX = cam.unproject(new Vector3(10, 50,0));
        Vector3 unprojectBallY = cam.unproject(new Vector3(10, 70,0));
        game.font.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        game.font.getData().setScale(cam.zoom*0.7f);
        game.font.setColor(Color.BLACK);
        game.font.draw(game.batch, "Speed: "+Math.round(Math.sqrt(Math.pow(ball.getVelocity().Vx,2)+Math.pow(ball.getVelocity().Vy,2))), unprojectSpeed.x, unprojectSpeed.y);
        game.font.draw(game.batch, "Height: "+Math.round(CourseManager.calculateHeight(ball.getPosition().x,ball.getPosition().y)), unprojectHeight.x, unprojectHeight.y);
        game.font.draw(game.batch, "Ball x: "+Math.round(ball.getPosition().x), unprojectBallX.x, unprojectBallX.y);
        game.font.draw(game.batch, "Ball y: "+Math.round(ball.getPosition().y), unprojectBallY.x, unprojectBallY.y);
    }
    @Override
    public void resize(int width, int height) {
        cam.viewportWidth = width/1.1f;
        cam.viewportHeight = cam.viewportWidth * height/width;
        cam.update();
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {

    }
    public void drawHeightMap(){

        // Hole hole = _gameManager.getHole();
        pixmap = new Pixmap(WORLD_WIDTH, WORLD_HEIGHT, Pixmap.Format.RGB888);
        texture = new Texture(pixmap);
        float maxValue = 0;
        // important
        float minValue = 0;
        boolean sameValue = true;
        float someValue = CourseManager.calculateHeight(0,0);
        for(int i = -pixmap.getWidth()/2; i<pixmap.getWidth()/2;i++) {
            for (int j = -pixmap.getHeight() / 2; j < pixmap.getHeight() / 2; j++) {
                if(CourseManager.calculateHeight(i,j)!=someValue){
                    sameValue = false;
                }
                if(CourseManager.calculateHeight(i,j)<=minValue){
                    minValue = CourseManager.calculateHeight(i,j);
                }
                if(CourseManager.calculateHeight(i,j)>=maxValue){
                    maxValue = CourseManager.calculateHeight(i,j);
                }
            }
        }

        if(sameValue){
            // just change a comment line
            for(int i = -pixmap.getWidth()/2; i<pixmap.getWidth()/2;i++){
                for(int j = -pixmap.getHeight()/2; j<pixmap.getHeight()/2;j++){
                    pixmap.setColor(Color.PURPLE);
                    pixmap.drawPixel(i+pixmap.getWidth()/2, pixmap.getHeight()/2 - j);
                }
            }
        }
        else{
            int nrOfIntervals = 10;
            float interval = (maxValue-minValue)/nrOfIntervals;
            float[] intervals = new float[nrOfIntervals+1];
            // should be rounded to smaller integer than minValue
            for(int x=0;x<intervals.length;x++){
                if(x!=0){
                    intervals[x] = intervals[x-1]+interval;
                }
                else if(x==0){
                    intervals[x] = minValue;
                }
                else{
                    intervals[x] = maxValue;
                }
                System.out.println("Interval "+x+" : "+intervals[x]);
            }
            //NOTE: Pixmap coordinates start from TOP LEFT so the picture is mirrored with this loop, I changed the y coordinate in the draw to
            //(pixmap.getHeight()/2 - j )instead of (j -pixmap.getHeight()/2 ) this way the image stays true to the world coordinates
            for(int i = -pixmap.getWidth()/2; i<pixmap.getWidth()/2;i++){
                for(int j = -pixmap.getHeight()/2; j<pixmap.getHeight()/2;j++){
                    for(int x=0;x<intervals.length-1;x++){
                        float height = CourseManager.calculateHeight(i,j);
                        if(height<0){
                            pixmap.setColor(new Color(Color.BLUE));
                            pixmap.drawPixel(i+pixmap.getWidth()/2, pixmap.getHeight()/2 - j);
                            break;
                        }
                        else if(height>intervals[x] && height<=intervals[x+1]){
//                        System.out.println("Bang");
                            pixmap.setColor(new Color((float)(200/255.0*(1/(double)(x+1))),(float)((250-x*20)/255.0),(float)(200/255.0*(1/(double)(x+1))),1));
                            pixmap.drawPixel(i+pixmap.getWidth()/2, pixmap.getHeight()/2 - j);
                            // System.out.println("i "+i+ " j "+j+" "+height(i,j)+" interval "+(x+1)+"r: "+200*(1/(double)(x+1))+"g: "+(250-x*20)+" b: "+200*(1/(double)(x+1)));
                            break;
                        }
                    }
                }
            }
        }

        //pixmap.setColor(Color.WHITE);
        //pixmap.fillCircle((int)hole.getPosition().x,(int)hole.getPosition().y,hole.getRadius());
        texture = new Texture(pixmap);
    }
}
