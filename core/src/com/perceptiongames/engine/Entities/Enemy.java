package com.perceptiongames.engine.Entities;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.perceptiongames.engine.Game;
import com.perceptiongames.engine.Handlers.Animation;

import java.util.ArrayList;
import java.util.List;

import static com.badlogic.gdx.math.MathUtils.floor;
import static com.badlogic.gdx.math.MathUtils.random;
import static java.lang.Math.sin;

public class Enemy extends Entity {

    private float ticker;
    private Vector2 pos = getPosition();
    private Vector2 velocity = new Vector2();
    private boolean onGround;
    private int[] actions;
    private int current;

    private boolean attacking;
    private Vector2 weaponOffset;
    private AABB weapon;
    private List<Sound> sounds;
    private float playerDir;

    public Enemy(Animation animation, String animationName, AABB aabb) {
        super(animation, animationName, aabb);
        onGround=false;
        playerDir=0;
        actions = new int[] { 0, 1, 2, 3 };
        current=3;

        attacking = false;
        weaponOffset = new Vector2();
        sounds = new ArrayList<Sound>();

    }

    @Override
    public void update(float dt) {
        if(!live) return;

        if(ticker > 5 && playerDir==0) {
            ticker = 0;
            current = random.nextInt(4) + 1;
        }
        else if(ticker > 5)
        {
            current=4;
        }


        int flags = aabb.getCollisionFlags();
        if((flags & AABB.TOP_BITS) == AABB.TOP_BITS) {
            onGround = true;
            if(velocity.y > 0) {
                velocity.y = 0;
            }
        }

        if((flags & AABB.BOTTOM_BITS) == AABB.BOTTOM_BITS) {
            onGround = false;
            if(velocity.y < 0) {
                velocity.y = 0;
            }
        }

        if((flags & AABB.LEFT_BITS) == AABB.LEFT_BITS) {
            velocity.x = 0;
        }

        if((flags & AABB.LEFT_BITS) == AABB.LEFT_BITS) {
            velocity.x = 0;
        }
        if(flags == AABB.NONE_BITS || flags == (AABB.SENSOR_BITS | AABB.NONE_BITS)) {
            if(getPosition().y + aabb.getHeight() == Game.WORLD_HEIGHT) { //If no collision, check if its on the world floor
                onGround = true;
            }
            else {
                onGround = false;
            }
        }
        if(!onGround) {
            velocity.y = Math.min(velocity.y + 2300f*dt, 2500f);
        }
        else {
            velocity.y=0;
        }

        flags = aabb.getCollisionFlags();
        switch(current) {
            case 1:
                velocity.x = 75;
                this.setCurrentAnimation("Right");
                if((flags&AABB.LEFT_BITS) == AABB.LEFT_BITS) {
                    //velocity.add(0, -400);
                    current = 2;
                }
                attacking = false;
                break;
            case 2:
                velocity.x=-75;
                this.setCurrentAnimation("Left");
                if((flags & AABB.RIGHT_BITS) == AABB.RIGHT_BITS) {
                    //velocity.add(0, 400);
                    current = 1;
                }
                attacking = false;
                break;
            case 3:
                this.setCurrentAnimation("idle");
                attacking = false;
                break;
            case 4:
                this.setCurrentAnimation("attack");
                attacking = true;
                if(playerDir!=0)
                    velocity.x=75*playerDir;
                break;
        }
        Vector2 newPos = new Vector2();
        newPos.x = getPosition().x + (velocity.x * dt); // Speed = distance / time, simple physics
        newPos.y = getPosition().y + (velocity.y * dt);

        if(attacking) {
            Animation a = getAnimation(getAnimationKey());
            switch (a.getCurrentFrame()) {
                case 0:
                    weaponOffset.x = 38;
                    weaponOffset.y = -5;
                    break;
                case 1:
                    weaponOffset.x = 38;
                    weaponOffset.y = 14;
                    break;
                case 2:
                    weaponOffset.x = 22;
                    weaponOffset.y = 20;
                    break;
                case 3:
                    weaponOffset.x = -7;
                    weaponOffset.y = 28;
                    break;
                case 4:
                    weaponOffset.x = -20;
                    weaponOffset.y = 18;
                    break;
                case 5:
                    weaponOffset.x = -23;
                    weaponOffset.y = -8;
                    break;
                case 6:
                    weaponOffset.x = 20;
                    weaponOffset.y = -18;
                    break;
            }
        }
        else {
            weaponOffset.x = weaponOffset.y = 0;
        }

        if(newPos.x < 0) { //Because speed never hit 0, we make it 0 if its under 1
            newPos.x = 0;
            velocity.x = 0;
        }
        else if(newPos.x + aabb.getWidth() > Game.WORLD_WIDTH) {
            newPos.x = Game.WORLD_WIDTH - aabb.getWidth();
            velocity.x = 0;
        }

        if(newPos.y < 0) {
            newPos.y = 0;
            velocity.y = 0;
        }
        else if(newPos.y + aabb.getHeight() > Game.WORLD_HEIGHT) {
            newPos.y = Game.WORLD_HEIGHT - aabb.getHeight();
            velocity.y = 0;
            onGround = true;
        }
        this.setPosition(newPos);
        ticker += dt;
        weapon.setCentre(new Vector2(aabb.getCentre().x + weaponOffset.x, aabb.getCentre().y + weaponOffset.y));
        super.update(dt);
        aabb.setCollisionFlags(AABB.NONE_BITS);
    }

    public void hit()
    {
        this.attacking=false;
        this.live=false;
    }
    public int getCurrent() { return current; }
    public AABB getWeapon() { return weapon; }
    public boolean isAttacking() { return attacking; }


    public void setCurrent(int current) { this.current = current; }
    public void setWeapon(AABB weapon) {
        this.weapon = weapon;
        this.weapon.setSensor(true);
    }

    public List<Sound> getSounds() {
        return sounds;
    }

    public void setSounds(List<Sound> sounds) {
        this.sounds = sounds;
    }
    public void playerDirection(float dir)
    {
        this.playerDir=dir;
    }
}
