package nl.jk_5.pumpkin.command;

import org.spongepowered.api.service.command.CommandService;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.command.CommandResult;
import org.spongepowered.api.util.command.spec.CommandSpec;

import nl.jk_5.pumpkin.PumpkinPlugin;

public class PumpkinCommands {

    public static void init(PumpkinPlugin plugin, CommandService dispatcher) {
        CommandSpec versionSpec = CommandSpec.builder()
                .permission("pumpkin.command.pumpkin.version")
                .description(Texts.of("Prints out the version of pumpkin"))
                .executor((src, args) -> {
                    src.sendMessage(Texts.of(TextColors.GREEN, "Pumpkin version " + PumpkinCommands.class.getPackage().getImplementationVersion()));
                    return CommandResult.success();
                }).build();

        CommandSpec pumpkinSpec = CommandSpec.builder()
                .permission("pumpkin.command.pumpkin")
                .description(Texts.of("Commands for the internals of pumpkin"))
                .child(versionSpec, "version")
                .build();

        dispatcher.register(plugin, pumpkinSpec, "pumpkin");
    }
}
