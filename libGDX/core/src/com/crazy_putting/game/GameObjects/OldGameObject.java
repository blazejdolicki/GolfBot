package com.crazy_putting.game.GameObjects;


import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.crazy_putting.game.Components.OldGraphicsComponent;
import com.crazy_putting.game.Others.Velocity;

public abstract class OldGameObject {
    private OldGraphicsComponent _graphicComponent;
    public void addGraphicComponent(OldGraphicsComponent pGC)
    {
        _graphicComponent = pGC;
      //  _graphicComponent.setOwner(this);
    }
    public abstract  Texture getTexture() ;
    public abstract Vector2 getPosition();
    public abstract Velocity getVelocity();
    public abstract float getMass();
    public abstract void setPosition(Vector2 position);
    public abstract void setPositionX(float x);
    public abstract void setPositionY(float y);
    public abstract void setVelocity(float speed, float angle);
    public abstract void setSpeed(float speed);
    public abstract boolean inTheWater();
    public abstract Vector2 getPreviousPosition();
    public abstract float getSpeed();
    public abstract boolean isFixed();
    public abstract void fix(boolean tf);
    public abstract boolean isSlow();
    public abstract void setVelocityComponents(float newSpeedX, float newSpeedY);

}