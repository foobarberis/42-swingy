package com.swingy.model.world;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Maze {
    private final int size;
    private final TileType[][] terrain;
    private Position heroStart;
    private final Set<Position> exits = new HashSet<>();
    private Position potionPos;
    private final List<EnemyInstance> enemies = new ArrayList<>();

    public Maze(int size, TileType[][] terrain) {
        this.size = size;
        this.terrain = terrain;
    }

    public int size() { return size; }
    public TileType terrainAt(Position p) { return terrain[p.y()][p.x()]; }
    public void setTerrain(Position p, TileType t) { terrain[p.y()][p.x()] = t; }
    public boolean isInside(Position p) { return p.x() >= 0 && p.y() >= 0 && p.x() < size && p.y() < size; }
    public boolean isWalkable(Position p) {
        if (!isInside(p)) return false;
        TileType t = terrainAt(p);
        return t == TileType.FLOOR || t == TileType.EXIT;
    }

    public Position heroStart() { return heroStart; }
    public void setHeroStart(Position heroStart) { this.heroStart = heroStart; }
    public Set<Position> exits() { return exits; }
    public Position potionPos() { return potionPos; }
    public void setPotionPos(Position potionPos) { this.potionPos = potionPos; }
    public List<EnemyInstance> enemies() { return enemies; }
}
