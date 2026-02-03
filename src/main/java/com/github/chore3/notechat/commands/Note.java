package com.github.chore3.notechat.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.network.chat.ChatType;
import net.minecraft.server.players.PlayerList;

public class Note {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        var note = Commands.literal("note")
                .requires((CommandSourceStack source) -> source.hasPermission(2));
        
        var info = Commands.literal("info");
        var warn = Commands.literal("warn");
        var alert = Commands.literal("alert");

        var message = Commands.argument("message", MessageArgument.message())
                .executes(context -> {
                    MessageArgument.resolveChatMessage(context, "message", (resolvedMessage) -> {
                        CommandSourceStack commandsourcestack = context.getSource();
                        PlayerList playerlist = commandsourcestack.getServer().getPlayerList();
                        playerlist.broadcastChatMessage(resolvedMessage, commandsourcestack, ChatType.bind(ChatType.SAY_COMMAND, commandsourcestack));
                    });
                    return 1;
                }
            );

    }
}