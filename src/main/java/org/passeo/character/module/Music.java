package org.passeo.character.module;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.common.collect.Lists;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.apache.hc.core5.http.ParseException;
import org.passeo.character.module.lavaplayer.GuildMusicManager;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.*;
import se.michaelthelin.spotify.requests.data.playlists.GetPlaylistRequest;
import se.michaelthelin.spotify.requests.data.tracks.GetTrackRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.passeo.character.Main.spotifyApi;
import static org.passeo.character.Main.youTubeApi;

public class Music {

    private final Map<Long, GuildMusicManager> musicManagers;
    private final AudioPlayerManager playerManager;

    public Music(final AudioPlayerManager playerManager) {
        this.playerManager = playerManager;
        AudioSourceManagers.registerRemoteSources(playerManager);
        AudioSourceManagers.registerLocalSource(playerManager);
        musicManagers = new HashMap<>();
    }

    public synchronized GuildMusicManager getGuildAudioPlayer(Guild guild) {
        long guildId = Long.parseLong(guild.getId());
        GuildMusicManager musicManager = musicManagers.get(guildId);

        if (musicManager == null) {
            musicManager = new GuildMusicManager(playerManager);
            musicManagers.put(guildId, musicManager);
        }
        guild.getAudioManager().setSendingHandler(musicManager.getSendHandler());

        return musicManager;
    }

    public void loadMusic(final TextChannel channel, String trackUrl) throws IOException {
        if (trackUrl.contains("spotify.com")) {
            try {
                YouTube.Search.List request = youTubeApi.search()
                        .list(Lists.newArrayList("snippet"));
                List<String> spotifyName = convert(trackUrl);
                for (final String spotify : spotifyName) {
                    SearchListResponse response = request.setMaxResults(1L)
                            .setQ(spotify)
                            .setType(Lists.newArrayList("video"))
                            .execute();
                    loadAndPlayMusic(channel,
                            "https://www.youtube.com/watch?v=" +
                                    response.getItems().get(0).getId().getVideoId());
                }
            } catch (SpotifyWebApiException | IOException | ParseException e) {
                throw new RuntimeException(e);
            }
        } else {
            loadAndPlayMusic(channel, trackUrl);
        }
    }

    public void loadAndPlayMusic(final TextChannel channel, String trackUrl) throws IOException {
        GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());


        playerManager.loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                channel.sendMessage("Adding to queue " + track.getInfo().title).queue();
                play(musicManager, track);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                AudioTrack firstTrack = playlist.getSelectedTrack();

                if (firstTrack == null) {
                    firstTrack = playlist.getTracks().get(0);
                }

                channel.sendMessage("Adding to queue " + firstTrack.getInfo().title + " (first track of playlist " + playlist.getName() + ")").queue();
                play(musicManager, firstTrack);
            }

            @Override
            public void noMatches() {
                channel.sendMessage("Nothing found by " + trackUrl).queue();
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                channel.sendMessage("Could not play: " + exception.getMessage()).queue();
            }
        });

    }

    public ArrayList<String> convert(String link) throws ParseException, SpotifyWebApiException, IOException {
        String[] firstSplit = link.split("/");
        String[] secondSplit;
        String id;
        String type;

        if (firstSplit.length > 5) {
            secondSplit = firstSplit[6].split("\\?");
            type = firstSplit[5];
        } else {
            secondSplit = firstSplit[4].split("\\?");
            type = firstSplit[3];
        }
        id = secondSplit[0];
        ArrayList<String> listOfTracks = new ArrayList<>();

        if (type.contentEquals("track")) {
            listOfTracks.add(getArtistAndName(id));
            return listOfTracks;
        }

        if (type.contentEquals("playlist")) {
            GetPlaylistRequest playlistRequest = spotifyApi.getPlaylist(id).build();
            Playlist playlist = playlistRequest.execute();

            Paging<PlaylistTrack> playlistPaging = playlist.getTracks();
            PlaylistTrack[] playlistTracks = playlistPaging.getItems();

            for (PlaylistTrack i : playlistTracks) {
                Track track = (Track) i.getTrack();
                String trackID = track.getId();
                try {
                    listOfTracks.add(getArtistAndName(trackID));
                } catch (SpotifyWebApiException | IOException | ParseException e) {
                    throw new RuntimeException(e);
                }
            }

            return listOfTracks;
        }

        return null;
    }

    private String getArtistAndName(String trackID) throws SpotifyWebApiException, IOException, org.apache.hc.core5.http.ParseException {
        StringBuilder artistNameAndTrackName;
        GetTrackRequest trackRequest = spotifyApi.getTrack(trackID).build();

        Track track = trackRequest.execute();
        artistNameAndTrackName = new StringBuilder(track.getName() + " - ");

        ArtistSimplified[] artists = track.getArtists();
        for (ArtistSimplified i : artists) {
            artistNameAndTrackName.append(i.getName()).append(" ");
        }

        return artistNameAndTrackName.toString();
    }

    private void play(GuildMusicManager musicManager, AudioTrack track) {
        musicManager.scheduler.queue(track);
    }

}
