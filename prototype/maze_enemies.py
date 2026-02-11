import random
import math

def get_odd_center(dimension):
    center = dimension // 2
    if center % 2 == 0:
        center += 1
    return center

def generate_maze_with_entities(width, height):
    # --- 1. GENERATE MAZE STRUCTURE ---
    maze = [[1 for _ in range(width)] for _ in range(height)]
    directions = [(-2, 0), (2, 0), (0, -2), (0, 2)]

    start_x = get_odd_center(width)
    start_y = get_odd_center(height)

    maze[start_y][start_x] = 0
    stack = [(start_y, start_x)]

    while stack:
        current_y, current_x = stack[-1]
        candidates = []
        for dy, dx in directions:
            ny, nx = current_y + dy, current_x + dx
            if 1 <= ny < height - 1 and 1 <= nx < width - 1:
                if maze[ny][nx] == 1:
                    candidates.append((dy, dx))

        if candidates:
            dy, dx = random.choice(candidates)
            maze[current_y + (dy // 2)][current_x + (dx // 2)] = 0
            maze[current_y + dy][current_x + dx] = 0
            stack.append((current_y + dy, current_x + dx))
        else:
            stack.pop()

    # Exits
    exit_x, exit_y = get_odd_center(width), get_odd_center(height)
    maze[0][exit_x] = 0
    maze[height-1][exit_x] = 0
    maze[exit_y][0] = 0
    maze[exit_y][width-1] = 0

    # --- 2. PLACE PLAYER ---
    maze[start_y][start_x] = 2  # 2 = Player
    player_pos = (start_y, start_x)

    # --- 3. CALCULATE ENEMY COUNT ---
    # Formula: Area / 32. Max ensures at least 1 enemy on tiny maps.
    area = width * height
    num_enemies = max(1, area // 32)

    # --- 4. PLACE ENEMIES (Evenly) ---
    # Get all valid floor tiles (coordinate tuples)
    valid_spots = []
    for y in range(height):
        for x in range(width):
            # Must be empty (0) and not the player (2)
            if maze[y][x] == 0:
                valid_spots.append((y, x))

    # Shuffle to make it random
    random.shuffle(valid_spots)

    enemies_placed = []

    # Minimum distance required between entities (scales slightly with map size)
    # We use a small base value so we don't run out of spots on small maps
    min_dist = max(2, width // 6)

    for spot_y, spot_x in valid_spots:
        if len(enemies_placed) >= num_enemies:
            break

        # Check distance to Player
        dist_to_player = math.sqrt((spot_y - player_pos[0])**2 + (spot_x - player_pos[1])**2)
        if dist_to_player < min_dist:
            continue

        # Check distance to other Enemies
        too_close = False
        for ey, ex in enemies_placed:
            dist = math.sqrt((spot_y - ey)**2 + (spot_x - ex)**2)
            if dist < min_dist:
                too_close = True
                break

        if not too_close:
            maze[spot_y][spot_x] = 3 # 3 = Enemy
            enemies_placed.append((spot_y, spot_x))

    return maze, num_enemies

def print_maze(maze, level_num, size, enemy_count):
    print(f"\n--- HERO LEVEL {level_num} ({size}x{size}) ---")
    print(f"Enemies Spawned: {enemy_count}")

    # 0=Path, 1=Wall, 2=Player, 3=Enemy
    chars = {
        1: '##',
        0: '  ',
        2: 'PP',
        3: 'EE'
    }

    for row in maze:
        print("".join(chars[cell] for cell in row))

# --- EXECUTION ---

level_data = [
    (1, 9), (2, 15), (3, 19), (4, 25), (5, 29),
    (6, 35), (7, 39), (8, 45), (9, 49), (10, 55)
]

# I reduced the output list to just 4 examples so you don't have to scroll too much
for level, size in level_data:
    my_maze, count = generate_maze_with_entities(size, size)
    print_maze(my_maze, level, size, count)
