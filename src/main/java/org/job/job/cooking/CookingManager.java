package org.job.job.cooking;

import org.bukkit.plugin.java.JavaPlugin;
import org.job.job.cooking.command.CookCommand;
import org.job.job.cooking.listener.GUIClickListener;
import org.job.job.cooking.listener.GUICloseListener;
import org.job.job.cooking.recipe.RecipeManager;
import org.job.job.cooking.util.SpecialCropUtil;
import org.job.job.skills.SkillManager;

import java.io.File;

public class CookingManager {

    private static CookingManager instance;
    private final JavaPlugin plugin;
    private final SkillManager skillManager;
    private RecipeManager recipeManager;
    private SpecialCropUtil cropUtil;
    private GUIClickListener guiClickListener;

    public CookingManager(JavaPlugin plugin, SkillManager skillManager) {
        this.plugin = plugin;
        this.skillManager = skillManager;
        instance = this;
    }

    public void enable() {
        // ✅ recipes.yml 파일 없으면 자동 복사
        saveDefaultConfigFile("recipes.yml");

        this.recipeManager = new RecipeManager();
        this.recipeManager.loadRecipes(plugin.getDataFolder());

        this.cropUtil = new SpecialCropUtil(plugin);

        plugin.getCommand("cook").setExecutor(new CookCommand());
        this.guiClickListener = new GUIClickListener(skillManager);
        plugin.getServer().getPluginManager().registerEvents(this.guiClickListener, plugin);
        plugin.getServer().getPluginManager().registerEvents(new GUICloseListener(), plugin);

        plugin.getLogger().info("🍳 요리 기능 활성화 완료!");
    }

    private void saveDefaultConfigFile(String fileName) {
        File file = new File(plugin.getDataFolder(), fileName);
        if (!file.exists()) {
            plugin.saveResource(fileName, false);
            plugin.getLogger().info("✅ " + fileName + " 파일을 생성했습니다.");
        }
    }

    public static CookingManager getInstance() {
        return instance;
    }

    public RecipeManager getRecipeManager() {
        return recipeManager;
    }

    public SpecialCropUtil getCropUtil() {
        return cropUtil;
    }

    public JavaPlugin getPlugin() {
        return plugin;
    }

    public SkillManager getSkillManager() {
        return skillManager;
    }

    public GUIClickListener getGuiClickListener() {
        return guiClickListener;
    }
}
