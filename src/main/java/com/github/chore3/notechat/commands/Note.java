package com.github.chore3.notechat.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

public class Note {
    private static final SimpleCommandExceptionType ERROR_TOO_FAR = new SimpleCommandExceptionType(Component.translatable("commands.playsound.failed"));

    @SuppressWarnings("removal")
    enum NoteType {
        INFO("ℹ Info", ChatFormatting.AQUA, "minecraft:block.note_block.bit", 1.0F),
        WARN("⚠ Warn", ChatFormatting.GOLD, "minecraft:block.note_block.bell", 0.5F),
        ALERT("❎ Alert", ChatFormatting.RED, "minecraft:block.note_block.bass", 1.0F);

        final String title;
        final ChatFormatting color;
        final  ResourceLocation sound;
        final float pitch;

        NoteType(String title, ChatFormatting color, String soundName, float pitch) {
            this.title = title;
            this.color = color;
            this.pitch = pitch;
            this.sound = new ResourceLocation(soundName);
        }
    }

    private static void playSound(ServerPlayer player, float pitch, ResourceLocation sound, long seed) {
        SoundSource soundSource = SoundSource.MASTER;
        Holder<SoundEvent> holder = Holder.direct(SoundEvent.createVariableRangeEvent(sound));
        player.connection.send(new ClientboundSoundPacket(holder, soundSource, player.getX(), player.getX(), player.getX(), 1.0F, pitch, seed));
    }

    private static int sendNote(CommandContext<CommandSourceStack> context, NoteType type) {
        String msg = StringArgumentType.getString(context, "message");
        MutableComponent component = Component.empty()
                .append(Component.literal("--------------------\n").withStyle(ChatFormatting.GRAY))
                .append(Component.literal(type.title + "\n").withStyle(type.color))
                .append(Component.literal(msg + "\n").withStyle(ChatFormatting.LIGHT_PURPLE))
                .append(Component.literal("--------------------").withStyle(ChatFormatting.GRAY));
        PlayerList list = context.getSource().getServer().getPlayerList();
        long seed = context.getSource().getLevel().getRandom().nextLong();

        int i = 0;
        for (ServerPlayer player : list.getPlayers()) {
            player.sendSystemMessage(component, false);
            playSound(player, type.pitch, type.sound, seed);
            i++;
        }
        return i;
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        var note = Commands.literal("note")
                .requires((CommandSourceStack source) -> source.hasPermission(2));

        var messageInfo = Commands.literal("info").then(Commands.argument("message", StringArgumentType.greedyString())
                .executes(ctx -> sendNote(ctx, NoteType.INFO)));
        var messageWarn = Commands.literal("warn").then(Commands.argument("message", StringArgumentType.greedyString())
                .executes(ctx -> sendNote(ctx, NoteType.WARN)));
        var messageAlert = Commands.literal("alert").then(Commands.argument("message", StringArgumentType.greedyString())
                .executes(ctx -> sendNote(ctx, NoteType.ALERT)));

        note.then(messageInfo)
            .then(messageWarn)
            .then(messageAlert);
        dispatcher.register(note);
    }
}