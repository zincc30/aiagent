package com.example.aiagent.command.impl;

import com.example.aiagent.command.Command;
import com.example.aiagent.command.CommandContext;
import com.example.aiagent.command.CommandResult;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ExitCommand implements Command {
    @Override
    public String name() {
        return "/exit";
    }

    @Override
    public String description() {
        return "프로그램을 종료합니다";
    }

    @Override
    public List<String> aliases() {
        return List.of("exit");
    }

    @Override
    public boolean availableInChat() {
        return true;
    }

    @Override
    public CommandResult execute(CommandContext context, String input) {
        context.console().println("프로그램을 종료합니다.");
        return CommandResult.EXIT;
    }
}
