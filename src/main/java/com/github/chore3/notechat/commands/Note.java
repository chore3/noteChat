package com.github.chore3.notechat.commands;


import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;

public class Note {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        var note = Commands.literal("note")
                .requires((CommandSourceStack source) -> source.hasPermission(2));
        
        var info = Commands.literal("info");
        var warn = Commands.literal("warn");
        var alert = Commands.literal("alert");

        var message = Commands.argument("message", StringArgumentType.greedyString()).executes((context) -> {
            CommandSourceStack commandsourcestack = context.getSource();
            PlayerList playerlist = commandsourcestack.getServer().getPlayerList();
            MutableComponent component = Component.empty()
                    .append(Component.literal("[警告] ").withStyle(ChatFormatting.RED))
                    .append(Component.literal("デスワームに騎乗しないでください").withStyle(ChatFormatting.GOLD));

            int i = 0;
            for(ServerPlayer serverplayer : playerlist.getPlayers()) {
                serverplayer.sendSystemMessage(component, false);
            }

            return i;
        });

        note.then(info.then(message))
                .then(warn.then(message))
                .then(alert.then(message));

        dispatcher.register(note);
    }
}