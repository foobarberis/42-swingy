package com.swingy.model.world;

import com.swingy.model.Enemy;

public class EnemyInstance {
    private final Enemy enemy;
    private Position pos;
    private Position prevPos;

    public EnemyInstance(Enemy enemy, Position pos) {
        this.enemy = enemy;
        this.pos = pos;
        this.prevPos = pos;
    }

    public Enemy enemy() { return enemy; }
    public Position pos() { return pos; }
    public Position prevPos() { return prevPos; }

    public void moveTo(Position newPos) {
        this.prevPos = this.pos;
        this.pos = newPos;
    }

    public void revertMove() {
        this.pos = this.prevPos;
    }
}
