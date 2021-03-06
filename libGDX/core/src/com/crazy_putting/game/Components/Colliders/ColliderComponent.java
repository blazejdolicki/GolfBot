package com.crazy_putting.game.Components.Colliders;

import com.badlogic.gdx.math.Vector3;
import com.crazy_putting.game.Components.BaseComponent;
import com.crazy_putting.game.GameObjects.PhysicsGameObject;
import com.crazy_putting.game.Others.Velocity;

public abstract class ColliderComponent extends BaseComponent {



    protected Vector3 position;
    protected boolean isStatic;
    protected Vector3 dimensions;



    public ColliderComponent(){
        CollisionManager.addCollider(this);
    }

    public void synchronize(){
        if(_owner==null){
            System.out.println("owner is null");
        }
        if(position==null){
            System.out.println("position is null");
        }
       position = _owner.getPosition();


    }
    public boolean isEnabled(){
        if(_owner == null)return true;
        else return _owner.enabled;
    }
    public Vector3 getPosition(){
        return this.position;
    }
     public void setPosition(Vector3 position){
         this.position = position;
     }

     protected boolean isStatic(){
         return this.isStatic;
     }
    public void setStatic(boolean tf){
         this.isStatic=tf;
    }
    protected  Velocity getVelocity(){
        if(_owner instanceof PhysicsGameObject){
            PhysicsGameObject obj = (PhysicsGameObject) _owner;
            return obj.getVelocity();
        }
        else{
            return Velocity.instance();
        }
    }

    protected void setVeloctiy(Velocity velocity){
        if(_owner instanceof PhysicsGameObject){
            PhysicsGameObject obj = (PhysicsGameObject)_owner;
            obj.setVelocity(velocity);
            return;
        }

    }
    protected float getInverseMass(){
        if(_owner instanceof PhysicsGameObject){
            PhysicsGameObject obj = (PhysicsGameObject) _owner;
            return obj.getInverseMass();
        }
        return 1.0f/500;
    }

    public Vector3 getDimensions() {
        return dimensions;
    }

    public abstract boolean containsPoint(Vector3 pPoint);

}
