package org.passeo.character.runnable;

import se.michaelthelin.spotify.model_objects.credentials.ClientCredentials;
import se.michaelthelin.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;

import java.util.concurrent.CompletableFuture;

import static org.passeo.character.Main.spotifyApi;


public class SpotifyTokenRefresh implements Runnable{
    @Override
    public void run() {
        final ClientCredentialsRequest request = spotifyApi.clientCredentials().build();

        final CompletableFuture<ClientCredentials> responseFuture = request.executeAsync();
        responseFuture
                .whenCompleteAsync((token, ex) -> {
                    spotifyApi.setAccessToken(token.getAccessToken());
                });
    }
}
