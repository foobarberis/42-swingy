package com.swingy.persistence;

import com.swingy.model.Hero;

import java.io.IOException;
import java.util.List;

public interface HeroRepository {
    List<Hero> list() throws IOException;

    Hero load(String name) throws IOException;

    void save(Hero hero) throws IOException;

    void delete(String name) throws IOException;
}
