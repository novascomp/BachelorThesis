package me.novascomp.files.server.webdav;

public enum WebDavCommand {

    CREATE_FOLDER("MKCOL"),
    DELETE_FOLDER("DELETE"),
    CREATE_FILE("PUT"),
    DELETE_FILE("DELETE"),
    JSON_FORMAT_REQUEST("?format=json");

    private final String command;

    private WebDavCommand(String command) {
        this.command = command;
    }

    @Override
    public String toString() {
        return command;
    }
}
