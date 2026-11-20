package com.example.aiagent.command.impl;

import com.example.aiagent.agent.AgentService;
import com.example.aiagent.command.Command;
import com.example.aiagent.command.CommandContext;
import com.example.aiagent.command.CommandResult;
import org.springframework.stereotype.Component;

@Component
public class ResetConversationCommand implements Command {
    private final AgentService agentService;

    public ResetConversationCommand(AgentService agentService) {
        this.agentService = agentService;
    }

    @Override
    public String name() {
        return "/reset";
    }

    @Override
    public String description() {
        return "현재 대화 기록을 초기화합니다";
    }

    @Override
    public boolean availableInChat() {
        return true;
    }

    @Override
    public CommandResult execute(CommandContext context, String input) {
        agentService.resetConversation();
        context.console().println("대화 기록을 초기화했습니다.");
        return CommandResult.CONTINUE;
    }
}
