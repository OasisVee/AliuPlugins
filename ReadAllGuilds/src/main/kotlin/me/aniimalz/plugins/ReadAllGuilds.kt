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
        commands.registerCommand("readallguilds", "Mark all of your guilds as read. Will wait 0.5 seconds between actions to not spam api") {
            Utils.threadPool.execute {
                val guildIds = StoreStream.getGuilds().guilds.keys.toList()
                var markedCount = 0
                
                guildIds.forEach { guildId ->
                    // Mark guild as read using the correct method
                    StoreStream.getMessageAck().ackMessages(ctx, guildId, null)
                    markedCount++
                    
                    // Sleep between requests to avoid API rate limits (reduced to 500ms)
                    Thread.sleep(500)
                }
            }
            CommandsAPI.CommandResult("Started marking ${StoreStream.getGuilds().guilds.size} guilds as read. This can take a while if you're in a lot of servers", null, false)
        }
    }

    override fun stop(ctx: Context) {
        commands.unregisterAll()
    }
}
