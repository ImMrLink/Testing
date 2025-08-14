package com.simplecore;

import com.simplecore.commands.*;
import com.simplecore.economy.EconomyService;
import com.simplecore.listeners.ChatListener;
import com.simplecore.listeners.PlayerListener;
import com.simplecore.util.DataStore;
import org.bukkit.plugin.java.JavaPlugin;

public class SimpleCorePlugin extends JavaPlugin implements org.bukkit.event.Listener {
    private static SimpleCorePlugin instance;
    private DataStore dataStore;
    private EconomyService economy;
    private com.simplecore.util.GroupManager groupManager;
    private com.simplecore.util.StaffModeManager staffModeManager;
    private com.simplecore.util.PunishmentManager punishmentManager;
    private com.simplecore.util.PunishmentGui punishmentGui;
    private com.simplecore.util.AfkZoneManager afkZoneManager;
    private com.simplecore.util.Announcer announcer;
    private com.simplecore.util.ScoreboardManager scoreboardManager;
    private com.simplecore.util.DailyRewardManager dailyRewardManager;
    private com.simplecore.util.VoteManager voteManager;
    private com.simplecore.util.MultiplierManager multiplierManager;
    private com.simplecore.util.MissionManager missionManager;
    private com.simplecore.util.MissionProgressService missionProgress;
    private com.simplecore.util.HologramManager hologramManager;
    private com.simplecore.util.CrateManager crateManager;
    private com.simplecore.util.LogWriter logWriter;
    private final java.util.Map<java.util.UUID, java.util.UUID> punishTargets = new java.util.concurrent.ConcurrentHashMap<>();

    private com.simplecore.util.AfkManager afkManager;
    private com.simplecore.util.KitManager kitManager;


    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        this.dataStore = new DataStore(this);
        this.dataStore.loadAll();
        this.economy = new EconomyService(this);
        this.afkManager = new com.simplecore.util.AfkManager(this);
        this.kitManager = new com.simplecore.util.KitManager(this);
        this.groupManager = new com.simplecore.util.GroupManager(this);
        this.staffModeManager = new com.simplecore.util.StaffModeManager(this);
        this.punishmentManager = new com.simplecore.util.PunishmentManager(this);
        this.punishmentGui = new com.simplecore.util.PunishmentGui(this);
        try {
            Class.forName("me.clip.placeholderapi.PlaceholderAPI");
            new com.simplecore.util.SimpleCoreExpansion(this).register();
            getLogger().info("PlaceholderAPI detected and hooked.");
        } catch (ClassNotFoundException ignored) {}
        this.afkZoneManager = new com.simplecore.util.AfkZoneManager(this);
        this.announcer = new com.simplecore.util.Announcer(this);
        this.announcer.start();
        this.scoreboardManager = new com.simplecore.util.ScoreboardManager(this);
        this.scoreboardManager.start();
        this.dailyRewardManager = new com.simplecore.util.DailyRewardManager(this);
        this.dailyRewardManager.start();
        this.voteManager = new com.simplecore.util.VoteManager(this);
        this.multiplierManager = new com.simplecore.util.MultiplierManager(this);
        this.missionManager = new com.simplecore.util.MissionManager(this);
        this.missionProgress = new com.simplecore.util.MissionProgressService(this);
        this.missionProgress.startTickers();
        this.hologramManager = new com.simplecore.util.HologramManager(this);
        this.hologramManager.refresh();
        this.crateManager = new com.simplecore.util.CrateManager(this);
        this.logWriter = new com.simplecore.util.LogWriter(this);

        // Register commands
        getCommand("spawn").setExecutor(new SpawnCommand(this));
        getCommand("setspawn").setExecutor(new SetSpawnCommand(this));
        getCommand("home").setExecutor(new HomeCommand(this));
        getCommand("sethome").setExecutor(new SetHomeCommand(this));
        getCommand("delhome").setExecutor(new DelHomeCommand(this));
        getCommand("homes").setExecutor(new HomesCommand(this));
        getCommand("warp").setExecutor(new WarpCommand(this));
        getCommand("setwarp").setExecutor(new SetWarpCommand(this));
        getCommand("delwarp").setExecutor(new DelWarpCommand(this));
        getCommand("warps").setExecutor(new WarpsCommand(this));
        getCommand("tpa").setExecutor(new TpaCommand(this));
        getCommand("tpaccept").setExecutor(new TpAcceptCommand(this));
        getCommand("tpdeny").setExecutor(new TpDenyCommand(this));
        getCommand("back").setExecutor(new BackCommand(this));
        getCommand("msg").setExecutor(new MsgCommand(this));
        getCommand("reply").setExecutor(new ReplyCommand(this));
        getCommand("balance").setExecutor(new BalanceCommand(this));
        getCommand("pay").setExecutor(new PayCommand(this));
        getCommand("heal").setExecutor(new HealCommand(this));
        getCommand("feed").setExecutor(new FeedCommand(this));
        getCommand("fly").setExecutor(new FlyCommand(this));
        getCommand("god").setExecutor(new GodCommand(this));
        getCommand("staffmode").setExecutor(new com.simplecore.commands.StaffModeCommand(this));
        getCommand("scgroup").setExecutor(new com.simplecore.commands.ScGroupCommand(this));
        getCommand("setjail").setExecutor(new com.simplecore.commands.SetJailCommand(this));
        getCommand("unjail").setExecutor(new com.simplecore.commands.UnJailCommand(this));
        getCommand("setafkzone").setExecutor(new com.simplecore.commands.SetAfkZoneCommand(this));
        getCommand("delafkzone").setExecutor(new com.simplecore.commands.DelAfkZoneCommand(this));
        getCommand("afkzones").setExecutor(new com.simplecore.commands.AfkZonesCommand(this));
        getCommand("daily").setExecutor(new com.simplecore.commands.DailyCommand(this));
        getCommand("togglesb").setExecutor(new com.simplecore.commands.ToggleScoreboardCommand(this));
        getCommand("voteadd").setExecutor(new com.simplecore.commands.VoteAddCommand(this));
        getCommand("voteparty").setExecutor(new com.simplecore.commands.VotePartyCommand(this));
        getCommand("multiplier").setExecutor(new com.simplecore.commands.MultiplierCommand(this));
        getCommand("missions").setExecutor(new com.simplecore.commands.MissionsCommand(this));
        getCommand("afkzonegui").setExecutor(new com.simplecore.commands.AfkZoneGuiCommand(this));
        getCommand("crate").setExecutor(new com.simplecore.commands.CrateCommand(this));
        getCommand("stafftools").setExecutor(new com.simplecore.commands.StaffToolsCommand(this));

        getCommand("afk").setExecutor(new com.simplecore.commands.AfkCommand(this));
getCommand("kit").setExecutor(new com.simplecore.commands.KitCommand(this));
getCommand("kits").setExecutor(new com.simplecore.commands.KitsCommand(this));

getCommand("afktop").setExecutor(new com.simplecore.commands.AfkTopCommand(this));

// Listeners
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new GuiListener(this), this);
        getServer().getPluginManager().registerEvents(new AfkListener(this), this);
        getServer().getPluginManager().registerEvents(new ChatListener(this), this);
        getServer().getPluginManager().registerEvents(new ChatFormatListener(this), this);
        getServer().getPluginManager().registerEvents(new StaffModeListener(this), this);
        getServer().getPluginManager().registerEvents(new PunishmentGuiListener(this), this);
        getServer().getPluginManager().registerEvents(new StaffToolsGuiListener(this), this);
        getServer().getPluginManager().registerEvents(new EnforcementListener(this), this);
        try {
            Class.forName("com.vexsoftware.votifier.model.VotifierEvent");
            getServer().getPluginManager().registerEvents(new VoteListener(this), this);
            getLogger().info("Votifier detected and hooked.");
        } catch (ClassNotFoundException ignored) {}
        getServer().getPluginManager().registerEvents(new XpListener(this), this);
        getServer().getPluginManager().registerEvents(new MissionBlocksListener(this), this);
        getServer().getPluginManager().registerEvents(new MissionFishListener(this), this);
        getServer().getPluginManager().registerEvents(new MissionTravelListener(this), this);
        getServer().getPluginManager().registerEvents(new MissionsGuiListener(this), this);
        getServer().getPluginManager().registerEvents(new CrateKeyListener(this), this);

        getLogger().info("SimpleCore enabled.");
    }

    @Override
    public void onDisable() {
        dataStore.saveAll();
        getLogger().info("SimpleCore disabled.");
    }

    public static SimpleCorePlugin getInstance() { return instance; }
    public DataStore getDataStore() { return dataStore; }
    public EconomyService getEconomy() { return economy; }

    public com.simplecore.util.AfkManager getAfkManager() { return afkManager; }
    public com.simplecore.util.KitManager getKitManager() { return kitManager; }

    public long getDataStoreTime(String path, long def) { return dataStore.getTime(path, def); }
    public void setDataStoreTime(String path, long value) { dataStore.setTime(path, value); }


    public String getDataStoreString(String path, String def) { 
        var val = this.dataStore.usersCfg.getString(path); 
        return val == null ? def : val; 
    }
    public void setDataStoreString(String path, String value) { this.dataStore.usersCfg.set(path, value); this.dataStore.saveAll(); }
    public double getDataStoreDouble(String path, double def) { return this.dataStore.getDouble(path, def); }
    public void setDataStoreDouble(String path, double value) { this.dataStore.setDouble(path, value); }

    public com.simplecore.util.GroupManager getGroupManager() { return groupManager; }
    public com.simplecore.util.StaffModeManager getStaffModeManager() { return staffModeManager; }
    public com.simplecore.util.PunishmentManager getPunishmentManager() { return punishmentManager; }
    public com.simplecore.util.PunishmentGui getPunishmentGui() { return punishmentGui; }
    public void setPunishTarget(java.util.UUID staff, java.util.UUID target) { punishTargets.put(staff, target); }
    public java.util.UUID getPunishTarget(java.util.UUID staff) { return punishTargets.get(staff); }


    public com.simplecore.util.LogWriter getLogWriter() { return logWriter; }


    public com.simplecore.util.AfkZoneManager getAfkZoneManager() { return afkZoneManager; }
    public com.simplecore.util.Announcer getAnnouncer() { return announcer; }


    public com.simplecore.util.ScoreboardManager getScoreboardManager() { return scoreboardManager; }


    public com.simplecore.util.DailyRewardManager getDailyRewardManager() { return dailyRewardManager; }


    public com.simplecore.util.VoteManager getVoteManager() { return voteManager; }
    public com.simplecore.util.MultiplierManager getMultiplierManager() { return multiplierManager; }


    public com.simplecore.util.CrateManager getCrateManager() { return crateManager; }


    public com.simplecore.util.HologramManager getHologramManager() { return hologramManager; }


    public com.simplecore.util.MissionManager getMissionManager() { return missionManager; }
    public com.simplecore.util.MissionProgressService getMissionProgress() { return missionProgress; }

}
