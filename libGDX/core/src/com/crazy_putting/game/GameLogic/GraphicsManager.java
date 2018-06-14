package com.crazy_putting.game.GameLogic;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.crazy_putting.game.Components.Graphics.Graphics2DComponent;
import com.crazy_putting.game.Components.Graphics.Graphics3DComponent;

import java.util.ArrayList;
import java.util.List;

public class GraphicsManager {
    // for 2D world dimensions should be 1500 and 1500 but this works for 3D
    public final static int WORLD_WIDTH = 2000;
    public final static int WORLD_HEIGHT = 2000;
    public final static int WINDOW_WIDTH = 1920;
    public final static int WINDOW_HEIGHT = 1080;
    private static List<Graphics2DComponent> _graphics2DComponentList = new ArrayList<Graphics2DComponent>();
    private static List<Graphics3DComponent> _graphics3DComponentList = new ArrayList<Graphics3DComponent>();
    private static Environment _environment;
    public static void addGraphics2DComponent(Graphics2DComponent pGC){
        _graphics2DComponentList.add(pGC);
    }
    public static void addGraphics3DComponent(Graphics3DComponent pGC){
        _graphics3DComponentList.add(pGC);
    }
    public static void set3DEnvironment(Environment pEnvironment)
    {
        _environment = pEnvironment;
    }
    public static List<Graphics2DComponent> getAllGraphics2DComponents()
    {
        return _graphics2DComponentList;
    }
    public static List<Graphics3DComponent> getAllGraphics3DComponents()
    {
        return _graphics3DComponentList;
    }

    /**
     * Render all the graphics components that exist
     * @param pBatch
     */
    public static void render2D(SpriteBatch pBatch){

        for(int i = 0; i < _graphics2DComponentList.size(); i++){
            _graphics2DComponentList.get(i).render(pBatch);
        }
    }
    public static void render3D(ModelBatch pBatch, Camera pCam3D){
        pBatch.begin(pCam3D);
        Gdx.gl.glViewport(0, 0, WINDOW_WIDTH-300, Gdx.graphics.getHeight());
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        for(int i = 0; i < _graphics3DComponentList.size(); i++){
            _graphics3DComponentList.get(i).render(pBatch, _environment);
        }
        pBatch.end();
    }
}

