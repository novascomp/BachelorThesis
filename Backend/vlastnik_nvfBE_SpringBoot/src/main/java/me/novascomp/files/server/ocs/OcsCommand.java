package me.novascomp.files.server.ocs;

public enum OcsCommand {

    SHARE_FILE("POST"),
    DELETE_SHARE_FILE("DELETE"),
    JSON_FORMAT_REQUEST("?format=json");

    private final String command;

    private OcsCommand(String command) {
        this.command = command;
    }

    public String getCommand() {
        return command;
    }

    @Override
    public String toString() {
        return command;
    }
}
