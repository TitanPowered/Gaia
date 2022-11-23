# Gaia

[![Gradle CI](https://img.shields.io/github/workflow/status/PrimordialMoros/Gaia/Build?style=flat-square)](https://github.com/PrimordialMoros/Gaia/actions)
[![License](https://img.shields.io/github/license/PrimordialMoros/Gaia?color=blue&style=flat-square)](LICENSE)
[![GitHub release](https://img.shields.io/github/v/release/PrimordialMoros/Gaia?style=flat-square)](https://github.com/PrimordialMoros/Gaia/releases)

## Requirements
- `Java 17 or newer`
- `PaperMC 1.18.2 or newer`
- `WorldEdit 7.2.6 or newer`

Gaia is a lightweight arena management plugin.
Gaia makes use of [Paper's](https://github.com/PaperMC/Paper) async chunk API.

## Creating an arena
1. Select a region with WorldEdit
2. Type and execute `/arena create <name>`
3. Wait for confirmation and you are done!

## A couple notes
Arenas are automatically split into sub-regions which are saved individually on your server's storage.
It's recommended you use an SSD to ensure disk IO is not bottlenecking Gaia.
Only basic information about arenas is kept in memory, making the plugin extremely lightweight.

When reverting an arena, Gaia verifies the file integrity for each individual saved sub-region.
This ensures that any corrupted data will NOT affect your world.
Any regions that can't be verified will be ignored while the rest will be reverted normally.

## Commands and Permissions
All commands except `/gaia version` default to OP only.

`gaia.admin`: Gives access to all Gaia commands

If no arena is specified, Gaia will attempt to select the one the player is currently standing in.
If no point is specified, Gaia will pick a random point from the ones available in the specified arena.

| Command                       | Permission              | Description                                       |
|-------------------------------|-------------------------|---------------------------------------------------|
| `/gaia help`                  | `gaia.command.help`     | View list of Gaia commands                        |
| `/gaia version`               | `gaia.command.version`  | Show plugin version and author                    |
| `/gaia reload`                | `gaia.command.reload`   | Reload the plugin and its config                  |
| `/gaia list [page]`           | `gaia.command.list`     | List all Gaia arenas                              |
| `/gaia info [arena]`          | `gaia.command.info`     | View info about the specified arena               |
| `/gaia remove [arena]`        | `gaia.command.remove`   | Remove the specified arena                        |
| `/gaia revert [arena]`        | `gaia.command.revert`   | Revert the specified arena                        |
| `/gaia cancel [arena]`        | `gaia.command.cancel`   | Cancel reverting the specified arena              |
| `/gaia create <name>`         | `gaia.command.create`   | Create a new arena with the specified name        |
| `/gaia addpoint`              | `gaia.command.point`    | Add a new point to the arena you are currently in |
| `/gaia clearpoints [arena]`   | `gaia.command.point`    | Clear all points for the specified arena          |
| `/gaia teleport [arena] [id]` | `gaia.command.teleport` | Teleport to a point in the specified arena        |
