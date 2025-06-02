package pro.trevor.cobblemonrandoms.command.permission;

public class Permissions {

    private static final String COMMAND_PERMISSION_PREFIX = "command.";

    private static Permission commandPermission(String unqualifiedIdentifier, PermissionLevel level) {
        return new Permission(COMMAND_PERMISSION_PREFIX + unqualifiedIdentifier, level);
    }

    public static final Permission GTS = commandPermission("gts", PermissionLevel.NONE);
    public static final Permission GTS_CHECK = commandPermission("gtscheck", PermissionLevel.NONE);
    public static final Permission GTS_CANCEL = commandPermission("gtscancel", PermissionLevel.NONE);
    public static final Permission GTS_SEARCH = commandPermission("gtssearch", PermissionLevel.NONE);
    public static final Permission GTS_QUERY = commandPermission("gtsquery", PermissionLevel.NONE);
    public static final Permission GTS_FORCE_QUERY = commandPermission("gtsforcequery", PermissionLevel.CHEATS);
    public static final Permission GTS_FORCE_COMPLETE = commandPermission("gtsforcecomplete", PermissionLevel.CHEATS);
    public static final Permission GTS_TEST = commandPermission("gtstest", PermissionLevel.CHEATS);


}
