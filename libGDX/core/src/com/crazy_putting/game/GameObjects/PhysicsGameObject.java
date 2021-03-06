package com.crazy_putting.game.GameObjects;


import com.badlogic.gdx.math.Vector3;
import com.crazy_putting.game.Others.Velocity;
import com.crazy_putting.game.Physics.Physics;

public abstract class PhysicsGameObject extends GameObject {
    Vector3 _startPosition;
    protected PhysicsGameObject(){
        _startPosition = new Vector3(_position);
    }
    public abstract Vector3 getStartPosition();
    public abstract Velocity getVelocity();
    public abstract float getInverseMass();
    public void destroy(){
        Physics.physics.removeMovableObject(this);
    }
    //public abstract void setPosition(Vector3 position);
    public abstract void setPositionXYZ(float x,float y);
    public abstract void setVelocity(float speed, float angle);
    public abstract void setVelocity(Velocity vel);
    public abstract Vector3 getPreviousPosition();
    public abstract float getSpeed();
    public abstract boolean isFixed();
    public abstract void fix(boolean tf);
    public abstract boolean isSlow();
    public abstract void setVelocityComponents(float newSpeedX, float newSpeedY);
    public abstract boolean isMoving();

}
