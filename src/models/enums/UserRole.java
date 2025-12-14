package models.enums;

public enum UserRole {
    ADMIN("Quản trị viên", new String[]{
            "MANAGE_BOOKS",
            "MANAGE_READERS",
            "MANAGE_BORROW",
            "VIEW_REPORTS",
            "MANAGE_USERS",
            "SYSTEM_CONFIG"
    }),

    LIBRARIAN("Thủ thư", new String[]{
            "MANAGE_BOOKS",
            "MANAGE_READERS",
            "MANAGE_BORROW",
            "VIEW_REPORTS"
    }),

    READER("Độc giả", new String[]{
            "VIEW_BOOKS",
            "VIEW_OWN_HISTORY",
            "REQUEST_BORROW",
            "SELF_RENEW"
    });

    private final String displayName;
    private final String[] permissions;

    UserRole(String displayName, String[] permissions) {
        this.displayName = displayName;
        this.permissions = permissions;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String[] getPermissions() {
        return permissions;
    }

    public boolean hasPermission(String permission) {
        for (String p : permissions) {
            if (p.equals(permission)) {
                return true;
            }
        }
        return false;
    }
}
