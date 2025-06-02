package pro.trevor.cobblemonrandoms.command.permission;

public enum PermissionLevel {
    NONE(0),
    ELEVATED(1),
    CHEATS(2),
    MULTIPLAYER(3),
    ALL(4);

    final int value;

    PermissionLevel(int value) {
        this.value = value;
    }

    int value() {
        return value;
    }
}
