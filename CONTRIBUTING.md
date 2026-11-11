# Contributing

- Build with JDK 25+: `./gradlew build` must pass (javac, Error Prone, NullAway and javadoc
  warnings fail the build).
- Try your change in game: `./gradlew runServer` starts a server with the kitchensink plugin (`/ks`).
- Code style: return early, full variable names, `final` on parameters and locals, `this.` for members, and an
  explicit `private` or `public` on every field. Classes are `@NullMarked`; mark nullable types `@Nullable`.
- Open an issue before large changes so we can agree on the approach.
