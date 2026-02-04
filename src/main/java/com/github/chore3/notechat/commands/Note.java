package com.github.chore3.notechat.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;

public class Note {
    enum NoteType {
        INFO("ℹ Info", ChatFormatting.AQUA),
        WARN("⚠ Warn", ChatFormatting.GOLD),
        ALERT("‼ Alert", ChatFormatting.RED);

        final String title;
        final ChatFormatting color;

        NoteType(String title, ChatFormatting color) {
            this.title = title;
            this.color = color;
        }
    }

    private static int sendNote(CommandContext<CommandSourceStack> context, NoteType type) {
        String msg = StringArgumentType.getString(context, "message");
        MutableComponent component = Component.empty()
                .append(Component.literal("--------------------\n").withStyle(ChatFormatting.GRAY))
                .append(Component.literal(type.title + "\n").withStyle(type.color))
                .append(Component.literal(msg + "\n").withStyle(ChatFormatting.LIGHT_PURPLE))
                .append(Component.literal("--------------------").withStyle(ChatFormatting.GRAY));
        PlayerList list = context.getSource().getServer().getPlayerList();
        int i = 0;
        for (ServerPlayer player : list.getPlayers()) {
            player.sendSystemMessage(component, false);
            i++;
        }
        return i;
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        var note = Commands.literal("note")
                .requires((CommandSourceStack source) -> source.hasPermission(2));
        
        var info = Commands.literal("info");
        var warn = Commands.literal("warn");
        var alert = Commands.literal("alert");

        var message = Commands.argument("message", StringArgumentType.greedyString()).executes((context) -> {
            CommandSourceStack commandsourcestack = context.getSource();
            PlayerList playerlist = commandsourcestack.getServer().getPlayerList();
            String strMsg = StringArgumentType.getString(context, "message");
            MutableComponent component = Component.empty()
                    .append(Component.literal("--------------------\n").withStyle(ChatFormatting.GRAY))
                    .append(Component.literal("ℹ Info\n").withStyle(ChatFormatting.AQUA))
                    .append(Component.literal(strMsg + "\n").withStyle(ChatFormatting.LIGHT_PURPLE))
                    .append(Component.literal("--------------------").withStyle(ChatFormatting.GRAY));

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