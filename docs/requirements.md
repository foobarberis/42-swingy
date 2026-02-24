# Requirements and evalutation

## Requirements

- You are allowed to use language features up to the latest Java LTS
  Version included.
- You are **NOT** allowed to use any external libraries, build tools or
  code generators.
- Do not use the default package.
- Create your own relevant packages following the Java package naming
  conventions.
- Java is compiled into an intermediate language. This will generate some
  `.class` files. Do not commit them on your repository!
- Make sure you have `javac` and `java` available as commands in your
  terminal.
- Make sure you have the `mvn` command line tool available, or use one
  bundled in your IDE.
- Build the project running the command below in the root of your project
  folder. This needs to generate a runnable `.jar` file that can launch the
  game.

```bash
mvn clean package
```

You need to implement a text-based RPG based on the gameplay and conditions
described below. The program needs to follow the Model-View-Controller
architecture and allow switching between the console view and GUI view.

### Gameplay

A player can have multiple heroes of different types. We leave it to you to
name the hero types and fine tune the different starting stats between
them. When the player starts the game he has 2 options:

- Create a hero
- Select a previously created hero.

In either case, the player can see the hero stats:

- Hero name
- Hero class
- Level
- Experience
- Attack
- Defense
- Hit Points

Hero stats are affected by the hero level and artifacts. There are 3 types
of artifacts:

- **Weapon** - increases the attack
- **Armor** - increases defense
- **Helm** - increases hit points

After choosing a hero the actual game begins. The hero needs to navigate a
square map with the size calculated by the formula: `(level - 1) * 5 + 10 - (level % 2)`.
For example, a hero of level 7 will be placed on a 39X39 map.

The initial position of the hero is in the center of the map. He wins the
game if he reaches one of the borders of the map. Each turn he can move one
position in one of the 4 directions:

- North
- East
- South
- West

When a map is generated, villains of varying power will be spread randomly
over the map. When a hero moves to a position occupied by a villain, the
hero has 2 options:

- **Fight**, which engages him in a battle with the villain.
- **Run**, which gives him a 50% chance of returning to the previous
  position. If the odds aren't on his side, he must fight the villain.

You will need to simulate the battle between the hero and monster and
present the user the outcome of the battle. We leave it at you to find a
nice simulation algorithm that decides based on the hero and monster stats,
who will win. You can include a small "luck" component in the algo in order
to make the game more entertaining.

If a hero loses a battle, he dies and also loses the mission.

If a hero wins a battle, he gains:

- **Experience points**, based on the villain power. Of course, he will
  level up if he reaches the next experience threshold.
- **An artifact**, which he can keep or leave. Of course, winning a battle
  doesn't guarantee that an artifact will be dropped and the quality of the
  artifact also varies depending on the villain's strength.

Leveling up is based on the following formula: `level * 1000 + (level - 1)^2 * 450`.
So the necessary experience to level up will follow this pattern:

- Level 1 - 1000 XP
- Level 2 - 2450 XP
- Level 3 - 4800 XP
- Level 4 - 8050 XP
- Level 5 - 12200 XP

### Features

The game can be launched in 2 modes as described below:

```bash
java -jar swingy.jar console
java -jar swingy.jar gui
```

A user's heroes and their state will be preserved, when the user exits the
game, in a text file. When starting the game, your program will load the
heroes from this file.

### Validation

You will need to integrate a third party library in your project in order
to provide annotation based validation. We highly recommend that you use a
library that implements the `javax.validation` specification.

You will not allow any abnormal user input to disrupt the game behaviour.
Validation failure will be highlighted to the user.

Although the use of libraries is generally prohibited, an exception is made
for integrating a third-party library compliant with the `javax.validation`
specification for annotation-based validation purposes.

### Bonus

Bonus points will be given if:

- You persist the user's heroes in a relational database, instead of a text
  file.
- You can switch between console view and GUI view at runtime, without
  closing the game.

> [!CAUTION] You are allowed to use a library for this bonus section.
> However, the use of the library must be explicitly justified and should
> only serve this specific part.

## Evaluation

- Compile and build the program with the commands described in the subject.
  Does the project compile and generate a runnable jar file?

- Does project follow the Model-View-Controller architecture? It is not a
  problem if there are additional classes that don't fit in the design if
  they are helper classes or add additional features.

- Check if the program preserves the state between plays. Are the heroes
  persisted? Is everything the same as the last time you played the game?

- Check if the program validates the user input. Try running the program
  and test what happens if there are missing values or out of range values.
  Does the validation work? Do you receive a reasonable error message?

- Check the project code and see if the validation was applied through
  annotations. They should be in the model classes.

- Does the program connect to a database server and save the player's
  heroes there?

- Does the program offer a means by which to change the views (console and
  GUI) during program execution?
