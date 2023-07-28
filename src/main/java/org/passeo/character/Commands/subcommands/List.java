package org.passeo.character.Commands.subcommands;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.apache.commons.text.WordUtils;
import org.passeo.character.Commands.api.ISubCommand;
import org.passeo.character.Main;
import org.passeo.character.module.lavaplayer.GuildMusicManager;

import java.awt.*;
import java.util.Queue;
import java.util.Random;

public class List implements ISubCommand {

    @Override
    public String getName() {
        return "list";
    }

    @Override
    public String getDescription() {
        return "List of Queue Music";
    }

    @Override
    public java.util.List<OptionData> getOptions() {
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
        final Queue<AudioTrack> tracks = manager.scheduler.getQueue();
        EmbedBuilder builder = getMusicEmbed(guild, event.getUser());
        for(final AudioTrack track : tracks){
            builder.addField(track.getInfo().title, track.getInfo().author, false);
        }
        event.replyEmbeds(builder.build()).queue();
    }

    private EmbedBuilder getMusicEmbed(Guild guild, User user) {
        EmbedBuilder builder = getDefaultEmbed(user);
        builder.setAuthor(WordUtils.capitalize(getName()), "https://www.google.com/", guild.getSelfMember().getUser().getAvatarUrl());
        return builder;
    }

    private EmbedBuilder getDefaultEmbed(User author) {
        try {
            final Random random = new Random();
            final float hue = random.nextFloat();
            final float saturation = (random.nextInt(2000) + 1000) / 10000f;
            final float luminance = 2f;
            final Color color = Color.getHSBColor(hue, saturation, luminance);

            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(color);
            builder.setFooter("Requested by {0}".replace
                    ("{0}", author.getAsMention()), author.getAvatarUrl());
            return builder;
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
