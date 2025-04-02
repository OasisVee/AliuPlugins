package me.aniimalz.plugins

import android.content.Context
import com.aliucord.Utils
import com.aliucord.annotations.AliucordPlugin
import com.aliucord.api.CommandsAPI
import com.aliucord.entities.Plugin
import com.discord.stores.StoreStream
import com.discord.api.message.reaction.MessageReactionUpdate
import com.discord.api.channel.Channel
import com.discord.models.domain.ModelReadState
import com.discord.utilities.rest.RestAPI

@AliucordPlugin
class ReadAllGuilds : Plugin() {
    override fun start(ctx: Context) {
        commands.registerCommand("readallguilds", "Mark all of your guilds as read. Will wait 0.5 seconds between actions to not spam api") {
            Utils.threadPool.execute {
                try {
                    // Get list of all guild IDs
                    val guildIds = StoreStream.getGuilds().guilds.keys.toList()
                    val channelsToAck = mutableListOf<Map<String, Any>>()
                    
                    // Collect all unread channels from all guilds
                    for (guildId in guildIds) {
                        val readStateStore = StoreStream.getReadState()
                        val guildChannels = StoreStream.getChannels().getGuildChannels(guildId)
                        
                        // Get all channels for this guild
                        guildChannels?.forEach { channelId, _ ->
                            // Check if channel has unread messages
                            if (readStateStore.hasUnread(channelId)) {
                                // Add to list of channels to acknowledge
                                val lastMessageId = readStateStore.lastMessageId(channelId)
                                if (lastMessageId != null) {
                                    channelsToAck.add(mapOf(
                                        "channelId" to channelId,
                                        "messageId" to lastMessageId,
                                        "readStateType" to 0
                                    ))
                                }
                            }
                        }
                    }
                    
                    // Dispatch a BULK_ACK event to mark all channels as read
                    StoreStream.getDispatcher().dispatch(mapOf(
                        "type" to "BULK_ACK",
                        "context" to "APP",
                        "channels" to channelsToAck
                    ))
                    
                    logger.info("Marked ${channelsToAck.size} channels as read across ${guildIds.size} guilds")
                    
                } catch (e: Exception) {
                    logger.error("Failed to mark guilds as read", e)
                }
            }
            CommandsAPI.CommandResult("Started marking guilds as read. This can take a while if you're in a lot of servers", null, false)
        }
    }

    override fun stop(ctx: Context) {
        commands.unregisterAll()
    }
}
