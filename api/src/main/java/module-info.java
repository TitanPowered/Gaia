module me.moros.gaia {
  exports me.moros.gaia.api;
  exports me.moros.gaia.api.arena;
  exports me.moros.gaia.api.arena.region;
  exports me.moros.gaia.api.chunk;
  exports me.moros.gaia.api.event;
  exports me.moros.gaia.api.operation;
  exports me.moros.gaia.api.platform;
  exports me.moros.gaia.api.service;
  exports me.moros.gaia.api.storage;
  exports me.moros.gaia.api.util;
  exports me.moros.gaia.api.util.supplier;

  requires transitive me.moros.tasker;
  requires transitive me.moros.math;
  requires static transitive net.kyori.adventure;
  requires static transitive net.kyori.adventure.key;
  requires static net.kyori.examination.api;
  requires static org.checkerframework.checker.qual;
}
