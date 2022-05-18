
# Releasing
It's getting complicated so to avoid forgetting it let's write it down!

- Remove the snapshot related stuff in the `PublishConfig.artifactsVersion` variable.
  Remember that this change should not be committed!

- Run
```
./gradlew clean check :happy-annotation:publish
```
Yes! Don't publish them all in one `gradlew` run, to make sure with this delay Sonatype Nexus can handle them correctly!
Therefore, open the Nexus dashboard and close/release `happy-annotation` library.

- Run
```
./gradlew :happy-processor-common:publish
```
Then open the Nexus dashboard and close/release `happy-processor-common` library.

- Run
```
./gradlew :happy-processor:publish
```
Then open the Nexus dashboard and close/release `happy-processor` library.

- Run
```
./gradlew :happy-processor-ks:publish
```
Then open the Nexus dashboard and close/release `happy-processor-ks` library.

- Check the sample module with the published version by filling out the `RELEASE_VERSION` below and run it.
```
RELEASED_VERSION=.... &&
./gradlew clean :happy-sample:check -Phappy.snapshot.version="${RELEASED_VERSION}" -Phappy.ksp.enable=false --no-build-cache && \
./gradlew clean :happy-sample:check -Phappy.snapshot.version="${RELEASED_VERSION}" -Phappy.ksp.enable=true -Pksp.incremental=false --no-build-cache && \
./gradlew :happy-sample:check -Phappy.snapshot.version="${RELEASED_VERSION}" -Phappy.ksp.enable=true -Pksp.incremental=true --no-build-cache
```
It may take a while for the released artifact to be available out there, so be patient!

- Fill out the `CHANGELOG.md` file, bump up `PublishConfig.LIB_VERSION`, commit and push
  with `Released ${RELEASED_VERSION} and bump up to ${NEXT_VERSION}` commit message.

- Tag the commit and push it
```
git tag v${RELEASED_VERSION} && git push origin v${RELEASED_VERSION}
```

-Use the above tag to make a release in Github release page.
