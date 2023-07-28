package org.passeo.character.Commands.api;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.List;

public interface ISubCommand {
    String getName();

    String getDescription();
    List<OptionData> getOptions();

    void execute(final SlashCommandInteractionEvent event);
}
