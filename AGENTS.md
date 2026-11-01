# GTNHLib, `pz` branch

This branch supplies the GTNHLib subset that the [Angelica `pz` fork](https://github.com/fopwoc/Angelica) compiles for
[Julia](https://github.com/fopwoc/julia-pz), a Project Zomboid mod. Julia's `:gtnhlib` module builds it; this
repository's own Gradle build is not used. Minecraft compatibility is not preserved: the subset targets LWJGL3 only
and has no Minecraft, Forge, or launcher classes.

## Submodule chain

- Julia pins Angelica at `third_party/angelica`, and Angelica pins this fork at `third_party/gtnhlib`. Clone and
  update with `--recursive`.
- Commit innermost first: here, then the pin bump in Angelica, then the pin bump in Julia.

## Upstream

- `upstream` is GTNewHorizons/GTNHLib. `pz` started from tag `0.11.36`, the version Angelica pins. When an Angelica
  merge bumps that version, merge the matching tag here, with `rerere` on. Never rebase or force-push `pz`: Angelica's
  history pins its commits.
- We do not open upstream PRs. Findings worth sharing go upstream as issues the user writes.

## What is built

- `pz-sources.txt` lists the upstream files under `src/main/java` that Julia compiles. Everything else is never
  built, may stay Minecraft-dependent, and must not be edited.
- `src/pz/java` holds this fork's own classes and is always compiled. A class there replaces the upstream class of
  the same name, which is then left out of `pz-sources.txt`.
- The list is the closure of what Angelica's `glsm` and `sdl-gpu` reach. When Angelica starts using another GTNHLib
  class, the build fails until the file is added. Add only what compiles without Minecraft, or replace it in
  `src/pz/java`.

## Zones

- **Listed**: upstream files in `pz-sources.txt`. Change them only as far as needed to drop Minecraft or LWJGL2: an
  import, a type, a dead Minecraft-only method. Match upstream style, and never reformat, reorder, or rename in them.
- **Owned**: everything in `src/pz/java`, plus `README.md`, `AGENTS.md`, and `pz-sources.txt`. Free to change. On an
  upstream conflict in `README.md`, keep ours.
- **Unlisted**: every other upstream file. Leave them untouched so merges stay clean.

## Owned classes

- `client.renderer.Tessellator`: the state and drawing methods of Minecraft's tessellator that the direct
  tessellators inherit and the vertex writers read. Its global `instance` is always `null`.
- `client.renderer.TessellatorManager`: only upstream's direct-capture stack, with its own buffer standing in for
  Minecraft's shared one. Upstream's version also captures Minecraft's global tessellator.
- `client.renderer.CapturingTessellator`: only its `Flags`, which the quad and triangle models carry.

## Edits in listed files

Keep this list current. One line per file or group: what changed and why.

- `DirectTessellator`: extends the owned `Tessellator`; `syncTessellator` accepts no global tessellator (`null`);
  `getVertexState`/`setVertexState` removed with Minecraft's vertex-state type.
- `VertexFlags`, `VertexFormat`, `NormalHelper`, and the six `vertex.writers` classes: import the owned `Tessellator`.
- `ModelQuadFacing`: Forge and Minecraft direction conversions removed.
- `GLCaps`, `UniversalVAO`: LWJGL3 `GLCapabilities` from `GL.getCapabilities()`.
- `MemoryManage`: always the standard allocator; the lwjgl3ify launcher check is gone.
- `MemoryUtilities`: LWJGL3 `PointerBuffer.create` and `address`.
- `Pointer`: pointer size from LWJGL3's `org.lwjgl.system.Pointer`.
- `IVertexBuffer`: LWJGL3 `glMultMatrixf`.
- `CheckIntrinsics.NewDirectByteBuffer`: uses LWJGL3 `MemoryUtil.memByteBuffer`; the private Java 8 constructor
  is absent on current JDKs.
