package com.example.aiagent.command;

import java.io.IOException;
import java.util.List;

public interface Command {
    String name();

    String description();

    CommandResult execute(CommandContext context, String input)
            throws IOException, InterruptedException;

    default List<String> aliases() {
        return List.of();
    }

    default boolean availableInMenu() {
        return true;
    }

    default boolean availableInChat() {
        return false;
    }
}
