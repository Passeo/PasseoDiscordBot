package org.passeo.character.Commands.api;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

import java.util.ArrayList;
import java.util.List;

public interface ISlashCommand {
    String getName();

    String getDescription();

    List<ISubCommand> getSubCommand();

    void execute(final SlashCommandInteractionEvent event);

    default List<SubcommandData> getSubCommandData(){
        final List<SubcommandData> subCommands = new ArrayList<>();
        for (final ISubCommand command : getSubCommand()) {
            SubcommandData data = null;
            if(command.getOptions() == null){
                data = new SubcommandData(command.getName(), command.getDescription());
            } else {
                data = new SubcommandData(command.getName(), command.getDescription()).addOptions(command.getOptions());
            }
            subCommands.add(data);
        }

        return subCommands;
    }
}
