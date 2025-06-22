package ch.kirby.commands;

import ch.kirby.SQL.DBConnection;
import ch.kirby.SQL.ReadFromSQL;
import ch.kirby.core.command.Command;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import reactor.core.publisher.Mono;

import java.sql.Connection;
import java.sql.SQLException;

public class StatsCommand implements Command {
    @Override
    public String getName() {
        return "stats";
    }

    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event) {
        // 1) Run your blocking JDBC code right here
        String replyContent;
        try {
            DBConnection dbConnection = new DBConnection();
            Connection conn = dbConnection.SQLDBConnection();
            ReadFromSQL reader = new ReadFromSQL(conn);
            replyContent = reader.readStats();
            conn.close();
        } catch (SQLException e) {
            replyContent = "⚠️ Error fetching stats: " + e.getMessage();
        }

        // 2) Send the reply immediately
        return event.reply()
                .withEphemeral(false)
                .withContent(replyContent);
    }
}
