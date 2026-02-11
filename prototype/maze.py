import random

def get_odd_center(dimension):
    """
    Calculates the center index, but ensures it is an ODD number.
    This guarantees the player spawns on a valid path tile,
    not inside a wall column.
    """
    center = dimension // 2
    if center % 2 == 0:
        center += 1
    return center

def generate_maze(width, height):
    # 1. Initialize Grid (1 = Wall, 0 = Path)
    maze = [[1 for _ in range(width)] for _ in range(height)]

    # 2. Define Directions
    directions = [(-2, 0), (2, 0), (0, -2), (0, 2)]

    # 3. Calculate Center (Start Point)
    start_x = get_odd_center(width)
    start_y = get_odd_center(height)

    # Initialize the center as empty
    maze[start_y][start_x] = 0
    stack = [(start_y, start_x)]

    # 4. Recursive Backtracker
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

    # 5. Add Exits
    exit_x = get_odd_center(width)
    exit_y = get_odd_center(height)

    maze[0][exit_x] = 0             # North
    maze[height-1][exit_x] = 0      # South
    maze[exit_y][0] = 0             # West
    maze[exit_y][width-1] = 0       # East

    # 6. Place Player (2 represents Player)
    # We place the player at the exact spot we started generation.
    maze[start_y][start_x] = 2

    return maze

def print_maze(maze, level_num, size):
    print(f"\n--- HERO LEVEL {level_num} ({size}x{size}) ---")

    # 0 = Empty, 1 = Wall, 2 = Player
    chars = {
        1: '##',
        0: '  ',
        2: 'PP'  # Visual representation of Player
    }

    for row in maze:
        print("".join(chars[cell] for cell in row))

# --- EXECUTION ---

level_data = [
    (1, 9), (2, 15), (3, 19), (4, 25), (5, 29),
    (6, 35), (7, 39), (8, 45), (9, 49), (10, 55)
]

for level, size in level_data:
    # Generate
    my_maze = generate_maze(size, size)

    # Visualize
    print_maze(my_maze, level, size)
