package com.swingy.support;

import com.swingy.model.Hero;
import com.swingy.persistence.HeroRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class InMemoryHeroRepository implements HeroRepository {
    private final List<Hero> heroes = new ArrayList<>();
    public int saveCalls;
    public int deleteCalls;
    public String lastDeletedName;

    @Override
    public List<Hero> list() {
        return new ArrayList<>(heroes);
    }

    @Override
    public Hero loadByName(String name) throws IOException {
        return heroes.stream().filter(h -> h.getName().equals(name)).findFirst()
                .orElseThrow(() -> new IOException("Unknown hero"));
    }

    @Override
    public void save(Hero hero) {
        saveCalls++;
        for (int i = 0; i < heroes.size(); i++) {
            if (heroes.get(i).getName().equals(hero.getName())) {
                heroes.set(i, hero);
                return;
            }
        }
        heroes.add(hero);
    }

    @Override
    public void deleteByName(String name) {
        deleteCalls++;
        lastDeletedName = name;
        heroes.removeIf(h -> h.getName().equals(name));
    }
}
