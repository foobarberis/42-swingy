package com.swingy.model;

import com.swingy.model.world.Direction;
import com.swingy.model.world.Position;
import com.swingy.model.world.Room;

import java.util.Objects;

public final class Mission {
    private final Hero hero;
    private final Room room;
    private Position heroPosition;
    private Position previousPosition;

    public Mission(Hero hero, Room room) {
        this.hero = Objects.requireNonNull(hero, "Hero is required.");
        this.room = Objects.requireNonNull(room, "Room is required.");
        heroPosition = room.center();
    }

    public MoveResult move(Direction direction) {
        Objects.requireNonNull(direction, "Direction is required.");
        Position destination = direction.move(heroPosition);
        if (!room.isInside(destination)) {
            return MoveResult.blocked();
        }

        previousPosition = heroPosition;
        heroPosition = destination;
        if (room.isBorder(destination)) {
            return MoveResult.won();
        }

        Enemy enemy = room.enemyAt(destination);
        return enemy == null ? MoveResult.moved() : MoveResult.encounter(enemy);
    }

    public void retreat() {
        if (previousPosition == null) {
            throw new IllegalStateException("There is no previous position to restore.");
        }
        heroPosition = previousPosition;
    }

    public Hero getHero() {
        return hero;
    }

    public Room getRoom() {
        return room;
    }

    public Position getHeroPosition() {
        return heroPosition;
    }

    public record MoveResult(Type type, Enemy enemy) {
        public enum Type {
            BLOCKED,
            MOVED,
            WON,
            ENCOUNTER
        }

        public MoveResult {
            Objects.requireNonNull(type, "Move result type is required.");
            if (type == Type.ENCOUNTER) {
                Objects.requireNonNull(enemy, "Encounter enemy is required.");
            }
        }

        private static MoveResult blocked() {
            return new MoveResult(Type.BLOCKED, null);
        }

        private static MoveResult moved() {
            return new MoveResult(Type.MOVED, null);
        }

        private static MoveResult won() {
            return new MoveResult(Type.WON, null);
        }

        private static MoveResult encounter(Enemy enemy) {
            return new MoveResult(Type.ENCOUNTER, enemy);
        }
    }
}
