package org.passeo.character.Commands.subcommands;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.passeo.character.Commands.api.ISubCommand;
import org.passeo.character.Main;

import java.util.List;

public class Skip implements ISubCommand {

    @Override
    public String getName() {
        return "skip";
    }

    @Override
    public String getDescription() {
        return "Skip a track";
    }

    @Override
    public List<OptionData> getOptions() {
        return null;
    }


    @Override
    public void execute(final SlashCommandInteractionEvent event) {
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

        Main.music.getGuildAudioPlayer(guild).scheduler.getPlayer().stopTrack();
        event.reply("Skipped").queue();
    }
}
