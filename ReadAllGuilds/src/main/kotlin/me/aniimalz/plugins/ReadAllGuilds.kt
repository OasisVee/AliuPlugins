package me.aniimalz.plugins

import android.content.Context
import com.aliucord.Utils
import com.aliucord.annotations.AliucordPlugin
import com.aliucord.api.CommandsAPI
import com.aliucord.entities.Plugin
import com.discord.stores.StoreStream
import kotlin.random.Random

@AliucordPlugin
class ReadAllGuilds : Plugin() {
    override fun start(ctx: Context) {
        commands.registerCommand("readallguilds", "Mark all of your guilds as read. Will wait between 1-5 seconds between actions to not spam api") {
            Utils.threadPool.execute {
                val guilds = StoreStream.getGuilds().guilds.keys.toList()
                val totalGuilds = guilds.size
                var markedCount = 0

                for (guildId in guilds) {
                    try {
                        // Mark the current guild as read
                        StoreStream.getMessageAck().ackGuild(ctx, guildId, null)
                        markedCount++
                        
                        // Only sleep if this isn't the last guild
                        if (markedCount < totalGuilds) {
                            // Random delay between 1000ms (1s) and 5000ms (5s)
                            val randomDelay = Random.nextInt(1000, 5001)
                            Thread.sleep(randomDelay.toLong())
                        }
                    } catch (e: Exception) {
                        // Handle any exceptions that might occur
                        Utils.showToast("Error marking guild $guildId as read: ${e.message}")
                    }
                }
                
                // Show a toast when all guilds have been processed
                Utils.showToast("Marked $markedCount out of $totalGuilds guilds as read")
            }
            
            CommandsAPI.CommandResult("Marking all guilds as read. This can take a while if you're in a lot of servers", null, false)
        }
    }

    override fun stop(ctx: Context) {
        commands.unregisterAll()
    }
}
