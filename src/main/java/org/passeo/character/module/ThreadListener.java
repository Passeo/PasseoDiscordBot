package org.passeo.character.module;

import com.theokanning.openai.completion.CompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.events.channel.ChannelCreateEvent;
import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ThreadListener extends ListenerAdapter {

    private final JDA api;
    private static final String OPENAI_KEY = "KEY HERE";
    private final List<ThreadChannel> channels;
    private String CONVO_STARTER;
    private String role = "boyfriend";
    private List<ChatMessage> messages = new ArrayList<>();

    public ThreadListener(final JDA api) {
        this.api = api;
        channels = new ArrayList<>();
        api.addEventListener(this);
        this.CONVO_STARTER = "The following is a conversation with an AI who represents a " + this.role.toLowerCase() + " NPC character. " +
                "The AI should limit his knowledge of the love and being a " + this.role.toLowerCase() + " and try not to stray even if asked about something else. " +
                "Play this " + this.role.toLowerCase() + "role the best you can.\n\nHuman: Hey!\n\nAI:";
    }

    @Override
    public void onChannelCreate(final ChannelCreateEvent event) {
        if (!(event.getChannelType().equals(ChannelType.GUILD_PUBLIC_THREAD) || event.getChannelType().equals(ChannelType.GUILD_PRIVATE_THREAD))
                || ((ThreadChannel) event.getChannel()).getSelfThreadMember() != null) {
            return;
        }
        final ThreadChannel channel = (ThreadChannel) event.getChannel();
        channel.join().complete();
        final ChatMessage message = new ChatMessage("system", CONVO_STARTER);
        messages.add(message);
        channels.add(channel);
    }

    @Override
    public void onChannelDelete(ChannelDeleteEvent event) {
        if (!(event.getChannelType().equals(ChannelType.GUILD_PUBLIC_THREAD) || event.getChannelType().equals(ChannelType.GUILD_PRIVATE_THREAD))
                || ((ThreadChannel) event.getChannel()).getSelfThreadMember() != null) {
            return;
        }
        final ThreadChannel channel = (ThreadChannel) event.getChannel();
        channels.remove(channel);
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        if ( !(event.getChannelType().equals(ChannelType.GUILD_PUBLIC_THREAD)
                || event.getChannelType().equals(ChannelType.GUILD_PRIVATE_THREAD))) {
            return;
        }

        CompletableFuture.runAsync(() -> {
            ThreadChannel channel = null;
            for (ThreadChannel c: channels) {
                if(c.equals(event.getChannel())){
                    channel = c;
                }
            }
            if(channel == null){
                return;
            }
            final OpenAiService service = new OpenAiService(OPENAI_KEY);
            final ChatMessage message = new ChatMessage("user", event.getMessage().getContentRaw());
            messages.add(message);
            ChatCompletionRequest request = ChatCompletionRequest.builder()
                    .model("gpt-3.5-turbo")
                    .messages(messages)
                    .temperature(1.0)
                    .maxTokens(256)
                    .topP(1.0)
                    .frequencyPenalty(1.0)
                    .presencePenalty(1.0)
                    .build();
            var choices = service.createChatCompletion(request).getChoices();
            var response = choices.get(0).getMessage();
            messages.add(response);
            channel.sendMessage(response.getContent()).queue();
        });
    }
}
