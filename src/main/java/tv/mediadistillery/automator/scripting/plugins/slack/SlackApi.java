package tv.mediadistillery.automator.scripting.plugins.slack;

import java.io.IOException;

import com.slack.api.Slack;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.request.chat.ChatPostMessageRequest;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;

/**
 * Example of an API where we wrap the Official Slack API in a simplified proxy
 * to reduce options in the scripting environment.
 * 
 * @author Geert
 *
 */
public class SlackApi {

	private final Slack slack = Slack.getInstance();
	private final MethodsClient methods; 
	
	public SlackApi() {
		String token = System.getenv("SLACK_TOKEN");
		methods = slack.methods(token);

	}
	
	
	/**
	 * Send a message
	 * 
	 * @param channelId The name or id of the channel. Use a channel name #channel or channel id C1234
	 * @param message The message
	 * @return ChatPostMessageResponse
	 * @throws SlackApiException 
	 * @throws IOException 
	 */
	public ChatPostMessageResponse send(String channelId, String message) throws IOException, SlackApiException {
		// Build a request object
		ChatPostMessageRequest request = ChatPostMessageRequest.builder()
		  .channel(channelId)
		  .text(message)
		  .build();
		 return methods.chatPostMessage(request);
	}
	
}
