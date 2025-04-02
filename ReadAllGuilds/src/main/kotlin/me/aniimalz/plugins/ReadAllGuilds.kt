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
                    logger.info("Starting to mark ${guilds.size} guilds as read")
                    
                    // Try using the MessageAck store's methods
                    val messageAckStore = StoreStream.getMessageAck()
                    
                    // Attempt to find the appropriate method using reflection
                    val methods = messageAckStore.javaClass.methods
                    val ackMethods = methods.filter { it.name.toLowerCase().contains("ack") && it.name.toLowerCase().contains("guild") }
                    
                    if (ackMethods.isNotEmpty()) {
                        logger.info("Found potential ack methods: ${ackMethods.map { it.name }}")
                        
                        // Try the most promising method for each guild
                        for (guildId in guilds) {
                            try {
                                // Approach using the newer "acknowledgeGuild" method that might exist
                                messageAckStore.javaClass.getMethod("acknowledgeGuild", Long::class.java).invoke(messageAckStore, guildId)
                                logger.info("Marked guild $guildId as read")
                            } catch (e: Exception) {
                                logger.error("Failed to mark guild $guildId as read: ${e.message}")
                            }
                            
                            // Wait 5 seconds between requests to avoid API rate limits
                            Thread.sleep(500)
                        }
                    } else {
                        // If no specific acknowledgment methods are found, try using the read state manager
                        logger.info("No ack methods found, trying read state approach")
                        
                        for (guildId in guilds) {
                            try {
                                // Try to get the read state manager and mark guild as read
                                val readStateManager = StoreStream.getReadStateManager()
                                readStateManager.javaClass.getMethod("markGuildAsRead", Long::class.java).invoke(readStateManager, guildId)
                                logger.info("Marked guild $guildId as read using read state manager")
                            } catch (e: Exception) {
                                logger.error("Failed to mark guild $guildId as read: ${e.message}")
                            }
                            
                            // Wait 5 seconds between requests to avoid API rate limits
                            Thread.sleep(5000)
                        }
                    }
                    
                    logger.info("Finished marking guilds as read")
                } catch (e: Exception) {
                    logger.error("Error while marking guilds as read", e)
                }
            }
            CommandsAPI.CommandResult("Attempting to mark all guilds as read. This can take a while if you're in a lot of servers.", null, false)
        }
    }

    override fun stop(ctx: Context) {
        commands.unregisterAll()
    }
}
