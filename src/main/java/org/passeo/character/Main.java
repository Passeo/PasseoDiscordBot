package org.passeo.character;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.common.collect.Lists;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.passeo.character.Commands.api.ISlashCommand;
import org.passeo.character.module.Music;
import org.passeo.character.runnable.SpotifyTokenRefresh;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.SpotifyHttpManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main extends ListenerAdapter {
    private static final Logger LOGGER = LogManager.getLogger(Main.class);

    private static final String TOKEN_BOT = "TOKEN BOT HERE";

    private static final String SPOTIFY_CLIENT_ID = "CLIENT ID HERE";
    private static final String SPOTIFY_CLIENT_SECRET = "SECRET ID HERE";

    private static JDA api;
    private static List<ISlashCommand> commands;

    private static final List<String> SCOPES = Lists.newArrayList("https://www.googleapis.com/auth/youtube.readonly");

    private static final String APPLICATION_NAME = "Passeo Discord Bot";
    /**
     * Global instance of the JSON factory.
     */
    private static final JsonFactory JSON_FACTORY = new GsonFactory();

    public static Music music;
    public static SpotifyApi spotifyApi;
    public static YouTube youTubeApi;
    public static ScheduledExecutorService globalExecutorService = Executors.newScheduledThreadPool(20);


    public Main() {
        api.addEventListener(this);
    }

    public static void main(String[] args) throws IOException {
        api = JDABuilder.createDefault(TOKEN_BOT)
                .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.MESSAGE_CONTENT)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .build();

        spotifyApi = SpotifyApi.builder()
                .setClientId(SPOTIFY_CLIENT_ID)
                .setClientSecret(SPOTIFY_CLIENT_SECRET)
                .setRedirectUri(SpotifyHttpManager.makeUri("http://localhost/"))
                .build();
        final SpotifyTokenRefresh spotifyTokenRefresh = new SpotifyTokenRefresh();
        spotifyTokenRefresh.run();
        try {
            youTubeApi = getService();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


        globalExecutorService.scheduleAtFixedRate(spotifyTokenRefresh, 10, 10, TimeUnit.SECONDS);
        LOGGER.info("Creating the bot, Please Wait!");
        try {
            api.awaitReady();
        } catch (final InterruptedException ex) {
            LOGGER.error("Error while waiting the bot", ex);
        }

        LOGGER.info("Connecting to all Listeners");

        commands = new ArrayList<>();

        addCommand(new org.passeo.character.Commands.Music());

        AudioPlayerManager playerManager = new DefaultAudioPlayerManager();

        music = new Music(playerManager);

        for (final ISlashCommand command : commands) {
            if (command.getSubCommandData() == null) {
                api.upsertCommand(command.getName(), command.getDescription()).queue(System.out::println);
            } else {
                api.upsertCommand(command.getName(), command.getDescription()).addSubcommands(command.getSubCommandData()).queue(System.out::println);
            }
        }

        new Main();

    }

    /**
     * Build and return an authorized API client service.
     *
     * @return an authorized API client service
     * @throws GeneralSecurityException, IOException
     */
    public static YouTube getService() throws GeneralSecurityException, IOException {
        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        try {
            final Credential credential = authorize(httpTransport);
            return new YouTube.Builder(httpTransport, JSON_FACTORY, credential)
                    .setApplicationName(APPLICATION_NAME)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Credential authorize(HttpTransport transport) throws Exception {
        final InputStream in = Main.class.getResourceAsStream("/client_secrets.json");
        if (in == null) {
            System.out.println("Client Secrets json file not found please create one in resources");
            System.exit(404);
        }
        // Load client secrets.
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(
                JSON_FACTORY, new InputStreamReader(in));

        // Checks that the defaults have been replaced (Default = "Enter X here").
        if (clientSecrets.getDetails().getClientId().startsWith("Enter")
                || clientSecrets.getDetails().getClientSecret().startsWith("Enter ")) {
            System.out.println(
                    "Enter Client ID and Secret from https://console.developers.google.com/project/_/apiui/credential"
                            + "into youtube-cmdline-channelbulletin-sample/src/main/resources/client_secrets.json");
            System.exit(1);
        }

        // Set up authorization code flow.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                transport, JSON_FACTORY, clientSecrets, SCOPES)
                .build();

        // Authorize.
        return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
    }


    public static void addCommand(final ISlashCommand command) {
        commands.add(command);
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        for (final ISlashCommand command : commands) {
            if (command.getName().equals(event.getName())) {
                if (event.getSubcommandName() == null) {
                    command.execute(event);
                    return;
                } else {
                    final String subcommandName = event.getSubcommandName();
                    command.getSubCommand()
                            .stream()
                            .filter((sc) -> sc.getName().equals(subcommandName))
                            .findAny()
                            .ifPresent((sc) -> {
                                sc.execute(event);
                            });
                }
            }
        }
    }
}
