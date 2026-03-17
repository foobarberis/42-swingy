package com.swingy.logic;

import com.swingy.model.Hero;
import com.swingy.model.world.Room;

@FunctionalInterface
public interface RoomFactory {
    Room create(Hero hero);
}
