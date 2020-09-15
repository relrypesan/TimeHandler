package me.reratos.timehandler.handler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.MemorySection;

import me.reratos.timehandler.TimeHandler;
import me.reratos.timehandler.WorldConfig;
import me.reratos.timehandler.core.TimeManager;
import me.reratos.timehandler.core.WeatherManager;
import me.reratos.timehandler.core.WorldManager;
import me.reratos.timehandler.enums.MoonPhasesEnum;
import me.reratos.timehandler.handler.commands.HelpCommand;
import me.reratos.timehandler.handler.commands.SetCommand;
import me.reratos.timehandler.utils.UpdateChecker;

public class CommandHandler {
	
	public static boolean info(CommandSender sender, String worldName) {
		World world = Bukkit.getWorld(worldName);
        if(world == null) {
        	return false;
        }

        MemorySection worldConfig = (MemorySection) TimeHandler.config.get("configWorld." + worldName);
        
        if(worldConfig == null) {
        	TimeHandler.sendMessage(sender, "Este mundo ainda N�O foi adicionado no manipulador.");
        } else {
        	TimeHandler.sendMessage(sender, ChatColor.YELLOW + "Mundo: " + ChatColor.GREEN + worldName);
        	TimeHandler.sendMessage(sender, "Tempo atual: " + world.getTime() + ", FullTime: " + world.getFullTime());
        	long days = world.getFullTime() / 24000;
        	int phase = (int) (days % 8);
        	TimeHandler.sendMessage(sender, "Fase da lua: " + ChatColor.BLUE + MoonPhasesEnum.values()[phase].name());
        	
        	LinkedHashMap<String, Object> list = (LinkedHashMap<String, Object>) worldConfig.getValues(true);

        	String climaAtual = WeatherManager.getClimaAtual(world);
        	TimeHandler.sendMessage(sender, "Clima atual: " + climaAtual + ", Clima mudar� em: " + world.getWeatherDuration());
        	
        	// Lista as informa��es de ambiente do mundo
        	WorldConfig.info(sender, list);
        }
        
		return true;
	}
	
	public static boolean help(CommandSender sender) {
		HelpCommand.helpAll(sender);
		return true;
	}
	
	public static boolean list(CommandSender sender) {
		MemorySection worlds = (MemorySection) TimeHandler.config.get("configWorld");

		
		if(worlds == null) {
			TimeHandler.sendMessage(sender, "Nenhum mundo foi configurado no plugin TimeHandler.");
		} else {
			LinkedHashMap<String, Object> map = (LinkedHashMap<String, Object>) worlds.getValues(false);
			List<String> lista = new ArrayList<>(map.keySet());
			
			Collections.sort(lista);
			
			TimeHandler.sendMessage(sender, "Lista de mundos configurados.");
			for(String w : lista) {
				TimeHandler.sendMessage(sender, ChatColor.YELLOW + " - " + w);
			}
		}
		
		return true;
	}

	public static boolean remove(CommandSender sender, String worldName) {
		Object obj = TimeHandler.config.get("configWorld." + worldName);
		
		if(obj != null) {
			TimeHandler.config.set("configWorld." + worldName, null);
			TimeHandler.sendMessage(sender, "O mundo " + ChatColor.GREEN + worldName + 
					" foi removido das configura��es do TimeHandler");
			TimeHandler.plugin.saveConfig();
		} else {
			TimeHandler.sendMessage(sender, "Este mundo n�o existe nas configura��es do TimeHandler");
		}
		
		return true;
	}

	public static boolean set(CommandSender sender, String worldName) {
		
		if(!TimeHandler.existWorld(worldName)) {
			TimeHandler.sendMessage(sender, ChatColor.RED + "Este mundo n�o existe: " + ChatColor.UNDERLINE + worldName);
			return false;
		} else if(TimeHandler.config.get("configWorld." + worldName) != null) {
			TimeHandler.sendMessage(sender, "Este mundo j� esta configurado: " + ChatColor.RED + ChatColor.UNDERLINE + worldName);
			return true;
		}

		return SetCommand.commandSetDefault(sender, worldName);
	}

	public static boolean set(CommandSender sender, String worldName, String property, String value) {
		WorldManager wm = TimeManager.getRunablesWorld().get(worldName);
		
		if(!TimeHandler.existWorld(worldName)) {
			TimeHandler.sendMessage(sender, ChatColor.RED + "Este mundo n�o existe: " + ChatColor.UNDERLINE + worldName);
			return false;
		} else if(wm == null) {
			TimeHandler.sendMessage(sender, "Este mundo ainda n�o foi configurado no plugin, utilize " + 
					ChatColor.GREEN + "/th set " + worldName + ChatColor.RESET + " para configura-l�.");
			return false;
		}
		
		return SetCommand.commandSetBase(sender, wm, property, value);
	}
	
	public static boolean update(String resourceId) {
		return update(TimeHandler.plugin.getServer().getConsoleSender(), resourceId);
	}

	public static boolean update(CommandSender sender, String resourceId) {
        new UpdateChecker(TimeHandler.plugin, resourceId).getVersionConsumer(version -> {
//        	TimeHandler.sendMessage(sender, "version: " + version + ", server: " + TimeHandler.plugin.getDescription().getVersion());
        	if (TimeHandler.plugin.getDescription().getVersion().equalsIgnoreCase(version)) {
    			TimeHandler.sendMessage(sender, "O plugin est� atualizado. " + ChatColor.LIGHT_PURPLE + version);
            } else {
    			TimeHandler.sendMessage(sender, "Existe uma nova vers�o disponivel: " + ChatColor.GREEN + version);
            }
        });
		return true;
	}

	public static List<String> getWorldsTimeHandler() {
		List<String> list = new ArrayList<String>();
		try {
			list.addAll(((LinkedHashMap<String, Object>)((MemorySection) TimeHandler.config.get("configWorld")).getValues(false)).keySet());			
			Collections.sort(list);
//			for(String s : list) {
//				s = ChatColor.LIGHT_PURPLE + s + ChatColor.RESET;
//			}
		} catch (Exception e) {
			// nothing
		}
		return list;
	}
	
}