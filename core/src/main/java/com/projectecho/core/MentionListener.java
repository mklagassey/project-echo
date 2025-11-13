package com.projectecho.core;

import java.util.List;

/**
 * An interface for components that wish to receive updates from a mention-finding service.
 * This decouples the background services from the UI.
 */
public interface MentionListener {

    /**
     * Called when new mentions have been found.
     * @param newMentions The list of new mentions.
     */
    void onMentionsFound(List<Mention> newMentions);

    /**
     * Called when there is a status update from the service.
     * @param status The new status message.
     */
    void onStatusUpdate(String status);
}