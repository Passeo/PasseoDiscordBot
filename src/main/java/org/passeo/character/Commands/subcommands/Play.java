package org.passeo.character.Commands.subcommands;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.managers.AudioManager;
import org.passeo.character.Commands.api.ISubCommand;
import org.passeo.character.Main;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

public class Play implements ISubCommand {
    @Override
    public String getName() {
        return "play";
    }

    @Override
    public String getDescription() {
        return "Play Music From Youtube Link";
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of(new OptionData(OptionType.STRING, "url", "Play Music From URL"));
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        final Member member = event.getMember();
        if (member == null) {
            return;
        }
        final VoiceChannel voiceChannel = member.getVoiceState().getChannel().asVoiceChannel();
        final AudioManager audioManager = event.getGuild().getAudioManager();
        audioManager.openAudioConnection(voiceChannel);
        event.reply("Succes").setEphemeral(true).queue();
        final String url = event.getOption("url").getAsString();
        try {
            Main.music.loadMusic(event.getChannel().asTextChannel(), url);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}