# Gaia

## Requirements
- `Java 8 or newer`
- `WorldEdit 7.0.0 or newer`

Gaia is a lightweight arena management plugin.
Gaia makes use of [PaperLib's](https://github.com/PaperMC/PaperLib) async chunk API.
While it will work with Spigot, to ensure smoothness and to take advantage of all of Gaia's async features
you should use PaperMC.

## Creating an arena
1. Select a region with WorldEdit
2. Type and execute `/arena create <name>`
3. Wait for confirmation and you are done!

## A couple notes
Gaia has been tested with PaperMC 1.16.x but should work with other versions (within reason).
Arenas are automatically split into sub-regions which are saved individually on your server's storage.
It's recommended you use an SSD to ensure disk IO is not bottlenecking Gaia.
Only basic information about arenas is kept in memory, making the plugin extremely lightweight.

When reverting an arena, Gaia verifies the file integrity for each individual saved sub-region.
This ensures that any corrupted data will NOT affect your world.
Any regions that can't be verified will be ignored while the rest will be reverted normally.

## Commands and Permissions
All commands except `/gaia version` default to OP only.

`gaia.admin`: Gives access to all Gaia commands

| Command               | Permission             | Description                                |
|-----------------------|------------------------|--------------------------------------------|
| `/gaia help`          | `gaia.command.help`    | View list of Gaia commands                 |
| `/gaia version`       | `gaia.command.version` | Show plugin version and author             |
| `/gaia list`          | `gaia.command.list`    | List all Gaia arenas                       |
| `/gaia info [name]`   | `gaia.command.info`    | View info about the specified arena        |
| `/gaia create <name>` | `gaia.command.create`  | Create a new arena with the specified name |
| `/gaia remove <name>` | `gaia.command.remove`  | Remove the specified arena                 |
| `/gaia revert <name>` | `gaia.command.revert`  | Revert the specified arena                 |
| `/gaia cancel [name]` | `gaia.command.cancel`  | Cancel reverting the the specified arena   |
