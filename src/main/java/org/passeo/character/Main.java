package org.passeo.character;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.passeo.character.module.ThreadListener;

public class Main {
    private static final Logger LOGGER = LogManager.getLogger(Main.class);
    private static final String TOKEN_BOT = "TOKEN HERE";
    private static JDA api;

    public static void main(String[] args) {
        api = JDABuilder.createDefault(TOKEN_BOT)
                .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.MESSAGE_CONTENT)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .build();
        LOGGER.info("Creating the bot, Please Wait!");
        try {
            api.awaitReady();
        } catch (final InterruptedException ex){
            LOGGER.error("Error while waiting the bot", ex);
        }
        LOGGER.info("Connecting to all Listeners");
        new ThreadListener(api);


    }

}
