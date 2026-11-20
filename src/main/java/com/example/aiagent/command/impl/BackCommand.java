package com.example.aiagent.command.impl;

import com.example.aiagent.agent.AgentService;
import com.example.aiagent.command.Command;
import com.example.aiagent.command.CommandContext;
import com.example.aiagent.command.CommandResult;
import org.springframework.stereotype.Component;

@Component
public class BackCommand implements Command {
    private final AgentService agentService;

    public BackCommand(AgentService agentService) {
        this.agentService = agentService;
    }

    @Override
    public String name() {
        return "/back";
    }

    @Override
    public String description() {
        return "대화 모드에서 명령 메뉴로 돌아갑니다";
    }

    @Override
    public boolean availableInChat() {
        return true;
    }

    @Override
    public CommandResult execute(CommandContext context, String input) {
        agentService.leaveChat();
        context.console().println("명령 메뉴로 돌아갑니다.");
        return CommandResult.LEAVE_CHAT;
    }
}
