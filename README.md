# GTNHLib subset fork

This fork supplies the GTNHLib classes that the [Angelica renderer fork](https://github.com/fopwoc/Angelica) needs:
the direct tessellators, vertex formats, VAO/VBO state, and the native memory utilities. It serves
[Julia for Project Zomboid](https://github.com/fopwoc/julia-pz) and is not a Minecraft mod anymore.

The Angelica fork pins this repository's `pz` branch under `third_party/gtnhlib/`, and Julia builds it from its own
root together with the renderer:

```sh
git submodule update --init --recursive
./java/gradlew -p java :gtnhlib:jar
```

Only part of this repository is compiled:

- `pz-sources.txt` lists the upstream files under `src/main/java` that are built.
- `src/pz/java` holds this fork's own classes. They are always built, and they replace any upstream class of the same
  name, such as Minecraft's tessellator API and the direct-capture part of `TessellatorManager`.

Everything else is upstream code that is never built here and is left as it is.

Original source notices and the [LGPL v3 license](LICENSE.txt) remain in force.
