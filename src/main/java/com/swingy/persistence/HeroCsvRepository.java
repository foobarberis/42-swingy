package com.swingy.persistence;

import com.swingy.model.Hero;

import javax.validation.Validator;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HeroCsvRepository implements HeroRepository {
    private final Path path;
    private final CsvHeroParser parser;
    private final CsvHeroSerializer serializer;
    private final Validator validator;

    public HeroCsvRepository(Path path, CsvHeroParser parser, CsvHeroSerializer serializer, Validator validator) {
        this.path = path;
        this.parser = parser;
        this.serializer = serializer;
        this.validator = validator;
    }

    @Override
    public List<Hero> list() throws IOException {
        if (!Files.exists(path)) {
            return List.of();
        }
        return readAllStrict();
    }

    @Override
    public Hero loadByName(String name) throws IOException {
        if (!Files.exists(path)) throw new IOException("Missing file");
        for (Hero h : readAllStrict()) {
            if (h.getName().equals(name)) return h;
        }
        throw new IOException("Unknown hero");
    }

    @Override
    public void save(Hero hero) throws IOException {
        List<Hero> heroes = Files.exists(path) ? readAllStrict() : new ArrayList<>();
        boolean replaced = false;
        for (int i = 0; i < heroes.size(); i++) {
            if (heroes.get(i).getName().equals(hero.getName())) {
                heroes.set(i, hero);
                replaced = true;
                break;
            }
        }
        if (!replaced) heroes.add(hero);
        writeAtomic(heroes);
    }

    @Override
    public void deleteByName(String name) throws IOException {
        if (!Files.exists(path)) return;
        List<Hero> heroes = readAllStrict();
        heroes.removeIf(h -> h.getName().equals(name));
        writeAtomic(heroes);
    }

    private List<Hero> readAllStrict() throws IOException {
        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        List<Hero> heroes = new ArrayList<>();
        Set<String> names = new HashSet<>();

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            int lineNumber = i + 1;

            if (line.isBlank()) {
                throw new SaveFileCorruptedException(path, lineNumber, "Blank line");
            }

            final Hero hero;
            try {
                hero = parser.parse(line);
            } catch (CsvParseException e) {
                throw new SaveFileCorruptedException(path, lineNumber, e.getMessage(), e);
            }

            if (!validator.validate(hero).isEmpty()) {
                throw new SaveFileCorruptedException(path, lineNumber, "Validation error");
            }
            if (!names.add(hero.getName())) {
                throw new SaveFileCorruptedException(path, lineNumber, "Duplicate names");
            }
            heroes.add(hero);
        }
        return heroes;
    }

    private void writeAtomic(List<Hero> heroes) throws IOException {
        Path tmp = path.resolveSibling(path.getFileName() + ".tmp");
        List<String> lines = heroes.stream().map(serializer::serialize).toList();
        Files.write(tmp, lines, StandardCharsets.UTF_8);
        try {
            Files.move(tmp, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            Files.move(tmp, path, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
