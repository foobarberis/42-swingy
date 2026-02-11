import random

def get_odd_center(dimension):
    center = dimension // 2
    if center % 2 == 0:
        center += 1
    return center

def generate_maze(width, height, maze_seed=None):
    # 1. Initialize the seed
    # If no seed is provided, we pick a random one and return it so we can save it.
    if maze_seed is None:
        maze_seed = random.randint(0, 1000000)

    random.seed(maze_seed) # <--- THIS IS THE MAGIC LINE

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
            # Because the seed is set, random.choice will pick the
            # EXACT SAME candidate every time this seed is used.
            dy, dx = random.choice(candidates)
            maze[current_y + (dy // 2)][current_x + (dx // 2)] = 0
            maze[current_y + dy][current_x + dx] = 0
            stack.append((current_y + dy, current_x + dx))
        else:
            stack.pop()

    # (Exits and Player placement logic remains the same...)
    exit_x = get_odd_center(width)
    exit_y = get_odd_center(height)
    maze[0][exit_x] = 0
    maze[height-1][exit_x] = 0
    maze[exit_y][0] = 0
    maze[exit_y][width-1] = 0
    maze[start_y][start_x] = 2

    return maze, maze_seed

# --- PROOF OF DETERMINISM ---

# 1. Generate a maze with a specific seed
seed_to_save = 42
maze_a, _ = generate_maze(15, 15, seed_to_save)

# 2. Generate it again with the same seed
maze_b, _ = generate_maze(15, 15, seed_to_save)

# 3. Generate a third maze with a different seed
maze_c, _ = generate_maze(15, 15, 999)

print(f"Is Maze A identical to Maze B? {maze_a == maze_b}") # True
print(f"Is Maze A identical to Maze C? {maze_a == maze_c}") # False
