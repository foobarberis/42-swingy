package com.swingy.model.world;

import com.swingy.model.Enemy;
import com.swingy.util.RandomProvider;

import java.util.ArrayList;
import java.util.List;

public class EntityPlacer {
    private static final List<String> NORMAL_NAMES = List.of("Kobold", "Goblin", "Orc", "Orc wizard", "Skeleton", "Zombie", "Ogre", "Troll", "Centaur", "Yak", "Ice beast", "Jelly", "Killer bee", "Spiny frog", "Two-headed ogre");
    private static final List<String> UNIQUE_NAMES = List.of("Grinder", "Jessica", "Sigmund", "Crazy Yiuf", "Prince Ribbit", "Pikel", "Urug", "Harold", "Rupert", "Louise");

    private final RandomProvider rng;

    public EntityPlacer(RandomProvider rng) {
        this.rng = rng;
    }

    public void placePotion(Maze maze) {
        Position start = maze.heroStart();
        int n = maze.size() / 4;
        List<Position> deadEnds = new ArrayList<>();
        List<Position> floors = new ArrayList<>();
        for (int y = 0; y < maze.size(); y++) {
            for (int x = 0; x < maze.size(); x++) {
                Position p = new Position(x, y);
                if (maze.terrainAt(p) != TileType.FLOOR) continue;
                if (p.manhattan(start) < n) continue;
                floors.add(p);
                int walkable = 0;
                for (Position ne : p.neighbors4()) {
                    if (maze.isInside(ne) && maze.isWalkable(ne)) walkable++;
                }
                if (walkable == 1) deadEnds.add(p);
            }
        }
        List<Position> src = deadEnds.isEmpty() ? floors : deadEnds;
        if (!src.isEmpty()) {
            maze.setPotionPos(src.get(rng.nextInt(src.size())));
        }
    }

    public void placeEnemies(Maze maze, int heroLevel) {
        Position start = maze.heroStart();
        int desired = (maze.size() * maze.size()) / 32;
        int minStartDist = maze.size() / 6;
        int baseMinEnemyDist = maze.size() / 8;

        List<Position> candidates = new ArrayList<>();
        for (int y = 0; y < maze.size(); y++) {
            for (int x = 0; x < maze.size(); x++) {
                Position p = new Position(x, y);
                if (maze.terrainAt(p) != TileType.FLOOR) continue;
                if (p.equals(start)) continue;
                if (p.equals(maze.potionPos())) continue;
                candidates.add(p);
            }
        }

        List<EnemyInstance> result = new ArrayList<>();
        for (int attempt = 0; attempt < 4; attempt++) {
            result.clear();
            int minEnemyDist = Math.max(0, baseMinEnemyDist - attempt);
            rng.shuffle(candidates);
            for (Position p : candidates) {
                if (p.manhattan(start) < minStartDist) continue;
                boolean farEnough = true;
                for (EnemyInstance e : result) {
                    if (p.manhattan(e.pos()) < minEnemyDist) {
                        farEnough = false;
                        break;
                    }
                }
                if (!farEnough) continue;
                result.add(new EnemyInstance(createEnemy(heroLevel, false), p));
                if (result.size() >= desired) break;
            }
            if (result.size() >= desired) break;
        }

        maybeMakeUnique(result, heroLevel);
        maze.enemies().clear();
        maze.enemies().addAll(result);
    }

    private Enemy createEnemy(int heroLevel, boolean unique) {
        int level = unique ? heroLevel + 2 : heroLevel;
        int hp = 100 + level * 10;
        int atk = 15 + level * 5;
        int def = 15 + level * 5;
        String name = unique ? UNIQUE_NAMES.get(rng.nextInt(UNIQUE_NAMES.size())) : NORMAL_NAMES.get(rng.nextInt(NORMAL_NAMES.size()));
        return new Enemy(name, unique, level, atk, def, hp);
    }

    public void maybeMakeUnique(List<EnemyInstance> enemies, int heroLevel) {
        if (enemies.isEmpty() || !rng.chance(0.25)) return;
        int idx = rng.nextInt(enemies.size());
        Position pos = enemies.get(idx).pos();
        enemies.set(idx, new EnemyInstance(createEnemy(heroLevel, true), pos));
    }
}
