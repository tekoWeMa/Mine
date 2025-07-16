# Discord Stats Bot

<img src="./assets/Mine.png" alt="Bot Profile Picture" width="120" height="120">


## Overview

This Java-based bot uses the Discord4J library to display user activity statistics directly inside Discord via slash commands and interactive buttons.

**Note:** This bot does not log any data on its own. It depends entirely on a separate logging bot called **Sugu**, which collects and stores activity data in a MariaDB database. This bot reads and visualizes that data inside Discord.

---

## Features

- **Slash Commands**
  - `/stats [user] [dayspan]`: Show activity statistics for a user
  - `/leaderboard [game] [dayspan]`: Leaderboard of top users for a given game
  - `/spotify [user] [dayspan]`: Show top songs and artists for a user

- **Button Interactions**
  - Interactive `dayspan` buttons allow quick timespan switching (e.g., 7, 14, 30 days)
  - Button IDs follow pattern: `command_dayspan_N` (e.g., `stats_dayspan_7`)

- **Statistical Analysis**
  - Uses MariaDB and SQL queries to track user activity
  - Supports detailed leaderboard rankings and personal summaries

---

## Architecture

| Component        | Purpose                             |
|------------------|--------------------------------------|
| Java             | Core logic                           |
| Discord4J        | Discord API binding                  |
| MariaDB          | Source of stored activity data       |
| Docker           | Containerization and deployment      |

---

## Dependency

This bot depends on the [Sugu Bot](https://github.com/tekoWeMa/sugu), which must be running and logging activity data to the same MariaDB database.

**Sugu** handles:

- Tracking presence states (online, idle, do-not-disturb)
- Recording game sessions with durations
- Logging Spotify activity (song, artist, listening time)
- Persisting activity logs to a relational schema

This bot connects to that same schema and renders the data in a user-friendly way through Discord.

---

## Docker Compose

```yaml
version: '3.8'

services:
  bot:
    build: .
    environment:
      - DISCORD_CLIENT_TOKEN_MINE=your-discord-token
      - DB_HOST_MINE=mariadb
      - DB_USERNAME_MINE=sugu
      - DB_PASSWORD_MINE=yourpassword
    restart: unless-stopped
