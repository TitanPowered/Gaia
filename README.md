<h1 align="center">
<br>
Gaia
<br>
</h1>

<p align="center">
<a href="https://github.com/PrimordialMoros/Gaia/actions"><img src="https://img.shields.io/github/actions/workflow/status/PrimordialMoros/Gaia/gradle.yml?branch=master&style=flat-square" alt="Gradle CI"/></a>
<a href="LICENSE"><img src="https://img.shields.io/github/license/PrimordialMoros/Gaia?color=blue&style=flat-square" alt="License"/></a>
<a href="https://github.com/PrimordialMoros/Gaia/wiki/Home"><img src="https://img.shields.io/badge/docs-wiki-informational?style=flat-square" alt="Wiki"/></a>
<a href="https://github.com/PrimordialMoros/Gaia/releases"><img src="https://img.shields.io/github/v/release/PrimordialMoros/Gaia?color=009185&style=flat-square" alt="Github release"/></a>
</p>

#### What is Gaia?
Gaia is a lightweight arena management plugin for Minecraft servers.

It allows you to create snapshots of areas that you can later restore.

#### Main Features:
- Define arena regions
- Restore arena regions to their original state
- Teleport to defined [[arena points|Getting-Started#Arena-Points]]
- Extremely efficient and lightweight
- Extensible API
- Support for custom translations and [[Localization]]

You can read about how it works in [Technical Details](TECHNICAL_DETAILS.md).

## Building

This project requires Java 17 or newer and uses Gradle (which comes with a wrapper, so you don't need to install it).

Open a terminal and run `./gradlew build`.

## Contributing - Developer API

Any contributions, large or small, major features, bug fixes, unit/integration tests are welcome and appreciated.

For information on how to use Gaia in your own projects check the [Developer API](https://github.com/PrimordialMoros/Gaia/wiki/Developer-API) wiki.
