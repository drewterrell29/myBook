package com.reader.myBook;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.slu.Slot;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.LaunchRequest;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SessionEndedRequest;
import com.amazon.speech.speechlet.SessionStartedRequest;
import com.amazon.speech.speechlet.Speechlet;
import com.amazon.speech.speechlet.SpeechletException;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.Reprompt;
import com.amazon.speech.ui.SimpleCard;
import com.amazon.speech.ui.SsmlOutputSpeech;


public class BibleReaderSpeechlet implements Speechlet
{
	private static final Logger log = LoggerFactory.getLogger(BibleReaderSpeechlet.class);
	
	  private static final String URL_PREFIX =
	            "https://api.biblia.com/v1/bible/content/LEB.html?";
	  
	  private static final String KEY = "f036f02371f77d5ef5fb555d14c81ec7";
	
	@Override
	public SpeechletResponse onIntent(IntentRequest request, Session session) throws SpeechletException {
		log.info("onIntent requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());
		
		Intent intent = request.getIntent();
		String intentName = (intent != null) ? intent.getName() : null;
		
		if("BibleReaderIntent".equals(intentName)) 
		{
			Bible bible =  getVerseLocation(intent);
			String text = getJsonEventsFromBible(bible);
			
			StringBuilder speechOutputBuilder = new StringBuilder();
			speechOutputBuilder.append("<p>");
			speechOutputBuilder.append(text);
			speechOutputBuilder.append("</p>");
			
			String speechOutput = speechOutputBuilder.toString();
			String repromptText = "This is the Bible Reader on Echo";
			
			SpeechletResponse response = newAskResponse("<speak>" + speechOutput + "</speak>", "<speak>" + repromptText + "</speak>");
			
			return response;
		}
		else
		{
			throw new SpeechletException("invalid Intent");
		}
		
		
		
	}

	@Override
	public SpeechletResponse onLaunch(LaunchRequest request, Session session) throws SpeechletException {
		log.info("onLaunch requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());
        return getOpeningResponse();
	}

	@Override
	public void onSessionEnded(SessionEndedRequest request, Session session) throws SpeechletException {
		 log.info("onSessionEnded requestId={}, sessionId={}", request.getRequestId(),
	                session.getSessionId());
	        // any cleanup logic goes here
		
	}

	@Override
	public void onSessionStarted(SessionStartedRequest request, Session session) throws SpeechletException {
		log.info("onSessionStarted requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());
        // any initialization logic goes here
	}
	
	private SpeechletResponse getOpeningResponse()
	{
		String speechText = "Welcome to my Bible Reader, you can say read Bible";
		
		// Create the Simple card content.
        SimpleCard card = new SimpleCard();
        card.setTitle("HelloWorld");
        card.setContent(speechText);

        // Create the plain text output.
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(speechText);

        // Create reprompt
        Reprompt reprompt = new Reprompt();
        reprompt.setOutputSpeech(speech);

        return SpeechletResponse.newAskResponse(speech, reprompt, card);
	}
	
	
	private Bible getVerseLocation(Intent intent)
	{
		Bible bible = new Bible();
		
		Slot bookSlot = intent.getSlot("book");
		Slot chapterSlot = intent.getSlot("chapter");
		Slot verseSlot = intent.getSlot("verse");
		
		System.out.println("Intent: " + bookSlot + chapterSlot + verseSlot);
		
		bible.book = bookSlot.getValue();
		bible.chapter = chapterSlot.getValue();
		bible.verse = verseSlot.getValue();
		
		return bible;
	}
	
	private String getJsonEventsFromBible(Bible bible) {
        InputStreamReader inputStream = null;
        BufferedReader bufferedReader = null;
        String text = "";
        try {
            String line;
            URL url = new URL(URL_PREFIX +"passage=" +bible.book + bible.chapter +"." + bible.verse + "&key=" + KEY);
            inputStream = new InputStreamReader(url.openStream(), Charset.forName("US-ASCII"));
            bufferedReader = new BufferedReader(inputStream);
            StringBuilder builder = new StringBuilder();
            while ((line = bufferedReader.readLine()) != null) {
                builder.append(line);
            }
            text = builder.toString();
        } catch (IOException e) {
            // reset text variable to a blank string
            text = "";
        } finally {
            IOUtils.closeQuietly(inputStream);
            IOUtils.closeQuietly(bufferedReader);
        }
        return text;
    }
	
	private SpeechletResponse newAskResponse(String stringOutput, String repromptText) {
        SsmlOutputSpeech outputSpeech = new SsmlOutputSpeech();
        outputSpeech.setSsml(stringOutput);
        SsmlOutputSpeech repromptOutputSpeech = new SsmlOutputSpeech();
        repromptOutputSpeech.setSsml(repromptText);
        Reprompt reprompt = new Reprompt();
        reprompt.setOutputSpeech(repromptOutputSpeech);
        return SpeechletResponse.newAskResponse(outputSpeech, reprompt);
    }
	
	
}
