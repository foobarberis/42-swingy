import time
import random
import sys

# --- CONFIGURATION ---
TIME_LIMIT = 5  # Seconds allowed to react
PLAYER_MAX_HP = 125
ENEMY_MAX_HP = 30
DAMAGE_AMOUNT = 15

# --- COLORS (ANSI Codes) ---
class Color:
    RED = '\033[91m'    # For Attack
    GREEN = '\033[92m'  # For Sunder
    BLUE = '\033[94m'   # For Defend
    RESET = '\033[0m'   # Reset to default

# --- MOVE DEFINITIONS ---
MOVES = {
    'a': 'attack',
    's': 'special',
    'd': 'defend'
}

WINNING_MATCHUPS = {
    'attack': 'special',
    'special': 'defend',
    'defend': 'attack'
}

class Game:
    def __init__(self):
        self.player_hp = PLAYER_MAX_HP
        self.enemy_hp = ENEMY_MAX_HP

    def get_colored_move(self, move_name):
        """Returns the move string wrapped in its specific color."""
        if move_name == 'attack':
            return f"{Color.RED}{move_name}{Color.RESET}"
        elif move_name == 'special':
            return f"{Color.GREEN}{move_name}{Color.RESET}"
        elif move_name == 'defend':
            return f"{Color.BLUE}{move_name}{Color.RESET}"
        return move_name

    def print_status(self):
        print(f"[HP: {self.player_hp}/{PLAYER_MAX_HP} | Enemy HP: {self.enemy_hp}/{ENEMY_MAX_HP}]")

    def get_enemy_move(self):
        keys = list(MOVES.keys())
        choice = random.choice(keys)
        return MOVES[choice]

    def get_player_input(self):
        start_time = time.time()
        try:
            # We don't use input() prompt text here so we can keep the cursor clean
            sys.stdout.write("> ")
            sys.stdout.flush()
            user_input = sys.stdin.readline().strip().lower()
        except EOFError:
            return None, False

        end_time = time.time()
        duration = end_time - start_time

        action = None
        for key, value in MOVES.items():
            if user_input == key or user_input == value:
                action = value
                break

        return action, duration <= TIME_LIMIT

    def resolve_combat(self, enemy_move, player_move, is_fast_enough):
        enemy_move_str = self.get_colored_move(enemy_move)

        # 1. Too Slow
        if not is_fast_enough:
            print(f"Too slow! You take a hit from {enemy_move_str}.")
            self.take_damage("player")
            return

        # 2. Invalid Input
        if player_move is None:
            print(f"You stumbled! You take a hit from {enemy_move_str}.")
            self.take_damage("player")
            return

        player_move_str = self.get_colored_move(player_move)

        # 3. Player Wins
        if WINNING_MATCHUPS[player_move] == enemy_move:
            self.success_text(player_move, enemy_move)
            self.enemy_hp -= DAMAGE_AMOUNT

        # 4. Enemy Wins
        elif WINNING_MATCHUPS[enemy_move] == player_move:
            print(f"Your {player_move_str} was overpowered by {enemy_move_str}!")
            self.player_hp -= DAMAGE_AMOUNT

        # 5. Tie
        else:
            print(f"Clash! Both used {player_move_str}. No damage.")

    def take_damage(self, target):
        if target == "player":
            self.player_hp -= DAMAGE_AMOUNT

    def success_text(self, p_move, e_move):
        # We define specific flavor text for counters
        if p_move == 'defend' and e_move == 'attack':
            print("You block the goblin's attack.")
        elif p_move == 'special' and e_move == 'defend':
            print("You breach the goblin's defense.")
        elif p_move == 'attack' and e_move == 'special':
            print("You interrupt the goblin's special.")
        else:
            # Fallback
            print(f"You hit the goblin with {p_move}!")

    def run(self):
        print("COMBAT STARTED!")
        print(f"Counters: {Color.RED}Attack{Color.RESET} beats {Color.GREEN}Sunder{Color.RESET}, "
              f"{Color.GREEN}Sunder{Color.RESET} beats {Color.BLUE}Defend{Color.RESET}, "
              f"{Color.BLUE}Defend{Color.RESET} beats {Color.RED}Attack{Color.RESET}.")

        time.sleep(1)

        while self.player_hp > 0 and self.enemy_hp > 0:
            # 1. Get Move
            enemy_move = self.get_enemy_move()

            # 2. Callout with Color
            print(f"Goblin is about to {self.get_colored_move(enemy_move)}.")

            self.print_status()

            # 3. Input
            player_move, is_fast_enough = self.get_player_input()

            # 4. Resolve
            self.resolve_combat(enemy_move, player_move, is_fast_enough)
            print("-" * 30)

        if self.player_hp <= 0:
            print("\nYOU DIED.")
            self.print_status()
        else:
            print("\nGoblin is dead.")
            print(f"[HP: {self.player_hp}/{PLAYER_MAX_HP} | Enemy HP: 0/{ENEMY_MAX_HP}]")

if __name__ == "__main__":
    game = Game()
    game.run()
