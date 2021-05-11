package me.novascomp.files.server.ocs;

public enum OcsShareCommand {

    PUBLIC_LINK(3);

    private final Integer number;

    private OcsShareCommand(Integer command) {
        this.number = command;
    }

    public Integer getCommand() {
        return number;
    }

    @Override
    public String toString() {
        return "OcsShareCommand{" + "command=" + number + '}';
    }

}
