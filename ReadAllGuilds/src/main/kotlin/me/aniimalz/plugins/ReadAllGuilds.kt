package me.aniimalz.plugins

import android.content.Context
import com.aliucord.Utils
import com.aliucord.annotations.AliucordPlugin
import com.aliucord.api.CommandsAPI
import com.aliucord.entities.Plugin
import com.discord.stores.StoreStream

@AliucordPlugin
class ReadAllGuilds : Plugin() {
    override fun start(ctx: Context) {
        commands.registerCommand("readallguilds", "Mark all of your guilds as read. Will wait 5 seconds between actions to not spam api") {
            Utils.threadPool.execute {
                try {
                    // Get the guilds collection
                    val guilds = StoreStream.getGuilds().guilds.keys
                    
                    // Log the number of guilds being processed
                    logger.info("Starting to mark ${guilds.size} guilds as read")
                    
                    // Try different approach based on available methods
                    for (guildId in guilds) {
                        try {
                            // First approach: Try to call ackGuild with just the guild ID
                            StoreStream.getMessageAck().ackGuild(guildId)
                        } catch (e: NoSuchMethodError) {
                            try {
                                // Second approach: Try the ackGuild method without the callback
                                StoreStream.getMessageAck().ackGuild(ctx, guildId)
                            } catch (e: NoSuchMethodError) {
                                logger.error("Could not find appropriate ackGuild method", e)
                            }
                        }
                        
                        // Wait 5 seconds between requests to avoid API rate limits
                        Thread.sleep(5000)
                    }
                    
                    logger.info("Finished marking guilds as read")
                } catch (e: Exception) {
                    logger.error("Error while marking guilds as read", e)
                }
            }
            CommandsAPI.CommandResult("Marking all guilds as read. This can take a while if you're in a lot of servers", null, false)
        }
    }

    override fun stop(ctx: Context) {
        commands.unregisterAll()
    }
}
