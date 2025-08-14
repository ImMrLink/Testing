package com.simplecore.listeners;

import com.simplecore.SimpleCorePlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class VoteListener implements Listener {
    private final SimpleCorePlugin plugin;
    public VoteListener(SimpleCorePlugin plugin) { this.plugin = plugin; }

    @EventHandler
    public void onVotifier(com.vexsoftware.votifier.model.VotifierEvent e) {
        var vote = e.getVote();
        plugin.getVoteManager().onVote(vote.getUsername(), vote.getServiceName());
    }
}
