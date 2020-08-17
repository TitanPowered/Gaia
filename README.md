# Gaia

Gaia is an arena management plugin. It requires World Edit to create arena regions.
Gaia makes use of [PaperLib's](https://github.com/PaperMC/PaperLib) async chunk API.

## Creating an arena
1. Select a region with WorldEdit
2. Type and execute `/arena create <name>`
3. Wait for confirmation and you are done!

## Commands
- `/gaia version`: Show plugin version and author
- `/gaia list`: List all gaia arenas
- `/gaia create <name>`: Create a new arena with the specified name
- `/gaia remove <name>`: Remove the specified arena
- `/gaia revert <name>`: Revert the specified arena
- `/gaia info [name]`: Allows to see info about specific arenas
- `/gaia cancel [name]`: Allows cancelling arenas' revert tasks

## Permissions
- `gaia.admin`: Gives access to all Gaia commands
- `gaia.command.list`: Allows listing arenas
- `gaia.command.info`: Allows to see info about specific arenas
- `gaia.command.version`: Allows to see plugin version and author
- `gaia.command.create`: Allows creating arenas
- `gaia.command.remove`: Allows removing arenas
- `gaia.command.revert`: Allows reverting arenas
- `gaia.command.cancel`: Allows cancelling arenas' revert tasks

All commands except `/gaia version` default to OP only.
