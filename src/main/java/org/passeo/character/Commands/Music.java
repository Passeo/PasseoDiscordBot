package org.passeo.character.Commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.passeo.character.Commands.api.ISlashCommand;
import org.passeo.character.Commands.api.ISubCommand;
import org.passeo.character.Commands.subcommands.*;

public class Music implements ISlashCommand {

    @Override
    public String getName() {
        return "lagu";
    }

    @Override
    public String getDescription() {
        return "Play Music";
    }

    @Override
    public java.util.List<ISubCommand> getSubCommand() {
        return java.util.List.of(
                new List(),
                new Pause(),
                new Resume(),
                new Skip(),
                new Play()
        );
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {

    }
}
