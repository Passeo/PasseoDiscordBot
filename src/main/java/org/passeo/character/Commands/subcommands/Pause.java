package org.passeo.character.Commands.subcommands;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.passeo.character.Commands.api.ISubCommand;
import org.passeo.character.Main;
import org.passeo.character.module.lavaplayer.GuildMusicManager;

import java.util.List;

public class Pause implements ISubCommand {

    @Override
    public String getName() {
        return "pause";
    }

    @Override
    public String getDescription() {
        return "Set Pause";
    }

    @Override
    public List<OptionData> getOptions() {
        return null;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        final Guild guild = event.getGuild();
        final Member member = event.getMember();
        GuildVoiceState memberVoiceState = member.getVoiceState();

        if(!memberVoiceState.inAudioChannel()) {
            event.reply("You need to be in a voice channel").queue();
            return;
        }

        Member self = event.getGuild().getSelfMember();
        GuildVoiceState selfVoiceState = self.getVoiceState();

        if(!selfVoiceState.inAudioChannel()) {
            event.reply("I am not in an audio channel").queue();
            return;
        }

        if(selfVoiceState.getChannel() != memberVoiceState.getChannel()) {
            event.reply("You are not in the same channel as me").queue();
            return;
        }

        final GuildMusicManager manager = Main.music.getGuildAudioPlayer(guild);

        if(manager.player.isPaused()){
            event.reply("The current music is already in a paused phase").queue();
            return;
        }

        manager.scheduler.getPlayer().setPaused(true);
        event.reply("Pause").queue();
    }
}
